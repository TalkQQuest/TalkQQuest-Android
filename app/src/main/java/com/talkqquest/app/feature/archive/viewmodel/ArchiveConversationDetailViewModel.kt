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
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class AiFeedbackItem(
    val title: String,
    val score: Int
)

data class ArchiveConversationDetailUiState(
    val isLoading: Boolean = true,
    val title: String = "",
    val date: String = "",
    val time: String = "", // UI 카드의 소요 시간("mm:ss")
    val duration: String = "", // "MM분 ss초" 텍스트 데이터
    val description: String = "", // 💡 상단 대화 카드용 짧은 요약
    val summaryKeywords: List<String> = emptyList(),
    val summaryText: String = "", // 💡 중간 '대화 요약' 줄글
    val mainContentText: String = "", // (기존 코드와의 호환성을 위해 유지)
    val keyPoints: List<String> = emptyList(), // 💡 하단 '주요 내용' 불릿 리스트
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

                    val mappedFeedbacks = mutableListOf<AiFeedbackItem>()
                    data.feedback?.let { fb ->
                        mappedFeedbacks.add(AiFeedbackItem("친절한 태도", fb.kindnessScore))
                        mappedFeedbacks.add(AiFeedbackItem("대화 주도", fb.initiativeScore))
                        mappedFeedbacks.add(AiFeedbackItem("공감 능력", fb.empathyScore))
                        mappedFeedbacks.add(AiFeedbackItem("질문 연결성", fb.questionLinkScore))
                    }

                    val mappedMessages = data.messages.mapIndexed { index, msg ->
                        val roleString = msg.actualRole
                        val timeString = msg.actualTime

                        val parsedMsgTime = try {
                            val zdt = ZonedDateTime.parse(timeString)
                            zdt.format(DateTimeFormatter.ofPattern("HH:mm"))
                        } catch (e: Exception) {
                            if (timeString.contains("T")) timeString.substringAfter("T").substringBeforeLast(":") else timeString
                        }

                        ReviewChatMessage(
                            id = msg.id ?: index.toString(),
                            text = msg.content,
                            isFromUser = roleString.equals("user", ignoreCase = true),
                            time = parsedMsgTime
                        )
                    }

                    val firstMessageTimeStr = data.messages.firstOrNull()?.actualTime ?: ""

                    val parsedDate = try {
                        if (firstMessageTimeStr.isNotBlank()) {
                            ZonedDateTime.parse(firstMessageTimeStr).format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
                        } else ""
                    } catch (e: Exception) { "" }

                    val parsedTime = data.duration ?: "00:00"

                    val parsedDuration = data.duration?.split(":")?.let { parts ->
                        if (parts.size == 2) {
                            "${parts[0].toIntOrNull() ?: 0}분 ${parts[1].toIntOrNull() ?: 0}초"
                        } else {
                            data.duration
                        }
                    } ?: ""

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            title = data.missionTitle ?: "대화 상세",
                            date = parsedDate,
                            time = parsedTime,
                            duration = parsedDuration,
                            description = data.description ?: data.summary ?: "", // 상단 카드용
                            summaryKeywords = data.summaryChips,
                            summaryText = data.summary ?: "", // 중간 대화 요약용
                            mainContentText = data.summary ?: "",
                            keyPoints = data.keyPoints, // 하단 주요 내용용(불릿)
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