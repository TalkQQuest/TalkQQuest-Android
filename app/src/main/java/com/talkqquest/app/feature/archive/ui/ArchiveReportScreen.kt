package com.talkqquest.app.feature.archive.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
import com.talkqquest.app.core.designsystem.component.rememberHapticTick
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

private fun tierEnglishColor(name: String): Color = when (name) {
    "브론즈" -> Color(0xFFB87333)
    "실버" -> Color(0xFFC0C7D1)
    "골드" -> Color(0xFFD4A72C)
    "플래티넘", "플레티넘" -> Color(0xFF8B7FF0)
    "다이아", "다이아몬드" -> Color(0xFF45C4E8)
    "마스터" -> Color(0xFFA05CDA)
    else -> Gray600
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
    var tierAutoTrigger by remember { mutableStateOf(0) }
    val tick = rememberHapticTick()

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
            Box(modifier = Modifier.fillMaxWidth().height(44.dp).padding(horizontal = 4.dp)) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .clickable(onClick = { tick(); onBackClick() }),
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
                    autoTrigger = tierAutoTrigger
                )

                Spacer(Modifier.height(13.dp))

                CompetencyCard(
                    competencies = growthData.competencies,
                    onCompletionAnimationFinished = {
                        tierAutoTrigger++
                    }
                )

                Spacer(Modifier.height(60.dp))
            }
        }

        if (showTierHelp) {
            TierPromotionSheet(
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
    autoTrigger: Int
) {
    val tierVisualShine = remember { Animatable(0f) }
    var playTierAnimation by remember { mutableStateOf<() -> Unit>({}) }

    suspend fun playTierVisualShine() {
        tierVisualShine.snapTo(0f)
        tierVisualShine.animateTo(1f, tween(300, easing = LinearEasing))
        tierVisualShine.snapTo(0f)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .softShadow(color = Gray1000.copy(alpha = 0.01f), offsetY = 8.dp, blur = 24.dp, cornerRadius = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(White)
            .padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 16.dp),
    ) {
        val tierInfoInteraction = remember { MutableInteractionSource() }
        val tierInfoPressed by tierInfoInteraction.collectIsPressedAsState()
        var tierInfoClickAnimating by remember { mutableStateOf(false) }
        val tierInfoCoroutineScope = rememberCoroutineScope()
        val tierInfoVisuallyPressed = tierInfoPressed || tierInfoClickAnimating

        val tierInfoScale by animateFloatAsState(targetValue = if (tierInfoVisuallyPressed) 0.88f else 1f, animationSpec = tween(durationMillis = if (tierInfoVisuallyPressed) 90 else 140, easing = FastOutSlowInEasing), label = "tierInfoPressScale")
        val tierInfoDepth by animateFloatAsState(targetValue = if (tierInfoVisuallyPressed) 2f else 0f, animationSpec = tween(durationMillis = if (tierInfoVisuallyPressed) 90 else 140, easing = FastOutSlowInEasing), label = "tierInfoPressDepth")
        val tierInfoColor by animateColorAsState(targetValue = if (tierInfoVisuallyPressed) Gray700 else Gray500, animationSpec = tween(durationMillis = if (tierInfoVisuallyPressed) 90 else 140, easing = FastOutSlowInEasing), label = "tierInfoPressTextColor")
        val tierInfoIconColor by animateColorAsState(targetValue = if (tierInfoVisuallyPressed) Gray600 else Gray400, animationSpec = tween(durationMillis = if (tierInfoVisuallyPressed) 90 else 140, easing = FastOutSlowInEasing), label = "tierInfoPressIconColor")
        val tierInfoDepthPx = with(LocalDensity.current) { tierInfoDepth.dp.toPx() }

        Row(
            modifier = Modifier
                .graphicsLayer { scaleX = tierInfoScale; scaleY = tierInfoScale; translationY = tierInfoDepthPx }
                .clickable(
                    interactionSource = tierInfoInteraction,
                    indication = null,
                    onClick = {
                        if (!tierInfoClickAnimating) {
                            tierInfoClickAnimating = true
                            tierInfoCoroutineScope.launch {
                                delay(100)
                                onInfoClick()
                                tierInfoClickAnimating = false
                            }
                        }
                    },
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "실전 티어", style = TqType.LabelL.figma(), color = tierInfoColor)
            Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                Icon(painter = painterResource(R.drawable.ic_notification_info), contentDescription = "안내", tint = tierInfoIconColor, modifier = Modifier.size(14.dp))
            }
        }

        Row(
            modifier = Modifier
                .drawWithContent {
                    drawContent()
                    if (tierVisualShine.value > 0f) {
                        val centerX = size.width * tierVisualShine.value
                        val beamWidth = size.width * 0.19f
                        drawRect(
                            brush = Brush.linearGradient(
                                colors = listOf(Color.Transparent, White.copy(alpha = 0.86f), Color.Transparent),
                                start = Offset(centerX - beamWidth, size.height),
                                end = Offset(centerX + beamWidth, 0f),
                            ),
                            blendMode = BlendMode.SrcAtop,
                        )
                    }
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { playTierAnimation() },
                ),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(painter = painterResource(tierEmblemSmallRes(tierName)), contentDescription = null, modifier = Modifier.size(55.dp))
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                TierNameAnimation(
                    tierName = tierName,
                    onShineStart = ::playTierVisualShine,
                    onPlayReady = { playTierAnimation = it },
                    autoTrigger = autoTrigger,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(3) { i ->
                        Icon(painter = painterResource(R.drawable.ic_tier_star), contentDescription = null, tint = if (i < tierStars) StarYellow else Gray300, modifier = Modifier.size(15.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        if (nextTierName.isNotBlank()) {
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
}

@Composable
private fun TierNameAnimation(
    tierName: String,
    onShineStart: suspend () -> Unit,
    onPlayReady: ((() -> Unit) -> Unit),
    autoTrigger: Int,
) {
    val englishName = when (tierName) {
        "브론즈" -> "Bronze"
        "실버" -> "Silver"
        "골드" -> "Gold"
        "플래티넘", "플레티넘" -> "Platinum"
        "다이아", "다이아몬드" -> "Diamond"
        "마스터" -> "Master"
        else -> null
    }
    var phase by remember { mutableStateOf(0) }
    var running by remember { mutableStateOf(false) }
    val restoreProgress = remember { Animatable(0f) }
    val textWipeProgress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val titleStyle = TqType.BodyL.figma().copy(fontWeight = FontWeight.Medium)
    val englishColor = tierEnglishColor(tierName)
    val restoreOffsetPx = with(LocalDensity.current) { 5.dp.toPx() }

    fun play() {
        if (englishName == null || running) return
        scope.launch {
            running = true
            try {
                textWipeProgress.snapTo(0f)
                phase = 5
                textWipeProgress.animateTo(targetValue = 1f, animationSpec = tween(durationMillis = 540, easing = LinearEasing))
                withFrameNanos { }
                phase = 3
                onShineStart()
                delay(200)
                phase = 4
                restoreProgress.snapTo(0f)
                restoreProgress.animateTo(targetValue = 1f, animationSpec = tween(220, easing = FastOutSlowInEasing))
                phase = 0
                restoreProgress.snapTo(0f)
                textWipeProgress.snapTo(0f)
            } finally {
                running = false
            }
        }
    }
    SideEffect { onPlayReady(::play) }
    LaunchedEffect(autoTrigger) { if (autoTrigger > 0) play() }

    Box(
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = ::play,
        ),
    ) {
        when (phase) {
            3 -> Text(text = englishName.orEmpty(), style = titleStyle, color = englishColor)
            4 -> {
                Text(text = englishName.orEmpty(), style = titleStyle, color = englishColor, modifier = Modifier.graphicsLayer { alpha = 1f - restoreProgress.value; translationY = -restoreOffsetPx * restoreProgress.value })
                Text(text = tierName, style = titleStyle, color = Gray800, modifier = Modifier.graphicsLayer { alpha = restoreProgress.value; translationY = restoreOffsetPx * (1f - restoreProgress.value) })
            }
            5 -> TierNameWipeText(koreanName = tierName, englishName = englishName.orEmpty(), progress = textWipeProgress.value, style = titleStyle, koreanColor = Gray800, englishColor = englishColor)
            else -> Text(text = tierName, style = titleStyle, color = Gray800)
        }
    }
}

@Composable
private fun TierNameWipeText(koreanName: String, englishName: String, progress: Float, style: TextStyle, koreanColor: Color, englishColor: Color) {
    val split = koreanName.length.toFloat() / (koreanName.length + englishName.length)
    Box {
        TierNameWipeLayer(text = koreanName, progress = (progress / split).coerceIn(0f, 1f), appearing = false, style = style, color = koreanColor)
        TierNameWipeLayer(text = englishName, progress = ((progress - split) / (1f - split)).coerceIn(0f, 1f), appearing = true, style = style, color = englishColor)
    }
}

@Composable
private fun TierNameWipeLayer(text: String, progress: Float, appearing: Boolean, style: TextStyle, color: Color) {
    val timeline = progress.coerceIn(0f, 1f) * text.length
    val animatedText = buildAnnotatedString {
        text.forEachIndexed { index, character ->
            val animationOrder = if (appearing) index else text.lastIndex - index
            val localProgress = (timeline - animationOrder).coerceIn(0f, 1f)
            val easedProgress = localProgress * localProgress * (3f - 2f * localProgress)
            val alpha = if (appearing) easedProgress else 1f - easedProgress
            withStyle(SpanStyle(color = color.copy(alpha = alpha))) { append(character) }
        }
    }
    Text(text = animatedText, style = style, softWrap = false, maxLines = 1, modifier = Modifier.wrapContentWidth(Alignment.Start, unbounded = true))
}

@Composable
private fun CompetencyCard(
    competencies: List<Competency>,
    onCompletionAnimationFinished: () -> Unit
) {
    val byAxis = competencies.associateBy { it.axis }
    val top = byAxis[CompetencyAxis.KINDNESS]
    val right = byAxis[CompetencyAxis.INITIATIVE]
    val bottom = byAxis[CompetencyAxis.EMPATHY]
    val left = byAxis[CompetencyAxis.QUESTION_LINK]

    var radarEntered by remember { mutableStateOf(false) }
    var gainsVisible by remember { mutableStateOf(false) }
    var legendScoresVisible by remember { mutableStateOf(false) }
    var completionChecksVisible by remember { mutableStateOf(false) }

    val radarScale by animateFloatAsState(targetValue = if (radarEntered) 1f else 0f, animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing), label = "competencyRadarEnter")
    val gainAlpha by animateFloatAsState(targetValue = if (gainsVisible) 1f else 0f, animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing), label = "competencyGainsEnter")
    val gainOffsetY by animateFloatAsState(targetValue = if (gainsVisible) 0f else 10f, animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing), label = "competencyGainsRise")

    LaunchedEffect(Unit) {
        radarEntered = false; gainsVisible = false; legendScoresVisible = false; completionChecksVisible = false
        withFrameNanos { }
        radarEntered = true
        delay(400)
        gainsVisible = true
        delay(360)
        legendScoresVisible = true
        delay(720)
        completionChecksVisible = true
        delay(340)
        onCompletionAnimationFinished()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .softShadow(color = Gray1000.copy(alpha = 0.01f), offsetY = 8.dp, blur = 24.dp, cornerRadius = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(White)
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(text = "핵심 역량", style = TqType.BodyL.figma().copy(fontWeight = FontWeight.Medium), color = Color.Black, modifier = Modifier.padding(start = 16.dp))

            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (top != null) AxisLabel(top.label, top.gain, gainAlpha, gainOffsetY)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                ) {
                    if (left != null) AxisLabel(left.label, left.gain, gainAlpha, gainOffsetY)

                    RadarChart(
                        top = frac(top), right = frac(right), bottom = frac(bottom), left = frac(left),
                        dataScale = radarScale,
                        modifier = Modifier.size(176.dp),
                    )

                    if (right != null) AxisLabel(right.label, right.gain, gainAlpha, gainOffsetY)
                }
                if (bottom != null) AxisLabel(bottom.label, bottom.gain, gainAlpha, gainOffsetY)
            }
        }

        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            competencies.forEach { c -> LegendRow(c, legendScoresVisible, completionChecksVisible) }
        }
    }
}

private fun frac(c: Competency?): Float = if (c == null || c.maxScore == 0) 0f else (c.score.toFloat() / c.maxScore).coerceIn(0f, 1f)

@Composable
private fun AxisLabel(label: String, gain: Int, gainAlpha: Float = 1f, gainOffsetY: Float = 0f) {
    Column(modifier = Modifier.size(width = 52.dp, height = 42.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = TqType.BodyM.figma(), color = Gray800, softWrap = false, maxLines = 1, modifier = Modifier.wrapContentWidth(Alignment.CenterHorizontally, unbounded = true))
        if (gain > 0) {
            Text(text = "+$gain", style = TqType.LabelL.figma(), color = Primary600, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().graphicsLayer { alpha = gainAlpha; translationY = gainOffsetY })
        } else {
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun RadarChart(
    top: Float, right: Float, bottom: Float, left: Float,
    dataScale: Float = 1f,
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
            val diamond = Path().apply { moveTo(cx, cy - rr); lineTo(cx + rr, cy); lineTo(cx, cy + rr); lineTo(cx - rr, cy); close() }
            drawPath(diamond, color = Gray300, style = gridStroke)
        }
        drawLine(Gray300, Offset(cx, cy - r), Offset(cx, cy + r), strokeWidth = 1.dp.toPx())
        drawLine(Gray300, Offset(cx - r, cy), Offset(cx + r, cy), strokeWidth = 1.dp.toPx())

        val pTop = Offset(cx, cy - r * top * dataScale)
        val pRight = Offset(cx + r * right * dataScale, cy)
        val pBottom = Offset(cx, cy + r * bottom * dataScale)
        val pLeft = Offset(cx - r * left * dataScale, cy)
        val data = Path().apply { moveTo(pTop.x, pTop.y); lineTo(pRight.x, pRight.y); lineTo(pBottom.x, pBottom.y); lineTo(pLeft.x, pLeft.y); close() }

        drawPath(data, color = Primary200.copy(alpha = 0.6f))
        drawPath(data, color = Primary600.copy(alpha = 0.6f), style = Stroke(width = 1.dp.toPx()))

        listOf(pTop, pRight, pBottom, pLeft).forEach { drawCircle(Primary600, radius = dot * dataScale, center = it) }
    }
}

@Composable
private fun LegendRow(competency: Competency, showFinalScore: Boolean, showCompletionCheck: Boolean) {
    val startScore = (competency.score - competency.gain).coerceAtLeast(0)
    val displayedScore by animateIntAsState(targetValue = if (showFinalScore) competency.score else startScore, animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing), label = "competencyScore")
    val becomesMaxed = competency.score >= competency.maxScore

    val checkFillScale by animateFloatAsState(targetValue = if (showCompletionCheck && becomesMaxed) 1f else 0f, animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing), label = "competencyCheckFill")
    val checkAlpha by animateFloatAsState(targetValue = if (showCompletionCheck && becomesMaxed) 1f else 0f, animationSpec = tween(durationMillis = 160, delayMillis = 140, easing = FastOutSlowInEasing), label = "competencyCheckMark")

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (becomesMaxed) {
                Box(
                    modifier = Modifier.size(19.dp).clip(CircleShape).border(1.dp, Gray300, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(modifier = Modifier.size(19.dp).graphicsLayer { scaleX = checkFillScale; scaleY = checkFillScale }.clip(CircleShape).background(Primary600))
                    Icon(painter = painterResource(R.drawable.ic_benefit_check), contentDescription = null, tint = White, modifier = Modifier.size(13.dp).graphicsLayer { alpha = checkAlpha })
                }
            } else {
                Box(modifier = Modifier.size(19.dp).clip(CircleShape).border(1.dp, Gray300, CircleShape))
            }
            Text(text = competency.legendLabel, style = TqType.BodyM.figma(), color = Gray800)
        }
        Text(
            text = buildAnnotatedString {
                append(displayedScore.toString())
                withStyle(SpanStyle(color = Gray400, fontWeight = FontWeight.Normal)) { append(" / ${competency.maxScore}") }
            },
            style = TqType.LabelL.figma(),
            color = if (displayedScore >= competency.maxScore) Primary600 else Gray600,
        )
    }
}

@Composable
private fun TierPromotionSheet(
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
                .softShadow(color = Gray1000.copy(alpha = 0.06f), offsetY = (-8).dp, blur = 24.dp, cornerRadius = 36.dp)
                .clip(RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp))
                .background(Gray50)
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = {})
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(modifier = Modifier.width(36.dp).height(4.dp).clip(CircleShape).background(Gray600))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(text = "티어 승급 안내", style = TqType.TitleL.figma(), color = Gray800)

                Column(
                    verticalArrangement = Arrangement.spacedBy(7.dp),
                    horizontalAlignment = Alignment.Start,
                ) {
                    HelpRow(title = "핵심 역량 채우기", subtitle = "미션을 통해 대화 역량을 넓혀요") {
                        Box(modifier = Modifier.size(50.dp), contentAlignment = Alignment.Center) {
                            Image(painter = painterResource(R.drawable.img_help_radar), contentDescription = null, modifier = Modifier.size(50.dp))
                        }
                    }
                    ChevronDown(Modifier.align(Alignment.CenterHorizontally))
                    HelpRow(title = "별 획득하기", subtitle = "핵심 역량 당 300점을 모두 채우면 별을 얻어요") {
                        Box(modifier = Modifier.size(50.dp), contentAlignment = Alignment.Center) {
                            Icon(painter = painterResource(R.drawable.ic_tier_star), contentDescription = null, tint = StarYellow, modifier = Modifier.size(44.dp))
                        }
                    }
                    ChevronDown(Modifier.align(Alignment.CenterHorizontally))
                    HelpRow(title = "티어 승급하기", subtitle = "별 3개를 다 모으면 다음 티어로!") {
                        Box(modifier = Modifier.size(50.dp), contentAlignment = Alignment.Center) {
                            Image(painter = painterResource(R.drawable.img_tier_master_s), contentDescription = null, modifier = Modifier.size(50.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HelpRow(title: String, subtitle: String, leading: @Composable () -> Unit) {
    Row(
        modifier = Modifier.height(50.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        leading()
        Column {
            Text(text = title, style = TqType.BodyL.figma().copy(fontWeight = FontWeight.Medium), color = Gray800)
            Text(text = subtitle, style = TqType.BodyM.figma(), color = Gray600, maxLines = 1, softWrap = false)
        }
    }
}

@Composable
private fun ChevronDown(modifier: Modifier = Modifier) {
    Icon(painter = painterResource(R.drawable.ic_chevron_down), contentDescription = null, tint = Primary600, modifier = modifier.size(24.dp))
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