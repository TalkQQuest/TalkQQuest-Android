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

            when (val result = repository.getArchiveReportDetail(reportId)) {
                is ApiResult.Success -> {
                    val (title, growth, weekly) = result.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            title = title,
                            isBookmarked = true,
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

    // 💡 변경됨: 상세 페이지에서도 오직 "해제"만 서버 통신을 수행하도록 강제
    fun toggleBookmark() {
        val id = _uiState.value.reportId
        if (id.isEmpty()) return

        val isCurrentlySaved = _uiState.value.isBookmarked

        if (isCurrentlySaved) {
            // 낙관적 UI 반영: 북마크 끄기
            _uiState.update { it.copy(isBookmarked = false) }

            viewModelScope.launch {
                when (val result = repository.toggleReportBookmark(id, true)) {
                    is ApiResult.Success -> {
                        // 성공 시 아무것도 안 함 (UI 유지)
                    }
                    else -> {
                        // 실패 시 롤백
                        android.util.Log.e("ArchiveTest", "리포트 해제 에러")
                        _uiState.update { it.copy(isBookmarked = true) }
                    }
                }
            }
        } else {
            // 이미 해제된 상태에서 하트를 다시 눌렀을 때는 서버 POST를 치지 않음
            // (보관함에서는 삭제된 리포트의 재발급을 금지)
        }
    }
}