package gg.pearl.model

import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

data class Post (
    val id: Uuid = Uuid.random(),
    val userId: Uuid,
    val gameId: Uuid,
    val title: String,
    val content: String,
    val score: Double,
    val createdAt: Instant = Clock.System.now(),
)