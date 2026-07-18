package com.talkqquest.app.feature.archive.viewmodel

import androidx.lifecycle.ViewModel
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

    // 💡 실시간으로 북마크를 누른 '시간'을 기억하는 맵 추가!
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

            // 💡 아이템별 고유 식별 키 생성 함수 (저장 시간 매칭용)
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
                results.addAll(allSentences.filter {
                    (query.isEmpty() || it.title.lowercase().contains(query)) && isDateInRange(it)
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

            // 💡 완벽하게 수정된 '저장한 순' 정렬
            when (sortType) {
                ArchiveSortType.LATEST -> results.sortByDescending { getItemDate(it) }
                ArchiveSortType.OLDEST -> results.sortBy { getItemDate(it) }
                ArchiveSortType.SAVED -> {
                    results.sortWith(
                        compareByDescending<Any> { if (isItemSaved(it)) 1 else 0 } // 1순위: 저장된 카드가 무조건 위로
                            .thenByDescending { savedTimestamps[getItemKey(it)] ?: 0L } // 2순위: 💡 방금 북마크를 눌러 저장된 시간이 큰 카드가 최상단으로!
                            .thenByDescending { getItemId(it) } // 3순위: 기존부터 저장되어 있던 카드들의 원래 순서
                    )
                }
            }

            return results
        }
}

@HiltViewModel
class ArchiveSearchViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(ArchiveSearchUiState())
    val uiState: StateFlow<ArchiveSearchUiState> = _uiState.asStateFlow()

    init {
        loadMockData()
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

    // ==================================================
    // 💡 북마크 토글 시 현재 시간(System.currentTimeMillis) 기록!
    // ==================================================
    fun toggleMissionBookmark(missionId: Long) {
        _uiState.update { state ->
            val isCurrentlySaved = state.allMissions.find { it.id == missionId }?.isSaved == true
            val updatedMissions = state.allMissions.map {
                if (it.id == missionId) it.copy(isSaved = !it.isSaved) else it
            }
            val key = "mission_$missionId"
            val updatedTimestamps = state.savedTimestamps.toMutableMap()
            if (!isCurrentlySaved) {
                updatedTimestamps[key] = System.currentTimeMillis() // 새롭게 저장할 때의 시간을 기록!
            }

            state.copy(allMissions = updatedMissions, savedTimestamps = updatedTimestamps)
        }
    }

    fun toggleSentenceBookmark(sentenceId: String) {
        _uiState.update { state ->
            val isCurrentlySaved = state.allSentences.find { it.id == sentenceId }?.isSaved == true
            val updatedSentences = state.allSentences.map {
                if (it.id == sentenceId) it.copy(isSaved = !it.isSaved) else it
            }
            val key = "bookmark_true_$sentenceId"
            val updatedTimestamps = state.savedTimestamps.toMutableMap()
            if (!isCurrentlySaved) {
                updatedTimestamps[key] = System.currentTimeMillis()
            }

            state.copy(allSentences = updatedSentences, savedTimestamps = updatedTimestamps)
        }
    }

    fun toggleReportBookmark(reportId: String) {
        _uiState.update { state ->
            val isCurrentlySaved = state.allReports.find { it.id == reportId }?.isSaved == true
            val updatedReports = state.allReports.map {
                if (it.id == reportId) it.copy(isSaved = !it.isSaved) else it
            }
            val key = "bookmark_false_$reportId"
            val updatedTimestamps = state.savedTimestamps.toMutableMap()
            if (!isCurrentlySaved) {
                updatedTimestamps[key] = System.currentTimeMillis()
            }

            state.copy(allReports = updatedReports, savedTimestamps = updatedTimestamps)
        }
    }

    private fun loadMockData() {
        _uiState.update { state ->
            state.copy(
                allMissions = listOf(
                    ArchiveMissionItem(1L, "처음 보는 사람에게 짧게 인사하기", "짧은 대화", "쉬움", 2, 20, isCompleted = true, isSaved = true, completedDate = "2026.07.16")
                ),
                allConversations = listOf(
                    RecentActivity(id = "1", title = "식당에서 메뉴 추천받고 주문하기", type = ActivityType.CONVERSATION, status = "대화 완료", date = "2026.07.15")
                ),
                allSentences = listOf(
                    BookmarkArchiveItem("1", "그렇군요! 저도 편해서 놀랐어요.", "문장 저장", "2026.06.25", isSaved = false)
                ),
                allReports = listOf(
                    BookmarkArchiveItem("2", "자기소개와 취미 공유하기", "리포트 열람", "2026.05.05", isSaved = true)
                )
            )
        }
    }
}