package com.talkqquest.app.feature.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talkqquest.app.core.network.ApiResult
import com.talkqquest.app.feature.auth.data.AuthRepository
import com.talkqquest.app.feature.auth.data.SocialLoginData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

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