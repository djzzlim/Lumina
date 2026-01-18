package com.example.lumina.core

import com.example.lumina.core.database.Profile
import com.example.lumina.core.database.ProfileDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val profileDao: ProfileDao
) {
    fun getProfiles(): Flow<List<Profile>> {
        return profileDao.getProfiles()
    }

    suspend fun createProfile(name: String) {
        val profile = Profile(name = name)
        profileDao.insertOrUpdate(profile)
    }

    suspend fun deleteProfile(profile: Profile) {
        profileDao.delete(profile)
    }
}
