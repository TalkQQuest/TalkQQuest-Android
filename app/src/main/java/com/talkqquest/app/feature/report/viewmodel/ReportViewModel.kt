package com.talkqquest.app.feature.report.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talkqquest.app.core.network.ApiResult
import com.talkqquest.app.feature.report.data.ReportRepository
import com.talkqquest.app.feature.report.data.model.GrowthReport
import com.talkqquest.app.feature.report.data.model.WeeklyCompareReport
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
)

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val reportRepository: ReportRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    init {
        loadReports()
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
