package main

import (
	"bufio"
	"context"
	"errors"
	"log"
	"net"
	"os"
	"os/signal"
	"strings"
	"syscall"

	igdbClient "github.com/Aliragm/pearl/services/ingestor-go/client"
	"github.com/Aliragm/pearl/services/ingestor-go/domain"
	pb "github.com/Aliragm/pearl/services/ingestor-go/proto/pearl/catalog/v1"
	"google.golang.org/grpc"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/health"
	healthpb "google.golang.org/grpc/health/grpc_health_v1"
	"google.golang.org/grpc/reflection"
	"google.golang.org/grpc/status"
)

func loadEnv() {
	paths := []string{".env", "../.env", "../../.env"}
	for _, p := range paths {
		file, err := os.Open(p)
		if err != nil {
			continue
		}
		defer file.Close()

		scanner := bufio.NewScanner(file)
		for scanner.Scan() {
			line := strings.TrimSpace(scanner.Text())
			if line == "" || strings.HasPrefix(line, "#") {
				continue
			}
			key, val, ok := strings.Cut(line, "=")
			if !ok {
				continue
			}
			key = strings.TrimSpace(key)
			val = strings.Trim(strings.TrimSpace(val), `"'`)
			if os.Getenv(key) == "" {
				_ = os.Setenv(key, val)
			}
		}
		log.Printf("Loaded environment variables from %s", p)
		return
	}
}

type server struct {
	pb.UnimplementedGameServiceServer
	searcher domain.GameSearcher
}

func (s *server) Search(ctx context.Context, req *pb.SearchRequest) (*pb.SearchResponse, error) {
	searchString := req.GetSearchString()
	domainGames, err := s.searcher.Search(ctx, searchString)
	if err != nil {
		if errors.Is(err, domain.ErrEmptySearch) {
			return nil, status.Error(codes.InvalidArgument, err.Error())
		}
		if errors.Is(err, context.DeadlineExceeded) {
			return nil, status.Error(codes.DeadlineExceeded, err.Error())
		}
		if errors.Is(err, context.Canceled) {
			return nil, status.Error(codes.Canceled, err.Error())
		}
		return nil, status.Error(codes.Unavailable, err.Error())
	}

	pbGames := make([]*pb.Game, 0, len(domainGames))
	for _, g := range domainGames {
		pbGames = append(pbGames, &pb.Game{
			Id:        g.ID,
			Name:      g.Name,
			Summary:   g.Summary,
			Platforms: g.Platforms,
			Companies: g.Companies,
			CoverUrl:  g.CoverURL,
		})
	}

	return &pb.SearchResponse{
		Games: pbGames,
	}, nil
}

func newGRPCServer(searcher domain.GameSearcher) (*grpc.Server, *health.Server) {
	s := grpc.NewServer()
	pb.RegisterGameServiceServer(s, &server{
		searcher: searcher,
	})

	healthServer := health.NewServer()
	healthServer.SetServingStatus("", healthpb.HealthCheckResponse_SERVING)
	healthServer.SetServingStatus(pb.GameService_ServiceDesc.ServiceName, healthpb.HealthCheckResponse_SERVING)
	healthpb.RegisterHealthServer(s, healthServer)

	reflection.Register(s)
	return s, healthServer
}

func main() {
	loadEnv()

	lis, err := net.Listen("tcp", ":8081")
	if err != nil {
		log.Fatalf("failed to listen: %v", err)
	}

	s, healthServer := newGRPCServer(igdbClient.NewIGDBHttpClient())

	sigCtx, stop := signal.NotifyContext(context.Background(), os.Interrupt, syscall.SIGTERM)
	defer stop()

	serveErr := make(chan error, 1)
	go func() {
		log.Printf("gRPC server listening at %v", lis.Addr())
		serveErr <- s.Serve(lis)
	}()

	select {
	case err := <-serveErr:
		if err != nil {
			log.Fatalf("failed to serve: %v", err)
		}
	case <-sigCtx.Done():
		log.Println("Shutdown signal received; stopping gRPC server gracefully...")
		healthServer.Shutdown()
		s.GracefulStop()
		if err := <-serveErr; err != nil {
			log.Fatalf("failed during graceful shutdown: %v", err)
		}
		log.Println("gRPC server stopped cleanly.")
	}
}
