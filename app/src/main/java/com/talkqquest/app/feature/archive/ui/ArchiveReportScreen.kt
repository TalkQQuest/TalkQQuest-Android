package com.talkqquest.app.feature.archive.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.talkqquest.app.core.designsystem.Gray1000
import com.talkqquest.app.core.designsystem.Gray300
import com.talkqquest.app.core.designsystem.Gray400
import com.talkqquest.app.core.designsystem.Gray50
import com.talkqquest.app.core.designsystem.Gray500
import com.talkqquest.app.core.designsystem.Gray600
import com.talkqquest.app.core.designsystem.Gray700
import com.talkqquest.app.core.designsystem.Gray800
import com.talkqquest.app.core.designsystem.Primary200
import com.talkqquest.app.core.designsystem.Primary600
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.White
import com.talkqquest.app.core.designsystem.softShadow
import com.talkqquest.app.feature.archive.viewmodel.ArchiveReportUiState
import com.talkqquest.app.feature.archive.viewmodel.ArchiveReportViewModel

import com.talkqquest.app.feature.archive.data.model.Competency
import com.talkqquest.app.feature.archive.data.model.CompetencyAxis
import com.talkqquest.app.feature.archive.data.model.GrowthReport

import com.talkqquest.app.feature.mission.ui.figma

private val StarYellow = Color(0xFFF9AC17)

private fun tierEmblemSmallRes(name: String): Int = when (name) {
    "브론즈" -> R.drawable.img_tier_bronze_s
    "실버" -> R.drawable.img_tier_silver_s
    "골드" -> R.drawable.img_tier_gold_s
    "플래티넘" -> R.drawable.img_tier_platinum_s
    "다이아" -> R.drawable.img_tier_dia_s
    "마스터" -> R.drawable.img_tier_master_s
    else -> R.drawable.img_tier_gold_s
}

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
                else -> {
                    ArchiveGrowthReportContent(
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
private fun ArchiveGrowthReportContent(
    uiState: ArchiveReportUiState,
    onBackClick: () -> Unit,
    onToggleBookmark: () -> Unit
) {
    var showTierHelp by remember { mutableStateOf(false) }

    val growthData = uiState.growth ?: GrowthReport(
        tierName = "골드",
        tierStars = 2,
        nextStarsNeeded = 1,
        nextTierName = "플래티넘",
        competencies = listOf(
            Competency(CompetencyAxis.KINDNESS, "친절한 태도", "친절한 태도", 300, 280, 70),
            Competency(CompetencyAxis.INITIATIVE, "대화 주도", "대화 주도", 300, 200, 70),
            Competency(CompetencyAxis.EMPATHY, "공감 표현", "공감 능력", 300, 150, 70),
            Competency(CompetencyAxis.QUESTION_LINK, "질문 연결성", "질문 연결성", 300, 250, 70),
        )
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            // 💡 피드백 반영: 상단 Spacer(8.dp) 제거됨 (상태바 직후 바로 헤더 노출)

            Box(modifier = Modifier.fillMaxWidth().height(44.dp).padding(horizontal = 4.dp)) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onBackClick),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(painter = painterResource(R.drawable.ic_back_chevron), contentDescription = "뒤로가기", tint = Gray500)
                }
                Text(
                    text = "성장 리포트",
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

            Spacer(Modifier.height(5.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                TierCard(
                    tierName = growthData.tierName,
                    tierStars = growthData.tierStars,
                    nextStarsNeeded = growthData.nextStarsNeeded,
                    nextTierName = growthData.nextTierName,
                    onInfoClick = { showTierHelp = true },
                )

                Spacer(Modifier.height(13.dp))

                CompetencyCard(competencies = growthData.competencies)

                Spacer(Modifier.height(60.dp))
            }
        }

        if (showTierHelp) {
            TierPromotionSheet(
                tierName = growthData.tierName,
                competencies = growthData.competencies,
                onDismiss = { showTierHelp = false },
            )
        }
    }
}

@Composable
private fun TierCard(
    tierName: String,
    tierStars: Int,
    nextStarsNeeded: Int,
    nextTierName: String,
    onInfoClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .softShadow(color = Gray1000.copy(alpha = 0.01f), offsetY = 8.dp, blur = 24.dp, cornerRadius = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(White)
            .padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "실전 티어", style = TqType.LabelL.figma(), color = Gray500)
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onInfoClick),
                contentAlignment = Alignment.Center,
            ) {
                // 💡 피드백 반영: 아이콘 실제 크기 18.dp -> 14.dp로 축소
                Icon(
                    painter = painterResource(R.drawable.ic_notification_info),
                    contentDescription = "안내",
                    tint = Gray400,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(painter = painterResource(tierEmblemSmallRes(tierName)), contentDescription = null, modifier = Modifier.size(55.dp))
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(text = tierName, style = TqType.BodyL.figma().copy(fontWeight = FontWeight.Medium), color = Gray800)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(3) { i ->
                        Icon(painter = painterResource(R.drawable.ic_tier_star), contentDescription = null, tint = if (i < tierStars) StarYellow else Gray300, modifier = Modifier.size(15.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = Gray600)) { append("별 ") }
                withStyle(SpanStyle(color = Primary600, fontWeight = FontWeight.Medium)) { append("${nextStarsNeeded}개") }
                withStyle(SpanStyle(color = Gray600)) { append("를 더 획득하면 ") }
                withStyle(SpanStyle(color = Primary600, fontWeight = FontWeight.Medium)) { append(nextTierName) }
                withStyle(SpanStyle(color = Gray600)) { append("이에요!") }
            },
            style = TqType.BodyM.figma(),
        )
    }
}

@Composable
private fun CompetencyCard(competencies: List<Competency>) {
    val byAxis = competencies.associateBy { it.axis }
    val top = byAxis[CompetencyAxis.KINDNESS]
    val right = byAxis[CompetencyAxis.INITIATIVE]
    val bottom = byAxis[CompetencyAxis.EMPATHY]
    val left = byAxis[CompetencyAxis.QUESTION_LINK]

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .softShadow(color = Gray1000.copy(alpha = 0.01f), offsetY = 8.dp, blur = 24.dp, cornerRadius = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(White)
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = "핵심 역량", style = TqType.BodyL.figma().copy(fontWeight = FontWeight.Medium), color = Color.Black, modifier = Modifier.padding(start = 16.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (top != null) AxisLabel(top.label, top.gain)
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                if (left != null) AxisLabel(left.label, left.gain)

                RadarChart(
                    top = frac(top),
                    right = frac(right),
                    bottom = frac(bottom),
                    left = frac(left),
                    modifier = Modifier.size(176.dp),
                )

                if (right != null) AxisLabel(right.label, right.gain)
            }
            if (bottom != null) AxisLabel(bottom.label, bottom.gain)
        }

        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            competencies.forEach { c -> LegendRow(c) }
        }
    }
}

private fun frac(c: Competency?): Float =
    if (c == null || c.maxScore == 0) 0f else (c.score.toFloat() / c.maxScore).coerceIn(0f, 1f)

@Composable
private fun AxisLabel(label: String, gain: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = TqType.BodyM.figma(), color = Gray800)
        Text(text = "+$gain", style = TqType.LabelL.figma(), color = Primary600)
    }
}

@Composable
private fun RadarChart(
    top: Float,
    right: Float,
    bottom: Float,
    left: Float,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = size.width / 2f - 4.dp.toPx()
        val dot = size.width * 0.028f

        val gridStroke = Stroke(width = 1.dp.toPx())
        listOf(1f, 0.752f, 0.540f, 0.293f).forEach { ring ->
            val rr = r * ring
            val diamond = Path().apply {
                moveTo(cx, cy - rr); lineTo(cx + rr, cy); lineTo(cx, cy + rr); lineTo(cx - rr, cy); close()
            }
            drawPath(diamond, color = Gray300, style = gridStroke)
        }
        drawLine(Gray300, Offset(cx, cy - r), Offset(cx, cy + r), strokeWidth = 1.dp.toPx())
        drawLine(Gray300, Offset(cx - r, cy), Offset(cx + r, cy), strokeWidth = 1.dp.toPx())

        val pTop = Offset(cx, cy - r * top)
        val pRight = Offset(cx + r * right, cy)
        val pBottom = Offset(cx, cy + r * bottom)
        val pLeft = Offset(cx - r * left, cy)
        val data = Path().apply {
            moveTo(pTop.x, pTop.y); lineTo(pRight.x, pRight.y)
            lineTo(pBottom.x, pBottom.y); lineTo(pLeft.x, pLeft.y); close()
        }

        drawPath(data, color = Primary200.copy(alpha = 0.6f))
        drawPath(data, color = Primary600.copy(alpha = 0.6f), style = Stroke(width = 1.dp.toPx()))

        listOf(pTop, pRight, pBottom, pLeft).forEach { drawCircle(Primary600, radius = dot, center = it) }
    }
}

@Composable
private fun LegendRow(c: Competency) {
    val maxed = c.score >= c.maxScore
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (maxed) {
                Box(
                    modifier = Modifier.size(19.dp).clip(CircleShape).background(Primary600),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_benefit_check),
                        contentDescription = null,
                        tint = White,
                        modifier = Modifier.size(13.dp),
                    )
                }
            } else {
                Box(modifier = Modifier.size(19.dp).clip(CircleShape).border(1.dp, Gray300, CircleShape))
            }
            Text(text = c.legendLabel, style = TqType.BodyM.figma(), color = Gray800)
        }
        Text(
            text = buildAnnotatedString {
                append(c.score.toString())
                withStyle(SpanStyle(color = Gray400, fontWeight = FontWeight.Normal)) { append(" / ${c.maxScore}") }
            },
            style = TqType.LabelL.figma(),
            color = if (maxed) Primary600 else Gray600,
        )
    }
}

@Composable
private fun TierPromotionSheet(
    tierName: String,
    competencies: List<Competency>,
    onDismiss: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss,
                ),
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .softShadow(
                    color = Gray1000.copy(alpha = 0.06f),
                    offsetY = (-8).dp,
                    blur = 24.dp,
                    cornerRadius = 36.dp,
                )
                .clip(RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp))
                .background(Gray50)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                )
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Gray600),
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "티어 승급 안내",
                    style = TqType.TitleL.figma(),
                    color = Gray800,
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(7.dp),
                    horizontalAlignment = Alignment.Start,
                ) {
                    HelpRow(
                        title = "핵심 역량 채우기",
                        subtitle = "미션을 통해 대화 역량을 넓혀요",
                    ) {
                        Box(modifier = Modifier.size(50.dp), contentAlignment = Alignment.Center) {
                            Image(
                                painter = painterResource(R.drawable.img_help_radar),
                                contentDescription = null,
                                modifier = Modifier.size(50.dp),
                            )
                        }
                    }
                    ChevronDown(Modifier.align(Alignment.CenterHorizontally))
                    HelpRow(
                        title = "별 획득하기",
                        subtitle = "핵심 역량 당 300점을 모두 채우면 별을 얻어요",
                    ) {
                        Box(modifier = Modifier.size(50.dp), contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(R.drawable.ic_tier_star),
                                contentDescription = null,
                                tint = StarYellow,
                                modifier = Modifier.size(44.dp),
                            )
                        }
                    }
                    ChevronDown(Modifier.align(Alignment.CenterHorizontally))
                    HelpRow(
                        title = "티어 승급하기",
                        subtitle = "별 3개를 다 모으면 다음 티어로!",
                    ) {
                        Box(modifier = Modifier.size(50.dp), contentAlignment = Alignment.Center) {
                            Image(
                                painter = painterResource(R.drawable.img_tier_master_s),
                                contentDescription = null,
                                modifier = Modifier.size(50.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HelpRow(
    title: String,
    subtitle: String,
    leading: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier.height(50.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        leading()
        Column {
            Text(
                text = title,
                style = TqType.BodyL.figma().copy(fontWeight = FontWeight.Medium),
                color = Gray800,
            )
            Text(
                text = subtitle,
                style = TqType.BodyM.figma(),
                color = Gray600,
                maxLines = 1,
                softWrap = false,
            )
        }
    }
}

@Composable
private fun ChevronDown(modifier: Modifier = Modifier) {
    Icon(
        painter = painterResource(R.drawable.ic_chevron_down),
        contentDescription = null,
        tint = Primary600,
        modifier = modifier.size(24.dp),
    )
}

@Preview(name = "보관함: 성장 리포트", showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun ArchiveGrowthReportScreenPreview() {
    TalkQQuestTheme {
        ArchiveGrowthReportContent(
            uiState = ArchiveReportUiState(
                title = "성장 리포트",
                isBookmarked = true
            ),
            onBackClick = {},
            onToggleBookmark = {}
        )
    }
}