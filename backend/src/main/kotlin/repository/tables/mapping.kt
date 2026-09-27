package gg.pearl.repository.tables

import gg.pearl.model.Comments
import gg.pearl.model.Follows
import gg.pearl.model.Game
import gg.pearl.model.Post
import gg.pearl.model.User
import gg.pearl.model.enums.Platform
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toKotlinUuid

// --- Users ---
object UserTable : UUIDTable("users") {
    val name = varchar("name", 255)
    val password = varchar("password", 255)
    val email = varchar("email", 255)
}

class UserDAO(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<UserDAO>(UserTable)

    var name by UserTable.name
    var password by UserTable.password
    var email by UserTable.email
}

@OptIn(ExperimentalUuidApi::class)
fun daoToModel(dao: UserDAO): User = User(
    id = dao.id.value.toKotlinUuid(),
    name = dao.name,
    password = dao.password,
    email = dao.email
)

// --- Games & Platforms ---
object GameTable : UUIDTable("games") {
    val name = varchar("name", 255)
    val developer = varchar("developer", 255)
    val meanScore = double("mean_score")
}

object GamePlatformsTable : Table("game_platforms") {
    val gameId = reference("game_id", GameTable, onDelete = ReferenceOption.CASCADE)
    val platform = varchar("platform", 50)
    override val primaryKey = PrimaryKey(gameId, platform)
}

class GameDAO(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<GameDAO>(GameTable)

    var name by GameTable.name
    var developer by GameTable.developer
    var meanScore by GameTable.meanScore
}

@OptIn(ExperimentalUuidApi::class)
fun daoToModel(dao: GameDAO, platforms: List<Platform>): Game = Game(
    id = dao.id.value.toKotlinUuid(),
    name = dao.name,
    developer = dao.developer,
    meanScore = dao.meanScore,
    platform = platforms
)

// --- Posts ---
object PostTable : UUIDTable("posts") {
    val userId = reference("user_id", UserTable, onDelete = ReferenceOption.CASCADE)
    val gameId = reference("game_id", GameTable, onDelete = ReferenceOption.CASCADE)
    val title = varchar("title", 255)
    val content = text("content")
    val score = double("score")
}

class PostDAO(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<PostDAO>(PostTable)

    var userId by PostTable.userId
    var gameId by PostTable.gameId
    var title by PostTable.title
    var content by PostTable.content
    var score by PostTable.score
}

@OptIn(ExperimentalUuidApi::class)
fun daoToModel(dao: PostDAO): Post = Post(
    id = dao.id.value.toKotlinUuid(),
    userId = dao.userId.value.toKotlinUuid(),
    gameId = dao.gameId.value.toKotlinUuid(),
    title = dao.title,
    content = dao.content,
    score = dao.score
)

// --- Comments ---
object CommentsTable : UUIDTable("comments") {
    val userId = reference("user_id", UserTable, onDelete = ReferenceOption.CASCADE)
    val postId = reference("post_id", PostTable, onDelete = ReferenceOption.CASCADE)
    val content = text("content")
    val isEdited = bool("is_edited")
}

class CommentsDAO(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<CommentsDAO>(CommentsTable)

    var userId by CommentsTable.userId
    var postId by CommentsTable.postId
    var content by CommentsTable.content
    var isEdited by CommentsTable.isEdited
}

@OptIn(ExperimentalUuidApi::class)
fun daoToModel(dao: CommentsDAO): Comments = Comments(
    id = dao.id.value.toKotlinUuid(),
    userId = dao.userId.value.toKotlinUuid(),
    postId = dao.postId.value.toKotlinUuid(),
    content = dao.content,
    isEdited = dao.isEdited
)

// --- Follows ---
object FollowsTable : Table("follows") {
    val userFollowingId = reference("user_following_id", UserTable, onDelete = ReferenceOption.CASCADE)
    val userFollowerId = reference("user_follower_id", UserTable, onDelete = ReferenceOption.CASCADE)
    override val primaryKey = PrimaryKey(userFollowingId, userFollowerId)
}

suspend fun <T> withTransaction(block: suspend Transaction.() -> T): T =
    newSuspendedTransaction(Dispatchers.IO) { block() }
