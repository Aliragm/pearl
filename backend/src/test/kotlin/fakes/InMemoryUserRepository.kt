package gg.pearl.fakes

import gg.pearl.dto.UpdateUserDTO
import gg.pearl.model.User
import gg.pearl.repository.UserRepository
import java.util.concurrent.ConcurrentHashMap
import kotlin.uuid.Uuid

class InMemoryUserRepository : UserRepository {
    private val storage = ConcurrentHashMap<Uuid, User>()

    override suspend fun getUsers(name: String?, email: String?, page: Int, size: Int): Pair<List<User>, Long> {
        val filtered = storage.values.filter { user ->
            (name == null || user.name.contains(name, ignoreCase = true)) &&
            (email == null || user.email.contains(email, ignoreCase = true))
        }.sortedBy { it.name }

        val total = filtered.size.toLong()
        val safePage = page.coerceAtLeast(1)
        val safeSize = size.coerceIn(1, 50)
        val offset = (safePage - 1) * safeSize
        val paged = filtered.drop(offset).take(safeSize)
        return Pair(paged, total)
    }

    override suspend fun getUserById(id: Uuid): User? = storage[id]

    override suspend fun getUserByUsername(username: String): User? =
        storage.values.firstOrNull { it.name.equals(username, ignoreCase = true) }

    override suspend fun getUserByEmail(email: String): User? =
        storage.values.firstOrNull { it.email.equals(email, ignoreCase = true) }

    override suspend fun getUserByEmailAndPassword(email: String, password: String): User? =
        storage.values.firstOrNull { it.email.equals(email, ignoreCase = true) && it.password == password }

    override suspend fun createUser(user: User): User? {
        if (getUserByEmail(user.email) != null) return null
        storage[user.id] = user
        return user
    }

    override suspend fun updateUser(id: Uuid, dto: UpdateUserDTO): User? {
        val existing = storage[id] ?: return null
        val updated = existing.copy(
            name = dto.name ?: existing.name,
            email = dto.email ?: existing.email
        )
        storage[id] = updated
        return updated
    }

    override suspend fun deleteUser(user: User): Boolean {
        return storage.remove(user.id) != null
    }

    fun clear() {
        storage.clear()
    }
}
