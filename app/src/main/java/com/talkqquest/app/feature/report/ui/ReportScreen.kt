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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
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
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.talkqquest.app.core.designsystem.component.rememberHapticTick
import com.talkqquest.app.core.util.TierProgress
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

// 범례 행(LegendRow)의 체크 원 크기 + 원↔라벨 간격 — 초과분 문장 들여쓰기(8번)가 이 값을 그대로 쓴다.
private val LegendCircleSize = 19.dp
private val LegendCircleLabelGap = 8.dp

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
    onArchiveClick: () -> Unit = {}, // 시트 "보관함 >" → 아카이브 보관함(리포트 상세)
    // id, 주간 비교 여부 — 종류에 따라 보관함 성장/주간 상세 중 어디로 갈지 갈린다(B).
    onReportClick: (String, Boolean) -> Unit = { _, _ -> },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ReportScreen(
        uiState = uiState,
        onBack = onBack,
        onRetry = viewModel::loadReports,
        onSaveReport = viewModel::saveReport,
        onToggleReportSave = viewModel::toggleReportSave,
        onDismissSaveSheet = viewModel::dismissSaveSheet,
        onToggleBookmark = viewModel::toggleArchiveBookmark,
        onSheetTopChange = onSheetTopChange,
        onArchiveClick = onArchiveClick,
        onSavedReportCardClick = viewModel::onSavedReportCardClick,
        onReportNavigationConsumed = viewModel::consumeReportNavigation,
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
    onToggleBookmark: () -> Unit = {},
    onSheetTopChange: (Float?) -> Unit = {},
    onArchiveClick: () -> Unit = {},
    // 저장 시트의 카드를 눌렀을 때 — 서버 번호가 있는지는 뷰모델이 먼저 판단한다(D).
    onSavedReportCardClick: (String) -> Unit = {},
    onReportNavigationConsumed: () -> Unit = {},
    onReportClick: (String, Boolean) -> Unit = { _, _ -> },
) = FitDesign { // 작은 화면에선 디자인(393x852) 통째 축소 — 다른 화면들과 동일
    // 아직 서버 번호가 없어 미뤄뒀던 카드 클릭 — 번호가 도착하면(navigateToReportId) 그 즉시 이동한다(D).
    LaunchedEffect(uiState.navigateToReportId) {
        val reportId = uiState.navigateToReportId ?: return@LaunchedEffect
        // 종류 판정은 카드가 들고 있는 값(SavedReportItem.type)으로 한다 — 제목 문자열 판정 금지(B-2).
        val isWeekly = uiState.saveSheetReport?.takeIf { it.id == reportId }?.type == "weekly_compare" ||
            uiState.savedReports.firstOrNull { it.id == reportId }?.type == "weekly_compare"
        onReportClick(reportId, isWeekly)
        onReportNavigationConsumed()
    }
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
                    onReportClick = onSavedReportCardClick,
                ) {
                    ReportContent(
                        growth = uiState.growth,
                        onBack = onBack,
                        onSaveClick = onSaveReport,
                        showBookmarkIcon = uiState.isArchiveEntry,
                        isBookmarked = uiState.isBookmarked,
                        onToggleBookmark = onToggleBookmark,
                        isArchiveEntry = uiState.isArchiveEntry,
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
    // 보관함·저장 시트로 특정 리포트를 열었을 때만 헤더에 북마크 아이콘을 더한다(12-A).
    showBookmarkIcon: Boolean = false,
    isBookmarked: Boolean = false,
    onToggleBookmark: () -> Unit = {},
    // 보관함 경로는 이미 저장된 리포트이고 북마크 아이콘이 저장 상태를 대신하므로
    // "리포트 저장하기" 버튼은 띄우지 않는다. 완료 연출(saveButtonVisible 전환)은 그대로 재생한다.
    isArchiveEntry: Boolean = false,
) {
    val tick = rememberHapticTick()
    val scope = rememberCoroutineScope()
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
    // 모션이 끝나면 카드가 승급 후 상태로 갈아탄다(그 전까지는 직전 상태).
    var swapped by remember(growth) { mutableStateOf(false) }
    // 저장 버튼은 연출이 다 끝난 뒤에 등장한다. 승급이 있으면 승급 모션까지 끝나고,
    // 없으면 카드 연출(초과분 문장 접힘 포함)이 끝나는 시점이다.
    var saveButtonVisible by remember(growth) { mutableStateOf(false) }
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
            .onGloballyPositioned { motionRoot = it },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            Spacer(Modifier.height(8.dp)) // 상태바(40) → 헤더(top 48)

            // 헤더: 뒤로가기(left 0) + "성장 리포트"(중앙) [+ 보관함 경로면 북마크(right 0)]
            // 북마크 아이콘이 있는 경로(보관함)만 ArchiveReportScreen 원안 그대로 좌우 4dp를 둔다 —
            // 홈 경로는 지금 값(패딩 없음) 그대로 유지.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .then(if (showBookmarkIcon) Modifier.padding(horizontal = 4.dp) else Modifier),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .clickable(onClick = { tick(); onBack() }),
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
                if (showBookmarkIcon) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .align(Alignment.CenterEnd)
                            .clip(CircleShape)
                            .clickable(onClick = onToggleBookmark),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(
                                id = if (isBookmarked) R.drawable.ic_mission_bookmark_filled else R.drawable.ic_mission_bookmark,
                            ),
                            contentDescription = "북마크",
                            tint = Color.Unspecified,
                        )
                    }
                }
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
                    masterMaxed = growth.masterMaxed,
                    onCompletionAnimationFinished = {
                        tierAutoTrigger++
                        // 승급 모션이 별을 직접 날려 꽂으므로, 그때는 별이 제자리에서 튀어나오는
                        // 연출(completedDiamondTrigger)을 겹쳐 재생하지 않는다.
                        // 마스터 만렙에서도 재생하지 않는다 — 별 3개가 최종이라 켤 별이 없는데
                        // 마름모를 채울 때마다 별이 또 생기는 연출이 나오던 것을 막는다(사용자 결정).
                        if (growth.completedDiamondThisReport && promotion == null && !growth.masterMaxed) {
                            completedDiamondTrigger++
                        }
                        // 승급 연출이 없는 회차는 여기서 연출이 끝나므로 버튼을 올린다.
                        // 승급이 있으면 그 모션이 끝나는 곳(아래 LaunchedEffect)에서 올린다.
                        if (promotion == null) saveButtonVisible = true
                    },
                    convergeProgress = motion.converge.value,
                    onRadarCenterPositioned = { center, coords ->
                        val root = motionRoot ?: return@CompetencyCard
                        motion.radarCenter = root.localPositionOf(coords, center)
                    },
                )
            }

            Spacer(Modifier.height(9.dp)) // 카드 끝 → 버튼 top 728

            // 리포트 저장하기 (CSS "다음" 버튼 자리 — 라벨은 이전 사용자 결정대로 유지). 누르면 저장 시트.
            // 처음부터 떠 있지 않고, 카드 연출(초과분 문장 접힘 → 승급·별 연출)이 다 끝난 뒤
            // 부드럽게 올라온다(사용자 결정). 자리는 미리 잡아둬 버튼이 나타날 때 밀리지 않는다.
            // 보관함 경로(isArchiveEntry)는 이미 저장된 리포트라 이 버튼이 할 일이 없다 — saveButtonVisible이
            // 켜지는 시점(완료 연출)은 그대로 두고 실제로 보이는 것만 막는다.
            AnimatedVisibility(
                visible = saveButtonVisible && !isArchiveEntry,
                enter = fadeIn(tween(280)) +
                    slideInVertically(tween(320, easing = FastOutSlowInEasing)) { it / 3 },
                exit = fadeOut(tween(160)),
            ) {
                TqButton(
                    text = "리포트 저장하기",
                    onClick = { onSaveClick("growth") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                )
            }
        }

        // 티어 승급 안내 바텀시트(공용) — 항상 컴포즈해 두고 visible로 등장/퇴장을 태운다.
        TierPromotionSheet(
            visible = showTierHelp,
            onDismiss = { showTierHelp = false },
        )

        // 승급 모션 오버레이 — 날아가는 별과 딤·휘장은 카드 위에 그려야 해서 최상단에 둔다.
        if (promotion != null) {
            TierPromotionOverlay(
                motion = motion,
                newTierName = promotion.newTierName,
                newTierEmblemRes = tierEmblemSmallRes(promotion.newTierName),
                onDismiss = {
                    scope.launch {
                        motion.dismissCelebration()
                        motion.settle()
                    }
                },
                // FitDesign이 위쪽에 넣어 둔 상태바 보정분까지 딤이 덮게 한다(승급 안내 시트와 동일).
                modifier = Modifier.coverStatusBarCompensation(LocalStatusBarCompensation.current),
            )
            // 만점 체크와 좌표 확보를 "안에서" 기다린다. 이 둘을 key로 두면 재생 도중 조건이
            // 다시 평가되며 코루틴이 취소돼 축하 화면이 걷히지 않은 채 멈춘다.
            LaunchedEffect(growth) {
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
                saveButtonVisible = true // 연출이 완전히 끝난 뒤에야 버튼이 올라온다
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

        // "별 N개를 더 획득하면 X이에요!" — 위 티어가 있으면 그 문구, 마스터면 전용 문구로 갈아탄다.
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
        } else if (tierName == TierProgress.TIERS.last()) {
            Text(
                text = buildAnnotatedString {
                    if (nextStarsNeeded > 0) {
                        withStyle(SpanStyle(color = Gray600)) { append("별 ") }
                        withStyle(SpanStyle(color = Primary600, fontWeight = FontWeight.Medium)) { append("${nextStarsNeeded}개") }
                        withStyle(SpanStyle(color = Gray600)) { append("를 더 획득하면 ") }
                        withStyle(SpanStyle(color = Primary600, fontWeight = FontWeight.Medium)) { append("최고 등급") }
                        withStyle(SpanStyle(color = Gray600)) { append("을 완성해요!") }
                    } else {
                        withStyle(SpanStyle(color = Primary600, fontWeight = FontWeight.Medium)) { append("최고 등급") }
                        withStyle(SpanStyle(color = Gray600)) { append("을 완성했어요!") }
                    }
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
    // 마스터 만렙이면 오를 점수가 없으므로 꼭짓점을 "+0"으로 띄운다.
    // (평소에는 증가분을 모르는 경로에서 "+0"이 뜨지 않도록 gain > 0일 때만 그린다)
    masterMaxed: Boolean = false,
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
    // 지금 문장이 펼쳐져 있는 축들. 초과분이 있는 축은 전부 동시에 들어왔다가 한꺼번에 빠진다(시차 없음).
    var surplusExpandedAxes by remember { mutableStateOf(emptySet<CompetencyAxis>()) }
    // 초과분 문장이 한 줄에 들어갈 크기(9번). 네 축 모두 같은 크기를 써야 하므로 이 카드에서
    // 한 번만 재서(rememberTextMeasurer) 네 문장에 동일 적용한다 — 축마다 다르게 줄이지 않는다.
    val surplusTextMeasurer = rememberTextMeasurer()
    var surplusColumnWidthPx by remember { mutableStateOf(0) }
    // 문장이 범례 라벨 시작 x(체크 원 + 간격)만큼 들여쓰기 때문에(8번), 두 줄 방지 계산도
    // 그만큼 줄어든 가용 폭을 기준으로 재야 한다.
    val surplusIndentPx = with(LocalDensity.current) { (LegendCircleSize + LegendCircleLabelGap).roundToPx() }
    val surplusFontSize = remember(surplusColumnWidthPx, surplusIndentPx) {
        surplusFontSizeFor(surplusTextMeasurer, surplusColumnWidthPx - surplusIndentPx)
    }
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

    // 만렙 등 "값이 실제로 안 변하는" 회차는 그 값을 기다리는 기다림만 건너뛴다.
    // (연출 자체를 지우는 게 아니라 대기만 없애는 것 — gain>0이거나 점수가 바뀌는 일반
    // 회차는 두 값 다 false라 아래 delay가 그대로 살아서 지금 연출과 조금도 다르지 않다.)
    val allGainsZero = competencies.all { it.gain == 0 }
    val allScoresUnchanged = competencies.all { it.startScore == it.score }

    suspend fun playCompletionSequence() {
        radarEntered = false
        gainsVisible = false
        legendScoresVisible = false
        completionChecksVisible = false
        surplusExpandedAxes = emptySet()
        completionEnergy.snapTo(0f)
        withFrameNanos { }
        radarEntered = true
        delay(400) // 보라 데이터 마름모가 완성된 뒤 +획득 수치를 함께 표시한다.
        gainsVisible = true
        if (!allGainsZero) delay(360) // +70 네 개가 위로 올라와 멈출 때까지 기다린다. (모두 +0이면 오를 게 없어 건너뜀)
        legendScoresVisible = true
        if (!allScoresUnchanged) delay(720) // 아래 점수의 상승이 완전히 끝난 뒤에만 만점 체크를 보여 준다. (모두 시작=끝이면 건너뜀)
        completionChecksVisible = true
        delay(340) // 원 채움(240ms)과 체크 등장(140ms 지연 + 160ms)을 모두 마친다. 체크는 항상 나오므로 이 대기는 유지.

        // 체크가 뜬 바로 이 시점에 초과분이 있는 축의 문장을 전부 동시에 펼친다(축별 시차 없음).
        // 다 펼쳐 보여준 뒤 접고, 카드가 원래 높이로 돌아간 다음에야 승급·별 연출로 넘어간다.
        val surplusAxes = competencies.filter { it.surplus > 0 }.map { it.axis }
        if (surplusAxes.isNotEmpty()) {
            surplusExpandedAxes = surplusAxes.toSet()
            delay(520) // 공백 열림(260) + 문장 페이드인(260, 260 지연 뒤 시작)이 끝날 때까지
            delay(1500) // 다 나온 문장을 읽을 틈(페이드인이 끝난 시점부터)
            surplusExpandedAxes = emptySet()
            delay(380) // 문장 퇴장(200) + 공백 닫힘(260, 지연 120 뒤 시작)이 다 끝날 때까지
        }

        // 체크 직후 별도로 꼭짓점을 모으던 completionEnergy 연출은 제거한다.
        // 마름모 집결은 이어지는 TierMotion.converge에서 한 번만 재생한다.
        onCompletionAnimationFinished()
    }

    LaunchedEffect(Unit) {
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
                if (top != null) AxisLabel(top.label, top.gain, gainAlpha, gainOffsetY, masterMaxed)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    // CSS: 52dp 라벨 + 12dp + 176dp 마름모 + 12dp + 52dp 라벨을 중앙 정렬.
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                ) {
                    if (left != null) AxisLabel(left.label, left.gain, gainAlpha, gainOffsetY, masterMaxed)
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
                    if (right != null) AxisLabel(right.label, right.gain, gainAlpha, gainOffsetY, masterMaxed)
                }
                if (bottom != null) AxisLabel(bottom.label, bottom.gain, gainAlpha, gainOffsetY, masterMaxed)
            }
        }

        // 범례 4행. 초과분이 있는 축은 그 줄 아래로 안내 문장이 잠깐 펼쳐졌다 접힌다.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                // 초과분 문장이 실제로 놓이는 폭(카드 폭 − 좌우 16dp씩) — 폰트 크기 계산의 기준.
                .onGloballyPositioned { surplusColumnWidthPx = it.size.width },
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            competencies.forEach { c ->
                // 위 줄에 바짝 붙이고 아래와는 원래 간격(12)을 두어 어느 줄에 속한 문장인지
                // 간격만으로 알 수 있게 한다. 그래서 행과 문장을 한 덩어리로 묶는다.
                Column {
                    LegendRow(
                        competency = c,
                        showFinalScore = legendScoresVisible,
                        showCompletionCheck = completionChecksVisible,
                    )
                    SurplusNotice(
                        competency = c,
                        expanded = surplusExpandedAxes.contains(c.axis),
                        fontSize = surplusFontSize,
                    )
                }
            }
        }
    }
}

// 초과분 안내 — "남은 +995점은 다음 목표에 반영돼요!"
// 등장은 2단계: ① 카드가 커지며 빈 공백이 먼저 열리고(글자 없음) ② 그 자리에서 문장이
// 알파+스케일로 페이드인(세로 이동 없음, 중앙 기준). 퇴장은 정확히 반대 순서.
@Composable
private fun SurplusNotice(
    competency: Competency,
    expanded: Boolean,
    fontSize: TextUnit = 14.sp,
) {
    if (competency.surplus <= 0) return
    // 문장 자체의 등장/퇴장 — 공백(아래 AnimatedVisibility)과 타이밍이 달라 따로 돌린다.
    // 등장: 공백이 다 열린(260ms) 뒤 260ms 지연돼 시작. 퇴장: 지연 없이 바로 200ms.
    val textProgress by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = tween(
            durationMillis = if (expanded) 260 else 200,
            delayMillis = if (expanded) 260 else 0,
            easing = FastOutSlowInEasing,
        ),
        label = "surplusTextProgress",
    )
    AnimatedVisibility(
        visible = expanded,
        // 여기의 expandVertically/shrinkVertically는 "빈 공백" 컨테이너에 대한 것이지 문장에
        // 거는 게 아니다 — 문장은 위 textProgress로 알파/스케일만 바뀐다(세로 이동 없음).
        enter = expandVertically(tween(260, easing = FastOutSlowInEasing)),
        exit = shrinkVertically(tween(260, delayMillis = 120, easing = FastOutSlowInEasing)),
    ) {
        Column {
            Spacer(Modifier.height(6.dp)) // 위 줄에 붙는 간격(아래 줄과의 12보다 좁게)
            Text(
                text = buildAnnotatedString {
                    append("남은 ")
                    // 숫자에만 보라를 준다 — 문장 전체가 보라면 강조가 흩어진다.
                    withStyle(SpanStyle(color = Primary600, fontWeight = FontWeight.Medium)) {
                        append("+${competency.surplus}점")
                    }
                    append("은 다음 목표에 반영돼요!")
                },
                style = TqType.BodyS.figma().copy(fontSize = fontSize),
                color = Gray600,
                maxLines = 1,
                softWrap = false,
                // 범례 라벨 시작 x(체크 원 + 간격)만큼 들여써 위 줄 라벨과 세로로 겹친다.
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = LegendCircleSize + LegendCircleLabelGap)
                    .graphicsLayer {
                        alpha = textProgress
                        scaleX = 0.94f + 0.06f * textProgress
                        scaleY = 0.94f + 0.06f * textProgress
                        transformOrigin = TransformOrigin.Center
                    },
            )
        }
    }
}

// 초과분 문장(9번)이 한 줄에 들어가는 가장 큰 크기를 찾는다. 문장 앞에 축 이름을 붙이지 않으므로
// 네 자리 숫자로만 최악의 문장을 만들어 실제 가용 폭(들여쓰기를 뺀 폭)에 재본다.
// 14sp에서 0.5sp씩 줄이되 11sp가 하한.
private const val SurplusWorstCaseText = "남은 +9999점은 다음 목표에 반영돼요!"

private fun surplusFontSizeFor(textMeasurer: TextMeasurer, availableWidthPx: Int): TextUnit {
    if (availableWidthPx <= 0) return 14.sp
    var size = 14f
    while (size > 11f) {
        val result = textMeasurer.measure(
            text = SurplusWorstCaseText,
            style = TqType.BodyS.figma().copy(fontSize = size.sp),
            maxLines = 1,
            softWrap = false,
        )
        if (result.size.width <= availableWidthPx) return size.sp
        size -= 0.5f
    }
    return 11f.sp
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
    showZeroGain: Boolean = false,
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
        // 단 마스터 만렙은 오를 점수가 없다는 뜻이라 "+0"을 그대로 보여준다(사용자 결정).
        if (gain > 0 || showZeroGain) {
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(LegendCircleLabelGap),
        ) {
            // 원의 자리는 항상 고정. 만점 항목만 점수 상승 뒤 중앙에서 채워지고 체크가 나타난다.
            if (becomesMaxed) {
                Box(
                    modifier = Modifier
                        .size(LegendCircleSize)
                        .clip(CircleShape)
                        .border(1.dp, Gray300, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(LegendCircleSize)
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
                Box(modifier = Modifier.size(LegendCircleSize).clip(CircleShape).border(1.dp, Gray300, CircleShape))
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
