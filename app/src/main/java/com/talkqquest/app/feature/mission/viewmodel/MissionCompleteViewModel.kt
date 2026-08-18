package com.talkqquest.app.feature.mission.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talkqquest.app.core.network.ApiResult
import com.talkqquest.app.feature.home.data.HomeRepository
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
    private val homeRepository: HomeRepository, // 완료 직후 홈 요약을 미리 받아 티어 공유 저장소를 갱신해 둠
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
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, result = result.data) }
                    // completeMission은 내부에서 피드백 생성(POST /feedback)까지 끝낸 뒤 반환하므로
                    // 이 시점엔 서버의 능력치 누적 점수가 이미 올라가 있다. 여기서 홈 요약을 미리
                    // 받아 TierStore를 갱신해 두면, 성장 리포트를 보지 않고 곧바로 홈으로 나가도
                    // 홈이 첫 프레임부터 새 티어·별을 그린다. 실패해도 화면 흐름엔 영향 없음.
                    launch { runCatching { homeRepository.getHomeSummary() } }
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
