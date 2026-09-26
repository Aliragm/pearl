package gg.pearl.plugins

import gg.pearl.repository.UserRepository
import gg.pearl.repository.UserRepositoryImpl
import gg.pearl.service.UserService
import io.ktor.server.application.*
import io.ktor.server.plugins.di.*

fun Application.configureDependencyInjection() {
    dependencies {
        provide<UserRepository> {
            UserRepositoryImpl()
        }
        provide<UserService> {
            UserService(resolve())
        }
    }
}
