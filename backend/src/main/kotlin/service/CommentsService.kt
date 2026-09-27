package gg.pearl.service

import gg.pearl.dto.CommentsDTO
import gg.pearl.dto.UpdateCommentDTO
import gg.pearl.model.Comments
import gg.pearl.repository.CommentsRepository
import kotlin.uuid.Uuid

class CommentsService(
    private val commentsRepository: CommentsRepository
) {

    suspend fun getPostComments(postId: Uuid): List<CommentsDTO> {
        return commentsRepository.getPostComments(postId).map(::convertCommentToDTO)
    }

    suspend fun getCommentById(id: Uuid): CommentsDTO? {
        return commentsRepository.getCommentById(id)?.let(::convertCommentToDTO)
    }

    suspend fun createComment(comment: CommentsDTO): CommentsDTO? {
        val newComment = convertDTOToComment(comment)
        val result = commentsRepository.addComment(newComment)
        return result?.let(::convertCommentToDTO)
    }

    suspend fun updateComment(id: Uuid, dto: UpdateCommentDTO): CommentsDTO? {
        val updated = commentsRepository.updateComment(id, dto)
        return updated?.let(::convertCommentToDTO)
    }

    suspend fun deleteComment(id: Uuid): Boolean {
        return commentsRepository.deleteComment(id)
    }

    private fun convertDTOToComment(dto: CommentsDTO): Comments {
        return if (dto.id != null) {
            Comments(
                id = dto.id,
                userId = dto.userId,
                postId = dto.postId,
                content = dto.content,
                isEdited = dto.isEdited
            )
        } else {
            Comments(
                userId = dto.userId,
                postId = dto.postId,
                content = dto.content,
                isEdited = dto.isEdited
            )
        }
    }

    private fun convertCommentToDTO(comment: Comments): CommentsDTO {
        return CommentsDTO(
            id = comment.id,
            userId = comment.userId,
            postId = comment.postId,
            content = comment.content,
            isEdited = comment.isEdited
        )
    }
}
