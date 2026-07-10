package com.talkqquest.app.feature.mission.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.talkqquest.app.R
import com.talkqquest.app.core.designsystem.Error
import com.talkqquest.app.core.designsystem.FitDesign
import com.talkqquest.app.core.designsystem.Gray1000
import com.talkqquest.app.core.designsystem.Gray200
import com.talkqquest.app.core.designsystem.Gray400
import com.talkqquest.app.core.designsystem.Gray50
import com.talkqquest.app.core.designsystem.Gray500
import com.talkqquest.app.core.designsystem.Gray600
import com.talkqquest.app.core.designsystem.Gray800
import com.talkqquest.app.core.designsystem.Primary100
import com.talkqquest.app.core.designsystem.Primary600
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.White
import com.talkqquest.app.core.designsystem.component.TqButton
import com.talkqquest.app.core.designsystem.softShadow
import com.talkqquest.app.feature.mission.data.model.FeedbackResult
import com.talkqquest.app.feature.mission.viewmodel.FeedbackUiState
import com.talkqquest.app.feature.mission.viewmodel.FeedbackViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ── AI 피드백 요약 (CSS "AI 피드백" 프레임 전사) ──
// 로봇(161x160) → 문구 → 분석 카드(4항목: 이름+바+점수+chevron) → "상세 리포트"/"홈으로" 버튼.
// 하단 네비 없음(CSS에 알약 없음) — bottomBarRoutes 미등록.
// CSS와 다른 점(합의됨):
//  - 버튼 열만 CSS가 left 17/폭 362로 콘텐츠(16/361)와 1px 어긋남 → 16/361 통일(디자이너 확인거리)
//  - 바 채움폭은 CSS 고정 px(점수와 비례 안 맞음) 대신 점수/100 비율
//  - 등장 연출(카드 600ms 뒤 등장 → 바·점수 행별 스태거 카운트업)은 디자인에 없어 자작(타이밍 근사)

@Composable
fun FeedbackScreen(
    onBack: () -> Unit = {},
    onItemClick: (Int) -> Unit = {},
    onDetailReport: () -> Unit = {},
    onHome: () -> Unit = {},
    viewModel: FeedbackViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    FeedbackScreen(
        uiState = uiState,
        onBack = onBack,
        onItemClick = onItemClick,
        onDetailReport = onDetailReport,
        onHome = onHome,
        onRetry = viewModel::loadFeedback,
    )
}

@Composable
private fun FeedbackScreen(
    uiState: FeedbackUiState,
    onBack: () -> Unit = {},
    onItemClick: (Int) -> Unit = {},
    onDetailReport: () -> Unit = {},
    onHome: () -> Unit = {},
    onRetry: () -> Unit = {},
) = FitDesign { // 작은 화면에선 디자인(393x852) 통째 축소 — 다른 화면들과 동일
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50), // 페이지 배경 Gray/50 BG (CSS)
        contentAlignment = Alignment.Center,
    ) {
        when {
            uiState.isLoading -> CircularProgressIndicator(color = Primary600)

            // 예외 E1(피드백 실패): 기본 안내 문구 + 재시도 버튼 (명세)
            uiState.errorMessage != null -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = uiState.errorMessage, style = TqType.BodyM.figma(), color = Error)
                    Spacer(Modifier.height(16.dp))
                    TqButton(text = "다시 시도", onClick = onRetry)
                }
            }

            uiState.result != null -> FeedbackContent(
                result = uiState.result,
                onBack = onBack,
                onItemClick = onItemClick,
                onDetailReport = onDetailReport,
                onHome = onHome,
            )
        }
    }
}

@Composable
private fun FeedbackContent(
    result: FeedbackResult,
    onBack: () -> Unit,
    onItemClick: (Int) -> Unit,
    onDetailReport: () -> Unit,
    onHome: () -> Unit,
    initialStage: Int = 0, // 프리뷰용: 1이면 연출 끝 상태로 그림
) {
    // 항목 4종 = 명세(E101) 필드 고정 — 순서는 CSS 카드 순서 그대로
    val items = listOf(
        "친절한 태도" to result.kindnessScore,
        "대화 주도" to result.initiativeScore,
        "공감 능력" to result.empathyScore,
        "질문 연결성" to result.questionLinkScore,
    )

    // 등장 단계: 0=로봇+문구 → 1=카드·버튼 등장 후 바/점수가 행별로 차오름
    var stage by remember { mutableIntStateOf(initialStage) }
    val shownScores = remember {
        items.map { (_, score) -> Animatable(if (initialStage >= 1) score.toFloat() else 0f) }
    }
    LaunchedEffect(Unit) {
        if (initialStage < 1) {
            delay(350); stage = 1 // 완료 화면과 같은 등장 템포 (느리다는 피드백으로 당김)
            delay(250) // 카드가 자리잡은 뒤 바가 차오르기 시작
            shownScores.forEachIndexed { index, anim ->
                launch {
                    delay(index * 120L) // 행별 스태거 — 위에서부터 차례로
                    anim.animateTo(items[index].second.toFloat(), tween(700))
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Spacer(Modifier.height(8.dp)) // 상태바(40) → 헤더(top 48) (CSS)
        // 헤더: 뒤로가기 44 왼끝만 (제목 없음, CSS chevron Gray/500)
        Box(modifier = Modifier.fillMaxWidth().height(44.dp)) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape) // 눌림 효과 원형 (아이콘 버튼 관례)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "뒤로가기",
                    tint = Gray500,
                )
            }
        }

        Spacer(Modifier.height(21.dp)) // 헤더 끝(92) → 콘텐츠(top 113) (CSS)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp), // 콘텐츠 열 left 16 / w361 (CSS)
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // 로봇 일러스트: PNG(483x480)가 피그마 161x160의 정확한 3배수 export
            Image(
                painter = painterResource(R.drawable.img_feedback_robot),
                contentDescription = null,
                modifier = Modifier.size(width = 161.dp, height = 160.dp),
            )
            Spacer(Modifier.height(12.dp)) // 로봇 → 문구 (CSS gap 12)
            Text(
                text = feedbackTitle(items.map { it.second }.average().toInt()),
                style = TqType.HeadingL.figma(),
                color = Gray800,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(4.dp)) // 제목 → 부제 (CSS gap 4)
            Text(
                text = "AI가 대화를 바탕으로 4가지 항목을 분석했어요", // 항목 4종은 명세 고정이라 문구도 고정
                style = TqType.BodyM.figma(),
                color = Gray500,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(12.dp)) // 문구 → 카드 (CSS gap 12)

            // 분석 카드: 높이 0→실제로 자라며 등장해 아래 배치가 자연스럽게 확장 (완료 화면과 동일)
            AnimatedVisibility(visible = stage >= 1, enter = fadeIn(tween(300)) + expandVertically(tween(300))) {
                ScoreCard(
                    items = items,
                    shownScores = shownScores.map { it.value },
                    onItemClick = onItemClick,
                )
            }
        }

        Spacer(Modifier.weight(1f))

        // 버튼 열: 자리는 처음부터 잡아두고 알파만 올림 — 등장 때 아래 배치가 안 튐
        val buttonsAlpha by animateFloatAsState(
            targetValue = if (stage >= 1) 1f else 0f,
            animationSpec = tween(300),
            label = "buttonsAlpha",
        )
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .graphicsLayer { alpha = buttonsAlpha },
        ) {
            TqButton(
                text = "상세 리포트",
                onClick = { if (stage >= 1) onDetailReport() },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp)) // 버튼 사이 (CSS gap 8)
            // 보조 버튼 (CSS 버튼L): 흰 배경 + Gray200 테두리 + Gray600 글자, 52/r16
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(White)
                    .border(1.dp, Gray200, RoundedCornerShape(16.dp))
                    .clickable { if (stage >= 1) onHome() },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "홈으로",
                    style = TqType.BodyL.copy(fontWeight = FontWeight.SemiBold), // TqButton과 동일 타이포
                    color = Gray600,
                )
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

// 분석 카드 (CSS Frame 427321046): 흰 r20 + 카드 그림자, 상하 16, 행 46 + 구분선 326x1, gap 12
@Composable
private fun ScoreCard(
    items: List<Pair<String, Int>>,
    shownScores: List<Float>,
    onItemClick: (Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .softShadow(color = Gray1000.copy(alpha = 0.04f), offsetY = 8.dp, blur = 24.dp, cornerRadius = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(White),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        items.forEachIndexed { index, (label, score) ->
            if (index > 0) {
                // 구분선 326x1 (CSS) — 행 사이 간격 12는 Spacer가 아니라 행 터치 면(패딩)에 포함
                Box(
                    modifier = Modifier
                        .width(326.dp)
                        .height(1.dp)
                        .background(Gray200),
                )
            }
            ScoreRow(
                label = label,
                score = score,
                shownScore = shownScores[index],
                isFirst = index == 0,
                isLast = index == items.lastIndex,
                onClick = { onItemClick(index) },
            )
        }
    }
}

// 점수 행 (CSS Frame 427321037): [이름 Body/L + 바 8(트랙 Purple100/채움 Purple600)] [점수+점] [chevron]
// shownScore: 카운트업 중인 표시 점수 — 바 채움폭도 이 값/100 비율로 같이 차오름.
// 터치 면 = 구분선~구분선의 칸 전체: 카드 위/아래 여백 16과 행↔구분선 간격 12를 전부 행의
// 패딩으로 포함해 물결이 칸을 꽉 채움(46 줄만 잡으면 칸 안에 뜬 내부 직사각형처럼 보임).
// 맨위/맨아래 행은 카드 모서리(r20) 모양으로 clip해 물결이 둥근 테두리를 그대로 따라감.
@Composable
private fun ScoreRow(
    label: String,
    score: Int,
    shownScore: Float,
    isFirst: Boolean,
    isLast: Boolean,
    onClick: () -> Unit,
) {
    val rippleShape = when {
        isFirst -> RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        isLast -> RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
        else -> RectangleShape
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(rippleShape)
            .clickable(onClick = onClick) // 행 전체 탭 = 항목 상세 (chevron이 유도)
            .padding(
                start = 16.dp, // 행 왼쪽 여백 (CSS, 오른쪽은 chevron이 카드 끝까지)
                top = if (isFirst) 16.dp else 12.dp, // 카드 상하 16·행 간격 12 (CSS 총 높이 동일)
                bottom = if (isLast) 16.dp else 12.dp,
            )
            .height(46.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = TqType.BodyL.figma(), color = Gray800)
            Spacer(Modifier.height(14.dp)) // 이름 → 바 (CSS gap 14)
            // 진행 바: 트랙 Purple100 / 채움 Purple600, h8 r8. 채움폭 = 점수/100 비율(사용자 결정)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Primary100),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth((shownScore / 100f).coerceIn(0f, 1f))
                        .height(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Primary600),
                )
            }
        }
        Spacer(Modifier.width(16.dp)) // 바 → 점수 (CSS gap 16)
        // 점수 + "점": 카운트업 중 자릿수가 변해도 바 폭이 안 흔들리게 최소폭 고정
        Box(modifier = Modifier.widthIn(min = 36.dp), contentAlignment = Alignment.Center) {
            Row {
                Text(
                    text = "${shownScore.toInt()}",
                    style = TqType.HeadingM.figma(),
                    color = Primary600,
                    modifier = Modifier.alignByBaseline(),
                )
                Spacer(Modifier.width(2.dp)) // 점수 ↔ 점 (CSS gap 2)
                Text(
                    text = "점",
                    style = TqType.BodyS.figma(),
                    color = Primary600,
                    modifier = Modifier.alignByBaseline(),
                )
            }
        }
        Box(modifier = Modifier.size(44.dp), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, // CSS: chevron-left 좌우반전
                contentDescription = "$label 상세",
                tint = Gray400,
            )
        }
    }
}

// 타이틀 문구: E101 응답에 문구 필드가 없어 클라에서 평균 점수 구간별로 결정(자작 — 기획 확인거리).
// 목업 점수 평균(85.75)이 최상 구간 → 목업 문구 "정말 잘했어요!" 그대로.
private fun feedbackTitle(average: Int): String = when {
    average >= 80 -> "정말 잘했어요!"
    average >= 60 -> "잘했어요!"
    else -> "차근차근 늘고 있어요!"
}

// 프리뷰: 연출 끝 상태(카드+바 채워짐). 393=디자인 기준폭, 360=흔한 폰.
@Preview(name = "AI 피드백 393dp", widthDp = 393, heightDp = 852, showBackground = true)
@Preview(name = "AI 피드백 360dp", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun FeedbackScreenPreview() {
    TalkQQuestTheme {
        Box(Modifier.background(Gray50)) {
            FeedbackContent(
                result = FeedbackResult(
                    kindnessScore = 92,
                    initiativeScore = 88,
                    empathyScore = 85,
                    questionLinkScore = 78,
                ),
                onBack = {},
                onItemClick = {},
                onDetailReport = {},
                onHome = {},
                initialStage = 1,
            )
        }
    }
}
