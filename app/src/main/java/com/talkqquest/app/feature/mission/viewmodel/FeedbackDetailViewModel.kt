package com.talkqquest.app.feature.mission.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talkqquest.app.core.network.ApiResult
import com.talkqquest.app.feature.mission.data.MissionRepository
import com.talkqquest.app.feature.mission.data.model.FeedbackResult
import com.talkqquest.app.feature.mission.data.model.SavedPhraseItem
import com.talkqquest.app.feature.mission.data.model.scoreItems
import com.talkqquest.app.feature.mission.data.model.textFor
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
    // 보관함(저장된 문장) — 진입 시 GET /archives?type=phrase로 교체됨.
    // 아래 값은 조회 실패/데모(USE_MOCK) 시 쓰이는 폴백 샘플.
    val savedPhrases: List<SavedPhraseItem> = listOf(
        SavedPhraseItem("1", "그 말씀 들으니 저도 기분이 좋아지네요", "2026.08.19"),
        SavedPhraseItem("2", "혹시 그때 어떤 기분이셨어요?", "2026.08.18"),
        SavedPhraseItem("3", "좋은 이야기 들려주셔서 감사해요", "2026.08.17"),
        SavedPhraseItem("4", "저도 비슷한 경험이 있어서 공감돼요", "2026.08.16"),
        SavedPhraseItem("5", "그건 정말 대단한 결정이었네요", "2026.08.15"),
    ),
)

// 지금 보고 있는 항목(itemIndex)의 베스트 문장 — 항목별 문구가 없으면 피드백 공통 값으로 폴백
private fun FeedbackDetailUiState.currentPhrase(): String {
    val res = result ?: return ""
    val label = res.scoreItems().getOrElse(itemIndex) { res.scoreItems().first() }.first
    return res.textFor(label).savedPhrase
}

@HiltViewModel
class FeedbackDetailViewModel @Inject constructor(
    private val missionRepository: MissionRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val feedbackId: String = checkNotNull(savedStateHandle["feedbackId"])

    // 요약에서 탭한 항목 번호. 직접 진입(딥링크 등) 시 0 = 첫 항목(친절한 태도).
    private val itemIndex: Int = savedStateHandle["item"] ?: 0

    private val _uiState = MutableStateFlow(FeedbackDetailUiState(itemIndex = itemIndex))
    val uiState: StateFlow<FeedbackDetailUiState> = _uiState.asStateFlow()

    init {
        loadFeedback()
        loadSavedPhrases()
    }

    // 시트에 뿌릴 "최근 저장한 문장"을 서버에서 채운다(GET /archives?type=phrase).
    // 실패/데모(USE_MOCK)면 상태를 건드리지 않아 기본값(목업 샘플)이 그대로 남는다 — 데모가 안 죽게.
    private fun loadSavedPhrases() {
        viewModelScope.launch {
            val result = missionRepository.getSavedPhrases()
            if (result is ApiResult.Success) {
                _uiState.update { it.copy(savedPhrases = result.data) }
            }
        }
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

    // 서버 저장 후 받은 실제 phraseId로 카드 id를 갈아끼움 (해제 시 DELETE 대상이 되게).
    private fun swapPhraseId(oldId: String, newId: String) {
        _uiState.update { s ->
            s.copy(
                saveSheetPhrase = s.saveSheetPhrase?.takeIf { it.id == oldId }?.copy(id = newId)
                    ?: s.saveSheetPhrase,
                savedPhrases = s.savedPhrases.map { if (it.id == oldId) it.copy(id = newId) else it },
            )
        }
    }

    // 베스트 문장 저장 토글. 저장하면 문장 저장 시트가 올라옴 (UI 5차 "문장 저장시 바텀 시트").
    // 저장 시 서버(아카이브 '문장', POST /archives/phrases)에도 저장 — conversationId가 있을 때만.
    // (서버 피드백 미연동/stub이면 conversationId=null이라 화면 표시만. 데모 스위치면 serverCall이 건너뜀.)
    fun togglePhraseSave() {
        val willSave = !_uiState.value.isPhraseSaved
        // 해제로 바뀌는 경우: 서버 보관함(문장)에서도 삭제. 서버 저장 성공분만 실제 id를 가짐.
        if (!willSave) {
            _uiState.value.saveSheetPhrase?.id?.let { id ->
                viewModelScope.launch { missionRepository.deletePhrase(id) }
            }
        }
        val localId = (nextSaveId++).toString()
        _uiState.update { state ->
            val nowSaved = !state.isPhraseSaved
            if (!nowSaved) {
                // 저장 해제: 시트도 함께 내려감
                state.copy(isPhraseSaved = false, saveSheetPhrase = null)
            } else {
                state.copy(
                    isPhraseSaved = true,
                    saveSheetPhrase = SavedPhraseItem(
                        id = localId, // 서버 저장 성공 시 아래 코루틴이 서버 phraseId로 교체
                        // 지금 보고 있는 항목의 베스트 문장 (항목마다 다름)
                        phrase = state.currentPhrase(),
                        savedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")),
                    ),
                )
            }
        }
        // 새로 저장하는 경우에만 서버 저장 시도(낙관적 UI는 위에서 이미 반영, 결과는 조용히 처리).
        if (willSave) {
            val state = _uiState.value
            val cid = state.result?.conversationId
            val content = state.currentPhrase()
            if (!cid.isNullOrBlank() && content.isNotBlank()) {
                // 저장 응답의 서버 phraseId로 카드 id를 교체 — 이후 북마크 해제(DELETE)가 가능해짐.
                viewModelScope.launch {
                    val saved = missionRepository.savePhrase(cid, content)
                    val serverId = (saved as? ApiResult.Success)?.data?.id?.takeIf { it.isNotBlank() }
                        ?: return@launch
                    swapPhraseId(localId, serverId)
                }
            }
        }
    }

    // 시트 안 북마크 토글: 시트에 뜬 문장을 해제하면 시트가 내려가고(화면 쪽 연출),
    // 보관함 카드는 해제 연출 후에도 목록에 남겨둬 다시 누르면 복구됨.
    fun toggleSavedPhrase(id: String) {
        // 서버 상태를 화면과 항상 같게 맞춘다: 끄면 삭제(DELETE), 다시 켜면 재저장(POST) 후 새 id로 교체.
        // (한쪽만 반영하면 화면엔 저장됐는데 보관함엔 없는 상태가 됨)
        val snapshot = _uiState.value
        val target = snapshot.saveSheetPhrase?.takeIf { it.id == id }
            ?: snapshot.savedPhrases.firstOrNull { it.id == id }
        if (target?.isSaved == true) {
            viewModelScope.launch { missionRepository.deletePhrase(id) }
        } else if (target != null) {
            val cid = snapshot.result?.conversationId
            if (!cid.isNullOrBlank() && target.phrase.isNotBlank()) {
                viewModelScope.launch {
                    val saved = missionRepository.savePhrase(cid, target.phrase)
                    val serverId = (saved as? ApiResult.Success)?.data?.id?.takeIf { it.isNotBlank() }
                        ?: return@launch
                    swapPhraseId(id, serverId)
                }
            }
        }
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
