package gg.pearl.repository

import gg.pearl.model.Follows
import gg.pearl.model.User

interface FollowsRepository {
    suspend fun createFollow(follow: Follows): Follows?
    suspend fun deleteFollow(follow: Follows): Boolean
    suspend fun getAllFollowers(user: User): List<Follows>
    suspend fun getAllFollowings(user: User): List<Follows>
}