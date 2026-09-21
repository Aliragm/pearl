package gg.pearl.repository

import gg.pearl.model.User
import kotlin.uuid.Uuid

interface UserRepository {
    fun getUsers(): MutableList<User>
    fun getUserById(id: Uuid): User?
    fun getUserByUsername(username: String): User?
    fun getUserByEmail(email: String): User?
    fun getUserByEmailAndPassword(email: String, password: String): User?
    fun createUser(user: User): User?
    fun updateUser(user: User): User?
    fun deleteUser(user: User): Boolean
}