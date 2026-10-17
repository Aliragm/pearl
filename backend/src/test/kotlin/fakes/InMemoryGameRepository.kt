package gg.pearl.fakes

import gg.pearl.dto.UpdateGameDTO
import gg.pearl.model.Game
import gg.pearl.model.enums.Platform
import gg.pearl.repository.GameRepository
import java.util.concurrent.ConcurrentHashMap
import kotlin.uuid.Uuid

class InMemoryGameRepository : GameRepository {
    private val storage = ConcurrentHashMap<Uuid, Game>()

    override suspend fun getGames(
        name: String?,
        developer: String?,
        platform: Platform?,
        minScore: Double?,
        page: Int,
        size: Int
    ): Pair<List<Game>, Long> {
        val filtered = storage.values.filter { game ->
            (name == null || game.name.contains(name, ignoreCase = true)) &&
            (developer == null || game.developer.contains(developer, ignoreCase = true)) &&
            (platform == null || game.platform.contains(platform)) &&
            (minScore == null || game.meanScore >= minScore)
        }.sortedBy { it.name }

        val total = filtered.size.toLong()
        val safePage = page.coerceAtLeast(1)
        val safeSize = size.coerceIn(1, 50)
        val offset = (safePage - 1) * safeSize
        val paged = filtered.drop(offset).take(safeSize)
        return Pair(paged, total)
    }

    override suspend fun getGameById(id: Uuid): Game? = storage[id]

    override suspend fun createGame(game: Game): Game? {
        storage[game.id] = game
        return game
    }

    override suspend fun updateGame(id: Uuid, dto: UpdateGameDTO): Game? {
        val existing = storage[id] ?: return null
        val updated = existing.copy(
            name = dto.name ?: existing.name,
            developer = dto.developer ?: existing.developer,
            platform = dto.platform ?: existing.platform,
            meanScore = dto.meanScore ?: existing.meanScore
        )
        storage[id] = updated
        return updated
    }

    override suspend fun deleteGame(id: Uuid): Boolean {
        return storage.remove(id) != null
    }

    fun clear() {
        storage.clear()
    }
}
