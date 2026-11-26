package gg.pearl.infrastructure.grpc

import gg.pearl.model.Game
import gg.pearl.model.enums.Platform
import gg.pearl.model.port.CatalogSearchResult
import gg.pearl.model.port.GameCatalogPort
import io.grpc.Channel
import io.grpc.Status
import io.grpc.StatusException
import io.grpc.StatusRuntimeException
import org.slf4j.LoggerFactory
import pearl.catalog.v1.GameServiceGrpcKt
import pearl.catalog.v1.searchRequest
import java.util.concurrent.TimeUnit

class GrpcGameCatalogAdapter(
    channel: Channel,
    private val deadlineMs: Long = 3000L
) : GameCatalogPort {

    private val logger = LoggerFactory.getLogger(GrpcGameCatalogAdapter::class.java)
    private val stub = GameServiceGrpcKt.GameServiceCoroutineStub(channel)

    override suspend fun searchGames(query: String): CatalogSearchResult {
        if (query.isBlank()) {
            return CatalogSearchResult.Success(emptyList())
        }

        val request = searchRequest {
            searchString = query
        }

        return try {
            val response = stub
                .withDeadlineAfter(deadlineMs, TimeUnit.MILLISECONDS)
                .search(request)

            val games = response.gamesList.map { protoGame ->
                val mappedPlatforms = protoGame.platformsList
                    .mapNotNull { raw -> runCatching { Platform.valueOf(raw.uppercase()) }.getOrNull() }
                    .distinct()

                val developerName = protoGame.companiesList
                    .filter { it.isNotBlank() }
                    .joinToString(", ")
                    .take(255)
                    .ifBlank { "Unknown" }

                Game(
                    externalId = protoGame.id,
                    cover = protoGame.coverUrl,
                    platform = mappedPlatforms,
                    name = protoGame.name,
                    summary = protoGame.summary,
                    developer = developerName,
                    meanScore = 0.0
                )
            }
            CatalogSearchResult.Success(games)
        } catch (e: StatusException) {
            handleGrpcStatus(e.status, e)
        } catch (e: StatusRuntimeException) {
            handleGrpcStatus(e.status, e)
        }
    }

    private fun handleGrpcStatus(status: Status, cause: Throwable): CatalogSearchResult {
        return when (status.code) {
            Status.Code.INVALID_ARGUMENT -> CatalogSearchResult.Success(emptyList())
            Status.Code.UNAVAILABLE, Status.Code.DEADLINE_EXCEEDED -> {
                logger.warn("Catalog gRPC service {} ({}): falling back to PostgreSQL", status.code, status.description)
                CatalogSearchResult.Unavailable(status.code.name)
            }
            else -> {
                logger.warn("Catalog gRPC call failed with {} ({}): falling back to PostgreSQL", status.code, status.description, cause)
                CatalogSearchResult.Unavailable(status.code.name)
            }
        }
    }
}
