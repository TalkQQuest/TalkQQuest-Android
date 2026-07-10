package com.talkqquest.app.feature.mission.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talkqquest.app.core.network.ApiResult
import com.talkqquest.app.feature.mission.data.MissionRepository
import com.talkqquest.app.feature.mission.data.model.FeedbackResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// AI 피드백 요약 화면 상태 (CONVENTIONS 6번: [화면이름]UiState)
data class FeedbackUiState(
    val isLoading: Boolean = true,
    val result: FeedbackResult? = null,
    val errorMessage: String? = null,
)

@HiltViewModel
class FeedbackViewModel @Inject constructor(
    private val missionRepository: MissionRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val feedbackId: Long = checkNotNull(savedStateHandle["feedbackId"])

    private val _uiState = MutableStateFlow(FeedbackUiState())
    val uiState: StateFlow<FeedbackUiState> = _uiState.asStateFlow()

    init {
        loadFeedback()
    }

    // 예외 E1(피드백 실패): 실패 시 기본 안내 문구 + 재시도 버튼 — 에러 분기가 그 역할.
    fun loadFeedback() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = missionRepository.getFeedback(feedbackId)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(isLoading = false, result = result.data)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message ?: "피드백을 불러오지 못했어요.")
                }
                is ApiResult.Exception -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = "네트워크 연결을 확인해주세요.")
                }
            }
        }
    }
}
