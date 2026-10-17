package gg.pearl.repository

import gg.pearl.dto.UpdateUserDTO
import gg.pearl.model.User
import gg.pearl.repository.tables.UserDAO
import gg.pearl.repository.tables.UserTable
import gg.pearl.repository.tables.daoToModel
import gg.pearl.repository.tables.withTransaction
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.selectAll
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

@OptIn(ExperimentalUuidApi::class)
class UserRepositoryImpl : UserRepository {

    override suspend fun getUsers(name: String?, email: String?, page: Int, size: Int): Pair<List<User>, Long> {
        val safePage = page.coerceAtLeast(1)
        val safeSize = size.coerceIn(1, 50)
        val offset = ((safePage - 1).toLong() * safeSize)

        return withTransaction {
            val conditions = mutableListOf<Op<Boolean>>()
            if (!name.isNullOrBlank()) {
                conditions.add(UserTable.name.lowerCase() like "%${name.lowercase()}%")
            }
            if (!email.isNullOrBlank()) {
                conditions.add(UserTable.email.lowerCase() like "%${email.lowercase()}%")
            }

            val query = if (conditions.isNotEmpty()) {
                val whereOp = conditions.reduce { acc, op -> acc and op }
                UserTable.selectAll().where { whereOp }
            } else {
                UserTable.selectAll()
            }

            val total = query.count()
            val users = query.limit(safeSize).offset(offset).map { row ->
                User(
                    id = row[UserTable.id].value.toKotlinUuid(),
                    name = row[UserTable.name],
                    password = row[UserTable.password],
                    email = row[UserTable.email]
                )
            }
            Pair(users, total)
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
            UserDAO.find { UserTable.email.lowerCase() eq email.lowercase() }
                .singleOrNull()
                ?.let(::daoToModel)
        }
    }

    override suspend fun getUserByEmailAndPassword(email: String, password: String): User? {
        return withTransaction {
            UserDAO.find { (UserTable.email.lowerCase() eq email.lowercase()) and (UserTable.password eq password) }
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