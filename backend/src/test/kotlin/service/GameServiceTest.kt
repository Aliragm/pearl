package gg.pearl.service

import gg.pearl.dto.GameDTO
import gg.pearl.dto.UpdateGameDTO
import gg.pearl.fakes.InMemoryGameRepository
import gg.pearl.model.enums.Platform
import kotlinx.coroutines.runBlocking
import kotlin.test.*
import kotlin.uuid.Uuid

class GameServiceTest {

    private val fakeRepo = InMemoryGameRepository()
    private val gameService = GameService(fakeRepo)

    @BeforeTest
    fun setUp() {
        fakeRepo.clear()
    }

    @Test
    fun `test create game and retrieve by id`() = runBlocking {
        val request = GameDTO(
            name = "Elden Ring",
            developer = "FromSoftware",
            platform = listOf(Platform.PC, Platform.PLAYSTATION, Platform.XBOX),
            meanScore = 9.6
        )

        val created = gameService.createGame(request)
        assertNotNull(created)
        val gameId = requireNotNull(created.id)
        assertEquals("Elden Ring", created.name)
        assertEquals(3, created.platform.size)
        assertEquals(9.6, created.meanScore)

        val retrieved = gameService.getGameById(gameId)
        assertNotNull(retrieved)
        assertEquals("FromSoftware", retrieved.developer)
    }

    @Test
    fun `test get games with developer, platform and minScore filters`() = runBlocking {
        gameService.createGame(GameDTO(name = "Bloodborne", developer = "FromSoftware", platform = listOf(Platform.PLAYSTATION), meanScore = 9.2))
        gameService.createGame(GameDTO(name = "Dark Souls 3", developer = "FromSoftware", platform = listOf(Platform.PC, Platform.PLAYSTATION), meanScore = 8.9))
        gameService.createGame(GameDTO(name = "Zelda: BotW", developer = "Nintendo", platform = listOf(Platform.NINTENDO), meanScore = 9.7))
        gameService.createGame(GameDTO(name = "Super Mario Odyssey", developer = "Nintendo", platform = listOf(Platform.NINTENDO), meanScore = 9.7))

        // Filter by developer "FromSoftware"
        val fromSoftGames = gameService.getGames(developer = "FromSoftware")
        assertEquals(2, fromSoftGames.total)
        assertTrue(fromSoftGames.items.all { it.developer == "FromSoftware" })

        // Filter by platform "PLAYSTATION"
        val psGames = gameService.getGames(platform = Platform.PLAYSTATION)
        assertEquals(2, psGames.total)

        // Filter by minScore >= 9.5
        val topGames = gameService.getGames(minScore = 9.5)
        assertEquals(2, topGames.total)
        assertTrue(topGames.items.all { it.meanScore >= 9.5 })
    }

    @Test
    fun `test update game partial fields`() = runBlocking {
        val created = gameService.createGame(
            GameDTO(name = "Cyberpunk 2077", developer = "CD Projekt Red", platform = listOf(Platform.PC), meanScore = 7.0)
        )!!

        val updated = gameService.updateGame(
            created.id!!,
            UpdateGameDTO(meanScore = 8.8, platform = listOf(Platform.PC, Platform.PLAYSTATION, Platform.XBOX))
        )

        assertNotNull(updated)
        assertEquals(8.8, updated.meanScore)
        assertEquals(3, updated.platform.size)
        assertEquals("Cyberpunk 2077", updated.name)
    }

    @Test
    fun `test delete game`() = runBlocking {
        val created = gameService.createGame(
            GameDTO(name = "Half-Life 2", developer = "Valve", platform = listOf(Platform.PC), meanScore = 9.6)
        )!!
        val gameId = requireNotNull(created.id)

        val deleted = gameService.deleteGame(gameId)
        assertTrue(deleted)

        val notFound = gameService.getGameById(gameId)
        assertNull(notFound)

        val nonExisting = gameService.deleteGame(Uuid.random())
        assertFalse(nonExisting)
    }
}
