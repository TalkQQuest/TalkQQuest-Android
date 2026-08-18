package com.talkqquest.app.feature.report.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talkqquest.app.core.network.ApiResult
import com.talkqquest.app.feature.report.data.ReportRepository
import com.talkqquest.app.feature.report.data.WeeklyCompareRepository
import com.talkqquest.app.feature.report.data.model.SavedReportItem
import com.talkqquest.app.feature.report.data.model.WeeklyCompareDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// 주간 비교 리포트(홈/알림창에서 진입) 화면 상태 (CONVENTIONS 6번: [화면이름]UiState)
data class WeeklyCompareUiState(
    val isLoading: Boolean = true,
    // 서버가 가진 주차 리포트 개수. 진입 시 목록 한 번 받아서 세어둔다.
    // 이동 자체엔 안 쓰고(이웃 id로 옮긴다), 화살표를 흐리게 할지 판단할 때만 본다.
    val weekCount: Int = 0,
    // 지금 보고 있는 주차의 상세. 주차를 옮기면 그때 받아와 캐시한다.
    val detail: WeeklyCompareDetail? = null,
    val isDetailLoading: Boolean = false,
    val errorMessage: String? = null,
    // 저장 시트: "리포트 저장하기"를 누르면 saveSheetReport가 생기며 시트가 올라옴.
    // 성장 리포트(ReportViewModel)와 같은 시트를 쓰고 카드 제목만 주차 범위로 채운다.
    val saveSheetReport: SavedReportItem? = null,
    // 시트 안 "보관함" 묶음 — 진입 시 GET /archives?type=report로 채운다.
    // 못 받으면 빈 목록이라 그 묶음이 통째로 접혀 사라진다(시트 자체는 정상 동작).
    val savedReports: List<SavedReportItem> = emptyList(),
) {
    // 좌우 이동 가능 여부는 서버가 상세에 실어 준 이웃 id로 판단한다(백엔드 2026-08-11).
    // 왼쪽 = 더 예전 주차, 오른쪽 = 더 최근 주차. 그 방향에 리포트가 없으면 서버가 null을 준다.
    val canGoPrev: Boolean get() = !detail?.prevReportId.isNullOrBlank()
    val canGoNext: Boolean get() = !detail?.nextReportId.isNullOrBlank()

    // 화살표를 흐리게 할지 — 시안엔 비활성 디자인이 없어 Gray/300으로 대신한다.
    // 주차가 하나뿐일 땐 흐리게 하지 않는다: 이동할 목록 자체가 아직 없는 것뿐인데
    // 양쪽이 다 회색이 되면 기능이 없는 화면처럼 보인다. 이 줄의 유일한 포인트 색이라
    // 시안대로 보라를 유지하고, 여러 주차가 있는데 끝에 닿았을 때만 흐려진다. (사용자 결정)
    // ※기획 문구는 "없으면 화살표 미노출"이지만, 이 화면은 흐리게로 확정돼 있어 그대로 둔다.
    private val hasRange: Boolean get() = weekCount > 1
    val dimPrev: Boolean get() = hasRange && !canGoPrev
    val dimNext: Boolean get() = hasRange && !canGoNext
}

@HiltViewModel
class WeeklyCompareViewModel @Inject constructor(
    private val weeklyCompareRepository: WeeklyCompareRepository,
    // 저장 시트의 "보관함" 묶음은 성장·주간이 한 목록이라(GET /archives?type=report)
    // 성장 리포트 화면과 같은 저장소에서 받아 온다.
    private val reportRepository: ReportRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    // 홈 도착 모달·알림에서 특정 리포트를 지정해 들어올 때 쓰는 id.
    // 비어 있으면(보관함 등 일반 진입) 목록의 가장 최근 리포트를 연다.
    private val startReportId: String =
        savedStateHandle.get<String>("reportId").orEmpty()

    private val _uiState = MutableStateFlow(WeeklyCompareUiState())
    val uiState: StateFlow<WeeklyCompareUiState> = _uiState.asStateFlow()

    // 이미 받아온 주차 상세 — 좌우로 오갈 때 매번 다시 부르지 않게.
    private val detailCache = mutableMapOf<String, WeeklyCompareDetail>()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val r = weeklyCompareRepository.getWeekList()) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(weekCount = r.data.size) }
                    // 지정해 들어온 리포트가 있으면 그것부터, 없으면 목록 맨 앞(가장 최근).
                    // 목록이 비면 id = null → Repository가 시안 목업을 돌려줘 화면이 비지 않는다.
                    loadDetail(
                        startReportId.ifBlank { null } ?: r.data.firstOrNull()?.id,
                        initial = true,
                    )
                    loadSavedReports()
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = r.message)
                }
                is ApiResult.Exception -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = "리포트를 불러오지 못했어요.")
                }
            }
        }
    }

    private suspend fun loadDetail(reportId: String?, initial: Boolean = false) {
        detailCache[reportId.orEmpty()]?.let { cached ->
            _uiState.update { it.copy(isLoading = false, isDetailLoading = false, detail = cached) }
            return
        }
        _uiState.update { it.copy(isDetailLoading = !initial) }
        when (val r = weeklyCompareRepository.getWeekDetail(reportId)) {
            is ApiResult.Success -> {
                detailCache[reportId.orEmpty()] = r.data
                _uiState.update {
                    it.copy(isLoading = false, isDetailLoading = false, detail = r.data)
                }
            }
            is ApiResult.Error -> _uiState.update {
                it.copy(isLoading = false, isDetailLoading = false, errorMessage = r.message)
            }
            is ApiResult.Exception -> _uiState.update {
                it.copy(isLoading = false, isDetailLoading = false, errorMessage = "리포트를 불러오지 못했어요.")
            }
        }
    }

    fun showPrevWeek() = moveTo { it.prevReportId }

    fun showNextWeek() = moveTo { it.nextReportId }

    private fun moveTo(target: (WeeklyCompareDetail) -> String?) {
        val id = _uiState.value.detail?.let(target)
        if (id.isNullOrBlank()) return // 그 방향엔 리포트가 없다
        viewModelScope.launch { loadDetail(id) }
    }

    // 시트에 뿌릴 "보관함" 묶음을 서버에서 채운다(GET /archives?type=report).
    // 실패하면 상태를 안 건드려 빈 목록이 유지되고, 시트에선 그 묶음만 접혀 사라진다.
    private suspend fun loadSavedReports() {
        val result = reportRepository.getSavedReports()
        if (result is ApiResult.Success) {
            _uiState.update { it.copy(savedReports = result.data) }
        }
    }

    // "리포트 저장하기" — 저장하고 시트를 올린다(성장 리포트와 같은 동작).
    // 성장 리포트의 삭제와 달리 원본은 남고 보관함 표시만 켜고 끈다.
    // 이미 저장돼 있으면 서버는 다시 부르지 않고 시트만 올린다 — 버튼은 "저장"이지 토글이 아니라,
    // 해제는 시트 안 북마크로 한다(성장 리포트와 같은 규칙).
    fun saveReport() {
        val detail = _uiState.value.detail ?: return
        if (detail.id.isBlank()) return // 목업 상태(서버 목록 없음)에선 저장할 대상이 없다
        if (!detail.isSaved) {
            val updated = detail.copy(isSaved = true)
            detailCache[detail.id] = updated
            _uiState.update { it.copy(detail = updated) } // 낙관적 표시
            viewModelScope.launch { weeklyCompareRepository.setSaved(detail.id, true) }
        }
        _uiState.update { state ->
            // 시트에 떠 있던 이전 저장분은 보관함 맨 앞으로 (연속 저장이 말이 되게 — 성장 리포트와 동일)
            val kept = state.saveSheetReport?.takeIf { it.isSaved }
            state.copy(
                saveSheetReport = SavedReportItem(
                    id = detail.id,
                    // 제목 자리는 주차 두 개로 카드가 직접 조립한다. title은 그림 없이 읽히는
                    // 자리(미리보기·접근성)를 위한 대체 문구.
                    title = "${detail.prevWeekLabel} → ${detail.thisWeekLabel} 주간 비교 리포트",
                    savedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")),
                    type = "weekly_compare",
                    prevWeekLabel = detail.prevWeekLabel,
                    thisWeekLabel = detail.thisWeekLabel,
                ),
                savedReports = (kept?.let { listOf(it) } ?: emptyList()) +
                    // 같은 리포트가 보관함 묶음에도 있으면 시트 위아래에 두 번 뜬다 — 아래쪽을 뺀다.
                    state.savedReports.filterNot { it.id == detail.id },
            )
        }
    }

    // 시트 안 북마크 토글. 시트 위 카드(방금 저장한 주간 비교)와 보관함 카드(성장이 섞여 있음)를
    // 모두 받으므로, 카드가 들고 있는 종류에 맞는 API로 보낸다.
    fun toggleReportSave(id: String) {
        val state = _uiState.value
        val sheet = state.saveSheetReport?.takeIf { it.id == id }
        val target = sheet ?: state.savedReports.firstOrNull { it.id == id } ?: return
        val next = !target.isSaved
        val isWeekly = target.type == "weekly_compare" || target.prevWeekLabel.isNotBlank()
        viewModelScope.launch {
            when {
                isWeekly -> weeklyCompareRepository.setSaved(id, next)
                // 성장 리포트는 해제(DELETE)만 이 화면에서 처리할 수 있다. 재저장(POST /reports)은
                // 리포트 id가 아니라 그 리포트가 나온 conversationId를 받는데 이 화면엔 그 값이 없다.
                // 그래서 다시 켜는 건 화면 표시만 되돌리고 서버는 건드리지 않는다.
                !next -> reportRepository.deleteReport(id)
            }
        }
        _uiState.update { s ->
            if (sheet != null) {
                s.copy(saveSheetReport = sheet.copy(isSaved = next))
            } else {
                s.copy(savedReports = s.savedReports.map { if (it.id == id) it.copy(isSaved = next) else it })
            }
        }
        // 시트 위 카드를 해제하면 본문 화면의 저장 상태도 같이 풀려야 한다(같은 리포트라서).
        if (sheet != null) {
            val detail = _uiState.value.detail
            if (detail != null && detail.id == id) {
                val updated = detail.copy(isSaved = next)
                detailCache[id] = updated
                _uiState.update { it.copy(detail = updated) }
            }
        }
    }

    // 시트가 다 내려간 뒤: 저장 상태로 닫혔으면 보관함 묶음으로 옮기고, 해제된 카드는 정리.
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
}
