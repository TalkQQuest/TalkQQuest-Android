package com.talkqquest.app.feature.mission.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.talkqquest.app.R
import com.talkqquest.app.core.designsystem.Error
import com.talkqquest.app.core.designsystem.FitDesign
import com.talkqquest.app.core.designsystem.Gray1000
import com.talkqquest.app.core.designsystem.Gray50
import com.talkqquest.app.core.designsystem.Gray500
import com.talkqquest.app.core.designsystem.Gray700
import com.talkqquest.app.core.designsystem.Gray800
import com.talkqquest.app.core.designsystem.Gray900
import com.talkqquest.app.core.designsystem.LocalDesignScale
import com.talkqquest.app.core.designsystem.Primary600
import com.talkqquest.app.core.designsystem.Success
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.White
import com.talkqquest.app.core.designsystem.component.TqButton
import com.talkqquest.app.core.designsystem.component.TqButtonSize
import com.talkqquest.app.feature.mission.data.model.MissionDetail
import com.talkqquest.app.feature.mission.data.model.MissionListItem
import com.talkqquest.app.feature.mission.viewmodel.MissionDetailUiState
import com.talkqquest.app.feature.mission.viewmodel.MissionDetailViewModel
import kotlin.math.pow

// ── 미션 상세 (UI 1차 v2.css "미션 상세"/"미션 상세에서 북마크" 전사) ──
// 화면 = 2단 분리(state hoisting): (1) viewModel 연결용 / (2) 값만 받아 그리는 부분(Preview용). 홈 패턴 동일.

// CSS 그라데이션 rgba(248,247,255,.8) → rgba(168,159,244,.01) 재현용 지점 21개.
// 안드로이드는 투명도를 색에 먼저 곱해 보간해서 두 점만 주면 중간 보라가 죽고,
// 지점을 조금만 찍으면 경계가 계단처럼 보임 → 중간값을 20등분으로 깔아 부드럽게.
// 투명도는 직선이 아니라 완만→막판 급감 곡선: 직선으로 빼면 끝의 투명도(1%) 근처 긴 구간이
// 눈에 안 보여서 "실제 끝보다 한참 위에서 끝난 것처럼" 느껴짐. 곡선을 쓰면 눈에 보이는
// 끝이 실제 끝(효과 카드 머리)과 일치.
private val detailGradientStops: Array<Pair<Float, Color>> = Array(21) { i ->
    val t = i / 20f
    t to Color(
        red = (248f + (168f - 248f) * t) / 255f,
        green = (247f + (159f - 247f) * t) / 255f,
        blue = (255f + (244f - 255f) * t) / 255f,
        alpha = 0.79f * (1f - t).pow(0.8f) + 0.01f, // 0.8 = 직선(1.0)과 급감 곡선 사이 절충 — 진하기↓, 카드 근처 가시성 유지
    )
}

@Composable
fun MissionDetailScreen(
    viewModel: MissionDetailViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onNextClick: (Long) -> Unit = {},
    onMissionClick: (Long) -> Unit = {}, // 시트 안 카드 클릭 → 그 미션 상세
    onSheetVisibleChange: (Boolean) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    MissionDetailScreen(
        uiState = uiState,
        onBack = onBack,
        onRetry = viewModel::loadDetail,
        onToggleSave = viewModel::toggleSave,
        onDismissSaveSheet = viewModel::dismissSaveSheet,
        onNextClick = onNextClick,
        onMissionClick = onMissionClick,
        onSheetVisibleChange = onSheetVisibleChange,
    )
}

@Composable
private fun MissionDetailScreen(
    uiState: MissionDetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onToggleSave: () -> Unit,
    onDismissSaveSheet: () -> Unit = {},
    onNextClick: (Long) -> Unit = {},
    onMissionClick: (Long) -> Unit = {},
    onSheetVisibleChange: (Boolean) -> Unit = {},
) = FitDesign { // 작은 화면에선 디자인(393x852) 통째 축소 — 스크롤 없이 한 화면에
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50), // 페이지 배경 Gray/50 (CSS)
        contentAlignment = Alignment.Center,
    ) {
        when {
            uiState.isLoading -> CircularProgressIndicator(color = Primary600)

            uiState.errorMessage != null -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = uiState.errorMessage, style = TqType.BodyM.figma(), color = Error)
                    Spacer(Modifier.height(16.dp))
                    TqButton(text = "다시 시도", onClick = onRetry, size = TqButtonSize.Medium)
                }
            }

            uiState.detail != null -> {
                // 북마크 저장 시트 (미션 목록과 같은 부품 재사용)
                MissionSaveSheetScaffold(
                    savedMission = uiState.saveSheetMission,
                    recentSavedMissions = uiState.otherSavedMissions,
                    onDismiss = onDismissSaveSheet,
                    onMissionClick = { id ->
                        onDismissSaveSheet()
                        onMissionClick(id)
                    },
                    // 시트 안 "저장 목록" 카드의 북마크는 다른 미션 소관이라 여기선 못 바꿈 (TODO 서버 연동 시 처리)
                    onToggleSave = { id -> if (id == uiState.detail.id) onToggleSave() },
                    onSheetVisibleChange = onSheetVisibleChange,
                ) {
                    MissionDetailContent(
                        detail = uiState.detail,
                        onBack = onBack,
                        onToggleSave = onToggleSave,
                        onNextClick = onNextClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun MissionDetailContent(
    detail: MissionDetail,
    onBack: () -> Unit,
    onToggleSave: () -> Unit,
    onNextClick: (Long) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        // 세로 배치: 내용이 다 들어가는 화면에선 여백을 늘려 피그마 비례를 유지.
        // 작은 화면에선 여백·일러스트를 부족한 비율만큼 점차 축소(최소 0.5배, 사용자 결정),
        // 그래도 넘치는 극소형만 스크롤. 글자·카드·버튼은 가독성 위해 고정.
        val compact = maxHeight < 760.dp
        val shrink = if (compact) (maxHeight / 760.dp).coerceIn(0.5f, 1f) else 1f

        // 상단 보라 그라데이션 배경 (CSS Frame 436: 높이 462).
        // CSS에서 그라데이션 끝(462)은 효과 카드 시작(437)보다 25 아래 — "카드 머리를 살짝 덮는" 관계.
        // 카드 위치는 화면 크기·문구 개수 따라 움직이므로 가정 계산 대신 실제 위치를 측정해 따라감.
        val density = LocalDensity.current
        var cardsTop by remember { mutableStateOf<Dp?>(null) }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(
                    when {
                        compact -> 462.dp * shrink // 작은 화면은 CSS 값을 축소 비율만큼
                        cardsTop != null -> cardsTop!! + 25.dp // 실측 카드 머리 + 25 (CSS 462-437)
                        else -> maxHeight - 390.dp // 측정 전 첫 프레임 임시값
                    },
                )
                .background(Brush.verticalGradient(*detailGradientStops)),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(if (compact) Modifier.verticalScroll(rememberScrollState()) else Modifier)
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            Spacer(Modifier.height(8.dp)) // 상태바 → 헤더 (CSS Frame 361 top 48 = 상태바 40 + 8)
            // 헤더 (CSS Frame 427321190): 뒤로가기 44 왼끝, 제목은 화면 가로 정중앙.
            // (제목 x166~226 → 중심 196 = 화면 중심 393/2. 개정 전 "화살표 옆" 배치에서 변경됨)
            Box(modifier = Modifier.fillMaxWidth().height(44.dp)) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape) // 눌림 효과를 네모 대신 동그라미로 (아이콘 버튼 관례)
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "뒤로가기",
                        tint = Gray500, // 기본 프레임 기준 (변형 프레임은 Gray800 — 목록과 통일해 Gray500, 합의됨)
                    )
                }
                Text(
                    text = "미션 상세",
                    style = TqType.BodyL.figma(),
                    color = Gray800,
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            // 헤더(92) → 중앙 블록(top 139) (CSS 47). 큰 화면의 여분 공간은 히어로 위아래로만 나눔.
            if (compact) Spacer(Modifier.height(47.dp * shrink)) else Spacer(Modifier.weight(1f))

            // 중앙 블록 (CSS Frame 435): 다트 과녁 → 제목 → 칩 3개, 모두 가운데 정렬
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // 다트 과녁 일러스트 (CSS: 117x123.68 + -7도 기울임 → 기울인 크기 131x137)
                // PNG = 피그마 자르기(안쪽 회전) 반영본에 디자이너 회전 -7도까지 구운 최종본.
                // (내보내기 export본은 자르기 전 원본이 나와서, fill의 이미지 이름 클릭으로 받은 파일 사용)
                Image(
                    painter = painterResource(R.drawable.img_mission_target),
                    contentDescription = null,
                    // 원래 크기 확정(확대 안 함 — 사용자 결정). 작은 화면에선 비율 축소만.
                    modifier = Modifier.size(width = 131.dp * shrink, height = 137.dp * shrink),
                )
                // 이미지 → 제목: CSS 수치는 2지만 그건 이미지 틀 안 투명 여백 포함 기준(우리 PNG는 잘라냄).
                // 이 간격은 CSS에 숫자로 안 드러나 목업과 눈 대조로 맞춘 값(16). 작은 화면에선 비율 축소.
                Spacer(Modifier.height(16.dp * shrink))
                Text(
                    // 서버 가변 제목: 어절 보호 + 한 글자 어절 고아 방지 + 줄 균형 (홈과 동일 규칙)
                    text = detail.title.glueShortWords().keepWordsIntact(),
                    style = TqType.HeadingL.figma().copy(lineBreak = LineBreak.Heading),
                    color = Gray700,
                    textAlign = TextAlign.Center,
                    // 디자인은 폭 186에 2줄 — 가변 제목 대응으로 최대 260까지 허용, 길면 3줄 (합의됨)
                    modifier = Modifier.widthIn(max = 260.dp),
                )
                Spacer(Modifier.height(16.dp)) // 제목 → 칩 (CSS Frame 434 gap)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { // 칩 간격 (CSS Frame 349 gap)
                    DetailDifficultyChip(difficulty = detail.difficulty)
                    DetailInfoChip(text = detail.category)
                    DetailInfoChip(text = "${detail.estimatedMinutes}분")
                }
            }

            // 중앙 블록(392) → 카드(top 437) (CSS 45)
            if (compact) Spacer(Modifier.height(45.dp * shrink)) else Spacer(Modifier.weight(1f))

            // 효과·보상 카드 (CSS Frame 441: 좌우 17, 카드 간격 12)
            Column(
                modifier = Modifier
                    .padding(horizontal = 17.dp)
                    // 그라데이션이 카드 머리를 살짝 덮도록 카드의 실제 세로 위치를 올려보냄
                    .onGloballyPositioned { coords ->
                        cardsTop = with(density) { coords.positionInRoot().y.toDp() }
                    },
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                BenefitsCard(benefits = detail.benefits)
                RewardCard(rewardXp = detail.rewardXp)
            }

            Spacer(Modifier.height(54.dp * shrink)) // 카드(606) → 하단 액션(top 660) (CSS 54, 화면 커져도 고정·작으면 축소)

            // 하단 액션 (CSS Frame 366): 북마크 + "다음" 버튼
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp), // 좌우 24 (CSS) — 하단 여백은 아래 알약 스페이서가 처리
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp), // 북마크 ↔ 버튼 (CSS gap)
            ) {
                // 북마크: 리플 없이 아이콘 색만 변화 (목록과 동일 규칙)
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onToggleSave,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(
                            if (detail.isSaved) R.drawable.ic_mission_bookmark_filled else R.drawable.ic_mission_bookmark,
                        ),
                        contentDescription = if (detail.isSaved) "북마크 해제" else "북마크",
                    )
                }
                TqButton(
                    text = "다음",
                    onClick = { onNextClick(detail.id) },
                    modifier = Modifier.weight(1f), // CSS 295 고정폭 대신 남는 폭 전부 (화면 크기 대응)
                    size = TqButtonSize.Large, // 52 / radius 16 = CSS 버튼L과 동일
                )
            }

            // 버튼 → 알약 간격 16 + 알약 64 + 알약 아래 12 (CSS). 알약은 축소 대상이 아니라
            // 화면이 축소된 만큼 나눠서 실제(물리) 크기를 유지.
            Spacer(Modifier.height(92.dp / LocalDesignScale.current))
        }
    }
}

// 난이도 칩 (CSS Frame 345): padding 4x12, radius 16, Label/L. 색은 미션 목록 알약과 동일 값.
@Composable
private fun DetailDifficultyChip(difficulty: String) {
    val (textColor, bgColor) = when (difficulty) {
        "쉬움" -> Success to EasyBg
        "보통" -> OrangeText to NormalBg
        "어려움" -> Error to HardBg
        else -> Gray700 to Gray50
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .padding(horizontal = 12.dp, vertical = 4.dp),
    ) {
        Text(text = difficulty, style = TqType.LabelL.figma(), color = textColor)
    }
}

// 카테고리·시간 칩 (CSS Frame 342/346): padding 4x12, radius 16, 배경 Gray50, Body/M Gray500.
// 배경이 라벤더 그라데이션 위라 Gray50 알약이 도드라져 보임 (CSS 값 그대로).
@Composable
private fun DetailInfoChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Gray50)
            .padding(horizontal = 12.dp, vertical = 4.dp),
    ) {
        Text(text = text, style = TqType.BodyM.figma(), color = Gray500)
    }
}

// 효과 카드 (CSS Frame 446): 흰 배경 r20 카드그림자, padding 12x16, 줄 간격 6.
// 줄 = 체크(24, 보라) + 문구. 개수는 서버 가변(디자인은 2개 예시).
@Composable
private fun BenefitsCard(benefits: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .softShadow(color = Gray1000.copy(alpha = 0.04f), offsetY = 8.dp, blur = 24.dp, cornerRadius = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        benefits.forEach { benefit ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp), // 체크 ↔ 문구 (CSS gap)
            ) {
                Icon(
                    imageVector = Icons.Default.Check, // CSS 체크와 근사(머티리얼) — 어긋나면 SVG로 교체
                    contentDescription = null,
                    tint = Primary600,
                    modifier = Modifier.size(24.dp),
                )
                Text(text = benefit, style = TqType.BodyL.figma(), color = Gray900)
            }
        }
    }
}

// 보상 카드 (CSS Frame 440): 흰 배경 r20 카드그림자, padding 12x16, 선물상자(55) + 문구.
@Composable
private fun RewardCard(rewardXp: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .softShadow(color = Gray1000.copy(alpha = 0.04f), offsetY = 8.dp, blur = 24.dp, cornerRadius = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(15.dp), // 이미지 ↔ 문구 (CSS gap)
    ) {
        Image(
            painter = painterResource(R.drawable.img_mission_gift),
            contentDescription = null,
            modifier = Modifier.size(55.dp),
        )
        Column {
            Text(text = "미션 보상", style = TqType.BodyM.figma(), color = Gray700)
            Text(text = "+${rewardXp} XP", style = TqType.TitleL.figma(), color = Primary600)
        }
    }
}

// ── Preview ──
private val previewDetail = MissionDetail(
    id = 1,
    title = "처음 보는 사람에게 짧게 인사하기",
    category = "짧은 대화",
    difficulty = "쉬움",
    estimatedMinutes = 2,
    rewardXp = 20,
    benefits = listOf(
        "낯선 사람과의 첫 대화에 자신감이 생겨요",
        "자연스럽게 대화를 이어갈 수 있어요",
    ),
)

@Preview(name = "미션 상세 (393dp 실기기)", showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun MissionDetailScreenPreview() {
    TalkQQuestTheme {
        MissionDetailScreen(
            uiState = MissionDetailUiState(detail = previewDetail),
            onBack = {}, onRetry = {}, onToggleSave = {},
        )
    }
}

// 긴 제목(3줄) + 효과 3개 + 어려움 난이도: 가변 데이터 대응 확인.
@Preview(name = "미션 상세 - 긴 제목·효과 3개", showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun MissionDetailScreenLongPreview() {
    TalkQQuestTheme {
        MissionDetailScreen(
            uiState = MissionDetailUiState(
                detail = previewDetail.copy(
                    title = "동아리에서 관심사가 비슷한 사람에게 먼저 말 걸어보기",
                    difficulty = "어려움",
                    estimatedMinutes = 15,
                    rewardXp = 60,
                    benefits = listOf(
                        "일상 속 이야깃거리를 찾는 눈이 생겨요",
                        "상대방과 공감대를 만들 수 있어요",
                        "대화를 오래 이어가는 힘이 생겨요",
                    ),
                ),
            ),
            onBack = {}, onRetry = {}, onToggleSave = {},
        )
    }
}

// 좁은 화면(320dp): 스크롤·버튼 겹침 확인.
@Preview(name = "미션 상세 (320dp 좁은 화면)", showSystemUi = true, device = "spec:width=320dp,height=640dp")
@Composable
private fun MissionDetailScreenNarrowPreview() {
    TalkQQuestTheme {
        MissionDetailScreen(
            uiState = MissionDetailUiState(detail = previewDetail),
            onBack = {}, onRetry = {}, onToggleSave = {},
        )
    }
}

@Preview(name = "미션 상세 - 에러", showBackground = true, backgroundColor = 0xFFF8FAFC)
@Composable
private fun MissionDetailScreenErrorPreview() {
    TalkQQuestTheme {
        MissionDetailScreen(
            uiState = MissionDetailUiState(errorMessage = "네트워크 연결을 확인해주세요."),
            onBack = {}, onRetry = {}, onToggleSave = {},
        )
    }
}
