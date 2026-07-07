package com.talkqquest.app.feature.home.ui

import android.graphics.BlurMaskFilter
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.talkqquest.app.R
import com.talkqquest.app.core.designsystem.Error
import com.talkqquest.app.core.designsystem.Gray1000
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
import com.talkqquest.app.core.designsystem.Success
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.White
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
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        uiState = uiState,
        onRetry = viewModel::loadHome,
    )
}

@Composable
private fun HomeScreen(
    uiState: HomeUiState,
    onRetry: () -> Unit,
) {
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
                HomeContent(summary = uiState.summary)
            }
        }
    }
}

// CSS 카드 그림자(0 8px 24px rgba(15,23,42,0.04))를 그대로 그리는 커스텀 소프트 그림자.
// Modifier.shadow(elevation)는 오프셋/색/투명도를 못 담아, 하단 네비와 같은 BlurMaskFilter 방식으로 재현.
// (팀 전체 통일이 필요하면 이 헬퍼를 공용 유틸로 빼서 공유. 지금은 홈 로컬.)
private fun Modifier.softShadow(
    color: Color,
    offsetY: Dp,
    blur: Dp,
    cornerRadius: Dp,
): Modifier = this.drawBehind {
    val paint = Paint().apply { this.color = color }
    val blurPx = blur.toPx()
    if (blurPx > 0f) {
        paint.asFrameworkPaint().maskFilter = BlurMaskFilter(blurPx, BlurMaskFilter.Blur.NORMAL)
    }
    drawIntoCanvas { canvas ->
        canvas.drawRoundRect(
            left = 0f,
            top = offsetY.toPx(),
            right = size.width,
            bottom = size.height + offsetY.toPx(),
            radiusX = cornerRadius.toPx(),
            radiusY = cornerRadius.toPx(),
            paint = paint,
        )
    }
}

// 홈 전용 카드: 흰 배경 + CSS 소프트 그림자. (공통 TqCard는 머티리얼 그림자라, 홈은 이걸로 디자인 그림자 재현)
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
                color = Gray1000.copy(alpha = 0.04f), // CSS rgba(15,23,42,0.04)
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
private fun HomeContent(summary: HomeSummary) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 16.dp),
    ) {
        HomeHeader(nickname = summary.nickname)
        Spacer(Modifier.height(18.dp)) // CSS: 인사(bottom ~132.67) → 콘텐츠(top 151) ≈ 18
        HomeLevelCard(
            level = summary.level,
            currentXp = summary.currentXp,
            nextLevelXp = summary.nextLevelXp,
        )
        Spacer(Modifier.height(16.dp))
        summary.todayMission?.let { HomeMissionCard(mission = it) }
        Spacer(Modifier.height(16.dp))
        OtherMissionsCard(onClick = { /* TODO: 미션 목록으로 이동 (NavGraph 배선) */ })
        Spacer(Modifier.height(100.dp)) // 떠 있는 하단 네비 가림 방지 여백
    }
}

// 피그마 line-height 박스처럼 위아래 여백을 살림(Compose 기본은 trim해서 텍스트 간격이 더 좁아짐).
private val FullLeading = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)

// 홈 텍스트는 이걸로 감싸 피그마 line-height 여백을 살림(공통 TqType 안 건드리고 홈 로컬).
private fun TextStyle.figma(): TextStyle = copy(lineHeightStyle = FullLeading)

// 인사 영역 + 알림 벨.
@Composable
private fun HomeHeader(nickname: String) {
    // CSS 절대위치 전사(상태바 40 기준): 인사 top 79 → 아래 39, 벨 top 50 → 아래 10 (벨이 29 위).
    // statusBarsPadding이 실제 상태바를 처리하고, 그 아래 이 오프셋만큼 배치.
    Box(modifier = Modifier.fillMaxWidth()) {
        // 알림 벨 — 우측, 상태바 아래 10
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 10.dp)
                .size(44.dp),
            contentAlignment = Alignment.Center,
        ) {
            // TODO: 알림 화면 생기면 클릭 배선. home_bell.svg 전사.
            Icon(
                painter = painterResource(R.drawable.ic_home_bell),
                contentDescription = "알림",
                tint = Gray300,
                modifier = Modifier.size(width = 20.dp, height = 22.dp),
            )
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
                    // 손 흔들기: CSS 21.52 x 29.74, -5.5° 회전
                    modifier = Modifier
                        .size(width = 21.52.dp, height = 29.74.dp)
                        .rotate(-5.5f),
                )
            }
            Text(text = "오늘도 좋은 대화를 시작해볼까요?", style = TqType.BodyS.figma(), color = Gray600)
        }
    }
}

// 레벨 카드. 흰 카드 radius 20 / "대화 진행 레벨" + Lv·XP + 진행바.
@Composable
private fun HomeLevelCard(level: Int, currentXp: Int, nextLevelXp: Int) {
    HomeCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(text = "대화 진행 레벨", style = TqType.LabelL.figma(), color = Gray600)
        Spacer(Modifier.height(5.dp)) // CSS Frame428 gap 5
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = "Lv.$level", style = TqType.LabelM.figma(), color = Primary600)
            Text(text = "$currentXp / ${nextLevelXp}XP", style = TqType.LabelM.figma(), color = Gray400)
        }
        Spacer(Modifier.height(4.dp))
        // 진행바: 트랙 Primary100 + 채움 Primary600 (currentXp/nextLevelXp 비율), 높이 10, radius 8
        val fraction = if (nextLevelXp > 0) (currentXp.toFloat() / nextLevelXp).coerceIn(0f, 1f) else 0f
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
private fun HomeMissionCard(mission: TodayMission) {
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
                // 제목·설명 영역 256(디자인 Frame313) 상한 → 이 제목은 2줄. 좁은 화면에선 그 이하로 줆(반응형).
                Text(
                    text = mission.title,
                    style = TqType.TitleL.figma(),
                    color = Gray900,
                    modifier = Modifier.widthIn(max = 256.dp),
                )
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
        Row(
            modifier = Modifier.fillMaxWidth(),
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
            onClick = { /* TODO: 미션 상세로 이동 (NavGraph 배선) */ },
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
        verticalArrangement = Arrangement.spacedBy(2.dp),
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
            .height(26.dp)
            .background(Gray300),
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
                color = Gray1000.copy(alpha = 0.04f),
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
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Gray400,
            modifier = Modifier.size(28.dp),
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
        id = 1,
        title = "처음 보는 사람에게\n짧게 인사하기", // 디자인 목업의 수동 줄바꿈 반영(실제 서버값은 폭따라 자동 줄바꿈)
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
