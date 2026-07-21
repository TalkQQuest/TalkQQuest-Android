package com.talkqquest.app.feature.archive.viewmodel

import androidx.lifecycle.ViewModel
import com.talkqquest.app.feature.archive.data.ArchiveRepository
import com.talkqquest.app.feature.archive.ui.ArchiveMissionItem
import com.talkqquest.app.feature.archive.ui.BookmarkArchiveItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class ArchiveUiState(
    val selectedFilter: String = "전체",
    val missions: List<ArchiveMissionItem> = emptyList(),
    val conversations: List<RecentActivity> = emptyList(),
    val sentences: List<BookmarkArchiveItem> = emptyList(),
    val reports: List<BookmarkArchiveItem> = emptyList()
) {
    val filteredMissions: List<ArchiveMissionItem>
        get() = when (selectedFilter) {
            "완료" -> missions.filter { it.isCompleted }
            "미완료" -> missions.filter { !it.isCompleted }
            else -> missions
        }
}

@HiltViewModel
class ArchiveViewModel @Inject constructor(
    private val repository: ArchiveRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArchiveUiState())
    val uiState: StateFlow<ArchiveUiState> = _uiState.asStateFlow()

    init {
        refreshData()
    }

    fun selectFilter(filter: String) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    // 💡 Long -> String으로 타입 변경
    fun toggleMissionSave(id: String) {
        repository.toggleMissionBookmark(id)
        refreshData()
    }

    fun toggleSentenceSave(id: String) {
        repository.toggleSentenceBookmark(id)
        refreshData()
    }

    fun toggleReportSave(id: String) {
        repository.toggleReportBookmark(id)
        refreshData()
    }

    fun refreshData() {
        _uiState.update { state ->
            state.copy(
                missions = repository.getArchiveMissions().filter { it.isSaved },
                conversations = repository.getArchiveConversations(),
                sentences = repository.getArchiveSentences().filter { it.isSaved },
                reports = repository.getArchiveReports().filter { it.isSaved }
            )
        }
    }
}