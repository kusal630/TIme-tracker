package io.github.dailytrack.data.repository

import io.github.dailytrack.data.db.dao.UserProfileDao
import io.github.dailytrack.data.db.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

class UserProfileRepository(private val userProfileDao: UserProfileDao) {
    fun getUserProfile(): Flow<UserProfileEntity?> = userProfileDao.getUserProfile()
    suspend fun getUserProfileSync(): UserProfileEntity? = userProfileDao.getUserProfileSync()
    suspend fun upsert(profile: UserProfileEntity) = userProfileDao.upsert(profile)
}
