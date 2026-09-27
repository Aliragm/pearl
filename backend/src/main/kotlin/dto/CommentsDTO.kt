package gg.pearl.dto

import kotlin.uuid.Uuid

data class CommentsDTO(
    val id: Uuid? = null,
    val userId: Uuid,
    val postId: Uuid,
    val content: String,
    val isEdited: Boolean = false
)

data class UpdateCommentDTO(
    val content: String? = null
)
