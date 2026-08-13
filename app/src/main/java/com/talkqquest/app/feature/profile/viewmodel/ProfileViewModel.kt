package com.talkqquest.app.feature.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talkqquest.app.core.network.ApiResult
import com.talkqquest.app.feature.home.data.model.ArchiveSummary
import com.talkqquest.app.feature.home.data.model.LegalDocument
import com.talkqquest.app.feature.home.data.model.MyBadge
import com.talkqquest.app.feature.home.data.model.MyPageDashboard
import com.talkqquest.app.feature.home.data.model.UserMe
import com.talkqquest.app.feature.home.data.model.UserSettings
import com.talkqquest.app.feature.home.data.model.UserSettingsUpdateRequest
import com.talkqquest.app.feature.home.data.model.UserUpdateRequest
import com.talkqquest.app.feature.mission.data.model.XpSummary
import com.talkqquest.app.feature.profile.data.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = false,
    val profile: UserMe? = null,
    val dashboard: MyPageDashboard? = null,
    val xpSummary: XpSummary? = null,
    val archiveSummary: ArchiveSummary? = null,
    val badges: List<MyBadge> = emptyList(),
    val serviceTerms: LegalDocument? = null,
    val privacyPolicy: LegalDocument? = null,
    val settings: UserSettings? = null,
    val errorMessage: String? = null,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = profileRepository.getMyPageDashboard()) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(isLoading = false, dashboard = result.data)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message ?: "\uB9C8\uC774\uD398\uC774\uC9C0 \uC694\uC57D\uC744 \uBD88\uB7EC\uC624\uC9C0 \uBABB\uD588\uC5B4\uC694.")
                }
                is ApiResult.Exception -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = networkErrorMessage)
                }
            }
        }
    }
    fun loadXpSummary() {
        viewModelScope.launch {
            when (val result = profileRepository.getXpSummary()) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(xpSummary = result.data)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(errorMessage = result.message ?: "XP 정보를 불러오지 못했어요.")
                }
                is ApiResult.Exception -> _uiState.update {
                    it.copy(errorMessage = networkErrorMessage)
                }
            }
        }
    }
    fun loadArchiveSummary() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = profileRepository.getArchiveSummary()) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(isLoading = false, archiveSummary = result.data)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message ?: "최근 활동을 불러오지 못했어요.")
                }
                is ApiResult.Exception -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = networkErrorMessage)
                }
            }
        }
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = profileRepository.getMyProfile()) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(isLoading = false, profile = result.data)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message ?: "\uD504\uB85C\uD544\uC744 \uBD88\uB7EC\uC624\uC9C0 \uBABB\uD588\uC5B4\uC694.")
                }
                is ApiResult.Exception -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = networkErrorMessage)
                }
            }
        }
    }

    fun loadBadges() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = profileRepository.getMyBadges()) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(isLoading = false, badges = result.data.badges)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message ?: "\uBC30\uC9C0 \uBAA9\uB85D\uC744 \uBD88\uB7EC\uC624\uC9C0 \uBABB\uD588\uC5B4\uC694.")
                }
                is ApiResult.Exception -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = networkErrorMessage)
                }
            }
        }
    }

    fun loadServiceTerms() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = profileRepository.getServiceTerms()) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(isLoading = false, serviceTerms = result.data)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message ?: "\uC774\uC6A9\uC57D\uAD00\uC744 \uBD88\uB7EC\uC624\uC9C0 \uBABB\uD588\uC5B4\uC694.")
                }
                is ApiResult.Exception -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = networkErrorMessage)
                }
            }
        }
    }

    fun loadPrivacyPolicy() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = profileRepository.getPrivacyPolicy()) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(isLoading = false, privacyPolicy = result.data)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message ?: "\uAC1C\uC778\uC815\uBCF4 \uCC98\uB9AC\uBC29\uCE68\uC744 \uBD88\uB7EC\uC624\uC9C0 \uBABB\uD588\uC5B4\uC694.")
                }
                is ApiResult.Exception -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = networkErrorMessage)
                }
            }
        }
    }

    fun loadSettings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = profileRepository.getUserSettings()) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(isLoading = false, settings = result.data)
                }
                is ApiResult.Error -> _uiState.update {
                    if (result.code == 404) {
                        it.copy(isLoading = false, settings = UserSettings())
                    } else {
                        it.copy(isLoading = false, errorMessage = result.message ?: "\uC124\uC815\uC744 \uBD88\uB7EC\uC624\uC9C0 \uBABB\uD588\uC5B4\uC694.")
                    }
                }
                is ApiResult.Exception -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = networkErrorMessage)
                }
            }
        }
    }
    fun uploadProfileImage(image: MultipartBody.Part, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = profileRepository.uploadProfileImage(image)) {
                is ApiResult.Success -> {
                    val avatarUrl = result.data.avatarUrl
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            profile = state.profile?.copy(avatarUrl = avatarUrl),
                            dashboard = state.dashboard?.copy(avatarUrl = avatarUrl),
                        )
                    }
                    onSuccess()
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message ?: "\uD504\uB85C\uD544 \uC774\uBBF8\uC9C0\uB97C \uC5C5\uB85C\uB4DC\uD558\uC9C0 \uBABB\uD588\uC5B4\uC694.")
                }
                is ApiResult.Exception -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = networkErrorMessage)
                }
            }
        }
    }
    fun updateMissionReminder(enabled: Boolean) {
        updateSettings(
            next = (_uiState.value.settings ?: UserSettings()).copy(missionReminder = enabled),
            request = UserSettingsUpdateRequest(missionReminder = enabled),
        )
    }

    fun updatePushNotifications(enabled: Boolean) {
        updateSettings(
            next = (_uiState.value.settings ?: UserSettings()).copy(
                communityApproved = enabled,
                reportReady = enabled,
                marketing = enabled,
            ),
            request = UserSettingsUpdateRequest(
                communityApproved = enabled,
                reportReady = enabled,
                marketing = enabled,
            ),
        )
    }

    fun updateMissionReminderTime(time: String) {
        val reminderTime = time.takeIf { it.matches(Regex("^\\d{2}:\\d{2}$")) } ?: return
        updateSettings(
            next = (_uiState.value.settings ?: UserSettings()).copy(missionReminderTime = reminderTime),
            request = UserSettingsUpdateRequest(missionReminderTime = reminderTime),
        )
    }
    private fun updateSettings(next: UserSettings, request: UserSettingsUpdateRequest) {
        val previous = _uiState.value.settings
        viewModelScope.launch {
            _uiState.update { it.copy(settings = next, errorMessage = null) }
            when (val result = profileRepository.updateUserSettings(request)) {
                is ApiResult.Success -> _uiState.update { it.copy(isLoading = false) }
                is ApiResult.Error -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        settings = previous,
                        errorMessage = if (result.code == 404) "알림 설정을 저장하지 못했어요. 잠시 후 다시 시도해주세요." else result.message ?: "설정을 수정하지 못했어요.",
                    )
                }
                is ApiResult.Exception -> _uiState.update {
                    it.copy(isLoading = false, settings = previous, errorMessage = networkErrorMessage)
                }
            }
        }
    }
    fun updateNickname(nickname: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = profileRepository.updateProfile(UserUpdateRequest(nickname = nickname))) {
                is ApiResult.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            profile = state.profile?.copy(nickname = nickname),
                            dashboard = state.dashboard?.copy(nickname = nickname),
                        )
                    }
                    onSuccess()
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message ?: "\uD504\uB85C\uD544\uC744 \uC218\uC815\uD558\uC9C0 \uBABB\uD588\uC5B4\uC694.")
                }
                is ApiResult.Exception -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = networkErrorMessage)
                }
            }
        }
    }

    fun verifyCurrentPassword(
        currentPassword: String,
        onSuccess: () -> Unit,
        onInvalidPassword: () -> Unit,
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (profileRepository.verifyCurrentPassword(currentPassword)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess()
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoading = false) }
                    onInvalidPassword()
                }
                is ApiResult.Exception -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = networkErrorMessage)
                }
            }
        }
    }

    fun changePassword(newPassword: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = profileRepository.changePassword(newPassword)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess()
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message ?: "\uBE44\uBC00\uBC88\uD638\uB97C \uBCC0\uACBD\uD558\uC9C0 \uBABB\uD588\uC5B4\uC694.")
                }
                is ApiResult.Exception -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = networkErrorMessage)
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private companion object {
        const val networkErrorMessage = "\uB124\uD2B8\uC6CC\uD06C \uC5F0\uACB0\uC744 \uD655\uC778\uD574\uC8FC\uC138\uC694."
    }
}
