package gg.pearl.service

import gg.pearl.dto.GameDTO
import gg.pearl.dto.PageResponse
import gg.pearl.dto.UpdateGameDTO
import gg.pearl.model.Game
import gg.pearl.model.enums.Platform
import gg.pearl.repository.GameRepository
import kotlin.uuid.Uuid

class GameService(
    private val gameRepository: GameRepository
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
                platform = dto.platform,
                name = dto.name,
                developer = dto.developer,
                meanScore = dto.meanScore
            )
        } else {
            Game(
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
            platform = game.platform,
            name = game.name,
            developer = game.developer,
            meanScore = game.meanScore
        )
    }
}
