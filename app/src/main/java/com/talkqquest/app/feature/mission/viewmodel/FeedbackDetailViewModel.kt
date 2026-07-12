package com.talkqquest.app.feature.mission.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talkqquest.app.core.network.ApiResult
import com.talkqquest.app.feature.mission.data.MissionRepository
import com.talkqquest.app.feature.mission.data.model.FeedbackResult
import com.talkqquest.app.feature.mission.data.model.SavedPhraseItem
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
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
    // 문장 저장 시트: 베스트 문장을 저장하면 saveSheetPhrase가 생기며 시트가 올라옴 (UI 5차)
    val saveSheetPhrase: SavedPhraseItem? = null,
    // 보관함(저장된 문장) — TODO(서버 연동): 문장 아카이브 API로 교체. 지금은 목업 샘플
    val savedPhrases: List<SavedPhraseItem> = listOf(
        SavedPhraseItem(1, "그 말씀 들으니 저도 기분이 좋아지네요", "2026.08.19"),
        SavedPhraseItem(2, "혹시 그때 어떤 기분이셨어요?", "2026.08.18"),
        SavedPhraseItem(3, "좋은 이야기 들려주셔서 감사해요", "2026.08.17"),
        SavedPhraseItem(4, "저도 비슷한 경험이 있어서 공감돼요", "2026.08.16"),
        SavedPhraseItem(5, "그건 정말 대단한 결정이었네요", "2026.08.15"),
    ),
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

    // 목업 저장 id: 초기 샘플(1~5)과 안 겹치게 100부터 (서버 오면 서버 id 사용)
    private var nextSaveId = 100L

    // 베스트 문장 저장 토글. 저장하면 문장 저장 시트가 올라옴 (UI 5차 "문장 저장시 바텀 시트").
    // TODO(서버 연동): 문장 저장 API(아카이브 '문장')로 교체 — 지금은 화면 상태만.
    fun togglePhraseSave() {
        _uiState.update { state ->
            val nowSaved = !state.isPhraseSaved
            if (!nowSaved) {
                // 저장 해제: 시트도 함께 내려감
                state.copy(isPhraseSaved = false, saveSheetPhrase = null)
            } else {
                state.copy(
                    isPhraseSaved = true,
                    saveSheetPhrase = SavedPhraseItem(
                        id = nextSaveId++,
                        phrase = state.result?.savedPhrase.orEmpty(),
                        savedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")),
                    ),
                )
            }
        }
    }

    // 시트 안 북마크 토글: 시트에 뜬 문장을 해제하면 시트가 내려가고(화면 쪽 연출),
    // 보관함 카드는 해제 연출 후에도 목록에 남겨둬 다시 누르면 복구됨.
    fun toggleSavedPhrase(id: Long) {
        _uiState.update { state ->
            val sheet = state.saveSheetPhrase
            if (sheet != null && sheet.id == id) {
                val nowSaved = !sheet.isSaved
                state.copy(saveSheetPhrase = sheet.copy(isSaved = nowSaved), isPhraseSaved = nowSaved)
            } else {
                state.copy(
                    savedPhrases = state.savedPhrases.map {
                        if (it.id == id) it.copy(isSaved = !it.isSaved) else it
                    },
                )
            }
        }
    }

    // 시트가 다 내려간 뒤: 저장 상태로 닫혔으면 보관함으로 옮기고, 해제된 카드는 정리.
    fun dismissSaveSheet() {
        _uiState.update { state ->
            val kept = state.saveSheetPhrase?.takeIf { it.isSaved }
            state.copy(
                saveSheetPhrase = null,
                savedPhrases = ((kept?.let { listOf(it) } ?: emptyList()) + state.savedPhrases)
                    .filter { it.isSaved },
            )
        }
    }
}
