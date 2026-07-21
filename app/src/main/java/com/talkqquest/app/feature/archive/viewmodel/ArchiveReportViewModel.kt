package com.talkqquest.app.feature.archive.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talkqquest.app.feature.archive.data.ArchiveRepository
import com.talkqquest.app.feature.report.data.model.GrowthReport
import com.talkqquest.app.feature.report.data.model.WeeklyCompareReport
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ArchiveReportUiState(
    val isLoading: Boolean = true,
    val reportId: String = "",
    val title: String = "",
    val isBookmarked: Boolean = true,
    val growth: GrowthReport? = null,
    val weekly: WeeklyCompareReport? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class ArchiveReportViewModel @Inject constructor(
    private val repository: ArchiveRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArchiveReportUiState())
    val uiState: StateFlow<ArchiveReportUiState> = _uiState.asStateFlow()

    init {
        val reportId = savedStateHandle.get<String>("reportId") ?: ""
        loadReportData(reportId)
    }

    private fun loadReportData(reportId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, reportId = reportId) }

            val reportDetail = repository.getArchiveReportDetail(reportId)

            // 💡 저장소에서 리포트의 최신 북마크 상태를 가져옴
            val isBookmarked = repository.getArchiveReports().find { it.id == reportId }?.isSaved ?: false

            if (reportDetail != null) {
                val (title, growth, weekly) = reportDetail
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        title = title,
                        isBookmarked = isBookmarked, // 💡 초기 북마크 상태 적용
                        growth = growth,
                        weekly = weekly
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "리포트를 불러오지 못했어요."
                    )
                }
            }
        }
    }

    fun toggleBookmark() {
        val id = _uiState.value.reportId
        if (id.isNotEmpty()) {
            // 💡 1. 공통 저장소 데이터 갱신
            repository.toggleReportBookmark(id)
            // 💡 2. 로컬 UI 상태 갱신
            _uiState.update { it.copy(isBookmarked = !it.isBookmarked) }
        }
    }
}