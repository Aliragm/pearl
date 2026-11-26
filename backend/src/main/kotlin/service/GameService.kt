package gg.pearl.service

import gg.pearl.dto.GameDTO
import gg.pearl.dto.PageResponse
import gg.pearl.dto.UpdateGameDTO
import gg.pearl.model.Game
import gg.pearl.model.enums.Platform
import gg.pearl.model.port.CatalogSearchResult
import gg.pearl.model.port.GameCatalogPort
import gg.pearl.repository.GameRepository
import kotlin.uuid.Uuid

data class GameSearchResult(
    val page: PageResponse<GameDTO>,
    val fromFallback: Boolean
)

class GameService(
    private val gameRepository: GameRepository,
    private val catalogPort: GameCatalogPort? = null
) {

    suspend fun getGames(
        name: String? = null,
        developer: String? = null,
        platform: Platform? = null,
        minScore: Double? = null,
        page: Int = 1,
        size: Int = 10
    ): PageResponse<GameDTO> {
        val (games, total) = gameRepository.getGames(name, developer, platform, minScore, page, size)
        return PageResponse(
            items = games.map(::convertGameToDTO),
            page = page.coerceAtLeast(1),
            size = size.coerceIn(1, 50),
            total = total
        )
    }

    suspend fun searchGames(
        query: String,
        page: Int = 1,
        size: Int = 10
    ): GameSearchResult {
        val safePage = page.coerceAtLeast(1)
        val safeSize = size.coerceIn(1, 50)

        val catalogResult = catalogPort?.searchGames(query)
            ?: CatalogSearchResult.Unavailable("NO_PORT")

        return when (catalogResult) {
            is CatalogSearchResult.Success -> {
                val enriched = catalogResult.games.map { externalGame ->
                    val persisted = externalGame.externalId?.let { extId ->
                        gameRepository.getGameByExternalId(extId)
                    }
                    if (persisted != null) {
                        convertGameToDTO(persisted)
                    } else {
                        GameDTO(
                            id = null,
                            externalId = externalGame.externalId,
                            cover = externalGame.cover,
                            summary = externalGame.summary,
                            platform = externalGame.platform,
                            name = externalGame.name,
                            developer = externalGame.developer,
                            meanScore = externalGame.meanScore
                        )
                    }
                }
                val total = enriched.size.toLong()
                val offset = (safePage - 1) * safeSize
                val paged = enriched.drop(offset).take(safeSize)
                GameSearchResult(
                    page = PageResponse(
                        items = paged,
                        page = safePage,
                        size = safeSize,
                        total = total
                    ),
                    fromFallback = false
                )
            }
            is CatalogSearchResult.Unavailable -> {
                val localPage = getGames(name = query, page = safePage, size = safeSize)
                GameSearchResult(
                    page = localPage,
                    fromFallback = true
                )
            }
        }
    }

    suspend fun getGameById(id: Uuid): GameDTO? {
        return gameRepository.getGameById(id)?.let(::convertGameToDTO)
    }

    suspend fun createGame(game: GameDTO): GameDTO? {
        val newGame = convertDTOToGame(game)
        val result = gameRepository.createGame(newGame)
        return result?.let(::convertGameToDTO)
    }

    suspend fun updateGame(id: Uuid, dto: UpdateGameDTO): GameDTO? {
        val updated = gameRepository.updateGame(id, dto)
        return updated?.let(::convertGameToDTO)
    }

    suspend fun deleteGame(id: Uuid): Boolean {
        return gameRepository.deleteGame(id)
    }

    private fun convertDTOToGame(dto: GameDTO): Game {
        return if (dto.id != null) {
            Game(
                id = dto.id,
                externalId = dto.externalId,
                cover = dto.cover,
                summary = dto.summary,
                platform = dto.platform,
                name = dto.name,
                developer = dto.developer,
                meanScore = dto.meanScore
            )
        } else {
            Game(
                externalId = dto.externalId,
                cover = dto.cover,
                summary = dto.summary,
                platform = dto.platform,
                name = dto.name,
                developer = dto.developer,
                meanScore = dto.meanScore
            )
        }
    }

    private fun convertGameToDTO(game: Game): GameDTO {
        return GameDTO(
            id = game.id,
            externalId = game.externalId,
            cover = game.cover,
            summary = game.summary,
            platform = game.platform,
            name = game.name,
            developer = game.developer,
            meanScore = game.meanScore
        )
    }
}
