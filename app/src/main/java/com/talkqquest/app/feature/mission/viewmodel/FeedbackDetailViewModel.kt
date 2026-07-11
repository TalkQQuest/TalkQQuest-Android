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

// AI 피드백 상세 화면 상태 (CONVENTIONS 6번: [화면이름]UiState)
data class FeedbackDetailUiState(
    val isLoading: Boolean = true,
    val result: FeedbackResult? = null,
    val itemIndex: Int = 0, // 배너에 보여줄 항목 (요약 화면에서 탭한 행 — scoreItems() 순서)
    val isPhraseSaved: Boolean = false, // 베스트 문장 저장(북마크) 여부
    val errorMessage: String? = null,
)

@HiltViewModel
class FeedbackDetailViewModel @Inject constructor(
    private val missionRepository: MissionRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val feedbackId: Long = checkNotNull(savedStateHandle["feedbackId"])

    // 요약에서 탭한 항목 번호. 직접 진입(딥링크 등) 시 0 = 첫 항목(친절한 태도).
    private val itemIndex: Int = savedStateHandle["item"] ?: 0

    private val _uiState = MutableStateFlow(FeedbackDetailUiState(itemIndex = itemIndex))
    val uiState: StateFlow<FeedbackDetailUiState> = _uiState.asStateFlow()

    init {
        loadFeedback()
    }

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

    // 베스트 문장 저장 토글. TODO(서버 연동): 문장 저장 API(아카이브 '문장')로 교체 — 지금은 화면 상태만.
    fun togglePhraseSave() {
        _uiState.update { it.copy(isPhraseSaved = !it.isPhraseSaved) }
    }
}
