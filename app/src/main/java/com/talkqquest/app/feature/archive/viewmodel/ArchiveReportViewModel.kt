package com.talkqquest.app.feature.archive.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talkqquest.app.core.network.ApiResult
import com.talkqquest.app.feature.archive.data.ArchiveRepository

// 💡 새롭게 아카이브 전용으로 만든 모델들 임포트!
import com.talkqquest.app.feature.archive.data.model.GrowthReport
import com.talkqquest.app.feature.archive.data.model.WeeklyCompareReport

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ArchiveReportUiState(
    val reportId: String = "",
    val title: String = "",
    val isBookmarked: Boolean = true,
    val growth: GrowthReport? = null,
    val weekly: WeeklyCompareReport? = null,
    val isLoading: Boolean = false,
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
        val reportId: String? = savedStateHandle.get<String>("reportId")
        if (reportId != null) {
            _uiState.update { it.copy(reportId = reportId) }
            loadReportData(reportId)
        }
    }

    private fun loadReportData(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = repository.getArchiveReportDetail(id)) {
                is ApiResult.Success -> {
                    val (title, growth, weekly) = result.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            title = title,
                            growth = growth,
                            weekly = weekly
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

    fun toggleBookmark() {
        val state = _uiState.value
        if (state.reportId.isEmpty()) return

        // 북마크 상태 우선 토글 (Optimistic UI)
        if (state.isBookmarked) {
            _uiState.update { it.copy(isBookmarked = false) }

            viewModelScope.launch {
                when (repository.toggleReportBookmark(state.reportId, true)) {
                    is ApiResult.Success -> { /* 성공 시 유지 */ }
                    else -> _uiState.update { it.copy(isBookmarked = true) } // 실패 시 롤백
                }
            }
        }
    }
}