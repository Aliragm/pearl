package gg.pearl.controller

import gg.pearl.dto.UpdateUserDTO
import gg.pearl.dto.UserDTO
import gg.pearl.dto.validate
import gg.pearl.exceptions.BadRequestException
import gg.pearl.exceptions.ConflictException
import gg.pearl.exceptions.InvalidUuidException
import gg.pearl.exceptions.MissingIdException
import gg.pearl.exceptions.NotFoundException
import gg.pearl.service.UserService
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.request.receive
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.openapi.describe
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.utils.io.ExperimentalKtorApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalKtorApi::class)
fun Route.userController() {
    val userService: UserService by application.dependencies

    route("/users") {
        get {
            val name = call.request.queryParameters["name"]
            val email = call.request.queryParameters["email"]
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 10

            val response = userService.getUsers(name = name, email = email, page = page, size = size)
            call.response.header("X-Total-Count", response.total.toString())
            call.respond(HttpStatusCode.OK, response)
        }.describe {
            summary = "List users (paginated with filters)"
            description = "Returns a paginated list of users filtered by name and/or email."
        }

        get("/{id}") {
            val idParam = call.parameters["id"] ?: throw MissingIdException("The user id parameter is required.")
            val uuid = try {
                Uuid.parse(idParam)
            } catch (e: IllegalArgumentException) {
                throw InvalidUuidException("The user UUID format is invalid.")
            }

            val user = userService.getUserById(uuid)
                ?: throw NotFoundException("User with id $uuid was not found.")
            call.respond(HttpStatusCode.OK, user)
        }.describe {
            summary = "Get user by ID"
            description = "Fetches a single user by UUID or returns 404 in Problem Details."
        }

        post {
            val request = call.receive<UserDTO>()
            request.validate()

            val existing = userService.getUserByEmail(request.email)
            if (existing != null) {
                throw ConflictException("User with email '${request.email}' already exists.")
            }

            val entity: UserDTO = userService.createUser(request)
                ?: throw BadRequestException("Could not create user")
            call.response.header(HttpHeaders.Location, "/users/${entity.id}")
            call.respond(HttpStatusCode.Created, entity)
        }.describe {
            summary = "Create a new user"
            description = "Validates input and creates a user, returning 201 Created with Location header or 409/422."
        }

        patch("/{id}") {
            val idParam = call.parameters["id"] ?: throw MissingIdException("The user id parameter is required.")
            val uuid = try {
                Uuid.parse(idParam)
            } catch (e: IllegalArgumentException) {
                throw InvalidUuidException("The user UUID format is invalid.")
            }

            val request = call.receive<UpdateUserDTO>()
            request.validate()

            val entity: UserDTO = userService.updateUser(uuid, request)
                ?: throw NotFoundException("User with id $uuid was not found.")
            call.respond(HttpStatusCode.OK, entity)
        }.describe {
            summary = "Update user by ID"
            description = "Partially updates user fields (name, email) after validation."
        }

        delete("/{id}") {
            val idParam = call.parameters["id"] ?: throw MissingIdException("The user id parameter is required.")
            val uuid = try {
                Uuid.parse(idParam)
            } catch (e: IllegalArgumentException) {
                throw InvalidUuidException("The user UUID format is invalid.")
            }

            val deleted = userService.deleteUser(uuid)
            if (!deleted) {
                throw NotFoundException("User with id $uuid was not found.")
            }
            call.respond(HttpStatusCode.NoContent)
        }.describe {
            summary = "Delete user by ID"
            description = "Deletes an existing user and returns 204 No Content."
        }
    }
}