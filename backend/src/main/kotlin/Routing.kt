package gg.pearl

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import gg.pearl.controller.userController

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
        userController()
    }
}