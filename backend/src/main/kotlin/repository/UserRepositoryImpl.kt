package gg.pearl.repository

import gg.pearl.model.User
import kotlin.uuid.Uuid

class UserRepositoryImpl : UserRepository {

    private val users: MutableList<User> = mutableListOf()

    override fun getUsers(): MutableList<User> {
        return users
    }

    override fun getUserById(id: Uuid): User? {
        return users.firstOrNull { it.id == id }
    }

    override fun getUserByUsername(username: String): User? {
        return users.firstOrNull { it.name == username }
    }

    override fun getUserByEmail(email: String): User? {
        return users.firstOrNull { it.email == email }
    }

    override fun getUserByEmailAndPassword(email: String, password: String): User? {
        return users.firstOrNull { it.email == email && it.password == password }
    }

    override fun createUser(user: User): User? {
        return if(users.add(user)){
            user
        } else {
            null
        }
    }

    override fun updateUser(user: User): User? {
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

    override fun deleteUser(user: User): Boolean {
        return users.remove(user)
    }
}