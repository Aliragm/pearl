package gg.pearl.plugins

import gg.pearl.repository.CommentsRepository
import gg.pearl.repository.CommentsRepositoryImpl
import gg.pearl.repository.FollowsRepository
import gg.pearl.repository.FollowsRepositoryImpl
import gg.pearl.repository.GameRepository
import gg.pearl.repository.GameRepositoryImpl
import gg.pearl.repository.PostRepository
import gg.pearl.repository.PostRepositoryImpl
import gg.pearl.repository.UserRepository
import gg.pearl.repository.UserRepositoryImpl
import gg.pearl.service.CommentsService
import gg.pearl.service.FollowsService
import gg.pearl.service.GameService
import gg.pearl.service.PostService
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

        provide<GameRepository> {
            GameRepositoryImpl()
        }
        provide<GameService> {
            GameService(resolve())
        }

        provide<PostRepository> {
            PostRepositoryImpl()
        }
        provide<PostService> {
            PostService(resolve())
        }

        provide<CommentsRepository> {
            CommentsRepositoryImpl()
        }
        provide<CommentsService> {
            CommentsService(resolve())
        }

        provide<FollowsRepository> {
            FollowsRepositoryImpl()
        }
        provide<FollowsService> {
            FollowsService(resolve())
        }
    }
}
