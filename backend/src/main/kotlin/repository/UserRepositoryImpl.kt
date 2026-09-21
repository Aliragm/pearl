package gg.pearl.repository

import gg.pearl.model.User
import kotlin.uuid.Uuid

class UserRepositoryImpl : UserRepository {

    private val users: MutableList<User> = mutableListOf()

    override suspend fun getUsers(): MutableList<User> {
        return users
    }

    override suspend fun getUserById(id: Uuid): User? {
        return users.firstOrNull { it.id == id }
    }

    override suspend fun getUserByUsername(username: String): User? {
        return users.firstOrNull { it.name == username }
    }

    override suspend fun getUserByEmail(email: String): User? {
        return users.firstOrNull { it.email == email }
    }

    override suspend fun getUserByEmailAndPassword(email: String, password: String): User? {
        return users.firstOrNull { it.email == email && it.password == password }
    }

    override suspend fun createUser(user: User): User? {
        return if(users.add(user)){
            user
        } else {
            null
        }
    }

    override suspend fun updateUser(user: User): User? {
        val oldUser = this.getUserById(user.id)

        if(oldUser != null) {
           val newUser = User(id = oldUser.id, name = user.name, email = user.email, password = user.password)
            if(this.deleteUser(oldUser)){
                this.users.add(newUser)
                return newUser
            } else {
                return oldUser
            }
        } else {
            return null
        }
    }

    override suspend fun deleteUser(user: User): Boolean {
        return users.remove(user)
    }
}