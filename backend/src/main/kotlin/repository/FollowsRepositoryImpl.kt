package gg.pearl.repository

import gg.pearl.model.Follows
import gg.pearl.repository.tables.FollowsTable
import gg.pearl.repository.tables.UserTable
import gg.pearl.repository.tables.withTransaction
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.selectAll
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

@OptIn(ExperimentalUuidApi::class)
class FollowsRepositoryImpl : FollowsRepository {

    override suspend fun createFollow(follow: Follows): Follows? {
        return try {
            withTransaction {
                val inserted = FollowsTable.insertIgnore {
                    it[userFollowingId] = EntityID(follow.userFollowingId.toJavaUuid(), UserTable)
                    it[userFollowerId] = EntityID(follow.userFollowerId.toJavaUuid(), UserTable)
                }
                if (inserted.insertedCount > 0) follow else null
            }
        } catch (e: ExposedSQLException) {
            null
        }
    }

    override suspend fun deleteFollow(follow: Follows): Boolean {
        return withTransaction {
            val count = FollowsTable.deleteWhere {
                (userFollowingId eq follow.userFollowingId.toJavaUuid()) and
                (userFollowerId eq follow.userFollowerId.toJavaUuid())
            }
            count > 0
        }
    }

    override suspend fun isFollowing(userFollowingId: Uuid, userFollowerId: Uuid): Boolean {
        return withTransaction {
            val query = FollowsTable.selectAll().where {
                (FollowsTable.userFollowingId eq userFollowingId.toJavaUuid()) and
                (FollowsTable.userFollowerId eq userFollowerId.toJavaUuid())
            }
            query.count() > 0
        }
    }

    override suspend fun getAllFollowers(userId: Uuid, page: Int, size: Int): Pair<List<Follows>, Long> {
        val safePage = page.coerceAtLeast(1)
        val safeSize = size.coerceIn(1, 50)
        val offset = ((safePage - 1).toLong() * safeSize)

        return withTransaction {
            val javaUuid = userId.toJavaUuid()
            val query = FollowsTable.selectAll()
                .where { FollowsTable.userFollowerId eq javaUuid }

            val total = query.count()
            val follows = query.limit(safeSize).offset(offset).map { row ->
                Follows(
                    userFollowingId = row[FollowsTable.userFollowingId].value.toKotlinUuid(),
                    userFollowerId = row[FollowsTable.userFollowerId].value.toKotlinUuid()
                )
            }
            Pair(follows, total)
        }
    }

    override suspend fun getAllFollowings(userId: Uuid, page: Int, size: Int): Pair<List<Follows>, Long> {
        val safePage = page.coerceAtLeast(1)
        val safeSize = size.coerceIn(1, 50)
        val offset = ((safePage - 1).toLong() * safeSize)

        return withTransaction {
            val javaUuid = userId.toJavaUuid()
            val query = FollowsTable.selectAll()
                .where { FollowsTable.userFollowingId eq javaUuid }

            val total = query.count()
            val follows = query.limit(safeSize).offset(offset).map { row ->
                Follows(
                    userFollowingId = row[FollowsTable.userFollowingId].value.toKotlinUuid(),
                    userFollowerId = row[FollowsTable.userFollowerId].value.toKotlinUuid()
                )
            }
            Pair(follows, total)
        }
    }
}