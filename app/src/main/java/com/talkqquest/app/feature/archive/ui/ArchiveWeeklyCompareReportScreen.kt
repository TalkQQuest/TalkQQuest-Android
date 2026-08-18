package com.talkqquest.app.feature.archive.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import com.talkqquest.app.R
import com.talkqquest.app.core.designsystem.Error
import com.talkqquest.app.core.designsystem.FitDesign
import com.talkqquest.app.core.designsystem.component.rememberHapticTick
import com.talkqquest.app.core.designsystem.Gray100
import com.talkqquest.app.core.designsystem.Gray1000
import com.talkqquest.app.core.designsystem.Gray200
import com.talkqquest.app.core.designsystem.Gray300
import com.talkqquest.app.core.designsystem.Gray400
import com.talkqquest.app.core.designsystem.Gray50
import com.talkqquest.app.core.designsystem.Gray500
import com.talkqquest.app.core.designsystem.Gray600
import com.talkqquest.app.core.designsystem.Gray700
import com.talkqquest.app.core.designsystem.Gray800
import com.talkqquest.app.core.designsystem.Gray900
import com.talkqquest.app.core.designsystem.Primary100
import com.talkqquest.app.core.designsystem.Primary600
import com.talkqquest.app.core.designsystem.Primary700
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.White
import com.talkqquest.app.core.designsystem.softShadow

import com.talkqquest.app.feature.archive.data.model.CategoryRank
import com.talkqquest.app.feature.archive.data.model.HighlightItem
import com.talkqquest.app.feature.archive.data.model.MetricChange
import com.talkqquest.app.feature.archive.data.model.WeeklyCompareReport
import com.talkqquest.app.feature.archive.viewmodel.ArchiveWeeklyCompareReportViewModel
import com.talkqquest.app.feature.archive.viewmodel.ArchiveWeeklyCompareUiState
import com.talkqquest.app.feature.mission.ui.figma
import kotlin.math.abs

@Composable
fun ArchiveWeeklyCompareReportScreen(
    onBackClick: () -> Unit = {},
    onCompletedMissionsClick: () -> Unit = {},
    viewModel: ArchiveWeeklyCompareReportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    FitDesign {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Gray50),
            contentAlignment = Alignment.Center,
        ) {
            when {
                uiState.isLoading -> CircularProgressIndicator(color = Primary600)
                uiState.errorMessage != null -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = uiState.errorMessage ?: "", style = TqType.BodyM.figma(), color = Error)
                    }
                }
                else -> {
                    ArchiveWeeklyCompareReportContent(
                        uiState = uiState,
                        onBackClick = onBackClick,
                        onToggleBookmark = viewModel::toggleBookmark,
                        onCompletedMissionsClick = onCompletedMissionsClick,
                        onPrevWeek = viewModel::showPrevWeek, // 💡 이벤트 전달
                        onNextWeek = viewModel::showNextWeek
                    )
                }
            }
        }
    }
}

@Composable
private fun ArchiveWeeklyCompareReportContent(
    uiState: ArchiveWeeklyCompareUiState,
    onBackClick: () -> Unit,
    onToggleBookmark: () -> Unit,
    onCompletedMissionsClick: () -> Unit,
    onPrevWeek: () -> Unit,
    onNextWeek: () -> Unit
) {
    val tick = rememberHapticTick()
    val weeklyData = uiState.weekly ?: WeeklyCompareReport(
        metrics = listOf(
            MetricChange("친절한 태도", 240, 300),
            MetricChange("대화 주도", 240, 300),
            MetricChange("공감 표현", 320, 310),
            MetricChange("질문 연결성", 280, 310)
        ),
        highlights = listOf(
            HighlightItem("질문 연결성을", " 꾸준히 개선하고 있어요"),
            HighlightItem("친절한 태도가", " 가장 많이 상승되었어요")
        ),
        completedMissions = 26,
        totalMissions = 100,
        topCategories = listOf(
            CategoryRank("여행", 10), CategoryRank("음식", 9),
            CategoryRank("일상", 7), CategoryRank("인사", 4)
        )
    )

    // 주차 라벨은 서버가 완성해 준 periodLabel을 ViewModel이 갈라 담아 준 값을 그대로 쓴다.
    // 예전에는 제목 문자열을 정규식으로 되짚어 뽑았는데, 서버 제목 형식이 그 정규식과 달라
    // 두 패턴 다 안 걸리면서 "이전 주차 / 선택 주차"라는 기본 문구가 그대로 노출됐다.
    // 서버가 값을 못 준 경우에만 그 기본 문구로 떨어진다.
    val prevLabel = uiState.prevWeekLabel.ifBlank { "이전 주차" }
    val thisLabel = uiState.thisWeekLabel.ifBlank { "선택 주차" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // [헤더 영역]
        Box(modifier = Modifier.fillMaxWidth().height(44.dp)) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .align(Alignment.CenterStart)
                    .clip(CircleShape)
                    .clickable(onClick = { tick(); onBackClick() }),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_back_chevron),
                    contentDescription = "뒤로가기",
                    tint = Gray500
                )
            }
            Text(
                text = "주간 비교 리포트",
                style = TqType.TitleL.figma(),
                color = Gray700,
                modifier = Modifier.align(Alignment.Center),
            )
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .align(Alignment.CenterEnd)
                    .clip(CircleShape)
                    .clickable(onClick = onToggleBookmark),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = if (uiState.isBookmarked) R.drawable.ic_mission_bookmark_filled else R.drawable.ic_mission_bookmark),
                    contentDescription = "북마크",
                    tint = Color.Unspecified
                )
            }
        }

        // [스크롤 영역]
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // 💡 [수정] 주차 이동 스위처 - ViewModel 이벤트 및 상태 전달
            WeekSwitcher(
                prevWeekLabel = prevLabel,
                thisWeekLabel = thisLabel,
                canGoPrev = uiState.canGoPrev,
                canGoNext = uiState.canGoNext,
                onPrevWeek = onPrevWeek,
                onNextWeek = onNextWeek
            )

            Spacer(Modifier.height(16.dp))

            SectionTitle("지난 주보다 이만큼 달라졌어요", color = Gray800)
            Spacer(Modifier.height(12.dp))
            MetricGrid(weeklyData.metrics)

            Spacer(Modifier.height(12.dp))
            HighlightCard(weeklyData.highlights)

            // 시안 순서는 [지표] → [자주 연습한 주제] → [진행률]이다. 홈에서 들어가는 주간 비교
            // 화면과 같은 순서이며, 이 화면만 진행률과 주제가 뒤바뀌어 있어 바로잡았다.
            Spacer(Modifier.height(18.dp))
            SectionTitle("자주 연습한 주제", color = Gray900)
            Spacer(Modifier.height(12.dp))
            TopicRow(weeklyData.topCategories)

            Spacer(Modifier.height(18.dp))
            SectionTitle("지금까지 얼마나 달려왔을까요?", color = Gray900)
            Spacer(Modifier.height(12.dp))
            MissionProgressCard(
                completed = weeklyData.completedMissions,
                total = weeklyData.totalMissions,
                onCompletedMissionsClick = onCompletedMissionsClick
            )

            Spacer(Modifier.height(60.dp))
        }
    }
}

// ── 주차 이동 (WeekSwitcher) ──
@Composable
private fun WeekSwitcher(
    prevWeekLabel: String,
    thisWeekLabel: String,
    canGoPrev: Boolean,
    canGoNext: Boolean,
    onPrevWeek: () -> Unit,
    onNextWeek: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(44.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 갈 리포트가 없어도 화살표를 지우지 않는다. 자리에 남기고 Gray/300으로 칠해
        // 비활성 상태임을 보여준다 — 홈에서 들어가는 주간 비교 화면과 같은 규칙(사용자 결정).
        // 예전에는 통째로 숨기고 빈 여백만 뒀는데, 화살표가 사라졌다 나타났다 해서 혼란스러웠다.
        WeekArrow(
            iconRes = R.drawable.ic_weekly_prev,
            description = "이전 주차",
            enabled = canGoPrev,
            onClick = onPrevWeek,
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = prevWeekLabel, style = TqType.BodyM.figma(), color = Gray600)
            Box(modifier = Modifier.size(18.dp), contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(R.drawable.ic_weekly_range_arrow),
                    contentDescription = null,
                    modifier = Modifier.requiredSize(width = 14.dp, height = 11.dp),
                )
            }
            Text(text = thisWeekLabel, style = TqType.LabelL.figma(), color = Gray600)
        }

        WeekArrow(
            iconRes = R.drawable.ic_weekly_next,
            description = "다음 주차",
            enabled = canGoNext,
            onClick = onNextWeek,
        )
    }
}

@Composable
private fun WeekArrow(
    iconRes: Int,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = description,
            modifier = Modifier.size(width = 8.dp, height = 13.dp),
            // 기본은 시안대로 Purple/600(벡터 자체 색). 갈 리포트가 없으면 Gray/300으로 덮어
            // 비활성으로 보이게 한다 — 홈 주간 비교 화면과 같은 처리.
            colorFilter = if (enabled) null else ColorFilter.tint(Gray300),
        )
    }
}

@Composable
private fun SectionTitle(text: String, color: androidx.compose.ui.graphics.Color) {
    Text(
        text = text,
        style = TqType.BodyM.figma(),
        color = color,
        modifier = Modifier.padding(start = 19.dp),
    )
}

// ── 핵심 지표 변화 카드 ──
@Composable
private fun MetricGrid(metrics: List<MetricChange>) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        metrics.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 15.dp, end = 11.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                row.forEach { MetricCard(it, Modifier.weight(1f)) }
                if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MetricCard(metric: MetricChange, modifier: Modifier = Modifier) {
    val delta = metric.thisWeek - metric.lastWeek
    val isPositive = delta >= 0

    Column(
        modifier = modifier
            .height(76.dp)
            .softShadow(color = Gray1000.copy(alpha = 0.01f), offsetY = 8.dp, blur = 24.dp, cornerRadius = 12.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(White)
            .padding(start = 16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = metric.name, style = TqType.BodyM.figma(), color = Gray900)
        Spacer(Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "${metric.lastWeek} → ${metric.thisWeek}",
                style = TqType.BodyS.figma(),
                color = Gray400,
                modifier = Modifier.widthIn(min = 68.dp),
            )

            Row(
                modifier = Modifier
                    .height(22.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isPositive) Primary100 else Gray100)
                    .padding(horizontal = 8.dp, vertical = 1.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (delta != 0) {
                    Box(
                        modifier = Modifier.size(width = 6.dp, height = 7.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            painter = painterResource(
                                if (isPositive) R.drawable.ic_metric_up else R.drawable.ic_metric_down,
                            ),
                            contentDescription = if (isPositive) "상승" else "하락",
                            modifier = Modifier.requiredSize(width = 9.dp, height = 10.dp),
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = abs(delta).toString(),
                    style = TqType.LabelL.copy(fontWeight = FontWeight.Medium).figma(),
                    color = if (isPositive) Primary700 else Gray500
                )
            }
        }
    }
}

// ── 하이라이트 문구 ──
@Composable
private fun HighlightCard(highlights: List<HighlightItem>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 15.dp, end = 11.dp)
            .heightIn(min = 76.dp)
            .background(White, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        highlights.forEach { item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(modifier = Modifier.size(20.dp), contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(R.drawable.ic_highlight_sparkle),
                        contentDescription = null,
                        modifier = Modifier.requiredSize(18.dp),
                    )
                }
                Text(
                    text = item.emphasis + item.rest,
                    style = TqType.BodyM.figma().copy(lineBreak = PhraseFirst),
                    color = Gray600,
                )
            }
        }
    }
}

// ── 미션 프로그래스 ──
@Composable
private fun MissionProgressCard(
    completed: Int,
    total: Int,
    onCompletedMissionsClick: () -> Unit,
) {
    val percent = if (total > 0) completed * 100 / total else 0
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 15.dp, end = 11.dp)
            .height(84.dp)
            .softShadow(
                color = Gray1000.copy(alpha = 0.01f),
                offsetY = 8.dp,
                blur = 24.dp,
                cornerRadius = 20.dp,
            )
            .background(White, RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        ProgressDonut(percent = percent)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "톡깨 미션 ${total}개 중, ${completed}개 완료했어요!",
                style = TqType.BodyS.figma().copy(lineBreak = PhraseFirst),
                color = Gray800,
                modifier = Modifier.padding(horizontal = 2.dp),
            )
            Row(
                modifier = Modifier
                    .height(26.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Gray100)
                    .clickable(onClick = onCompletedMissionsClick)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(text = "완료한 미션", style = TqType.LabelM.figma(), color = Gray600)
                Image(
                    painter = painterResource(R.drawable.ic_forward_chevron_small),
                    contentDescription = null,
                    modifier = Modifier.size(width = 16.dp, height = 17.dp),
                    colorFilter = ColorFilter.tint(Gray500),
                )
            }
        }
    }
}

@Composable
private fun ProgressDonut(percent: Int) {
    Box(modifier = Modifier.size(60.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(60.dp)) {
            val stroke = 5.dp.toPx()
            val inset = stroke / 2f
            val arcSize = Size(size.width - stroke, size.height - stroke)
            val topLeft = Offset(inset, inset)
            drawArc(
                color = Gray200,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke),
            )
            drawArc(
                color = Primary600,
                startAngle = -90f,
                sweepAngle = 360f * percent.coerceIn(0, 100) / 100f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke),
            )
        }
        Text(text = "$percent%", style = TqType.LabelL.figma(), color = Primary600)
    }
}

// ── 카테고리 랭킹 (가로 스크롤) ──
@Composable
private fun TopicRow(topics: List<CategoryRank>) {
    Box(modifier = Modifier.fillMaxWidth().height(100.dp)) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(start = 17.dp, end = 15.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            itemsIndexed(topics) { index, topic -> TopicChip(rank = index + 1, topic = topic) }
        }
        Box(
            modifier = Modifier
                .padding(start = 15.dp)
                .fillMaxWidth()
                .height(100.dp)
                .background(
                    Brush.horizontalGradient(
                        colorStops = arrayOf(
                            0.8413f to Gray50.copy(alpha = 0f),
                            1f to Gray50,
                        ),
                    ),
                ),
        )
    }
}

@Composable
private fun TopicChip(rank: Int, topic: CategoryRank) {
    val isTop = rank == 1
    Column(
        modifier = Modifier
            .width(89.dp)
            .height(100.dp)
            .softShadow(
                color = Primary600.copy(alpha = 0.04f),
                offsetX = 2.dp,
                offsetY = 4.dp,
                blur = 4.dp,
                cornerRadius = 12.dp,
            )
            .background(White, RoundedCornerShape(12.dp))
            .padding(top = 14.dp, bottom = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .background(if (isTop) Primary100 else Gray100, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = rank.toString(),
                style = TqType.BodyM.figma(),
                color = if (isTop) Primary600 else Gray600,
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = topic.name,
            style = TqType.BodyL.copy(fontWeight = FontWeight.Medium).figma(),
            color = if (isTop) Primary600 else Gray600,
            maxLines = 1,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(2.dp))
        Text(text = "${topic.count}회", style = TqType.BodyS.figma(), color = Gray500)
    }
}

// ── 텍스트 포맷팅 유틸 ──
private val PhraseFirst = LineBreak(
    strategy = LineBreak.Strategy.Simple,
    strictness = LineBreak.Strictness.Normal,
    wordBreak = LineBreak.WordBreak.Phrase,
)

// ── 프리뷰 ──

@Preview(name = "1. 보관함: 주간 비교 리포트 (화살표 없음)", showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun ArchiveWeeklyCompareReportScreenNoArrowsPreview() {
    TalkQQuestTheme {
        ArchiveWeeklyCompareReportContent(
            uiState = ArchiveWeeklyCompareUiState(
                title = "8월 1-2주차 주간 비교 리포트",
                canGoPrev = false,
                canGoNext = false
            ),
            onBackClick = {},
            onToggleBookmark = {},
            onCompletedMissionsClick = {},
            onPrevWeek = {},
            onNextWeek = {}
        )
    }
}

@Preview(name = "2. 보관함: 주간 비교 리포트 (양쪽 화살표 모두 노출)", showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun ArchiveWeeklyCompareReportScreenWithArrowsPreview() {
    TalkQQuestTheme {
        ArchiveWeeklyCompareReportContent(
            uiState = ArchiveWeeklyCompareUiState(
                title = "8월 2-3주차 주간 비교 리포트",
                canGoPrev = true, // 💡 이전 리포트가 있을 때 노출
                canGoNext = true  // 💡 다음 리포트가 있을 때 노출
            ),
            onBackClick = {},
            onToggleBookmark = {},
            onCompletedMissionsClick = {},
            onPrevWeek = {},
            onNextWeek = {}
        )
    }
}