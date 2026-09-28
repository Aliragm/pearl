package gg.pearl.controller

import gg.pearl.dto.UpdateUserDTO
import gg.pearl.dto.UserDTO
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
            val idParam = call.parameters["id"]
            if (idParam == null) {
                call.respond(HttpStatusCode.BadRequest, "Missing id parameter")
                return@get
            }

            val uuid = try {
                Uuid.parse(idParam)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, "Invalid UUID format")
                return@get
            }

            val user = userService.getUserById(uuid)
            if (user != null) {
                call.respond(HttpStatusCode.OK, user)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        post {
            val request = call.receive<UserDTO>()

            val existing = userService.getUserByEmail(request.email)
            if (existing != null) {
                call.respond(HttpStatusCode.Conflict, "User with this email already exists")
                return@post
            }

            val entity: UserDTO? = userService.createUser(request)
            if (entity != null) {
                call.response.header(HttpHeaders.Location, "/users/${entity.id}")
                call.respond(HttpStatusCode.Created, entity)
            } else {
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        patch("/{id}") {
            val idParam = call.parameters["id"]
            if (idParam == null) {
                call.respond(HttpStatusCode.BadRequest, "Missing id parameter")
                return@patch
            }

            val uuid = try {
                Uuid.parse(idParam)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, "Invalid UUID format")
                return@patch
            }

            val request = call.receive<UpdateUserDTO>()

            val entity: UserDTO? = userService.updateUser(uuid, request)
            if (entity != null) {
                call.respond(HttpStatusCode.OK, entity)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        delete("/{id}") {
            val idParam = call.parameters["id"]
            if (idParam == null) {
                call.respond(HttpStatusCode.BadRequest, "Missing id parameter")
                return@delete
            }

            val uuid = try {
                Uuid.parse(idParam)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, "Invalid UUID format")
                return@delete
            }

            val deleted = userService.deleteUser(uuid)
            if (deleted) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}