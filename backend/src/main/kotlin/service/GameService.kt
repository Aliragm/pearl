package gg.pearl.service

import gg.pearl.dto.GameDTO
import gg.pearl.dto.UpdateGameDTO
import gg.pearl.model.Game
import gg.pearl.repository.GameRepository
import kotlin.uuid.Uuid

class GameService(
    private val gameRepository: GameRepository
) {

    suspend fun getGames(): List<GameDTO> {
        return gameRepository.getGames().map(::convertGameToDTO)
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
