package com.talkqquest.app.feature.archive.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talkqquest.app.core.network.ApiResult
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
            _uiState.update { it.copy(isLoading = true, reportId = reportId, errorMessage = null) }

            // 💡 저장소에서 리포트의 최신 북마크 상태를 가져옴 (기존 검색 목록 기준)
            val isBookmarked = repository.getArchiveReports().find { it.id == reportId }?.isSaved ?: false

            when (val result = repository.getArchiveReportDetail(reportId)) {
                is ApiResult.Success -> {
                    val (title, growth, weekly) = result.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            title = title,
                            isBookmarked = isBookmarked,
                            growth = growth,
                            weekly = weekly
                        )
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.message ?: "리포트를 불러오지 못했어요.")
                    }
                }
                is ApiResult.Exception -> {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = "네트워크 오류 발생")
                    }
                }
            }
        }
    }

    fun toggleBookmark() {
        val id = _uiState.value.reportId
        if (id.isNotEmpty()) {
            // 현재 리포트 북마크 취소/저장은 아직 백엔드 API가 연동되지 않았으므로 로컬 목업만 사용
            repository.toggleReportBookmark(id)
            _uiState.update { it.copy(isBookmarked = !it.isBookmarked) }
        }
    }
}