package com.talkqquest.app.feature.mission.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talkqquest.app.core.network.ApiResult
import com.talkqquest.app.feature.mission.data.MissionRepository
import com.talkqquest.app.feature.mission.data.model.MissionListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// 필터 칩 목록. 디자인(UI 1차 v2, 피그마 스크린샷 확인 완료) 순서: 1줄째 4개 + 2줄째 3개.
val missionFilters = listOf("전체", "쉬움", "보통", "어려움", "짧은 대화", "친구 만들기", "일상 대화")

private val difficultyFilters = setOf("쉬움", "보통", "어려움")

// 미션 목록 화면 상태 (CONVENTIONS 6번: [화면이름]UiState)
data class MissionListUiState(
    val isLoading: Boolean = false,
    val missions: List<MissionListItem> = emptyList(),
    val selectedFilter: String = "전체",
    val errorMessage: String? = null,
) {
    // 선택된 칩 기준으로 걸러낸 목록. 난이도 칩이면 난이도로, 아니면 카테고리로 비교.
    val filteredMissions: List<MissionListItem>
        get() = when {
            selectedFilter == "전체" -> missions
            selectedFilter in difficultyFilters -> missions.filter { it.difficulty == selectedFilter }
            else -> missions.filter { it.category == selectedFilter }
        }
}

@HiltViewModel
class MissionListViewModel @Inject constructor(
    private val missionRepository: MissionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MissionListUiState())
    val uiState: StateFlow<MissionListUiState> = _uiState.asStateFlow()

    init {
        loadMissions()
    }

    fun loadMissions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = missionRepository.getMissions()) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(isLoading = false, missions = result.data)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message ?: "미션을 불러오지 못했어요.")
                }
                is ApiResult.Exception -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = "네트워크 연결을 확인해주세요.")
                }
            }
        }
    }

    // 필터 칩 선택 (서버 연동 후에도 우선 로컬 필터 — 서버 Query 필터로 바꿀지는 그때 결정)
    fun selectFilter(filter: String) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    // 북마크 토글. TODO(서버 연동): POST/DELETE /api/v1/missions/{id}/save 호출로 교체.
    fun toggleSave(missionId: Long) {
        _uiState.update { state ->
            state.copy(
                missions = state.missions.map {
                    if (it.id == missionId) it.copy(isSaved = !it.isSaved) else it
                },
            )
        }
    }
}
