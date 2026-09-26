package gg.pearl.repository

import gg.pearl.model.User
import kotlin.uuid.Uuid

interface UserRepository {
    suspend fun getUsers(): List<User>
    suspend fun getUserById(id: Uuid): User?
    suspend fun getUserByUsername(username: String): User?
    suspend fun getUserByEmail(email: String): User?
    suspend fun getUserByEmailAndPassword(email: String, password: String): User?
    suspend fun createUser(user: User): User?
    suspend fun updateUser(user: User): User?
    suspend fun deleteUser(user: User): Boolean
}