package gg.pearl.repository

import gg.pearl.model.Post
import kotlin.uuid.Uuid

interface PostRepository {
    fun getPosts(): MutableList<Post>
    fun createPost(post: Post): Post?
    fun deletePost(post: Post): Boolean
    fun updatePost(post: Post): Post?
    fun getPostById(id: Uuid): Post?
    fun getAllByUserId(id: Uuid): List<Post>
}