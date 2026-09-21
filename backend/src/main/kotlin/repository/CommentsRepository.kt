package gg.pearl.repository

import gg.pearl.model.Comments
import kotlin.uuid.Uuid

interface CommentsRepository {
    suspend fun getPostComments(postId: Uuid): List<Comments>
    suspend fun addComment(comment: Comments): Comments?
    suspend fun deleteComment(comment: Comments): Boolean
    suspend fun updateComment(comment: Comments): Comments?
    suspend fun getCommentById(id: Uuid): Comments?
}