package gg.pearl.integration

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import gg.pearl.model.Comments
import gg.pearl.model.Follows
import gg.pearl.model.Game
import gg.pearl.model.Post
import gg.pearl.model.User
import gg.pearl.model.enums.Platform
import gg.pearl.repository.*
import kotlinx.coroutines.runBlocking
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.testcontainers.DockerClientFactory
import org.testcontainers.containers.PostgreSQLContainer
import kotlin.test.*

class PostgresIntegrationTest {

    companion object {
        val isDockerAvailable: Boolean by lazy {
            try {
                DockerClientFactory.instance().isDockerAvailable
            } catch (e: Exception) {
                false
            }
        }

        private var postgresContainer: PostgreSQLContainer<*>? = null
        private var dataSource: HikariDataSource? = null

        fun initDatabase() {
            if (!isDockerAvailable || dataSource != null) return

            val container = PostgreSQLContainer("postgres:16-alpine").apply {
                withDatabaseName("pearl_test")
                withUsername("test")
                withPassword("test")
                start()
            }
            postgresContainer = container

            val config = HikariConfig().apply {
                jdbcUrl = container.jdbcUrl
                username = container.username
                password = container.password
                driverClassName = "org.postgresql.Driver"
                maximumPoolSize = 5
                addDataSourceProperty("stringtype", "unspecified")
            }
            val ds = HikariDataSource(config)
            dataSource = ds

            Flyway.configure()
                .dataSource(ds)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .load()
                .migrate()

            Database.connect(ds)
        }
    }

    private val userRepository: UserRepository = UserRepositoryImpl()
    private val gameRepository: GameRepository = GameRepositoryImpl()
    private val postRepository: PostRepository = PostRepositoryImpl()
    private val commentsRepository: CommentsRepository = CommentsRepositoryImpl()
    private val followsRepository: FollowsRepository = FollowsRepositoryImpl()

    @BeforeTest
    fun setUp() {
        if (isDockerAvailable) {
            initDatabase()
        }
    }

    @Test
    fun `test user repository persistence with CITEXT case insensitivity`() = runBlocking {
        if (!isDockerAvailable) {
            println("Skipping test: Docker is not available.")
            return@runBlocking
        }

        val user = User(
            name = "Commander Shepard",
            email = "SHEPARD@NORMANDY.COM",
            password = "SecretPassword"
        )
        val created = userRepository.createUser(user)
        assertNotNull(created)

        val found = userRepository.getUserByEmail("shepard@normandy.com")
        assertNotNull(found)
        assertEquals("Commander Shepard", found.name)
    }

    @Test
    fun `test game repository with 1-to-N platforms relation`() = runBlocking {
        if (!isDockerAvailable) {
            println("Skipping test: Docker is not available.")
            return@runBlocking
        }

        val game = Game(
            name = "Portal 2",
            developer = "Valve",
            platform = listOf(Platform.PC, Platform.PLAYSTATION, Platform.XBOX),
            meanScore = 9.5
        )
        val created = gameRepository.createGame(game)
        assertNotNull(created)

        val retrieved = gameRepository.getGameById(created.id)
        assertNotNull(retrieved)
        assertEquals(3, retrieved.platform.size)
        assertTrue(retrieved.platform.contains(Platform.PC))
        assertTrue(retrieved.platform.contains(Platform.PLAYSTATION))
    }

    @Test
    fun `test comments pagination with limit and offset in SQL`() = runBlocking {
        if (!isDockerAvailable) {
            println("Skipping test: Docker is not available.")
            return@runBlocking
        }

        val user = userRepository.createUser(User(name = "Reviewer", email = "rev@pearl.gg", password = "pass"))!!
        val game = gameRepository.createGame(Game(name = "Halo", developer = "Bungie", platform = listOf(Platform.XBOX), meanScore = 9.0))!!
        val post = postRepository.createPost(Post(userId = user.id, gameId = game.id, title = "Great Game", content = "Masterpiece", score = 9.5))!!

        for (i in 1..15) {
            commentsRepository.addComment(
                Comments(userId = user.id, postId = post.id, content = "Comment number $i")
            )
        }

        val (page1, total) = commentsRepository.getPostComments(postId = post.id, page = 1, size = 10)
        assertEquals(15L, total)
        assertEquals(10, page1.size)

        val (page2, _) = commentsRepository.getPostComments(postId = post.id, page = 2, size = 10)
        assertEquals(5, page2.size)
    }

    @Test
    fun `test follows composite key and check constraint`() = runBlocking {
        if (!isDockerAvailable) {
            println("Skipping test: Docker is not available.")
            return@runBlocking
        }

        val userA = userRepository.createUser(User(name = "User A", email = "usera@pearl.gg", password = "pass"))!!
        val userB = userRepository.createUser(User(name = "User B", email = "userb@pearl.gg", password = "pass"))!!

        val follow = Follows(userFollowingId = userA.id, userFollowerId = userB.id)
        val created = followsRepository.createFollow(follow)
        assertNotNull(created)
        assertTrue(followsRepository.isFollowing(userA.id, userB.id))

        val duplicate = followsRepository.createFollow(follow)
        assertNull(duplicate)
    }
}
