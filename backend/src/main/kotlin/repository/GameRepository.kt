package gg.pearl.repository

import gg.pearl.model.Game
import kotlin.time.Clock
import kotlin.uuid.Uuid

class GameRepository {

    private val games: MutableList<Game> = mutableListOf()

    fun getGames(): MutableList<Game> {
        return games
    }

    fun createGame(game: Game): Game? {
        return if(games.add(game)){
            game
        } else {
            null
        }
    }

    fun updateGame(game: Game): Game? {
        val oldGame = this.getGameById(game.id)

        if(oldGame != null) {
            val newGame = Game(id = oldGame.id, platform = game.platform, name = game.name, developer = game.developer,
                meanScore = game.meanScore, createdAt = oldGame.createdAt, updatedAt = Clock.System.now())
            if(this.deleteGame(oldGame)){
                this.games.add(newGame)
                return newGame
            } else {
                return oldGame
            }
        } else {
            return null
        }
    }

    fun deleteGame(game: Game): Boolean {
        return games.remove(game)
    }

    fun getGameById(id: Uuid): Game? {
        return games.firstOrNull { it.id == id }
    }
}