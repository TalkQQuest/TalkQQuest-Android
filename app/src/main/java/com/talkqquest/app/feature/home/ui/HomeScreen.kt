package com.talkqquest.app.feature.home.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
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
    )
}

@Composable
private fun HomeScreen(
    uiState: HomeUiState,
    onRetry: () -> Unit,
    onStartMissionClick: (String) -> Unit = {},
    onOtherMissionsClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
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
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            // CSS: 카드 묶음 Frame 430 left 16, 폭 362 → 우측 여백 15 (좌우 비대칭)
            .padding(start = 16.dp, end = 15.dp),
    ) {
        HomeHeader(
            nickname = summary.nickname,
            hasNewNotification = summary.hasNewNotification,
            onNotificationClick = onNotificationClick,
        )
        Spacer(Modifier.height(18.dp)) // CSS: 인사(bottom ~132.67) → 콘텐츠(top 151) ≈ 18
        HomeLevelCard(
            level = summary.level,
            currentXp = summary.currentXp,
            nextLevelXp = summary.nextLevelXp,
        )
        Spacer(Modifier.height(16.dp))
        summary.todayMission?.let { mission ->
            HomeMissionCard(mission = mission, onStartClick = { onStartMissionClick(mission.id) })
        }
        Spacer(Modifier.height(16.dp))
        OtherMissionsCard(onClick = onOtherMissionsClick)
        // 떠 있는 하단 네비 가림 방지 여백. 네비 알약은 축소 대상 밖(MainScreen)이라
        // 화면이 축소돼도 알약 크기는 그대로 → 여백은 축소분만큼 되돌려(/scale) 원래 픽셀을 확보.
        Spacer(Modifier.height(100.dp / LocalDesignScale.current))
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
                .padding(top = 39.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
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
            Text(text = "오늘도 좋은 대화를 시작해볼까요?", style = TqType.BodyS.figma(), color = Gray600)
        }
    }
}

// 레벨 카드. 흰 카드 radius 20 / "대화 진행 레벨" + Lv·XP + 진행바.
// XP가 바뀌면(미션 완료 후 복귀 등) 숫자·바가 부드럽게 차오르고, 레벨업이면
// 미션 완료 화면과 같은 연출: 바 가득 → "Lv" 글자가 튀며 +1 → 새 레벨 바가 0부터 재충전.
@Composable
private fun HomeLevelCard(level: Int, currentXp: Int, nextLevelXp: Int) {
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
        Text(text = "대화 진행 레벨", style = TqType.LabelL.figma(), color = Gray600)
        Spacer(Modifier.height(5.dp)) // CSS Frame428 gap 5
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
            Text(text = "${xpShown.value.toInt()} / ${nextLevelXp}XP", style = TqType.LabelM.figma(), color = Gray400)
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
    }
}

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
            Text(text = "다른 미션 보기", style = TqType.BodyL.figma(), color = Gray600)
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
    nickname = "다민",
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
