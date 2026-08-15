package com.talkqquest.app.feature.home.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.material3.Text
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt
import com.talkqquest.app.R
import com.talkqquest.app.core.designsystem.Error
import com.talkqquest.app.core.designsystem.FitDesign
import androidx.compose.foundation.border
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
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
import com.talkqquest.app.core.designsystem.LocalDesignScale
import com.talkqquest.app.core.designsystem.Primary100
import com.talkqquest.app.core.designsystem.Primary600
import com.talkqquest.app.core.designsystem.Success
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.White
import com.talkqquest.app.core.designsystem.softShadow
import com.talkqquest.app.core.designsystem.component.LevelUpBurst
import com.talkqquest.app.core.designsystem.component.TierPromotionSheet
import com.talkqquest.app.core.designsystem.component.TqButton
import com.talkqquest.app.core.designsystem.component.TqButtonSize
import com.talkqquest.app.feature.home.data.model.HomeSummary
import com.talkqquest.app.feature.home.data.model.TodayMission
import com.talkqquest.app.feature.home.viewmodel.HomeUiState
import com.talkqquest.app.feature.home.viewmodel.HomeViewModel

// ── 화면 = 2단으로 분리 (state hoisting) ──
// (1) HomeScreen(viewModel): ViewModel과 연결하는 바깥 껍데기. 실제 앱에서 이걸 씀.
// (2) HomeScreen(uiState, onRetry): 상태를 "값으로만" 받아 그리는 부분 → 서버 없이 Preview로 검증.
// 배경은 Gray50(#F8FAFC) = 페이지 배경(디자인시스템: Primary50은 앱 껍데기, Gray50은 페이지).

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    resumeAnimationTrigger: Int = 0,
    xpResetToken: Int = 0,
    animateXpFromZero: Boolean = true,
    onXpAnimationStarted: () -> Unit = {},
    onStartMissionClick: (String) -> Unit = {}, // 오늘의 미션 "미션 시작하기" → 미션 상세
    onOtherMissionsClick: () -> Unit = {},    // "다른 미션 보기" → 미션 목록
    onNotificationClick: () -> Unit = {},     // 상단 벨 → 알림창
    // 주간 비교 리포트 도착 모달은 앱 최상위 레이어에서 표시. 서버가 준 reportId를 함께 올려
    // "보러가기"가 목록 최신을 추측하지 않고 그 리포트를 바로 열게 한다.
    onShowWeeklyReportModal: (String?) -> Unit = {},
    onBadgeCollectionClick: () -> Unit = {}, // 나의 배지 컬렉션 → 프로필 배지 목록
    onSheetTopChange: (Float?) -> Unit = {},  // 티어 승급 안내 시트가 하단 네비를 덮는 동안 네비 가림
    onModalSheetChange: (Boolean) -> Unit = {}, // 티어 시트가 떠 있는 동안 탭 스와이프를 끄기 위한 신호
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(resumeAnimationTrigger) {
        if (resumeAnimationTrigger <= 0) return@LaunchedEffect
        // 별도 화면에서 실제로 돌아온 경우만 들어오는 신호다. 하단 탭·앱 재개에는 호출되지 않는다.
        viewModel.loadHome(showLoading = false)
    }
    HomeScreen(
        uiState = uiState,
        xpAnimationTrigger = resumeAnimationTrigger,
        xpResetToken = xpResetToken,
        animateXpFromZero = animateXpFromZero,
        onXpAnimationStarted = onXpAnimationStarted,
        onRetry = viewModel::loadHome,
        onRefreshTodayMission = viewModel::refreshTodayMission,
        onStartMissionClick = onStartMissionClick,
        onOtherMissionsClick = onOtherMissionsClick,
        onNotificationClick = onNotificationClick,
        onShowWeeklyReportModal = onShowWeeklyReportModal,
        onBadgeCollectionClick = onBadgeCollectionClick,
        onSheetTopChange = onSheetTopChange,
        onModalSheetChange = onModalSheetChange,
    )
}

@Composable
private fun HomeScreen(
    uiState: HomeUiState,
    xpAnimationTrigger: Int = 0,
    xpResetToken: Int = 0,
    animateXpFromZero: Boolean = true,
    onXpAnimationStarted: () -> Unit = {},
    onRetry: () -> Unit,
    onRefreshTodayMission: () -> Unit = {},
    onStartMissionClick: (String) -> Unit = {},
    onOtherMissionsClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onShowWeeklyReportModal: (String?) -> Unit = {},
    onBadgeCollectionClick: () -> Unit = {},
    onSheetTopChange: (Float?) -> Unit = {},
    onModalSheetChange: (Boolean) -> Unit = {},
) = FitDesign { // 작은 화면에선 디자인(393x852) 통째 축소 — 미션 화면들과 동일하게 스크롤 없이 한 화면에
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50),
        contentAlignment = Alignment.Center,
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(color = Primary600)
            }

            uiState.errorMessage != null -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = uiState.errorMessage, style = TqType.BodyM, color = Error)
                    Spacer(Modifier.height(16.dp))
                    TqButton(text = "다시 시도", onClick = onRetry, size = TqButtonSize.Medium)
                }
            }

            uiState.summary != null -> {
                HomeContent(
                    summary = uiState.summary,
                    isRefreshingMission = uiState.isRefreshingMission,
                    xpAnimationTrigger = xpAnimationTrigger,
                    xpResetToken = xpResetToken,
                    animateXpFromZero = animateXpFromZero,
                    onXpAnimationStarted = onXpAnimationStarted,
                    onStartMissionClick = onStartMissionClick,
                    onRefreshTodayMission = onRefreshTodayMission,
                    onOtherMissionsClick = onOtherMissionsClick,
                    onNotificationClick = onNotificationClick,
                    onShowWeeklyReportModal = onShowWeeklyReportModal,
                    onBadgeCollectionClick = onBadgeCollectionClick,
                    onSheetTopChange = onSheetTopChange,
                    onModalSheetChange = onModalSheetChange,
                )
            }
        }
    }
}

// 홈 전용 카드: 흰 배경 + CSS 소프트 그림자(core/designsystem softShadow 공통 사용).
// (공통 TqCard는 머티리얼 그림자라, 홈은 이걸로 디자인 그림자 재현)
@Composable
private fun HomeCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .softShadow(
                color = Gray1000.copy(alpha = 0.01f), // CSS rgba(15,23,42,0.01)
                offsetY = 8.dp,
                blur = 24.dp,
                cornerRadius = cornerRadius,
            )
            .clip(RoundedCornerShape(cornerRadius))
            .background(White)
            .padding(contentPadding),
        content = content,
    )
}

// 홈 메인 콘텐츠 (위→아래로 전사). 좌우 여백 16(디자인 left 16).
@Composable
private fun HomeContent(
    summary: HomeSummary,
    isRefreshingMission: Boolean = false,
    xpAnimationTrigger: Int = 0,
    xpResetToken: Int = 0,
    animateXpFromZero: Boolean = true,
    onXpAnimationStarted: () -> Unit = {},
    onStartMissionClick: (String) -> Unit = {},
    onRefreshTodayMission: () -> Unit = {},
    onOtherMissionsClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onShowWeeklyReportModal: (String?) -> Unit = {},
    onBadgeCollectionClick: () -> Unit = {},
    onSheetTopChange: (Float?) -> Unit = {},
    onModalSheetChange: (Boolean) -> Unit = {},
) {
    // 새 주간 비교 리포트 도착 신호(서버 newWeeklyCompareReport). available일 때만 모달을 띄운다.
    val newWeekly = summary.newWeeklyCompareReport
    LaunchedEffect(newWeekly) {
        if (newWeekly?.available == true) onShowWeeklyReportModal(newWeekly.reportId)
    }
    var missionHasLongText by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    // 홈이 화면에서 벗어나는 순간 받은 reset 신호로 스크롤 위치도 항상 맨 위로 되돌린다.
    LaunchedEffect(xpResetToken) {
        if (xpResetToken > 0) scrollState.scrollTo(0)
    }
    // 실전 티어 ⓘ 탭 → 티어 승급 안내 시트(성장 리포트와 동일 공용 시트).
    var showTierHelp by remember { mutableStateOf(false) }
    // 콘텐츠가 떠 있는 하단 네비(118 몫) 위로 다 들어가게 "필요한 만큼만" 균등 축소.
    // 짧은 미션이면 자연 높이가 여유에 들어가 축소 0 = 피그마 그대로. 넘치면 넘치는 만큼만.
    // 축소는 그리기 단계(graphicsLayer)만 → 레이아웃 크기 불변 → 측정 안정(진동/깜빡임 없음).
    // scale을 여기서 val로 계산 → 홈·팝업이 같은 값을 써서 팝업도 같은 비율로 줄어듦(뒤 화면과 한 세트).
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val availPx = constraints.maxHeight - with(LocalDensity.current) { 118.dp.roundToPx() }
        var naturalPx by remember { mutableIntStateOf(0) }
        val scale = if (!missionHasLongText && naturalPx > 0 && naturalPx > availPx) {
            (availPx.toFloat() / naturalPx).coerceIn(0.8f, 1f) // 하한 0.8 (과축소 방지)
        } else 1f
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 118.dp)
                .verticalScroll(scrollState)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            transformOrigin = TransformOrigin(0.5f, 0f) // 위쪽 기준(상단 고정, 아래로만 당겨 올림)
                        }
                        .onGloballyPositioned { naturalPx = it.size.height } // graphicsLayer는 레이아웃 크기 불변 → 안정
                        .statusBarsPadding()
                        // CSS: 카드 묶음 Frame 430 left 16, 폭 362 → 우측 여백 15 (좌우 비대칭)
                        .padding(start = 16.dp, end = 15.dp),
                ) {
                HomeHeader(
                    nickname = summary.nickname,
                    hasNewNotification = summary.hasNewNotification,
                    onNotificationClick = onNotificationClick, // 상단 벨 → 알림창
                )
                Spacer(Modifier.height(16.dp)) // CSS Frame427321631 gap 16 (인사 블록 → 카드 스택)
                HomeLevelCard(
                    level = summary.level,
                    currentXp = summary.currentXp,
                    nextLevelXp = summary.nextLevelXp,
                    tierName = summary.tierName,
                    tierStars = summary.tierStars,
                    xpAnimationTrigger = xpAnimationTrigger,
                    xpResetToken = xpResetToken,
                    animateXpFromZero = animateXpFromZero,
                    onXpAnimationStarted = onXpAnimationStarted,
                    onTierInfoClick = { showTierHelp = true },
                )
                Spacer(Modifier.height(12.dp)) // CSS 카드 스택 gap 12
                summary.todayMission?.let { mission ->
                    HomeMissionCard(
                        mission = mission,
                        isRefreshing = isRefreshingMission,
                        onRefreshClick = onRefreshTodayMission,
                        onStartClick = { onStartMissionClick(mission.id) },
                        onTextLineCountChanged = { titleLines, descriptionLines ->
                            missionHasLongText = titleLines >= 3 || descriptionLines >= 3
                        },
                    )
                }
                Spacer(Modifier.height(12.dp)) // CSS 카드 스택 gap 12
                OtherMissionsCard(onClick = onOtherMissionsClick)
                Spacer(Modifier.height(12.dp)) // CSS 카드 스택 gap 12 (다른 미션 보기 → 배지)
                    BadgeCollectionCard(onClick = onBadgeCollectionClick)
                }
                // 콘텐츠가 화면에 모두 보여도 다른 미션 보기 카드 높이만큼 더 내려갈 수 있게 한다.
                Spacer(Modifier.height(50.dp))
            }
        }

        // 티어 승급 안내 시트(공용) — 홈 축소(scale)와 무관하게 전체 화면에 오버레이.
        // onSheetTopChange로 시트가 덮는 동안 하단 네비를 가림(저장 시트와 동일).
        // onModalChange는 시트가 떠 있는 동안 탭 스와이프를 끄는 신호 — 딤이 깔린 모달이라 뒤로 못 넘어가야 한다.
        TierPromotionSheet(
            visible = showTierHelp,
            onDismiss = { showTierHelp = false },
            onSheetTopChange = onSheetTopChange,
            onModalChange = onModalSheetChange,
        )
    }
}

// 피그마 line-height 박스처럼 위아래 여백을 살림(Compose 기본은 trim해서 텍스트 간격이 더 좁아짐).
private val FullLeading = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)

// 홈 텍스트는 이걸로 감싸 피그마 line-height 여백을 살림(공통 TqType 안 건드리고 홈 로컬).
private fun TextStyle.figma(): TextStyle = copy(lineHeightStyle = FullLeading)

// 어절 안 글자 사이에 WORD JOINER(U+2060, 폭 0·비표시)를 끼워 어절 중간 줄바꿈("사/람에게")을 막음.
// API 33+의 LineBreak.WordBreak.Phrase와 같은 효과를 전 버전(minSdk 26)에서 보장. 공백·\n은 그대로 둠.
private fun String.keepWordsIntact(): String =
    replace(Regex("(?<=\\S)(?=\\S)"), "⁠")

// "한 번"의 "한"처럼 한 글자 어절이 줄 끝에 홀로 남지 않게, 한 글자 어절 뒤 공백을
// 줄바꿈 금지 공백(NBSP, U+00A0)으로 바꿔 다음 어절과 한 덩어리로 묶음. ("한 번" → 항상 같은 줄)
private fun String.glueShortWords(): String =
    replace(Regex("(?<=(^|\\s)\\S) "), " ")

// 제목은 먼저 필요한 최소 줄 수를 구한 뒤, 그 줄 수 안에서 실제 글자 폭이 가장 균등해지는
// 어절 경계를 선택한다. 제목이 한 줄에 들어가면 디자인처럼 한 줄을 그대로 유지한다.
private const val TITLE_MAX_WIDTH_DP = 256 // 디자인 Frame313 제목 영역 폭

@Composable
private fun MissionTitleText(
    title: String,
    modifier: Modifier = Modifier,
    onLineCountChanged: (Int) -> Unit = {},
) {
    val style = TqType.TitleL.figma().copy(lineBreak = LineBreak.Heading)
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val displayTitle = remember(title) {
        val maxWidthPx = with(density) { TITLE_MAX_WIDTH_DP.dp.toPx() }
        fun width(text: String): Int =
            measurer.measure(AnnotatedString(text.keepWordsIntact()), style = style).size.width

        val rawWords = title.trim().split(Regex("\\s+")).filter(String::isNotEmpty)
        val words = buildList {
            var index = 0
            while (index < rawWords.size) {
                val word = rawWords[index]
                if (word.length == 1 && index + 1 < rawWords.size) {
                    add("$word ${rawWords[index + 1]}")
                    index += 2
                } else {
                    add(word)
                    index++
                }
            }
        }
        val oneLine = words.joinToString(" ")
        if (width(oneLine) <= maxWidthPx || words.size <= 1) {
            oneLine
        } else {
            // Greedy packing으로 모든 어절을 담을 수 있는 최소 줄 수를 구한다.
            val minimumLines = buildList {
                var current = ""
                words.forEach { word ->
                    val candidate = if (current.isEmpty()) word else "$current $word"
                    if (current.isNotEmpty() && width(candidate) > maxWidthPx) {
                        add(current)
                        current = word
                    } else {
                        current = candidate
                    }
                }
                if (current.isNotEmpty()) add(current)
            }
            val lineCount = minimumLines.size
            val targetWidth = minimumLines.sumOf { width(it) }.toFloat() / lineCount
            data class Partition(val lines: List<String>, val score: Float)
            val memo = mutableMapOf<Pair<Int, Int>, Partition?>()
            fun choose(start: Int, linesLeft: Int): Partition? {
                if (start >= words.size) return null
                if (linesLeft == 1) {
                    val last = words.subList(start, words.size).joinToString(" ")
                    val lastWidth = width(last)
                    return if (lastWidth <= maxWidthPx || start == words.lastIndex) {
                        Partition(listOf(last), abs(lastWidth - targetWidth))
                    } else null
                }
                val key = start to linesLeft
                memo[key]?.let { return it }
                var current = ""
                var best: Partition? = null
                for (end in start until words.size - (linesLeft - 1)) {
                    current = if (current.isEmpty()) words[end] else "$current ${words[end]}"
                    val currentWidth = width(current)
                    if (currentWidth > maxWidthPx && end > start) break
                    val tail = choose(end + 1, linesLeft - 1) ?: continue
                    val candidate = Partition(
                        lines = listOf(current) + tail.lines,
                        score = abs(currentWidth - targetWidth) + tail.score,
                    )
                    if (best == null || candidate.score < best!!.score) best = candidate
                }
                memo[key] = best
                return best
            }
            val chosen = choose(0, lineCount)?.lines ?: minimumLines
            chosen.joinToString("\n")
        }
    }
    Text(
        text = displayTitle,
        style = style,
        color = Gray900,
        modifier = modifier.widthIn(max = TITLE_MAX_WIDTH_DP.dp),
        onTextLayout = { onLineCountChanged(it.lineCount) },
    )
}

// 인사 영역 + 알림 벨.
@Composable
private fun HomeHeader(
    nickname: String,
    hasNewNotification: Boolean = false,
    onNotificationClick: () -> Unit = {},
) {
    val handWaveRotation = remember { Animatable(0f) }
    var handWaveRunning by remember { mutableStateOf(false) }
    var handWaveInitialPlayed by rememberSaveable { mutableStateOf(false) }
    val handWaveScope = rememberCoroutineScope()

    suspend fun waveHand() {
        handWaveRunning = true
        handWaveRotation.animateTo(5f, tween(120, easing = FastOutSlowInEasing))
        handWaveRotation.animateTo(-5f, tween(180, easing = FastOutSlowInEasing))
        handWaveRotation.animateTo(3f, tween(150, easing = FastOutSlowInEasing))
        handWaveRotation.animateTo(0f, tween(180, easing = LinearOutSlowInEasing))
        handWaveRunning = false
    }

    LaunchedEffect(Unit) {
        if (!handWaveInitialPlayed) {
            handWaveInitialPlayed = true
            delay(200)
            waveHand()
        }
    }

    // CSS 절대위치 전사(상태바 40 기준): 인사 top 79 → 아래 39, 벨 top 50 → 아래 10 (벨이 29 위).
    // statusBarsPadding이 실제 상태바를 처리하고, 그 아래 이 오프셋만큼 배치.
    Box(modifier = Modifier.fillMaxWidth()) {
        // 알림 벨 — 우측, 상태바 아래 10. 알림 있으면 점 붙은 변형(피그마 Property 1=알림있음).
        val bellInteraction = remember { MutableInteractionSource() }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                // CSS: 알람 left=343 → 우측 여백 6px. 이 헤더는 좌우 16 패딩 안이라 그대로면 16px에서 멈춤 → 10 더 내보냄.
                .offset(x = 10.dp)
                .padding(top = 10.dp)
                .size(44.dp)
                .clip(CircleShape) // 리플이 원형으로 퍼지도록 먼저 원형 클립
                .clickable(
                    interactionSource = bellInteraction,
                    indication = ripple(bounded = true), // 원 안을 채우는 원형 물결
                    onClick = onNotificationClick,
                ),
            contentAlignment = Alignment.Center,
        ) {
            // 벨 → 알림창(placeholder). 알림창 디자인 나오면 NotificationScreen 본문만 채우면 됨.
            if (hasNewNotification) {
                Icon(
                    painter = painterResource(R.drawable.ic_home_bell_active),
                    contentDescription = "알림 (새 알림 있음)",
                    tint = Color.Unspecified, // 벡터에 색 포함 (벨 Gray300 + 점 Primary600)
                    modifier = Modifier.size(44.dp),
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.ic_home_bell),
                    contentDescription = "알림",
                    tint = Gray300,
                    modifier = Modifier.size(width = 20.dp, height = 22.dp),
                )
            }
        }
        // 인사 — 좌측, 상태바 아래 39
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 6.dp, top = 39.dp), // 인삿말 프레임 왼쪽 패딩 6 (UI 10차)
            verticalArrangement = Arrangement.spacedBy(0.dp), // 인사 두 줄 간격 0 (UI 10차)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(text = "안녕하세요, ${nickname}님!", style = TqType.HeadingM.figma(), color = Gray800)
                Image(
                    painter = painterResource(R.drawable.img_home_waving_hand),
                    contentDescription = null,
                    // 손 흔들기: PNG(73x95)는 CSS "Frame 304"(24.27 x 31.67 = -5.5° 회전 후 경계)의 3배 export로,
                    // 회전까지 이미 구워져 있음(알파 실측: 내용이 캔버스를 꽉 채움) → 프레임 크기로만 그린다.
                    // PNG의 -5.5°는 유지하고 손목을 중심으로 추가 흔들림만 준다.
                    modifier = Modifier
                        .size(width = 24.27.dp, height = 31.67.dp)
                        .graphicsLayer {
                            rotationZ = handWaveRotation.value
                            transformOrigin = TransformOrigin(0.53f, 0.88f)
                        }
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                if (!handWaveRunning) {
                                    handWaveScope.launch { waveHand() }
                                }
                            },
                        ),
                )
            }
            Text(text = "오늘도 좋은 대화를 시작해볼까요?", style = TqType.BodyM.figma(), color = Gray600) // 13→14 regular (UI 10차)
        }
    }
}

// 레벨 카드. 흰 카드 radius 20 / "대화 진행 레벨" + Lv·XP + 진행바.
// XP가 바뀌면(미션 완료 후 복귀 등) 숫자·바가 부드럽게 차오르고, 레벨업이면
// 미션 완료 화면과 같은 연출: 바 가득 → "Lv" 글자가 튀며 +1 → 새 레벨 바가 0부터 재충전.
@Composable
private fun HomeLevelCard(
    level: Int,
    currentXp: Int,
    nextLevelXp: Int,
    tierName: String,
    tierStars: Int,
    xpAnimationTrigger: Int = 0,
    xpResetToken: Int = 0,
    animateXpFromZero: Boolean = true,
    onXpAnimationStarted: () -> Unit = {},
    onTierInfoClick: () -> Unit = {},
) {
    // 홈이 처음 보이거나 다른 화면에서 돌아올 때 현재 XP를 먼저 그리지 않는다.
    // 빈 게이지에서 최신 XP까지 한 번만 차오르게 한다.
    val xpShown = remember {
        Animatable(if (animateXpFromZero) 0f else currentXp.toFloat())
    }
    var displayLevel by remember { mutableIntStateOf(level) }
    val levelScale = remember { Animatable(1f) } // 레벨업 순간 Lv 글자가 튀는 배율
    val levelBurst = remember { Animatable(0f) } // 레벨업 순간 Lv 글자 주변 작은 폭죽 (완료 화면과 동일)
    val levelTapScale = remember { Animatable(1f) }
    var levelTapRunning by remember { mutableStateOf(false) }
    val levelTapScope = rememberCoroutineScope()
    var lastXpAnimationTrigger by rememberSaveable { mutableIntStateOf(0) }
    val levelTitleInteraction = remember { MutableInteractionSource() }
    val levelTitlePressed by levelTitleInteraction.collectIsPressedAsState()
    var levelTitleClickAnimating by remember { mutableStateOf(false) }
    val levelTitleScope = rememberCoroutineScope()
    val levelTitleVisuallyPressed = levelTitlePressed || levelTitleClickAnimating
    val levelTitleScale by animateFloatAsState(
        targetValue = if (levelTitleVisuallyPressed) 0.94f else 1f,
        animationSpec = tween(
            durationMillis = if (levelTitleVisuallyPressed) 90 else 140,
            easing = FastOutSlowInEasing,
        ),
        label = "levelTitlePressScale",
    )
    val levelXpInteraction = remember { MutableInteractionSource() }
    val levelXpPressed by levelXpInteraction.collectIsPressedAsState()
    var levelXpClickAnimating by remember { mutableStateOf(false) }
    val levelXpScope = rememberCoroutineScope()
    val levelXpVisuallyPressed = levelXpPressed || levelXpClickAnimating
    val levelXpScale by animateFloatAsState(
        targetValue = if (levelXpVisuallyPressed) 0.94f else 1f,
        animationSpec = tween(
            durationMillis = if (levelXpVisuallyPressed) 90 else 140,
            easing = FastOutSlowInEasing,
        ),
        label = "levelXpPressScale",
    )
    // 티어 전환/광택 단계는 화면 복귀 때 복원하면 안 되는 일회성 애니메이션 상태다.
    // 저장 상태로 두면 다른 화면으로 이동 중의 phase=1이 복원되어 게이지보다 먼저 재생된다.
    var tierTextTrigger by remember { mutableIntStateOf(0) }
    var tierVisualPhase by remember { mutableIntStateOf(0) }
    val tierVisualScope = rememberCoroutineScope()
    val xpFillEasing = CubicBezierEasing(0.12f, 0.78f, 0.22f, 1f)
    suspend fun playTierGrowthSequence() {
        // animateTo가 마지막 값을 반영한 뒤 한 프레임을 실제로 그린 다음 티어 전환을 시작한다.
        // 그래야 게이지가 끝까지 찬 화면을 건너뛰고 Gold가 먼저 보이는 일이 없다.
        withFrameNanos { }
        tierTextTrigger++ // 게이지 완료 후 티어명 전환 시작
    }
    suspend fun playTierVisualSequence() {
        tierVisualPhase = 1 // 광택 시작 시 뱃지 + 별 동시
        delay(720)
        tierVisualPhase = 0
    }
    LaunchedEffect(xpResetToken) {
        if (xpResetToken > 0 && animateXpFromZero) {
            xpShown.snapTo(0f)
        }
    }
    LaunchedEffect(level, currentXp, xpAnimationTrigger) {
        if (xpAnimationTrigger != lastXpAnimationTrigger) {
            lastXpAnimationTrigger = xpAnimationTrigger
            displayLevel = level
            if (animateXpFromZero) {
                onXpAnimationStarted()
                xpShown.snapTo(0f)
                xpShown.animateTo(
                    currentXp.toFloat(),
                    tween(durationMillis = 700, easing = xpFillEasing),
                )
                playTierGrowthSequence()
            } else {
                xpShown.snapTo(currentXp.toFloat())
            }
        } else if (xpAnimationTrigger == 0 && lastXpAnimationTrigger == 0 && animateXpFromZero) {
            // 최초 홈 데이터도 빈 게이지에서 현재 XP까지 바로 한 번 재생한다.
            onXpAnimationStarted()
            displayLevel = level
            xpShown.animateTo(
                currentXp.toFloat(),
                tween(durationMillis = 700, easing = xpFillEasing),
            )
            playTierGrowthSequence()
        } else if (level > displayLevel) {
            xpShown.animateTo(
                nextLevelXp.toFloat(),
                tween(durationMillis = 700, easing = xpFillEasing),
            )
            displayLevel = level
            launch { levelBurst.snapTo(0f); levelBurst.animateTo(1f, tween(600)) } // 글자 튐과 동시 재생
            levelScale.animateTo(1.4f, tween(150))
            levelScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
            delay(150)
            xpShown.snapTo(0f)
            xpShown.animateTo(
                currentXp.toFloat(),
                tween(durationMillis = 720, easing = xpFillEasing),
            )
            playTierGrowthSequence()
        } else if (xpShown.value.roundToInt() != currentXp) {
            displayLevel = level
            xpShown.animateTo(
                currentXp.toFloat(),
                tween(durationMillis = 950, easing = xpFillEasing),
            )
            playTierGrowthSequence()
        } else {
            // 하단 탭 이동으로 홈 페이지가 폐기됐다 재생성된 경우: 현재값을 그대로 표시하고
            // 최초 진입으로 오인한 게이지·티어 모션은 실행하지 않는다.
            displayLevel = level
            xpShown.snapTo(currentXp.toFloat())
        }
    }
    HomeCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            text = "대화 진행 레벨",
            style = TqType.LabelL.figma().copy(fontSize = 16.sp),
            color = Gray700,
            modifier = Modifier
                .graphicsLayer {
                    scaleX = levelTitleScale
                    scaleY = levelTitleScale
                }
                .clickable(
                    interactionSource = levelTitleInteraction,
                    indication = null,
                    onClick = {
                        if (!levelTitleClickAnimating) {
                            levelTitleClickAnimating = true
                            levelTitleScope.launch {
                                delay(100)
                                levelTitleClickAnimating = false
                            }
                        }
                    },
                ),
        ) // 16 medium / Gray700 (UI 10차)
        Spacer(Modifier.height(4.dp)) // CSS Frame428 gap 5→4 (UI 10차)
        Row(
            // CSS Frame 333 높이 22 (Lv 배지 틀 22, 텍스트 18) — 없으면 행이 18로 줄어 카드가 4px 낮아짐
            modifier = Modifier.fillMaxWidth().height(22.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = levelTapScale.value
                        scaleY = levelTapScale.value
                        transformOrigin = TransformOrigin.Center
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            if (!levelTapRunning) {
                                levelTapRunning = true
                                levelTapScope.launch {
                                    levelTapScale.snapTo(1f)
                                    levelTapScale.animateTo(
                                        targetValue = 0.91f,
                                        animationSpec = tween(70, easing = FastOutSlowInEasing),
                                    )
                                    levelTapScale.animateTo(
                                        targetValue = 1.17f,
                                        animationSpec = tween(120, easing = FastOutSlowInEasing),
                                    )
                                    levelTapScale.animateTo(
                                        targetValue = 1f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessMedium,
                                        ),
                                    )
                                    levelTapRunning = false
                                }
                            }
                        },
                    ),
            ) {
                LevelUpBurst(progress = levelBurst.value, modifier = Modifier.matchParentSize()) // 글자 뒤 폭죽
                Text(
                    text = "Lv.$displayLevel",
                    style = TqType.LabelM.figma(),
                    color = Primary600,
                    modifier = Modifier.graphicsLayer {
                        scaleX = levelScale.value
                        scaleY = levelScale.value
                        transformOrigin = TransformOrigin(0f, 0.5f) // 왼쪽 기준으로 튀게(자리 유지)
                    },
                )
            }
            Text(
                text = buildAnnotatedString {
                    // 획득 XP 숫자만 Gray500, 나머지(" / nXP")는 Gray400 (UI 10차)
                    withStyle(SpanStyle(color = Gray500)) { append("${xpShown.value.toInt()}") }
                    withStyle(SpanStyle(color = Gray400)) { append(" / ${nextLevelXp}XP") }
                },
                style = TqType.LabelM.figma(),
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = levelXpScale
                        scaleY = levelXpScale
                    }
                    .clickable(
                        interactionSource = levelXpInteraction,
                        indication = null,
                        onClick = {
                            if (!levelXpClickAnimating) {
                                levelXpClickAnimating = true
                                levelXpScope.launch {
                                    delay(100)
                                    levelXpClickAnimating = false
                                }
                            }
                        },
                    ),
            )
        }
        Spacer(Modifier.height(4.dp))
        // 진행바: 트랙 Primary100 + 채움 Primary600 (currentXp/nextLevelXp 비율), 높이 10, radius 8
        val fraction = if (nextLevelXp > 0) (xpShown.value / nextLevelXp).coerceIn(0f, 1f) else 0f
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Primary100),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(10.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Primary600),
            )
        }
        // 진행바 → 구분선 gap 12 (CSS Frame427321765 gap)
        Spacer(Modifier.height(12.dp))
        // 구분선 (Frame 427321759: 폭 330 = full, 높이 1, Gray/200)
        Box(Modifier.fillMaxWidth().height(1.dp).background(Gray200))
        // 구분선 → 티어 행 gap 4 (CSS 카드 Frame427321766 gap)
        Spacer(Modifier.height(4.dp))
        HomeTierRow(
            tierName = tierName,
            tierStars = tierStars,
            tierTextTrigger = tierTextTrigger,
            tierVisualPhase = tierVisualPhase,
            onTierVisualAnimation = {
                if (tierVisualPhase == 0) {
                    tierVisualScope.launch { playTierVisualSequence() }
                }
            },
            onInfoClick = onTierInfoClick,
        )
    }
}

// 실전 티어 행 (Frame 427321763, 높이 40, space-between).
// 좌: 티어 휘장 40x40 + 티어명 + 별 3개 / 우: "실전 티어" + info-circle.
@Composable
private fun HomeTierRow(
    tierName: String,
    tierStars: Int,
    tierTextTrigger: Int = 0,
    tierVisualPhase: Int = 0,
    onTierVisualAnimation: () -> Unit = {},
    onInfoClick: () -> Unit = {},
) {
    // 휘장·티어명·별 전체를 한 번에 가로지르는 성장 광선.
    // 개별 요소의 펄스 대신 성장 리포트와 같은 한 줄 모션을 사용한다.
    val tierGroupShineProgress = remember { Animatable(0f) }
    val tierBadgeShineProgress = remember { Animatable(0f) }
    var tierBadgeShineVisible by remember { mutableStateOf(false) }
    val tierBadgeScope = rememberCoroutineScope()
    val tierBadgeOuterShinePath = remember { Path() }
    val tierBadgeInnerShinePath = remember { Path() }
    val tierEnglish = tierEnglishName(tierName)
    val tierPalette = tierTextPalette(tierName)
    // 브론즈·실버 리소스는 투명 외곽 여백이 커서 같은 40dp로 그리면 글자와 멀어 보인다.
    // 이미지 자체는 건드리지 않고 시각 영역만 확대해 두 뱃지의 외곽 여백을 맞춘다.
    val tierBadgeScale = if (tierName == "브론즈" || tierName == "실버") 1.14f else 1f
    // 확대 후에도 남는 투명 외곽만큼 티어명·별 묶음을 왼쪽으로 당겨 체감 간격을 맞춘다.
    val tierBadgeContentOffset = if (tierName == "브론즈" || tierName == "실버") (-6).dp else 0.dp
    // 날개가 없는 뱃지의 왼쪽 투명 여백 때문에 전체 그룹이 안쪽으로 보이지 않게 기준선에 맞춘다.
    val tierBadgeGroupOffset = if (tierName == "브론즈" || tierName == "실버") (-6).dp else 0.dp
    var tierTextPhase by remember { mutableIntStateOf(0) } // 0=한글, 3=광택, 4=크로스페이드, 5=글자 띠 전환
    var tierTextRunning by remember { mutableStateOf(false) }
    val tierTextFadeProgress = remember { Animatable(0f) }
    val tierTextWipeProgress = remember { Animatable(0f) }
    val tierTextScope = rememberCoroutineScope()

    suspend fun playTierGroupShine() {
        tierGroupShineProgress.snapTo(0f)
        tierGroupShineProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(300, easing = LinearEasing),
        )
        tierGroupShineProgress.snapTo(0f)
        // 광선이 없는 상태를 한 프레임 먼저 그린 후 한글 복귀를 시작한다.
        withFrameNanos { }
    }

    suspend fun playBadgeShine() {
        tierBadgeShineVisible = true
        try {
            tierBadgeShineProgress.snapTo(0f)
            tierBadgeShineProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 450,
                    easing = LinearEasing,
                ),
            )
        } finally {
            tierBadgeShineVisible = false
        }
    }

    suspend fun playTierTextAnimation() {
        if (tierEnglish == null || tierTextRunning) return
        tierTextRunning = true
        try {
            tierTextFadeProgress.snapTo(0f)
            tierTextWipeProgress.snapTo(0f)
            tierTextPhase = 5
            tierTextWipeProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    // 이름 길이와 무관하게 기존 골드 → Gold 전환 속도로 통일한다.
                    durationMillis = 660,
                    easing = LinearEasing,
                ),
            )
            // 영문 전체가 그려진 프레임을 먼저 노출한 뒤 광선 단계로 넘어간다.
            withFrameNanos { }
            tierTextPhase = 3
            // Gold 광택이 시작되는 순간, 휘장·글자·별을 한 줄 광선으로 함께 훑는다.
            playTierGroupShine()
            // Gold 완성 시점부터 500ms를 유지한다. 광선 300ms 뒤 200ms만 더 머문다.
            delay(200)
            // 광선이 사라진 뒤에만 Gold → 골드 복귀를 시작한다.
            tierTextPhase = 4
            tierTextFadeProgress.snapTo(0f)
            tierTextFadeProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(400, easing = LinearOutSlowInEasing),
            )
            tierTextPhase = 0
            tierTextFadeProgress.snapTo(0f)
            tierTextWipeProgress.snapTo(0f)
        } finally {
            tierTextRunning = false
        }
    }

    LaunchedEffect(tierTextTrigger) {
        if (tierTextTrigger > 0) {
            // 게이지 완료 기준에서는 텍스트 전환부터 시작한다. 뱃지·별은 광택 시점에 별도 트리거된다.
            tierTextScope.launch { playTierTextAnimation() }
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth().height(40.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 좌측 그룹 (Frame 427321762, gap 7)
        Row(
            modifier = Modifier
                .offset(x = tierBadgeGroupOffset)
                .drawWithContent {
                    drawContent()
                    if (tierGroupShineProgress.value > 0f) {
                        val centerX = size.width * tierGroupShineProgress.value
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
                // 휘장·티어명·별 어디를 눌러도 동일한 전환과 한 줄 광선을 실행한다.
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { tierTextScope.launch { playTierTextAnimation() } },
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            // 티어 휘장 (사용자_티어 40x40) — 남서쪽 점에서 북동쪽 점으로 이동하는 광선
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            tierTextScope.launch { playTierTextAnimation() }
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(tierEmblemRes(tierName)),
                    contentDescription = "$tierName 티어",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(40.dp).graphicsLayer {
                        scaleX = tierBadgeScale
                        scaleY = tierBadgeScale
                    },
                )
                if (tierBadgeShineVisible) {
                    // 기존 뱃지 전용 타원 광선 모션.
                    /*
                    Image(
                        painter = painterResource(tierEmblemRes(tierName)),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        colorFilter = ColorFilter.tint(White.copy(alpha = 0.12f)),
                        modifier = Modifier
                            .size(40.dp)
                            .graphicsLayer {
                                scaleX = tierBadgeScale
                                scaleY = tierBadgeScale
                            }
                            .drawWithContent {
                                drawContent()
                                val progress = tierBadgeShineProgress.value
                                val spread = 1f - abs((progress * 2f) - 1f)
                                val center = Offset(
                                    x = size.width * progress,
                                    y = size.height * (1f - progress),
                                )
                                val width = (3f + (29f * spread)).dp.toPx()
                                val height = (3f + (13f * spread)).dp.toPx()
                                tierBadgeOuterShinePath.reset()
                                tierBadgeOuterShinePath.addOval(
                                    Rect(
                                        left = center.x - (width / 2f),
                                        top = center.y - (height / 2f),
                                        right = center.x + (width / 2f),
                                        bottom = center.y + (height / 2f),
                                    ),
                                )
                                clipPath(tierBadgeOuterShinePath) {
                                    this@drawWithContent.drawContent()
                                }
                            },
                    )
                    Image(
                        painter = painterResource(tierEmblemRes(tierName)),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        colorFilter = ColorFilter.tint(White.copy(alpha = 0.2f)),
                        modifier = Modifier
                            .size(40.dp)
                            .graphicsLayer {
                                scaleX = tierBadgeScale
                                scaleY = tierBadgeScale
                            }
                            .drawWithContent {
                                val progress = tierBadgeShineProgress.value
                                val spread = 1f - abs((progress * 2f) - 1f)
                                val center = Offset(
                                    x = size.width * progress,
                                    y = size.height * (1f - progress),
                                )
                                val width = (2f + (15f * spread)).dp.toPx()
                                val height = (2f + (7f * spread)).dp.toPx()
                                tierBadgeInnerShinePath.reset()
                                tierBadgeInnerShinePath.addOval(
                                    Rect(
                                        left = center.x - (width / 2f),
                                        top = center.y - (height / 2f),
                                        right = center.x + (width / 2f),
                                        bottom = center.y + (height / 2f),
                                    ),
                                )
                                clipPath(tierBadgeInnerShinePath) {
                                    this@drawWithContent.drawContent()
                                }
                        },
                    )
                    */
                    // Gold 글자와 같은 방향의 빛이 뱃지 표면을 왼쪽에서 오른쪽으로 통과한다.
                    Image(
                        painter = painterResource(tierEmblemRes(tierName)),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        colorFilter = ColorFilter.tint(White.copy(alpha = 0.18f)),
                        modifier = Modifier
                            .size(40.dp)
                            .graphicsLayer {
                                scaleX = tierBadgeScale
                                scaleY = tierBadgeScale
                            }
                            .drawWithContent {
                                val progress = tierBadgeShineProgress.value
                                // 뱃지 리소스의 투명 외곽 때문에 빛이 늦게 보이는 것을 보정한다.
                                // 시작 위치만 앞당기고 종료점은 1.0에 고정해 글자 광택과 맞춘다.
                                val lead = when (tierName) {
                                    "브론즈", "실버" -> 0.16f
                                    "플래티넘", "플레티넘", "다이아", "다이아몬드", "마스터" -> 0.10f
                                    else -> 0.08f
                                }
                                val adjustedProgress = lead + (progress * (1f - lead))
                                val centerX = size.width * adjustedProgress
                                val shineWidth = size.width * 0.24f
                                val shine = Brush.linearGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        White.copy(alpha = 0.82f),
                                        Color.Transparent,
                                    ),
                                    start = Offset(centerX - shineWidth, size.height),
                                    end = Offset(centerX + shineWidth, 0f),
                                )
                                drawRect(
                                    brush = shine,
                                    blendMode = BlendMode.SrcAtop,
                                )
                            },
                    )
                }
            }
            // 티어명 + 별 (Frame 427321761, gap 4)
            Row(
                modifier = Modifier.offset(x = tierBadgeContentOffset),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (tierEnglish != null) {
                    val titleStyle = TqType.BodyL.figma().copy(fontWeight = FontWeight.Medium)
                    val titleMeasurer = rememberTextMeasurer()
                    val density = LocalDensity.current
                    val displayedText = if (tierTextPhase == 0) tierName else tierEnglish
                    fun textWidth(text: String): Int =
                        titleMeasurer.measure(AnnotatedString(text), style = titleStyle).size.width
                    fun progressiveTextWidth(text: String, progress: Float): Float {
                        val scaledProgress = progress.coerceIn(0f, 1f) * text.length
                        val completedCharacters = scaledProgress.toInt().coerceAtMost(text.length)
                        val characterProgress = scaledProgress - completedCharacters
                        val easedProgress = characterProgress * characterProgress * (3f - 2f * characterProgress)
                        val startWidth = textWidth(text.take(completedCharacters)).toFloat()
                        val endWidth = textWidth(text.take((completedCharacters + 1).coerceAtMost(text.length))).toFloat()
                        return startWidth + (endWidth - startWidth) * easedProgress
                    }
                    val wipeSplit = tierName.length.toFloat() / (tierName.length + tierEnglish.length)
                    val wipeWidth = if (tierTextWipeProgress.value <= wipeSplit) {
                        val eraseProgress = tierTextWipeProgress.value / wipeSplit
                        progressiveTextWidth(tierName, 1f - eraseProgress)
                    } else {
                        val writeProgress = (tierTextWipeProgress.value - wipeSplit) / (1f - wipeSplit)
                        progressiveTextWidth(tierEnglish, writeProgress)
                    }
                    val targetWidthDp = with(density) {
                        (when (tierTextPhase) {
                            4 -> {
                                val englishWidth = textWidth(tierEnglish).toFloat()
                                val koreanWidth = textWidth(tierName).toFloat()
                                englishWidth + (koreanWidth - englishWidth) * tierTextFadeProgress.value
                            }
                            5 -> wipeWidth
                            else -> textWidth(displayedText).toFloat()
                        }).toDp()
                    }
                    Box(
                        modifier = Modifier
                            // 한글 티어명의 왼쪽 시작점을 고정하고, 영문이 길어지는 만큼만
                            // 오른쪽 폭을 늘려 별 묶음을 밀어낸다. 크로스페이드 때는 원래 폭으로 복귀.
                            .width(targetWidthDp)
                            .height(24.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { tierTextScope.launch { playTierTextAnimation() } },
                            ),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        if (tierTextPhase == 4) {
                            Text(
                                text = tierEnglish,
                                style = titleStyle,
                                color = tierPalette.base,
                                softWrap = false,
                                maxLines = 1,
                                modifier = Modifier
                                    .wrapContentWidth(Alignment.Start, unbounded = true)
                                    .graphicsLayer {
                                        alpha = 1f - tierTextFadeProgress.value
                                    },
                            )
                            Text(
                                text = tierName,
                                style = titleStyle,
                                color = Gray600,
                                softWrap = false,
                                maxLines = 1,
                                modifier = Modifier
                                    .wrapContentWidth(Alignment.Start, unbounded = true)
                                    .graphicsLayer {
                                        alpha = tierTextFadeProgress.value
                                    },
                            )
                        } else if (tierTextPhase == 5) {
                            HomeTierNameWipeText(
                                koreanName = tierName,
                                englishName = tierEnglish,
                                progress = tierTextWipeProgress.value,
                                style = titleStyle,
                                koreanColor = Gray600,
                                englishColor = tierPalette.base,
                            )
                        } else {
                            val isEnglish = tierTextPhase >= 2
                            Text(
                                text = displayedText,
                                style = titleStyle,
                                color = if (isEnglish) tierPalette.base else Gray600,
                                softWrap = false,
                                maxLines = 1,
                                modifier = Modifier
                                    .wrapContentWidth(Alignment.Start, unbounded = true),
                            )
                        }
                    }
                } else {
                    Text(text = tierName, style = TqType.BodyL.figma().copy(fontWeight = FontWeight.Medium), color = Gray600)
                }
                // 별 3개 (Frame 427321742, gap 4) — 채움 #F9AC17 / 빈칸 Gray300
                TierProgressStars(
                    tierStars = tierStars,
                    growthPhase = tierVisualPhase,
                    onClick = { tierTextScope.launch { playTierTextAnimation() } },
                )
            }
        }
        // 우측 그룹 (Frame 427321760, gap 2)
        Row(
            modifier = Modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
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
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = "실전 티어",
                    style = TqType.BodyM.figma(),
                    color = tierInfoColor,
                )
                // information-circle (24x24 슬롯 안 글리프 ~14 → 18dp 벡터 중앙 배치, tint Gray400).
                // 글자와 하나의 탭 영역으로 묶어 같은 눌림 피드백과 시트를 공유한다.
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
        }
    }
}

@Composable
private fun TierProgressStars(
    tierStars: Int,
    growthPhase: Int = 0,
    onClick: () -> Unit = {},
) {
    val progress = remember { Animatable(0f) }
    var animationRunning by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val filledCount = tierStars.coerceIn(0, 3)

    suspend fun playStarSequence() {
        if (filledCount > 0 && !animationRunning) {
            animationRunning = true
            try {
                progress.snapTo(0f)
                progress.animateTo(1f, tween(650, easing = FastOutSlowInEasing))
                delay(70)
                progress.snapTo(0f)
            } finally {
                animationRunning = false
            }
        }
    }

    LaunchedEffect(growthPhase) {
        if (growthPhase == 1) playStarSequence()
    }

    Row(
        modifier = Modifier
            .width(53.dp)
            .height(32.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    onClick()
                },
            ),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(3) { index ->
            val filled = index < filledCount
            val starPhase = ((progress.value * (filledCount + 0.8f)) - index).coerceIn(0f, 1f)
            val pulse = if (!filled) 0f else {
                if (starPhase < 0.5f) starPhase * 2f else (1f - starPhase) * 2f
            }.coerceIn(0f, 1f)
            val starScale = 1f + (0.16f * pulse)
            val starColor = if (filled) lerp(StarYellow, StarHighlight, pulse) else Gray300

            Box(Modifier.size(15.dp), contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(R.drawable.ic_tier_star),
                    contentDescription = if (filled) "획득한 별" else "잠긴 별",
                    tint = starColor,
                    modifier = Modifier.size(15.dp).graphicsLayer {
                        scaleX = starScale
                        scaleY = starScale
                    },
                )
                if (filled && pulse > 0.45f) {
                    Canvas(Modifier.size(22.dp)) {
                        val alpha = ((pulse - 0.45f) / 0.55f).coerceIn(0f, 1f) * 0.75f
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val radius = 3.dp.toPx() + (2.dp.toPx() * pulse)
                        drawLine(White.copy(alpha = alpha), center.copy(x = center.x - radius), center.copy(x = center.x + radius), 1.5.dp.toPx(), StrokeCap.Round)
                        drawLine(White.copy(alpha = alpha), center.copy(y = center.y - radius), center.copy(y = center.y + radius), 1.5.dp.toPx(), StrokeCap.Round)
                    }
                }
            }
        }
    }
}

// 티어 이름 → 휘장 drawable (홈용 큰 PNG). 성장 리포트는 별도 작은 'Xs' 버전 사용 예정.
private fun tierEmblemRes(tierName: String): Int = when (tierName) {
    "브론즈" -> R.drawable.img_tier_bronze
    "실버" -> R.drawable.img_tier_silver
    "골드" -> R.drawable.img_tier_gold
    "플래티넘", "플레티넘" -> R.drawable.img_tier_platinum
    "다이아", "다이아몬드" -> R.drawable.img_tier_dia
    "마스터" -> R.drawable.img_tier_master
    else -> R.drawable.img_tier_gold
}

private data class TierTextPalette(val dark: Color, val base: Color, val highlight: Color)

private fun tierEnglishName(tierName: String): String? = when (tierName) {
    "브론즈" -> "Bronze"
    "실버" -> "Silver"
    "골드" -> "Gold"
    "플래티넘", "플레티넘" -> "Platinum"
    "다이아", "다이아몬드" -> "Diamond"
    "마스터" -> "Master"
    else -> null
}

// 각 뱃지의 어두운 외곽·기본 금속·밝은 하이라이트 계열을 글자에 그대로 대응시킨다.
private fun tierTextPalette(tierName: String): TierTextPalette = when (tierName) {
    "브론즈" -> TierTextPalette(Color(0xFF7A431F), Color(0xFFB87333), Color(0xFFFFD3A8))
    "실버" -> TierTextPalette(Color(0xFF64748B), Color(0xFFC0C7D1), Color(0xFFF8FAFC))
    "골드" -> TierTextPalette(Color(0xFF9A6700), Color(0xFFD4A72C), Color(0xFFFFE69A))
    "플래티넘", "플레티넘" -> TierTextPalette(Color(0xFF4C3F9B), Color(0xFF8B7FF0), Color(0xFFDCD8FF))
    "다이아", "다이아몬드" -> TierTextPalette(Color(0xFF087E9B), Color(0xFF45C4E8), Color(0xFFD6F7FF))
    "마스터" -> TierTextPalette(Color(0xFF5B2B8A), Color(0xFFA05CDA), Color(0xFFF0D7FF))
    else -> TierTextPalette(Gray700, Gray600, Gray400)
}

@Composable
private fun HomeTierNameWipeText(
    koreanName: String,
    englishName: String,
    progress: Float,
    style: TextStyle,
    koreanColor: Color,
    englishColor: Color,
) {
    val split = koreanName.length.toFloat() / (koreanName.length + englishName.length)
    Box {
        HomeTierNameWipeLayer(
            text = koreanName,
            progress = (progress / split).coerceIn(0f, 1f),
            appearing = false,
            style = style,
            color = koreanColor,
        )
        HomeTierNameWipeLayer(
            text = englishName,
            progress = ((progress - split) / (1f - split)).coerceIn(0f, 1f),
            appearing = true,
            style = style,
            color = englishColor,
        )
    }
}

@Composable
private fun HomeTierNameWipeLayer(
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

private fun TierTextPalette.brush(progress: Float): Brush {
    val shift = (progress * 2f) - 1f
    return Brush.linearGradient(
        colors = listOf(dark, base, highlight, base, dark),
        start = Offset(shift * 180f, 0f),
        end = Offset((shift * 180f) + 180f, 0f),
    )
}

// 실전 티어 별 채움색 (YELLOW_star, CSS #F9AC17) — 디자인 토큰 아님, 로컬.
private val StarYellow = Color(0xFFF9AC17)
private val StarHighlight = Color(0xFFFFE7A3)

// 오늘의 미션 카드.
@Composable
private fun HomeMissionCard(
    mission: TodayMission,
    isRefreshing: Boolean = false,
    onRefreshClick: () -> Unit = {},
    onStartClick: () -> Unit = {},
    onTextLineCountChanged: (titleLines: Int, descriptionLines: Int) -> Unit = { _, _ -> },
) {
    var titleLineCount by remember(mission.title) { mutableIntStateOf(1) }
    var descriptionLineCount by remember(mission.description) { mutableIntStateOf(0) }
    val targetImpactProgress = remember { Animatable(0f) }
    var targetImpactRunning by remember { mutableStateOf(false) }
    val targetImpactScope = rememberCoroutineScope()
    val targetImpactOffsetPx = with(LocalDensity.current) { 3.dp.toPx() }
    val recommendationInteraction = remember { MutableInteractionSource() }
    val recommendationPressed by recommendationInteraction.collectIsPressedAsState()
    var recommendationClickAnimating by remember { mutableStateOf(false) }
    val recommendationScope = rememberCoroutineScope()
    val recommendationVisuallyPressed = recommendationPressed || recommendationClickAnimating
    val recommendationScale by animateFloatAsState(
        targetValue = if (recommendationVisuallyPressed) 0.88f else 1f,
        animationSpec = tween(
            durationMillis = if (recommendationVisuallyPressed) 90 else 140,
            easing = FastOutSlowInEasing,
        ),
        label = "recommendationPressScale",
    )
    val missionHeaderInteraction = remember { MutableInteractionSource() }
    val missionHeaderPressed by missionHeaderInteraction.collectIsPressedAsState()
    var missionHeaderClickAnimating by remember { mutableStateOf(false) }
    val missionHeaderScope = rememberCoroutineScope()
    val missionHeaderScale by animateFloatAsState(
        targetValue = if (missionHeaderPressed || missionHeaderClickAnimating) 0.94f else 1f,
        animationSpec = tween(
            durationMillis = if (missionHeaderPressed || missionHeaderClickAnimating) 90 else 140,
            easing = FastOutSlowInEasing,
        ),
        label = "missionHeaderPressScale",
    )
    val titleInteraction = remember { MutableInteractionSource() }
    val titlePressed by titleInteraction.collectIsPressedAsState()
    var titleClickAnimating by remember { mutableStateOf(false) }
    val titleScope = rememberCoroutineScope()
    val titleScale by animateFloatAsState(
        targetValue = if (titlePressed || titleClickAnimating) 0.94f else 1f,
        animationSpec = tween(
            durationMillis = if (titlePressed || titleClickAnimating) 90 else 140,
            easing = FastOutSlowInEasing,
        ),
        label = "missionTitlePressScale",
    )
    val descriptionInteraction = remember { MutableInteractionSource() }
    val descriptionPressed by descriptionInteraction.collectIsPressedAsState()
    var descriptionClickAnimating by remember { mutableStateOf(false) }
    val descriptionScope = rememberCoroutineScope()
    val descriptionScale by animateFloatAsState(
        targetValue = if (descriptionPressed || descriptionClickAnimating) 0.94f else 1f,
        animationSpec = tween(
            durationMillis = if (descriptionPressed || descriptionClickAnimating) 90 else 140,
            easing = FastOutSlowInEasing,
        ),
        label = "missionDescriptionPressScale",
    )
    HomeCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
    ) {
        // 헤더: 오늘의 미션 + 추천 뱃지
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "오늘의 미션",
                style = TqType.BodyM.figma(),
                color = Gray800,
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = missionHeaderScale
                        scaleY = missionHeaderScale
                    }
                    .clickable(
                        interactionSource = missionHeaderInteraction,
                        indication = null,
                        onClick = {
                            if (!missionHeaderClickAnimating) {
                                missionHeaderClickAnimating = true
                                missionHeaderScope.launch {
                                    delay(100)
                                    missionHeaderClickAnimating = false
                                }
                            }
                        },
                    ),
            )
            // 추천 뱃지: 컴포넌트.css 그대로 (고정 40x22, Primary100, radius 4, 텍스트 중앙)
            // 횟수 정보가 없는 폴백 미션은 활성 상태로 유지하고,
            // 서버가 명시적으로 0회를 내려준 경우에만 비활성화한다.
            val canRefresh = mission.remainingRefreshes != 0 && !isRefreshing
            Box(
                modifier = Modifier
                    .height(22.dp)
                    .widthIn(min = 40.dp)
                    .graphicsLayer {
                        scaleX = recommendationScale
                        scaleY = recommendationScale
                        // 작은 배지는 아래 이동을 더하면 축소된 하단 여백이 상쇄되어
                        // 위쪽만 줄어드는 것처럼 보이므로, 중심 기준 축소만 적용한다.
                        translationY = 0f
                    }
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (canRefresh) Primary100 else Gray100)
                    .padding(horizontal = 7.dp)
                    .clickable(
                        enabled = canRefresh,
                        interactionSource = recommendationInteraction,
                        indication = null,
                        onClick = {
                            if (!recommendationClickAnimating) {
                                onRefreshClick()
                                recommendationClickAnimating = true
                                recommendationScope.launch {
                                    delay(100)
                                    recommendationClickAnimating = false
                                }
                            }
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (mission.refreshLimit != null && mission.remainingRefreshes != null && mission.remainingRefreshes > 0) {
                        "추천 ${mission.remainingRefreshes}"
                    } else {
                        "추천"
                    },
                    style = TqType.LabelM.figma(),
                    color = if (canRefresh) Primary600 else Gray400,
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        // 일러스트(다트 60x63) + 제목/설명
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.img_home_target),
                contentDescription = "오늘의 미션 과녁",
                modifier = Modifier
                    .size(width = 60.dp, height = 63.dp)
                    .graphicsLayer {
                        val progress = targetImpactProgress.value
                        val impactScale = 1f - (0.07f * progress)
                        scaleX = impactScale
                        scaleY = impactScale
                        translationX = targetImpactOffsetPx * progress
                        translationY = targetImpactOffsetPx * progress
                        transformOrigin = TransformOrigin(0.54f, 0.58f)
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            if (!targetImpactRunning) {
                                targetImpactRunning = true
                                targetImpactScope.launch {
                                    targetImpactProgress.snapTo(0f)
                                    targetImpactProgress.animateTo(
                                        targetValue = 1f,
                                        animationSpec = tween(
                                            durationMillis = 100,
                                            easing = FastOutSlowInEasing,
                                        ),
                                    )
                                    delay(50)
                                    targetImpactProgress.animateTo(
                                        targetValue = 0f,
                                        animationSpec = tween(
                                            durationMillis = 150,
                                            easing = LinearOutSlowInEasing,
                                        ),
                                    )
                                    targetImpactRunning = false
                                }
                            }
                        },
                    ),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                // 제목·설명 영역 256(디자인 Frame313) 상한. 제목은 필요한 최소 줄 수 안에서
                // 어절 경계를 균등하게 나누고, 설명은 AI 피드백 상세와 같은 Phrase 줄바꿈을 쓴다.
                MissionTitleText(
                    title = mission.title,
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = titleScale
                            scaleY = titleScale
                        }
                        .clickable(
                            interactionSource = titleInteraction,
                            indication = null,
                            onClick = {
                                if (!titleClickAnimating) {
                                    titleClickAnimating = true
                                    titleScope.launch {
                                        delay(100)
                                        titleClickAnimating = false
                                    }
                                }
                            },
                        ),
                    onLineCountChanged = { lines ->
                        titleLineCount = lines
                        onTextLineCountChanged(lines, descriptionLineCount)
                    },
                )
                mission.description?.let {
                    Text(
                        text = it.glueShortWords(),
                        style = TqType.BodyS.figma().copy(
                            lineBreak = LineBreak(
                                strategy = LineBreak.Strategy.HighQuality,
                                strictness = LineBreak.Strictness.Strict,
                                wordBreak = LineBreak.WordBreak.Phrase,
                            ),
                        ),
                        color = Gray600,
                        modifier = Modifier
                            .widthIn(max = 256.dp)
                            .graphicsLayer {
                                scaleX = descriptionScale
                                scaleY = descriptionScale
                            }
                            .clickable(
                                interactionSource = descriptionInteraction,
                                indication = null,
                                onClick = {
                                    if (!descriptionClickAnimating) {
                                        descriptionClickAnimating = true
                                        descriptionScope.launch {
                                            delay(100)
                                            descriptionClickAnimating = false
                                        }
                                    }
                                },
                            ),
                        onTextLayout = { result ->
                            descriptionLineCount = result.lineCount
                            onTextLineCountChanged(titleLineCount, result.lineCount)
                        },
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        // 난이도 / 예상 시간 / 보상 (세로 구분선으로 3분할)
        // CSS Frame 321: 행 54 = 상하 패딩 8 + 칸 38. (좌우 29는 칸 고정폭 합이 330을 넘는 과제약 → weight 유지)
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MissionInfo(
                label = "난이도",
                value = mission.difficulty,
                valueColor = if (mission.difficulty == "쉬움") Success else Gray700,
            )
            InfoDivider()
            MissionInfo(label = "예상 시간", value = "${mission.estimatedMinutes}분", valueColor = Gray700)
            InfoDivider()
            MissionInfo(label = "보상", value = "+${mission.rewardXp} XP", valueColor = Gray700)
        }
        Spacer(Modifier.height(14.dp)) // CSS Frame324 gap 14 (info행→버튼)
        // 미션 시작하기 (버튼M = 높이 44 / radius 12 / Primary600)
        TqButton(
            text = "미션 시작하기",
            onClick = onStartClick,
            size = TqButtonSize.Medium,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun RowScope.MissionInfo(label: String, value: String, valueColor: Color) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    var clickAnimating by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val visuallyPressed = pressed || clickAnimating
    val scale by animateFloatAsState(
        targetValue = if (visuallyPressed) 0.88f else 1f,
        animationSpec = tween(
            durationMillis = if (visuallyPressed) 90 else 140,
            easing = FastOutSlowInEasing,
        ),
        label = "missionInfoPressScale",
    )
    val labelColor by animateColorAsState(
        targetValue = if (visuallyPressed) Gray700 else Gray500,
        animationSpec = tween(
            durationMillis = if (visuallyPressed) 90 else 140,
            easing = FastOutSlowInEasing,
        ),
        label = "missionInfoPressLabelColor",
    )
    Column(
        modifier = Modifier
            .weight(1f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = {
                    if (!clickAnimating) {
                        clickAnimating = true
                        scope.launch {
                            delay(100)
                            clickAnimating = false
                        }
                    }
                },
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        // CSS Frame 316: 칸 38 = 라벨 18 + 값 20, 사이 gap 0
    ) {
        Text(text = label, style = TqType.Caption.figma(), color = labelColor)
        Text(text = value, style = TqType.LabelL.figma(), color = valueColor, textAlign = TextAlign.Center)
    }
}

@Composable
private fun InfoDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(30.dp) // 길이 26 → 30 (디자인 변경 2026-07)
            .background(Gray200), // Gray300 → Gray200 (디자인 변경 2026-07)
    )
}

// 나의 배지 컬렉션 카드 (UI 10차 신규). 흰 카드 radius 12 / 높이 64 (CSS: box-shadow 없음).
// 좌: 제목 16 medium + 부제 13 regular / 우: 44×44 테두리 박스(Gray100) 안 배지 38×38(회전은 PNG에 구워짐).
@Composable
private fun BadgeCollectionCard(onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(White)
            // 클릭 물결도 카드의 둥근 모서리 안에서만 그려져야 한다.
            .clickable(onClick = onClick)
            .padding(start = 20.dp, end = 16.dp), // CSS padding 10 16 10 20 (상하 10은 center로 흡수)
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(text = "나의 배지 컬렉션", style = TqType.BodyL.figma().copy(fontWeight = FontWeight.Medium), color = Gray700)
            Text(text = "대화 경험이 쌓일 수록 새로운 배지를 획득해요!", style = TqType.BodyS.figma(), color = Gray600)
        }
        Box(
            modifier = Modifier
                .size(44.dp)
                .border(1.dp, Gray100, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.img_home_badge),
                contentDescription = null,
                // PNG(134px@3x) = 38×38 레이어를 11° 회전까지 포함해 export한 바운딩박스 = 134/3 ≈ 44.7dp.
                // 그대로 44.7dp로 렌더해야 Figma와 1:1 (38dp로 넣으면 0.85배 축소됨). 회전·크기 보정 금지.
                // 위치는 실렌더 기준 박스 중앙 (CSS left0/top0을 offset로 옮기면 좌상으로 치우침 — 금지).
                contentScale = ContentScale.Fit,
                modifier = Modifier.requiredSize((134f / 3).dp),
            )
        }
    }
}

// 다른 미션 보기 카드. 흰 카드 radius 12 / 높이 50 (CSS 소프트 그림자).
@Composable
private fun OtherMissionsCard(onClick: () -> Unit) {
    val otherMissionsInteraction = remember { MutableInteractionSource() }
    val isOtherMissionsHovered by otherMissionsInteraction.collectIsHoveredAsState()
    // 기본 리플이 눌렸을 때 보이는 회색과 흰색 사이의 호버색.
    val otherMissionsBackground by animateColorAsState(
        targetValue = if (isOtherMissionsHovered) Color(0xFFF2F3F5) else White,
        animationSpec = tween(durationMillis = 120),
        label = "otherMissionsHover",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .softShadow(
                color = Gray1000.copy(alpha = 0.01f),
                offsetY = 8.dp,
                blur = 24.dp,
                cornerRadius = 12.dp,
            )
            .clip(RoundedCornerShape(12.dp))
            .background(otherMissionsBackground)
            .hoverable(otherMissionsInteraction)
            // indication을 지정하지 않아 기존 기본 클릭 리플의 색과 강도를 그대로 유지한다.
            .clickable(
                interactionSource = otherMissionsInteraction,
                indication = ripple(bounded = true),
                onClick = onClick,
            )
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 앞 target 아이콘(26) + 텍스트, CSS Frame328 gap 6
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.ic_home_goal),
                contentDescription = null,
                modifier = Modifier.size(26.dp),
            )
            Text(text = "다른 미션 보기", style = TqType.BodyL.figma().copy(fontWeight = FontWeight.Medium), color = Gray700) // 16 regular→medium / Gray700 (UI 10차)
        }
        Icon(
            // CSS: 뒤로가기와 같은 chevron(12x6·stroke2, 글리프 8x14)의 좌우반전 — 머티리얼 대신 실측 벡터
            painter = painterResource(R.drawable.ic_forward_chevron),
            contentDescription = null,
            tint = Gray400,
            modifier = Modifier.size(24.dp),
        )
    }
}

// ── Preview: 서버 없이 상태별로 확인. 배경 = 실제 앱 배경 Gray50(#F8FAFC). ──
private val previewSummary = HomeSummary(
    nickname = "소다123",
    level = 2,
    currentXp = 30,
    nextLevelXp = 100,
    todayMission = TodayMission(
        id = "1",
        title = "처음 보는 사람에게 짧게 인사하기", // 실제 서버값 그대로(\n 없음) — 줄바꿈은 알고리즘이 처리한 결과를 확인
        description = "가벼운 인사로 좋은 대화의 시작을 열어보세요!",
        difficulty = "쉬움",
        estimatedMinutes = 5,
        rewardXp = 20,
    ),
    archiveCount = 12,
    communityCount = 4,
    questionOfDay = "요즘 가장 설렜던 순간은?",
)

// showSystemUi=true → 상태바 실제로 그려짐. device 393dp = 디자인 기준폭과 동일(폭 착시 제거).
@Preview(name = "홈 - 성공 (393dp 실기기)", showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun HomeScreenSuccessPreview() {
    TalkQQuestTheme {
        HomeScreen(uiState = HomeUiState(summary = previewSummary), onRetry = {})
    }
}

// 긴 제목(\n 없는 실제 서버값 시뮬레이션) 자동 줄바꿈 검증용:
// 어절 중간 안 끊기는지("사/람에게" 금지) + 줄 길이 균형 잡히는지 확인.
@Preview(name = "홈 - 긴 미션 제목", showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun HomeScreenLongTitlePreview() {
    TalkQQuestTheme {
        HomeScreen(
            uiState = HomeUiState(
                summary = previewSummary.copy(
                    todayMission = previewSummary.todayMission?.copy(
                        title = "아까 전에 같이 이야기했던 사람에게 다가가서 날씨에 관해 화제 던지기",
                    ),
                ),
            ),
            onRetry = {},
        )
    }
}

// 3줄 이상 제목 검증용: 카드가 줄 수만큼 자연히 늘어나고 레이아웃이 안 깨지는지 확인.
// (maxLines 미제한 = 의도. 미션 제목은 지시문이라 말줄임으로 자르지 않음 — 정책 확정은 디자이너 확인 후)
@Preview(name = "홈 - 미션 제목 3줄(자동)", showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun HomeScreenVeryLongTitlePreview() {
    TalkQQuestTheme {
        HomeScreen(
            uiState = HomeUiState(
                summary = previewSummary.copy(
                    todayMission = previewSummary.todayMission?.copy(
                        title = "오늘 처음 마주친 카페 직원에게 눈을 마주치고 웃으면서 오늘 날씨에 대한 가벼운 한 마디 건네보기",
                    ),
                ),
            ),
            onRetry = {},
        )
    }
}

// 중간 길이 제목(2줄 예상) 자동 줄바꿈 검증용.
@Preview(name = "홈 - 미션 제목 2줄(자동)", showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun HomeScreenMediumTitlePreview() {
    TalkQQuestTheme {
        HomeScreen(
            uiState = HomeUiState(
                summary = previewSummary.copy(
                    todayMission = previewSummary.todayMission?.copy(
                        title = "처음보는 친구에게 한 번 고개 숙여 인사하기",
                    ),
                ),
            ),
            onRetry = {},
        )
    }
}

@Preview(name = "홈 - 로딩", showBackground = true, backgroundColor = 0xFFF8FAFC)
@Composable
private fun HomeScreenLoadingPreview() {
    TalkQQuestTheme {
        HomeScreen(uiState = HomeUiState(isLoading = true), onRetry = {})
    }
}

@Preview(name = "홈 - 에러", showBackground = true, backgroundColor = 0xFFF8FAFC)
@Composable
private fun HomeScreenErrorPreview() {
    TalkQQuestTheme {
        HomeScreen(uiState = HomeUiState(errorMessage = "네트워크 연결을 확인해주세요."), onRetry = {})
    }
}
