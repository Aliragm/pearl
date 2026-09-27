package gg.pearl.repository

import gg.pearl.dto.UpdateGameDTO
import gg.pearl.model.Game
import kotlin.uuid.Uuid

interface GameRepository {
    suspend fun getGames(): List<Game>
    suspend fun getGameById(id: Uuid): Game?
    suspend fun createGame(game: Game): Game?
    suspend fun updateGame(id: Uuid, dto: UpdateGameDTO): Game?
    suspend fun deleteGame(id: Uuid): Boolean
}