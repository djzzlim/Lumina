package com.example.lumina.features.profiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lumina.core.ProfileManager
import com.example.lumina.core.ProfileRepository
import com.example.lumina.core.database.Profile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the [ProfilesScreen].
 *
 * Manages the list of user profiles and handles operations like creating,
 * deleting, and switching the active profile.
 *
 * @property profileRepository Repository for accessing and modifying profile data.
 * @property profileManager Manager for the currently active profile state.
 */
@HiltViewModel
class ProfilesViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val profileManager: ProfileManager
) : ViewModel() {

    /**
     * Flow of all available user profiles.
     */
    val profiles: Flow<List<Profile>> = profileRepository.getProfiles()

    /**
     * Flow of the ID of the currently active profile.
     */
    val currentProfileId: Flow<String?> = profileManager.getCurrentProfileId()

    /**
     * Creates a new profile with the given name and automatically switches to it.
     *
     * @param name The name of the new profile.
     */
    fun createProfile(name: String) {
        viewModelScope.launch {
            val newProfile = profileRepository.createProfile(name)
            profileManager.setCurrentProfile(newProfile.id)
        }
    }

    /**
     * Updates an existing profile.
     *
     * @param profile The [Profile] entity to update.
     */
    fun updateProfile(profile: Profile) {
        viewModelScope.launch {
            profileRepository.updateProfile(profile)
        }
    }

    /**
     * Deletes the specified profile.
     *
     * Does not allow deleting the last remaining profile.
     *
     * @param profile The [Profile] entity to delete.
     */
    fun deleteProfile(profile: Profile) {
        viewModelScope.launch {
            val currentProfiles = profiles.first()
            if (currentProfiles.size > 1) {
                profileRepository.deleteProfile(profile)
            }
        }
    }

    /**
     * Switches the active profile to the one with the specified ID.
     *
     * @param profileId The ID of the profile to switch to.
     */
    fun switchProfile(profileId: String) {
        viewModelScope.launch {
            profileManager.setCurrentProfile(profileId)
        }
    }
}
