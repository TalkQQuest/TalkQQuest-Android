package com.talkqquest.app.feature.report.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talkqquest.app.core.network.ApiResult
import com.talkqquest.app.feature.report.data.ReportRepository
import com.talkqquest.app.feature.report.data.model.GrowthTierReport
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
    val growth: GrowthTierReport? = null,      // 성장 리포트(B, 실전 티어 + 핵심 역량)
    val weekly: WeeklyCompareReport? = null,   // 주간 비교 리포트 탭
    val errorMessage: String? = null,
    // 이 리포트가 나온 미션의 제목 — 저장 카드의 제목으로 들어감(CSS 목업이 미션명).
    // 리포트는 미션 대화의 AI 피드백에서 진입하므로, 피드백 화면이 route 인자로 넘겨준다.
    val missionTitle: String = "",
    // 리포트 저장 시트: "리포트 저장하기"를 누르면 saveSheetReport가 생기며 시트가 올라옴
    val saveSheetReport: SavedReportItem? = null,
    // 보관함(저장된 리포트) — 진입 시 GET /archives?type=report로 교체됨.
    // 아래 값은 조회 실패/데모(USE_MOCK) 시 쓰이는 폴백 샘플.
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

    // 피드백 요약 "성장 리포트"에서 넘겨준 미션 제목 (route 인자). 직접 진입 시엔 빈 값.
    private val missionTitle: String = savedStateHandle["missionTitle"] ?: ""

    // 이 리포트가 나온 대화 id (route 인자). POST /reports가 2026-08-10부터 conversationId를 받는다.
    // 피드백 응답(FeedbackDetailResponse.conversationId)에서 받아 피드백 화면이 넘겨준다.
    // 직접 진입(아카이브 등)이면 빈 값 → 저장은 화면 표시만 되고 서버 저장은 건너뛴다.
    private val conversationId: String = savedStateHandle["conversationId"] ?: ""

    // 마름모 꼭짓점 "+N" — 이번 대화로 오른 점수 4개(친절,주도,공감,질문 순).
    // 서버 성장 리포트 응답에 증가분 필드가 없어 피드백 화면이 route로 넘겨준다.
    // 직접 진입 등으로 비어 있으면 빈 목록 → 화면이 "+N"을 그리지 않는다.
    private val gains: List<Int> = (savedStateHandle["gains"] ?: "")
        .split(",")
        .mapNotNull { it.trim().toIntOrNull() }

    private val _uiState = MutableStateFlow(ReportUiState(missionTitle = missionTitle))
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    // 목업 저장 id: 초기 샘플(1,2)과 안 겹치게 100부터 (서버 오면 서버 id 사용)
    private var nextSaveId = 100L

    // 카드 id → 저장할 때 쓴 리포트 종류. 북마크를 껐다 다시 켤 때 같은 종류로 재저장하려고 기억한다.
    private val savedReportTypes = mutableMapOf<String, String>()
    private var lastSavedType = "growth"

    // 서버 저장 후 받은 실제 id로 카드 id를 갈아끼움 (해제 시 DELETE 대상이 되게).
    private fun swapReportId(oldId: String, newId: String) {
        _uiState.update { s ->
            s.copy(
                saveSheetReport = s.saveSheetReport?.takeIf { it.id == oldId }?.copy(id = newId)
                    ?: s.saveSheetReport,
                savedReports = s.savedReports.map { if (it.id == oldId) it.copy(id = newId) else it },
            )
        }
    }

    init {
        loadReports()
    }

    // "리포트 저장하기": 리포트를 저장하고 시트를 올림.
    // 카드 제목은 탭(성장/주간)과 무관하게 이 리포트가 나온 미션명 — 보관함에선 "어떤 미션의
    // 리포트인지"로 구분하고, 리포트 종류는 메타줄의 "리포트 열람"이 아니라 진입해서 확인한다(CSS).
    // 저장 시 서버(POST /reports, 바디 conversationId)에도 저장. 낙관적 UI는 그대로 유지,
    // 데모/실패면 serverCall이 건너뛰어 화면 표시만(보관함 실반영은 실서버 모드에서).
    fun saveReport(reportType: String) {
        val localId = (nextSaveId++).toString()
        lastSavedType = reportType
        savedReportTypes[localId] = reportType
        // 저장 응답의 서버 reportId를 받아 카드 id를 교체 — 이후 북마크 해제(DELETE)가 가능해짐.
        viewModelScope.launch {
            val saved = reportRepository.saveReport(conversationId)
            val serverId = (saved as? ApiResult.Success)?.data?.reportId?.takeIf { it.isNotBlank() } ?: return@launch
            savedReportTypes[serverId] = reportType
            swapReportId(localId, serverId)
        }
        _uiState.update { state ->
            // 시트에 떠 있던 이전 저장분은 보관함 맨 앞으로 (연속 저장 데모가 말이 되게)
            val kept = state.saveSheetReport?.takeIf { it.isSaved }
            state.copy(
                saveSheetReport = SavedReportItem(
                    id = localId, // 서버 저장 성공 시 위 코루틴이 서버 reportId로 교체
                    // 미션명이 없는 경로(아카이브 등 직접 진입)로 들어온 경우만 화면 이름으로 대체
                    title = state.missionTitle.ifBlank { "성장 리포트" },
                    savedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")),
                    type = reportType,
                ),
                savedReports = (kept?.let { listOf(it) } ?: emptyList()) + state.savedReports,
            )
        }
    }

    // 시트 안 북마크 토글: 시트에 뜬 리포트를 해제하면 시트가 내려가고(화면 쪽 연출),
    // 보관함 카드는 해제 연출 후에도 목록에 남겨둬 다시 누르면 복구됨.
    fun toggleReportSave(id: String) {
        // 서버 상태를 화면과 항상 같게 맞춘다: 끄면 삭제(DELETE), 다시 켜면 재저장(POST) 후 새 id로 교체.
        // (한쪽만 반영하면 화면엔 저장됐는데 보관함엔 없는 상태가 됨)
        val wasSaved = _uiState.value.saveSheetReport?.takeIf { it.id == id }?.isSaved
            ?: _uiState.value.savedReports.firstOrNull { it.id == id }?.isSaved
        if (wasSaved == true) {
            viewModelScope.launch { reportRepository.deleteReport(id) }
        } else if (wasSaved == false) {
            // 재저장 종류: 카드가 들고 있는 서버 type(GET /reports 응답) 우선, 없으면 이 세션에서
            // 저장했던 종류 — 둘 다 없을 때만 마지막 저장 종류로 폴백.
            val type = _uiState.value.savedReports.firstOrNull { it.id == id }?.type?.takeIf { it.isNotBlank() }
                ?: savedReportTypes[id]
                ?: lastSavedType
            viewModelScope.launch {
                val saved = reportRepository.saveReport(conversationId)
                val serverId = (saved as? ApiResult.Success)?.data?.reportId?.takeIf { it.isNotBlank() }
                    ?: return@launch
                savedReportTypes[serverId] = type
                swapReportId(id, serverId)
            }
        }
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
            val growth = reportRepository.getGrowthReport(gains)
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
            loadSavedReports()
        }
    }

    // 시트에 뿌릴 "최근 저장한 리포트"를 서버에서 채운다(GET /archives?type=report).
    // 실패/데모(USE_MOCK)면 상태를 건드리지 않아 기본값(목업 샘플)이 그대로 남는다 — 데모가 안 죽게.
    private suspend fun loadSavedReports() {
        val result = reportRepository.getSavedReports()
        if (result is ApiResult.Success) {
            _uiState.update { it.copy(savedReports = result.data) }
        }
    }
}
