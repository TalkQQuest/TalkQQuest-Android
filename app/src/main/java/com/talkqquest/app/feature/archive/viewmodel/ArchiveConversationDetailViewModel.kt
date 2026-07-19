package com.talkqquest.app.feature.archive.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.talkqquest.app.feature.archive.data.ArchiveRepository
import com.talkqquest.app.feature.archive.data.ReviewChatMessage // 💡 변경된 경로 Import!
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class AiFeedbackItem(val title: String, val score: Int)

data class ArchiveConversationDetailUiState(
    val title: String = "",
    val date: String = "",
    val duration: String = "",
    val summaryKeywords: List<String> = emptyList(),
    val summaryText: String = "",
    val mainContentText: String = "",
    val feedbacks: List<AiFeedbackItem> = emptyList(),

    val messages: List<ReviewChatMessage> = emptyList(),
    val isReviewMode: Boolean = false,

    val isLoading: Boolean = true,
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
        val conversationId = savedStateHandle.get<String>("conversationId") ?: "1"
        loadConversationDetail(conversationId)
    }

    fun toggleReviewMode() {
        _uiState.update { it.copy(isReviewMode = !it.isReviewMode) }
    }

    private fun loadConversationDetail(id: String) {
        val detailData = repository.getConversationDetail(id)

        if (detailData != null) {
            _uiState.update { state ->
                state.copy(
                    title = detailData.title,
                    date = detailData.date,
                    duration = detailData.duration,
                    summaryKeywords = detailData.summaryKeywords,
                    summaryText = detailData.summaryText,
                    mainContentText = detailData.mainContentText,
                    feedbacks = detailData.feedbacks.map { AiFeedbackItem(it.first, it.second) },
                    // 💡 Repository 안에 있는 messages 리스트를 그대로 가져옵니다!
                    messages = detailData.messages,
                    isLoading = false
                )
            }
        } else {
            _uiState.update { state ->
                state.copy(isLoading = false, errorMessage = "대화 기록을 찾을 수 없습니다.")
            }
        }
    }
}