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
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.selectAll
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

@OptIn(ExperimentalUuidApi::class)
class CommentsRepositoryImpl : CommentsRepository {

    override suspend fun getPostComments(
        postId: Uuid,
        userId: Uuid?,
        content: String?,
        page: Int,
        size: Int
    ): Pair<List<Comments>, Long> {
        val safePage = page.coerceAtLeast(1)
        val safeSize = size.coerceIn(1, 50)
        val offset = ((safePage - 1).toLong() * safeSize)

        return withTransaction {
            val conditions = mutableListOf<Op<Boolean>>()
            conditions.add(CommentsTable.postId eq postId.toJavaUuid())

            if (userId != null) {
                conditions.add(CommentsTable.userId eq userId.toJavaUuid())
            }
            if (!content.isNullOrBlank()) {
                conditions.add(CommentsTable.content.lowerCase() like "%${content.lowercase()}%")
            }

            val whereOp = conditions.reduce { acc, op -> acc and op }
            val query = CommentsTable.selectAll().where { whereOp }

            val total = query.count()
            val comments = query.limit(safeSize).offset(offset).map { row ->
                CommentsDAO.wrapRow(row).let(::daoToModel)
            }
            Pair(comments, total)
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