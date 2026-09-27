package gg.pearl.repository

import gg.pearl.dto.UpdateUserDTO
import gg.pearl.model.User
import gg.pearl.repository.tables.UserDAO
import gg.pearl.repository.tables.UserTable
import gg.pearl.repository.tables.daoToModel
import gg.pearl.repository.tables.withTransaction
import org.jetbrains.exposed.sql.and
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

@OptIn(ExperimentalUuidApi::class)
class UserRepositoryImpl : UserRepository {

    override suspend fun getUsers(): List<User> {
        return withTransaction {
            UserDAO.all().map(::daoToModel)
        }
    }

    override suspend fun getUserById(id: Uuid): User? {
        return withTransaction {
            UserDAO.findById(id.toJavaUuid())?.let(::daoToModel)
        }
    }

    override suspend fun getUserByUsername(username: String): User? {
        return withTransaction {
            UserDAO.find { UserTable.name eq username }
                .singleOrNull()
                ?.let(::daoToModel)
        }
    }

    override suspend fun getUserByEmail(email: String): User? {
        return withTransaction {
            UserDAO.find { UserTable.email eq email }
                .singleOrNull()
                ?.let(::daoToModel)
        }
    }

    override suspend fun getUserByEmailAndPassword(email: String, password: String): User? {
        return withTransaction {
            UserDAO.find { (UserTable.email eq email) and (UserTable.password eq password) }
                .singleOrNull()
                ?.let(::daoToModel)
        }
    }

    override suspend fun createUser(user: User): User? {
        return withTransaction {
            val entity = UserDAO.new {
                this.name = user.name
                this.email = user.email
                this.password = user.password
            }
            daoToModel(entity)
        }
    }

    override suspend fun updateUser(id: Uuid, dto: UpdateUserDTO): User? {
        return withTransaction {
            val entity = UserDAO.findById(id.toJavaUuid()) ?: return@withTransaction null
            dto.name?.let { entity.name = it }
            dto.email?.let { entity.email = it }
            daoToModel(entity)
        }
    }

    override suspend fun deleteUser(user: User): Boolean {
        return withTransaction {
            val entity = UserDAO.findById(user.id.toJavaUuid()) ?: return@withTransaction false
            entity.delete()
            true
        }
    }
}