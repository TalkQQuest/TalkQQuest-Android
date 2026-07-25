package com.talkqquest.app.feature.archive.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talkqquest.app.core.network.ApiResult
import com.talkqquest.app.feature.archive.data.ArchiveRepository
import com.talkqquest.app.feature.archive.ui.ArchiveMissionItem
import com.talkqquest.app.feature.archive.ui.BookmarkArchiveItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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

    init { refreshData() }

    fun selectFilter(filter: String) { _uiState.update { it.copy(selectedFilter = filter) } }

    fun toggleMissionSave(id: String) {
        val targetMission = _uiState.value.missions.find { it.id == id } ?: return
        val isCurrentlySaved = targetMission.isSaved

        _uiState.update { state ->
            state.copy(
                missions = state.missions.map {
                    if (it.id == id) it.copy(isSaved = !isCurrentlySaved) else it
                }
            )
        }

        viewModelScope.launch {
            when (val result = repository.toggleMissionBookmark(id, isCurrentlySaved)) {
                is ApiResult.Success -> refreshData()
                is ApiResult.Error -> {
                    android.util.Log.e("ArchiveTest", "API 에러: ${result.message}")
                    refreshData()
                }
                is ApiResult.Exception -> {
                    android.util.Log.e("ArchiveTest", "통신 예외 (주소 틀림 등)")
                    refreshData()
                }
            }
        }
    }

    fun toggleSentenceSave(id: String) {
        val target = _uiState.value.sentences.find { it.id == id } ?: return
        val isCurrentlySaved = target.isSaved

        // 즉각적인 UI 선반영 (Optimistic UI)
        _uiState.update { state ->
            state.copy(
                sentences = state.sentences.map {
                    if (it.id == id) it.copy(isSaved = !isCurrentlySaved) else it
                }
            )
        }

        viewModelScope.launch {
            when (val result = repository.toggleSentenceBookmark(
                id = id,
                isCurrentlySaved = isCurrentlySaved,
                conversationId = target.relatedConversationId,
                content = target.title,
                memo = target.memoText
            )) {
                is ApiResult.Success -> refreshData()
                is ApiResult.Error -> {
                    android.util.Log.e("ArchiveTest", "문장 저장 API 에러: ${result.message}")
                    refreshData()
                }
                is ApiResult.Exception -> {
                    android.util.Log.e("ArchiveTest", "문장 저장 통신 예외 (주소 틀림 등)")
                    refreshData()
                }
            }
        }
    }

    fun toggleReportSave(id: String) {
        repository.toggleReportBookmark(id)
        refreshData()
    }

    fun refreshData() {
        viewModelScope.launch {
            // 필요 시 page와 size를 인자로 넘겨줄 수 있습니다.
            when (val result = repository.searchArchives()) {
                is ApiResult.Success -> {
                    val items = result.data.items

                    // 🔍 디버그: 서버가 실제로 내려주는 mission 타입 아이템 전체를 확인
                    android.util.Log.d(
                        "ArchiveDebug",
                        "mission 타입 전체 개수=${items.count { it.type.lowercase() == "mission" }} / " +
                                items.filter { it.type.lowercase() == "mission" }
                                    .joinToString("\n") { "  id=${it.id}, missionId=${it.missionId}, missionRecordId=${it.missionRecordId}, isBookmarked=${it.isBookmarked}, title=${it.title}" }
                    )

                    val missions = items.filter { it.type.lowercase() == "mission" && it.isBookmarked }.map {
                        ArchiveMissionItem(
                            id = it.missionId ?: it.id, // 💡 핵심: 저장/취소 API를 위해 missionId 최우선 사용!
                            title = it.title,
                            category = it.category ?: "",
                            difficulty = it.difficulty ?: "",
                            duration = it.estimatedMinutes ?: 0,
                            xp = it.rewardXp ?: 0,
                            isCompleted = it.missionStatus == "completed",
                            isSaved = it.isBookmarked,
                            completedDate = it.createdAt
                        )
                    }
                    val conversations = items.filter { it.type.lowercase() == "conversation" }.map {
                        RecentActivity(id = it.id, type = ActivityType.CONVERSATION, title = it.title, status = "대화 완료", date = it.createdAt)
                    }
                    val sentences = items.filter { (it.type.lowercase() == "phrase" || it.type.lowercase() == "sentence") && it.isBookmarked }.map {
                        BookmarkArchiveItem(id = it.id, title = it.title, status = "문장 저장", date = it.createdAt, isSaved = it.isBookmarked, memoKeywords = it.tags, memoText = "", relatedConversationId = "")
                    }
                    val reports = items.filter { it.type.lowercase() == "report" && it.isBookmarked }.map {
                        BookmarkArchiveItem(id = it.id, title = it.title, status = "리포트 열람", date = it.createdAt, isSaved = it.isBookmarked, memoKeywords = it.tags, memoText = "", relatedConversationId = "")
                    }

                    _uiState.update { state -> state.copy(missions = missions, conversations = conversations, sentences = sentences, reports = reports) }
                }
                is ApiResult.Error -> { }
                is ApiResult.Exception -> { }
            }
        }
    }
}