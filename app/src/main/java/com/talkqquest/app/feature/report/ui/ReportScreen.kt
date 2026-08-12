package com.talkqquest.app.feature.report.ui

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
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
import com.talkqquest.app.core.designsystem.component.TierPromotionSheet
import com.talkqquest.app.core.designsystem.component.TqButton
import com.talkqquest.app.feature.report.data.model.Competency
import com.talkqquest.app.feature.report.data.model.CompetencyAxis
import com.talkqquest.app.feature.report.data.model.GrowthTierReport
import com.talkqquest.app.feature.report.viewmodel.ReportUiState
import com.talkqquest.app.feature.report.viewmodel.ReportViewModel

// ── 성장 리포트 (CSS UI-14 "성장 리포트" 프레임 전사 — 성장 티어 시스템 시각화) ──
// 실전 티어 카드(티어 휘장 + 별) + 핵심 역량 카드(마름모 4축 레이더 + 4행 범례).
// 주간 비교 리포트는 홈 알림 → 모달/보관함으로 빠져 이 화면에선 탭 없이 단독.
// 저장 시트(ReportSaveSheet)는 성장·주간 저장을 모두 담당 — 그대로 유지.
// 실전 티어 info 아이콘 → "티어 승급 안내" 바텀시트.

private val StarYellow = Color(0xFFF9AC17) // YELLOW_star

private val FullLeading = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)
private fun TextStyle.figma(): TextStyle = copy(lineHeightStyle = FullLeading)

// 티어 이름 → 성장 리포트용 소형(s) 휘장 리소스
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
fun ReportScreen(
    onBack: () -> Unit = {},
    viewModel: ReportViewModel = hiltViewModel(),
    onSheetTopChange: (Float?) -> Unit = {}, // 저장 시트가 하단 네비를 덮는 동안 네비 가림
    // ── C담당(아카이브) 연결 지점 — 저장 시트 안에서 아카이브로 나가는 두 경로 ──
    onArchiveClick: () -> Unit = {}, // 시트 "보관함 >" → 아카이브 보관함(리포트 탭)
    onReportClick: (String) -> Unit = {}, // 시트의 저장된 리포트 카드 → 보관함 리포트 상세
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ReportScreen(
        uiState = uiState,
        onBack = onBack,
        onRetry = viewModel::loadReports,
        onSaveReport = viewModel::saveReport,
        onToggleReportSave = viewModel::toggleReportSave,
        onDismissSaveSheet = viewModel::dismissSaveSheet,
        onSheetTopChange = onSheetTopChange,
        onArchiveClick = onArchiveClick,
        onReportClick = onReportClick,
    )
}

@Composable
private fun ReportScreen(
    uiState: ReportUiState,
    onBack: () -> Unit = {},
    onRetry: () -> Unit = {},
    onSaveReport: (String) -> Unit = {},
    onToggleReportSave: (String) -> Unit = {},
    onDismissSaveSheet: () -> Unit = {},
    onSheetTopChange: (Float?) -> Unit = {},
    onArchiveClick: () -> Unit = {},
    onReportClick: (String) -> Unit = {},
) = FitDesign { // 작은 화면에선 디자인(393x852) 통째 축소 — 다른 화면들과 동일
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50), // 페이지 배경 Gray/50 BG
        contentAlignment = Alignment.Center,
    ) {
        when {
            uiState.isLoading -> CircularProgressIndicator(color = Primary600)

            uiState.errorMessage != null -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = uiState.errorMessage, style = TqType.BodyM.figma(), color = Error)
                    Spacer(Modifier.height(16.dp))
                    TqButton(text = "다시 시도", onClick = onRetry)
                }
            }

            uiState.growth != null ->
                // "리포트 저장하기"를 누르면 화면 위로 "저장됨" 시트가 올라옴. 표준 시트라 배경 안 어두워지고
                // 뒤 화면도 계속 스크롤 가능 — 미션 저장 시트와 동일. 시트는 성장·주간 저장 공용.
                ReportSaveSheetScaffold(
                    savedReport = uiState.saveSheetReport,
                    recentSavedReports = uiState.savedReports,
                    onDismiss = onDismissSaveSheet,
                    onToggleSave = onToggleReportSave,
                    onSheetTopChange = onSheetTopChange,
                    onArchiveClick = onArchiveClick,
                    onReportClick = onReportClick,
                ) {
                    ReportContent(
                        growth = uiState.growth,
                        onBack = onBack,
                        onSaveClick = onSaveReport,
                    )
                }
        }
    }
}

@Composable
private fun ReportContent(
    growth: GrowthTierReport,
    onBack: () -> Unit,
    onSaveClick: (String) -> Unit = {},
) {
    // info 아이콘 → 티어 승급 안내 바텀시트
    var showTierHelp by remember { mutableStateOf(false) }
    var tierAutoTrigger by remember { mutableStateOf(0) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            Spacer(Modifier.height(8.dp)) // 상태바(40) → 헤더(top 48)

            // 헤더: 뒤로가기(left 0) + "성장 리포트"(중앙)
            Box(modifier = Modifier.fillMaxWidth().height(44.dp)) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_back_chevron),
                        contentDescription = "뒤로가기",
                        tint = Gray500,
                    )
                }
                Text(
                    text = "성장 리포트",
                    style = TqType.TitleL.figma(), // 18/600
                    color = Gray700,
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            Spacer(Modifier.height(5.dp)) // 헤더 끝(92) → 콘텐츠 top 97

            // 카드 묶음 (left 16, gap 13)
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                TierCard(
                    tierName = growth.tierName,
                    tierStars = growth.tierStars,
                    nextStarsNeeded = growth.nextStarsNeeded,
                    nextTierName = growth.nextTierName,
                    onInfoClick = { showTierHelp = true },
                    autoTrigger = tierAutoTrigger,
                )
                Spacer(Modifier.height(13.dp))
                CompetencyCard(
                    competencies = growth.competencies,
                    onCompletionAnimationFinished = { tierAutoTrigger++ },
                )
            }

            Spacer(Modifier.height(9.dp)) // 카드 끝 → 버튼 top 728

            // 리포트 저장하기 (CSS "다음" 버튼 자리 — 라벨은 이전 사용자 결정대로 유지). 누르면 저장 시트.
            TqButton(
                text = "리포트 저장하기",
                onClick = { onSaveClick("growth") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            )
        }

        // 티어 승급 안내 바텀시트(공용) — 항상 컴포즈해 두고 visible로 등장/퇴장을 태운다.
        TierPromotionSheet(
            visible = showTierHelp,
            onDismiss = { showTierHelp = false },
        )
    }
}

// ── 실전 티어 카드 (361x125) ──
@Composable
private fun TierCard(
    tierName: String,
    tierStars: Int,
    nextStarsNeeded: Int,
    nextTierName: String,
    onInfoClick: () -> Unit,
    autoTrigger: Int,
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
            .clip(RoundedCornerShape(20.dp))
            .background(White)
            .padding(start = 16.dp, top = 12.dp, bottom = 12.dp),
    ) {
        // "실전 티어" + info
        val tierInfoInteraction = remember { MutableInteractionSource() }
        val tierInfoPressed by tierInfoInteraction.collectIsPressedAsState()
        var tierInfoClickAnimating by remember { mutableStateOf(false) }
        val tierInfoCoroutineScope = rememberCoroutineScope()
        val tierInfoVisuallyPressed = tierInfoPressed || tierInfoClickAnimating
        val tierInfoScale by animateFloatAsState(
            targetValue = if (tierInfoVisuallyPressed) 0.88f else 1f,
            animationSpec = tween(
                durationMillis = if (tierInfoVisuallyPressed) 90 else 140,
                easing = FastOutSlowInEasing,
            ),
            label = "tierInfoPressScale",
        )
        val tierInfoDepth by animateFloatAsState(
            targetValue = if (tierInfoVisuallyPressed) 2f else 0f,
            animationSpec = tween(
                durationMillis = if (tierInfoVisuallyPressed) 90 else 140,
                easing = FastOutSlowInEasing,
            ),
            label = "tierInfoPressDepth",
        )
        val tierInfoColor by animateColorAsState(
            targetValue = if (tierInfoVisuallyPressed) Gray700 else Gray500,
            animationSpec = tween(
                durationMillis = if (tierInfoVisuallyPressed) 90 else 140,
                easing = FastOutSlowInEasing,
            ),
            label = "tierInfoPressTextColor",
        )
        val tierInfoIconColor by animateColorAsState(
            targetValue = if (tierInfoVisuallyPressed) Gray600 else Gray400,
            animationSpec = tween(
                durationMillis = if (tierInfoVisuallyPressed) 90 else 140,
                easing = FastOutSlowInEasing,
            ),
            label = "tierInfoPressIconColor",
        )
        val tierInfoDepthPx = with(LocalDensity.current) { tierInfoDepth.dp.toPx() }

        Row(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = tierInfoScale
                    scaleY = tierInfoScale
                    translationY = tierInfoDepthPx
                }
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
            Text(
                text = "실전 티어",
                style = TqType.LabelL.figma(),
                color = tierInfoColor,
            ) // 14/500
            Box(
                modifier = Modifier.size(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_notification_info),
                    contentDescription = "실전 티어 안내",
                    tint = tierInfoIconColor,
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        // 휘장·티어명·별을 하나의 묶음으로 두고, 광선도 이 묶음을 한 번에 지난다.
        Row(
            modifier = Modifier
                .drawWithContent {
                    drawContent()
                    if (tierVisualShine.value > 0f) {
                        val centerX = size.width * tierVisualShine.value
                        val beamWidth = size.width * 0.19f
                        drawRect(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    White.copy(alpha = 0.86f),
                                    Color.Transparent,
                                ),
                                start = Offset(centerX - beamWidth, size.height),
                                end = Offset(centerX + beamWidth, 0f),
                            ),
                            blendMode = BlendMode.SrcAtop,
                        )
                    }
                }
                // 휘장·티어명·별 어느 곳을 눌러도 같은 성장 모션을 실행한다.
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { playTierAnimation() },
                ),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(tierEmblemSmallRes(tierName)),
                contentDescription = null,
                modifier = Modifier.size(55.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                TierNameAnimation(
                    tierName = tierName,
                    onShineStart = ::playTierVisualShine,
                    onPlayReady = { playTierAnimation = it },
                    autoTrigger = autoTrigger,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(3) { i ->
                        Icon(
                            painter = painterResource(R.drawable.ic_tier_star),
                            contentDescription = null,
                            tint = if (i < tierStars) StarYellow else Gray300,
                            modifier = Modifier.size(15.dp),
                        )
                    }
                }
            }
        }

        // "별 N개를 더 획득하면 X이에요!"
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = Gray600)) { append("별 ") }
                withStyle(SpanStyle(color = Primary600, fontWeight = FontWeight.Medium)) { append("${nextStarsNeeded}개") }
                withStyle(SpanStyle(color = Gray600)) { append("를 더 획득하면 ") }
                withStyle(SpanStyle(color = Primary600, fontWeight = FontWeight.Medium)) { append(nextTierName) }
                withStyle(SpanStyle(color = Gray600)) { append("이에요!") }
            },
            style = TqType.BodyM.figma(), // 14/400
        )
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
    var phase by remember { mutableStateOf(0) } // 0=한글, 1=삭제, 2=영문 입력, 3=광택
    var charCount by remember { mutableStateOf(tierName.length) }
    var running by remember { mutableStateOf(false) }
    val restoreProgress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val titleStyle = TqType.BodyL.figma().copy(fontWeight = FontWeight.Medium)
    val restoreOffsetPx = with(LocalDensity.current) { 5.dp.toPx() }

    fun play() {
        if (englishName == null || running) return
        scope.launch {
            running = true
            try {
                phase = 1
                for (count in (tierName.length - 1) downTo 0) {
                    charCount = count
                    delay(90)
                }
                phase = 2
                for (count in 1..englishName.length) {
                    charCount = count
                    delay(90)
                }
                phase = 3
                // Gold 완성 즉시 300ms 광선을 실행한 뒤 200ms를 더 유지한다.
                onShineStart()
                delay(200)
                // Gold가 사라지는 동안 골드는 아래에서 제자리로 올라온다.
                phase = 4
                restoreProgress.snapTo(0f)
                restoreProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(220, easing = FastOutSlowInEasing),
                )
                phase = 0
                charCount = tierName.length
                restoreProgress.snapTo(0f)
            } finally {
                running = false
            }
        }
    }
    SideEffect { onPlayReady(::play) }
    LaunchedEffect(autoTrigger) {
        if (autoTrigger > 0) play()
    }

    Box(
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = ::play,
        ),
    ) {
        when (phase) {
            1 -> Text(text = tierName.take(charCount), style = titleStyle, color = Gray800)
            2, 3 -> Text(
                text = englishName?.take(charCount).orEmpty(),
                style = titleStyle,
                color = Color(0xFFD4A72C),
            )
            4 -> {
                Text(
                    text = englishName.orEmpty(),
                    style = titleStyle,
                    color = Color(0xFFD4A72C),
                    modifier = Modifier.graphicsLayer {
                        alpha = 1f - restoreProgress.value
                        translationY = -restoreOffsetPx * restoreProgress.value
                    },
                )
                Text(
                    text = tierName,
                    style = titleStyle,
                    color = Gray800,
                    modifier = Modifier.graphicsLayer {
                        alpha = restoreProgress.value
                        translationY = restoreOffsetPx * (1f - restoreProgress.value)
                    },
                )
            }
            else -> Text(text = tierName, style = titleStyle, color = Gray800)
        }
    }
}

// ── 핵심 역량 카드 (361x484) ──
@Composable
private fun CompetencyCard(
    competencies: List<Competency>,
    onCompletionAnimationFinished: () -> Unit,
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
    val radarScale by animateFloatAsState(
        targetValue = if (radarEntered) 1f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "competencyRadarEnter",
    )
    val gainAlpha by animateFloatAsState(
        targetValue = if (gainsVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "competencyGainsEnter",
    )
    val gainOffsetY by animateFloatAsState(
        targetValue = if (gainsVisible) 0f else 10f,
        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
        label = "competencyGainsRise",
    )

    LaunchedEffect(Unit) {
        radarEntered = true
        delay(400) // 보라 데이터 마름모가 완성된 뒤 +획득 수치를 함께 표시한다.
        gainsVisible = true
        delay(360) // +70 네 개가 위로 올라와 멈출 때까지 기다린다.
        legendScoresVisible = true
        delay(720) // 아래 점수의 상승이 완전히 끝난 뒤에만 만점 체크를 보여 준다.
        completionChecksVisible = true
        delay(340) // 원 채움(240ms)과 체크 등장(140ms 지연 + 160ms)을 모두 마친다.
        onCompletionAnimationFinished()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(White)
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // 제목
        Text(
            text = "핵심 역량",
            style = TqType.BodyL.figma().copy(fontWeight = FontWeight.Medium), // 16/500
            color = Color.Black,
            modifier = Modifier.padding(start = 16.dp),
        )

        // 레이더 + 4축 라벨 (위=친절/왼=질문/오른=주도/아래=공감)
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (top != null) AxisLabel(top.label, top.gain, gainAlpha, gainOffsetY)
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                if (left != null) AxisLabel(left.label, left.gain, gainAlpha, gainOffsetY)
                RadarChart(
                    top = frac(top),
                    right = frac(right),
                    bottom = frac(bottom),
                    left = frac(left),
                    dataScale = radarScale,
                    modifier = Modifier.size(176.dp),
                )
                if (right != null) AxisLabel(right.label, right.gain, gainAlpha, gainOffsetY)
            }
            if (bottom != null) AxisLabel(bottom.label, bottom.gain, gainAlpha, gainOffsetY)
        }

        // 범례 4행
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            competencies.forEach { c ->
                LegendRow(
                    competency = c,
                    showFinalScore = legendScoresVisible,
                    showCompletionCheck = completionChecksVisible,
                )
            }
        }
    }
}

private fun frac(c: Competency?): Float =
    if (c == null || c.maxScore == 0) 0f else (c.score.toFloat() / c.maxScore).coerceIn(0f, 1f)

// 레이더 축 라벨: 라벨 + "+획득" (세로)
@Composable
private fun AxisLabel(
    label: String,
    gain: Int,
    gainAlpha: Float = 1f,
    gainOffsetY: Float = 0f,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = TqType.BodyM.figma(), color = Gray800) // 14/400
        Text(
            text = "+$gain",
            style = TqType.LabelL.figma(),
            color = Primary600,
            modifier = Modifier.graphicsLayer {
                alpha = gainAlpha
                translationY = gainOffsetY
            },
        ) // 14/500
    }
}

// 마름모 4축 레이더. 값(0..1)에 비례해 중심에서 각 축(위/오/아/왼)으로 뻗은 폴리곤.
@Composable
private fun RadarChart(
    top: Float,
    right: Float,
    bottom: Float,
    left: Float,
    dataScale: Float = 1f,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = size.width / 2f - 4.dp.toPx() // 격자 마름모 반경(테두리 여백)
        val dot = size.width * 0.028f // 꼭짓점 점 반경(176일 때 ≈5)

        // 격자 (Gray/300) — Vector.svg 그대로: 동심 마름모 4겹(비율 1/0.75/0.54/0.29) + 수직·수평 축선
        val gridStroke = Stroke(width = 1.dp.toPx())
        listOf(1f, 0.752f, 0.540f, 0.293f).forEach { ring ->
            val rr = r * ring
            val diamond = Path().apply {
                moveTo(cx, cy - rr); lineTo(cx + rr, cy); lineTo(cx, cy + rr); lineTo(cx - rr, cy); close()
            }
            drawPath(diamond, color = Gray300, style = gridStroke)
        }
        drawLine(Gray300, Offset(cx, cy - r), Offset(cx, cy + r), strokeWidth = 1.dp.toPx()) // 수직 축선
        drawLine(Gray300, Offset(cx - r, cy), Offset(cx + r, cy), strokeWidth = 1.dp.toPx()) // 수평 축선

        // 데이터 폴리곤 (Purple/200 채움 + Purple/600 선)
        // 거미줄은 고정. 보라 데이터 마름모만 중심에서 각 축 값까지 자란다.
        val pTop = Offset(cx, cy - r * top * dataScale)
        val pRight = Offset(cx + r * right * dataScale, cy)
        val pBottom = Offset(cx, cy + r * bottom * dataScale)
        val pLeft = Offset(cx - r * left * dataScale, cy)
        val data = Path().apply {
            moveTo(pTop.x, pTop.y); lineTo(pRight.x, pRight.y)
            lineTo(pBottom.x, pBottom.y); lineTo(pLeft.x, pLeft.y); close()
        }
        // Vector 78: Purple/200 채움 + Purple/600 선, 요소 전체 opacity 0.6 → 안쪽 격자가 비쳐 보임
        drawPath(data, color = Primary200.copy(alpha = 0.6f))
        drawPath(data, color = Primary600.copy(alpha = 0.6f), style = Stroke(width = 1.dp.toPx()))

        // 꼭짓점 점 (Purple/600)
        listOf(pTop, pRight, pBottom, pLeft).forEach {
            drawCircle(Primary600, radius = dot * dataScale, center = it)
        }
    }
}

// 범례 행: [체크닷 + 라벨] ... [점수 / 만점]
@Composable
private fun LegendRow(
    competency: Competency,
    showFinalScore: Boolean,
    showCompletionCheck: Boolean,
) {
    val startScore = (competency.score - competency.gain).coerceAtLeast(0)
    val displayedScore by animateIntAsState(
        targetValue = if (showFinalScore) competency.score else startScore,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "competencyScore",
    )
    val becomesMaxed = competency.score >= competency.maxScore
    val checkFillScale by animateFloatAsState(
        targetValue = if (showCompletionCheck && becomesMaxed) 1f else 0f,
        animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
        label = "competencyCheckFill",
    )
    val checkAlpha by animateFloatAsState(
        targetValue = if (showCompletionCheck && becomesMaxed) 1f else 0f,
        animationSpec = tween(durationMillis = 160, delayMillis = 140, easing = FastOutSlowInEasing),
        label = "competencyCheckMark",
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // 원의 자리는 항상 고정. 만점 항목만 점수 상승 뒤 중앙에서 채워지고 체크가 나타난다.
            if (becomesMaxed) {
                Box(
                    modifier = Modifier
                        .size(19.dp)
                        .clip(CircleShape)
                        .border(1.dp, Gray300, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(19.dp)
                            .graphicsLayer {
                                scaleX = checkFillScale
                                scaleY = checkFillScale
                            }
                            .clip(CircleShape)
                            .background(Primary600),
                    )
                    Icon(
                        painter = painterResource(R.drawable.ic_benefit_check),
                        contentDescription = null,
                        tint = White,
                        modifier = Modifier
                            .size(13.dp)
                            .graphicsLayer { alpha = checkAlpha },
                    )
                }
            } else {
                Box(modifier = Modifier.size(19.dp).clip(CircleShape).border(1.dp, Gray300, CircleShape))
            }
            Text(text = competency.legendLabel, style = TqType.BodyM.figma(), color = Gray800) // 14/400
        }
        // 점수 / 만점 — 슬래시 양쪽 균등 간격
        Text(
            text = buildAnnotatedString {
                append(displayedScore.toString())
                withStyle(SpanStyle(color = Gray400, fontWeight = FontWeight.Normal)) { append(" / ${competency.maxScore}") }
            },
            style = TqType.LabelL.figma(), // 14/500 (점수), 슬래시부는 span으로 400
            color = if (displayedScore >= competency.maxScore) Primary600 else Gray600,
        )
    }
}


@Preview(name = "성장 리포트", showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun GrowthReportPreview() {
    TalkQQuestTheme {
        ReportContent(
            growth = GrowthTierReport(
                tierName = "골드",
                tierStars = 2,
                nextStarsNeeded = 1,
                nextTierName = "플래티넘",
                competencies = listOf(
                    Competency(CompetencyAxis.KINDNESS, "친절한 태도", "친절한 태도", 300, 70),
                    Competency(CompetencyAxis.INITIATIVE, "대화 주도", "대화 주도", 200, 70),
                    Competency(CompetencyAxis.EMPATHY, "공감 표현", "공감 능력", 100, 70),
                    Competency(CompetencyAxis.QUESTION_LINK, "질문 연결성", "질문 연결성", 300, 70),
                ),
            ),
            onBack = {},
        )
    }
}
