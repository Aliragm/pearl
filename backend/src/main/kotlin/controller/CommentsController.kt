package gg.pearl.controller

import gg.pearl.dto.CommentsDTO
import gg.pearl.dto.UpdateCommentDTO
import gg.pearl.dto.validate
import gg.pearl.exceptions.BadRequestException
import gg.pearl.exceptions.InvalidUuidException
import gg.pearl.exceptions.MissingIdException
import gg.pearl.exceptions.NotFoundException
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
import io.ktor.server.routing.openapi.describe
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.utils.io.ExperimentalKtorApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalKtorApi::class)
fun Route.commentsController() {
    val commentsService: CommentsService by application.dependencies

    route("/comments") {
        get("/{id}") {
            val idParam = call.parameters["id"] ?: throw MissingIdException("The comment id parameter is required.")
            val uuid = try {
                Uuid.parse(idParam)
            } catch (e: IllegalArgumentException) {
                throw InvalidUuidException("The comment UUID format is invalid.")
            }

            val comment = commentsService.getCommentById(uuid)
                ?: throw NotFoundException("Comment with id $uuid was not found.")
            call.respond(HttpStatusCode.OK, comment)
        }.describe {
            summary = "Get comment by ID"
            description = "Fetches a single comment by UUID."
        }

        get("/post/{postId}") {
            val postIdParam = call.parameters["postId"] ?: throw MissingIdException("The comment post id parameter is required.")
            val postUuid = try {
                Uuid.parse(postIdParam)
            } catch (e: IllegalArgumentException) {
                throw InvalidUuidException("The comment post UUID format is invalid.")
            }

            val userId = call.request.queryParameters["userId"]?.let {
                try {
                    Uuid.parse(it)
                } catch (e: IllegalArgumentException) {
                    throw InvalidUuidException("The userId query parameter UUID format is invalid.")
                }
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
        }.describe {
            summary = "List comments for a post (paginated with filters)"
            description = "Returns paginated comments belonging to a specific post, filtered by userId and content."
        }

        post {
            val request = call.receive<CommentsDTO>()
            request.validate()

            val created = commentsService.createComment(request)
                ?: throw BadRequestException("Could not create comment")
            call.response.header(HttpHeaders.Location, "/comments/${created.id}")
            call.respond(HttpStatusCode.Created, created)
        }.describe {
            summary = "Create a comment"
            description = "Validates and creates a comment on a post."
        }

        patch("/{id}") {
            val idParam = call.parameters["id"] ?: throw MissingIdException("The comment id parameter is required.")
            val uuid = try {
                Uuid.parse(idParam)
            } catch (e: IllegalArgumentException) {
                throw InvalidUuidException("The comment UUID format is invalid.")
            }

            val request = call.receive<UpdateCommentDTO>()
            request.validate()

            val updated = commentsService.updateComment(uuid, request)
                ?: throw NotFoundException("Comment with id $uuid was not found.")
            call.respond(HttpStatusCode.OK, updated)
        }.describe {
            summary = "Update comment by ID"
            description = "Updates comment content and marks it as edited."
        }

        delete("/{id}") {
            val idParam = call.parameters["id"] ?: throw MissingIdException("The comment id parameter is required.")
            val uuid = try {
                Uuid.parse(idParam)
            } catch (e: IllegalArgumentException) {
                throw InvalidUuidException("The comment UUID format is invalid.")
            }

            val deleted = commentsService.deleteComment(uuid)
            if (!deleted) {
                throw NotFoundException("Comment with id $uuid was not found.")
            }
            call.respond(HttpStatusCode.NoContent)
        }.describe {
            summary = "Delete comment by ID"
            description = "Deletes an existing comment and returns 204 No Content."
        }
    }
}
