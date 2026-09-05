package gg.pearl.repository

import gg.pearl.model.Comments
import kotlin.time.Clock
import kotlin.uuid.Uuid

class CommentsRepository {

    private val comments: MutableList<Comments> = mutableListOf()

    fun getPostComments(postId: Uuid): List<Comments>{
        return comments.filter { it.postId == postId }
    }

    fun addComment(comment: Comments): Comments? {
        return if(comments.add(comment)){
            comment
        } else {
            null
        }
    }

    fun deleteComment(comment: Comments): Boolean{
        return comments.remove(comment)
    }

    fun updateComment(comment: Comments): Comments? {
        val oldComment = this.getCommentById(comment.id)

        if(oldComment != null) {
            val newComment = Comments(id = oldComment.id, userId = oldComment.userId, postId = oldComment.postId,
                content = comment.content, isEdited = true, createdAt = oldComment.createdAt, updatedAt = Clock.System.now())
            if(this.deleteComment(oldComment)){
                this.comments.add(newComment)
                return newComment
            } else {
                return oldComment
            }
        } else {
            return null
        }
    }

    fun getCommentById(id: Uuid): Comments? {
        return comments.firstOrNull { it.id == id }
    }
}