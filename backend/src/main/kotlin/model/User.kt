package gg.pearl.model

import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class User(
    val id: Uuid = Uuid.random(),
    val name: String,
    val password: String,
    val email: String,
    val createdAt: Instant = Clock.System.now(),
    var updatedAt: Instant = Clock.System.now()
)
