package gg.pearl.dto

import gg.pearl.model.enums.Platform
import kotlin.uuid.Uuid

data class GameDTO(
    val id: Uuid? = null,
    val platform: List<Platform> = emptyList(),
    val name: String,
    val developer: String,
    val meanScore: Double = 0.0
)

data class UpdateGameDTO(
    val platform: List<Platform>? = null,
    val name: String? = null,
    val developer: String? = null,
    val meanScore: Double? = null
)
