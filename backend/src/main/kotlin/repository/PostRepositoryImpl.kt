package gg.pearl.repository

import gg.pearl.model.Post
import kotlin.time.Clock
import kotlin.uuid.Uuid

class PostRepositoryImpl : PostRepository {

    private val posts: MutableList<Post> = mutableListOf()

    override suspend fun getPosts(): MutableList<Post> {
        return posts
    }

    override suspend fun createPost(post: Post): Post?{
        return if(posts.add(post)){
            post
        } else {
            null
        }
    }

    override suspend fun deletePost(post: Post): Boolean {
        return posts.remove(post)
    }

    override suspend fun updatePost(post: Post): Post? {
        val oldPost = this.getPostById(post.id)

        if(oldPost != null) {
            val newPost = Post(id = oldPost.id, userId = oldPost.userId, gameId = oldPost.gameId, title = post.title,
                content = post.content, score = post.score, createdAt = oldPost.createdAt, updatedAt = Clock.System.now())
            if(this.deletePost(oldPost)){
                this.posts.add(newPost)
                return newPost
            } else {
                return oldPost
            }
        } else {
            return null
        }
    }

    override suspend fun getPostById(id: Uuid): Post? {
        return posts.firstOrNull { it.id == id }
    }

    override suspend fun getAllByUserId(id: Uuid): List<Post> {
        return posts.filter { it.userId == id }
    }
}