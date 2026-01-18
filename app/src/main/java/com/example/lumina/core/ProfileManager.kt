package com.example.lumina.core

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val profileRepository: ProfileRepository
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
    private val currentProfileId = MutableStateFlow(prefs.getString("current_profile_id", null))

    fun getCurrentProfileId(): Flow<String?> {
        return currentProfileId
    }

    suspend fun setCurrentProfile(profileId: String) {
        prefs.edit().putString("current_profile_id", profileId).apply()
        currentProfileId.value = profileId
    }

    suspend fun createDefaultProfileIfNeeded() {
        profileRepository.getProfiles().first().let { profiles ->
            if (profiles.isEmpty()) {
                profileRepository.createProfile("Default")
                profileRepository.getProfiles().first().let { newProfiles ->
                    setCurrentProfile(newProfiles.first().id)
                }
            }
        }
    }
}
