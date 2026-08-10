package com.talkqquest.app.feature.report.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talkqquest.app.core.network.ApiResult
import com.talkqquest.app.feature.report.data.WeeklyCompareRepository
import com.talkqquest.app.feature.report.data.model.WeeklyCompareDetail
import com.talkqquest.app.feature.report.data.model.WeeklyCompareListItem
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
    // 서버 주차 목록 — 최신이 앞(index 0). 실서버 확인: 가입일 기준 끝난 주끼리 비교한 목록.
    val weeks: List<WeeklyCompareListItem> = emptyList(),
    val index: Int = 0,
    // 지금 보고 있는 주차의 상세. 주차를 옮기면 그때 받아와 캐시한다.
    val detail: WeeklyCompareDetail? = null,
    val isDetailLoading: Boolean = false,
    val errorMessage: String? = null,
) {
    // 왼쪽 = 더 예전 주차. 목록 끝이면 못 간다.
    val canGoPrev: Boolean get() = index + 1 < weeks.size

    // 오른쪽 = 더 최근 주차. 맨 앞(가장 최근)이면 못 간다.
    val canGoNext: Boolean get() = index > 0

    // 화살표를 흐리게 할지 — 시안엔 비활성 디자인이 없어 Gray/300으로 대신한다.
    // 주차가 하나뿐일 땐 흐리게 하지 않는다: 이동할 목록 자체가 아직 없는 것뿐인데
    // 양쪽이 다 회색이 되면 기능이 없는 화면처럼 보인다. 이 줄의 유일한 포인트 색이라
    // 시안대로 보라를 유지하고, 여러 주차가 있는데 끝에 닿았을 때만 흐려진다. (사용자 결정)
    private val hasRange: Boolean get() = weeks.size > 1
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
                    _uiState.update { it.copy(weeks = r.data, index = 0) }
                    // 목록이 비면 item = null → Repository가 시안 목업을 돌려줘 화면이 비지 않는다.
                    loadDetail(r.data.firstOrNull(), initial = true)
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

    private suspend fun loadDetail(item: WeeklyCompareListItem?, initial: Boolean = false) {
        detailCache[item?.id.orEmpty()]?.let { cached ->
            _uiState.update { it.copy(isLoading = false, isDetailLoading = false, detail = cached) }
            return
        }
        _uiState.update { it.copy(isDetailLoading = !initial) }
        when (val r = weeklyCompareRepository.getWeekDetail(item)) {
            is ApiResult.Success -> {
                detailCache[item?.id.orEmpty()] = r.data
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

    fun showPrevWeek() = moveTo { it.index + 1 }

    fun showNextWeek() = moveTo { it.index - 1 }

    private fun moveTo(next: (WeeklyCompareUiState) -> Int) {
        val s = _uiState.value
        val target = next(s)
        if (target !in s.weeks.indices) return
        _uiState.update { it.copy(index = target) }
        viewModelScope.launch { loadDetail(s.weeks[target]) }
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
