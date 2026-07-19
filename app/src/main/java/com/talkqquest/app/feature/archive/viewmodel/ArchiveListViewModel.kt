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

// 💡 화면이 가질 모든 상태를 하나의 Data Class로 묶어 관리합니다.
data class ArchiveUiState(
    val selectedFilter: String = "전체",
    val missions: List<ArchiveMissionItem> = emptyList(),
    val conversations: List<RecentActivity> = emptyList(),
    val sentences: List<BookmarkArchiveItem> = emptyList(),
    val reports: List<BookmarkArchiveItem> = emptyList()
) {
    // 💡 ViewModel 내에서 필터링된 결과만 바로 빼서 쓸 수 있도록 계산 속성(Computed Property)을 둡니다.
    val filteredMissions: List<ArchiveMissionItem>
        get() = when (selectedFilter) {
            "완료" -> missions.filter { it.isCompleted }
            "미완료" -> missions.filter { !it.isCompleted }
            else -> missions
        }
}

@HiltViewModel
class ArchiveViewModel @Inject constructor(
    private val repository: ArchiveRepository // 💡 새로 만든 Repository를 주입받습니다!
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArchiveUiState())
    val uiState: StateFlow<ArchiveUiState> = _uiState.asStateFlow()

    init {
        loadMockData() // 초기 통합 데이터 로드
    }

    // 필터 변경
    fun selectFilter(filter: String) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    // 미션 북마크 해제 (보관함 화면이므로 해제 시 리스트에서 즉시 제거됩니다)
    fun toggleMissionSave(id: Long) {
        _uiState.update { state ->
            val updatedMissions = state.missions.map {
                if (it.id == id) it.copy(isSaved = !it.isSaved) else it
            }.filter { it.isSaved }
            state.copy(missions = updatedMissions)
        }
    }

    // 문장 북마크 해제
    fun toggleSentenceSave(id: String) {
        _uiState.update { state ->
            val updatedSentences = state.sentences.map {
                if (it.id == id) it.copy(isSaved = !it.isSaved) else it
            }.filter { it.isSaved }
            state.copy(sentences = updatedSentences)
        }
    }

    // 리포트 북마크 해제
    fun toggleReportSave(id: String) {
        _uiState.update { state ->
            val updatedReports = state.reports.map {
                if (it.id == id) it.copy(isSaved = !it.isSaved) else it
            }.filter { it.isSaved }
            state.copy(reports = updatedReports)
        }
    }

    // ── Repository 연동으로 단일 진실 공급원(SSOT) 바라보기 ──
    private fun loadMockData() {
        _uiState.update { state ->
            state.copy(
                // 💡 하드코딩된 더미를 지우고 Repository에서 통합된 원본 데이터를 가져옵니다.
                // 보관함 목록이므로 '저장된(isSaved = true)' 아이템만 명시적으로 필터링해서 넣습니다.
                missions = repository.getArchiveMissions().filter { it.isSaved },
                conversations = repository.getArchiveConversations(), // 대화는 자동저장이므로 전체 로드
                sentences = repository.getArchiveSentences().filter { it.isSaved },
                reports = repository.getArchiveReports().filter { it.isSaved }
            )
        }
    }
}