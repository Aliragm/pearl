package gg.pearl.controller

import gg.pearl.dto.PostDTO
import gg.pearl.dto.UpdatePostDTO
import gg.pearl.exceptions.BadRequestException
import gg.pearl.exceptions.InvalidUuidException
import gg.pearl.exceptions.MissingIdException
import gg.pearl.exceptions.NotFoundException
import gg.pearl.service.PostService
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

fun Route.postController() {
    val postService: PostService by application.dependencies

    route("/posts") {
        get {
            val title = call.request.queryParameters["title"]
            val userId = call.request.queryParameters["userId"]?.let {
                try { Uuid.parse(it) } catch (e: Exception) { null }
            }
            val gameId = call.request.queryParameters["gameId"]?.let {
                try { Uuid.parse(it) } catch (e: Exception) { null }
            }
            val minScore = call.request.queryParameters["minScore"]?.toDoubleOrNull()
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 10

            val response = postService.getPosts(
                title = title,
                userId = userId,
                gameId = gameId,
                minScore = minScore,
                page = page,
                size = size
            )
            call.response.header("X-Total-Count", response.total.toString())
            call.respond(HttpStatusCode.OK, response)
        }

        get("/{id}") {
            val idParam = call.parameters["id"] ?: throw MissingIdException("The post id parameter is required.")
            val uuid = try {
                Uuid.parse(idParam)
            } catch (e: IllegalArgumentException) {
                throw InvalidUuidException("The post UUID format is invalid.")
            }

            val post = postService.getPostById(uuid)
                ?: throw NotFoundException("Post with id $uuid was not found.")
            call.respond(HttpStatusCode.OK, post)
        }

        get("/user/{userId}") {
            val userIdParam = call.parameters["userId"] ?: throw MissingIdException("The user id parameter is required.")
            val userUuid = try {
                Uuid.parse(userIdParam)
            } catch (e: IllegalArgumentException) {
                throw InvalidUuidException("The user UUID format is invalid.")
            }

            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 10

            val response = postService.getPostsByUserId(userUuid, page = page, size = size)
            call.response.header("X-Total-Count", response.total.toString())
            call.respond(HttpStatusCode.OK, response)
        }

        post {
            val request = call.receive<PostDTO>()
            val created = postService.createPost(request)
                ?: throw BadRequestException("Could not create post")
            call.response.header(HttpHeaders.Location, "/posts/${created.id}")
            call.respond(HttpStatusCode.Created, created)
        }

        patch("/{id}") {
            val idParam = call.parameters["id"] ?: throw MissingIdException("The post id parameter is required.")
            val uuid = try {
                Uuid.parse(idParam)
            } catch (e: IllegalArgumentException) {
                throw InvalidUuidException("The post UUID format is invalid.")
            }

            val request = call.receive<UpdatePostDTO>()
            val updated = postService.updatePost(uuid, request)
                ?: throw NotFoundException("Post with id $uuid was not found.")
            call.respond(HttpStatusCode.OK, updated)
        }

        delete("/{id}") {
            val idParam = call.parameters["id"] ?: throw MissingIdException("The post id parameter is required.")
            val uuid = try {
                Uuid.parse(idParam)
            } catch (e: IllegalArgumentException) {
                throw InvalidUuidException("The post UUID format is invalid.")
            }

            val deleted = postService.deletePost(uuid)
            if (!deleted) {
                throw NotFoundException("Post with id $uuid was not found.")
            }
            call.respond(HttpStatusCode.NoContent)
        }
    }
}
