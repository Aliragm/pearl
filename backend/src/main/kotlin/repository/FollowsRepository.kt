package gg.pearl.repository

import gg.pearl.model.Follows
import kotlin.uuid.Uuid

interface FollowsRepository {
    suspend fun createFollow(follow: Follows): Follows?
    suspend fun deleteFollow(follow: Follows): Boolean
    suspend fun isFollowing(userFollowingId: Uuid, userFollowerId: Uuid): Boolean
    suspend fun getAllFollowers(userId: Uuid, page: Int = 1, size: Int = 10): Pair<List<Follows>, Long>
    suspend fun getAllFollowings(userId: Uuid, page: Int = 1, size: Int = 10): Pair<List<Follows>, Long>
}