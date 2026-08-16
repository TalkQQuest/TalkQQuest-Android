package com.talkqquest.app.feature.profile.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talkqquest.app.feature.profile.data.ProfilePhotoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfilePhotoPickerUiState(
    val isLoading: Boolean = false,
    val photos: List<Uri> = emptyList(),
    val failedToLoad: Boolean = false,
)

@HiltViewModel
class ProfilePhotoPickerViewModel @Inject constructor(
    private val photoRepository: ProfilePhotoRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfilePhotoPickerUiState())
    val uiState: StateFlow<ProfilePhotoPickerUiState> = _uiState.asStateFlow()

    fun loadPhotos() {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.value = ProfilePhotoPickerUiState(isLoading = true)
            _uiState.value = runCatching { photoRepository.recentPhotos() }
                .fold(
                    onSuccess = { ProfilePhotoPickerUiState(photos = it) },
                    onFailure = { ProfilePhotoPickerUiState(failedToLoad = true) },
                )
        }
    }
}
