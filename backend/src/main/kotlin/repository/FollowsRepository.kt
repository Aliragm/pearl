package gg.pearl.repository

import gg.pearl.model.Follows
import gg.pearl.model.User

interface FollowsRepository {
    fun createFollow(follow: Follows): Follows?
    fun deleteFollow(follow: Follows): Boolean
    fun getAllFollowers(user: User): List<Follows>
    fun getAllFollowings(user: User): List<Follows>
}