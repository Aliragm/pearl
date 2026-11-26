package gg.pearl.service

import gg.pearl.dto.GameDTO
import gg.pearl.dto.PostDTO
import gg.pearl.fakes.InMemoryGameRepository
import gg.pearl.fakes.InMemoryPostRepository
import gg.pearl.model.Game
import gg.pearl.model.enums.Platform
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.uuid.Uuid

class PostServiceTest {

    private val gameRepo = InMemoryGameRepository()
    private val postRepo = InMemoryPostRepository()
    private val postService = PostService(postRepo, gameRepo)

    @BeforeTest
    fun setUp() {
        gameRepo.clear()
        postRepo.clear()
    }

    @Test
    fun `createPost persists external game on demand and reuses it on subsequent reviews`() = runBlocking {
        val userId = Uuid.random()
        val externalGame = GameDTO(
            externalId = 1022,
            name = "The Legend of Zelda",
            developer = "Nintendo",
            cover = "https://images.igdb.com/igdb/image/upload/t_cover_big/co1uii.jpg",
            summary = "Classic adventure.",
            platform = listOf(Platform.NINTENDO)
        )

        val firstPost = postService.createPost(
            PostDTO(
                userId = userId,
                game = externalGame,
                title = "Masterpiece",
                content = "Incredible dungeon design.",
                score = 5.0
            )
        )
        assertNotNull(firstPost)
        val createdGameId = assertNotNull(firstPost.gameId)

        val persistedGame = gameRepo.getGameByExternalId(1022)
        assertNotNull(persistedGame)
        assertEquals(createdGameId, persistedGame.id)
        assertEquals("The Legend of Zelda", persistedGame.name)

        val secondPost = postService.createPost(
            PostDTO(
                userId = Uuid.random(),
                game = externalGame,
                title = "Still great",
                content = "Timeless classic.",
                score = 4.5
            )
        )
        assertNotNull(secondPost)
        assertEquals(createdGameId, secondPost.gameId)

        val (_, totalGames) = gameRepo.getGames()
        assertEquals(1L, totalGames)
    }

    @Test
    fun `createPost continues to work with an existing gameId`() = runBlocking {
        val existingGame = gameRepo.createGame(
            Game(
                name = "Hades",
                developer = "Supergiant Games",
                platform = listOf(Platform.PC, Platform.NINTENDO),
                meanScore = 4.9
            )
        )!!

        val created = postService.createPost(
            PostDTO(
                userId = Uuid.random(),
                gameId = existingGame.id,
                title = "Great roguelike",
                content = "Amazing combat and story.",
                score = 5.0
            )
        )

        assertNotNull(created)
        assertEquals(existingGame.id, created.gameId)
    }
}
