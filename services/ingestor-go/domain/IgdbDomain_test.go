package domain

import (
	"reflect"
	"testing"
)

func TestNormalizePlatform(t *testing.T) {
	tests := []struct {
		name   string
		raw    string
		want   string
		wantOK bool
	}{
		// Nintendo
		{name: "FDS", raw: "Family Computer Disk System", want: PlatformNintendo, wantOK: true},
		{name: "3DS", raw: "Nintendo 3DS", want: PlatformNintendo, wantOK: true},
		{name: "Wii", raw: "Wii", want: PlatformNintendo, wantOK: true},
		{name: "Wii U", raw: "Wii U", want: PlatformNintendo, wantOK: true},
		{name: "Family Computer", raw: "Family Computer", want: PlatformNintendo, wantOK: true},
		{name: "NES full", raw: "Nintendo Entertainment System", want: PlatformNintendo, wantOK: true},
		{name: "Switch", raw: "Nintendo Switch", want: PlatformNintendo, wantOK: true},
		{name: "GBA", raw: "Game Boy Advance", want: PlatformNintendo, wantOK: true},
		{name: "GameCube", raw: "GameCube", want: PlatformNintendo, wantOK: true},
		{name: "Super Famicom", raw: "Super Famicom", want: PlatformNintendo, wantOK: true},
		{name: "SNES exact", raw: "SNES", want: PlatformNintendo, wantOK: true},
		{name: "NES exact", raw: "NES", want: PlatformNintendo, wantOK: true},
		{name: "N64 exact", raw: "N64", want: PlatformNintendo, wantOK: true},

		// PlayStation
		{name: "PS1", raw: "PlayStation", want: PlatformPlayStation, wantOK: true},
		{name: "PS5", raw: "PlayStation 5", want: PlatformPlayStation, wantOK: true},
		{name: "PS4 short", raw: "PS4", want: PlatformPlayStation, wantOK: true},
		{name: "PSP", raw: "PSP", want: PlatformPlayStation, wantOK: true},
		{name: "PS Vita", raw: "PlayStation Vita", want: PlatformPlayStation, wantOK: true},

		// Xbox
		{name: "Xbox", raw: "Xbox", want: PlatformXbox, wantOK: true},
		{name: "Xbox 360", raw: "Xbox 360", want: PlatformXbox, wantOK: true},
		{name: "Xbox One", raw: "Xbox One", want: PlatformXbox, wantOK: true},
		{name: "Xbox Series", raw: "Xbox Series X|S", want: PlatformXbox, wantOK: true},

		// PC
		{name: "PC Windows", raw: "PC (Microsoft Windows)", want: PlatformPC, wantOK: true},
		{name: "Linux", raw: "Linux", want: PlatformPC, wantOK: true},
		{name: "Mac", raw: "Mac", want: PlatformPC, wantOK: true},
		{name: "macOS", raw: "macOS", want: PlatformPC, wantOK: true},
		{name: "DOS", raw: "DOS", want: PlatformPC, wantOK: true},
		{name: "SteamOS", raw: "SteamOS", want: PlatformPC, wantOK: true},

		// Ignored / Unknown
		{name: "Android", raw: "Android", want: "", wantOK: false},
		{name: "iOS", raw: "iOS", want: "", wantOK: false},
		{name: "Sega Genesis must not match nes", raw: "Sega Genesis", want: "", wantOK: false},
		{name: "Arcade", raw: "Arcade", want: "", wantOK: false},
		{name: "Empty", raw: "   ", want: "", wantOK: false},
	}

	for _, tc := range tests {
		t.Run(tc.name, func(t *testing.T) {
			got, ok := NormalizePlatform(tc.raw)
			if ok != tc.wantOK || got != tc.want {
				t.Fatalf("NormalizePlatform(%q) = (%q, %v), want (%q, %v)", tc.raw, got, ok, tc.want, tc.wantOK)
			}
		})
	}
}

func TestNormalizePlatforms_DeduplicationAndFiltering(t *testing.T) {
	input := []IGDBPlatform{
		{ID: 51, Name: "Family Computer Disk System"},
		{ID: 37, Name: "Nintendo 3DS"},
		{ID: 5, Name: "Wii"},
		{ID: 99, Name: "Family Computer"},
		{ID: 41, Name: "Wii U"},
		{ID: 18, Name: "Nintendo Entertainment System"},
		{ID: 34, Name: "Android"},
		{ID: 6, Name: "PC (Microsoft Windows)"},
		{ID: 3, Name: "Linux"},
		{ID: 167, Name: "PlayStation 5"},
		{ID: 48, Name: "PlayStation 4"},
		{ID: 169, Name: "Xbox Series X|S"},
		{ID: 29, Name: "Sega Genesis"},
	}

	got := NormalizePlatforms(input)
	want := []string{PlatformNintendo, PlatformPC, PlatformPlayStation, PlatformXbox}

	if !reflect.DeepEqual(got, want) {
		t.Fatalf("NormalizePlatforms() = %v, want %v", got, want)
	}
}
