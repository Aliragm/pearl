package gg.pearl.service

import gg.pearl.dto.FollowsDTO
import gg.pearl.dto.PageResponse
import gg.pearl.model.Follows
import gg.pearl.repository.FollowsRepository
import kotlin.uuid.Uuid

class FollowsService(
    private val followsRepository: FollowsRepository
) {

    suspend fun createFollow(dto: FollowsDTO): FollowsDTO? {
        val model = Follows(userFollowingId = dto.userFollowingId, userFollowerId = dto.userFollowerId)
        val result = followsRepository.createFollow(model)
        return result?.let { FollowsDTO(userFollowingId = it.userFollowingId, userFollowerId = it.userFollowerId) }
    }

    suspend fun isFollowing(userFollowingId: Uuid, userFollowerId: Uuid): Boolean {
        return followsRepository.isFollowing(userFollowingId, userFollowerId)
    }

    suspend fun deleteFollow(dto: FollowsDTO): Boolean {
        val model = Follows(userFollowingId = dto.userFollowingId, userFollowerId = dto.userFollowerId)
        return followsRepository.deleteFollow(model)
    }

    suspend fun getAllFollowers(userId: Uuid, page: Int = 1, size: Int = 10): PageResponse<FollowsDTO> {
        val (followers, total) = followsRepository.getAllFollowers(userId, page, size)
        return PageResponse(
            items = followers.map { FollowsDTO(userFollowingId = it.userFollowingId, userFollowerId = it.userFollowerId) },
            page = page.coerceAtLeast(1),
            size = size.coerceIn(1, 50),
            total = total
        )
    }

    suspend fun getAllFollowings(userId: Uuid, page: Int = 1, size: Int = 10): PageResponse<FollowsDTO> {
        val (followings, total) = followsRepository.getAllFollowings(userId, page, size)
        return PageResponse(
            items = followings.map { FollowsDTO(userFollowingId = it.userFollowingId, userFollowerId = it.userFollowerId) },
            page = page.coerceAtLeast(1),
            size = size.coerceIn(1, 50),
            total = total
        )
    }
}
