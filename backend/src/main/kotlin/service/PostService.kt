package gg.pearl.service

import gg.pearl.dto.PageResponse
import gg.pearl.dto.PostDTO
import gg.pearl.dto.UpdatePostDTO
import gg.pearl.exceptions.ValidationException
import gg.pearl.model.Game
import gg.pearl.model.Post
import gg.pearl.repository.GameRepository
import gg.pearl.repository.PostRepository
import kotlin.uuid.Uuid

class PostService(
    private val postRepository: PostRepository,
    private val gameRepository: GameRepository
) {

    suspend fun getPosts(
        title: String? = null,
        userId: Uuid? = null,
        gameId: Uuid? = null,
        minScore: Double? = null,
        page: Int = 1,
        size: Int = 10
    ): PageResponse<PostDTO> {
        val (posts, total) = postRepository.getPosts(title, userId, gameId, minScore, page, size)
        return PageResponse(
            items = posts.map(::convertPostToDTO),
            page = page.coerceAtLeast(1),
            size = size.coerceIn(1, 50),
            total = total
        )
    }

    suspend fun getPostById(id: Uuid): PostDTO? {
        return postRepository.getPostById(id)?.let(::convertPostToDTO)
    }

    suspend fun getPostsByUserId(userId: Uuid, page: Int = 1, size: Int = 10): PageResponse<PostDTO> {
        val (posts, total) = postRepository.getAllByUserId(userId, page, size)
        return PageResponse(
            items = posts.map(::convertPostToDTO),
            page = page.coerceAtLeast(1),
            size = size.coerceIn(1, 50),
            total = total
        )
    }

    suspend fun createPost(post: PostDTO): PostDTO? {
        val resolvedGameId = when {
            post.gameId != null -> post.gameId
            post.game != null -> {
                val gameToPersist = Game(
                    externalId = post.game.externalId,
                    cover = post.game.cover,
                    summary = post.game.summary,
                    platform = post.game.platform.distinct(),
                    name = post.game.name,
                    developer = post.game.developer,
                    meanScore = post.game.meanScore
                )
                gameRepository.findOrCreateByExternalId(gameToPersist).id
            }
            else -> throw ValidationException("Either gameId or game metadata must be provided.")
        }

        val newPost = convertDTOToPost(post, resolvedGameId)
        val result = postRepository.createPost(newPost)
        return result?.let(::convertPostToDTO)
    }

    suspend fun updatePost(id: Uuid, dto: UpdatePostDTO): PostDTO? {
        val updated = postRepository.updatePost(id, dto)
        return updated?.let(::convertPostToDTO)
    }

    suspend fun deletePost(id: Uuid): Boolean {
        return postRepository.deletePost(id)
    }

    private fun convertDTOToPost(dto: PostDTO, resolvedGameId: Uuid): Post {
        return if (dto.id != null) {
            Post(
                id = dto.id,
                userId = dto.userId,
                gameId = resolvedGameId,
                title = dto.title,
                content = dto.content,
                score = dto.score
            )
        } else {
            Post(
                userId = dto.userId,
                gameId = resolvedGameId,
                title = dto.title,
                content = dto.content,
                score = dto.score
            )
        }
    }

    private fun convertPostToDTO(post: Post): PostDTO {
        return PostDTO(
            id = post.id,
            userId = post.userId,
            gameId = post.gameId,
            title = post.title,
            content = post.content,
            score = post.score
        )
    }
}
