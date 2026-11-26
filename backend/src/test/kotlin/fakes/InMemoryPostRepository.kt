package gg.pearl.fakes

import gg.pearl.dto.UpdatePostDTO
import gg.pearl.model.Post
import gg.pearl.repository.PostRepository
import java.util.concurrent.ConcurrentHashMap
import kotlin.uuid.Uuid

class InMemoryPostRepository : PostRepository {
    private val storage = ConcurrentHashMap<Uuid, Post>()

    override suspend fun getPosts(
        title: String?,
        userId: Uuid?,
        gameId: Uuid?,
        minScore: Double?,
        page: Int,
        size: Int
    ): Pair<List<Post>, Long> {
        val filtered = storage.values.filter { post ->
            (title == null || post.title.contains(title, ignoreCase = true)) &&
            (userId == null || post.userId == userId) &&
            (gameId == null || post.gameId == gameId) &&
            (minScore == null || post.score >= minScore)
        }
        val total = filtered.size.toLong()
        val safePage = page.coerceAtLeast(1)
        val safeSize = size.coerceIn(1, 50)
        val offset = (safePage - 1) * safeSize
        return Pair(filtered.drop(offset).take(safeSize), total)
    }

    override suspend fun getPostById(id: Uuid): Post? = storage[id]

    override suspend fun getAllByUserId(userId: Uuid, page: Int, size: Int): Pair<List<Post>, Long> {
        return getPosts(userId = userId, page = page, size = size)
    }

    override suspend fun createPost(post: Post): Post? {
        storage[post.id] = post
        return post
    }

    override suspend fun updatePost(id: Uuid, dto: UpdatePostDTO): Post? {
        val existing = storage[id] ?: return null
        val updated = existing.copy(
            title = dto.title ?: existing.title,
            content = dto.content ?: existing.content,
            score = dto.score ?: existing.score
        )
        storage[id] = updated
        return updated
    }

    override suspend fun deletePost(id: Uuid): Boolean {
        return storage.remove(id) != null
    }

    fun clear() {
        storage.clear()
    }
}
