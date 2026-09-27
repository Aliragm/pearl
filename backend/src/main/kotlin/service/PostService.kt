package gg.pearl.service

import gg.pearl.dto.PostDTO
import gg.pearl.dto.UpdatePostDTO
import gg.pearl.model.Post
import gg.pearl.repository.PostRepository
import kotlin.uuid.Uuid

class PostService(
    private val postRepository: PostRepository
) {

    suspend fun getPosts(): List<PostDTO> {
        return postRepository.getPosts().map(::convertPostToDTO)
    }

    suspend fun getPostById(id: Uuid): PostDTO? {
        return postRepository.getPostById(id)?.let(::convertPostToDTO)
    }

    suspend fun getPostsByUserId(userId: Uuid): List<PostDTO> {
        return postRepository.getAllByUserId(userId).map(::convertPostToDTO)
    }

    suspend fun createPost(post: PostDTO): PostDTO? {
        val newPost = convertDTOToPost(post)
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

    private fun convertDTOToPost(dto: PostDTO): Post {
        return if (dto.id != null) {
            Post(
                id = dto.id,
                userId = dto.userId,
                gameId = dto.gameId,
                title = dto.title,
                content = dto.content,
                score = dto.score
            )
        } else {
            Post(
                userId = dto.userId,
                gameId = dto.gameId,
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
