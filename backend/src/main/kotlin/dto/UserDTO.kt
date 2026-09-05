package gg.pearl.dto

import kotlin.uuid.Uuid

data class UserDTO(
    val id: Uuid? = null,
    val name: String,
    val password: String,
    val email: String,
)