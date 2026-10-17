package gg.pearl.controller

import gg.pearl.dto.FollowsDTO
import gg.pearl.exceptions.BadRequestException
import gg.pearl.exceptions.ConflictException
import gg.pearl.exceptions.InvalidUuidException
import gg.pearl.exceptions.MissingIdException
import gg.pearl.exceptions.NotFoundException
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
import io.ktor.server.routing.openapi.describe
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.utils.io.ExperimentalKtorApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalKtorApi::class)
fun Route.followsController() {
    val followsService: FollowsService by application.dependencies

    route("/follows") {
        post {
            val request = call.receive<FollowsDTO>()

            if (request.userFollowingId == request.userFollowerId) {
                throw BadRequestException("User cannot follow themselves")
            }

            if (followsService.isFollowing(request.userFollowingId, request.userFollowerId)) {
                throw ConflictException("Already following this user")
            }

            val created = followsService.createFollow(request)
                ?: throw BadRequestException("Could not follow user")
            call.response.header(HttpHeaders.Location, "/follows/following/${request.userFollowingId}")
            call.respond(HttpStatusCode.Created, created)
        }.describe {
            summary = "Follow a user"
            description = "Creates a follow relationship between two distinct users."
        }

        delete {
            val request = call.receive<FollowsDTO>()
            val deleted = followsService.deleteFollow(request)
            if (!deleted) {
                throw NotFoundException("Follow relationship was not found.")
            }
            call.respond(HttpStatusCode.NoContent)
        }.describe {
            summary = "Unfollow a user"
            description = "Removes an existing follow relationship and returns 204 No Content."
        }

        get("/followers/{userId}") {
            val userIdParam = call.parameters["userId"] ?: throw MissingIdException("The user id parameter is required.")
            val uuid = try {
                Uuid.parse(userIdParam)
            } catch (e: IllegalArgumentException) {
                throw InvalidUuidException("The user UUID format is invalid.")
            }

            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 10

            val response = followsService.getAllFollowers(uuid, page = page, size = size)
            call.response.header("X-Total-Count", response.total.toString())
            call.respond(HttpStatusCode.OK, response)
        }.describe {
            summary = "List followers of a user"
            description = "Returns a paginated list of follower relationships for the specified user."
        }

        get("/following/{userId}") {
            val userIdParam = call.parameters["userId"] ?: throw MissingIdException("The user id parameter is required.")
            val uuid = try {
                Uuid.parse(userIdParam)
            } catch (e: IllegalArgumentException) {
                throw InvalidUuidException("The user UUID format is invalid.")
            }

            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 10

            val response = followsService.getAllFollowings(uuid, page = page, size = size)
            call.response.header("X-Total-Count", response.total.toString())
            call.respond(HttpStatusCode.OK, response)
        }.describe {
            summary = "List users followed by a user"
            description = "Returns a paginated list of following relationships for the specified user."
        }
    }
}
