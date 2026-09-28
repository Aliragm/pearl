package gg.pearl.controller

import gg.pearl.dto.FollowsDTO
import gg.pearl.service.FollowsService
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
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlin.uuid.Uuid

fun Route.followsController() {
    val followsService: FollowsService by application.dependencies

    route("/follows") {
        post {
            val request = call.receive<FollowsDTO>()

            if (request.userFollowingId == request.userFollowerId) {
                call.respond(HttpStatusCode.BadRequest, "User cannot follow themselves")
                return@post
            }

            if (followsService.isFollowing(request.userFollowingId, request.userFollowerId)) {
                call.respond(HttpStatusCode.Conflict, "Already following this user")
                return@post
            }

            val created = followsService.createFollow(request)
            if (created != null) {
                call.response.header(HttpHeaders.Location, "/follows/following/${request.userFollowingId}")
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

            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 10

            val response = followsService.getAllFollowers(uuid, page = page, size = size)
            call.response.header("X-Total-Count", response.total.toString())
            call.respond(HttpStatusCode.OK, response)
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

            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 10

            val response = followsService.getAllFollowings(uuid, page = page, size = size)
            call.response.header("X-Total-Count", response.total.toString())
            call.respond(HttpStatusCode.OK, response)
        }
    }
}
