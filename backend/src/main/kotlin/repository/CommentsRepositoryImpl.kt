package gg.pearl.repository

import gg.pearl.dto.UpdateCommentDTO
import gg.pearl.model.Comments
import gg.pearl.repository.tables.CommentsDAO
import gg.pearl.repository.tables.CommentsTable
import gg.pearl.repository.tables.PostTable
import gg.pearl.repository.tables.UserTable
import gg.pearl.repository.tables.daoToModel
import gg.pearl.repository.tables.withTransaction
import org.jetbrains.exposed.dao.id.EntityID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

@OptIn(ExperimentalUuidApi::class)
class CommentsRepositoryImpl : CommentsRepository {

    override suspend fun getPostComments(postId: Uuid): List<Comments> {
        return withTransaction {
            CommentsDAO.find { CommentsTable.postId eq postId.toJavaUuid() }.map(::daoToModel)
        }
    }

    override suspend fun getCommentById(id: Uuid): Comments? {
        return withTransaction {
            CommentsDAO.findById(id.toJavaUuid())?.let(::daoToModel)
        }
    }

    override suspend fun addComment(comment: Comments): Comments? {
        return withTransaction {
            val entity = CommentsDAO.new {
                this.userId = EntityID(comment.userId.toJavaUuid(), UserTable)
                this.postId = EntityID(comment.postId.toJavaUuid(), PostTable)
                this.content = comment.content
                this.isEdited = comment.isEdited
            }
            daoToModel(entity)
        }
    }

    override suspend fun updateComment(id: Uuid, dto: UpdateCommentDTO): Comments? {
        return withTransaction {
            val entity = CommentsDAO.findById(id.toJavaUuid()) ?: return@withTransaction null
            dto.content?.let {
                entity.content = it
                entity.isEdited = true
            }
            daoToModel(entity)
        }
    }

    override suspend fun deleteComment(id: Uuid): Boolean {
        return withTransaction {
            val entity = CommentsDAO.findById(id.toJavaUuid()) ?: return@withTransaction false
            entity.delete()
            true
        }
    }
}