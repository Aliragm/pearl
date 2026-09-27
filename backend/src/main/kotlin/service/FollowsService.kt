package gg.pearl.service

import gg.pearl.dto.FollowsDTO
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

    suspend fun deleteFollow(dto: FollowsDTO): Boolean {
        val model = Follows(userFollowingId = dto.userFollowingId, userFollowerId = dto.userFollowerId)
        return followsRepository.deleteFollow(model)
    }

    suspend fun getAllFollowers(userId: Uuid): List<FollowsDTO> {
        return followsRepository.getAllFollowers(userId).map {
            FollowsDTO(userFollowingId = it.userFollowingId, userFollowerId = it.userFollowerId)
        }
    }

    suspend fun getAllFollowings(userId: Uuid): List<FollowsDTO> {
        return followsRepository.getAllFollowings(userId).map {
            FollowsDTO(userFollowingId = it.userFollowingId, userFollowerId = it.userFollowerId)
        }
    }
}
