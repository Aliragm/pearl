package gg.pearl.repository

import gg.pearl.dto.UpdateGameDTO
import gg.pearl.model.Game
import gg.pearl.model.enums.Platform
import kotlin.uuid.Uuid

interface GameRepository {
    suspend fun getGames(
        name: String? = null,
        developer: String? = null,
        platform: Platform? = null,
        minScore: Double? = null,
        page: Int = 1,
        size: Int = 10
    ): Pair<List<Game>, Long>

    suspend fun getGameById(id: Uuid): Game?
    suspend fun createGame(game: Game): Game?
    suspend fun updateGame(id: Uuid, dto: UpdateGameDTO): Game?
    suspend fun deleteGame(id: Uuid): Boolean
}