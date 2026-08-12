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

    private val feedbackId: String = checkNotNull(savedStateHandle["feedbackId"])

    private val _uiState = MutableStateFlow(FeedbackUiState())
    val uiState: StateFlow<FeedbackUiState> = _uiState.asStateFlow()

    init {
        loadFeedback()
    }

    /**
     * 예외 E1(피드백 실패): 실패 시 기본 안내 문구 + 재시도 버튼 — 에러 분기가 그 역할.
     *
     * @param regenerate 재시도 버튼에서 부를 때 true. 서버에 피드백을 다시 만들어 달라고 한 뒤
     *   조회한다. 조회만 다시 하면 서버 생성이 failed로 끝난 피드백은 몇 번을 눌러도 그대로다.
     *   첫 진입(init)은 생성이 이미 걸려 있으므로 false로 둔다.
     */
    fun loadFeedback(regenerate: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            if (regenerate) missionRepository.retryFeedback(feedbackId)
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
