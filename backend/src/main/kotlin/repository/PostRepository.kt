package gg.pearl.repository

import gg.pearl.dto.UpdatePostDTO
import gg.pearl.model.Post
import kotlin.uuid.Uuid

interface PostRepository {
    suspend fun getPosts(
        title: String? = null,
        userId: Uuid? = null,
        gameId: Uuid? = null,
        minScore: Double? = null,
        page: Int = 1,
        size: Int = 10
    ): Pair<List<Post>, Long>

    suspend fun getPostById(id: Uuid): Post?
    suspend fun getAllByUserId(userId: Uuid, page: Int = 1, size: Int = 10): Pair<List<Post>, Long>
    suspend fun createPost(post: Post): Post?
    suspend fun updatePost(id: Uuid, dto: UpdatePostDTO): Post?
    suspend fun deletePost(id: Uuid): Boolean
}