package com.talkqquest.app.feature.archive.viewmodel

import androidx.lifecycle.ViewModel
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
            "완료" -> missions.filter { it.isCompleted } // 실제 속성명에 맞게 변경
            "미완료" -> missions.filter { !it.isCompleted } // 실제 속성명에 맞게 변경
            else -> missions
        }
}

@HiltViewModel
class ArchiveViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(ArchiveUiState())
    val uiState: StateFlow<ArchiveUiState> = _uiState.asStateFlow()

    init {
        loadMockData() // 초기 더미 데이터 로드
    }

    // 필터 변경
    fun selectFilter(filter: String) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    // 미션 북마크 해제 (해제 시 리스트에서 제거)
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

    // ── 임시 더미 데이터 셋업 ──
    private fun loadMockData() {
        _uiState.update { state ->
            state.copy(
                missions = listOf(
                    ArchiveMissionItem(1, "처음 보는 사람에게 짧게 인사하기", "짧은 대화", "쉬움", 2, 20, isCompleted = true, isSaved = true),
                    ArchiveMissionItem(2, "최근 본 영화 이야기하기", "짧은 대화", "쉬움", 5, 20, isCompleted = false, isSaved = true),
                    ArchiveMissionItem(3, "학교 생활 꿀팁 나누기", "일상 대화", "보통", 8, 30, isCompleted = true, isSaved = true)
                ),
                conversations = listOf(
                    RecentActivity(id = "1", title = "처음 보는 사람에게 짧게 인사하기", type = ActivityType.CONVERSATION, status = "대화 완료", date = "2026.08.20"),
                    RecentActivity(id = "2", title = "최근 본 영화 이야기하기", type = ActivityType.CONVERSATION, status = "대화 완료", date = "2026.08.20"),
                    RecentActivity(id = "3", title = "학교 생활 꿀팁 나누기", type = ActivityType.CONVERSATION, status = "대화 완료", date = "2026.08.20")
                ),
                sentences = listOf(
                    BookmarkArchiveItem("1", "그렇군요! 저도 편해서 놀랐어요.", "문장 저장", "2026.08.20"),
                    BookmarkArchiveItem("2", "그 말씀 들으니 저도 기분이 좋아지네요", "문장 저장", "2026.08.19"),
                    BookmarkArchiveItem("3", "혹시 그때 어떤 기분이셨어요?", "문장 저장", "2026.08.18")
                ),
                reports = listOf(
                    BookmarkArchiveItem("1", "처음 보는 사람에게 짧게 인사하기", "리포트 열람", "2026.08.20"),
                    BookmarkArchiveItem("2", "최근 본 영화 이야기하기", "리포트 열람", "2026.08.19"),
                    BookmarkArchiveItem("3", "학교 생활 꿀팁 나누기", "리포트 열람", "2026.08.18")
                )
            )
        }
    }
}