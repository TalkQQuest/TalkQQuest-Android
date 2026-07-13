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
                    it.copy(isLoading = false, errorMessage = result.message ?: "로그인에 실패했어요.")
                }
                is ApiResult.Exception -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = "네트워크 연결을 확인해주세요.")
                }
            }
        }
    }
}