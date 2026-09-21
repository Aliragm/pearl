package gg.pearl.service

import gg.pearl.dto.UserDTO
import gg.pearl.model.User
import gg.pearl.repository.UserRepositoryImpl

class UserService {
    private val userRepository = UserRepositoryImpl()

    suspend fun getUsers(): MutableList<UserDTO>{
        //má implementação
        val users = userRepository.getUsers()
        return users.mapTo(mutableListOf()) { convertUserToDTO(it) }
    }

    suspend fun createUser(user: UserDTO): UserDTO? {
        val newUser = convertDTOToUser(user)

        val result = userRepository.createUser(newUser)

        return if(result != null){
            convertUserToDTO(result)
        } else{
            null
        }
    }

    suspend fun deleteUser(user: UserDTO): Boolean {
        val deletedUser = convertDTOToUser(user)

        return userRepository.deleteUser(deletedUser)
    }

    suspend fun updateUser(user: UserDTO): UserDTO? {
        val oldUser = convertDTOToUser(user)

        val updatedUser = userRepository.updateUser(oldUser)
        return if(updatedUser != null){
            convertUserToDTO(updatedUser)
        } else{
            null
        }

    }

    suspend fun getUserById(user: UserDTO): UserDTO? {
        return if(user.id != null){
            convertUserToDTO(userRepository.getUserById(user.id)!!)
        } else {
            null
        }

    }

    private fun convertDTOToUser(userDTO: UserDTO): User {
        return User(name = userDTO.name, password = userDTO.password, email =  userDTO.email)
    }

    private fun convertUserToDTO(user: User): UserDTO {
        return UserDTO(id = user.id, name = user.name, email = user.email, password = user.password)
    }
}