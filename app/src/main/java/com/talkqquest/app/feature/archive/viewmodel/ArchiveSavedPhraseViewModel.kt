package com.talkqquest.app.feature.archive.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.talkqquest.app.feature.archive.data.ArchiveRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    savedStateHandle: SavedStateHandle // 💡 네비게이션 인자(phraseId)를 자동으로 받아오는 마법의 클래스입니다!
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArchiveSavedPhraseUiState())
    val uiState: StateFlow<ArchiveSavedPhraseUiState> = _uiState.asStateFlow()

    init {
        // 💡 뷰모델이 생성될 때 네비게이션에서 넘어온 phraseId를 찾아 데이터를 로드합니다.
        // 이렇게 하면 UI 컴포저블에 파라미터를 추가할 필요가 없어 에러가 나지 않습니다.
        val phraseId: String? = savedStateHandle.get<String>("phraseId")
        if (phraseId != null) {
            loadPhraseData(phraseId)
        }
    }

    private fun loadPhraseData(id: String) {
        // 이전에 Repository에 만들어둔 연관 데이터 조회 함수를 호출합니다.
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
        _uiState.value = _uiState.value.copy(isBookmarked = !_uiState.value.isBookmarked)
    }
}