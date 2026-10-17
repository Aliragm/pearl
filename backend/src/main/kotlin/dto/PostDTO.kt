package gg.pearl.dto

import kotlin.uuid.Uuid

data class PostDTO(
    val id: Uuid? = null,
    val userId: Uuid,
    val gameId: Uuid,
    val title: String,
    val content: String,
    val score: Double
)

data class UpdatePostDTO(
    val title: String? = null,
    val content: String? = null,
    val score: Double? = null
)
