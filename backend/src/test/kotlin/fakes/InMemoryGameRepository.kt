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

    override suspend fun getGameByExternalId(externalId: Int): Game? {
        return storage.values.firstOrNull { it.externalId == externalId }
    }

    override suspend fun findOrCreateByExternalId(game: Game): Game {
        if (game.externalId != null) {
            val existing = getGameByExternalId(game.externalId)
            if (existing != null) {
                return existing
            }
        }
        val toStore = game.copy(platform = game.platform.distinct())
        storage[toStore.id] = toStore
        return toStore
    }

    override suspend fun createGame(game: Game): Game? {
        val toStore = game.copy(platform = game.platform.distinct())
        storage[toStore.id] = toStore
        return toStore
    }

    override suspend fun updateGame(id: Uuid, dto: UpdateGameDTO): Game? {
        val existing = storage[id] ?: return null
        val updated = existing.copy(
            externalId = dto.externalId ?: existing.externalId,
            cover = dto.cover ?: existing.cover,
            summary = dto.summary ?: existing.summary,
            name = dto.name ?: existing.name,
            developer = dto.developer ?: existing.developer,
            platform = dto.platform?.distinct() ?: existing.platform,
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
