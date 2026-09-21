package gg.pearl.repository

import gg.pearl.model.Post
import kotlin.uuid.Uuid

interface PostRepository {
    suspend fun getPosts(): MutableList<Post>
    suspend fun createPost(post: Post): Post?
    suspend fun deletePost(post: Post): Boolean
    suspend fun updatePost(post: Post): Post?
    suspend fun getPostById(id: Uuid): Post?
    suspend fun getAllByUserId(id: Uuid): List<Post>
}