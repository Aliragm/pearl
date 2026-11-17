package domain

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
