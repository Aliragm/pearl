package gg.pearl.controller

import gg.pearl.dto.UserDTO
import gg.pearl.service.UserService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.response.respond
import io.ktor.server.routing.get


fun Route.userController(){
    val userService: UserService = UserService()

    route("/users"){
        get {
            call.respond(HttpStatusCode.OK, userService.getUsers())
        }

        post {
            val request = call.receive<UserDTO>()

            val entity: UserDTO? = userService.createUser(request)
            if(entity != null) {
                call.respond(HttpStatusCode.Created, entity)
            } else {
                call.respond(HttpStatusCode.BadRequest)
            }
        }
    }
}