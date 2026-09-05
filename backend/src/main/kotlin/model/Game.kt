package gg.pearl.model

import gg.pearl.model.enums.Platform
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

data class Game (
    val id: Uuid = Uuid.random(),
    val platform: List<Platform> = emptyList(),
    val name: String,
    val developer: String,
    val meanScore: Double,
    val createdAt: Instant = Clock.System.now(),
    var updatedAt: Instant = Clock.System.now()
)