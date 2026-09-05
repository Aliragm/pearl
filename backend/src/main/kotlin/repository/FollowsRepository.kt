package gg.pearl.repository

import gg.pearl.model.Follows
import gg.pearl.model.User

class FollowsRepository {

    private val follows: MutableList<Follows> = mutableListOf()

    fun createFollow(follow: Follows): Follows? {
        return if(follows.add(follow)){
            follow
        } else {
            null
        }
    }

    fun deleteFollow(follow: Follows): Boolean {
        return follows.remove(follow)
    }

    fun getAllFollowers(user: User): List<Follows> {
        return follows.filter { it.userFollowingId == user.id }
    }

    fun getAllFollowings(user: User): List<Follows> {
        return follows.filter { it.userFollowerId == user.id}
    }
}