package gg.pearl.dto

import gg.pearl.model.enums.Platform
import kotlin.uuid.Uuid

data class GameDTO(
    val id: Uuid? = null,
    val externalId: Int? = null,
    val cover: String = "",
    val summary: String = "",
    val platform: List<Platform> = emptyList(),
    val name: String,
    val developer: String,
    val meanScore: Double = 0.0
)

data class UpdateGameDTO(
    val externalId: Int? = null,
    val cover: String? = null,
    val summary: String? = null,
    val platform: List<Platform>? = null,
    val name: String? = null,
    val developer: String? = null,
    val meanScore: Double? = null
)
