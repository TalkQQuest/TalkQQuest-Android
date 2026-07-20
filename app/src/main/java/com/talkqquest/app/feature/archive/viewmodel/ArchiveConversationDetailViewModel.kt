package com.talkqquest.app.feature.archive.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talkqquest.app.feature.archive.data.ArchiveRepository
import com.talkqquest.app.feature.archive.data.model.ReviewChatMessage // 💡 [수정됨] 새롭게 이동한 경로로 올바르게 임포트!
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class AiFeedbackItem(
    val title: String,
    val score: Int
)

data class ArchiveConversationDetailUiState(
    val isLoading: Boolean = true,
    val title: String = "",
    val date: String = "",
    val duration: String = "",
    val summaryKeywords: List<String> = emptyList(),
    val summaryText: String = "",
    val mainContentText: String = "",
    val feedbacks: List<AiFeedbackItem> = emptyList(),
    // 💡 뷰모델에서 이 타입(ReviewChatMessage)을 정확히 인식해야 UI에서 에러가 안 납니다!
    val messages: List<ReviewChatMessage> = emptyList(),
    val isReviewMode: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ArchiveConversationDetailViewModel @Inject constructor(
    private val repository: ArchiveRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArchiveConversationDetailUiState())
    val uiState: StateFlow<ArchiveConversationDetailUiState> = _uiState.asStateFlow()

    init {
        val conversationId = savedStateHandle.get<String>("conversationId") ?: ""
        loadDetail(conversationId)
    }

    private fun loadDetail(id: String) {
        val detail = repository.getConversationDetail(id)
        if (detail != null) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    title = detail.title,
                    date = detail.date,
                    duration = detail.duration,
                    summaryKeywords = detail.summaryKeywords,
                    summaryText = detail.summaryText,
                    mainContentText = detail.mainContentText,
                    feedbacks = detail.feedbacks.map { f -> AiFeedbackItem(f.first, f.second) },
                    messages = detail.messages
                )
            }
        } else {
            _uiState.update {
                it.copy(isLoading = false, errorMessage = "데이터를 불러올 수 없습니다.")
            }
        }
    }

    fun toggleReviewMode() {
        _uiState.update { it.copy(isReviewMode = !it.isReviewMode) }
    }
}