package com.talkqquest.app.feature.archive.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import com.talkqquest.app.R
import com.talkqquest.app.core.designsystem.FitDesign
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
import com.talkqquest.app.core.designsystem.component.ChipContentCrossfade
import com.talkqquest.app.core.designsystem.component.SlidingChipRow
import com.talkqquest.app.core.designsystem.component.rememberHapticTick
import com.talkqquest.app.core.designsystem.component.TextAnchoredPillRipple
import com.talkqquest.app.core.designsystem.component.rememberTextPillRippleBounds
import com.talkqquest.app.core.designsystem.component.rememberTextPillRippleGlyphBounds
import com.talkqquest.app.core.designsystem.component.rememberTextPillRippleGlyphBoundsUpdater
import com.talkqquest.app.core.designsystem.component.textPillRippleAnchor

import com.talkqquest.app.feature.archive.viewmodel.ActivityType
import com.talkqquest.app.feature.archive.viewmodel.ArchiveSearchUiState
import com.talkqquest.app.feature.archive.viewmodel.ArchiveSearchViewModel
import com.talkqquest.app.feature.archive.viewmodel.ArchiveSortType
import com.talkqquest.app.feature.archive.viewmodel.RecentActivity
import com.talkqquest.app.feature.archive.viewmodel.SearchBookmarkWrapper
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// 검색 결과 목록의 "칩 전환" 여부를 판단하는 키(정렬은 제외). 카테고리·기간 중 하나라도 바뀌면
// 새 키가 되어 ChipContentCrossfade가 페이드 스루를 재생한다.
private data class SearchResultFilterKey(
    val category: String?,
    val dateTab: String?,
    val leftDate: LocalDate,
    val rightDate: LocalDate,
)

// 정렬 전환(5-B)으로 카드가 자리를 옮길 때 쓰는 공통 이동 스펙.
private val searchResultItemPlacementSpec = tween<androidx.compose.ui.unit.IntOffset>(300, easing = FastOutSlowInEasing)

@Composable
fun ArchiveSearchScreen(
    onBackClick: () -> Unit = {},
    // 💡 [수정됨] isWeeklyCompare 파라미터 추가
    onNavigateToDetail: (String, ActivityType, Boolean) -> Unit = { _, _, _ -> },
    viewModel: ArchiveSearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshData()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

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
        onSortSelected = viewModel::setSortType,
        onNavigateToDetail = onNavigateToDetail
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
    onToggleMissionBookmark: (String) -> Unit,
    onToggleSentenceBookmark: (String) -> Unit,
    onToggleReportBookmark: (String) -> Unit,
    onSortSelected: (ArchiveSortType) -> Unit,
    // 💡 [수정됨]
    onNavigateToDetail: (String, ActivityType, Boolean) -> Unit
) {
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
    val focusManager = LocalFocusManager.current
    val tick = rememberHapticTick()

    var showBottomSheet by remember { mutableStateOf(false) }
    var isSelectingStartDate by remember { mutableStateOf(true) }
    var showSortSheet by remember { mutableStateOf(false) }

    val dateTabs = listOf("전체", "7일", "30일", "3개월")
    val categoryTabs = listOf("전체", "미션", "대화", "문장", "리포트")

    // 결과 화면에서 활성 필터 칩(x)을 지워 카테고리·기간이 바뀌면 목록 전체를 페이드 스루로
    // 교체한다(5-A). 정렬만 바뀌면 이 키가 그대로라 같은 목록이 유지되고, 카드는 animateItem으로
    // 자리만 옮긴다(5-B) — "정렬 오래된 순에서 이상함" 신고가 여기 해당.
    val searchFilterKey = SearchResultFilterKey(
        category = uiState.selectedCategoryTab,
        dateTab = uiState.selectedDateTab,
        leftDate = uiState.leftDate,
        rightDate = uiState.rightDate,
    )

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
                val headerBounds = rememberTextPillRippleBounds()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .height(44.dp)
                        .textPillRippleAnchor(headerBounds)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .align(Alignment.CenterStart)
                            .clip(CircleShape)
                            .clickable { tick(); onBackClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back_chevron),
                            contentDescription = "뒤로가기",
                            tint = Gray500
                        )
                    }

                    Text(
                        text = "검색 및 필터",
                        style = TqType.BodyL.copy(fontWeight = FontWeight.Medium).figma(),
                        color = Gray800,
                        maxLines = 1, softWrap = false, overflow = TextOverflow.Visible,
                        modifier = Modifier.align(Alignment.Center)
                    )

                    if (!uiState.showResults) {
                        val resetInteractionSource = remember { MutableInteractionSource() }
                        val resetTextStyle = TqType.BodyL.figma()
                        val resetTextBounds = rememberTextPillRippleBounds()
                        val resetGlyphBounds = rememberTextPillRippleGlyphBounds()
                        val resetOnTextLayout = rememberTextPillRippleGlyphBoundsUpdater(
                            resetGlyphBounds,
                            "초기화",
                            resetTextStyle,
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .offset(x = 332.dp)
                                .size(width = 42.dp, height = 44.dp)
                                .clip(CircleShape)
                                .clickable(
                                    interactionSource = resetInteractionSource,
                                    indication = null,
                                ) { onResetClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "초기화",
                                style = resetTextStyle,
                                color = Gray600,
                                onTextLayout = resetOnTextLayout,
                                modifier = Modifier.textPillRippleAnchor(resetTextBounds),
                            )
                        }

                        TextAnchoredPillRipple(
                            bounds = resetTextBounds.value,
                            glyphBounds = resetGlyphBounds.value,
                            parentPositionInRoot = headerBounds.value?.positionInRoot,
                            interactionSource = resetInteractionSource,
                            horizontalInset = 12.dp,
                            verticalInset = 10.dp,
                        )
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
                        // 카테고리·기간(칩) 조합이 바뀔 때만 페이드 스루(5-A). 정렬만 바뀌면 같은
                        // 목록이 유지되고 카드는 key 기반으로 animateItem이 자리만 옮긴다(5-B).
                        ChipContentCrossfade(
                            targetState = searchFilterKey,
                            modifier = Modifier.weight(1f),
                        ) { key ->
                            // 받은 key(카테고리·기간)로만 목록을 계산한다 — 바깥 uiState의 "지금"
                            // 필터를 읽으면 전환 중 겹쳐 있는 두 화면이 같은 목록을 그리게 된다.
                            // 정렬은 키에 없으므로 uiState.sortType(현재 값)을 그대로 적용해도 된다.
                            val displayResults = uiState.sortedResults(
                                uiState.filteredResults(key.category, key.leftDate, key.rightDate)
                            )
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 100.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                            ) {
                                items(
                                    items = displayResults,
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
                                            onClick = { onNavigateToDetail(item.id.toString(), ActivityType.MISSION, false) }, // 미션은 항상 false
                                            onToggleSave = { onToggleMissionBookmark(item.id) },
                                            modifier = Modifier.animateItem(placementSpec = searchResultItemPlacementSpec)
                                        )
                                        is RecentActivity -> {
                                            // 종류는 서버가 준 reportType으로 가른다(제목 문자열 추측 금지)
                                            val isWeeklyCompare = item.reportType == "weekly_compare"
                                            if (item.type == ActivityType.CONVERSATION) {
                                                ArchiveConversationCard(
                                                    title = item.title,
                                                    tags = item.tags,
                                                    summary = item.summary ?: "",
                                                    date = item.date,
                                                    time = item.duration,
                                                    onClick = { onNavigateToDetail(item.id, item.type, false) },
                                                    modifier = Modifier.animateItem(placementSpec = searchResultItemPlacementSpec)
                                                )
                                            } else {
                                                RecentActivityCard(
                                                    activity = item,
                                                    onClick = { onNavigateToDetail(item.id, item.type, isWeeklyCompare) },
                                                    modifier = Modifier.animateItem(placementSpec = searchResultItemPlacementSpec)
                                                )
                                            }
                                        }
                                        is SearchBookmarkWrapper -> {
                                            // 종류는 서버가 준 reportType으로 가른다(제목 문자열 추측 금지)
                                            val isWeeklyCompare = item.item.isWeeklyCompare
                                            BookmarkCard(
                                                item = item.item,
                                                isSentence = item.isSentence,
                                                onClick = {
                                                    val type = if (item.isSentence) ActivityType.SENTENCE else ActivityType.REPORT
                                                    onNavigateToDetail(item.item.id, type, isWeeklyCompare)
                                                },
                                                onToggleSave = {
                                                    if (item.isSentence) {
                                                        onToggleSentenceBookmark(item.item.id)
                                                    } else {
                                                        onToggleReportBookmark(item.item.id)
                                                    }
                                                },
                                                modifier = Modifier.animateItem(placementSpec = searchResultItemPlacementSpec)
                                            )
                                        }
                                    }
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

                                SlidingChipRow(
                                    options = dateTabs,
                                    selectedIndex = dateTabs.indexOf(uiState.selectedDateTab).takeIf { it >= 0 },
                                    onSelect = { index -> onDateTabSelected(dateTabs[index]) },
                                    scrollable = true,
                                )

                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    DateInputBox(
                                        dateText = if (uiState.selectedDateTab != null) "직접 선택하기" else uiState.leftDate.format(dateFormatter),
                                        isActive = uiState.selectedDateTab == null,
                                        modifier = Modifier.width(133.dp),
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

                                SlidingChipRow(
                                    options = categoryTabs,
                                    selectedIndex = categoryTabs.indexOf(uiState.selectedCategoryTab).takeIf { it >= 0 },
                                    onSelect = { index -> onCategoryTabSelected(categoryTabs[index]) },
                                    scrollable = true,
                                )
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
    val textColor = if (isActive) Primary600 else Gray400
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
        ArchiveMissionItem("1", "처음 보는 사람에게 짧게 인사하기", "짧은 대화", "쉬움", 2, 20, isCompleted = true, isSaved = true, completedDate = "2026.07.16")
    ),
    allConversations = listOf(
        RecentActivity(
            id = "1",
            title = "식당에서 메뉴 추천받고 주문하기",
            type = ActivityType.CONVERSATION,
            status = "대화 완료",
            date = "2026.07.15",
            tags = listOf("상황극", "주문"),
            summary = "직원에게 정중하게 메뉴를 묻고 주문하는 연습을 했습니다."
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
            title = "8월 2-3주차 주간 비교 리포트",
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
            onSortSelected = {}, onNavigateToDetail = { _, _, _ -> }
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
            onSortSelected = {}, onNavigateToDetail = { _, _, _ -> }
        )
    }
}
