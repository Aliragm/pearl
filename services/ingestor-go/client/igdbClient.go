package client

import (
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"os"
	"strings"
	"time"

	"github.com/Aliragm/pearl/services/ingestor-go/domain"
)

type IGDBHttpClient struct {
	httpClient *http.Client
	baseURL    string
}

func NewIGDBHttpClient() *IGDBHttpClient {
	return &IGDBHttpClient{
		httpClient: &http.Client{Timeout: time.Second * 10},
		baseURL:    "https://api.igdb.com/v4/games",
	}
}

func (c *IGDBHttpClient) Search(ctx context.Context, searchString string) ([]domain.Game, error) {
	if strings.TrimSpace(searchString) == "" {
		return nil, fmt.Errorf("%w: empty search term", domain.ErrEmptySearch)
	}

	query := fmt.Sprintf("search %q; "+
		"fields name, involved_companies.company.name, platforms.name, summary, cover.image_id; "+
		"where game_type = 0; "+
		"limit 20;", searchString)

	requestBody := bytes.NewBufferString(query)

	httpReq, err := http.NewRequestWithContext(ctx, http.MethodPost, c.baseURL, requestBody)
	if err != nil {
		return nil, fmt.Errorf("%w: failed to create request: %w", domain.ErrExternalAPI, err)
	}
	httpReq.Header.Add("Client-ID", os.Getenv("CLIENT_ID_TWITCH"))
	httpReq.Header.Add("Authorization", "Bearer "+os.Getenv("ACCESS_TOKEN"))
	httpReq.Header.Add("Content-Type", "text/plain")

	response, err := c.httpClient.Do(httpReq)
	if err != nil {
		return nil, fmt.Errorf("%w: request to IGDB failed: %w", domain.ErrExternalAPI, err)
	}
	defer response.Body.Close()

	if response.StatusCode != http.StatusOK {
		bodyBytes, _ := io.ReadAll(response.Body)
		return nil, fmt.Errorf("%w: unexpected status code %d (%s)", domain.ErrExternalAPI, response.StatusCode, string(bodyBytes))
	}

	var igdbGames []domain.IGDBGame
	if err := json.NewDecoder(response.Body).Decode(&igdbGames); err != nil {
		return nil, fmt.Errorf("%w: failed to decode IGDB response: %w", domain.ErrExternalAPI, err)
	}

	games := make([]domain.Game, 0, len(igdbGames))
	for _, g := range igdbGames {
		platforms := domain.NormalizePlatforms(g.Platforms)

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
