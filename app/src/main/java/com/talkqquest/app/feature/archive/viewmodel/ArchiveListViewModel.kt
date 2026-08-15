package com.talkqquest.app.feature.archive.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talkqquest.app.core.network.ApiResult
import com.talkqquest.app.feature.archive.data.ArchiveRepository
import com.talkqquest.app.feature.archive.ui.ArchiveMissionItem
import com.talkqquest.app.feature.archive.ui.BookmarkArchiveItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class ArchiveUiState(
    val selectedCategory: String = "전체",
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

    fun selectCategory(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
        // 💡 탭 이동 시마다 API를 다시 부르지 않고, 필터만 바뀔 때 갱신하도록 분리
    }

    fun selectFilter(filter: String) {
        if (_uiState.value.selectedFilter == filter) return
        _uiState.update { it.copy(selectedFilter = filter) }
        refreshData() // 필터(완료/미완료 등)가 바뀔 때만 데이터를 새로 고침
    }

    fun toggleMissionSave(id: String) {
        val targetMission = _uiState.value.missions.find { it.id == id } ?: return
        val isCurrentlySaved = targetMission.isSaved

        _uiState.update { state ->
            state.copy(missions = state.missions.map { if (it.id == id) it.copy(isSaved = !isCurrentlySaved) else it })
        }

        viewModelScope.launch {
            when (val result = repository.toggleMissionBookmark(id, isCurrentlySaved)) {
                is ApiResult.Success -> refreshData()
                is ApiResult.Error -> refreshData()
                is ApiResult.Exception -> refreshData()
            }
        }
    }

    fun toggleSentenceSave(id: String) {
        val target = _uiState.value.sentences.find { it.id == id } ?: return
        val isCurrentlySaved = target.isSaved

        _uiState.update { state ->
            state.copy(sentences = state.sentences.map { if (it.id == id) it.copy(isSaved = !isCurrentlySaved) else it })
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
                is ApiResult.Error -> refreshData()
                is ApiResult.Exception -> refreshData()
            }
        }
    }

    fun toggleReportSave(id: String) {
        val target = _uiState.value.reports.find { it.id == id } ?: return

        if (target.isSaved) {
            _uiState.update { state -> state.copy(reports = state.reports.filter { it.id != id }) }

            viewModelScope.launch {
                when (val result = repository.toggleReportBookmark(id, true)) {
                    is ApiResult.Success -> {}
                    else -> refreshData()
                }
            }
        }
    }

    private fun formatIsoDate(isoString: String): String {
        return try {
            val zdt = ZonedDateTime.parse(isoString)
            zdt.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
        } catch (e: Exception) { isoString.substringBefore("T").replace("-", ".") }
    }

    fun refreshData() {
        val currentFilter = _uiState.value.selectedFilter

        // 미션 필터 조건
        val apiMissionFilter = when (currentFilter) {
            "완료" -> "completed"
            "미완료" -> "incomplete"
            else -> "all"
        }

        viewModelScope.launch {
            try {
                coroutineScope {
                    // 💡 1. 각 카테고리별로 API를 병렬(async)로 호출하여 최대 100개씩 가져옵니다.
                    val missionsDef = async { repository.searchArchives(type = "mission", missionFilter = apiMissionFilter, size = 100) }
                    val convosDef = async { repository.searchArchives(type = "conversation", size = 100) }
                    val phrasesDef = async { repository.searchArchives(type = "phrase", size = 100) }
                    val reportsDef = async { repository.searchArchives(type = "report", size = 100) }

                    val missionsRes = missionsDef.await()
                    val convosRes = convosDef.await()
                    val phrasesRes = phrasesDef.await()
                    val reportsRes = reportsDef.await()

                    // 💡 2. 결과를 파싱합니다. 각 탭의 데이터가 다른 탭을 덮어쓰지 않고 독립적으로 유지됩니다.
                    val missions = if (missionsRes is ApiResult.Success) {
                        missionsRes.data.items.filter { it.isBookmarked }.map {
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
                    } else _uiState.value.missions

                    val conversations = if (convosRes is ApiResult.Success) {
                        convosRes.data.items.map {
                            RecentActivity(
                                id = it.referenceId ?: it.id,
                                type = ActivityType.CONVERSATION,
                                title = it.title,
                                status = "대화 완료",
                                date = formatIsoDate(it.createdAt),
                                duration = it.duration ?: "00:00",
                                tags = it.tags,
                                summary = it.description
                            )
                        }
                    } else _uiState.value.conversations

                    val sentences = if (phrasesRes is ApiResult.Success) {
                        phrasesRes.data.items.filter { it.isBookmarked }.map {
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
                    } else _uiState.value.sentences

                    val reports = if (reportsRes is ApiResult.Success) {
                        reportsRes.data.items.filter { it.isBookmarked }.map {
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
                    } else _uiState.value.reports

                    // 💡 3. 조합된 전체 데이터를 UI 상태에 묶어서 업데이트합니다.
                    _uiState.update { state ->
                        state.copy(
                            missions = missions,
                            conversations = conversations,
                            sentences = sentences,
                            reports = reports
                        )
                    }
                }
            } catch (e: Exception) {
                // 에러 발생 시 처리
            }
        }
    }
}
