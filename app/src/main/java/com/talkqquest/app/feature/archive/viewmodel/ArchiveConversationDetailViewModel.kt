package com.talkqquest.app.feature.archive.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talkqquest.app.core.network.ApiResult
import com.talkqquest.app.feature.archive.data.ArchiveRepository
import com.talkqquest.app.feature.archive.data.model.ReviewChatMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = repository.getConversationDetail(id)) {
                is ApiResult.Success -> {
                    val data = result.data

                    // 서버의 feedback DTO를 UI용 AiFeedbackItem 리스트로 변환
                    val mappedFeedbacks = mutableListOf<AiFeedbackItem>()
                    data.feedback?.let { fb ->
                        mappedFeedbacks.add(AiFeedbackItem("친절한 태도", fb.kindnessScore))
                        mappedFeedbacks.add(AiFeedbackItem("대화 주도", fb.initiativeScore))
                        mappedFeedbacks.add(AiFeedbackItem("공감 표현", fb.empathyScore))
                        mappedFeedbacks.add(AiFeedbackItem("질문 연결성", fb.questionLinkScore))
                    }

                    // 서버의 messages DTO를 UI용 ReviewChatMessage 리스트로 변환
                    val mappedMessages = data.messages.mapIndexed { index, msg ->
                        ReviewChatMessage(
                            id = index.toString(),
                            text = msg.content,
                            isFromUser = msg.sender.uppercase() == "USER", // "USER"일 경우 true 처리
                            time = msg.sentAt
                        )
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            title = data.missionTitle ?: "대화 상세",
                            date = "", // 새로운 API 스펙에 누락된 필드 (필요시 백엔드 요청)
                            duration = "", // 새로운 API 스펙에 누락된 필드 (필요시 백엔드 요청)
                            summaryKeywords = emptyList(), // 새로운 API 스펙에 누락된 필드
                            summaryText = data.summary ?: "",
                            mainContentText = data.summary ?: "", // summaryText와 함께 사용하도록 기본 할당
                            feedbacks = mappedFeedbacks,
                            messages = mappedMessages
                        )
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.message)
                    }
                }
                is ApiResult.Exception -> {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = "네트워크 오류가 발생했습니다.")
                    }
                }
            }
        }
    }

    fun toggleReviewMode() {
        _uiState.update { it.copy(isReviewMode = !it.isReviewMode) }
    }
}