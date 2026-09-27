package gg.pearl.repository

import gg.pearl.dto.UpdateGameDTO
import gg.pearl.model.Game
import gg.pearl.model.enums.Platform
import gg.pearl.repository.tables.GameDAO
import gg.pearl.repository.tables.GamePlatformsTable
import gg.pearl.repository.tables.daoToModel
import gg.pearl.repository.tables.withTransaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

@OptIn(ExperimentalUuidApi::class)
class GameRepositoryImpl : GameRepository {

    private fun loadPlatforms(gameId: UUID): List<Platform> {
        return GamePlatformsTable
            .selectAll()
            .where { GamePlatformsTable.gameId eq gameId }
            .map { row -> Platform.valueOf(row[GamePlatformsTable.platform]) }
    }

    override suspend fun getGames(): List<Game> {
        return withTransaction {
            GameDAO.all().map { dao ->
                daoToModel(dao, loadPlatforms(dao.id.value))
            }
        }
    }

    override suspend fun getGameById(id: Uuid): Game? {
        return withTransaction {
            GameDAO.findById(id.toJavaUuid())?.let { dao ->
                daoToModel(dao, loadPlatforms(dao.id.value))
            }
        }
    }

    override suspend fun createGame(game: Game): Game? {
        return withTransaction {
            val entity = GameDAO.new {
                this.name = game.name
                this.developer = game.developer
                this.meanScore = game.meanScore
            }
            if (game.platform.isNotEmpty()) {
                GamePlatformsTable.batchInsert(game.platform) { p ->
                    this[GamePlatformsTable.gameId] = entity.id
                    this[GamePlatformsTable.platform] = p.name
                }
            }
            daoToModel(entity, game.platform)
        }
    }

    override suspend fun updateGame(id: Uuid, dto: UpdateGameDTO): Game? {
        return withTransaction {
            val javaUuid = id.toJavaUuid()
            val entity = GameDAO.findById(javaUuid) ?: return@withTransaction null

            dto.name?.let { entity.name = it }
            dto.developer?.let { entity.developer = it }
            dto.meanScore?.let { entity.meanScore = it }

            if (dto.platform != null) {
                GamePlatformsTable.deleteWhere { gameId eq javaUuid }
                if (dto.platform.isNotEmpty()) {
                    GamePlatformsTable.batchInsert(dto.platform) { p ->
                        this[GamePlatformsTable.gameId] = entity.id
                        this[GamePlatformsTable.platform] = p.name
                    }
                }
            }

            daoToModel(entity, loadPlatforms(javaUuid))
        }
    }

    override suspend fun deleteGame(id: Uuid): Boolean {
        return withTransaction {
            val entity = GameDAO.findById(id.toJavaUuid()) ?: return@withTransaction false
            entity.delete()
            true
        }
    }
}