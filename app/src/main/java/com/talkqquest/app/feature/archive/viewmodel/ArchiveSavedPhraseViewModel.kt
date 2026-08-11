package com.talkqquest.app.feature.archive.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talkqquest.app.core.network.ApiResult
import com.talkqquest.app.feature.archive.data.ArchiveRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class ArchiveSavedPhraseUiState(
    val phraseId: String = "",
    val phraseText: String = "",
    val isBookmarked: Boolean = true,
    val memoKeywords: List<String> = emptyList(),
    val memoText: String = "",
    val relatedConversation: RecentActivity? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ArchiveSavedPhraseViewModel @Inject constructor(
    private val repository: ArchiveRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArchiveSavedPhraseUiState())
    val uiState: StateFlow<ArchiveSavedPhraseUiState> = _uiState.asStateFlow()

    init {
        val phraseId: String? = savedStateHandle.get<String>("phraseId")
        if (phraseId != null) {
            loadPhraseData(phraseId)
        }
    }

    private fun formatIsoDate(isoString: String): String {
        return try {
            val zdt = ZonedDateTime.parse(isoString)
            zdt.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
        } catch (e: Exception) {
            isoString.substringBefore("T").replace("-", ".")
        }
    }

    // 💡 추가됨: 시간 파싱 함수
    private fun formatIsoTime(isoString: String): String {
        return try {
            val zdt = ZonedDateTime.parse(isoString)
            zdt.format(DateTimeFormatter.ofPattern("HH:mm"))
        } catch (e: Exception) {
            val timePart = isoString.substringAfter("T").substringBefore("+").substringBefore("Z")
            if (timePart.length >= 5) timePart.substring(0, 5) else ""
        }
    }

    private fun loadPhraseData(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = repository.getSavedSentenceDetail(id)) {
                is ApiResult.Success -> {
                    val data = result.data

                    val relatedConv = if (data.conversationId != null && data.missionTitle != null) {
                        RecentActivity(
                            id = data.conversationId,
                            type = ActivityType.CONVERSATION,
                            title = data.missionTitle,
                            status = "대화 완료",
                            date = formatIsoDate(data.createdAt),
                            duration = formatIsoTime(data.createdAt) // 💡 시간 파싱 적용
                        )
                    } else null

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            phraseId = data.id,
                            phraseText = data.content,
                            isBookmarked = true,
                            memoKeywords = data.summaryChips,
                            memoText = data.memo ?: "",
                            relatedConversation = relatedConv
                        )
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                is ApiResult.Exception -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "네트워크 오류 발생") }
                }
            }
        }
    }

    fun toggleBookmark() {
        val state = _uiState.value
        val id = state.phraseId
        if (id.isEmpty()) return

        val isCurrentlySaved = state.isBookmarked

        if (isCurrentlySaved) {
            _uiState.update { it.copy(isBookmarked = false) }

            viewModelScope.launch {
                when (val result = repository.toggleSentenceBookmark(
                    id = id,
                    isCurrentlySaved = true,
                    conversationId = state.relatedConversation?.id,
                    content = state.phraseText,
                    memo = state.memoText
                )) {
                    is ApiResult.Success -> {}
                    is ApiResult.Error -> _uiState.update { it.copy(isBookmarked = true) }
                    is ApiResult.Exception -> _uiState.update { it.copy(isBookmarked = true) }
                }
            }
        }
    }
}