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
    val saveSheetMissionId: Long? = null, // 방금 북마크로 저장해 시트에 띄울 미션 (null = 시트 닫힘)
) {
    // 선택된 칩 기준으로 걸러낸 목록. 난이도 칩이면 난이도로, 아니면 카테고리로 비교.
    val filteredMissions: List<MissionListItem>
        get() = when {
            selectedFilter == "전체" -> missions
            selectedFilter in difficultyFilters -> missions.filter { it.difficulty == selectedFilter }
            else -> missions.filter { it.category == selectedFilter }
        }

    // 시트에 보여줄 "저장됨" 미션. 해제돼도 미션은 그대로 넘겨서(isSaved=false)
    // 시트가 회색으로 바뀐 아이콘을 보여준 뒤 내려갈 수 있게 함.
    val saveSheetMission: MissionListItem?
        get() = missions.firstOrNull { it.id == saveSheetMissionId }

    // 시트 하단 "저장 목록"에 보여줄 다른 저장 미션 (디자인 기준 최대 2개).
    // TODO(서버 연동): 최근 저장순 정렬 필드가 생기면 그 순서로 교체.
    val otherSavedMissions: List<MissionListItem>
        get() = missions.filter { it.isSaved && it.id != saveSheetMissionId }.take(2)
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

    // showLoading=false: 화면 복귀 시 조용한 재조회(스피너 없이 북마크 등 최신 상태만 반영)
    fun loadMissions(showLoading: Boolean = true) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = showLoading, errorMessage = null) }

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
    // 저장하는 순간에만 "저장됨" 시트를 띄움. 해제는 시트 없이 아이콘만 되돌림(피그마에 해제 장면 없음 — 합의된 동작).
    fun toggleSave(missionId: Long) {
        missionRepository.toggleSave(missionId) // 공유 상태에 반영 (다른 화면·저장 목록에서도 보이게)
        _uiState.update { state ->
            val toggled = state.missions.map {
                if (it.id == missionId) it.copy(isSaved = !it.isSaved) else it
            }
            val nowSaved = toggled.firstOrNull { it.id == missionId }?.isSaved == true
            state.copy(
                missions = toggled,
                saveSheetMissionId = if (nowSaved) missionId else state.saveSheetMissionId,
            )
        }
    }

    // 저장 시트 닫기 (아래로 쓸어내리거나 바깥 탭)
    fun dismissSaveSheet() {
        _uiState.update { it.copy(saveSheetMissionId = null) }
    }
}
