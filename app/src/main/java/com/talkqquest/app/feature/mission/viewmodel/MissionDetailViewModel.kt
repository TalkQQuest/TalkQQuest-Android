package com.talkqquest.app.feature.mission.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talkqquest.app.core.network.ApiResult
import com.talkqquest.app.feature.mission.data.MissionRepository
import com.talkqquest.app.feature.mission.data.model.MissionDetail
import com.talkqquest.app.feature.mission.data.model.MissionListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// 미션 상세 화면 상태 (CONVENTIONS 6번: [화면이름]UiState)
data class MissionDetailUiState(
    val isLoading: Boolean = false,
    val detail: MissionDetail? = null,
    val errorMessage: String? = null,
    val saveSheetVisible: Boolean = false, // 방금 북마크로 저장해 시트를 띄울지
    val otherSavedMissions: List<MissionListItem> = emptyList(), // 시트 "저장 목록"용 (이 미션 제외 전부)
) {
    // 시트에 넘길 "저장됨" 미션. 해제돼도 미션은 그대로 넘겨서(isSaved=false)
    // 시트가 회색으로 바뀐 아이콘을 보여준 뒤 내려갈 수 있게 함 (목록 화면과 동일 동작).
    val saveSheetMission: MissionListItem?
        get() = if (saveSheetVisible) detail?.toListItem() else null
}

@HiltViewModel
class MissionDetailViewModel @Inject constructor(
    private val missionRepository: MissionRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val missionId: Long = checkNotNull(savedStateHandle["missionId"]) // route 인자 mission_detail/{missionId}

    private val _uiState = MutableStateFlow(MissionDetailUiState())
    val uiState: StateFlow<MissionDetailUiState> = _uiState.asStateFlow()

    init {
        loadDetail()
    }

    // showLoading=false: 화면 복귀 시 조용한 재조회(스피너 없이 북마크 등 최신 상태만 반영)
    fun loadDetail(showLoading: Boolean = true) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = showLoading, errorMessage = null) }

            when (val result = missionRepository.getMissionDetail(missionId)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, detail = result.data) }
                    loadOtherSavedMissions()
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

    // 시트 "저장 목록"에 보여줄 다른 저장 미션. 실패해도 화면엔 지장 없어 조용히 무시.
    // TODO(서버 연동): 저장 목록 전용 API(saved 필터)로 교체.
    private fun loadOtherSavedMissions() {
        viewModelScope.launch {
            val result = missionRepository.getMissions()
            if (result is ApiResult.Success) {
                _uiState.update { state ->
                    state.copy(
                        // 저장된 다른 미션 전부 — 개수 제한 없음 (목록 화면 시트와 동일 기준)
                        otherSavedMissions = result.data
                            .filter { it.isSaved && it.id != missionId },
                    )
                }
            }
        }
    }

    // 북마크 토글. TODO(서버 연동): POST/DELETE /api/v1/missions/{id}/save 호출로 교체.
    // 저장하는 순간에만 시트를 띄움. 해제는 시트 없이 아이콘만 되돌림(목록 화면과 동일 합의).
    fun toggleSave() {
        _uiState.value.detail?.let { missionRepository.toggleSave(it.id) } // 공유 상태 반영
        _uiState.update { state ->
            val detail = state.detail ?: return@update state
            val nowSaved = !detail.isSaved
            state.copy(
                detail = detail.copy(isSaved = nowSaved),
                saveSheetVisible = if (nowSaved) true else state.saveSheetVisible,
            )
        }
    }

    // 시트 "저장 목록"의 다른 미션 북마크 토글. 해제해도 즉시 빼지 않고 isSaved만 바꿈 —
    // 시트가 보라 풀림 → 가라앉는 퇴장 연출을 그리고, 연출 중 재저장하면 카드가 복귀함(시트 담당).
    // (즉시 filter하면 재저장 때 항목을 못 찾아 복귀가 안 됨. 목록 화면은 파생 계산이라 이미 동일 동작)
    // TODO(서버 연동): POST/DELETE /api/v1/missions/{id}/save 호출로 교체. (지금은 로컬 상태만 갱신)
    fun toggleSaveInList(missionId: Long) {
        val nowSaved = missionRepository.toggleSave(missionId) // 공유 상태 반영
        _uiState.update { state ->
            state.copy(
                otherSavedMissions = state.otherSavedMissions
                    .map { if (it.id == missionId) it.copy(isSaved = nowSaved) else it },
            )
        }
    }

    // 저장 시트 닫기 (쓸어내림·자동 닫힘)
    fun dismissSaveSheet() {
        _uiState.update { it.copy(saveSheetVisible = false) }
    }
}
