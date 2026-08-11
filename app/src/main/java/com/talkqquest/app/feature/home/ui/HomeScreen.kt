package com.talkqquest.app.feature.home.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.material3.Text
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ── 화면 = 2단으로 분리 (state hoisting) ──
// (1) HomeScreen(viewModel): ViewModel과 연결하는 바깥 껍데기. 실제 앱에서 이걸 씀.
// (2) HomeScreen(uiState, onRetry): 상태를 "값으로만" 받아 그리는 부분 → 서버 없이 Preview로 검증.
// 배경은 Gray50(#F8FAFC) = 페이지 배경(디자인시스템: Primary50은 앱 껍데기, Gray50은 페이지).

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onStartMissionClick: (String) -> Unit = {}, // 오늘의 미션 "미션 시작하기" → 미션 상세
    onOtherMissionsClick: () -> Unit = {},    // "다른 미션 보기" → 미션 목록
    onNotificationClick: () -> Unit = {},     // 상단 벨 → 알림창
    onWeeklyReportClick: () -> Unit = {},     // 주간 비교 리포트 도착 모달 "보러가기" → 주간 비교 리포트
    onSheetTopChange: (Float?) -> Unit = {},  // 티어 승급 안내 시트가 하단 네비를 덮는 동안 네비 가림
    onModalSheetChange: (Boolean) -> Unit = {}, // 티어 시트가 떠 있는 동안 탭 스와이프를 끄기 위한 신호
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // 화면 복귀 시(미션 완료 후 등) XP·레벨 최신값 조용히 재조회 — 미션 목록과 같은 패턴
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.loadHome(showLoading = false) }
    HomeScreen(
        uiState = uiState,
        onRetry = viewModel::loadHome,
        onStartMissionClick = onStartMissionClick,
        onOtherMissionsClick = onOtherMissionsClick,
        onNotificationClick = onNotificationClick,
        onWeeklyReportClick = onWeeklyReportClick,
        onSheetTopChange = onSheetTopChange,
        onModalSheetChange = onModalSheetChange,
    )
}

@Composable
private fun HomeScreen(
    uiState: HomeUiState,
    onRetry: () -> Unit,
    onStartMissionClick: (String) -> Unit = {},
    onOtherMissionsClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onWeeklyReportClick: () -> Unit = {},
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
                    onStartMissionClick = onStartMissionClick,
                    onOtherMissionsClick = onOtherMissionsClick,
                    onNotificationClick = onNotificationClick,
                    onWeeklyReportClick = onWeeklyReportClick,
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
    onStartMissionClick: (String) -> Unit = {},
    onOtherMissionsClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onWeeklyReportClick: () -> Unit = {},
    onSheetTopChange: (Float?) -> Unit = {},
    onModalSheetChange: (Boolean) -> Unit = {},
) {
    // 검증용(임시): 상단 벨 탭으로 주간 비교 리포트 도착 모달 토글. 실제 트리거는 summary.hasNewWeeklyReport.
    var showWeekly by remember { mutableStateOf(summary.hasNewWeeklyReport) }
    // 실전 티어 ⓘ 탭 → 티어 승급 안내 시트(성장 리포트와 동일 공용 시트).
    var showTierHelp by remember { mutableStateOf(false) }
    // 콘텐츠가 떠 있는 하단 네비(118 몫) 위로 다 들어가게 "필요한 만큼만" 균등 축소.
    // 짧은 미션이면 자연 높이가 여유에 들어가 축소 0 = 피그마 그대로. 넘치면 넘치는 만큼만.
    // 축소는 그리기 단계(graphicsLayer)만 → 레이아웃 크기 불변 → 측정 안정(진동/깜빡임 없음).
    // scale을 여기서 val로 계산 → 홈·팝업이 같은 값을 써서 팝업도 같은 비율로 줄어듦(뒤 화면과 한 세트).
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val availPx = constraints.maxHeight - with(LocalDensity.current) { 118.dp.roundToPx() }
        var naturalPx by remember { mutableIntStateOf(0) }
        val scale = if (naturalPx > 0 && naturalPx > availPx) {
            (availPx.toFloat() / naturalPx).coerceIn(0.8f, 1f) // 하한 0.8 (과축소 방지)
        } else 1f
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    transformOrigin = TransformOrigin(0.5f, 0f) // 위쪽 기준(상단 고정, 아래로만 당겨 올림)
                }
                .onGloballyPositioned { naturalPx = it.size.height }, // graphicsLayer는 레이아웃 크기 불변 → 안정
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
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
                    onTierInfoClick = { showTierHelp = true },
                )
                Spacer(Modifier.height(12.dp)) // CSS 카드 스택 gap 12
                summary.todayMission?.let { mission ->
                    HomeMissionCard(mission = mission, onStartClick = { onStartMissionClick(mission.id) })
                }
                Spacer(Modifier.height(12.dp)) // CSS 카드 스택 gap 12
                OtherMissionsCard(onClick = onOtherMissionsClick)
                Spacer(Modifier.height(12.dp)) // CSS 카드 스택 gap 12 (다른 미션 보기 → 배지)
                BadgeCollectionCard()
            }
        }

        // 주간 비교 리포트 도착 모달 — 설계상 고정 크기(302dp 카드) 모달이라 피그마 원본 크기로 띄운다.
        // 홈 축소(scale)는 실제 2줄 미션을 담기 위한 콘텐츠 대응일 뿐, 모달 오버레이는 그걸 따라가지 않는다.
        if (showWeekly) {
            WeeklyReportModal(
                onConfirm = { showWeekly = false; onWeeklyReportClick() },
                onDismiss = { showWeekly = false },
            )
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

// 한 줄에 담기더라도 제목이 영역 폭의 절반을 넘으면 2줄로 나눠 보여줌(디자인 목업의 2줄 형태).
// 시스템 줄바꿈(Balanced)은 줄 수를 늘리지 않아서 이 규칙은 직접 처리:
// 그리기 전에 폭을 재보고 → 절반 초과·한 줄이면 → 중간을 넘는 어절까지 첫 줄에 두고 그 다음 어절부터 둘째 줄로.
private const val TITLE_MAX_WIDTH_DP = 256 // 디자인 Frame313 제목 영역 폭
private const val TITLE_SPLIT_RATIO = 0.5f // 이 비율(폭의 절반)을 넘으면 2줄로

@Composable
private fun MissionTitleText(title: String, modifier: Modifier = Modifier) {
    val style = TqType.TitleL.figma().copy(lineBreak = LineBreak.Heading)
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val displayTitle = remember(title) {
        val glued = title.glueShortWords()
        val maxWidthPx = with(density) { TITLE_MAX_WIDTH_DP.dp.toPx() }
        val oneLineWidth = measurer.measure(AnnotatedString(glued.keepWordsIntact()), style = style).size.width
        val needsSplit = oneLineWidth <= maxWidthPx && oneLineWidth > maxWidthPx * TITLE_SPLIT_RATIO
        if (needsSplit) {
            // 중간 지점 이후에 나오는 첫 일반 공백(NBSP 제외)에서 끊음
            // → 중간에 걸친 어절은 첫 줄에 남고, 그 다음 어절부터 둘째 줄 시작.
            val middle = glued.length / 2
            val spaces = glued.indices.filter { glued[it] == ' ' }
            val breakAt = spaces.firstOrNull { it >= middle } ?: spaces.lastOrNull()
            if (breakAt != null) {
                glued.replaceRange(breakAt, breakAt + 1, "\n").keepWordsIntact()
            } else {
                glued.keepWordsIntact()
            }
        } else {
            glued.keepWordsIntact()
        }
    }
    Text(
        text = displayTitle,
        style = style,
        color = Gray900,
        modifier = modifier.widthIn(max = TITLE_MAX_WIDTH_DP.dp),
    )
}

// 인사 영역 + 알림 벨.
@Composable
private fun HomeHeader(
    nickname: String,
    hasNewNotification: Boolean = false,
    onNotificationClick: () -> Unit = {},
) {
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
                    indication = ripple(bounded = true, color = Primary600), // 원 안을 채우는 원형 물결(브랜드색으로 진하게)
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
                    // ⚠️ 여기에 .rotate(-5.5f)를 또 걸면 이중 회전(~11°)이 됨 — 사용자 제보로 제거 (2026-07-20)
                    modifier = Modifier.size(width = 24.27.dp, height = 31.67.dp),
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
    onTierInfoClick: () -> Unit = {},
) {
    val xpShown = remember { Animatable(currentXp.toFloat()) }
    var displayLevel by remember { mutableIntStateOf(level) }
    val levelScale = remember { Animatable(1f) } // 레벨업 순간 Lv 글자가 튀는 배율
    val levelBurst = remember { Animatable(0f) } // 레벨업 순간 Lv 글자 주변 작은 폭죽 (완료 화면과 동일)
    LaunchedEffect(level, currentXp) {
        if (level > displayLevel) {
            xpShown.animateTo(nextLevelXp.toFloat(), tween(700))
            displayLevel = level
            launch { levelBurst.snapTo(0f); levelBurst.animateTo(1f, tween(600)) } // 글자 튐과 동시 재생
            levelScale.animateTo(1.4f, tween(150))
            levelScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
            delay(150)
            xpShown.snapTo(0f)
            xpShown.animateTo(currentXp.toFloat(), tween(600))
        } else {
            displayLevel = level
            xpShown.animateTo(currentXp.toFloat(), tween(800))
        }
    }
    HomeCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(text = "대화 진행 레벨", style = TqType.LabelL.figma().copy(fontSize = 16.sp), color = Gray700) // 16 medium / Gray700 (UI 10차)
        Spacer(Modifier.height(4.dp)) // CSS Frame428 gap 5→4 (UI 10차)
        Row(
            // CSS Frame 333 높이 22 (Lv 배지 틀 22, 텍스트 18) — 없으면 행이 18로 줄어 카드가 4px 낮아짐
            modifier = Modifier.fillMaxWidth().height(22.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box {
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
        HomeTierRow(tierName = tierName, tierStars = tierStars, onInfoClick = onTierInfoClick)
    }
}

// 실전 티어 행 (Frame 427321763, 높이 40, space-between).
// 좌: 티어 휘장 40x40 + 티어명 + 별 3개 / 우: "실전 티어" + info-circle.
@Composable
private fun HomeTierRow(tierName: String, tierStars: Int, onInfoClick: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth().height(40.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 좌측 그룹 (Frame 427321762, gap 7)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            // 티어 휘장 (사용자_티어 40x40) — PNG(512x512 정사각) 그대로 축소
            Image(
                painter = painterResource(tierEmblemRes(tierName)),
                contentDescription = "$tierName 티어",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(40.dp),
            )
            // 티어명 + 별 (Frame 427321761, gap 4)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(text = tierName, style = TqType.BodyL.figma().copy(fontWeight = FontWeight.Medium), color = Gray600)
                // 별 3개 (Frame 427321742, gap 4) — 채움 #F9AC17 / 빈칸 Gray300
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(3) { idx ->
                        Icon(
                            painter = painterResource(R.drawable.ic_tier_star),
                            contentDescription = null,
                            tint = if (idx < tierStars) StarYellow else Gray300,
                            modifier = Modifier.size(15.dp),
                        )
                    }
                }
            }
        }
        // 우측 그룹 (Frame 427321760, gap 2)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(text = "실전 티어", style = TqType.BodyM.figma(), color = Gray500)
            // information-circle (24x24 슬롯 안 글리프 ~14 → 18dp 벡터 중앙 배치, tint Gray400).
            // 탭 → 티어 승급 안내 시트(성장 리포트와 동일 공용 시트).
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onInfoClick,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_notification_info),
                    contentDescription = "실전 티어 안내",
                    tint = Gray400,
                    modifier = Modifier.size(18.dp),
                )
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

// 실전 티어 별 채움색 (YELLOW_star, CSS #F9AC17) — 디자인 토큰 아님, 로컬.
private val StarYellow = Color(0xFFF9AC17)

// 오늘의 미션 카드.
@Composable
private fun HomeMissionCard(mission: TodayMission, onStartClick: () -> Unit = {}) {
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
            Text(text = "오늘의 미션", style = TqType.BodyM.figma(), color = Gray800)
            // 추천 뱃지: 컴포넌트.css 그대로 (고정 40x22, Primary100, radius 4, 텍스트 중앙)
            Box(
                modifier = Modifier
                    .size(width = 40.dp, height = 22.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Primary100),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "추천", style = TqType.LabelM.figma(), color = Primary600)
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
                contentDescription = null,
                modifier = Modifier.size(width = 60.dp, height = 63.dp),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                // 제목·설명 영역 256(디자인 Frame313) 상한. 줄바꿈 규칙은 MissionTitleText 참고:
                // 어절 중간 끊김 방지 + 줄 길이 균형 + 한 글자 어절 고아 방지 + 반 넘는 한 줄은 2줄로.
                MissionTitleText(title = mission.title)
                mission.description?.let {
                    Text(
                        text = it,
                        style = TqType.BodyS.figma(),
                        color = Gray600,
                        modifier = Modifier.widthIn(max = 256.dp),
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
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
        // CSS Frame 316: 칸 38 = 라벨 18 + 값 20, 사이 gap 0
    ) {
        Text(text = label, style = TqType.Caption.figma(), color = Gray500)
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
private fun BadgeCollectionCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(White)
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
            .background(White)
            .clickable(onClick = onClick)
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
