package client

import (
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"os"
	"time"

	"github.com/Aliragm/pearl/services/ingestor-go/domain"
)

func IgdbSearch(ctx context.Context, searchString string) ([]domain.Game, error) {
	query := fmt.Sprintf("search %q; "+
		"fields name, involved_companies.company.name, platforms.name, summary, cover.image_id; "+
		"where game_type = 0; "+
		"limit 20;", searchString)

	requestBody := bytes.NewBufferString(query)

	httpReq, err := http.NewRequestWithContext(ctx, http.MethodPost, "https://api.igdb.com/v4/games", requestBody)
	if err != nil {
		return nil, fmt.Errorf("failed to create request: %w", err)
	}
	httpReq.Header.Add("Client-ID", os.Getenv("CLIENT_ID_TWITCH"))
	httpReq.Header.Add("Authorization", "Bearer "+os.Getenv("ACCESS_TOKEN"))
	httpReq.Header.Add("Content-Type", "text/plain")

	client := &http.Client{Timeout: time.Second * 10}
	response, err := client.Do(httpReq)
	if err != nil {
		return nil, fmt.Errorf("request to IGDB failed: %w", err)
	}
	defer response.Body.Close()

	if response.StatusCode != http.StatusOK {
		bodyBytes, _ := io.ReadAll(response.Body)
		return nil, fmt.Errorf("unexpected status code: %d (%s)", response.StatusCode, string(bodyBytes))
	}

	var igdbGames []domain.IGDBGame
	if err := json.NewDecoder(response.Body).Decode(&igdbGames); err != nil {
		return nil, fmt.Errorf("failed to decode IGDB response: %w", err)
	}

	games := make([]domain.Game, 0, len(igdbGames))
	for _, g := range igdbGames {
		platforms := make([]string, 0, len(g.Platforms))
		for _, p := range g.Platforms {
			if p.Name != "" {
				platforms = append(platforms, p.Name)
			}
		}

		companies := make([]string, 0, len(g.InvolvedCompanies))
		for _, ic := range g.InvolvedCompanies {
			if ic.Company.Name != "" {
				companies = append(companies, ic.Company.Name)
			}
		}

		var coverURL string
		if g.Cover != nil && g.Cover.ImageID != "" {
			coverURL = fmt.Sprintf("https://images.igdb.com/igdb/image/upload/t_cover_big/%s.jpg", g.Cover.ImageID)
		}

		games = append(games, domain.Game{
			ID:        g.ID,
			Name:      g.Name,
			Summary:   g.Summary,
			Platforms: platforms,
			Companies: companies,
			CoverURL:  coverURL,
		})
	}
	return games, nil
}
