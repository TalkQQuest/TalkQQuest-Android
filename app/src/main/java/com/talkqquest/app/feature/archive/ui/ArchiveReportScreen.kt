package com.talkqquest.app.feature.archive.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import com.talkqquest.app.R
import com.talkqquest.app.core.designsystem.Error
import com.talkqquest.app.core.designsystem.FitDesign
import com.talkqquest.app.core.designsystem.Gray100
import com.talkqquest.app.core.designsystem.Gray1000
import com.talkqquest.app.core.designsystem.Gray200
import com.talkqquest.app.core.designsystem.Gray300
import com.talkqquest.app.core.designsystem.Gray400
import com.talkqquest.app.core.designsystem.Gray50
import com.talkqquest.app.core.designsystem.Gray500
import com.talkqquest.app.core.designsystem.Gray600
import com.talkqquest.app.core.designsystem.Gray800
import com.talkqquest.app.core.designsystem.Gray900
import com.talkqquest.app.core.designsystem.Primary100
import com.talkqquest.app.core.designsystem.Primary50
import com.talkqquest.app.core.designsystem.Primary600
import com.talkqquest.app.core.designsystem.Primary700
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.White
import com.talkqquest.app.core.designsystem.softShadow
import com.talkqquest.app.feature.report.data.model.CategoryRank
import com.talkqquest.app.feature.report.data.model.GrowthReport
import com.talkqquest.app.feature.report.data.model.HighlightItem
import com.talkqquest.app.feature.report.data.model.MetricChange
import com.talkqquest.app.feature.report.data.model.WeeklyCompareReport
import com.talkqquest.app.feature.archive.viewmodel.ArchiveReportUiState
import com.talkqquest.app.feature.archive.viewmodel.ArchiveReportViewModel

@Composable
fun ArchiveReportScreen(
    onBackClick: () -> Unit = {},
    viewModel: ArchiveReportViewModel = hiltViewModel()
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
                uiState.growth != null && uiState.weekly != null -> {
                    ArchiveReportContent(
                        uiState = uiState,
                        onBackClick = onBackClick,
                        onToggleBookmark = viewModel::toggleBookmark
                    )
                }
            }
        }
    }
}

@Composable
private fun ArchiveReportContent(
    uiState: ArchiveReportUiState,
    onBackClick: () -> Unit,
    onToggleBookmark: () -> Unit,
    initialTab: Int = 0
) {
    var tab by rememberSaveable { mutableIntStateOf(initialTab) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // [헤더 영역]
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
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
                    imageVector = Icons.Default.Close,
                    contentDescription = "닫기",
                    tint = Gray500
                )
            }

            Text(
                text = uiState.title,
                style = TqType.BodyL.copy(fontWeight = FontWeight.Medium).figma(),
                color = Gray800,
                modifier = Modifier.align(Alignment.Center)
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

        Spacer(modifier = Modifier.height(28.dp))

        // [탭 바 영역]
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            ReportTabBar(
                tab = tab,
                onTabChange = { tab = it }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // [본문 스크롤 영역]
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            Crossfade(targetState = tab, animationSpec = tween(300), label = "archiveReportTab") { current ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    when (current) {
                        0 -> GrowthTab(uiState.growth!!)
                        else -> WeeklyCompareTab(uiState.weekly!!)
                    }
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }
        }
    }
}

// ==========================================
// ── 공통 UI 컴포넌트 ──
// ==========================================

@Composable
private fun ReportTabBar(
    tab: Int,
    onTabChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val indicatorX by animateDpAsState(
        targetValue = if (tab == 0) 0.dp else 130.dp,
        animationSpec = tween(250),
        label = "tabIndicator",
    )
    Box(
        modifier = modifier
            .size(width = 260.dp, height = 33.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(White),
    ) {
        Box(
            modifier = Modifier
                .offset(x = indicatorX)
                .size(width = 130.dp, height = 33.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Primary700),
        )
        Row(Modifier.fillMaxSize()) {
            listOf("성장 리포트", "주간 비교 리포트").forEachIndexed { index, label ->
                val textColor by animateColorAsState(
                    targetValue = if (tab == index) Gray50 else Gray600,
                    animationSpec = tween(250),
                    label = "tabText",
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { onTabChange(index) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = label, style = TqType.LabelL.figma(), color = textColor)
                }
            }
        }
    }
}

// ── 1. 성장 리포트 탭 ──

@Composable
private fun GrowthTab(growth: GrowthReport) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LevelCompareRow(prevLevel = growth.prevLevel, currentLevel = growth.currentLevel)
        Spacer(Modifier.height(24.dp))
        GrowthTrendCard(
            growthPercent = growth.growthPercent,
            weekLabels = growth.weekLabels,
            modifier = Modifier.padding(horizontal = 15.dp),
        )
        Spacer(Modifier.height(24.dp))
        CategoryRankRow(ranks = growth.categoryRanks)
        Spacer(Modifier.height(24.dp))
        CompletedMissionCard(
            completed = growth.completedMissions,
            total = growth.totalMissions,
            modifier = Modifier.padding(horizontal = 15.dp),
        )
    }
}

@Composable
private fun LevelCompareRow(prevLevel: Int, currentLevel: Int) {
    Row(verticalAlignment = Alignment.Bottom) {
        LevelChip(label = "저번 주", level = prevLevel, highlighted = false)
        Spacer(Modifier.width(8.dp))
        Image(
            painter = painterResource(R.drawable.ic_report_level_arrow),
            contentDescription = null,
            modifier = Modifier.size(width = 42.dp, height = 36.dp),
        )
        Spacer(Modifier.width(8.dp))
        LevelChip(label = "이번 주", level = currentLevel, highlighted = true)
    }
}

@Composable
private fun LevelChip(label: String, level: Int, highlighted: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = TqType.LabelM.figma(),
            color = if (highlighted) Gray600 else Gray500,
        )
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .size(width = 98.dp, height = 36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (highlighted) Primary100 else Gray100),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Lv.$level",
                style = TqType.TitleL.figma(),
                color = if (highlighted) Primary600 else Gray400,
            )
        }
    }
}

@Composable
private fun GrowthTrendCard(
    growthPercent: Int,
    weekLabels: List<String>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .softShadow(color = Gray1000.copy(alpha = 0.01f), offsetY = 8.dp, blur = 24.dp, cornerRadius = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = "대화 성장 추이", style = TqType.LabelL.figma(), color = Gray800)
            Text(text = "+ $growthPercent%", style = TqType.LabelL.figma(), color = Primary600)
        }
        Spacer(Modifier.height(8.dp))
        GrowthChart()
        Spacer(Modifier.height(8.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(Gray300))
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            weekLabels.forEach { label ->
                Text(text = label, style = TqType.Caption.figma(), color = Gray400)
            }
        }
    }
}

@Composable
private fun GrowthChart() {
    val linePoints = listOf(
        Offset(0.00f, 87.00f),
        Offset(121.71f, 57.00f),
        Offset(209.09f, 77.50f),
        Offset(304.80f, 35.00f),
    )
    val dots = listOf(
        Offset(32.09f, 79.00f),
        Offset(121.55f, 57.00f),
        Offset(209.97f, 77.00f),
    )
    val endDot = Offset(304.60f, 35.00f)
    val glowColor = Primary600.copy(alpha = 0.45f).toArgb()

    Canvas(modifier = Modifier.size(width = 327.dp, height = 97.dp)) {
        val s = size.width / 327f
        val areaPath = Path().apply {
            linePoints.forEachIndexed { index, p ->
                if (index == 0) moveTo(p.x * s, p.y * s) else lineTo(p.x * s, p.y * s)
            }
            lineTo(linePoints.last().x * s, 97f * s)
            lineTo(0f, 97f * s)
            close()
        }
        drawPath(
            path = areaPath,
            brush = Brush.verticalGradient(
                0.0000f to Primary600.copy(alpha = 0.0871f),
                0.2291f to Primary600.copy(alpha = 0.06f),
                0.9887f to Primary600.copy(alpha = 0f),
                startY = 35f * s,
                endY = 97f * s,
            ),
        )

        val path = Path().apply {
            linePoints.forEachIndexed { index, p ->
                if (index == 0) moveTo(p.x * s, p.y * s) else lineTo(p.x * s, p.y * s)
            }
        }
        drawPath(path = path, color = Primary600, style = Stroke(width = 2f * s))

        dots.forEach { c ->
            drawCircle(color = Primary600, radius = 4f * s, center = Offset(c.x * s, c.y * s))
            drawCircle(color = Primary50, radius = 2f * s, center = Offset(c.x * s, c.y * s))
        }

        drawIntoCanvas { canvas ->
            val paint = android.graphics.Paint().apply {
                isAntiAlias = true
                color = glowColor
                maskFilter = android.graphics.BlurMaskFilter(4f * s, android.graphics.BlurMaskFilter.Blur.NORMAL)
            }
            canvas.nativeCanvas.drawCircle(endDot.x * s, endDot.y * s, 5f * s, paint)
        }
        drawCircle(color = Primary600, radius = 5f * s, center = Offset(endDot.x * s, endDot.y * s))
        drawCircle(color = Primary50, radius = 3f * s, center = Offset(endDot.x * s, endDot.y * s))
    }
}

@Composable
private fun CategoryRankRow(ranks: List<CategoryRank>) {
    Box(modifier = Modifier.fillMaxWidth().height(100.dp)) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 17.dp),
        ) {
            itemsIndexed(ranks) { index, rank ->
                RankCard(rank = rank, order = index + 1)
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .width(60.dp)
                .background(Brush.horizontalGradient(0f to Gray50.copy(alpha = 0f), 1f to Gray50)),
        )
    }
}

@Composable
private fun RankCard(rank: CategoryRank, order: Int) {
    val top = order == 1
    Column(
        modifier = Modifier
            .size(width = 89.dp, height = 100.dp)
            .softShadow(color = Primary600.copy(alpha = 0.04f), offsetX = 2.dp, offsetY = 4.dp, blur = 4.dp, cornerRadius = 12.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(White)
            .padding(top = 14.dp, bottom = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(if (top) Primary100 else Gray100),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "$order",
                style = TqType.BodyM.figma(),
                color = if (top) Primary600 else Gray600,
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = rank.name,
            style = TqType.BodyL.copy(fontWeight = FontWeight.Medium).figma(),
            color = if (top) Primary600 else Gray600,
        )
        Spacer(Modifier.height(2.dp))
        Text(text = "${rank.count}회", style = TqType.BodyS.figma(), color = Gray500)
    }
}

@Composable
private fun CompletedMissionCard(
    completed: Int,
    total: Int,
    modifier: Modifier = Modifier,
) {
    val percent = if (total > 0) completed * 100 / total else 0
    Row(
        modifier = modifier
            .fillMaxWidth()
            .softShadow(color = Gray1000.copy(alpha = 0.01f), offsetY = 8.dp, blur = 24.dp, cornerRadius = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(60.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = Stroke(width = size.width * 5f / 60f)
                val inset = stroke.width / 2f
                drawCircle(color = Gray200, radius = size.width / 2f - inset, style = stroke)
                drawArc(
                    color = Primary600, startAngle = -90f, sweepAngle = percent * 3.6f,
                    useCenter = false, topLeft = Offset(inset, inset),
                    size = Size(size.width - inset * 2f, size.height - inset * 2f), style = stroke,
                )
            }
            Text(text = "$percent%", style = TqType.LabelL.figma(), color = Primary600)
        }
        Spacer(Modifier.width(24.dp))
        Column {
            Text(
                text = "톡깨 미션 ${total}개 중, ${completed}개 완료했어요!",
                style = TqType.BodyS.figma(), color = Gray900,
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Gray100)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "완료한 미션", style = TqType.LabelM.figma(), color = Gray600)
                Spacer(Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null, tint = Gray500, modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

// ── 2. 주간 비교 리포트 탭 ──

@Composable
private fun WeeklyCompareTab(weekly: WeeklyCompareReport) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(90.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Primary50)
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = buildAnnotatedString {
                        append("지난주보다 ")
                        withStyle(SpanStyle(color = Primary600)) { append("이렇게 달라졌어요") }
                    },
                    style = TqType.TitleL.figma(),
                    color = Gray800,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "4가지 핵심 지표를 비교해\n이번 주의 변화를 확인해보세요",
                    style = TqType.Caption.figma(),
                    color = Gray500,
                )
            }
            Box(modifier = Modifier.align(Alignment.Bottom)) {
                if (android.os.Build.VERSION.SDK_INT >= 31) {
                    Image(
                        painter = painterResource(R.drawable.img_report_robot),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(Primary600.copy(alpha = 0.2f)),
                        modifier = Modifier
                            .width(104.86.dp)
                            .blur(48.dp, BlurredEdgeTreatment.Unbounded),
                    )
                }
                Image(
                    painter = painterResource(R.drawable.img_report_robot),
                    contentDescription = null,
                    modifier = Modifier.width(104.86.dp),
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(text = "핵심 지표 변화", style = TqType.LabelL.figma(), color = Gray800)
            Spacer(Modifier.height(16.dp))

            val metrics = weekly.metrics
            metrics.chunked(2).forEachIndexed { rowIndex, rowMetrics ->
                if (rowIndex > 0) Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    rowMetrics.forEachIndexed { colIndex, metric ->
                        MetricCard(
                            metric = metric,
                            index = rowIndex * 2 + colIndex,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(text = "개선 하이라이트", style = TqType.LabelL.figma(), color = Gray800)
            weekly.highlights.forEach { highlight ->
                Spacer(Modifier.height(16.dp))
                HighlightRow(item = highlight)
            }
        }
    }
}

private data class MetricIconSpec(val resId: Int, val width: Float, val height: Float)

private val metricIconSpecs = listOf(
    MetricIconSpec(R.drawable.img_report_kindness, width = 67f, height = 70f),
    MetricIconSpec(R.drawable.img_report_initiative, width = 73.9f, height = 72.9f),
    MetricIconSpec(R.drawable.img_report_empathy, width = 69.6f, height = 64.6f),
    MetricIconSpec(R.drawable.img_report_question, width = 74.9f, height = 70.9f),
)

@Composable
private fun MetricCard(
    metric: MetricChange,
    index: Int,
    modifier: Modifier = Modifier,
) {
    val icon = metricIconSpecs.getOrNull(index)
    Box(
        modifier = modifier
            .height(76.dp)
            .softShadow(color = Gray1000.copy(alpha = 0.01f), offsetY = 8.dp, blur = 24.dp, cornerRadius = 12.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(White),
    ) {
        if (icon != null) {
            Image(
                painter = painterResource(icon.resId),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(width = icon.width.dp, height = icon.height.dp),
            )
        }
        Column(modifier = Modifier.offset(x = 14.dp, y = 15.dp)) {
            Text(text = metric.name, style = TqType.BodyM.figma(), color = Color.Black)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "${metric.lastWeek} → ${metric.thisWeek}",
                    style = TqType.Caption.figma(),
                    color = Gray400,
                )
                Spacer(Modifier.width(6.dp))
                val diff = metric.thisWeek - metric.lastWeek
                Box(
                    modifier = Modifier
                        .size(width = 38.dp, height = 20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Primary100),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (diff >= 0) "+ $diff" else "- ${-diff}",
                        style = TqType.BodyS.figma(),
                        color = Primary700,
                    )
                }
            }
        }
    }
}

@Composable
private fun HighlightRow(item: HighlightItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Gray100)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(20.dp), contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(R.drawable.img_report_star),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = Primary600)) { append(item.emphasis) }
                append(item.rest)
            },
            style = TqType.BodyM.figma(),
            color = Color.Black,
        )
    }
}

// ==========================================
// ── Previews ──
// ==========================================
@Preview(name = "보관함 성장 리포트 탭", showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun ArchiveGrowthReportPreview() {
    TalkQQuestTheme {
        ArchiveReportContent(
            uiState = ArchiveReportUiState(
                title = "최근 본 영화 이야기 하기",
                isBookmarked = true,
                growth = GrowthReport(
                    prevLevel = 1, currentLevel = 2, growthPercent = 18,
                    weekLabels = listOf("7월 4주", "8월 1주", "8월 2주", "8월 3주"),
                    categoryRanks = listOf(
                        CategoryRank("여행", 10), CategoryRank("음식", 9),
                        CategoryRank("일상", 7), CategoryRank("인사", 4)
                    ),
                    completedMissions = 26, totalMissions = 100
                ),
                weekly = null
            ),
            onBackClick = {},
            onToggleBookmark = {},
            initialTab = 0
        )
    }
}

@Preview(name = "보관함 주간 비교 리포트 탭", showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun ArchiveWeeklyComparePreview() {
    TalkQQuestTheme {
        ArchiveReportContent(
            uiState = ArchiveReportUiState(
                title = "최근 본 영화 이야기 하기",
                isBookmarked = false,
                growth = null,
                weekly = WeeklyCompareReport(
                    metrics = listOf(
                        MetricChange("친절한 태도", 88, 92), MetricChange("대화 주도", 86, 88),
                        MetricChange("공감 표현", 82, 85), MetricChange("질문 연결성", 74, 78)
                    ),
                    highlights = listOf(
                        HighlightItem("전체 점수", "가 78점에서 86점으로 상승했어요"),
                        HighlightItem("친절한 태도", "가 가장 많이 상승되었어요"),
                        HighlightItem("질문 연결성", "을 꾸준히 개선하고 있어요")
                    )
                )
            ),
            onBackClick = {},
            onToggleBookmark = {},
            initialTab = 1
        )
    }
}