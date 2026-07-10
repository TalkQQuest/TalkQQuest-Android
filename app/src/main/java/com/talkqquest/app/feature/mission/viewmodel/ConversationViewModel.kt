package com.talkqquest.app.feature.mission.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talkqquest.app.core.network.ApiResult
import com.talkqquest.app.feature.mission.data.MissionRepository
import com.talkqquest.app.feature.mission.data.model.ChatMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

// 대화 진행 화면 상태 (CONVENTIONS 6번: [화면이름]UiState)
data class ConversationUiState(
    val isLoading: Boolean = false,
    val missionTitle: String = "",              // 헤더 가운데 제목 (가변 — 미션 제목)
    val messages: List<ChatMessage> = emptyList(),
    val recommendations: List<String> = emptyList(), // 톡깨의 추천 답변 (서버 개수 가변)
    val recommendationsExpanded: Boolean = true, // 시작 = 펼침(목업 "대화 시작"), chevron으로 토글
    val inputText: String = "",
    val isAiReplying: Boolean = false,           // AI 응답 대기 중 (그동안 보내기 잠금)
    val showExitDialog: Boolean = false,         // 나가기 → "대화를 종료하시겠어요?" 팝업
    val errorMessage: String? = null,
) {
    val canSend: Boolean get() = inputText.isNotBlank() && !isAiReplying
}

@HiltViewModel
class ConversationViewModel @Inject constructor(
    private val missionRepository: MissionRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    // route 인자 conversation/{conversationId}. 서버 전이라 미션 id를 그대로 씀(TODO 서버: 대화 세션 id).
    private val conversationId: Long = checkNotNull(savedStateHandle["conversationId"])

    private val _uiState = MutableStateFlow(ConversationUiState())
    val uiState: StateFlow<ConversationUiState> = _uiState.asStateFlow()

    private val timeFormat = SimpleDateFormat("H:mm", Locale.KOREA)

    // 대화 시작 시각 — 종료 시 미션 완료 화면의 "대화 시간"에 전달.
    // TODO(서버 연동): 서버가 대화 시간을 기록하면 그 값으로 대체 가능.
    private val startedAtMs = System.currentTimeMillis()

    fun elapsedSeconds(): Long = (System.currentTimeMillis() - startedAtMs) / 1000
    private var nextMessageId = 1L
    private var turnIndex = 0 // 몇 번째 주고받기인지 (stub 대사·추천 묶음 순환용)

    init {
        startConversation()
    }

    fun startConversation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // 헤더 제목 = 미션 제목
            val title = when (val d = missionRepository.getMissionDetail(conversationId)) {
                is ApiResult.Success -> d.data.title
                else -> ""
            }
            when (val intro = missionRepository.getConversationIntro(conversationId)) {
                is ApiResult.Success -> {
                    val now = timeFormat.format(Date())
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            missionTitle = title,
                            messages = intro.data.map { text ->
                                ChatMessage(id = nextMessageId++, text = text, isFromUser = false, time = now)
                            },
                        )
                    }
                    loadRecommendations()
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = intro.message ?: "대화를 시작하지 못했어요.")
                }
                is ApiResult.Exception -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = "네트워크 연결을 확인해주세요.")
                }
            }
        }
    }

    // 추천 답변 조회. 실패해도 대화엔 지장 없어 조용히 무시.
    private fun loadRecommendations() {
        viewModelScope.launch {
            val result = missionRepository.getRecommendedReplies(turnIndex)
            if (result is ApiResult.Success) {
                _uiState.update { it.copy(recommendations = result.data) }
            }
        }
    }

    fun onInputChange(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    // 추천 칩 탭: 그 문장을 입력창에 채움(수정 가능). 카드 접기는 사용자가 chevron으로만 (사용자 결정).
    fun selectRecommendation(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun toggleRecommendations() {
        _uiState.update { it.copy(recommendationsExpanded = !it.recommendationsExpanded) }
    }

    fun sendMessage() {
        val text = _uiState.value.inputText.trim()
        if (text.isEmpty() || _uiState.value.isAiReplying) return

        _uiState.update {
            it.copy(
                messages = it.messages +
                    ChatMessage(nextMessageId++, text, isFromUser = true, time = timeFormat.format(Date())),
                inputText = "",
                isAiReplying = true,
            )
        }
        viewModelScope.launch {
            delay(1200) // 상대가 입력하는 듯한 간격 (stub. 서버 연동 시 실제 응답 대기로 대체)
            when (val reply = missionRepository.getAiReply(turnIndex)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        messages = it.messages +
                            ChatMessage(nextMessageId++, reply.data, isFromUser = false, time = timeFormat.format(Date())),
                        isAiReplying = false,
                    )
                }
                else -> _uiState.update { it.copy(isAiReplying = false) } // 응답 실패는 조용히 (재시도는 서버 연동 때)
            }
            turnIndex++
            loadRecommendations()
        }
    }

    fun setExitDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(showExitDialog = visible) }
    }
}
