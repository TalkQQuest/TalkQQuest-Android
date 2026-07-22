package com.talkqquest.app.feature.mission.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talkqquest.app.core.network.ApiResult
import com.talkqquest.app.feature.mission.data.MissionRepository
import com.talkqquest.app.feature.mission.data.model.MissionCompleteResult
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// 미션 완료&XP 화면 상태 (CONVENTIONS 6번: [화면이름]UiState)
data class MissionCompleteUiState(
    val isLoading: Boolean = true,
    val result: MissionCompleteResult? = null,
    val durationText: String = "00:00", // 대화 시간 mm:ss (목업 02:30)
    val errorMessage: String? = null,
)

@HiltViewModel
class MissionCompleteViewModel @Inject constructor(
    private val missionRepository: MissionRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val missionId: String = checkNotNull(savedStateHandle["missionId"])

    // 대화 화면이 넘겨준 대화 시간(초). 직접 진입(딥링크 등) 시 0 → 00:00.
    private val durationSec: Long = savedStateHandle["durationSec"] ?: 0L

    private val _uiState = MutableStateFlow(
        MissionCompleteUiState(durationText = formatDuration(durationSec)),
    )
    val uiState: StateFlow<MissionCompleteUiState> = _uiState.asStateFlow()

    init {
        loadResult()
    }

    fun loadResult() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = missionRepository.completeMission(missionId, durationSec)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(isLoading = false, result = result.data)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message ?: "완료 처리에 실패했어요.")
                }
                is ApiResult.Exception -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = "네트워크 연결을 확인해주세요.")
                }
            }
        }
    }

    private fun formatDuration(seconds: Long): String =
        String.format(Locale.KOREA, "%02d:%02d", seconds / 60, seconds % 60)
}
