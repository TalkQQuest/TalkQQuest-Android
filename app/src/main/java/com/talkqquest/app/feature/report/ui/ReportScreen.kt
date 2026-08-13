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
import androidx.compose.foundation.layout.wrapContentWidth
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
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import com.talkqquest.app.core.designsystem.LocalStatusBarCompensation
import com.talkqquest.app.core.designsystem.coverStatusBarCompensation
import kotlinx.coroutines.flow.first

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

// 영문 티어 전환도 현재 휘장의 금속색 계열을 쓴다. 홈 티어와 같은 기준값이다.
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
    var completedDiamondTrigger by remember { mutableStateOf(0) }

    // 승급 모션(꼭짓점 집결 → 별 → 비행 → 딤·휘장). 마름모를 완성한 회차에만 재생된다.
    val motion = rememberTierMotion()
    val promotion = growth.promotion
    // 마름모 중심과 별 자리는 서로 다른 카드에 있다. 오버레이가 쓰는 좌표계로 환산하려면
    // 두 좌표를 이 루트 Box 기준으로 옮겨야 한다.
    var motionRoot by remember { mutableStateOf<LayoutCoordinates?>(null) }
    // 빈 곳을 누르면 연출을 처음부터 다시 본다.
    var replayKey by remember(growth) { mutableStateOf(0) }
    // 모션이 끝나면 카드가 승급 후 상태로 갈아탄다(그 전까지는 직전 상태).
    var swapped by remember(growth) { mutableStateOf(false) }
    val shownTierName = if (swapped && promotion != null) promotion.newTierName else growth.tierName
    val shownStars = when {
        swapped && promotion != null -> promotion.newTierStars
        motion.litStars >= 0 -> motion.litStars
        else -> growth.tierStars
    }
    val shownNextStarsNeeded =
        if (swapped && promotion != null) promotion.newNextStarsNeeded else growth.nextStarsNeeded
    val shownNextTierName =
        if (swapped && promotion != null) promotion.newNextTierName else growth.nextTierName
    // 승급 후에는 마름모도 리셋된 값으로 내려앉는다 — 왜 점수가 줄었는지가 눈에 보이게.
    val shownCompetencies = if (swapped && promotion != null) {
        growth.competencies.mapIndexed { i, c ->
            c.copy(score = promotion.afterScores.getOrElse(i) { c.score }, gain = 0, startScore = c.score)
        }
    } else {
        growth.competencies
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { motionRoot = it }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { replayKey++ },
            ),
    ) {
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
                    tierName = shownTierName,
                    tierStars = shownStars,
                    nextStarsNeeded = shownNextStarsNeeded,
                    nextTierName = shownNextTierName,
                    onInfoClick = { showTierHelp = true },
                    autoTrigger = tierAutoTrigger,
                    completedDiamondTrigger = completedDiamondTrigger,
                    landPunchSlot = if (motion.litStars > 0) motion.litStars - 1 else -1,
                    landPunch = motion.landPunch.value,
                    onStarSlotsPositioned = { slots, coords ->
                        val root = motionRoot ?: return@TierCard
                        motion.starSlots = slots.map { root.localPositionOf(coords, it) }
                    },
                )
                Spacer(Modifier.height(13.dp))
                CompetencyCard(
                    competencies = shownCompetencies,
                    completedDiamondThisReport = growth.completedDiamondThisReport,
                    onCompletionAnimationFinished = {
                        tierAutoTrigger++
                        // 승급 모션이 별을 직접 날려 꽂으므로, 그때는 별이 제자리에서 튀어나오는
                        // 연출(completedDiamondTrigger)을 겹쳐 재생하지 않는다.
                        if (growth.completedDiamondThisReport && promotion == null) completedDiamondTrigger++
                    },
                    replayKey = replayKey,
                    convergeProgress = motion.converge.value,
                    onRadarCenterPositioned = { center, coords ->
                        val root = motionRoot ?: return@CompetencyCard
                        motion.radarCenter = root.localPositionOf(coords, center)
                    },
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

        // 승급 모션 오버레이 — 날아가는 별과 딤·휘장은 카드 위에 그려야 해서 최상단에 둔다.
        if (promotion != null) {
            val scope = rememberCoroutineScope()
            TierPromotionOverlay(
                motion = motion,
                newTierName = promotion.newTierName,
                newTierEmblemRes = tierEmblemSmallRes(promotion.newTierName),
                onDismiss = { scope.launch { motion.dismissCelebration(); motion.settle() } },
                // FitDesign이 위쪽에 넣어 둔 상태바 보정분까지 딤이 덮게 한다(승급 안내 시트와 동일).
                modifier = Modifier.coverStatusBarCompensation(LocalStatusBarCompensation.current),
            )
            // 만점 체크와 좌표 확보를 "안에서" 기다린다. 이 둘을 key로 두면 재생 도중 조건이
            // 다시 평가되며 코루틴이 취소돼 축하 화면이 걷히지 않은 채 멈춘다.
            LaunchedEffect(growth, replayKey) {
                val base = tierAutoTrigger // 이번 회차의 만점 체크가 끝나는 시점만 기다린다
                motion.reset()
                swapped = false
                snapshotFlow { tierAutoTrigger > base && motion.ready }.first { it }
                delay(220) // 체크가 다 켜진 걸 눈으로 확인할 틈
                motion.play(
                    slot = promotion.slotIndex,
                    isTierUp = promotion.isTierUp,
                    onTierSwap = { swapped = true },
                )
                if (promotion.isTierUp) {
                    delay(1800) // 휘장을 보고 있을 시간(탭하면 바로 닫힌다)
                    motion.dismissCelebration()
                } else {
                    swapped = true
                }
                motion.settle() // 새 마름모가 다시 피어나며 끝난다
            }
        }
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
    completedDiamondTrigger: Int,
    // 승급 모션용 — 방금 별이 박힌 자리(-1이면 없음)와 그 튕김 진행값.
    landPunchSlot: Int = -1,
    landPunch: Float = 0f,
    // 별 3칸의 중심 좌표를 이 Row 기준으로 올려 준다(오버레이가 루트 좌표로 환산).
    onStarSlotsPositioned: (List<Offset>, LayoutCoordinates) -> Unit = { _, _ -> },
) {
    val tierVisualShine = remember { Animatable(0f) }
    val completedStarScale = remember { Animatable(1f) }
    val completedStarAlpha = remember { Animatable(1f) }
    var playTierAnimation by remember { mutableStateOf<() -> Unit>({}) }

    suspend fun playTierVisualShine() {
        tierVisualShine.snapTo(0f)
        tierVisualShine.animateTo(1f, tween(300, easing = LinearEasing))
        tierVisualShine.snapTo(0f)
    }

    LaunchedEffect(completedDiamondTrigger) {
        if (completedDiamondTrigger == 0) return@LaunchedEffect
        // 마지막 빈 별이 중심에서 생겨나 제자리 크기로 안착한다.
        completedStarAlpha.snapTo(0f)
        completedStarScale.snapTo(0.15f)
        completedStarAlpha.animateTo(1f, tween(120, easing = FastOutSlowInEasing))
        completedStarScale.animateTo(1.18f, tween(220, easing = FastOutSlowInEasing))
        completedStarScale.animateTo(1f, tween(110, easing = FastOutSlowInEasing))
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.onGloballyPositioned { coords ->
                        // 별 하나 15dp + 간격 4dp. 각 칸의 중심을 Row 좌표계로 계산한다.
                        val h = coords.size.height.toFloat()
                        val step = coords.size.width.toFloat() / 3f
                        onStarSlotsPositioned(
                            List(3) { i -> Offset(step * i + step / 2f, h / 2f) },
                            coords,
                        )
                    },
                ) {
                    repeat(3) { i ->
                        val isNewlyCompletedStar = i == tierStars - 1
                        // 날아온 별이 박히는 칸은 한 번 크게 튕긴다.
                        val punch = if (i == landPunchSlot) 1f + landPunch * 0.55f else 1f
                        Icon(
                            painter = painterResource(R.drawable.ic_tier_star),
                            contentDescription = null,
                            tint = if (i < tierStars) StarYellow else Gray300,
                            modifier = Modifier
                                .size(15.dp)
                                .graphicsLayer {
                                    if (isNewlyCompletedStar) {
                                        scaleX = completedStarScale.value
                                        scaleY = completedStarScale.value
                                        alpha = completedStarAlpha.value
                                    }
                                    if (punch != 1f) {
                                        scaleX = punch
                                        scaleY = punch
                                    }
                                },
                        )
                    }
                }
            }
        }

        // "별 N개를 더 획득하면 X이에요!" — 마스터는 위 티어가 없어 이 줄 자체를 그리지 않는다.
        if (nextTierName.isNotBlank()) {
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
    var phase by remember { mutableStateOf(0) } // 0=한글, 3=광택, 4=한글 복귀, 5=한영 전환
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
                textWipeProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        // 이름 길이와 무관하게 기존 골드 → Gold 전환 속도로 통일한다.
                        durationMillis = 540,
                        easing = LinearEasing,
                    ),
                )
                // 영문 전체가 그려진 프레임을 먼저 노출한 뒤 광선 단계로 넘어간다.
                withFrameNanos { }
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
                restoreProgress.snapTo(0f)
                textWipeProgress.snapTo(0f)
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
            3 -> Text(
                text = englishName.orEmpty(),
                style = titleStyle,
                color = englishColor,
            )
            4 -> {
                Text(
                    text = englishName.orEmpty(),
                    style = titleStyle,
                    color = englishColor,
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
            5 -> TierNameWipeText(
                koreanName = tierName,
                englishName = englishName.orEmpty(),
                progress = textWipeProgress.value,
                style = titleStyle,
                koreanColor = Gray800,
                englishColor = englishColor,
            )
            else -> Text(text = tierName, style = titleStyle, color = Gray800)
        }
    }
}

@Composable
private fun TierNameWipeText(
    koreanName: String,
    englishName: String,
    progress: Float,
    style: TextStyle,
    koreanColor: Color,
    englishColor: Color,
) {
    val split = koreanName.length.toFloat() / (koreanName.length + englishName.length)
    Box {
        TierNameWipeLayer(
            text = koreanName,
            progress = (progress / split).coerceIn(0f, 1f),
            appearing = false,
            style = style,
            color = koreanColor,
        )
        TierNameWipeLayer(
            text = englishName,
            progress = ((progress - split) / (1f - split)).coerceIn(0f, 1f),
            appearing = true,
            style = style,
            color = englishColor,
        )
    }
}

@Composable
private fun TierNameWipeLayer(
    text: String,
    progress: Float,
    appearing: Boolean,
    style: TextStyle,
    color: Color,
) {
    val timeline = progress.coerceIn(0f, 1f) * text.length
    val animatedText = buildAnnotatedString {
        text.forEachIndexed { index, character ->
            val animationOrder = if (appearing) index else text.lastIndex - index
            val localProgress = (timeline - animationOrder).coerceIn(0f, 1f)
            val easedProgress = localProgress * localProgress * (3f - 2f * localProgress)
            val alpha = if (appearing) easedProgress else 1f - easedProgress
            withStyle(SpanStyle(color = color.copy(alpha = alpha))) {
                append(character)
            }
        }
    }
    Text(
        text = animatedText,
        style = style,
        softWrap = false,
        maxLines = 1,
        modifier = Modifier.wrapContentWidth(Alignment.Start, unbounded = true),
    )
}

// ── 핵심 역량 카드 (361x484) ──
@Composable
private fun CompetencyCard(
    competencies: List<Competency>,
    completedDiamondThisReport: Boolean,
    onCompletionAnimationFinished: () -> Unit,
    // 값이 바뀔 때마다 등장 연출을 처음부터 다시 재생한다.
    replayKey: Int = 0,
    // 승급 모션 — 1이면 네 꼭짓점이 마름모 중심으로 모인다.
    convergeProgress: Float = 0f,
    onRadarCenterPositioned: (Offset, LayoutCoordinates) -> Unit = { _, _ -> },
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
    val completionEnergy = remember { Animatable(0f) }
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
    // 아래 점수가 올라가는 것과 같은 박자로 마름모도 직전 값에서 이번 값까지 자란다.
    val fillProgress by animateFloatAsState(
        targetValue = if (legendScoresVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "competencyRadarFill",
    )

    suspend fun playCompletionSequence() {
        radarEntered = false
        gainsVisible = false
        legendScoresVisible = false
        completionChecksVisible = false
        completionEnergy.snapTo(0f)
        withFrameNanos { }
        radarEntered = true
        delay(400) // 보라 데이터 마름모가 완성된 뒤 +획득 수치를 함께 표시한다.
        gainsVisible = true
        delay(360) // +70 네 개가 위로 올라와 멈출 때까지 기다린다.
        legendScoresVisible = true
        delay(720) // 아래 점수의 상승이 완전히 끝난 뒤에만 만점 체크를 보여 준다.
        completionChecksVisible = true
        delay(340) // 원 채움(240ms)과 체크 등장(140ms 지연 + 160ms)을 모두 마친다.
        // 체크 직후 별도로 꼭짓점을 모으던 completionEnergy 연출은 제거한다.
        // 마름모 집결은 이어지는 TierMotion.converge에서 한 번만 재생한다.
        onCompletionAnimationFinished()
    }

    LaunchedEffect(replayKey) {
        playCompletionSequence()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(White)
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // CSS Frame 427321736: 제목과 레이더 묶음 사이 간격 16dp.
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "핵심 역량",
                style = TqType.BodyL.figma().copy(fontWeight = FontWeight.Medium), // 16/500
                color = Color.Black,
                modifier = Modifier.padding(start = 16.dp),
            )

            // CSS Frame 427321711: 위·가운데·아래 축 사이 간격 12dp.
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (top != null) AxisLabel(top.label, top.gain, gainAlpha, gainOffsetY)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    // CSS: 52dp 라벨 + 12dp + 176dp 마름모 + 12dp + 52dp 라벨을 중앙 정렬.
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                ) {
                    if (left != null) AxisLabel(left.label, left.gain, gainAlpha, gainOffsetY)
                    RadarChart(
                        top = fracAt(top, fillProgress),
                        right = fracAt(right, fillProgress),
                        bottom = fracAt(bottom, fillProgress),
                        left = fracAt(left, fillProgress),
                        dataScale = radarScale,
                        completionEnergy = completionEnergy.value,
                        showCompletionEnergy = completedDiamondThisReport,
                        convergeProgress = convergeProgress,
                        modifier = Modifier
                            .size(176.dp)
                            .onGloballyPositioned { coords ->
                                onRadarCenterPositioned(
                                    Offset(coords.size.width / 2f, coords.size.height / 2f),
                                    coords,
                                )
                            },
                    )
                    if (right != null) AxisLabel(right.label, right.gain, gainAlpha, gainOffsetY)
                }
                if (bottom != null) AxisLabel(bottom.label, bottom.gain, gainAlpha, gainOffsetY)
            }
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

// progress 0이면 직전 값(startScore), 1이면 이번 값(score).
// 증가분을 모르는 경로(보관함 등)는 두 값이 같아 그냥 제자리에 머문다.
private fun fracAt(c: Competency?, progress: Float): Float {
    if (c == null || c.maxScore == 0) return 0f
    val before = c.startScore.coerceAtLeast(0).toFloat() / c.maxScore
    return (before + (frac(c) - before) * progress).coerceIn(0f, 1f)
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
    Column(
        modifier = Modifier.size(width = 52.dp, height = 42.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            style = TqType.BodyM.figma(),
            color = Gray800,
            softWrap = false,
            maxLines = 1,
            modifier = Modifier.wrapContentWidth(Alignment.CenterHorizontally, unbounded = true),
        ) // 14/400
        // 증가분을 모르는 경로(보관함 등 피드백을 안 거친 진입)면 "+0" 대신 아예 그리지 않는다.
        if (gain > 0) {
            Text(
                text = "+$gain",
                style = TqType.LabelL.figma(),
                color = Primary600,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = gainAlpha
                        translationY = gainOffsetY
                    },
            ) // 14/500
        } else {
            Spacer(Modifier.height(20.dp))
        }
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
    completionEnergy: Float = 0f,
    showCompletionEnergy: Boolean = false,
    // 승급 모션 — 1이면 네 꼭짓점이 중심으로 빨려 들어가고 보라 마름모는 사라진다.
    convergeProgress: Float = 0f,
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
        // 승급 모션에서는 converge가 올라가며 네 꼭짓점이 중심으로 빨려 들어간다.
        val pull = 1f - convergeProgress
        val pTop = Offset(cx, cy - r * top * dataScale * pull)
        val pRight = Offset(cx + r * right * dataScale * pull, cy)
        val pBottom = Offset(cx, cy + r * bottom * dataScale * pull)
        val pLeft = Offset(cx - r * left * dataScale * pull, cy)
        val data = Path().apply {
            moveTo(pTop.x, pTop.y); lineTo(pRight.x, pRight.y)
            lineTo(pBottom.x, pBottom.y); lineTo(pLeft.x, pLeft.y); close()
        }
        // Vector 78: Purple/200 채움 + Purple/600 선, 요소 전체 opacity 0.6 → 안쪽 격자가 비쳐 보임
        // 집결이 끝날 무렵 면은 먼저 지워 별만 남게 한다.
        val faceAlpha = 0.6f * (1f - convergeProgress)
        drawPath(data, color = Primary200.copy(alpha = faceAlpha))
        drawPath(data, color = Primary600.copy(alpha = faceAlpha), style = Stroke(width = 1.dp.toPx()))

        // 꼭짓점 점 (Purple/600) — 모이는 동안 조금 굵어지며 노란빛으로 물든다.
        val dotColor = androidx.compose.ui.graphics.lerp(Primary600, StarYellow, convergeProgress)
        val dotScale = dataScale * (1f + convergeProgress * 0.9f)
        listOf(pTop, pRight, pBottom, pLeft).forEach {
            drawCircle(dotColor, radius = dot * dotScale, center = it)
        }
        // 다 모이면 중심에서 빛이 한 번 터진다(별이 태어나는 자리).
        if (convergeProgress > 0.55f) {
            val burst = (convergeProgress - 0.55f) / 0.45f
            val burstR = r * 0.55f * burst + 1f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFD874).copy(alpha = 0.75f * burst), Color.Transparent),
                    center = Offset(cx, cy),
                    radius = burstR,
                ),
                radius = burstR,
                center = Offset(cx, cy),
            )
        }

        // 마름모 완성 순간, 네 축 끝점이 중심으로 짧게 빨려 들어가 별 완성을 연결한다.
        if (showCompletionEnergy && completionEnergy > 0f) {
            val outerPoints = listOf(
                Offset(cx, cy - r), Offset(cx + r, cy), Offset(cx, cy + r), Offset(cx - r, cy),
            )
            outerPoints.forEach { outer ->
                val point = Offset(
                    x = outer.x + (cx - outer.x) * completionEnergy,
                    y = outer.y + (cy - outer.y) * completionEnergy,
                )
                drawCircle(
                    color = Primary600.copy(alpha = 1f - completionEnergy),
                    radius = dot * (1f - completionEnergy * 0.35f),
                    center = point,
                )
            }
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
    val startScore = competency.startScore.coerceAtLeast(0)
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
                    Competency(CompetencyAxis.EMPATHY, "공감 능력", "공감 능력", 100, 70),
                    Competency(CompetencyAxis.QUESTION_LINK, "질문 연결성", "질문 연결성", 300, 70),
                ),
            ),
            onBack = {},
        )
    }
}
