package com.talkqquest.app.feature.mission.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talkqquest.app.core.network.ApiResult
import com.talkqquest.app.feature.mission.data.MissionRepository
import com.talkqquest.app.feature.mission.data.model.MissionListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// 저장 목록(북마크→저장목록) 화면 상태 (CONVENTIONS 6번: [화면이름]UiState)
data class SavedMissionsUiState(
    val isLoading: Boolean = false,
    val savedMissions: List<MissionListItem> = emptyList(), // 이 화면에 들어올 때 저장돼 있던 미션들
    val selectedStatus: String = "완료", // 필터 칩 (완료/진행중/미완료) — 디자인 기본 선택 = 완료
    val errorMessage: String? = null,
) {
    // 선택한 진행 상태의 미션만. 북마크를 해제하면 즉시 빼지 않고 isSaved=false로 남겨
    // 화면이 퇴장 연출(보라 풀림 → 카드 가라앉음)을 보여준 뒤, 잠시 후 실제로 제거됨(아래 toggleSave).
    val filteredMissions: List<MissionListItem>
        get() = savedMissions.filter { it.status == selectedStatus }
}

@HiltViewModel
class SavedMissionsViewModel @Inject constructor(
    private val missionRepository: MissionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SavedMissionsUiState())
    val uiState: StateFlow<SavedMissionsUiState> = _uiState.asStateFlow()

    init {
        loadSavedMissions()
    }

    // TODO(서버 연동): 저장 목록 전용 API(saved 필터)로 교체. 지금은 전체 목록에서 저장된 것만 골라냄.
    fun loadSavedMissions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = missionRepository.getMissions()) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(isLoading = false, savedMissions = result.data.filter { m -> m.isSaved })
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message ?: "저장 목록을 불러오지 못했어요.")
                }
                is ApiResult.Exception -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = "네트워크 연결을 확인해주세요.")
                }
            }
        }
    }

    fun selectStatus(status: String) {
        _uiState.update { it.copy(selectedStatus = status) }
    }

    // 해제 후 목록에서 실제 제거까지의 대기 — 화면의 퇴장 연출(풀림 인지 250 + 퇴장 350)보다 살짝 길게.
    private val removalJobs = mutableMapOf<Long, Job>()

    // 북마크 토글. TODO(서버 연동): POST/DELETE /api/v1/missions/{id}/save 호출로 교체.
    // 해제: isSaved=false로 두고(화면이 퇴장 연출을 그림) 연출이 끝난 뒤 목록에서 제거.
    // 연출 중 재저장: 제거 예약을 취소하고 카드 복귀(실수 복구).
    fun toggleSave(missionId: Long) {
        val nowSaved = missionRepository.toggleSave(missionId) // 공유 상태 반영
        _uiState.update { state ->
            state.copy(
                savedMissions = state.savedMissions
                    .map { if (it.id == missionId) it.copy(isSaved = nowSaved) else it },
            )
        }
        if (!nowSaved) {
            removalJobs[missionId] = viewModelScope.launch {
                delay(700)
                _uiState.update { state ->
                    state.copy(savedMissions = state.savedMissions.filterNot { it.id == missionId })
                }
                removalJobs.remove(missionId)
            }
        } else {
            removalJobs.remove(missionId)?.cancel()
        }
    }
}
