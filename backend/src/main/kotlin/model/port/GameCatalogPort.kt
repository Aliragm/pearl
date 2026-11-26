package gg.pearl.model.port

import gg.pearl.model.Game

sealed interface CatalogSearchResult {
    data class Success(val games: List<Game>) : CatalogSearchResult
    data class Unavailable(val reason: String) : CatalogSearchResult
}

interface GameCatalogPort {
    suspend fun searchGames(query: String): CatalogSearchResult
}
