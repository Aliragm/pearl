package gg.pearl.repository

import gg.pearl.model.Follows
import gg.pearl.repository.tables.FollowsTable
import gg.pearl.repository.tables.UserTable
import gg.pearl.repository.tables.withTransaction
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

@OptIn(ExperimentalUuidApi::class)
class FollowsRepositoryImpl : FollowsRepository {

    override suspend fun createFollow(follow: Follows): Follows? {
        return withTransaction {
            val inserted = FollowsTable.insert {
                it[userFollowingId] = EntityID(follow.userFollowingId.toJavaUuid(), UserTable)
                it[userFollowerId] = EntityID(follow.userFollowerId.toJavaUuid(), UserTable)
            }
            if (inserted.insertedCount > 0) follow else null
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

    override suspend fun getAllFollowers(userId: Uuid): List<Follows> {
        return withTransaction {
            val javaUuid = userId.toJavaUuid()
            FollowsTable.selectAll()
                .where { FollowsTable.userFollowerId eq javaUuid }
                .map { row ->
                    Follows(
                        userFollowingId = row[FollowsTable.userFollowingId].value.toKotlinUuid(),
                        userFollowerId = row[FollowsTable.userFollowerId].value.toKotlinUuid()
                    )
                }
        }
    }

    override suspend fun getAllFollowings(userId: Uuid): List<Follows> {
        return withTransaction {
            val javaUuid = userId.toJavaUuid()
            FollowsTable.selectAll()
                .where { FollowsTable.userFollowingId eq javaUuid }
                .map { row ->
                    Follows(
                        userFollowingId = row[FollowsTable.userFollowingId].value.toKotlinUuid(),
                        userFollowerId = row[FollowsTable.userFollowerId].value.toKotlinUuid()
                    )
                }
        }
    }
}