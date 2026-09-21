package gg.pearl.repository

import gg.pearl.model.Follows
import gg.pearl.model.User

class FollowsRepositoryImpl : FollowsRepository {

    private val follows: MutableList<Follows> = mutableListOf()

    override fun createFollow(follow: Follows): Follows? {
        return if(follows.add(follow)){
            follow
        } else {
            null
        }
    }

    override fun deleteFollow(follow: Follows): Boolean {
        return follows.remove(follow)
    }

    override fun getAllFollowers(user: User): List<Follows> {
        return follows.filter { it.userFollowingId == user.id }
    }

    override fun getAllFollowings(user: User): List<Follows> {
        return follows.filter { it.userFollowerId == user.id}
    }
}