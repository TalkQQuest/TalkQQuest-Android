package com.talkqquest.app.feature.report.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talkqquest.app.core.network.ApiResult
import com.talkqquest.app.feature.report.data.ReportRepository
import com.talkqquest.app.feature.report.data.model.GrowthReport
import com.talkqquest.app.feature.report.data.model.SavedReportItem
import com.talkqquest.app.feature.report.data.model.WeeklyCompareReport
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// 리포트 화면 상태 (CONVENTIONS 6번: [화면이름]UiState)
data class ReportUiState(
    val isLoading: Boolean = true,
    val growth: GrowthReport? = null,          // 성장 리포트 탭
    val weekly: WeeklyCompareReport? = null,   // 주간 비교 리포트 탭
    val errorMessage: String? = null,
    // 이 리포트가 나온 미션의 제목 — 저장 카드의 제목으로 들어감(CSS 목업이 미션명).
    // 리포트는 미션 대화의 AI 피드백에서 진입하므로, 피드백 화면이 route 인자로 넘겨준다.
    val missionTitle: String = "",
    // 리포트 저장 시트: "리포트 저장하기"를 누르면 saveSheetReport가 생기며 시트가 올라옴
    val saveSheetReport: SavedReportItem? = null,
    // 보관함(저장된 리포트) — TODO(서버 연동): 리포트 아카이브 API(E102)로 교체. 지금은 CSS 샘플 목업
    val savedReports: List<SavedReportItem> = listOf(
        SavedReportItem(id = "1", title = "최근 본 영화 이야기 하기", savedDate = "2026.08.20"),
        SavedReportItem(id = "2", title = "학교 생활 꿀팁 나누기", savedDate = "2026.08.19"),
        SavedReportItem(id = "3", title = "주말 계획 이야기하기", savedDate = "2026.08.18"),
        SavedReportItem(id = "4", title = "나의 취미를 소개해보기", savedDate = "2026.08.17"),
    ),
)

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val reportRepository: ReportRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    // 피드백 요약 "상세 리포트"에서 넘겨준 미션 제목 (route 인자). 직접 진입 시엔 빈 값.
    private val missionTitle: String = savedStateHandle["missionTitle"] ?: ""

    private val _uiState = MutableStateFlow(ReportUiState(missionTitle = missionTitle))
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    // 목업 저장 id: 초기 샘플(1,2)과 안 겹치게 100부터 (서버 오면 서버 id 사용)
    private var nextSaveId = 100L

    init {
        loadReports()
    }

    // "리포트 저장하기": 리포트를 저장하고 시트를 올림.
    // 카드 제목은 탭(성장/주간)과 무관하게 이 리포트가 나온 미션명 — 보관함에선 "어떤 미션의
    // 리포트인지"로 구분하고, 리포트 종류는 메타줄의 "리포트 열람"이 아니라 진입해서 확인한다(CSS).
    // TODO(서버 연동): POST /api/v1/reports/{reportId}/archive (E102) 호출로 교체.
    fun saveReport() {
        _uiState.update { state ->
            // 시트에 떠 있던 이전 저장분은 보관함 맨 앞으로 (연속 저장 데모가 말이 되게)
            val kept = state.saveSheetReport?.takeIf { it.isSaved }
            state.copy(
                saveSheetReport = SavedReportItem(
                    id = (nextSaveId++).toString(),
                    // 미션명이 없는 경로(아카이브 등 직접 진입)로 들어온 경우만 화면 이름으로 대체
                    title = state.missionTitle.ifBlank { "성장 리포트" },
                    savedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")),
                ),
                savedReports = (kept?.let { listOf(it) } ?: emptyList()) + state.savedReports,
            )
        }
    }

    // 시트 안 북마크 토글: 시트에 뜬 리포트를 해제하면 시트가 내려가고(화면 쪽 연출),
    // 보관함 카드는 해제 연출 후에도 목록에 남겨둬 다시 누르면 복구됨.
    fun toggleReportSave(id: String) {
        _uiState.update { state ->
            val sheet = state.saveSheetReport
            if (sheet != null && sheet.id == id) {
                state.copy(saveSheetReport = sheet.copy(isSaved = !sheet.isSaved))
            } else {
                state.copy(
                    savedReports = state.savedReports.map {
                        if (it.id == id) it.copy(isSaved = !it.isSaved) else it
                    },
                )
            }
        }
    }

    // 시트가 다 내려간 뒤: 저장 상태로 닫혔으면 보관함으로 옮기고, 해제된 카드는 정리.
    fun dismissSaveSheet() {
        _uiState.update { state ->
            val kept = state.saveSheetReport?.takeIf { it.isSaved }
            state.copy(
                saveSheetReport = null,
                savedReports = ((kept?.let { listOf(it) } ?: emptyList()) + state.savedReports)
                    .filter { it.isSaved },
            )
        }
    }

    // 두 탭 데이터를 한 번에 로드 — 탭 전환 때마다 다시 불러오지 않게.
    fun loadReports() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val growth = reportRepository.getGrowthReport()
            val weekly = reportRepository.getWeeklyCompare()
            if (growth is ApiResult.Success && weekly is ApiResult.Success) {
                _uiState.update {
                    it.copy(isLoading = false, growth = growth.data, weekly = weekly.data)
                }
            } else {
                val message = (growth as? ApiResult.Error)?.message
                    ?: (weekly as? ApiResult.Error)?.message
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = message ?: "리포트를 불러오지 못했어요.")
                }
            }
        }
    }
}
