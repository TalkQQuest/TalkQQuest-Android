package com.talkqquest.app.feature.archive.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import com.talkqquest.app.R
import com.talkqquest.app.core.designsystem.component.pagerAxisLock
import com.talkqquest.app.core.designsystem.component.rememberPagerAxisLockState
import com.talkqquest.app.core.designsystem.FitDesign
import com.talkqquest.app.core.designsystem.component.rememberHapticTick
import com.talkqquest.app.core.designsystem.Gray300
import com.talkqquest.app.core.designsystem.Gray400
import com.talkqquest.app.core.designsystem.Gray50
import com.talkqquest.app.core.designsystem.Gray500
import com.talkqquest.app.core.designsystem.Gray800
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.component.ChipContentCrossfade
import com.talkqquest.app.core.designsystem.component.SlidingChipRow
import com.talkqquest.app.core.designsystem.component.TextAnchoredPillRipple
import com.talkqquest.app.core.designsystem.component.rememberDebouncedChipSelection
import com.talkqquest.app.core.designsystem.component.rememberTextPillRippleBounds
import com.talkqquest.app.core.designsystem.component.rememberTextPillRippleGlyphBounds
import com.talkqquest.app.core.designsystem.component.rememberTextPillRippleGlyphBoundsUpdater
import com.talkqquest.app.core.designsystem.component.rememberTextPillRippleParentPosition
import com.talkqquest.app.core.designsystem.component.textPillRippleAnchor
import com.talkqquest.app.core.designsystem.component.textPillRippleParentPosition

import com.talkqquest.app.feature.archive.viewmodel.ArchiveUiState
import com.talkqquest.app.feature.archive.viewmodel.ArchiveViewModel
import com.talkqquest.app.feature.archive.viewmodel.ActivityType
import com.talkqquest.app.feature.archive.viewmodel.RecentActivity
import com.talkqquest.app.feature.mission.ui.figma
import com.talkqquest.app.navigation.NavigationMotion

import kotlinx.coroutines.launch
import kotlin.math.floor

// 정렬·목록 개수 변화로 카드가 자리를 옮길 때 쓰는 공통 이동 스펙 (5-B).
private val archiveListItemPlacementSpec = tween<androidx.compose.ui.unit.IntOffset>(300, easing = FastOutSlowInEasing)

// 빈 목록 안내 문구 — 4개 탭이 같은 스타일을 쓴다.
@Composable
private fun ArchiveListEmptyMessage(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 100.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = TqType.BodyL.copy(fontWeight = FontWeight.Medium).figma(),
            color = Gray500
        )
    }
}

@Composable
fun ArchiveListScreen(
    initialTabIndex: Int = 0,
    viewModel: ArchiveViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onMissionClick: (String) -> Unit = {},
    onConversationClick: (String) -> Unit = {},
    onSentenceClick: (String) -> Unit = {},
    // 💡 수정됨: Boolean 파라미터(isWeeklyCompare) 추가
    onReportClick: (String, Boolean) -> Unit = { _, _ -> }
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

    ArchiveListScreenContent(
        initialTabIndex = initialTabIndex,
        uiState = uiState,
        onBackClick = onBackClick,
        onFilterSelect = viewModel::selectFilter,
        onMissionClick = onMissionClick,
        onToggleMissionSave = viewModel::toggleMissionSave,
        onConversationClick = onConversationClick,
        onSentenceClick = onSentenceClick,
        onToggleSentenceSave = viewModel::toggleSentenceSave,
        onReportClick = onReportClick,
        onToggleReportSave = viewModel::toggleReportSave
    )
}

@Composable
private fun ArchiveListScreenContent(
    initialTabIndex: Int,
    uiState: ArchiveUiState,
    onBackClick: () -> Unit,
    onFilterSelect: (String) -> Unit,
    onMissionClick: (String) -> Unit,
    onToggleMissionSave: (String) -> Unit,
    onConversationClick: (String) -> Unit,
    onSentenceClick: (String) -> Unit,
    onToggleSentenceSave: (String) -> Unit,
    // 💡 수정됨: Boolean 파라미터(isWeeklyCompare) 추가
    onReportClick: (String, Boolean) -> Unit,
    onToggleReportSave: (String) -> Unit
) {
    val tick = rememberHapticTick()
    val tabs = listOf("미션", "대화", "문장", "리포트")
    val pagerState = rememberPagerState(initialPage = initialTabIndex, pageCount = { tabs.size })
    val axisLock = rememberPagerAxisLockState()
    val coroutineScope = rememberCoroutineScope()
    val missionFilterScrollState = rememberScrollState()
    val pagePosition = (pagerState.currentPage + pagerState.currentPageOffsetFraction)
        .coerceIn(0f, (tabs.lastIndex).toFloat())
    val filterRegionHeights = listOf(61.dp, 16.dp, 16.dp, 48.dp)
    val lowerPage = floor(pagePosition).toInt()
    val upperPage = (lowerPage + 1).coerceAtMost(tabs.lastIndex)
    val pageProgress = pagePosition - lowerPage
    val filterRegionHeight = filterRegionHeights[lowerPage] +
        (filterRegionHeights[upperPage] - filterRegionHeights[lowerPage]) * pageProgress
    val missionFilterProgress = pagePosition.coerceIn(0f, 1f)
    val missionFilterTranslationY = 27.dp - (61.dp * missionFilterProgress)
    val missionFilterAlpha = 1f - missionFilterProgress
    val reportFilterProgress = (pagePosition - 2f).coerceIn(0f, 1f)
    val reportFilterTranslationY = (-32).dp + (48.dp * reportFilterProgress)
    val reportFilterAlpha = reportFilterProgress
    val isMissionFilterInteractive = pagerState.settledPage == 0 && !pagerState.isScrollInProgress
    val isReportFilterInteractive = pagerState.settledPage == 3 && !pagerState.isScrollInProgress

    var showReportFilterSheet by remember { mutableStateOf(false) }

    LaunchedEffect(pagerState.settledPage) {
        if (uiState.selectedFilter != "전체") {
            onFilterSelect("전체")
        }
    }

    // 칩 강조는 누르는 즉시, 실제 목록 교체(페이드 스루)는 100ms 디바운스 — 연타해도 마지막 선택만 반영.
    // 미션 탭·리포트 탭이 같은 selectedFilter 필드를 공유하므로(탭 전환 시 "전체"로 리셋) 두 훅 다
    // 같은 uiState.selectedFilter를 current로 받아 외부 리셋에도 따라간다.
    val missionFilterSelection = rememberDebouncedChipSelection(
        current = uiState.selectedFilter,
        onCommit = onFilterSelect,
    )
    val reportFilterSelection = rememberDebouncedChipSelection(
        current = uiState.selectedFilter,
        onCommit = onFilterSelect,
    )

    FitDesign {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Gray50)
                    .statusBarsPadding()
            ) {
                // [1] 상단 헤더
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
                            .clickable(onClick = { tick(); onBackClick() }),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back_chevron),
                            contentDescription = "뒤로가기",
                            tint = Gray500
                        )
                    }
                    Text("보관함", style = TqType.BodyL.copy(fontWeight = FontWeight.Medium).figma(), color = Gray800, modifier = Modifier.align(Alignment.Center))
                }

                // [2] 카테고리 탭
                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .zIndex(1f)) {
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Gray300)
                        .align(Alignment.BottomCenter))
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)) {
                        tabs.forEachIndexed { index, tab ->
                            val isActive = (pagerState.currentPage == index)
                            val interactionSource = remember { MutableInteractionSource() }
                            val textBounds = rememberTextPillRippleBounds()
                            val glyphBounds = rememberTextPillRippleGlyphBounds()
                            val tabTextStyle = TqType.TitleL.figma()
                            val onTextLayout = rememberTextPillRippleGlyphBoundsUpdater(
                                glyphBounds,
                                tab,
                                tabTextStyle,
                            )
                            val parentPosition = rememberTextPillRippleParentPosition()
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable(
                                        interactionSource = interactionSource,
                                        indication = null,
                                    ) {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(
                                                index,
                                                animationSpec = NavigationMotion.floatSpec,
                                            )
                                        }
                                    }
                                    .textPillRippleParentPosition(parentPosition),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                TextAnchoredPillRipple(textBounds.value, glyphBounds.value, parentPosition.value, interactionSource, horizontalInset = 12.dp, verticalInset = 10.dp)
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        tab,
                                        style = tabTextStyle,
                                        color = if (isActive) Gray800 else Gray400,
                                        onTextLayout = onTextLayout,
                                        modifier = Modifier
                                            .height(28.dp)
                                            .textPillRippleAnchor(textBounds),
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                }
                                if (isActive) Box(
                                    Modifier
                                        .align(Alignment.BottomCenter)
                                        .requiredWidth(44.dp)
                                        .height(3.dp)
                                        .background(Gray800, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)))
                            }
                        }
                    }
                }

                // [3] 필터 영역
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(filterRegionHeight)
                        .clipToBounds()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopStart)
                            .then(
                                if (isMissionFilterInteractive) {
                                    Modifier.horizontalScroll(missionFilterScrollState)
                                } else {
                                    Modifier
                                }
                            )
                            .padding(horizontal = 15.dp)
                            .graphicsLayer {
                                translationY = missionFilterTranslationY.toPx()
                                alpha = missionFilterAlpha
                            },
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val missionFilterOptions = listOf("전체", "완료", "미완료")
                        SlidingChipRow(
                            options = missionFilterOptions,
                            selectedIndex = missionFilterOptions.indexOf(missionFilterSelection.displayed).takeIf { it >= 0 },
                            onSelect = { index -> missionFilterSelection.select(missionFilterOptions[index]) },
                            interactionEnabled = isMissionFilterInteractive,
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopStart)
                            .padding(horizontal = 22.dp)
                            .graphicsLayer {
                                translationY = reportFilterTranslationY.toPx()
                                alpha = reportFilterAlpha
                            },
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(percent = 50))
                                .clickable(enabled = isReportFilterInteractive) {
                                    showReportFilterSheet = true
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = reportFilterSelection.displayed,
                                style = TqType.BodyL.copy(fontWeight = FontWeight.Medium).figma(),
                                color = Gray500
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "필터 선택",
                                tint = Gray500,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // [4] Pager 리스트
                // 축 잠금: 목록을 위아래로 밀 때 손가락이 살짝 대각선으로 흘러도 탭이 넘어가지
                // 않게 한다. 하단 네비 탭과 같은 장치를 쓴다(core의 pagerAxisLock).
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .weight(1f)
                        .pagerAxisLock(axisLock),
                    userScrollEnabled = !axisLock.isVerticalDrag,
                    flingBehavior = PagerDefaults.flingBehavior(
                        state = pagerState,
                        snapAnimationSpec = NavigationMotion.pagerSnapSpec,
                    ),
                ) { page ->
                    when (page) {
                        0 -> { // 미션 탭 — 칩(전체/완료/미완료) 전환은 목록 전체를 페이드 스루로 교체
                            ChipContentCrossfade(targetState = uiState.selectedFilter) { selectedFilter ->
                                // 받은 selectedFilter로만 걸러 그린다(리포트 탭과 같은 형태) — 바깥
                                // uiState.selectedFilter를 읽으면 전환 중 두 화면이 같은 목록을 그린다.
                                val displayMissions = when (selectedFilter) {
                                    "완료" -> uiState.missions.filter { it.isCompleted }
                                    "미완료" -> uiState.missions.filter { !it.isCompleted }
                                    else -> uiState.missions
                                }
                                if (displayMissions.isEmpty()) {
                                    ArchiveListEmptyMessage("저장한 미션이 없어요")
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp, start = 16.dp, end = 16.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        items(displayMissions, key = { it.id }) { mission ->
                                            ArchiveMissionCard(
                                                mission = mission,
                                                onClick = { onMissionClick(mission.id) },
                                                onToggleSave = { onToggleMissionSave(mission.id) },
                                                modifier = Modifier.animateItem(placementSpec = archiveListItemPlacementSpec)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        1 -> { // 대화 탭 (칩·정렬 없음)
                            if (uiState.conversations.isEmpty()) {
                                ArchiveListEmptyMessage("진행한 대화가 없어요")
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp, start = 16.dp, end = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    items(uiState.conversations, key = { it.id }) { conversation ->
                                        ArchiveConversationCard(
                                            title = conversation.title,
                                            tags = conversation.tags,
                                            summary = conversation.summary ?: "",
                                            date = conversation.date,
                                            time = conversation.duration,
                                            onClick = { onConversationClick(conversation.id) },
                                            modifier = Modifier.animateItem(placementSpec = archiveListItemPlacementSpec)
                                        )
                                    }
                                }
                            }
                        }
                        2 -> { // 문장 탭 (칩·정렬 없음)
                            if (uiState.sentences.isEmpty()) {
                                ArchiveListEmptyMessage("저장한 문장이 없어요")
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp, start = 16.dp, end = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    items(uiState.sentences, key = { it.id }) { sentence ->
                                        BookmarkCard(
                                            item = sentence,
                                            isSentence = true,
                                            onClick = { onSentenceClick(sentence.id) },
                                            onToggleSave = { onToggleSentenceSave(sentence.id) },
                                            modifier = Modifier.animateItem(placementSpec = archiveListItemPlacementSpec)
                                        )
                                    }
                                }
                            }
                        }
                        3 -> { // 리포트 탭 — 필터(전체/성장/주간 비교) 전환은 목록 전체를 페이드 스루로 교체
                            ChipContentCrossfade(targetState = uiState.selectedFilter) { selectedFilter ->
                                val displayReports = when (selectedFilter) {
                                    // 종류는 서버가 준 reportType으로 가른다(제목 문자열 추측 금지 — 서버 제목이
                                    // "4주차 비교 리포트"라 "주간 비교"로는 안 걸려 주간 비교가 0건으로 나왔다)
                                    "성장 리포트" -> uiState.reports.filter { !it.isWeeklyCompare }
                                    "주간 비교 리포트" -> uiState.reports.filter { it.isWeeklyCompare }
                                    else -> uiState.reports
                                }
                                if (displayReports.isEmpty()) {
                                    ArchiveListEmptyMessage("저장한 리포트가 없어요")
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp, start = 16.dp, end = 16.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        items(displayReports, key = { it.id }) { report ->
                                            val isWeeklyCompare = report.isWeeklyCompare
                                            val reportTypeLabel = if (isWeeklyCompare) "주간 비교 리포트" else "성장 리포트"
                                            val displayItem = report.copy(status = reportTypeLabel)

                                            BookmarkCard(
                                                item = displayItem,
                                                isSentence = false,
                                                // 💡 수정됨: report.id 와 isWeeklyCompare를 함께 전달
                                                onClick = { onReportClick(report.id, isWeeklyCompare) },
                                                onToggleSave = { onToggleReportSave(report.id) },
                                                modifier = Modifier.animateItem(placementSpec = archiveListItemPlacementSpec)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            ArchiveReportBottomSheet(
                isVisible = showReportFilterSheet,
                currentFilter = uiState.selectedFilter,
                onFilterSelected = { filter ->
                    onFilterSelect(filter)
                    showReportFilterSheet = false
                },
                onDismissRequest = { showReportFilterSheet = false }
            )
        }
    }
}

// ==========================================
// ── Previews (더미 데이터를 통한 화면 확인) ──
// ==========================================
private val previewUiState = ArchiveUiState(
    selectedFilter = "전체",
    missions = listOf(
        ArchiveMissionItem("1", "처음 보는 사람에게 짧게 인사하기", "짧은 대화", "쉬움", 2, 20, isCompleted = true, isSaved = true),
        ArchiveMissionItem("2", "최근 본 영화 이야기하기", "짧은 대화", "쉬움", 5, 20, isCompleted = false, isSaved = true)
    ),
    conversations = listOf(
        RecentActivity(id = "1", title = "처음 보는 사람에게 짧게 인사하기", type = ActivityType.CONVERSATION, status = "대화 완료", date = "2026.08.20", tags = listOf("자기 성장", "첫 만남"), summary = "간단한 인사와 자기소개를 나누며 첫 만남의 어색함을 줄이고 대화를 시작했어요.")
    ),
    sentences = listOf(
        BookmarkArchiveItem("1", "그렇군요! 저도 편해서 놀랐어요.", "문장 저장", "2026.08.20")
    ),
    reports = listOf(
        BookmarkArchiveItem("1", "처음 보는 사람에게 짧게 인사하기", "리포트 열람", "2026.08.20"),
        BookmarkArchiveItem("2", "8월 2-3주차 주간 비교 리포트", "리포트 열람", "2026.08.20")
    )
)

@Preview(name = "1. 보관함 리스트 [미션]", showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun ArchiveListMissionPreview() {
    TalkQQuestTheme {
        ArchiveListScreenContent(
            initialTabIndex = 0, uiState = previewUiState, onBackClick = {}, onFilterSelect = {}, onMissionClick = {}, onToggleMissionSave = {}, onConversationClick = {}, onSentenceClick = {}, onToggleSentenceSave = {}, onReportClick = { _, _ -> }, onToggleReportSave = {}
        )
    }
}

@Preview(name = "4. 보관함 리스트 [리포트]", showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun ArchiveListReportPreview() {
    TalkQQuestTheme {
        ArchiveListScreenContent(
            initialTabIndex = 3, uiState = previewUiState, onBackClick = {}, onFilterSelect = {}, onMissionClick = {}, onToggleMissionSave = {}, onConversationClick = {}, onSentenceClick = {}, onToggleSentenceSave = {}, onReportClick = { _, _ -> }, onToggleReportSave = {}
        )
    }
}
