package com.talkqquest.app.feature.archive.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talkqquest.app.core.network.ApiResult
import com.talkqquest.app.feature.archive.data.ArchiveRepository
import com.talkqquest.app.feature.archive.data.model.ArchiveSearchItem
import com.talkqquest.app.feature.archive.ui.ArchiveMissionItem
import com.talkqquest.app.feature.archive.ui.BookmarkArchiveItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

enum class ArchiveSortType(val title: String) { LATEST("최신순"), OLDEST("오래된 순"), SAVED("저장한 순") }

data class SearchBookmarkWrapper(val item: BookmarkArchiveItem, val isSentence: Boolean)

data class ArchiveSearchUiState(
    val searchQuery: String = "", val selectedDateTab: String? = "전체", val selectedCategoryTab: String? = "전체",
    val oldestCardDate: LocalDate = LocalDate.of(2025, 1, 1), val leftDate: LocalDate = LocalDate.of(2025, 1, 1), val rightDate: LocalDate = LocalDate.now(),
    val showResults: Boolean = false, val isDateChipVisible: Boolean = true, val isCategoryChipVisible: Boolean = true,
    val sortType: ArchiveSortType = ArchiveSortType.LATEST,
    val allMissions: List<ArchiveMissionItem> = emptyList(), val allConversations: List<RecentActivity> = emptyList(),
    val allSentences: List<BookmarkArchiveItem> = emptyList(), val allReports: List<BookmarkArchiveItem> = emptyList(),
    val savedTimestamps: Map<String, Long> = emptyMap()
) {
    // 카테고리·기간(칩) → 목록. 정렬은 여기 들어가지 않는다 — ChipContentCrossfade의 필터 키와
    // 짝을 맞추기 위한 순수 함수라, 인자로 받은 category/leftDate/rightDate로만 값을 정한다
    // (this.selectedCategoryTab 등 "지금" 상태를 읽지 않는다). 크로스페이드 진행 중엔 나가는 화면과
    // 들어오는 화면이 동시에 살아있는데, 그 둘이 각자 받은 키로 이 함수를 불러야 서로 다른 목록을
    // 그리게 되어 카드가 섞이지 않는다.
    fun filteredResults(category: String?, leftDate: LocalDate, rightDate: LocalDate): List<Any> {
        val results = mutableListOf<Any>()

        // 💡 더 이상 여기서 로컬 텍스트 필터링(contains(query))을 하지 않습니다!
        // 이미 서버에서 keyword에 맞는 데이터만 정확하게 1차 필터링해서 내려주었기 때문입니다.
        // 여기서는 탭(카테고리), 날짜(Date)만 UI 상태에 맞게 2차로 처리합니다.

        val showMission = category == "전체" || category == "미션"
        val showConversation = category == "전체" || category == "대화"
        val showSentence = category == "전체" || category == "문장"
        val showReport = category == "전체" || category == "리포트"

        fun isDateInRange(item: Any): Boolean {
            val itemDate = getItemDisplayDate(item)
            if (itemDate == LocalDate.MIN) return true
            return !itemDate.isBefore(leftDate) && !itemDate.isAfter(rightDate)
        }

        if (showMission) results.addAll(allMissions.filter { isDateInRange(it) })
        if (showConversation) results.addAll(allConversations.filter { isDateInRange(it) })
        if (showSentence) results.addAll(allSentences.filter { isDateInRange(it) }.map { SearchBookmarkWrapper(it, isSentence = true) })
        if (showReport) results.addAll(allReports.filter { isDateInRange(it) }.map { SearchBookmarkWrapper(it, isSentence = false) })
        return results
    }

    // 필터링된 목록에 현재 정렬 기준을 적용한다. 정렬은 크로스페이드 키에 안 들어가므로 이 함수는
    // (필터 키가 아니라) sortType이 바뀔 때만 순서를 바꾼다 — 같은 LazyColumn 안에서 카드가
    // animateItem으로 자리만 옮기게 하기 위함.
    fun sortedResults(items: List<Any>): List<Any> {
        val sorted = items.toMutableList()
        when (sortType) {
            // 최신순/오래된 순은 원본 시각(시각 포함)으로 비교하고, 동률이면 문자열 id로 예비 비교한다.
            ArchiveSortType.LATEST -> sorted.sortWith(
                compareByDescending<Any> { getItemSortInstant(it) }.thenBy { getItemStringId(it) }
            )
            ArchiveSortType.OLDEST -> sorted.sortWith(
                compareBy<Any> { getItemSortInstant(it) }.thenBy { getItemStringId(it) }
            )
            ArchiveSortType.SAVED -> sorted.sortWith(
                compareByDescending<Any> { if (isItemSaved(it)) 1 else 0 }
                    .thenByDescending { savedTimestamps[getItemKey(it)] ?: 0L }
                    .thenByDescending { getItemSortInstant(it) }
                    .thenBy { getItemStringId(it) }
            )
        }
        return sorted
    }

    val searchResults: List<Any>
        get() = sortedResults(filteredResults(selectedCategoryTab, leftDate, rightDate))
}

private val archiveDisplayDateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")

// 화면 표시용 날짜(yyyy.MM.dd, 시각 없음) — 기간 필터(leftDate~rightDate)는 지금도 이 값을 기준으로 한다.
private fun getItemDisplayDate(item: Any): LocalDate = try {
    when (item) {
        is RecentActivity -> LocalDate.parse(item.date, archiveDisplayDateFormatter)
        is SearchBookmarkWrapper -> LocalDate.parse(item.item.date, archiveDisplayDateFormatter)
        is BookmarkArchiveItem -> LocalDate.parse(item.date, archiveDisplayDateFormatter)
        is ArchiveMissionItem -> LocalDate.parse(item.completedDate, archiveDisplayDateFormatter)
        else -> LocalDate.MIN
    }
} catch (e: Exception) { LocalDate.MIN }

// 정렬 전용 원본 시각(createdAtRaw, 시각 포함 ISO). 화면 표시 날짜(getItemDisplayDate)는 시각이
// 잘려 있어 같은 날 항목이 전부 동률이 되던 문제 때문에 별도로 둔 값이다. 파싱 실패·빈 값이면
// Instant.MIN으로 취급해 항상 맨 뒤로 밀린다(예외로 죽지 않게).
private fun getItemSortInstant(item: Any): java.time.Instant = try {
    val raw = when (item) {
        is ArchiveMissionItem -> item.createdAtRaw
        is RecentActivity -> item.createdAtRaw
        is SearchBookmarkWrapper -> item.item.createdAtRaw
        else -> ""
    }
    ZonedDateTime.parse(raw).toInstant()
} catch (e: Exception) { java.time.Instant.MIN }

// 동률일 때의 예비 정렬 기준. 서버 id가 "3eef1b6e-78ca-453f-..." 형태의 문자열이라
// toLongOrNull()은 항상 null이 되어 예비 기준이 없는 것과 같았다 — 문자열 그대로 비교한다.
private fun getItemStringId(item: Any): String = when (item) {
    is ArchiveMissionItem -> item.id
    is RecentActivity -> item.id
    is SearchBookmarkWrapper -> item.item.id
    else -> ""
}

private fun getItemKey(item: Any): String = when (item) {
    is ArchiveMissionItem -> "mission_${item.id}"
    is SearchBookmarkWrapper -> "bookmark_${item.isSentence}_${item.item.id}"
    else -> ""
}

private fun isItemSaved(item: Any): Boolean = when (item) {
    is ArchiveMissionItem -> item.isSaved
    is SearchBookmarkWrapper -> item.item.isSaved
    else -> false
}

@HiltViewModel
class ArchiveSearchViewModel @Inject constructor(
    private val repository: ArchiveRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArchiveSearchUiState())
    val uiState: StateFlow<ArchiveSearchUiState> = _uiState.asStateFlow()

    init { refreshData() } // 초기 화면 진입 시 전체 데이터 로드용

    fun updateSearchQuery(query: String) { _uiState.update { it.copy(searchQuery = query) } }
    fun setSortType(type: ArchiveSortType) { _uiState.update { it.copy(sortType = type) } }
    fun selectDateTab(tab: String) {
        val right = LocalDate.now()
        val left = when (tab) { "7일" -> right.minusDays(7); "30일" -> right.minusDays(30); "3개월" -> right.minusMonths(3); else -> _uiState.value.oldestCardDate }
        _uiState.update { it.copy(selectedDateTab = tab, leftDate = left, rightDate = right) }
    }
    fun selectCustomDate(date: LocalDate, isStartDate: Boolean) {
        _uiState.update { state ->
            var newLeft = state.leftDate; var newRight = state.rightDate
            if (isStartDate) { newLeft = date; if (newLeft.isAfter(newRight)) newRight = newLeft.plusDays(1) }
            else { newRight = date; if (newRight.isBefore(newLeft)) newLeft = newRight.minusDays(1) }
            state.copy(selectedDateTab = null, leftDate = newLeft, rightDate = newRight)
        }
    }
    fun selectCategoryTab(tab: String) { _uiState.update { it.copy(selectedCategoryTab = tab) } }
    fun resetFilters() { selectDateTab("전체"); selectCategoryTab("전체"); updateSearchQuery(""); clearSearch(); setSortType(ArchiveSortType.LATEST) }

    // 💡 검색 버튼을 눌렀을 때 실행되는 함수
    fun performSearch() {
        _uiState.update {
            it.copy(
                showResults = true,
                isDateChipVisible = true,
                isCategoryChipVisible = true,
                // 검색 서버 통신 전, 기존 리스트를 비워주어 이전 결과가 깜빡이는 것을 방지합니다.
                allMissions = emptyList(),
                allConversations = emptyList(),
                allSentences = emptyList(),
                allReports = emptyList()
            )
        }
        // 💡 돋보기를 누르면 현재 입력된 검색어(searchQuery)를 서버에 보내 백엔드 로직을 태웁니다!
        refreshData(_uiState.value.searchQuery)
    }

    fun clearSearch() { _uiState.update { it.copy(showResults = false) } }
    fun clearDateFilter() {
        val right = LocalDate.now()
        val left = _uiState.value.oldestCardDate
        _uiState.update { it.copy(selectedDateTab = "전체", leftDate = left, rightDate = right, isDateChipVisible = false) }
    }
    fun clearCategoryFilter() { _uiState.update { it.copy(selectedCategoryTab = "전체", isCategoryChipVisible = false) } }

    fun toggleMissionBookmark(missionId: String) {
        val target = _uiState.value.allMissions.find { it.id == missionId } ?: return
        val isCurrentlySaved = target.isSaved
        _uiState.update { state -> state.copy(allMissions = state.allMissions.map { if (it.id == missionId) it.copy(isSaved = !isCurrentlySaved) else it }) }
        viewModelScope.launch { repository.toggleMissionBookmark(missionId, isCurrentlySaved).also { refreshData(_uiState.value.searchQuery) } }
    }

    fun toggleSentenceBookmark(sentenceId: String) {
        val target = _uiState.value.allSentences.find { it.id == sentenceId } ?: return
        val isCurrentlySaved = target.isSaved
        viewModelScope.launch { repository.toggleSentenceBookmark(id = sentenceId, isCurrentlySaved = isCurrentlySaved, conversationId = target.relatedConversationId, content = target.title, memo = target.memoText).also { refreshData(_uiState.value.searchQuery) } }
    }

    fun toggleReportBookmark(reportId: String) {
        val target = _uiState.value.allReports.find { it.id == reportId } ?: return
        if (target.isSaved) {
            _uiState.update { state -> state.copy(allReports = state.allReports.filter { it.id != reportId }) }
            viewModelScope.launch { repository.toggleReportBookmark(reportId, true).also { refreshData(_uiState.value.searchQuery) } }
        }
    }

    private fun formatIsoDate(isoString: String): String {
        return try {
            val zdt = ZonedDateTime.parse(isoString)
            zdt.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
        } catch (e: Exception) { isoString.substringBefore("T").replace("-", ".") }
    }

    // 💡 검색어 파라미터를 추가로 받을 수 있도록 개선되었습니다.
    fun refreshData(searchKeyword: String? = null) {
        viewModelScope.launch {
            val allFetchedItems = mutableListOf<ArchiveSearchItem>()
            var currentPage = 1
            var totalPages = 1
            var hasErrorOnFirstPage = false

            // 검색어가 비어있지 않으면 param으로 세팅
            val keywordParam = if (searchKeyword.isNullOrBlank()) null else searchKeyword.trim()

            while (currentPage <= totalPages) {
                // 💡 repository에 keyword 파라미터를 정상적으로 넘겨줍니다.
                when (val result = repository.searchArchives(keyword = keywordParam, page = currentPage, size = 50)) {
                    is ApiResult.Success -> {
                        allFetchedItems.addAll(result.data.items)
                        totalPages = result.data.pageInfo?.totalPages ?: 1
                        currentPage++
                    }
                    else -> {
                        if (currentPage == 1) hasErrorOnFirstPage = true
                        break
                    }
                }
            }

            if (!hasErrorOnFirstPage) {
                val allMissions = allFetchedItems.filter { it.type.lowercase() == "mission" }.map {
                    ArchiveMissionItem(id = it.missionId ?: it.id, title = it.title, category = it.category ?: "", difficulty = it.difficulty ?: "", duration = it.estimatedMinutes ?: 0, xp = it.rewardXp ?: 0, isCompleted = it.missionStatus == "completed", isSaved = it.isBookmarked, completedDate = formatIsoDate(it.createdAt), createdAtRaw = it.createdAt)
                }

                val allConversations = allFetchedItems.filter { it.type.lowercase() == "conversation" }.map {
                    RecentActivity(
                        id = it.referenceId ?: it.id,
                        type = ActivityType.CONVERSATION,
                        title = it.title,
                        status = "대화 완료",
                        date = formatIsoDate(it.createdAt),
                        duration = it.duration ?: "",
                        tags = it.tags,
                        summary = it.description,
                        createdAtRaw = it.createdAt
                    )
                }

                val allSentences = allFetchedItems.filter { it.type.lowercase() == "phrase" || it.type.lowercase() == "sentence" }.map {
                    BookmarkArchiveItem(id = it.referenceId ?: it.id, title = it.title, status = "문장 저장", date = formatIsoDate(it.createdAt), isSaved = it.isBookmarked, memoKeywords = it.tags, memoText = "", relatedConversationId = "", createdAtRaw = it.createdAt)
                }

                val allReports = allFetchedItems.filter { it.type.lowercase() == "report" }.let { items ->
                    val mapped = items.map {
                        // 종류는 서버 reportType을 그대로 쓴다. 예전엔 제목에 "주간 비교"가 들어있는지로
                        // 추측했는데 서버 제목이 "4주차 비교 리포트"라 전부 성장 리포트로 찍혔다.
                        val reportType = it.reportType.orEmpty()
                        BookmarkArchiveItem(
                            id = it.referenceId ?: it.id,
                            title = it.title,
                            status = if (reportType == "weekly_compare") "주간 비교 리포트" else "성장 리포트",
                            date = formatIsoDate(it.createdAt),
                            isSaved = it.isBookmarked,
                            memoKeywords = it.tags,
                            memoText = "",
                            relatedConversationId = "",
                            reportType = reportType,
                            createdAtRaw = it.createdAt
                        )
                    }
                    // 주간 비교 항목만 제목을 주차 범위로 바꿔 끼운다(상세를 한 번씩 더 부름).
                    repository.withWeeklyCompareTitles(mapped)
                }

                _uiState.update { state ->
                    state.copy(
                        allMissions = allMissions,
                        allConversations = allConversations,
                        allSentences = allSentences,
                        allReports = allReports
                    )
                }
            }
        }
    }
}