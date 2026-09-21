package gg.pearl.repository

import gg.pearl.model.Comments
import kotlin.uuid.Uuid

interface CommentsRepository {
    fun getPostComments(postId: Uuid): List<Comments>
    fun addComment(comment: Comments): Comments?
    fun deleteComment(comment: Comments): Boolean
    fun updateComment(comment: Comments): Comments?
    fun getCommentById(id: Uuid): Comments?
}