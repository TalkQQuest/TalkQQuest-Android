package com.talkqquest.app.feature.archive.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import com.talkqquest.app.R
import com.talkqquest.app.core.designsystem.FitDesign
import com.talkqquest.app.core.designsystem.Gray1000
import com.talkqquest.app.core.designsystem.Gray300
import com.talkqquest.app.core.designsystem.Gray400
import com.talkqquest.app.core.designsystem.Gray50
import com.talkqquest.app.core.designsystem.Gray500
import com.talkqquest.app.core.designsystem.Gray800
import com.talkqquest.app.core.designsystem.Gray900
import com.talkqquest.app.core.designsystem.Primary50
import com.talkqquest.app.core.designsystem.Primary600
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.White
import com.talkqquest.app.core.designsystem.softShadow

import com.talkqquest.app.feature.archive.viewmodel.ArchiveUiState
import com.talkqquest.app.feature.archive.viewmodel.ArchiveViewModel
import com.talkqquest.app.feature.archive.viewmodel.ActivityType
import com.talkqquest.app.feature.archive.viewmodel.RecentActivity
import com.talkqquest.app.feature.mission.ui.figma

import kotlinx.coroutines.launch

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
    val tabs = listOf("미션", "대화", "문장", "리포트")
    val pagerState = rememberPagerState(initialPage = initialTabIndex, pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()

    var showReportFilterSheet by remember { mutableStateOf(false) }

    LaunchedEffect(pagerState.currentPage) {
        onFilterSelect("전체")
    }

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
                            .clickable(onClick = onBackClick),
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
                    .height(38.dp)) {
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
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        coroutineScope.launch { pagerState.animateScrollToPage(index) }
                                    },
                                contentAlignment = Alignment.TopCenter
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(tab, style = TqType.TitleL.figma(), color = if (isActive) Gray800 else Gray400, modifier = Modifier.height(28.dp))
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
                when (pagerState.currentPage) {
                    0 -> {
                        Spacer(modifier = Modifier.height(27.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 15.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("전체", "완료", "미완료").forEach { filter ->
                                FilterChip(
                                    text = filter,
                                    isSelected = uiState.selectedFilter == filter,
                                    onClick = { onFilterSelect(filter) }
                                )
                            }
                        }
                    }
                    3 -> {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 22.dp),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable { showReportFilterSheet = true }
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = uiState.selectedFilter,
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
                    else -> {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                val displayReports = when (uiState.selectedFilter) {
                    "성장 리포트" -> uiState.reports.filter { !it.title.contains("주간 비교") }
                    "주간 비교 리포트" -> uiState.reports.filter { it.title.contains("주간 비교") }
                    else -> uiState.reports
                }

                // [4] Pager 리스트
                HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
                    val isListEmpty = when (page) {
                        0 -> uiState.filteredMissions.isEmpty()
                        1 -> uiState.conversations.isEmpty()
                        2 -> uiState.sentences.isEmpty()
                        3 -> displayReports.isEmpty()
                        else -> true
                    }

                    if (isListEmpty) {
                        val emptyMessage = when (page) {
                            0 -> "저장한 미션이 없어요"
                            1 -> "진행한 대화가 없어요"
                            2 -> "저장한 문장이 없어요"
                            3 -> "저장한 리포트가 없어요"
                            else -> "저장된 항목이 없어요"
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = emptyMessage,
                                style = TqType.BodyL.copy(fontWeight = FontWeight.Medium).figma(),
                                color = Gray500
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp, start = 16.dp, end = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            when (page) {
                                0 -> { // 미션 탭
                                    items(uiState.filteredMissions, key = { it.id }) { mission ->
                                        ArchiveMissionCard(
                                            mission = mission,
                                            onClick = { onMissionClick(mission.id) },
                                            onToggleSave = { onToggleMissionSave(mission.id) },
                                            modifier = Modifier.animateItem()
                                        )
                                    }
                                }
                                1 -> { // 대화 탭
                                    items(uiState.conversations, key = { it.id }) { conversation ->
                                        ArchiveConversationCard(
                                            title = conversation.title,
                                            tags = conversation.tags,
                                            summary = conversation.summary ?: "",
                                            date = conversation.date,
                                            time = conversation.time,
                                            onClick = { onConversationClick(conversation.id) },
                                            modifier = Modifier.animateItem()
                                        )
                                    }
                                }
                                2 -> { // 문장 탭
                                    items(uiState.sentences, key = { it.id }) { sentence ->
                                        BookmarkCard(
                                            item = sentence,
                                            isSentence = true,
                                            onClick = { onSentenceClick(sentence.id) },
                                            onToggleSave = { onToggleSentenceSave(sentence.id) },
                                            modifier = Modifier.animateItem()
                                        )
                                    }
                                }
                                3 -> { // 리포트 탭
                                    items(displayReports, key = { it.id }) { report ->
                                        val isWeeklyCompare = report.title.contains("주간 비교")
                                        val reportTypeLabel = if (isWeeklyCompare) "주간 비교 리포트" else "성장 리포트"
                                        val displayItem = report.copy(status = reportTypeLabel)

                                        BookmarkCard(
                                            item = displayItem,
                                            isSentence = false,
                                            // 💡 수정됨: report.id 와 isWeeklyCompare를 함께 전달
                                            onClick = { onReportClick(report.id, isWeeklyCompare) },
                                            onToggleSave = { onToggleReportSave(report.id) },
                                            modifier = Modifier.animateItem()
                                        )
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

// ── 필터 칩 UI (미션 전용) ──
@Composable
private fun FilterChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    val baseModifier = if (isSelected) {
        Modifier
            .clip(shape)
            .background(Primary600)
    } else {
        Modifier
            .softShadow(color = Gray1000.copy(alpha = 0.01f), offsetY = 8.dp, blur = 24.dp, cornerRadius = 20.dp)
            .clip(shape)
            .background(White)
    }

    Box(
        modifier = baseModifier
            .clickable(onClick = onClick)
            .height(34.dp)
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = if (isSelected) TqType.BodyM.copy(fontWeight = FontWeight.Medium).figma() else TqType.BodyM.figma(),
            color = if (isSelected) Primary50 else Gray900
        )
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