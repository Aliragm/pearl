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
import org.jetbrains.exposed.exceptions.ExposedSQLException
import java.sql.SQLException

private fun mapSqlExceptionToProblem(sqlEx: SQLException, instanceUri: String): Pair<HttpStatusCode, ProblemDetails>? {
    val state = sqlEx.sqlState ?: return null
    return when {
        state == "23505" -> {
            val status = HttpStatusCode.Conflict
            status to ProblemDetails(
                type = "https://pearl.com/exceptions/ConflictException",
                title = "Conflict",
                status = status.value,
                detail = "A resource with the provided unique constraint already exists.",
                instance = instanceUri
            )
        }
        state == "23503" -> {
            val status = HttpStatusCode.UnprocessableEntity
            status to ProblemDetails(
                type = "https://pearl.com/exceptions/ForeignKeyViolation",
                title = "Referenced Resource Not Found",
                status = status.value,
                detail = "One or more referenced IDs (such as userId, gameId, or postId) do not exist.",
                instance = instanceUri
            )
        }
        state == "23514" || state == "23502" || state.startsWith("22") -> {
            val status = HttpStatusCode.BadRequest
            status to ProblemDetails(
                type = "https://pearl.com/exceptions/ConstraintViolation",
                title = "Constraint Violation",
                status = status.value,
                detail = "The request violates a database integrity check or data format constraint.",
                instance = instanceUri
            )
        }
        else -> null
    }
}

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
            val jsonCause = generateSequence<Throwable>(cause) { it.cause }
                .filterIsInstance<JsonProcessingException>()
                .firstOrNull()
            val detailMsg = jsonCause?.originalMessage
                ?: cause.cause?.message
                ?: cause.message
                ?: "Invalid request parameters or payload"

            val problem = ProblemDetails(
                type = "https://pearl.com/exceptions/BadRequestException",
                title = "Bad Request",
                status = HttpStatusCode.BadRequest.value,
                detail = detailMsg,
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

        exception<ExposedSQLException> { call, cause ->
            val sqlCause = cause.cause as? SQLException
            val mapped = sqlCause?.let { mapSqlExceptionToProblem(it, call.request.uri) }
            if (mapped != null) {
                call.response.headers.append("Content-Type", "application/problem+json")
                call.respond(mapped.first, mapped.second)
            } else {
                call.application.environment.log.error("Database exception: ${cause.message}", cause)
                val problem = ProblemDetails(
                    type = "about:blank",
                    title = "Internal Server Error",
                    status = HttpStatusCode.InternalServerError.value,
                    detail = "An unexpected database error occurred.",
                    instance = call.request.uri
                )
                call.response.headers.append("Content-Type", "application/problem+json")
                call.respond(HttpStatusCode.InternalServerError, problem)
            }
        }

        exception<SQLException> { call, cause ->
            val mapped = mapSqlExceptionToProblem(cause, call.request.uri)
            if (mapped != null) {
                call.response.headers.append("Content-Type", "application/problem+json")
                call.respond(mapped.first, mapped.second)
            } else {
                call.application.environment.log.error("SQL exception: ${cause.message}", cause)
                val problem = ProblemDetails(
                    type = "about:blank",
                    title = "Internal Server Error",
                    status = HttpStatusCode.InternalServerError.value,
                    detail = "An unexpected database error occurred.",
                    instance = call.request.uri
                )
                call.response.headers.append("Content-Type", "application/problem+json")
                call.respond(HttpStatusCode.InternalServerError, problem)
            }
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
