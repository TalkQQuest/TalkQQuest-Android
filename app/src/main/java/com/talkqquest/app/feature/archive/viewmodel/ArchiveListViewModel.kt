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
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
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
        val target = _uiState.value.reports.find { it.id == id } ?: return

        if (target.isSaved) {
            _uiState.update { state ->
                state.copy(reports = state.reports.filter { it.id != id })
            }

            viewModelScope.launch {
                when (val result = repository.toggleReportBookmark(id, true)) {
                    is ApiResult.Success -> {
                        // 정상 삭제됨, 유지
                    }
                    else -> {
                        android.util.Log.e("ArchiveTest", "리포트 해제 실패")
                        refreshData() // 실패 시에만 원상 복구
                    }
                }
            }
        }
    }

    private fun formatIsoDate(isoString: String): String {
        return try {
            val zdt = ZonedDateTime.parse(isoString)
            zdt.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
        } catch (e: Exception) {
            isoString.substringBefore("T").replace("-", ".")
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            when (val result = repository.searchArchives()) {
                is ApiResult.Success -> {
                    val items = result.data.items

                    val missions = items.filter { it.type.lowercase() == "mission" && it.isBookmarked }.map {
                        ArchiveMissionItem(
                            id = it.missionId ?: it.id,
                            title = it.title,
                            category = it.category ?: "",
                            difficulty = it.difficulty ?: "",
                            duration = it.estimatedMinutes ?: 0,
                            xp = it.rewardXp ?: 0,
                            isCompleted = it.missionStatus == "completed",
                            isSaved = it.isBookmarked,
                            completedDate = formatIsoDate(it.createdAt)
                        )
                    }

                    // 💡 변경됨: 대화, 문장, 리포트 모두 원본 ID(`referenceId`)를 우선 매핑
                    val conversations = items.filter { it.type.lowercase() == "conversation" }.map {
                        RecentActivity(
                            id = it.referenceId ?: it.id,
                            type = ActivityType.CONVERSATION,
                            title = it.title,
                            status = "대화 완료",
                            date = formatIsoDate(it.createdAt)
                        )
                    }
                    val sentences = items.filter { (it.type.lowercase() == "phrase" || it.type.lowercase() == "sentence") && it.isBookmarked }.map {
                        BookmarkArchiveItem(
                            id = it.referenceId ?: it.id,
                            title = it.title,
                            status = "문장 저장",
                            date = formatIsoDate(it.createdAt),
                            isSaved = it.isBookmarked,
                            memoKeywords = it.tags,
                            memoText = "",
                            relatedConversationId = ""
                        )
                    }
                    val reports = items.filter { it.type.lowercase() == "report" && it.isBookmarked }.map {
                        BookmarkArchiveItem(
                            id = it.referenceId ?: it.id,
                            title = it.title,
                            status = "리포트 열람",
                            date = formatIsoDate(it.createdAt),
                            isSaved = it.isBookmarked,
                            memoKeywords = it.tags,
                            memoText = "",
                            relatedConversationId = ""
                        )
                    }

                    _uiState.update { state -> state.copy(missions = missions, conversations = conversations, sentences = sentences, reports = reports) }
                }
                is ApiResult.Error -> { }
                is ApiResult.Exception -> { }
            }
        }
    }
}