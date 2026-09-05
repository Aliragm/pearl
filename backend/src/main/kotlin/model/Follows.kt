package gg.pearl.model

import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

data class Follows (
    val userFollowingId: Uuid,
    val userFollowerId: Uuid,
    val createdAt: Instant = Clock.System.now()
)