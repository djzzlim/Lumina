package com.example.lumina.core

import com.example.lumina.core.database.Profile
import com.example.lumina.core.database.ProfileDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository class that manages [Profile] data operations.
 *
 * This class provides a clean API for the rest of the application to interact with
 * profile-related data, abstracting the underlying data source ([ProfileDao]).
 *
 * @property profileDao The Data Access Object for profile operations.
 */
@Singleton
class ProfileRepository @Inject constructor(
    private val profileDao: ProfileDao
) {
    /**
     * Returns a [Flow] of all [Profile]s in the database.
     *
     * @return A flow emitting a list of profiles.
     */
    fun getProfiles(): Flow<List<Profile>> {
        return profileDao.getProfiles()
    }

    /**
     * Creates a new [Profile] with the given name and inserts it into the database.
     *
     * @param name The name of the profile to create.
     */
    suspend fun createProfile(name: String) {
        val profile = Profile(name = name)
        profileDao.insertOrUpdate(profile)
    }

    /**
     * Deletes a specific [Profile] from the database.
     *
     * @param profile The profile to delete.
     */
    suspend fun deleteProfile(profile: Profile) {
        profileDao.delete(profile)
    }
}
