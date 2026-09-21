package gg.pearl.plugins

import gg.pearl.repository.UserRepository
import gg.pearl.repository.UserRepositoryImpl
import io.ktor.server.application.*
import io.ktor.server.plugins.di.*

// The contents of the `install` function will be used for the project template
fun Application.configureDependencyInjection() {
    dependencies {
        provide<UserRepository> {
            UserRepositoryImpl()
        }
    }
}
