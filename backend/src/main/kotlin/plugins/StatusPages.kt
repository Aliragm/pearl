package gg.pearl.plugins

import com.fasterxml.jackson.core.JsonProcessingException
import gg.pearl.dto.ProblemDetails
import gg.pearl.exceptions.AppException
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.uri
import io.ktor.server.response.respond

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<AppException> { call, cause ->
            val problem = ProblemDetails(
                type = cause.type,
                title = cause.title,
                status = cause.status.value,
                detail = cause.detail,
                instance = call.request.uri
            )
            call.response.headers.append("Content-Type", "application/problem+json")
            call.respond(cause.status, problem)
        }

        exception<BadRequestException> { call, cause ->
            val problem = ProblemDetails(
                type = "https://pearl.com/exceptions/BadRequestException",
                title = "Bad Request",
                status = HttpStatusCode.BadRequest.value,
                detail = cause.message ?: "Invalid request parameters or payload",
                instance = call.request.uri
            )
            call.response.headers.append("Content-Type", "application/problem+json")
            call.respond(HttpStatusCode.BadRequest, problem)
        }

        exception<JsonProcessingException> { call, cause ->
            val problem = ProblemDetails(
                type = "https://pearl.com/exceptions/JsonProcessingException",
                title = "Malformed JSON",
                status = HttpStatusCode.BadRequest.value,
                detail = cause.originalMessage ?: "Malformed JSON body",
                instance = call.request.uri
            )
            call.response.headers.append("Content-Type", "application/problem+json")
            call.respond(HttpStatusCode.BadRequest, problem)
        }

        exception<Throwable> { call, cause ->
            call.application.environment.log.error("Unhandled exception: ${cause.message}", cause)
            val problem = ProblemDetails(
                type = "about:blank",
                title = "Internal Server Error",
                status = HttpStatusCode.InternalServerError.value,
                detail = "An unexpected error occurred. Please try again later.",
                instance = call.request.uri
            )
            call.response.headers.append("Content-Type", "application/problem+json")
            call.respond(HttpStatusCode.InternalServerError, problem)
        }
    }
}
