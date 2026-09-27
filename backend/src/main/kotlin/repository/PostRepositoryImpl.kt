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
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

@OptIn(ExperimentalUuidApi::class)
class PostRepositoryImpl : PostRepository {

    override suspend fun getPosts(): List<Post> {
        return withTransaction {
            PostDAO.all().map(::daoToModel)
        }
    }

    override suspend fun getPostById(id: Uuid): Post? {
        return withTransaction {
            PostDAO.findById(id.toJavaUuid())?.let(::daoToModel)
        }
    }

    override suspend fun getAllByUserId(userId: Uuid): List<Post> {
        return withTransaction {
            PostDAO.find { PostTable.userId eq userId.toJavaUuid() }.map(::daoToModel)
        }
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