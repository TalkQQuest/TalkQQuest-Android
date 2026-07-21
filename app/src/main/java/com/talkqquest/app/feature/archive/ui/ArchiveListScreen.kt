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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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
    onMissionClick: (Long) -> Unit = {},
    onConversationClick: (String) -> Unit = {},
    onSentenceClick: (String) -> Unit = {},
    onReportClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 💡 상세 화면에서 돌아왔을 때 최신 북마크 상태를 다시 불러오도록 반영
    LaunchedEffect(Unit) {
        viewModel.refreshData()
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
    onMissionClick: (Long) -> Unit,
    onToggleMissionSave: (Long) -> Unit,
    onConversationClick: (String) -> Unit,
    onSentenceClick: (String) -> Unit,
    onToggleSentenceSave: (String) -> Unit,
    onReportClick: (String) -> Unit,
    onToggleReportSave: (String) -> Unit
) {
    val tabs = listOf("미션", "대화", "문장", "리포트")
    val pagerState = rememberPagerState(initialPage = initialTabIndex, pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()
    val filters = listOf("전체", "완료", "미완료")

    FitDesign {
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

            // [3] 필터 칩 영역 (미션 탭 전용)
            if (pagerState.currentPage == 0) {
                Spacer(modifier = Modifier.height(27.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 15.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filters.forEach { filter ->
                        FilterChip(
                            text = filter,
                            isSelected = uiState.selectedFilter == filter,
                            onClick = { onFilterSelect(filter) }
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }

            // [4] Pager 리스트
            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
                val isListEmpty = when (page) {
                    0 -> uiState.filteredMissions.isEmpty()
                    1 -> uiState.conversations.isEmpty()
                    2 -> uiState.sentences.isEmpty()
                    3 -> uiState.reports.isEmpty()
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
                                    RecentActivityCard(
                                        activity = conversation,
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
                                items(uiState.reports, key = { it.id }) { report ->
                                    BookmarkCard(
                                        item = report,
                                        isSentence = false,
                                        onClick = { onReportClick(report.id) },
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
    }
}

// ── 필터 칩 UI ──
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
        ArchiveMissionItem(1, "처음 보는 사람에게 짧게 인사하기", "짧은 대화", "쉬움", 2, 20, isCompleted = true, isSaved = true),
        ArchiveMissionItem(2, "최근 본 영화 이야기하기", "짧은 대화", "쉬움", 5, 20, isCompleted = false, isSaved = true)
    ),
    conversations = listOf(
        RecentActivity(id = "1", title = "처음 보는 사람에게 짧게 인사하기", type = ActivityType.CONVERSATION, status = "대화 완료", date = "2026.08.20")
    ),
    sentences = listOf(
        BookmarkArchiveItem("1", "그렇군요! 저도 편해서 놀랐어요.", "문장 저장", "2026.08.20")
    ),
    reports = listOf(
        BookmarkArchiveItem("1", "처음 보는 사람에게 짧게 인사하기", "리포트 열람", "2026.08.20")
    )
)

@Preview(name = "1. 보관함 리스트 [미션]", showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun ArchiveListMissionPreview() {
    TalkQQuestTheme {
        ArchiveListScreenContent(
            initialTabIndex = 0, uiState = previewUiState, onBackClick = {}, onFilterSelect = {}, onMissionClick = {}, onToggleMissionSave = {}, onConversationClick = {}, onSentenceClick = {}, onToggleSentenceSave = {}, onReportClick = {}, onToggleReportSave = {}
        )
    }
}

@Preview(name = "2. 보관함 리스트 [대화]", showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun ArchiveListConversationPreview() {
    TalkQQuestTheme {
        ArchiveListScreenContent(
            initialTabIndex = 1, uiState = previewUiState, onBackClick = {}, onFilterSelect = {}, onMissionClick = {}, onToggleMissionSave = {}, onConversationClick = {}, onSentenceClick = {}, onToggleSentenceSave = {}, onReportClick = {}, onToggleReportSave = {}
        )
    }
}

@Preview(name = "3. 보관함 리스트 [문장]", showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun ArchiveListSentencePreview() {
    TalkQQuestTheme {
        ArchiveListScreenContent(
            initialTabIndex = 2, uiState = previewUiState, onBackClick = {}, onFilterSelect = {}, onMissionClick = {}, onToggleMissionSave = {}, onConversationClick = {}, onSentenceClick = {}, onToggleSentenceSave = {}, onReportClick = {}, onToggleReportSave = {}
        )
    }
}

@Preview(name = "4. 보관함 리스트 [리포트]", showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun ArchiveListReportPreview() {
    TalkQQuestTheme {
        ArchiveListScreenContent(
            initialTabIndex = 3, uiState = previewUiState, onBackClick = {}, onFilterSelect = {}, onMissionClick = {}, onToggleMissionSave = {}, onConversationClick = {}, onSentenceClick = {}, onToggleSentenceSave = {}, onReportClick = {}, onToggleReportSave = {}
        )
    }
}