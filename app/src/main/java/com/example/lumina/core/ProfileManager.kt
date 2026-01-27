package com.example.lumina.core

import android.content.Context
import android.content.SharedPreferences
import com.example.lumina.core.database.LuminaInfo
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
 * @property luminaRepository The repository for lumina data operations.
 */
@Singleton
class ProfileManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val profileRepository: ProfileRepository,
    private val luminaRepository: LuminaRepository
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
                
                // Add default apps for the first-ever profile
                addDefaultLuminaApps(newProfile.id)
            } else if (currentProfileId.value == null) {
                // If profiles exist but none is selected, select the first one
                setCurrentProfile(profiles.first().id)
            }
        }
    }

    /**
     * Adds a set of default apps to the specified profile.
     * Uses colors and icons that are strictly available in the Add/Edit Lumina screens.
     */
    private suspend fun addDefaultLuminaApps(profileId: String) {
        val defaultApps = listOf(
            LuminaInfo(profileId = profileId, name = "Google", url = "https://www.google.com", icon = "Search", color = 0xFF007AFF),
            LuminaInfo(profileId = profileId, name = "YouTube", url = "https://www.youtube.com", icon = "Laptop", color = 0xFFFF3B30),
            LuminaInfo(profileId = profileId, name = "Reddit", url = "https://www.reddit.com", icon = "Forum", color = 0xFFFF9500),
            LuminaInfo(profileId = profileId, name = "Instagram", url = "https://www.instagram.com", icon = "CameraAlt", color = 0xFFFF2D55),
            LuminaInfo(profileId = profileId, name = "DuckDuckGo", url = "https://duckduckgo.com", icon = "Shield", color = 0xFFFFCC00),
            LuminaInfo(profileId = profileId, name = "Wikipedia", url = "https://www.wikipedia.org", icon = "MenuBook", color = 0xFF8E8E93)
        )
        
        defaultApps.forEach { app ->
            luminaRepository.insertLumina(app)
        }
    }
}
