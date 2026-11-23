package main

import (
	"context"
	"fmt"
	"net"
	"reflect"
	"testing"
	"time"

	"github.com/Aliragm/pearl/services/ingestor-go/domain"
	pb "github.com/Aliragm/pearl/services/ingestor-go/proto/pearl/catalog/v1"
	"google.golang.org/grpc"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/credentials/insecure"
	healthpb "google.golang.org/grpc/health/grpc_health_v1"
	"google.golang.org/grpc/status"
	"google.golang.org/grpc/test/bufconn"
)

type mockGameSearcher struct {
	searchFunc func(ctx context.Context, searchString string) ([]domain.Game, error)
}

func (m *mockGameSearcher) Search(ctx context.Context, searchString string) ([]domain.Game, error) {
	return m.searchFunc(ctx, searchString)
}

func setupBufconnServer(t *testing.T, searcher domain.GameSearcher) *grpc.ClientConn {
	t.Helper()

	const bufSize = 1024 * 1024
	lis := bufconn.Listen(bufSize)
	srv, _ := newGRPCServer(searcher)

	go func() {
		_ = srv.Serve(lis)
	}()

	conn, err := grpc.NewClient(
		"passthrough:///bufnet",
		grpc.WithContextDialer(func(ctx context.Context, _ string) (net.Conn, error) {
			return lis.DialContext(ctx)
		}),
		grpc.WithTransportCredentials(insecure.NewCredentials()),
	)
	if err != nil {
		srv.Stop()
		_ = lis.Close()
		t.Fatalf("failed to dial bufnet: %v", err)
	}

	t.Cleanup(func() {
		_ = conn.Close()
		srv.GracefulStop()
		_ = lis.Close()
	})

	return conn
}

func TestGRPCServer_Search_Success(t *testing.T) {
	searcher := &mockGameSearcher{
		searchFunc: func(ctx context.Context, searchString string) ([]domain.Game, error) {
			if searchString != "zelda" {
				t.Fatalf("expected searchString %q, got %q", "zelda", searchString)
			}
			return []domain.Game{
				{
					ID:        1022,
					Name:      "The Legend of Zelda",
					Summary:   "Adventure classic.",
					Platforms: []string{domain.PlatformNintendo},
					Companies: []string{"Nintendo"},
					CoverURL:  "https://images.igdb.com/igdb/image/upload/t_cover_big/co1uii.jpg",
				},
			}, nil
		},
	}

	conn := setupBufconnServer(t, searcher)
	client := pb.NewGameServiceClient(conn)

	resp, err := client.Search(context.Background(), &pb.SearchRequest{SearchString: "zelda"})
	if err != nil {
		t.Fatalf("Search RPC failed: %v", err)
	}

	if len(resp.GetGames()) != 1 {
		t.Fatalf("expected 1 game, got %d", len(resp.GetGames()))
	}

	got := resp.GetGames()[0]
	if got.GetId() != 1022 ||
		got.GetName() != "The Legend of Zelda" ||
		got.GetSummary() != "Adventure classic." ||
		!reflect.DeepEqual(got.GetPlatforms(), []string{domain.PlatformNintendo}) ||
		!reflect.DeepEqual(got.GetCompanies(), []string{"Nintendo"}) ||
		got.GetCoverUrl() != "https://images.igdb.com/igdb/image/upload/t_cover_big/co1uii.jpg" {
		t.Fatalf("unexpected game response: %+v", got)
	}
}

func TestGRPCServer_Search_EmptySearch(t *testing.T) {
	searcher := &mockGameSearcher{
		searchFunc: func(ctx context.Context, searchString string) ([]domain.Game, error) {
			return nil, fmt.Errorf("%w: empty search term", domain.ErrEmptySearch)
		},
	}

	conn := setupBufconnServer(t, searcher)
	client := pb.NewGameServiceClient(conn)

	_, err := client.Search(context.Background(), &pb.SearchRequest{SearchString: ""})
	if status.Code(err) != codes.InvalidArgument {
		t.Fatalf("expected status code %v, got %v (err: %v)", codes.InvalidArgument, status.Code(err), err)
	}
}

func TestGRPCServer_Search_ContextTimeout(t *testing.T) {
	searcher := &mockGameSearcher{
		searchFunc: func(ctx context.Context, searchString string) ([]domain.Game, error) {
			<-ctx.Done()
			return nil, ctx.Err()
		},
	}

	conn := setupBufconnServer(t, searcher)
	client := pb.NewGameServiceClient(conn)

	ctx, cancel := context.WithTimeout(context.Background(), 20*time.Millisecond)
	defer cancel()

	_, err := client.Search(ctx, &pb.SearchRequest{SearchString: "zelda"})
	if status.Code(err) != codes.DeadlineExceeded {
		t.Fatalf("expected status code %v, got %v (err: %v)", codes.DeadlineExceeded, status.Code(err), err)
	}
}

func TestGRPCServer_Search_Unavailable(t *testing.T) {
	searcher := &mockGameSearcher{
		searchFunc: func(ctx context.Context, searchString string) ([]domain.Game, error) {
			return nil, fmt.Errorf("%w: status 503", domain.ErrExternalAPI)
		},
	}

	conn := setupBufconnServer(t, searcher)
	client := pb.NewGameServiceClient(conn)

	_, err := client.Search(context.Background(), &pb.SearchRequest{SearchString: "zelda"})
	if status.Code(err) != codes.Unavailable {
		t.Fatalf("expected status code %v, got %v (err: %v)", codes.Unavailable, status.Code(err), err)
	}
}

func TestGRPCServer_HealthCheck(t *testing.T) {
	searcher := &mockGameSearcher{
		searchFunc: func(ctx context.Context, searchString string) ([]domain.Game, error) {
			return nil, nil
		},
	}

	conn := setupBufconnServer(t, searcher)
	healthClient := healthpb.NewHealthClient(conn)

	for _, svc := range []string{"", pb.GameService_ServiceDesc.ServiceName} {
		resp, err := healthClient.Check(context.Background(), &healthpb.HealthCheckRequest{Service: svc})
		if err != nil {
			t.Fatalf("Health.Check(%q) failed: %v", svc, err)
		}
		if resp.GetStatus() != healthpb.HealthCheckResponse_SERVING {
			t.Fatalf("Health.Check(%q) = %v, want SERVING", svc, resp.GetStatus())
		}
	}
}
