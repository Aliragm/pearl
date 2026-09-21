package gg.pearl.repository

import gg.pearl.model.Game
import kotlin.uuid.Uuid

interface GameRepository {
    fun getGames(): MutableList<Game>
    fun createGame(game: Game): Game?
    fun updateGame(game: Game): Game?
    fun deleteGame(game: Game): Boolean
    fun getGameById(id: Uuid): Game?
}