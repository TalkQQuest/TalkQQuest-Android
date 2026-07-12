package com.talkqquest.app.feature.mission.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.talkqquest.app.R
import com.talkqquest.app.core.designsystem.Error
import com.talkqquest.app.core.designsystem.FitDesign
import com.talkqquest.app.core.designsystem.Gray1000
import com.talkqquest.app.core.designsystem.Gray400
import com.talkqquest.app.core.designsystem.Gray50
import com.talkqquest.app.core.designsystem.Gray500
import com.talkqquest.app.core.designsystem.Gray600
import com.talkqquest.app.core.designsystem.Gray700
import com.talkqquest.app.core.designsystem.Gray800
import com.talkqquest.app.core.designsystem.Primary100
import com.talkqquest.app.core.designsystem.Primary600
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.White
import com.talkqquest.app.core.designsystem.component.LevelUpBurst
import com.talkqquest.app.core.designsystem.component.TqButton
import com.talkqquest.app.core.designsystem.softShadow
import com.talkqquest.app.feature.mission.data.model.MissionCompleteResult
import com.talkqquest.app.feature.mission.viewmodel.MissionCompleteUiState
import com.talkqquest.app.feature.mission.viewmodel.MissionCompleteViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// 미션 완료&XP 화면 (CSS "미션 완료& XP 획득" 4프레임 = 순차 등장 단계).
// 문구만 → 대화 시간 카드 → 체크리스트 카드 → XP 카드(바 카운트업) 순으로 나타나고,
// 컨페티는 단계마다 0.7→0.8→0.9→1.0으로 선명해짐(프레임별 opacity 전사).
// 등장 간격·카운트업 시간은 디자인에 없어 근사(350/300/300ms, 카운트업 800ms — 느리다는 피드백으로 당김).
// 연출 중 탭 = 끝 상태로 건너뛰기, 연출 후 탭 = AI 피드백 화면으로.

@Composable
fun MissionCompleteScreen(
    onContinue: () -> Unit = {},
    viewModel: MissionCompleteViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    MissionCompleteScreen(
        uiState = uiState,
        onContinue = onContinue,
        onRetry = viewModel::loadResult,
    )
}

@Composable
private fun MissionCompleteScreen(
    uiState: MissionCompleteUiState,
    onContinue: () -> Unit = {},
    onRetry: () -> Unit = {},
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
                    TqButton(text = "다시 시도", onClick = onRetry)
                }
            }

            uiState.result != null -> MissionCompleteContent(
                result = uiState.result,
                durationText = uiState.durationText,
                onContinue = onContinue,
            )
        }
    }
}

// 연출이 다 끝난 뒤 자동 전환까지 머무는 시간 (완성 화면을 인지할 여유 — 자작 값, 취향껏 조정)
private const val AUTO_ADVANCE_HOLD_MS = 1000L

// 터치 스킵 후 전환까지 = 남은 항목 등장 모션 길이 그대로(300ms) —
// 모션이 다 끝나는 순간 딜레이 없이 바로 이동 (사용자 결정: 연출은 다 보이되 끝나면 즉시)
private const val SKIP_ADVANCE_HOLD_MS = 300L

@Composable
private fun MissionCompleteContent(
    result: MissionCompleteResult,
    durationText: String,
    onContinue: () -> Unit,
    initialStage: Int = 0, // 프리뷰용: 3이면 연출 끝 상태로 그림
) {
    // 등장 단계: 0=컨페티+문구 → 1=대화 시간 카드 → 2=체크리스트 → 3=XP 카드(바 카운트업)
    var stage by remember { mutableIntStateOf(initialStage) }
    var skipped by remember { mutableStateOf(false) }
    // 자동/터치 전환이 겹쳐도 피드백으로 두 번 이동하지 않게 한 번만 통과
    var advanced by remember { mutableStateOf(false) }
    val advance = {
        if (!advanced) {
            advanced = true
            onContinue()
        }
    }
    // 레벨 칩에 표시할 레벨 — 레벨업 연출에선 바가 가득 차는 순간 바뀜
    var displayLevel by remember { mutableIntStateOf(if (initialStage >= 3) result.levelAfter else result.levelBefore) }
    var celebrationDone by remember { mutableStateOf(initialStage >= 3) }
    val xpShown = remember {
        Animatable(if (initialStage >= 3) result.xpAfter.toFloat() else result.xpBefore.toFloat())
    }
    val chipScale = remember { Animatable(1f) } // 레벨업 순간 칩이 튀는 배율
    val chipBurst = remember { Animatable(0f) } // 레벨업 순간 칩 주변 작은 폭죽 진행도 (0/1 = 안 그림)
    // skipped가 켜지면 진행 중이던 연출 코루틴이 취소되고 끝 상태로 건너뜀
    LaunchedEffect(skipped) {
        if (skipped) {
            stage = 3
            displayLevel = result.levelAfter
            xpShown.snapTo(result.xpAfter.toFloat())
            chipScale.snapTo(1f)
            chipBurst.snapTo(0f)
            celebrationDone = true
        } else if (initialStage < 3) {
            delay(350); stage = 1
            delay(300); stage = 2
            delay(300); stage = 3
            delay(250)
            if (result.levelAfter > result.levelBefore) {
                // 레벨업 연출: 바를 끝까지 채우고 → 칩이 튀며 새 레벨로(+작은 폭죽) → 새 레벨 바가 0부터 다시 참
                xpShown.animateTo(result.nextLevelXp.toFloat(), tween(700))
                displayLevel = result.levelAfter
                launch { chipBurst.snapTo(0f); chipBurst.animateTo(1f, tween(600)) } // 칩 튐과 동시 재생
                chipScale.animateTo(1.4f, tween(150))
                chipScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                delay(150)
                xpShown.snapTo(0f)
                xpShown.animateTo(result.xpAfter.toFloat(), tween(600))
            } else {
                xpShown.animateTo(result.xpAfter.toFloat(), tween(800))
            }
            celebrationDone = true
        }
    }
    val finished = celebrationDone

    // 다 뜨면 자동으로 피드백 화면으로 (피그마 프로토타입의 After delay 의도, 사용자 결정).
    // 자동 완주 = 완성 화면을 잠깐(홀드) 보여주고 이동.
    // 터치 스킵 = 안 뜬 것들이 한 번에 뜨는 등장 모션(300ms)까지 보여준 뒤 바로 이동(사용자 결정)
    //  — 0초로 하면 등장 모션이 화면 전환에 잘려서 "다 띄우는" 게 안 보임. 시간 값은 자작(아래 상수).
    LaunchedEffect(finished) {
        if (finished && initialStage < 3) { // initialStage 3 = 프리뷰 정지 화면 — 자동 이동 없음
            delay(if (skipped) SKIP_ADVANCE_HOLD_MS else AUTO_ADVANCE_HOLD_MS)
            advance()
        }
    }

    // 컨페티 선명도: 프레임별 opacity 전사(0.7/0.8/0.9/1.0) — 단계 따라 부드럽게 상승
    val confettiAlpha by animateFloatAsState(
        targetValue = 0.7f + 0.1f * stage,
        animationSpec = tween(400),
        label = "confettiAlpha",
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                if (finished) advance() else skipped = true
            }
            .statusBarsPadding()
            .padding(horizontal = 16.dp), // CSS 콘텐츠 열 left 16 / w361
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(51.dp)) // CSS top 91 - 상태바 40

        // 컨페티 일러스트: PNG(559x633)가 피그마 186x211의 3배수 export.
        // CSS상 위아래 -16 겹침(블록 257 = 195 + 문구 62) → 195 틀 가운데에 211을 넘치게 그림.
        Box(
            modifier = Modifier.size(width = 186.dp, height = 195.dp),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.img_mission_complete_confetti),
                contentDescription = null,
                modifier = Modifier
                    .requiredSize(width = 186.dp, height = 211.dp)
                    .alpha(confettiAlpha),
            )
        }
        Text(
            text = "미션 완료!",
            style = TqType.HeadingL.figma(),
            color = Gray800,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(4.dp)) // 제목 → 부제 (CSS gap 4)
        Text(
            text = "잘했어요! 오늘도 한 걸음 성장했어요",
            style = TqType.BodyL.figma(),
            color = Gray500,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(24.dp)) // 문구 묶음 → 카드 묶음 (CSS gap 24)

        // 카드 묶음 (CSS gap 12) — 하나씩 등장. 높이 0→실제로 자라며 나타나 아래 배치가 밀리지 않고 자연스럽게 확장.
        AnimatedVisibility(visible = stage >= 1, enter = fadeIn(tween(300)) + expandVertically(tween(300))) {
            DurationCard(durationText = durationText)
        }
        AnimatedVisibility(visible = stage >= 2, enter = fadeIn(tween(300)) + expandVertically(tween(300))) {
            Column {
                Spacer(Modifier.height(12.dp))
                ChecklistCard(checklist = result.checklist)
            }
        }
        AnimatedVisibility(visible = stage >= 3, enter = fadeIn(tween(300)) + expandVertically(tween(300))) {
            Column {
                Spacer(Modifier.height(12.dp))
                XpCard(
                    result = result,
                    xpShown = xpShown.value,
                    level = displayLevel,
                    chipScale = chipScale.value,
                    chipBurst = chipBurst.value,
                )
            }
        }
    }
}

// 대화 시간 카드 (CSS Frame 427321017): 361x72, 흰 r20 + 카드 그림자, 시계 24 + 라벨/시간
@Composable
private fun DurationCard(durationText: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .softShadow(color = Gray1000.copy(alpha = 0.01f), offsetY = 8.dp, blur = 24.dp, cornerRadius = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(White)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_mission_complete_clock),
            contentDescription = null,
            tint = Color.Unspecified, // 벡터에 색(#6353F0) 포함
            modifier = Modifier.size(24.dp),
        )
        Column {
            Text(text = "대화 시간", style = TqType.BodyM.figma(), color = Gray600)
            Text(text = durationText, style = TqType.HeadingM.figma(), color = Gray600)
        }
    }
}

// 체크리스트 카드 (CSS Frame 427321022): 흰 r20(그림자 없음), 상하 16, 행 gap 12
@Composable
private fun ChecklistCard(checklist: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(White)
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        checklist.forEach { item ->
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Check, // 미션 상세와 동일(머티리얼 근사)
                    contentDescription = null,
                    tint = Primary600,
                    modifier = Modifier.size(24.dp),
                )
                Text(text = item, style = TqType.BodyM.figma(), color = Gray700)
            }
        }
    }
}

// XP 카드 (CSS Frame 427321028): 흰 r20(그림자 없음), 12x16, +XP·레벨 칩 / 진행 바 / 안내 문구
// xpShown: 카운트업 중인 현재 표시 XP. 레벨업 땐 가득 찼다가 0에서 다시 참.
// level/chipScale: 레벨 칩 표시 레벨과 튀는 배율(레벨업 순간 1→1.4→1).
// chipBurst: 레벨업 순간 칩 주변 작은 폭죽 진행도(0→1, 0/1이면 안 그림).
@Composable
private fun XpCard(
    result: MissionCompleteResult,
    xpShown: Float,
    level: Int,
    chipScale: Float,
    chipBurst: Float = 0f,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "+${result.gainedXp} XP", style = TqType.HeadingM.figma(), color = Primary600)
            // 레벨 칩: Purple100 r4, 패딩 2x8. 레벨업 순간 chipScale로 튀고 주변에 작은 폭죽.
            Box {
                LevelUpBurst(progress = chipBurst, modifier = Modifier.matchParentSize()) // 칩 뒤에서 사방으로
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = chipScale
                            scaleY = chipScale
                        }
                        .background(Primary100, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                ) {
                    Text(text = "Lv.$level", style = TqType.LabelM.figma(), color = Primary600)
                }
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // 진행 바: 트랙 Purple100 / 채움 Purple600, h10 r8 (CSS 고정폭 249 = 393에서 남는 폭과 동일)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(10.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Primary100),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth((xpShown / result.nextLevelXp).coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Primary600),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = "${xpShown.toInt()}", style = TqType.LabelM.figma(), color = Primary600)
                Text(text = "/ ${result.nextLevelXp}XP", style = TqType.LabelM.figma(), color = Gray400)
            }
        }
        Text(
            text = "다음 레벨까지 ${result.nextLevelXp - result.xpAfter}XP 남았어요!",
            style = TqType.BodyS.figma(),
            color = Gray500,
        )
    }
}

// 프리뷰: 연출 끝 상태(카드 3장 + 바 채워짐). 393=디자인 기준폭, 360=흔한 폰.
@Preview(name = "미션 완료 393dp", widthDp = 393, heightDp = 852, showBackground = true)
@Preview(name = "미션 완료 360dp", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun MissionCompleteScreenPreview() {
    TalkQQuestTheme {
        Box(Modifier.background(Gray50)) {
            MissionCompleteContent(
                result = MissionCompleteResult(
                    checklist = listOf(
                        "장소 경험을 공유했어요",
                        "상대의 이야기에 공감했어요",
                        "자연스럽게 질문을 주고받았어요",
                        "긍정적인 분위기로 대화를 마무리했어요",
                    ),
                    gainedXp = 20,
                    levelBefore = 2,
                    levelAfter = 2,
                    xpBefore = 30,
                    xpAfter = 50,
                    nextLevelXp = 100,
                ),
                durationText = "02:30",
                onContinue = {},
                initialStage = 3,
            )
        }
    }
}
