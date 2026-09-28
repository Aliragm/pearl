package gg.pearl.repository

import gg.pearl.dto.UpdateCommentDTO
import gg.pearl.model.Comments
import kotlin.uuid.Uuid

interface CommentsRepository {
    suspend fun getPostComments(
        postId: Uuid,
        userId: Uuid? = null,
        content: String? = null,
        page: Int = 1,
        size: Int = 10
    ): Pair<List<Comments>, Long>

    suspend fun getCommentById(id: Uuid): Comments?
    suspend fun addComment(comment: Comments): Comments?
    suspend fun updateComment(id: Uuid, dto: UpdateCommentDTO): Comments?
    suspend fun deleteComment(id: Uuid): Boolean
}