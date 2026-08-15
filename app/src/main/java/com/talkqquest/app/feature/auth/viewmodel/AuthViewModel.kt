package com.talkqquest.app.feature.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talkqquest.app.core.network.ApiResult
import com.talkqquest.app.feature.auth.data.AuthRepository
import com.talkqquest.app.feature.auth.data.EmailSignupRequest
import com.talkqquest.app.feature.auth.data.OnboardingStepSaveRequest
import com.talkqquest.app.feature.auth.data.SocialLoginData
import com.talkqquest.app.feature.auth.data.SessionCheckResult
import com.talkqquest.app.feature.home.data.HomeRepository
import com.talkqquest.app.feature.home.data.model.LegalDocument
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val serviceTerms: LegalDocument? = null,
    val privacyPolicy: LegalDocument? = null,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val homeRepository: HomeRepository, // 스플래시에서 홈 요약을 병렬 프리페치하기 위해
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun checkStoredSession(
        onAuthenticated: () -> Unit,
        onUnauthenticated: () -> Unit,
        onNetworkError: () -> Unit,
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            // 세션 확인(getMe)과 동시에 홈 요약을 미리 받아 캐시에 담아둔다(대기 안 함).
            // 홈에 들어가면 두 번째 스피너 없이 바로 내용이 뜬다. 라우팅은 아래 결과가 그대로 결정.
            launch { runCatching { homeRepository.getHomeSummary() } }
            when (authRepository.checkStoredSession()) {
                SessionCheckResult.Authenticated -> {
                    _uiState.update { it.copy(isLoading = false) }
                    onAuthenticated()
                }
                SessionCheckResult.Unauthenticated -> {
                    _uiState.update { it.copy(isLoading = false) }
                    onUnauthenticated()
                }
                SessionCheckResult.NetworkError -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "네트워크 연결을 확인해주세요.") }
                    onNetworkError()
                }
            }
        }
    }
    fun loadSignupLegalDocuments() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val termsResult = authRepository.getServiceTerms()
            val privacyResult = authRepository.getPrivacyPolicy()
            val nextError = when {
                termsResult is ApiResult.Exception || privacyResult is ApiResult.Exception -> "\uB124\uD2B8\uC6CC\uD06C \uC5F0\uACB0\uC744 \uD655\uC778\uD574\uC8FC\uC138\uC694."
                termsResult is ApiResult.Error -> termsResult.message ?: "\uC774\uC6A9\uC57D\uAD00\uC744 \uBD88\uB7EC\uC624\uC9C0 \uBABB\uD588\uC5B4\uC694."
                privacyResult is ApiResult.Error -> privacyResult.message ?: "\uAC1C\uC778\uC815\uBCF4 \uCC98\uB9AC\uBC29\uCE68\uC744 \uBD88\uB7EC\uC624\uC9C0 \uBABB\uD588\uC5B4\uC694."
                else -> null
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = nextError,
                    serviceTerms = (termsResult as? ApiResult.Success)?.data ?: it.serviceTerms,
                    privacyPolicy = (privacyResult as? ApiResult.Success)?.data ?: it.privacyPolicy,
                )
            }
        }
    }
    fun loginWithKakao(
        providerAccessToken: String,
        onSuccess: (SocialLoginData) -> Unit,
    ) {
        login(onSuccess) { authRepository.loginWithKakao(providerAccessToken) }
    }

    fun loginWithNaver(
        providerAccessToken: String,
        onSuccess: (SocialLoginData) -> Unit,
    ) {
        login(onSuccess) { authRepository.loginWithNaver(providerAccessToken) }
    }

    fun loginWithEmail(
        email: String,
        password: String,
        onSuccess: () -> Unit,
    ) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "\uC774\uBA54\uC77C \uB610\uB294 \uBE44\uBC00\uBC88\uD638\uB97C \uD655\uC778\uD574\uC8FC\uC138\uC694.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = authRepository.loginWithEmail(email.trim(), password)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess()
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message ?: "\uC774\uBA54\uC77C \uB610\uB294 \uBE44\uBC00\uBC88\uD638\uB97C \uD655\uC778\uD574\uC8FC\uC138\uC694.")
                }
                is ApiResult.Exception -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = "\uB124\uD2B8\uC6CC\uD06C \uC5F0\uACB0\uC744 \uD655\uC778\uD574\uC8FC\uC138\uC694.")
                }
            }
        }
    }

    fun requestEmailCode(
        email: String,
        onSuccess: () -> Unit,
    ) {
        runUnitAuthCall(
            emptyInputMessage = "\uC774\uBA54\uC77C\uC744 \uC785\uB825\uD574\uC8FC\uC138\uC694.",
            isInputValid = email.isNotBlank(),
            call = { authRepository.requestEmailCode(email.trim()) },
            onSuccess = onSuccess,
        )
    }

    fun verifyEmailCode(
        email: String,
        code: String,
        onSuccess: () -> Unit,
    ) {
        runUnitAuthCall(
            emptyInputMessage = "\uC778\uC99D\uBC88\uD638 6\uC790\uB9AC\uB97C \uC785\uB825\uD574\uC8FC\uC138\uC694.",
            isInputValid = email.isNotBlank() && code.length == 6,
            call = { authRepository.verifyEmailCode(email.trim(), code) },
            onSuccess = onSuccess,
        )
    }

    fun signupWithEmail(
        email: String,
        password: String,
        nickname: String,
        onSuccess: () -> Unit,
    ) {
        if (email.isBlank() || password.isBlank() || nickname.isBlank()) {
            _uiState.update { it.copy(errorMessage = "\uC785\uB825\uAC12\uC744 \uD655\uC778\uD574\uC8FC\uC138\uC694.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = authRepository.signupWithEmail(
                EmailSignupRequest(
                    email = email.trim(),
                    password = password,
                    name = nickname.trim(),
                    termsAgreedAt = Instant.now().toString(),
                ),
            )) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess()
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message ?: "\uD68C\uC6D0\uAC00\uC785\uC5D0 \uC2E4\uD328\uD588\uC5B4\uC694.")
                }
                is ApiResult.Exception -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = "\uB124\uD2B8\uC6CC\uD06C \uC5F0\uACB0\uC744 \uD655\uC778\uD574\uC8FC\uC138\uC694.")
                }
            }
        }
    }


    fun updateSocialNickname(
        nickname: String,
        onSuccess: () -> Unit,
    ) {
        if (nickname.isBlank()) {
            _uiState.update { it.copy(errorMessage = "닉네임을 입력해주세요.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = authRepository.updateMyNickname(
                nickname = nickname.trim(),
                termsAgreedAt = Instant.now().toString(),
            )) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess()
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message ?: "닉네임 저장에 실패했어요.")
                }
                is ApiResult.Exception -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = "네트워크 연결을 확인해주세요.")
                }
            }
        }
    }

    fun saveOnboardingStep(
        request: OnboardingStepSaveRequest,
        onSuccess: () -> Unit,
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = authRepository.saveOnboardingStep(request)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess()
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message ?: "\uC628\uBCF4\uB529 \uC800\uC7A5\uC5D0 \uC2E4\uD328\uD588\uC5B4\uC694.")
                }
                is ApiResult.Exception -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = "\uB124\uD2B8\uC6CC\uD06C \uC5F0\uACB0\uC744 \uD655\uC778\uD574\uC8FC\uC138\uC694.")
                }
            }
        }
    }

    fun completeOnboarding(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = authRepository.completeOnboarding()) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess()
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoading = false) }
                    if (result.code == 409) {
                        onSuccess()
                    } else {
                        _uiState.update {
                            it.copy(errorMessage = result.message ?: "\uC628\uBCF4\uB529 \uC644\uB8CC \uCC98\uB9AC\uC5D0 \uC2E4\uD328\uD588\uC5B4\uC694.")
                        }
                    }
                }
                is ApiResult.Exception -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = "\uB124\uD2B8\uC6CC\uD06C \uC5F0\uACB0\uC744 \uD655\uC778\uD574\uC8FC\uC138\uC694.")
                }
            }
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = authRepository.logout()) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess()
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message ?: "로그아웃에 실패했어요.")
                }
                is ApiResult.Exception -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = "네트워크 연결을 확인해주세요.")
                }
            }
        }
    }

    fun withdraw(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = authRepository.withdraw()) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess()
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message ?: "회원 탈퇴에 실패했어요.")
                }
                is ApiResult.Exception -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = "네트워크 연결을 확인해주세요.")
                }
            }
        }
    }
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun login(
        onSuccess: (SocialLoginData) -> Unit,
        call: suspend () -> ApiResult<SocialLoginData>,
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = call()) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess(result.data)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message ?: "\uB85C\uADF8\uC778\uC5D0 \uC2E4\uD328\uD588\uC5B4\uC694.")
                }
                is ApiResult.Exception -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = "\uB124\uD2B8\uC6CC\uD06C \uC5F0\uACB0\uC744 \uD655\uC778\uD574\uC8FC\uC138\uC694.")
                }
            }
        }
    }

    private fun runUnitAuthCall(
        emptyInputMessage: String,
        isInputValid: Boolean,
        call: suspend () -> ApiResult<Unit>,
        onSuccess: () -> Unit,
    ) {
        if (!isInputValid) {
            _uiState.update { it.copy(errorMessage = emptyInputMessage) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = call()) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess()
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message ?: "\uC694\uCCAD\uC5D0 \uC2E4\uD328\uD588\uC5B4\uC694.")
                }
                is ApiResult.Exception -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = "\uB124\uD2B8\uC6CC\uD06C \uC5F0\uACB0\uC744 \uD655\uC778\uD574\uC8FC\uC138\uC694.")
                }
            }
        }
    }
}





