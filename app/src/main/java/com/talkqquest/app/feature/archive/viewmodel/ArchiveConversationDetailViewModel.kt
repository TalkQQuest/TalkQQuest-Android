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
    val time: String = "", // 대화 프로필 카드에 들어갈 시간(HH:mm)
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

                    val mappedFeedbacks = mutableListOf<AiFeedbackItem>()
                    data.feedback?.let { fb ->
                        mappedFeedbacks.add(AiFeedbackItem("친절한 태도", fb.kindnessScore))
                        mappedFeedbacks.add(AiFeedbackItem("대화 주도", fb.initiativeScore))
                        mappedFeedbacks.add(AiFeedbackItem("공감 능력", fb.empathyScore))
                        mappedFeedbacks.add(AiFeedbackItem("질문 연결성", fb.questionLinkScore))
                    }

                    val mappedMessages = data.messages.mapIndexed { index, msg ->
                        // API 명세 변경 대응: actualRole과 actualTime 사용
                        val roleString = msg.actualRole
                        val timeString = msg.actualTime

                        val parsedTime = try {
                            val zdt = ZonedDateTime.parse(timeString)
                            zdt.format(DateTimeFormatter.ofPattern("HH:mm"))
                        } catch (e: Exception) {
                            if (timeString.contains("T")) timeString.substringAfter("T").substringBeforeLast(":") else timeString
                        }

                        ReviewChatMessage(
                            id = msg.id ?: index.toString(),
                            text = msg.content,
                            isFromUser = roleString.equals("user", ignoreCase = true),
                            time = parsedTime
                        )
                    }

                    // 첫 번째 메시지의 시간을 기준으로 전체 대화의 날짜와 생성 시간을 추출
                    val firstMessageTimeStr = data.messages.firstOrNull()?.actualTime ?: ""

                    val parsedDate = try {
                        if (firstMessageTimeStr.isNotBlank()) {
                            ZonedDateTime.parse(firstMessageTimeStr).format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
                        } else ""
                    } catch (e: Exception) { "" }

                    val parsedTime = try {
                        if (firstMessageTimeStr.isNotBlank()) {
                            ZonedDateTime.parse(firstMessageTimeStr).format(DateTimeFormatter.ofPattern("HH:mm"))
                        } else ""
                    } catch (e: Exception) { "" }

                    val parsedDuration = data.durationMinutes?.let { "${it}분" } ?: ""

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            title = data.missionTitle ?: "대화 상세",
                            date = parsedDate,
                            time = parsedTime,
                            duration = parsedDuration,
                            summaryKeywords = data.summaryChips,
                            summaryText = data.summary ?: "",
                            mainContentText = data.summary ?: "",
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