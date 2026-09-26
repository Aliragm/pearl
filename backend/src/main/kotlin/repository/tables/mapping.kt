package gg.pearl.repository.tables

import gg.pearl.model.User
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toKotlinUuid

object UserTable : UUIDTable("users") {
    val name = varchar("name", 32)
    val password = varchar("password", 255)
    val email = varchar("email", 255)
}

class UserDAO(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<UserDAO>(UserTable)

    var name by UserTable.name
    var password by UserTable.password
    var email by UserTable.email
}

suspend fun <T> withTransaction(block: suspend Transaction.() -> T): T =
    newSuspendedTransaction(Dispatchers.IO) { block() }

@OptIn(ExperimentalUuidApi::class)
fun daoToModel(dao: UserDAO): User = User(
    id = dao.id.value.toKotlinUuid(),
    name = dao.name,
    password = dao.password,
    email = dao.email
)
