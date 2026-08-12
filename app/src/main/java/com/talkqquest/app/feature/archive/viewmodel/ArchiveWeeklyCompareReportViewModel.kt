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

    val canGoPrev: Boolean = false,
    val canGoNext: Boolean = false,
    val prevReportId: String? = null, // 💡 서버에서 받아온 이전 리포트 ID
    val nextReportId: String? = null  // 💡 서버에서 받아온 다음 리포트 ID
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
            loadReportData(reportId)
        }
    }

    private fun loadReportData(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = repository.getWeeklyCompareReportDetail(id)) {
                is ApiResult.Success -> {
                    val uiModel = result.data

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            reportId = uiModel.id,
                            title = uiModel.title,
                            isBookmarked = uiModel.isSaved,
                            weekly = uiModel.report,
                            canGoPrev = uiModel.prevReportId != null,
                            canGoNext = uiModel.nextReportId != null,
                            prevReportId = uiModel.prevReportId,
                            nextReportId = uiModel.nextReportId
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

    // 💡 과거 리포트(prevReportId)로 깔끔하게 바로 이동
    fun showPrevWeek() {
        val prevId = _uiState.value.prevReportId
        if (prevId != null) {
            loadReportData(prevId)
        }
    }

    // 💡 최신 리포트(nextReportId)로 깔끔하게 바로 이동
    fun showNextWeek() {
        val nextId = _uiState.value.nextReportId
        if (nextId != null) {
            loadReportData(nextId)
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