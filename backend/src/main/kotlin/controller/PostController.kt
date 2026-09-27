package gg.pearl.controller

import gg.pearl.dto.PostDTO
import gg.pearl.dto.UpdatePostDTO
import gg.pearl.service.PostService
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.request.receive
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
            call.respond(HttpStatusCode.OK, postService.getPosts())
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

            val post = postService.getPostById(uuid)
            if (post != null) {
                call.respond(HttpStatusCode.OK, post)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        get("/user/{userId}") {
            val userIdParam = call.parameters["userId"]
            if (userIdParam == null) {
                call.respond(HttpStatusCode.BadRequest, "Missing userId parameter")
                return@get
            }

            val userUuid = try {
                Uuid.parse(userIdParam)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, "Invalid UUID format")
                return@get
            }

            call.respond(HttpStatusCode.OK, postService.getPostsByUserId(userUuid))
        }

        post {
            val request = call.receive<PostDTO>()
            val created = postService.createPost(request)
            if (created != null) {
                call.respond(HttpStatusCode.Created, created)
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

            val request = call.receive<UpdatePostDTO>()
            val updated = postService.updatePost(uuid, request)
            if (updated != null) {
                call.respond(HttpStatusCode.OK, updated)
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

            val deleted = postService.deletePost(uuid)
            if (deleted) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}
