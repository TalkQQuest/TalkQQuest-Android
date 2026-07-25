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

    private fun loadPhraseData(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = repository.getSavedSentenceDetail(id)) {
                is ApiResult.Success -> {
                    val data = result.data

                    // API 응답에 있는 conversationId와 missionTitle을 활용해 연관 대화 객체(RecentActivity) 생성
                    val relatedConv = if (data.conversationId != null && data.missionTitle != null) {
                        RecentActivity(
                            id = data.conversationId,
                            type = ActivityType.CONVERSATION,
                            title = data.missionTitle,
                            status = "대화 완료",
                            date = data.createdAt // fallback 날짜
                        )
                    } else null

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            phraseId = data.id,
                            phraseText = data.content,
                            isBookmarked = true, // 아카이브 상세 페이지에 존재한다는 것 자체가 저장됨을 의미
                            memoKeywords = emptyList(), // 💡 API 응답에 태그 배열(tags)이 없으므로 빈 값 처리
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

        // 즉각적인 UI 선반영 (Optimistic UI)
        _uiState.update { it.copy(isBookmarked = !isCurrentlySaved) }

        viewModelScope.launch {
            when (val result = repository.toggleSentenceBookmark(
                id = id,
                isCurrentlySaved = isCurrentlySaved,
                conversationId = state.relatedConversation?.id,
                content = state.phraseText,
                memo = state.memoText
            )) {
                is ApiResult.Success -> {
                    // 서버 반영 성공, 선반영한 상태 그대로 유지
                }
                is ApiResult.Error -> {
                    android.util.Log.e("ArchiveTest", "문장 저장 토글 에러: ${result.message}")
                    _uiState.update { it.copy(isBookmarked = isCurrentlySaved) } // 실패 시 원상복구
                }
                is ApiResult.Exception -> {
                    android.util.Log.e("ArchiveTest", "문장 저장 토글 통신 예외 (주소 틀림 등)")
                    _uiState.update { it.copy(isBookmarked = isCurrentlySaved) } // 실패 시 원상복구
                }
            }
        }
    }
}