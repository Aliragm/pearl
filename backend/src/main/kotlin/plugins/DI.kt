package gg.pearl.plugins

import gg.pearl.infrastructure.grpc.GrpcGameCatalogAdapter
import gg.pearl.model.port.GameCatalogPort
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
import io.grpc.ManagedChannelBuilder
import io.ktor.server.application.*
import io.ktor.server.plugins.di.*

fun Application.configureDependencyInjection() {
    val grpcHost = System.getenv("INGESTOR_GRPC_HOST")?.takeIf { it.isNotBlank() }
        ?: environment.config.propertyOrNull("ingestor.grpc.host")?.getString()?.takeIf { it.isNotBlank() }
        ?: "localhost"
    val grpcPort = System.getenv("INGESTOR_GRPC_PORT")?.toIntOrNull()
        ?: environment.config.propertyOrNull("ingestor.grpc.port")?.getString()?.toIntOrNull()
        ?: 8081
    val grpcDeadlineMs = System.getenv("INGESTOR_GRPC_DEADLINE_MS")?.toLongOrNull()
        ?: environment.config.propertyOrNull("ingestor.grpc.deadlineMs")?.getString()?.toLongOrNull()
        ?: 3000L

    val ingestorChannel = ManagedChannelBuilder
        .forAddress(grpcHost, grpcPort)
        .usePlaintext()
        .build()

    monitor.subscribe(ApplicationStopped) {
        ingestorChannel.shutdown()
    }

    dependencies {
        provide<UserRepository> {
            UserRepositoryImpl()
        }
        provide<UserService> {
            UserService(resolve())
        }

        provide<GameCatalogPort> {
            GrpcGameCatalogAdapter(ingestorChannel, grpcDeadlineMs)
        }
        provide<GameRepository> {
            GameRepositoryImpl()
        }
        provide<GameService> {
            GameService(resolve(), resolve())
        }

        provide<PostRepository> {
            PostRepositoryImpl()
        }
        provide<PostService> {
            PostService(resolve(), resolve())
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
