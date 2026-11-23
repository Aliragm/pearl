package client

import (
	"context"
	"errors"
	"io"
	"net/http"
	"net/http/httptest"
	"reflect"
	"strings"
	"testing"
	"time"

	"github.com/Aliragm/pearl/services/ingestor-go/domain"
)

func TestIGDBHttpClient_Search_Success(t *testing.T) {
	var capturedMethod string
	var capturedBody string

	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		capturedMethod = r.Method
		bodyBytes, err := io.ReadAll(r.Body)
		if err != nil {
			t.Fatalf("failed reading request body: %v", err)
		}
		capturedBody = string(bodyBytes)

		w.Header().Set("Content-Type", "application/json")
		_, _ = w.Write([]byte(`[
			{
				"id": 1022,
				"name": "The Legend of Zelda",
				"summary": "Classic adventure game.",
				"cover": {"id": 86202, "image_id": "co1uii"},
				"platforms": [
					{"id": 51, "name": "Family Computer Disk System"},
					{"id": 37, "name": "Nintendo 3DS"},
					{"id": 5, "name": "Wii"},
					{"id": 99, "name": "Family Computer"},
					{"id": 41, "name": "Wii U"},
					{"id": 18, "name": "Nintendo Entertainment System"}
				],
				"involved_companies": [
					{"id": 180342, "company": {"id": 70, "name": "Nintendo"}},
					{"id": 197987, "company": {"id": 32471, "name": "Nintendo R&D4"}}
				]
			},
			{
				"id": 2000,
				"name": "Multi Platform Title",
				"summary": "Runs everywhere.",
				"platforms": [
					{"id": 6, "name": "PC (Microsoft Windows)"},
					{"id": 167, "name": "PlayStation 5"},
					{"id": 169, "name": "Xbox Series X|S"},
					{"id": 34, "name": "Android"},
					{"id": 29, "name": "Sega Genesis"}
				],
				"involved_companies": [
					{"id": 1, "company": {"id": 2, "name": "Indie Studio"}}
				]
			}
		]`))
	}))
	defer srv.Close()

	c := &IGDBHttpClient{
		httpClient: srv.Client(),
		baseURL:    srv.URL,
	}

	games, err := c.Search(context.Background(), "zelda")
	if err != nil {
		t.Fatalf("Search returned unexpected error: %v", err)
	}

	if capturedMethod != http.MethodPost {
		t.Errorf("expected HTTP POST, got %s", capturedMethod)
	}
	if !strings.Contains(capturedBody, `search "zelda";`) {
		t.Errorf("expected query to contain search term, got %q", capturedBody)
	}

	want := []domain.Game{
		{
			ID:        1022,
			Name:      "The Legend of Zelda",
			Summary:   "Classic adventure game.",
			Platforms: []string{domain.PlatformNintendo},
			Companies: []string{"Nintendo", "Nintendo R&D4"},
			CoverURL:  "https://images.igdb.com/igdb/image/upload/t_cover_big/co1uii.jpg",
		},
		{
			ID:        2000,
			Name:      "Multi Platform Title",
			Summary:   "Runs everywhere.",
			Platforms: []string{domain.PlatformPC, domain.PlatformPlayStation, domain.PlatformXbox},
			Companies: []string{"Indie Studio"},
			CoverURL:  "",
		},
	}

	if !reflect.DeepEqual(games, want) {
		t.Fatalf("Search() = %+v, want %+v", games, want)
	}
}

func TestIGDBHttpClient_Search_EmptySearch(t *testing.T) {
	c := NewIGDBHttpClient()

	for _, input := range []string{"", "   \t\n"} {
		_, err := c.Search(context.Background(), input)
		if !errors.Is(err, domain.ErrEmptySearch) {
			t.Fatalf("Search(%q) error = %v, want errors.Is(err, domain.ErrEmptySearch)", input, err)
		}
	}
}

func TestIGDBHttpClient_Search_ContextTimeout(t *testing.T) {
	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		select {
		case <-r.Context().Done():
		case <-time.After(300 * time.Millisecond):
		}
	}))
	defer srv.Close()

	c := &IGDBHttpClient{
		httpClient: srv.Client(),
		baseURL:    srv.URL,
	}

	ctx, cancel := context.WithTimeout(context.Background(), 30*time.Millisecond)
	defer cancel()

	_, err := c.Search(ctx, "zelda")
	if !errors.Is(err, context.DeadlineExceeded) {
		t.Fatalf("expected context.DeadlineExceeded, got %v", err)
	}
	if !errors.Is(err, domain.ErrExternalAPI) {
		t.Fatalf("expected domain.ErrExternalAPI, got %v", err)
	}
}

func TestIGDBHttpClient_Search_ExternalAPIStatusError(t *testing.T) {
	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusInternalServerError)
		_, _ = w.Write([]byte("internal error"))
	}))
	defer srv.Close()

	c := &IGDBHttpClient{
		httpClient: srv.Client(),
		baseURL:    srv.URL,
	}

	_, err := c.Search(context.Background(), "zelda")
	if !errors.Is(err, domain.ErrExternalAPI) {
		t.Fatalf("expected domain.ErrExternalAPI, got %v", err)
	}
}
