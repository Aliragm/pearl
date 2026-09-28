package gg.pearl

import com.fasterxml.jackson.databind.ObjectMapper
import gg.pearl.dto.ProblemDetails
import gg.pearl.exceptions.*
import gg.pearl.plugins.configureSerialization
import gg.pearl.plugins.configureStatusPages
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StatusPagesTest {

    private val objectMapper = ObjectMapper().findAndRegisterModules()

    @Test
    fun `test invalid uuid exception problem details`() = testApplication {
        application {
            configureSerialization()
            configureStatusPages()
            routing {
                get("/test/invalid-uuid") {
                    throw InvalidUuidException("The comment UUID format is invalid.")
                }
            }
        }

        val response = client.get("/test/invalid-uuid")
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.headers[HttpHeaders.ContentType]?.contains("application/problem+json") == true)

        val problem = objectMapper.readValue(response.bodyAsText(), ProblemDetails::class.java)
        assertEquals(400, problem.status)
        assertEquals("Invalid UUID format", problem.title)
        assertEquals("The comment UUID format is invalid.", problem.detail)
        assertEquals("https://pearl.com/exceptions/InvalidUuidException", problem.type)
        assertEquals("/test/invalid-uuid", problem.instance)
    }

    @Test
    fun `test missing id exception problem details`() = testApplication {
        application {
            configureSerialization()
            configureStatusPages()
            routing {
                get("/test/missing-id") {
                    throw MissingIdException("The comment id parameter is required.")
                }
            }
        }

        val response = client.get("/test/missing-id")
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.headers[HttpHeaders.ContentType]?.contains("application/problem+json") == true)

        val problem = objectMapper.readValue(response.bodyAsText(), ProblemDetails::class.java)
        assertEquals(400, problem.status)
        assertEquals("Missing ID parameter", problem.title)
        assertEquals("The comment id parameter is required.", problem.detail)
        assertEquals("https://pearl.com/exceptions/MissingIdException", problem.type)
        assertEquals("/test/missing-id", problem.instance)
    }

    @Test
    fun `test not found exception problem details`() = testApplication {
        application {
            configureSerialization()
            configureStatusPages()
            routing {
                get("/test/not-found") {
                    throw NotFoundException("User with id 123 was not found.")
                }
            }
        }

        val response = client.get("/test/not-found")
        assertEquals(HttpStatusCode.NotFound, response.status)
        assertTrue(response.headers[HttpHeaders.ContentType]?.contains("application/problem+json") == true)

        val problem = objectMapper.readValue(response.bodyAsText(), ProblemDetails::class.java)
        assertEquals(404, problem.status)
        assertEquals("Resource Not Found", problem.title)
        assertEquals("User with id 123 was not found.", problem.detail)
        assertEquals("/test/not-found", problem.instance)
    }

    @Test
    fun `test conflict exception problem details`() = testApplication {
        application {
            configureSerialization()
            configureStatusPages()
            routing {
                get("/test/conflict") {
                    throw ConflictException("User with email 'test@example.com' already exists.")
                }
            }
        }

        val response = client.get("/test/conflict")
        assertEquals(HttpStatusCode.Conflict, response.status)
        assertTrue(response.headers[HttpHeaders.ContentType]?.contains("application/problem+json") == true)

        val problem = objectMapper.readValue(response.bodyAsText(), ProblemDetails::class.java)
        assertEquals(409, problem.status)
        assertEquals("Conflict", problem.title)
        assertEquals("User with email 'test@example.com' already exists.", problem.detail)
        assertEquals("/test/conflict", problem.instance)
    }

    @Test
    fun `test bad request exception problem details`() = testApplication {
        application {
            configureSerialization()
            configureStatusPages()
            routing {
                get("/test/bad-request") {
                    throw BadRequestException("User cannot follow themselves")
                }
            }
        }

        val response = client.get("/test/bad-request")
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.headers[HttpHeaders.ContentType]?.contains("application/problem+json") == true)

        val problem = objectMapper.readValue(response.bodyAsText(), ProblemDetails::class.java)
        assertEquals(400, problem.status)
        assertEquals("Bad Request", problem.title)
        assertEquals("User cannot follow themselves", problem.detail)
        assertEquals("/test/bad-request", problem.instance)
    }

    @Test
    fun `test unhandled exception masks details as 500`() = testApplication {
        application {
            configureSerialization()
            configureStatusPages()
            routing {
                get("/test/internal-error") {
                    throw RuntimeException("Database connection timeout or internal secret")
                }
            }
        }

        val response = client.get("/test/internal-error")
        assertEquals(HttpStatusCode.InternalServerError, response.status)
        assertTrue(response.headers[HttpHeaders.ContentType]?.contains("application/problem+json") == true)

        val problem = objectMapper.readValue(response.bodyAsText(), ProblemDetails::class.java)
        assertEquals(500, problem.status)
        assertEquals("Internal Server Error", problem.title)
        assertEquals("An unexpected error occurred. Please try again later.", problem.detail)
        assertEquals("/test/internal-error", problem.instance)
    }
}
