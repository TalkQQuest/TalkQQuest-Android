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
    // 헤더 "대화 완료" → "대화를 종료하시겠어요?" (미션 완료·저장)
    val showCompleteDialog: Boolean = false,
    // 헤더 뒤로가기 → "정말 나가시겠습니까?" (저장하지 않고 종료)
    val showLeaveDialog: Boolean = false,
    val errorMessage: String? = null,
) {
    val canSend: Boolean get() = inputText.isNotBlank() && !isAiReplying
}

@HiltViewModel
class ConversationViewModel @Inject constructor(
    private val missionRepository: MissionRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    // route 인자 이름은 conversationId지만 실제로 넘어오는 값은 미션 id다(NavGraph가 "conversation/$missionId"로 이동).
    // 서버 대화 세션 id는 MissionRepository가 createConversation 응답에서 받아 activeConversationId로 들고 있다.
    private val conversationId: String = checkNotNull(savedStateHandle["conversationId"])

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
            val startedAt = System.currentTimeMillis()
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // 헤더 제목 = 미션 제목
            val title = when (val d = missionRepository.getMissionDetail(conversationId)) {
                is ApiResult.Success -> d.data.title
                else -> ""
            }
            val intro = missionRepository.getConversationIntro(conversationId)
            awaitMinimumIntro(startedAt)
            when (intro) {
                is ApiResult.Success -> {
                    val now = timeFormat.format(Date())
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            missionTitle = title,
                            messages = intro.data.map { text ->
                                ChatMessage(id = (nextMessageId++).toString(), text = text, isFromUser = false, time = now)
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

    // 대화 진입 대기 화면이 깜빡하고 지나가지 않게 최소 1초는 보여준다(사용자 결정).
    // 서버가 그보다 오래 걸리면 그냥 끝날 때까지 기다린다 — 여기서 더 늘리지 않는다.
    private suspend fun awaitMinimumIntro(startedAt: Long) {
        val elapsed = System.currentTimeMillis() - startedAt
        if (elapsed < MIN_INTRO_MILLIS) delay(MIN_INTRO_MILLIS - elapsed)
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
                    ChatMessage((nextMessageId++).toString(), text, isFromUser = true, time = timeFormat.format(Date())),
                inputText = "",
                isAiReplying = true,
            )
        }
        viewModelScope.launch {
            // 서버(LLM) 응답 대기가 실제 간격이 됨. 오프라인 stub 폴백일 때만 즉답이라 최소 간격 유지.
            delay(600)
            when (val reply = missionRepository.sendUserMessage(text, turnIndex)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        messages = it.messages +
                            ChatMessage((nextMessageId++).toString(), reply.data, isFromUser = false, time = timeFormat.format(Date())),
                        isAiReplying = false,
                    )
                }
                else -> _uiState.update { it.copy(isAiReplying = false) } // 응답 실패는 조용히 (재시도는 서버 연동 때)
            }
            turnIndex++
            loadRecommendations()
        }
    }

    // "정말 나가시겠습니까?"에서 나가기 — 서버에 열려 있는 대화를 abandoned로 닫는다.
    // 화면은 기다리지 않고 바로 벗어나고, 요청은 Repository가 자기 스코프에서 마저 보낸다.
    // ★완료 경로에서는 부르지 않는다: 미션 완료 API가 종료를 겸하고, 이걸 먼저 부르면 완료가 막힌다.
    fun abandonConversation() = missionRepository.abandonConversation()

    fun setCompleteDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(showCompleteDialog = visible) }
    }

    fun setLeaveDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(showLeaveDialog = visible) }
    }
}

// 대화 진입 대기 화면 최소 노출 시간(사용자 결정).
private const val MIN_INTRO_MILLIS = 1_000L
