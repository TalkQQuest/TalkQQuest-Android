package com.talkqquest.app.feature.archive.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import com.talkqquest.app.R
import com.talkqquest.app.core.designsystem.FitDesign
import com.talkqquest.app.core.designsystem.Gray1000
import com.talkqquest.app.core.designsystem.Gray300
import com.talkqquest.app.core.designsystem.Gray400
import com.talkqquest.app.core.designsystem.Gray50
import com.talkqquest.app.core.designsystem.Gray500
import com.talkqquest.app.core.designsystem.Gray600
import com.talkqquest.app.core.designsystem.Gray800
import com.talkqquest.app.core.designsystem.Gray900
import com.talkqquest.app.core.designsystem.Primary50
import com.talkqquest.app.core.designsystem.Primary600
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.White
import com.talkqquest.app.core.designsystem.softShadow

import com.talkqquest.app.feature.archive.viewmodel.ActivityType
import com.talkqquest.app.feature.archive.viewmodel.ArchiveSearchUiState
import com.talkqquest.app.feature.archive.viewmodel.ArchiveSearchViewModel
import com.talkqquest.app.feature.archive.viewmodel.ArchiveSortType
import com.talkqquest.app.feature.archive.viewmodel.RecentActivity
import com.talkqquest.app.feature.archive.viewmodel.SearchBookmarkWrapper
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun ArchiveSearchScreen(
    onBackClick: () -> Unit = {},
    viewModel: ArchiveSearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ArchiveSearchScreenContent(
        uiState = uiState,
        onBackClick = {
            if (uiState.showResults) {
                viewModel.clearSearch()
            } else {
                onBackClick()
            }
        },
        onResetClick = viewModel::resetFilters,
        onDateTabSelected = viewModel::selectDateTab,
        onCustomDateSelected = viewModel::selectCustomDate,
        onCategoryTabSelected = viewModel::selectCategoryTab,
        onSearchQueryChanged = viewModel::updateSearchQuery,
        onSearchTriggered = viewModel::performSearch,
        onClearSearch = viewModel::clearSearch,
        onClearDateFilter = viewModel::clearDateFilter,
        onClearCategoryFilter = viewModel::clearCategoryFilter,
        onToggleMissionBookmark = viewModel::toggleMissionBookmark,
        onToggleSentenceBookmark = viewModel::toggleSentenceBookmark,
        onToggleReportBookmark = viewModel::toggleReportBookmark,
        onSortSelected = viewModel::setSortType
    )
}

@Composable
private fun ArchiveSearchScreenContent(
    uiState: ArchiveSearchUiState,
    onBackClick: () -> Unit,
    onResetClick: () -> Unit,
    onDateTabSelected: (String) -> Unit,
    onCustomDateSelected: (LocalDate, Boolean) -> Unit,
    onCategoryTabSelected: (String) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onSearchTriggered: () -> Unit,
    onClearSearch: () -> Unit,
    onClearDateFilter: () -> Unit,
    onClearCategoryFilter: () -> Unit,
    onToggleMissionBookmark: (Long) -> Unit,
    onToggleSentenceBookmark: (String) -> Unit,
    onToggleReportBookmark: (String) -> Unit,
    onSortSelected: (ArchiveSortType) -> Unit
) {
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
    val focusManager = LocalFocusManager.current

    var showBottomSheet by remember { mutableStateOf(false) }
    var isSelectingStartDate by remember { mutableStateOf(true) }
    var showSortSheet by remember { mutableStateOf(false) }

    val dateTabs = listOf("전체", "7일", "30일", "3개월")
    val categoryTabs = listOf("전체", "미션", "대화", "문장", "리포트")

    FitDesign {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(Gray50)
                .statusBarsPadding()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // ==========================================
                // [상단 헤더 영역]
                // ==========================================
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .height(44.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .align(Alignment.CenterStart)
                            .clip(CircleShape)
                            .clickable { onBackClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "뒤로가기", tint = Gray500)
                    }

                    Text(
                        text = "검색 및 필터",
                        style = TqType.BodyL.copy(fontWeight = FontWeight.Medium).figma(),
                        color = Gray800,
                        maxLines = 1, softWrap = false, overflow = TextOverflow.Visible,
                        modifier = Modifier.align(Alignment.Center)
                    )

                    if (!uiState.showResults) {
                        Box(
                            modifier = Modifier
                                .padding(end = 10.dp)
                                .size(width = 44.dp, height = 44.dp)
                                .align(Alignment.CenterEnd)
                                .clip(CircleShape)
                                .clickable { onResetClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "초기화", style = TqType.BodyL.figma(), color = Gray600)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ==========================================
                // [본문 컨텐츠 영역]
                // ==========================================
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    // 1. 검색어 입력창
                    BasicTextField(
                        value = uiState.searchQuery,
                        onValueChange = onSearchQueryChanged,
                        textStyle = TqType.BodyL.copy(fontWeight = FontWeight.Medium, color = Gray900).figma(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            focusManager.clearFocus()
                            onSearchTriggered()
                        }),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .background(Gray50, RoundedCornerShape(12.dp))
                            .border(1.dp, Gray300, RoundedCornerShape(12.dp)),
                        decorationBox = { innerTextField ->
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(start = 16.dp, end = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    if (uiState.searchQuery.isEmpty()) {
                                        Text("검색어를 입력하세요", style = TqType.BodyL.copy(fontWeight = FontWeight.Medium).figma(), color = Gray400)
                                    }
                                    innerTextField()
                                }
                                Spacer(modifier = Modifier.width(8.dp))

                                Box(
                                    modifier = Modifier.size(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .clip(CircleShape)
                                            .clickable {
                                                focusManager.clearFocus()
                                                onSearchTriggered()
                                            }
                                    )
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_archive_search),
                                        contentDescription = "검색",
                                        tint = Gray400
                                    )
                                }
                            }
                        }
                    )

                    if (uiState.showResults) {
                        // ==========================================
                        // 🔎 [검색 결과 화면]
                        // ==========================================
                        Spacer(modifier = Modifier.height(16.dp))

                        val showDateChip = uiState.isDateChipVisible
                        val showCategoryChip = uiState.isCategoryChipVisible

                        if (showDateChip || showCategoryChip) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (showDateChip) {
                                    val dateString = when (uiState.selectedDateTab) {
                                        "전체" -> "기간 • 전체"
                                        null -> "${uiState.leftDate.format(dateFormatter)} ~ ${uiState.rightDate.format(dateFormatter)}"
                                        else -> uiState.selectedDateTab
                                    }
                                    ActiveFilterChip(text = dateString, onRemove = onClearDateFilter)
                                }

                                if (showCategoryChip) {
                                    val categoryString = if (uiState.selectedCategoryTab == "전체" || uiState.selectedCategoryTab == null) {
                                        "카테고리 • 전체"
                                    } else {
                                        uiState.selectedCategoryTab
                                    }
                                    ActiveFilterChip(text = categoryString, onRemove = onClearCategoryFilter)
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        } else {
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // 2. 검색 결과 개수 및 정렬 기준
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(24.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "검색 결과 ", style = TqType.BodyL.figma(), color = Gray800)
                                Text(text = "${uiState.searchResults.size}개", style = TqType.BodyL.copy(fontWeight = FontWeight.Medium).figma(), color = Gray800)
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { showSortSheet = true }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Icon(painter = painterResource(id = R.drawable.ic_archive_sort), contentDescription = null, tint = Gray500, modifier = Modifier.size(24.dp))
                                Text(text = uiState.sortType.title, style = TqType.BodyL.copy(fontWeight = FontWeight.Medium).figma(), color = Gray500)
                                Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = "정렬", tint = Gray500, modifier = Modifier.size(24.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 3. 필터링된 카드 리스트
                        LazyColumn(
                            contentPadding = PaddingValues(bottom = 100.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(
                                items = uiState.searchResults,
                                // 💡 고유 키 부여: 카드 타입이 섞여 있으므로 접두어를 붙여 완벽히 구분합니다.
                                key = { item ->
                                    when (item) {
                                        is ArchiveMissionItem -> "mission_${item.id}"
                                        is RecentActivity -> "activity_${item.id}"
                                        is SearchBookmarkWrapper -> "bookmark_${item.isSentence}_${item.item.id}"
                                        else -> item.hashCode()
                                    }
                                }
                            ) { item ->
                                when (item) {
                                    is ArchiveMissionItem -> ArchiveMissionCard(
                                        mission = item,
                                        onClick = {},
                                        onToggleSave = { onToggleMissionBookmark(item.id) },
                                        modifier = Modifier.animateItem() // 💡 애니메이션 적용!
                                    )
                                    is RecentActivity -> RecentActivityCard(
                                        activity = item,
                                        onClick = {},
                                        modifier = Modifier.animateItem() // 💡 애니메이션 적용!
                                    )
                                    is SearchBookmarkWrapper -> BookmarkCard(
                                        item = item.item,
                                        isSentence = item.isSentence,
                                        onClick = {},
                                        onToggleSave = {
                                            if (item.isSentence) {
                                                onToggleSentenceBookmark(item.item.id)
                                            } else {
                                                onToggleReportBookmark(item.item.id)
                                            }
                                        },
                                        modifier = Modifier.animateItem() // 💡 애니메이션 적용!
                                    )
                                }
                            }
                        }

                    } else {
                        // ==========================================
                        // 🛠 [기존 검색/필터 설정 화면]
                        // ==========================================
                        Spacer(modifier = Modifier.height(24.dp))

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Text(text = "기간", style = TqType.BodyL.copy(fontWeight = FontWeight.Medium).figma(), color = Gray900)

                                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    dateTabs.forEach { tab ->
                                        FilterChip(text = tab, isSelected = uiState.selectedDateTab == tab, onClick = { onDateTabSelected(tab) })
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    DateInputBox(
                                        dateText = uiState.leftDate.format(dateFormatter),
                                        isActive = uiState.selectedDateTab == null,
                                        modifier = Modifier.width(128.dp),
                                        onClick = { isSelectingStartDate = true; showBottomSheet = true }
                                    )
                                    Box(modifier = Modifier.width(9.dp), contentAlignment = Alignment.Center) {
                                        Text(text = "~", style = TqType.BodyS.figma(), color = if (uiState.selectedDateTab == null) Primary600 else Gray400)
                                    }
                                    DateInputBox(
                                        dateText = uiState.rightDate.format(dateFormatter),
                                        isActive = uiState.selectedDateTab == null,
                                        modifier = Modifier.width(130.dp),
                                        onClick = { isSelectingStartDate = false; showBottomSheet = true }
                                    )
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Text(text = "카테고리", style = TqType.BodyL.copy(fontWeight = FontWeight.Medium).figma(), color = Gray900)

                                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    categoryTabs.forEach { tab ->
                                        FilterChip(text = tab, isSelected = uiState.selectedCategoryTab == tab, onClick = { onCategoryTabSelected(tab) })
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        TqCalendarBottomSheet(
            isVisible = showBottomSheet,
            initialDate = if (isSelectingStartDate) uiState.leftDate else uiState.rightDate,
            onDateSelected = { selectedDate ->
                onCustomDateSelected(selectedDate, isSelectingStartDate)
                showBottomSheet = false
            },
            onDismissRequest = { showBottomSheet = false }
        )

        ArchiveSortSheet(
            isVisible = showSortSheet,
            currentSortType = uiState.sortType,
            onSortSelected = { type ->
                onSortSelected(type)
                showSortSheet = false
            },
            onDismissRequest = { showSortSheet = false }
        )
    }
}

// ==========================================
// [선택된 필터 칩 (검색 결과창 전용)]
// ==========================================
@Composable
private fun ActiveFilterChip(text: String, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .height(34.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Primary600)
            .border(1.dp, Primary600, RoundedCornerShape(20.dp))
            .padding(start = 18.dp, top = 4.dp, bottom = 4.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = text,
            style = TqType.BodyM.copy(fontWeight = FontWeight.Medium).figma(),
            color = Primary50
        )
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .clickable { onRemove() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "필터 해제",
                tint = Primary50,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// ==========================================
// [필터 칩 공통 컴포넌트 (선택창 전용)]
// ==========================================
@Composable
private fun FilterChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (isSelected) Primary600 else White
    val textColor = if (isSelected) White else Gray900
    val borderModifier = if (isSelected) Modifier.border(1.dp, Primary600, RoundedCornerShape(20.dp)) else Modifier

    Box(
        modifier = Modifier
            .height(34.dp)
            .then(if (!isSelected) Modifier.softShadow(Gray1000.copy(alpha = 0.01f), offsetY = 8.dp, blur = 24.dp, cornerRadius = 20.dp) else Modifier)
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .then(borderModifier)
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, style = TqType.LabelL.figma(), color = textColor)
    }
}

// ==========================================
// [날짜 선택 박스 공통 컴포넌트]
// ==========================================
@Composable
private fun DateInputBox(
    dateText: String,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val borderColor = if (isActive) Primary600 else Gray300
    val textColor = if (isActive) Primary600 else Gray500
    val iconColor = if (isActive) Primary600 else Gray400

    Row(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .background(White)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = dateText, style = TqType.BodyS.figma(), color = textColor)
        Icon(
            painter = painterResource(id = R.drawable.ic_archive_calendar),
            contentDescription = "날짜 선택",
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
    }
}

// ==========================================
// ── Previews (검색 화면 & 결과 화면 미리보기) ──
// ==========================================

private val previewUiState = ArchiveSearchUiState(
    searchQuery = "",
    selectedDateTab = "전체",
    selectedCategoryTab = "전체",
    showResults = true,
    isDateChipVisible = true,
    isCategoryChipVisible = true,
    sortType = ArchiveSortType.LATEST,
    allMissions = listOf(
        ArchiveMissionItem(1L, "처음 보는 사람에게 짧게 인사하기", "짧은 대화", "쉬움", 2, 20, isCompleted = true, isSaved = true, completedDate = "2026.07.16")
    ),
    allConversations = listOf(
        RecentActivity(
            id = "1",
            title = "식당에서 메뉴 추천받고 주문하기",
            type = ActivityType.CONVERSATION,
            status = "대화 완료",
            date = "2026.07.15"
        )
    ),
    allSentences = listOf(
        BookmarkArchiveItem(
            id = "1",
            title = "그렇군요! 저도 편해서 놀랐어요.",
            status = "문장 저장",
            date = "2026.06.25",
            isSaved = false
        )
    ),
    allReports = listOf(
        BookmarkArchiveItem(
            id = "2",
            title = "자기소개와 취미 공유하기",
            status = "리포트 열람",
            date = "2026.05.05",
            isSaved = true
        )
    )
)

@Preview(name = "1. 검색 필터 설정", showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun ArchiveSearchFilterPreview() {
    TalkQQuestTheme {
        ArchiveSearchScreenContent(
            uiState = previewUiState.copy(showResults = false),
            onBackClick = {}, onResetClick = {}, onDateTabSelected = {},
            onCustomDateSelected = { _, _ -> }, onCategoryTabSelected = {},
            onSearchQueryChanged = {}, onSearchTriggered = {}, onClearSearch = {},
            onClearDateFilter = {}, onClearCategoryFilter = {},
            onToggleMissionBookmark = {}, onToggleSentenceBookmark = {}, onToggleReportBookmark = {},
            onSortSelected = {}
        )
    }
}

@Preview(name = "2. 검색 결과 리스트", showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun ArchiveSearchResultsPreview() {
    TalkQQuestTheme {
        ArchiveSearchScreenContent(
            uiState = previewUiState.copy(showResults = true),
            onBackClick = {}, onResetClick = {}, onDateTabSelected = {},
            onCustomDateSelected = { _, _ -> }, onCategoryTabSelected = {},
            onSearchQueryChanged = {}, onSearchTriggered = {}, onClearSearch = {},
            onClearDateFilter = {}, onClearCategoryFilter = {},
            onToggleMissionBookmark = {}, onToggleSentenceBookmark = {}, onToggleReportBookmark = {},
            onSortSelected = {}
        )
    }
}