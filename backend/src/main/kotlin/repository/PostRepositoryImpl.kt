package gg.pearl.repository

import gg.pearl.dto.UpdatePostDTO
import gg.pearl.model.Post
import gg.pearl.repository.tables.GameTable
import gg.pearl.repository.tables.PostDAO
import gg.pearl.repository.tables.PostTable
import gg.pearl.repository.tables.UserTable
import gg.pearl.repository.tables.daoToModel
import gg.pearl.repository.tables.withTransaction
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.selectAll
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

@OptIn(ExperimentalUuidApi::class)
class PostRepositoryImpl : PostRepository {

    override suspend fun getPosts(
        title: String?,
        userId: Uuid?,
        gameId: Uuid?,
        minScore: Double?,
        page: Int,
        size: Int
    ): Pair<List<Post>, Long> {
        val safePage = page.coerceAtLeast(1)
        val safeSize = size.coerceIn(1, 50)
        val offset = ((safePage - 1).toLong() * safeSize)

        return withTransaction {
            val conditions = mutableListOf<Op<Boolean>>()
            if (!title.isNullOrBlank()) {
                conditions.add(PostTable.title.lowerCase() like "%${title.lowercase()}%")
            }
            if (userId != null) {
                conditions.add(PostTable.userId eq userId.toJavaUuid())
            }
            if (gameId != null) {
                conditions.add(PostTable.gameId eq gameId.toJavaUuid())
            }
            if (minScore != null) {
                conditions.add(PostTable.score greaterEq minScore)
            }

            val query = if (conditions.isNotEmpty()) {
                val whereOp = conditions.reduce { acc, op -> acc and op }
                PostTable.selectAll().where { whereOp }
            } else {
                PostTable.selectAll()
            }

            val total = query.count()
            val posts = query.limit(safeSize).offset(offset).map { row ->
                PostDAO.wrapRow(row).let(::daoToModel)
            }
            Pair(posts, total)
        }
    }

    override suspend fun getPostById(id: Uuid): Post? {
        return withTransaction {
            PostDAO.findById(id.toJavaUuid())?.let(::daoToModel)
        }
    }

    override suspend fun getAllByUserId(userId: Uuid, page: Int, size: Int): Pair<List<Post>, Long> {
        return getPosts(userId = userId, page = page, size = size)
    }

    override suspend fun createPost(post: Post): Post? {
        return withTransaction {
            val entity = PostDAO.new {
                this.userId = EntityID(post.userId.toJavaUuid(), UserTable)
                this.gameId = EntityID(post.gameId.toJavaUuid(), GameTable)
                this.title = post.title
                this.content = post.content
                this.score = post.score
            }
            daoToModel(entity)
        }
    }

    override suspend fun updatePost(id: Uuid, dto: UpdatePostDTO): Post? {
        return withTransaction {
            val entity = PostDAO.findById(id.toJavaUuid()) ?: return@withTransaction null
            dto.title?.let { entity.title = it }
            dto.content?.let { entity.content = it }
            dto.score?.let { entity.score = it }
            daoToModel(entity)
        }
    }

    override suspend fun deletePost(id: Uuid): Boolean {
        return withTransaction {
            val entity = PostDAO.findById(id.toJavaUuid()) ?: return@withTransaction false
            entity.delete()
            true
        }
    }
}