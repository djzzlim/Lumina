package com.example.lumina.core

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages the currently active profile and handle profile-related persistence.
 *
 * This class uses [SharedPreferences] to persist the current profile ID and
 * [ProfileRepository] to interact with profile data.
 *
 * @property context The application context.
 * @property profileRepository The repository for profile data operations.
 */
@Singleton
class ProfileManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val profileRepository: ProfileRepository
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
    private val currentProfileId = MutableStateFlow(prefs.getString("current_profile_id", null))
    private val initializationMutex = Mutex()

    /**
     * Returns a [Flow] of the currently active profile ID.
     *
     * @return A flow emitting the current profile ID, or null if none is set.
     */
    fun getCurrentProfileId(): Flow<String?> {
        return currentProfileId
    }

    /**
     * Sets the currently active profile and persists its ID.
     *
     * @param profileId The ID of the profile to set as current.
     */
    suspend fun setCurrentProfile(profileId: String) {
        prefs.edit().putString("current_profile_id", profileId).apply()
        currentProfileId.value = profileId
    }

    /**
     * Creates a "Default" profile if no profiles exist in the database.
     *
     * This is typically called during application initialization to ensure a valid
     * profile state.
     */
    suspend fun createDefaultProfileIfNeeded() {
        initializationMutex.withLock {
            val profiles = profileRepository.getProfiles().first()
            if (profiles.isEmpty()) {
                val newProfile = profileRepository.createProfile("Default")
                setCurrentProfile(newProfile.id)
            } else if (currentProfileId.value == null) {
                // If profiles exist but none is selected, select the first one
                setCurrentProfile(profiles.first().id)
            }
        }
    }
}
