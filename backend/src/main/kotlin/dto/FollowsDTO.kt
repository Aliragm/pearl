package gg.pearl.dto

import kotlin.uuid.Uuid

data class FollowsDTO(
    val userFollowingId: Uuid,
    val userFollowerId: Uuid
)
