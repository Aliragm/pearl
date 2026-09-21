package gg.pearl.repository

import gg.pearl.model.Game
import kotlin.uuid.Uuid

interface GameRepository {
    suspend fun getGames(): MutableList<Game>
    suspend fun createGame(game: Game): Game?
    suspend fun updateGame(game: Game): Game?
    suspend fun deleteGame(game: Game): Boolean
    suspend fun getGameById(id: Uuid): Game?
}