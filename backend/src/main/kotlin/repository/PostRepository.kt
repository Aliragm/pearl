package gg.pearl.repository

import gg.pearl.dto.UpdatePostDTO
import gg.pearl.model.Post
import kotlin.uuid.Uuid

interface PostRepository {
    suspend fun getPosts(): List<Post>
    suspend fun getPostById(id: Uuid): Post?
    suspend fun getAllByUserId(userId: Uuid): List<Post>
    suspend fun createPost(post: Post): Post?
    suspend fun updatePost(id: Uuid, dto: UpdatePostDTO): Post?
    suspend fun deletePost(id: Uuid): Boolean
}