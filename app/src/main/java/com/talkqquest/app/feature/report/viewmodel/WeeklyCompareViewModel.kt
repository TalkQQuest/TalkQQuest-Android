package com.talkqquest.app.feature.report.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talkqquest.app.core.network.ApiResult
import com.talkqquest.app.feature.report.data.WeeklyCompareRepository
import com.talkqquest.app.feature.report.data.model.WeeklyCompareDetail
import dagger.hilt.android.lifecycle.HiltViewModel
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
) : ViewModel() {

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
                    // 목록은 "어느 리포트로 들어갈지"에만 쓴다 — 맨 앞이 가장 최근.
                    // 목록이 비면 id = null → Repository가 시안 목업을 돌려줘 화면이 비지 않는다.
                    loadDetail(r.data.firstOrNull()?.id, initial = true)
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

    // "리포트 저장하기" — 성장 리포트의 삭제와 달리 원본은 남고 보관함 표시만 켜고 끈다.
    fun toggleSaved() {
        val detail = _uiState.value.detail ?: return
        if (detail.id.isBlank()) return // 목업 상태(서버 목록 없음)에선 저장할 대상이 없다
        val next = !detail.isSaved
        _uiState.update { it.copy(detail = detail.copy(isSaved = next)) } // 낙관적 표시
        viewModelScope.launch {
            val r = weeklyCompareRepository.setSaved(detail.id, next)
            val confirmed = (r as? ApiResult.Success)?.data ?: return@launch
            val updated = detail.copy(isSaved = confirmed)
            detailCache[detail.id] = updated
            _uiState.update { it.copy(detail = updated) }
        }
    }
}
