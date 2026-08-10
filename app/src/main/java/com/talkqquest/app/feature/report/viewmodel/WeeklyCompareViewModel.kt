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
    // 주차 목록 — 최신이 앞(index 0). 지금은 서버가 한 주치만 줘서 1개다.
    val weeks: List<WeeklyCompareDetail> = emptyList(),
    val index: Int = 0,
    val errorMessage: String? = null,
) {
    val current: WeeklyCompareDetail? get() = weeks.getOrNull(index)

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

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val r = weeklyCompareRepository.getWeeklyCompareDetail()) {
                // TODO(서버 연동): 주간 비교가 목록 API로 바뀌면 listOf(...) 대신 응답 목록을 그대로 넣는다.
                //   그때부터 좌우 화살표가 실제로 동작한다(지금은 한 주치뿐이라 양쪽 다 비활성).
                is ApiResult.Success -> _uiState.update {
                    it.copy(isLoading = false, weeks = listOf(r.data), index = 0)
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

    fun showPrevWeek() {
        _uiState.update { if (it.canGoPrev) it.copy(index = it.index + 1) else it }
    }

    fun showNextWeek() {
        _uiState.update { if (it.canGoNext) it.copy(index = it.index - 1) else it }
    }
}
