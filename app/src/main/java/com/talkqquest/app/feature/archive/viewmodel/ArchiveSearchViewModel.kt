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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

enum class ArchiveSortType(val title: String) {
    LATEST("최신순"),
    OLDEST("오래된 순"),
    SAVED("저장한 순")
}

data class SearchBookmarkWrapper(
    val item: BookmarkArchiveItem,
    val isSentence: Boolean
)

data class ArchiveSearchUiState(
    val searchQuery: String = "",
    val selectedDateTab: String? = "전체",
    val selectedCategoryTab: String? = "전체",
    val oldestCardDate: LocalDate = LocalDate.of(2025, 1, 1),
    val leftDate: LocalDate = LocalDate.of(2025, 1, 1),
    val rightDate: LocalDate = LocalDate.now(),
    val showResults: Boolean = false,

    val isDateChipVisible: Boolean = true,
    val isCategoryChipVisible: Boolean = true,

    val sortType: ArchiveSortType = ArchiveSortType.LATEST,

    val allMissions: List<ArchiveMissionItem> = emptyList(),
    val allConversations: List<RecentActivity> = emptyList(),
    val allSentences: List<BookmarkArchiveItem> = emptyList(),
    val allReports: List<BookmarkArchiveItem> = emptyList(),

    val savedTimestamps: Map<String, Long> = emptyMap()
) {
    val searchResults: List<Any>
        get() {
            val results = mutableListOf<Any>()
            val query = searchQuery.trim().lowercase()

            val showMission = selectedCategoryTab == "전체" || selectedCategoryTab == "미션"
            val showConversation = selectedCategoryTab == "전체" || selectedCategoryTab == "대화"
            val showSentence = selectedCategoryTab == "전체" || selectedCategoryTab == "문장"
            val showReport = selectedCategoryTab == "전체" || selectedCategoryTab == "리포트"

            val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")

            fun getItemDate(item: Any): LocalDate {
                return try {
                    when (item) {
                        is RecentActivity -> LocalDate.parse(item.date, formatter)
                        is SearchBookmarkWrapper -> LocalDate.parse(item.item.date, formatter)
                        is BookmarkArchiveItem -> LocalDate.parse(item.date, formatter)
                        is ArchiveMissionItem -> LocalDate.parse(item.completedDate, formatter)
                        else -> LocalDate.MIN
                    }
                } catch (e: Exception) {
                    LocalDate.MIN
                }
            }

            fun getItemId(item: Any): Long {
                return try {
                    when (item) {
                        is ArchiveMissionItem -> item.id
                        is RecentActivity -> item.id.toLongOrNull() ?: 0L
                        is SearchBookmarkWrapper -> item.item.id.toLongOrNull() ?: 0L
                        else -> 0L
                    }
                } catch (e: Exception) {
                    0L
                }
            }

            fun getItemKey(item: Any): String {
                return when (item) {
                    is ArchiveMissionItem -> "mission_${item.id}"
                    is SearchBookmarkWrapper -> "bookmark_${item.isSentence}_${item.item.id}"
                    else -> ""
                }
            }

            fun isDateInRange(item: Any): Boolean {
                val itemDate = getItemDate(item)
                if (itemDate == LocalDate.MIN) return true
                return !itemDate.isBefore(leftDate) && !itemDate.isAfter(rightDate)
            }

            if (showMission) {
                results.addAll(allMissions.filter {
                    (query.isEmpty() || it.title.lowercase().contains(query)) && isDateInRange(it)
                })
            }
            if (showConversation) {
                results.addAll(allConversations.filter {
                    (query.isEmpty() || it.title.lowercase().contains(query)) && isDateInRange(it)
                })
            }
            if (showSentence) {
                results.addAll(allSentences.filter { sentence ->
                    // 💡 [수정] 문장 내용 검사뿐만 아니라, 원본 대화의 제목도 함께 검사합니다.
                    val matchSentenceTitle = sentence.title.lowercase().contains(query)
                    val relatedConv = allConversations.find { it.id == sentence.relatedConversationId }
                    val matchConvTitle = relatedConv?.title?.lowercase()?.contains(query) == true

                    (query.isEmpty() || matchSentenceTitle || matchConvTitle) && isDateInRange(sentence)
                }.map { SearchBookmarkWrapper(it, isSentence = true) })
            }
            if (showReport) {
                results.addAll(allReports.filter {
                    (query.isEmpty() || it.title.lowercase().contains(query)) && isDateInRange(it)
                }.map { SearchBookmarkWrapper(it, isSentence = false) })
            }

            fun isItemSaved(item: Any): Boolean {
                return when (item) {
                    is ArchiveMissionItem -> item.isSaved
                    is SearchBookmarkWrapper -> item.item.isSaved
                    else -> false
                }
            }

            when (sortType) {
                ArchiveSortType.LATEST -> results.sortByDescending { getItemDate(it) }
                ArchiveSortType.OLDEST -> results.sortBy { getItemDate(it) }
                ArchiveSortType.SAVED -> {
                    results.sortWith(
                        compareByDescending<Any> { if (isItemSaved(it)) 1 else 0 }
                            .thenByDescending { savedTimestamps[getItemKey(it)] ?: 0L }
                            .thenByDescending { getItemId(it) }
                    )
                }
            }

            return results
        }
}

@HiltViewModel
class ArchiveSearchViewModel @Inject constructor(
    private val repository: ArchiveRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArchiveSearchUiState())
    val uiState: StateFlow<ArchiveSearchUiState> = _uiState.asStateFlow()

    init {
        refreshData()
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setSortType(type: ArchiveSortType) {
        _uiState.update { it.copy(sortType = type) }
    }

    fun selectDateTab(tab: String) {
        val right = LocalDate.now()
        val left = when (tab) {
            "7일" -> right.minusDays(7)
            "30일" -> right.minusDays(30)
            "3개월" -> right.minusMonths(3)
            else -> _uiState.value.oldestCardDate
        }
        _uiState.update { it.copy(selectedDateTab = tab, leftDate = left, rightDate = right) }
    }

    fun selectCustomDate(date: LocalDate, isStartDate: Boolean) {
        _uiState.update { state ->
            var newLeft = state.leftDate
            var newRight = state.rightDate

            if (isStartDate) {
                newLeft = date
                if (newLeft.isAfter(newRight)) newRight = newLeft.plusDays(1)
            } else {
                newRight = date
                if (newRight.isBefore(newLeft)) newLeft = newRight.minusDays(1)
            }
            state.copy(selectedDateTab = null, leftDate = newLeft, rightDate = newRight)
        }
    }

    fun selectCategoryTab(tab: String) {
        _uiState.update { it.copy(selectedCategoryTab = tab) }
    }

    fun resetFilters() {
        selectDateTab("전체")
        selectCategoryTab("전체")
        updateSearchQuery("")
        clearSearch()
        setSortType(ArchiveSortType.LATEST)
    }

    fun performSearch() {
        _uiState.update { it.copy(showResults = true, isDateChipVisible = true, isCategoryChipVisible = true) }
    }

    fun clearSearch() {
        _uiState.update { it.copy(showResults = false) }
    }

    fun clearDateFilter() {
        val right = LocalDate.now()
        val left = _uiState.value.oldestCardDate
        _uiState.update { it.copy(selectedDateTab = "전체", leftDate = left, rightDate = right, isDateChipVisible = false) }
    }

    fun clearCategoryFilter() {
        _uiState.update { it.copy(selectedCategoryTab = "전체", isCategoryChipVisible = false) }
    }

    fun toggleMissionBookmark(missionId: Long) {
        val isCurrentlySaved = _uiState.value.allMissions.find { it.id == missionId }?.isSaved == true

        repository.toggleMissionBookmark(missionId)
        refreshData()

        _uiState.update { state ->
            val key = "mission_$missionId"
            val updatedTimestamps = state.savedTimestamps.toMutableMap()
            if (!isCurrentlySaved) {
                updatedTimestamps[key] = System.currentTimeMillis()
            }
            state.copy(savedTimestamps = updatedTimestamps)
        }
    }

    fun toggleSentenceBookmark(sentenceId: String) {
        val isCurrentlySaved = _uiState.value.allSentences.find { it.id == sentenceId }?.isSaved == true

        repository.toggleSentenceBookmark(sentenceId)
        refreshData()

        _uiState.update { state ->
            val key = "bookmark_true_$sentenceId"
            val updatedTimestamps = state.savedTimestamps.toMutableMap()
            if (!isCurrentlySaved) {
                updatedTimestamps[key] = System.currentTimeMillis()
            }
            state.copy(savedTimestamps = updatedTimestamps)
        }
    }

    fun toggleReportBookmark(reportId: String) {
        val isCurrentlySaved = _uiState.value.allReports.find { it.id == reportId }?.isSaved == true

        repository.toggleReportBookmark(reportId)
        refreshData()

        _uiState.update { state ->
            val key = "bookmark_false_$reportId"
            val updatedTimestamps = state.savedTimestamps.toMutableMap()
            if (!isCurrentlySaved) {
                updatedTimestamps[key] = System.currentTimeMillis()
            }
            state.copy(savedTimestamps = updatedTimestamps)
        }
    }

    fun refreshData() {
        _uiState.update { state ->
            state.copy(
                allMissions = repository.getArchiveMissions(),
                allConversations = repository.getArchiveConversations(),
                allSentences = repository.getArchiveSentences(),
                allReports = repository.getArchiveReports()
            )
        }
    }
}