package com.talkqquest.app.feature.mission.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talkqquest.app.core.network.ApiResult
import com.talkqquest.app.feature.mission.data.MissionRepository
import com.talkqquest.app.feature.mission.data.model.ConversationPrep
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// 대화 준비 화면 상태 (CONVENTIONS 6번: [화면이름]UiState)
data class ConversationPrepUiState(
    val isLoading: Boolean = false,
    val prep: ConversationPrep? = null,
    val errorMessage: String? = null,
)

@HiltViewModel
class ConversationPrepViewModel @Inject constructor(
    private val missionRepository: MissionRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val missionId: Long = checkNotNull(savedStateHandle["missionId"]) // route 인자 conversation_prep/{missionId}
    private var refreshIndex = 0 // 새로고침 누를 때마다 다음 문장 묶음으로 (stub용, 서버 연동 시 불필요)

    private val _uiState = MutableStateFlow(ConversationPrepUiState())
    val uiState: StateFlow<ConversationPrepUiState> = _uiState.asStateFlow()

    init {
        loadPrep()
    }

    fun loadPrep() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = missionRepository.getConversationPrep(missionId, refreshIndex)) {
                is ApiResult.Success -> _uiState.update { it.copy(isLoading = false, prep = result.data) }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message ?: "대화 준비를 불러오지 못했어요.")
                }
                is ApiResult.Exception -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = "네트워크 연결을 확인해주세요.")
                }
            }
        }
    }

    // 첫 마디 새로고침 — 새 문장 묶음 조회. (로딩 스피너 대신 문장만 조용히 갱신)
    fun refreshOpeners() {
        refreshIndex++
        viewModelScope.launch {
            when (val result = missionRepository.getConversationPrep(missionId, refreshIndex)) {
                is ApiResult.Success -> _uiState.update { it.copy(prep = result.data) }
                else -> Unit // 새로고침 실패는 조용히 무시(기존 문장 유지)
            }
        }
    }
}
