package gg.pearl.repository

import gg.pearl.model.Follows
import gg.pearl.model.User

class FollowsRepositoryImpl : FollowsRepository {

    private val follows: MutableList<Follows> = mutableListOf()

    override suspend fun createFollow(follow: Follows): Follows? {
        return if(follows.add(follow)){
            follow
        } else {
            null
        }
    }

    override suspend fun deleteFollow(follow: Follows): Boolean {
        return follows.remove(follow)
    }

    override suspend fun getAllFollowers(user: User): List<Follows> {
        return follows.filter { it.userFollowingId == user.id }
    }

    override suspend fun getAllFollowings(user: User): List<Follows> {
        return follows.filter { it.userFollowerId == user.id}
    }
}