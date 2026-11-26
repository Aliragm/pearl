package domain

import (
	"context"
	"errors"
	"strings"
)

const (
	PlatformPlayStation = "PLAYSTATION"
	PlatformXbox        = "XBOX"
	PlatformPC          = "PC"
	PlatformNintendo    = "NINTENDO"
)

var (
	ErrEmptySearch = errors.New("search string cannot be empty")
	ErrExternalAPI = errors.New("failed to fetch games from external API")
)

// NormalizePlatform mapeia nomes de plataformas da IGDB para os valores aceitos pelo PostgreSQL.
func NormalizePlatform(raw string) (string, bool) {
	p := strings.ToLower(strings.TrimSpace(raw))
	if p == "" {
		return "", false
	}

	switch {
	case containsAny(p, "playstation", "ps1", "ps2", "ps3", "ps4", "ps5", "ps vita", "psp", "pocketstation"):
		return PlatformPlayStation, true
	case strings.Contains(p, "xbox"):
		return PlatformXbox, true
	case containsAny(p, "nintendo", "wii", "gamecube", "game boy", "gameboy", "famicom", "family computer", "virtual boy", "satellaview", "switch") ||
		equalsAny(p, "nes", "snes", "n64", "nds", "3ds", "gba", "gbc", "gb"):
		return PlatformNintendo, true
	case p == "pc" || p == "mac" || p == "dos" ||
		strings.HasPrefix(p, "pc ") || strings.HasPrefix(p, "pc(") || strings.HasPrefix(p, "pc-") ||
		containsAny(p, "windows", "linux", "macos", "mac os", "macintosh", "ms-dos", "steamos", "steam os"):
		return PlatformPC, true
	default:
		return "", false
	}
}

// NormalizePlatforms normaliza e remove duplicatas de plataformas preservando a ordem original.
func NormalizePlatforms(raw []IGDBPlatform) []string {
	seen := make(map[string]struct{}, len(raw))
	normalized := make([]string, 0, len(raw))
	for _, p := range raw {
		canon, ok := NormalizePlatform(p.Name)
		if !ok {
			continue
		}
		if _, exists := seen[canon]; exists {
			continue
		}
		seen[canon] = struct{}{}
		normalized = append(normalized, canon)
	}
	return normalized
}

func containsAny(s string, substrs ...string) bool {
	for _, sub := range substrs {
		if strings.Contains(s, sub) {
			return true
		}
	}
	return false
}

func equalsAny(s string, candidates ...string) bool {
	for _, c := range candidates {
		if s == c {
			return true
		}
	}
	return false
}

type GameSearcher interface {
	Search(ctx context.Context, searchString string) ([]Game, error)
}

type Game struct {
	ID        int32
	Name      string
	Summary   string
	Platforms []string
	Companies []string
	CoverURL  string
}

type IGDBGame struct {
	ID                int32                 `json:"id"`
	Name              string                `json:"name"`
	Summary           string                `json:"summary"`
	Cover             *IGDBCover            `json:"cover"`
	Platforms         []IGDBPlatform        `json:"platforms"`
	InvolvedCompanies []IGDBInvolvedCompany `json:"involved_companies"`
}

type IGDBCover struct {
	ID      int32  `json:"id"`
	ImageID string `json:"image_id"`
}

type IGDBPlatform struct {
	ID   int32  `json:"id"`
	Name string `json:"name"`
}

type IGDBInvolvedCompany struct {
	ID      int32       `json:"id"`
	Company IGDBCompany `json:"company"`
}

type IGDBCompany struct {
	ID   int32  `json:"id"`
	Name string `json:"name"`
}
