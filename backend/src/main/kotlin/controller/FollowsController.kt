package gg.pearl.controller

import gg.pearl.dto.FollowsDTO
import gg.pearl.service.FollowsService
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlin.uuid.Uuid

fun Route.followsController() {
    val followsService: FollowsService by application.dependencies

    route("/follows") {
        post {
            val request = call.receive<FollowsDTO>()
            val created = followsService.createFollow(request)
            if (created != null) {
                call.respond(HttpStatusCode.Created, created)
            } else {
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        delete {
            val request = call.receive<FollowsDTO>()
            val deleted = followsService.deleteFollow(request)
            if (deleted) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        get("/followers/{userId}") {
            val userIdParam = call.parameters["userId"]
            if (userIdParam == null) {
                call.respond(HttpStatusCode.BadRequest, "Missing userId parameter")
                return@get
            }

            val uuid = try {
                Uuid.parse(userIdParam)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, "Invalid UUID format")
                return@get
            }

            call.respond(HttpStatusCode.OK, followsService.getAllFollowers(uuid))
        }

        get("/following/{userId}") {
            val userIdParam = call.parameters["userId"]
            if (userIdParam == null) {
                call.respond(HttpStatusCode.BadRequest, "Missing userId parameter")
                return@get
            }

            val uuid = try {
                Uuid.parse(userIdParam)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, "Invalid UUID format")
                return@get
            }

            call.respond(HttpStatusCode.OK, followsService.getAllFollowings(uuid))
        }
    }
}
