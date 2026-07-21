package com.talkqquest.app.feature.archive.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.talkqquest.app.feature.archive.data.ArchiveRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class ArchiveSavedPhraseUiState(
    val phraseId: String = "",
    val phraseText: String = "",
    val isBookmarked: Boolean = true,
    val memoKeywords: List<String> = emptyList(),
    val memoText: String = "",
    val relatedConversation: RecentActivity? = null
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
        val detail = repository.getSavedSentenceDetail(id)
        if (detail != null) {
            val (sentence, conversation) = detail
            _uiState.value = ArchiveSavedPhraseUiState(
                phraseId = sentence.id,
                phraseText = sentence.title,
                isBookmarked = sentence.isSaved,
                memoKeywords = sentence.memoKeywords,
                memoText = sentence.memoText,
                relatedConversation = conversation
            )
        }
    }

    fun toggleBookmark() {
        val id = _uiState.value.phraseId
        if (id.isNotEmpty()) {
            // 💡 1. 공통 저장소 데이터 갱신
            repository.toggleSentenceBookmark(id)
            // 💡 2. 로컬 UI 상태 갱신
            _uiState.update { it.copy(isBookmarked = !it.isBookmarked) }
        }
    }
}