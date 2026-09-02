package gg.pearl

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.fasterxml.jackson.databind.*
import io.ktor.http.ContentType
import io.ktor.serialization.jackson.*
import io.ktor.server.plugins.contentnegotiation.*

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello, World!")
        }
        get("/json/jackson") {
                call.respond(mapOf("hello" to "world"))
            }

    }
}