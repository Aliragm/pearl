package gg.pearl.repository

import gg.pearl.model.Follows
import kotlin.uuid.Uuid

interface FollowsRepository {
    suspend fun createFollow(follow: Follows): Follows?
    suspend fun deleteFollow(follow: Follows): Boolean
    suspend fun getAllFollowers(userId: Uuid): List<Follows>
    suspend fun getAllFollowings(userId: Uuid): List<Follows>
}