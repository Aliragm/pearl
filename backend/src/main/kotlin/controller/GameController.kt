package gg.pearl.controller

import gg.pearl.dto.GameDTO
import gg.pearl.dto.UpdateGameDTO
import gg.pearl.model.enums.Platform
import gg.pearl.service.GameService
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

fun Route.gameController() {
    val gameService: GameService by application.dependencies

    route("/games") {
        get {
            val name = call.request.queryParameters["name"]
            val developer = call.request.queryParameters["developer"]
            val platformStr = call.request.queryParameters["platform"]
            val platform = platformStr?.let {
                try {
                    Platform.valueOf(it.uppercase())
                } catch (e: IllegalArgumentException) {
                    null
                }
            }
            val minScore = call.request.queryParameters["minScore"]?.toDoubleOrNull()
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 10

            val response = gameService.getGames(
                name = name,
                developer = developer,
                platform = platform,
                minScore = minScore,
                page = page,
                size = size
            )
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

            val game = gameService.getGameById(uuid)
            if (game != null) {
                call.respond(HttpStatusCode.OK, game)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        post {
            val request = call.receive<GameDTO>()
            val created = gameService.createGame(request)
            if (created != null) {
                call.response.header(HttpHeaders.Location, "/games/${created.id}")
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

            val request = call.receive<UpdateGameDTO>()
            val updated = gameService.updateGame(uuid, request)
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

            val deleted = gameService.deleteGame(uuid)
            if (deleted) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}
