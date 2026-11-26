package gg.pearl.model

import gg.pearl.model.enums.Platform
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

data class Game(
    val id: Uuid = Uuid.random(),
    val externalId: Int? = null,
    val cover: String = "",
    val platform: List<Platform> = emptyList(),
    val name: String,
    val summary: String = "",
    val developer: String,
    val meanScore: Double = 0.0,
    val createdAt: Instant = Clock.System.now(),
    var updatedAt: Instant = Clock.System.now()
)
