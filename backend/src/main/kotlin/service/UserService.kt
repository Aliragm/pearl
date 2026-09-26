package gg.pearl.service

import gg.pearl.dto.UserDTO
import gg.pearl.model.User
import gg.pearl.repository.UserRepository
import kotlin.uuid.Uuid

class UserService(
    private val userRepository: UserRepository
) {

    suspend fun getUsers(): List<UserDTO> {
        return userRepository.getUsers().map(::convertUserToDTO)
    }

    suspend fun getUserById(id: Uuid): UserDTO? {
        return userRepository.getUserById(id)?.let(::convertUserToDTO)
    }

    suspend fun createUser(user: UserDTO): UserDTO? {
        val newUser = convertDTOToUser(user)
        val result = userRepository.createUser(newUser)
        return result?.let(::convertUserToDTO)
    }

    suspend fun updateUser(user: UserDTO): UserDTO? {
        val oldUser = convertDTOToUser(user)
        val updatedUser = userRepository.updateUser(oldUser)
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