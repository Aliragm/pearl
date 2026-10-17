package gg.pearl.service

import gg.pearl.dto.PageResponse
import gg.pearl.dto.UpdateUserDTO
import gg.pearl.dto.UserDTO
import gg.pearl.model.User
import gg.pearl.repository.UserRepository
import kotlin.uuid.Uuid

class UserService(
    private val userRepository: UserRepository
) {

    suspend fun getUsers(name: String? = null, email: String? = null, page: Int = 1, size: Int = 10): PageResponse<UserDTO> {
        val (users, total) = userRepository.getUsers(name, email, page, size)
        return PageResponse(
            items = users.map(::convertUserToDTO),
            page = page.coerceAtLeast(1),
            size = size.coerceIn(1, 50),
            total = total
        )
    }

    suspend fun getUserById(id: Uuid): UserDTO? {
        return userRepository.getUserById(id)?.let(::convertUserToDTO)
    }

    suspend fun getUserByEmail(email: String): UserDTO? {
        return userRepository.getUserByEmail(email)?.let(::convertUserToDTO)
    }

    suspend fun createUser(user: UserDTO): UserDTO? {
        val newUser = convertDTOToUser(user)
        val result = userRepository.createUser(newUser)
        return result?.let(::convertUserToDTO)
    }

    suspend fun updateUser(id: Uuid, user: UpdateUserDTO): UserDTO? {
        val updatedUser = userRepository.updateUser(id, user)
        return updatedUser?.let(::convertUserToDTO)
    }

    suspend fun deleteUser(id: Uuid): Boolean {
        val existing = userRepository.getUserById(id) ?: return false
        return userRepository.deleteUser(existing)
    }

    private fun convertDTOToUser(userDTO: UserDTO): User {
        return if (userDTO.id != null) {
            User(id = userDTO.id, name = userDTO.name, password = userDTO.password, email = userDTO.email)
        } else {
            User(name = userDTO.name, password = userDTO.password, email = userDTO.email)
        }
    }

    private fun convertUserToDTO(user: User): UserDTO {
        return UserDTO(id = user.id, name = user.name, email = user.email, password = user.password)
    }
}