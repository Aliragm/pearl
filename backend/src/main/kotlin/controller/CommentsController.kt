package gg.pearl.controller

import gg.pearl.dto.CommentsDTO
import gg.pearl.dto.UpdateCommentDTO
import gg.pearl.service.CommentsService
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

fun Route.commentsController() {
    val commentsService: CommentsService by application.dependencies

    route("/comments") {
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

            val comment = commentsService.getCommentById(uuid)
            if (comment != null) {
                call.respond(HttpStatusCode.OK, comment)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        get("/post/{postId}") {
            val postIdParam = call.parameters["postId"]
            if (postIdParam == null) {
                call.respond(HttpStatusCode.BadRequest, "Missing postId parameter")
                return@get
            }

            val postUuid = try {
                Uuid.parse(postIdParam)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, "Invalid UUID format")
                return@get
            }

            val userId = call.request.queryParameters["userId"]?.let {
                try { Uuid.parse(it) } catch (e: Exception) { null }
            }
            val content = call.request.queryParameters["content"]
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 10

            val response = commentsService.getPostComments(
                postId = postUuid,
                userId = userId,
                content = content,
                page = page,
                size = size
            )
            call.response.header("X-Total-Count", response.total.toString())
            call.respond(HttpStatusCode.OK, response)
        }

        post {
            val request = call.receive<CommentsDTO>()
            val created = commentsService.createComment(request)
            if (created != null) {
                call.response.header(HttpHeaders.Location, "/comments/${created.id}")
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

            val request = call.receive<UpdateCommentDTO>()
            val updated = commentsService.updateComment(uuid, request)
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

            val deleted = commentsService.deleteComment(uuid)
            if (deleted) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}
