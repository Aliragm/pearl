package gg.pearl

import gg.pearl.controller.commentsController
import gg.pearl.controller.followsController
import gg.pearl.controller.gameController
import gg.pearl.controller.postController
import gg.pearl.controller.userController
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
        userController()
        gameController()
        postController()
        commentsController()
        followsController()
    }
}