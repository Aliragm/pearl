package gg.pearl.infrastructure.grpc

import gg.pearl.model.enums.Platform
import gg.pearl.model.port.CatalogSearchResult
import io.grpc.ManagedChannel
import io.grpc.Server
import io.grpc.Status
import io.grpc.StatusException
import io.grpc.inprocess.InProcessChannelBuilder
import io.grpc.inprocess.InProcessServerBuilder
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import pearl.catalog.v1.GameServiceGrpcKt
import pearl.catalog.v1.SearchRequest
import pearl.catalog.v1.SearchResponse
import pearl.catalog.v1.game
import pearl.catalog.v1.searchResponse
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class GrpcGameCatalogAdapterTest {

    private var server: Server? = null
    private var channel: ManagedChannel? = null

    private fun startInProcessServer(
        serviceImpl: GameServiceGrpcKt.GameServiceCoroutineImplBase
    ): ManagedChannel {
        val serverName = InProcessServerBuilder.generateName()
        server = InProcessServerBuilder
            .forName(serverName)
            .directExecutor()
            .addService(serviceImpl)
            .build()
            .start()

        val ch = InProcessChannelBuilder
            .forName(serverName)
            .directExecutor()
            .build()
        channel = ch
        return ch
    }

    @AfterTest
    fun tearDown() {
        channel?.shutdownNow()
        server?.shutdownNow()
    }

    @Test
    fun `searchGames maps protobuf response to domain Game list`() = runBlocking {
        val ch = startInProcessServer(object : GameServiceGrpcKt.GameServiceCoroutineImplBase() {
            override suspend fun search(request: SearchRequest): SearchResponse {
                assertEquals("zelda", request.searchString)
                return searchResponse {
                    games.add(
                        game {
                            id = 1022
                            name = "The Legend of Zelda"
                            summary = "Adventure classic."
                            coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co1uii.jpg"
                            platforms.addAll(listOf("NINTENDO", "PC", "UNKNOWN_CONSOLE", "NINTENDO"))
                            companies.addAll(listOf("Nintendo", "Nintendo R&D4"))
                        }
                    )
                }
            }
        })

        val adapter = GrpcGameCatalogAdapter(ch, deadlineMs = 2000L)
        val result = adapter.searchGames("zelda")

        val success = assertIs<CatalogSearchResult.Success>(result)
        assertEquals(1, success.games.size)
        val g = success.games.first()
        assertEquals(1022, g.externalId)
        assertEquals("The Legend of Zelda", g.name)
        assertEquals("Adventure classic.", g.summary)
        assertEquals("https://images.igdb.com/igdb/image/upload/t_cover_big/co1uii.jpg", g.cover)
        assertEquals("Nintendo, Nintendo R&D4", g.developer)
        assertEquals(listOf(Platform.NINTENDO, Platform.PC), g.platform)
    }

    @Test
    fun `searchGames returns Unavailable when gRPC server is unavailable`() = runBlocking {
        val ch = startInProcessServer(object : GameServiceGrpcKt.GameServiceCoroutineImplBase() {
            override suspend fun search(request: SearchRequest): SearchResponse {
                throw StatusException(Status.UNAVAILABLE.withDescription("ingestor down"))
            }
        })

        val adapter = GrpcGameCatalogAdapter(ch, deadlineMs = 2000L)
        val result = adapter.searchGames("zelda")

        val unavailable = assertIs<CatalogSearchResult.Unavailable>(result)
        assertEquals("UNAVAILABLE", unavailable.reason)
    }

    @Test
    fun `searchGames enforces withDeadlineAfter and returns Unavailable on timeout`() = runBlocking {
        val ch = startInProcessServer(object : GameServiceGrpcKt.GameServiceCoroutineImplBase() {
            override suspend fun search(request: SearchRequest): SearchResponse {
                delay(300)
                return searchResponse {}
            }
        })

        val adapter = GrpcGameCatalogAdapter(ch, deadlineMs = 50L)
        val result = adapter.searchGames("zelda")

        val unavailable = assertIs<CatalogSearchResult.Unavailable>(result)
        assertEquals("DEADLINE_EXCEEDED", unavailable.reason)
    }

    @Test
    fun `searchGames returns empty Success on INVALID_ARGUMENT`() = runBlocking {
        val ch = startInProcessServer(object : GameServiceGrpcKt.GameServiceCoroutineImplBase() {
            override suspend fun search(request: SearchRequest): SearchResponse {
                throw StatusException(Status.INVALID_ARGUMENT.withDescription("empty search"))
            }
        })

        val adapter = GrpcGameCatalogAdapter(ch, deadlineMs = 2000L)
        val result = adapter.searchGames("zelda")

        val success = assertIs<CatalogSearchResult.Success>(result)
        assertEquals(emptyList(), success.games)
    }
}
