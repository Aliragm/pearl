package main

import (
	"bufio"
	"context"
	"log"
	"net"
	"os"
	"strings"

	igdbClient "github.com/Aliragm/pearl/services/ingestor-go/client"
	pb "github.com/Aliragm/pearl/services/ingestor-go/proto"
	"google.golang.org/grpc"
	"google.golang.org/grpc/reflection"
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
}

func (s *server) Search(ctx context.Context, req *pb.SearchGameRequest) (*pb.SearchGameResponse, error) {
	searchString := req.GetSearchString()
	domainGames, err := igdbClient.IgdbSearch(ctx, searchString)
	if err != nil {
		return nil, err
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
	pb.RegisterGameServiceServer(s, &server{})
	reflection.Register(s)
	log.Printf("gRPC server listening at %v", lis.Addr())
	if err := s.Serve(lis); err != nil {
		log.Fatalf("failed to serve: %v", err)
	}
}
