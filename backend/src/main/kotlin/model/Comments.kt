package gg.pearl.model

import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

data class Comments (
    val id: Uuid = Uuid.random(),
    val userId: Uuid,
    val postId: Uuid,
    val content: String,
    val createdAt: Instant = Clock.System.now(),
    val updatedAt: Instant = Clock.System.now()
)