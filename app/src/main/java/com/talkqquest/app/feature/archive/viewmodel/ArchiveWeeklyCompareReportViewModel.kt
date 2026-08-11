package com.talkqquest.app.feature.archive.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talkqquest.app.core.network.ApiResult
import com.talkqquest.app.feature.archive.data.ArchiveRepository
import com.talkqquest.app.feature.archive.data.model.WeeklyCompareReport
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ArchiveWeeklyCompareUiState(
    val reportId: String = "",
    val title: String = "",
    val isBookmarked: Boolean = true,
    val weekly: WeeklyCompareReport? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,

    // 💡 [추가] 네비게이션 제어를 위한 상태값
    val canGoPrev: Boolean = false, // 과거 리포트 존재 여부
    val canGoNext: Boolean = false, // 최신 리포트 존재 여부
    val allReportIds: List<String> = emptyList(),
    val currentIndex: Int = -1
)

@HiltViewModel
class ArchiveWeeklyCompareReportViewModel @Inject constructor(
    private val repository: ArchiveRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArchiveWeeklyCompareUiState())
    val uiState: StateFlow<ArchiveWeeklyCompareUiState> = _uiState.asStateFlow()

    init {
        val reportId: String? = savedStateHandle.get<String>("reportId")
        if (reportId != null) {
            _uiState.update { it.copy(reportId = reportId) }
            loadInitialData(reportId)
        }
    }

    private fun loadInitialData(initialId: String) {
        viewModelScope.launch {
            // 1. 보관함에서 주간 비교 리포트 목록 전체를 가져옵니다 (최신순 정렬 가정)
            val allReports = repository.getArchiveReports().filter { it.status == "주간 비교 리포트" }
            val ids = allReports.map { it.id }
            _uiState.update { it.copy(allReportIds = ids) }

            // 2. 초기 리포트 데이터를 로드합니다
            loadReportData(initialId)
        }
    }

    private fun loadReportData(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // 현재 ID의 인덱스를 찾아 이전/다음 이동 가능 여부 판단
            val ids = _uiState.value.allReportIds
            val index = ids.indexOf(id)

            // 리스트의 뒤쪽 인덱스일수록 과거 데이터
            val canGoPrev = index != -1 && index < ids.size - 1
            // 리스트의 앞쪽 인덱스일수록 최신 데이터
            val canGoNext = index > 0

            when (val result = repository.getArchiveReportDetail(id)) {
                is ApiResult.Success -> {
                    val (title, _, weekly) = result.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            reportId = id,
                            title = title,
                            weekly = weekly,
                            currentIndex = index,
                            canGoPrev = canGoPrev,
                            canGoNext = canGoNext
                        )
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                is ApiResult.Exception -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "네트워크 오류가 발생했습니다.") }
                }
            }
        }
    }

    // 💡 [추가] 과거 리포트로 이동
    fun showPrevWeek() {
        val state = _uiState.value
        if (state.canGoPrev) {
            loadReportData(state.allReportIds[state.currentIndex + 1])
        }
    }

    // 💡 [추가] 최신 리포트로 이동
    fun showNextWeek() {
        val state = _uiState.value
        if (state.canGoNext) {
            loadReportData(state.allReportIds[state.currentIndex - 1])
        }
    }

    fun toggleBookmark() {
        val state = _uiState.value
        if (state.reportId.isEmpty()) return

        if (state.isBookmarked) {
            _uiState.update { it.copy(isBookmarked = false) }

            viewModelScope.launch {
                when (repository.toggleReportBookmark(state.reportId, true)) {
                    is ApiResult.Success -> { }
                    else -> _uiState.update { it.copy(isBookmarked = true) }
                }
            }
        }
    }
}