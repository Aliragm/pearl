package main

import (
	"bufio"
	"context"
	"errors"
	"log"
	"net"
	"os"
	"strings"

	igdbClient "github.com/Aliragm/pearl/services/ingestor-go/client"
	"github.com/Aliragm/pearl/services/ingestor-go/domain"
	pb "github.com/Aliragm/pearl/services/ingestor-go/proto"
	"google.golang.org/grpc"
	"google.golang.org/grpc/codes"
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

func (s *server) Search(ctx context.Context, req *pb.SearchGameRequest) (*pb.SearchGameResponse, error) {
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

	return &pb.SearchGameResponse{
		Games: pbGames,
	}, nil
}

func main() {
	loadEnv()

	lis, err := net.Listen("tcp", ":8081")
	if err != nil {
		panic(err)
	}

	s := grpc.NewServer()
	pb.RegisterGameServiceServer(s, &server{
		searcher: igdbClient.NewIGDBHttpClient(),
	})
	reflection.Register(s)
	log.Printf("gRPC server listening at %v", lis.Addr())
	if err := s.Serve(lis); err != nil {
		log.Fatalf("failed to serve: %v", err)
	}
}
