package gg.pearl.controller

import gg.pearl.dto.UpdateUserDTO
import gg.pearl.dto.UserDTO
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
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlin.uuid.Uuid

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
        }

        post {
            val request = call.receive<UserDTO>()

            val existing = userService.getUserByEmail(request.email)
            if (existing != null) {
                throw ConflictException("User with email '${request.email}' already exists.")
            }

            val entity: UserDTO = userService.createUser(request)
                ?: throw BadRequestException("Could not create user")
            call.response.header(HttpHeaders.Location, "/users/${entity.id}")
            call.respond(HttpStatusCode.Created, entity)
        }

        patch("/{id}") {
            val idParam = call.parameters["id"] ?: throw MissingIdException("The user id parameter is required.")
            val uuid = try {
                Uuid.parse(idParam)
            } catch (e: IllegalArgumentException) {
                throw InvalidUuidException("The user UUID format is invalid.")
            }

            val request = call.receive<UpdateUserDTO>()
            val entity: UserDTO = userService.updateUser(uuid, request)
                ?: throw NotFoundException("User with id $uuid was not found.")
            call.respond(HttpStatusCode.OK, entity)
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
        }
    }
}