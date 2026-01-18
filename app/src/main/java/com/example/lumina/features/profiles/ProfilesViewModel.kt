package com.example.lumina.features.profiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lumina.core.ProfileManager
import com.example.lumina.core.ProfileRepository
import com.example.lumina.core.database.Profile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfilesViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val profileManager: ProfileManager
) : ViewModel() {

    val profiles: Flow<List<Profile>> = profileRepository.getProfiles()
    val currentProfileId: Flow<String?> = profileManager.getCurrentProfileId()

    fun createProfile(name: String) {
        viewModelScope.launch {
            profileRepository.createProfile(name)
        }
    }

    fun deleteProfile(profile: Profile) {
        viewModelScope.launch {
            profileRepository.deleteProfile(profile)
        }
    }

    fun switchProfile(profileId: String) {
        viewModelScope.launch {
            profileManager.setCurrentProfile(profileId)
        }
    }
}