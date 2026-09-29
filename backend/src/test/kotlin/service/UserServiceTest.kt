package gg.pearl.service

import gg.pearl.dto.UpdateUserDTO
import gg.pearl.dto.UserDTO
import gg.pearl.fakes.InMemoryUserRepository
import kotlinx.coroutines.runBlocking
import kotlin.test.*
import kotlin.uuid.Uuid

class UserServiceTest {

    private val fakeRepo = InMemoryUserRepository()
    private val userService = UserService(fakeRepo)

    @BeforeTest
    fun setUp() {
        fakeRepo.clear()
    }

    @Test
    fun `test create user and retrieve by id and email`() = runBlocking {
        val request = UserDTO(
            name = "Aloy",
            email = "aloy@horizon.com",
            password = "SecretPassword123"
        )

        val created = userService.createUser(request)
        assertNotNull(created)
        val userId = requireNotNull(created.id)
        assertEquals("Aloy", created.name)
        assertEquals("aloy@horizon.com", created.email)

        val byId = userService.getUserById(userId)
        assertNotNull(byId)
        assertEquals("Aloy", byId.name)

        val byEmail = userService.getUserByEmail("aloy@horizon.com")
        assertNotNull(byEmail)
        assertEquals(userId, byEmail.id)
    }

    @Test
    fun `test create user with duplicate email fails`() = runBlocking {
        val request = UserDTO(
            name = "Gordon Freeman",
            email = "gordon@blackmesa.gov",
            password = "CrowbarPassword"
        )

        val first = userService.createUser(request)
        assertNotNull(first)

        val second = userService.createUser(request)
        assertNull(second)
    }

    @Test
    fun `test get users with name and email filters and pagination`() = runBlocking {
        userService.createUser(UserDTO(name = "Mario", email = "mario@nintendo.com", password = "pass"))
        userService.createUser(UserDTO(name = "Luigi", email = "luigi@nintendo.com", password = "pass"))
        userService.createUser(UserDTO(name = "Peach", email = "peach@mushroom.com", password = "pass"))
        userService.createUser(UserDTO(name = "Bowser", email = "bowser@castle.com", password = "pass"))

        // Filter by email domain "nintendo.com"
        val nintendoUsers = userService.getUsers(email = "nintendo.com", page = 1, size = 10)
        assertEquals(2, nintendoUsers.total)
        assertEquals(2, nintendoUsers.items.size)
        assertTrue(nintendoUsers.items.any { it.name == "Mario" })
        assertTrue(nintendoUsers.items.any { it.name == "Luigi" })

        // Pagination: page 1 size 2 of all 4 users
        val page1 = userService.getUsers(page = 1, size = 2)
        assertEquals(4, page1.total)
        assertEquals(2, page1.items.size)
        assertEquals(1, page1.page)
        assertEquals(2, page1.size)
        assertEquals(2, page1.totalPages)

        // Page 2
        val page2 = userService.getUsers(page = 2, size = 2)
        assertEquals(4, page2.total)
        assertEquals(2, page2.items.size)
        assertEquals(2, page2.page)
    }

    @Test
    fun `test update user partial fields`() = runBlocking {
        val created = userService.createUser(
            UserDTO(name = "Samus Aran", email = "samus@metroid.com", password = "pass")
        )!!

        val updated = userService.updateUser(
            created.id!!,
            UpdateUserDTO(name = "Samus Aran (Zero Suit)")
        )

        assertNotNull(updated)
        assertEquals("Samus Aran (Zero Suit)", updated.name)
        assertEquals("samus@metroid.com", updated.email)
    }

    @Test
    fun `test delete user returns true for existing and false for non-existing`() = runBlocking {
        val created = userService.createUser(
            UserDTO(name = "Kratos", email = "kratos@sparta.com", password = "pass")
        )!!
        val userId = requireNotNull(created.id)

        val deleted = userService.deleteUser(userId)
        assertTrue(deleted)

        val notFound = userService.getUserById(userId)
        assertNull(notFound)

        val nonExistingDeleted = userService.deleteUser(Uuid.random())
        assertFalse(nonExistingDeleted)
    }
}
