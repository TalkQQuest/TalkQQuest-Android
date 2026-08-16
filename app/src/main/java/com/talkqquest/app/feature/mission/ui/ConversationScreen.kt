package com.talkqquest.app.feature.mission.ui

import android.graphics.BlurMaskFilter
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.talkqquest.app.R
import com.talkqquest.app.core.designsystem.Error
import com.talkqquest.app.core.designsystem.FitDesign
import com.talkqquest.app.core.designsystem.Gray100
import com.talkqquest.app.core.designsystem.Gray1000
import com.talkqquest.app.core.designsystem.Gray200
import com.talkqquest.app.core.designsystem.Gray300
import com.talkqquest.app.core.designsystem.Gray50
import com.talkqquest.app.core.designsystem.Gray500
import com.talkqquest.app.core.designsystem.Gray600
import com.talkqquest.app.core.designsystem.Gray700
import com.talkqquest.app.core.designsystem.Gray800
import com.talkqquest.app.core.designsystem.Gray900
import com.talkqquest.app.core.designsystem.LocalDesignScale
import com.talkqquest.app.core.designsystem.LocalStatusBarCompensation
import com.talkqquest.app.core.designsystem.ModalDimOverlay
import com.talkqquest.app.core.designsystem.modalCardEnter
import com.talkqquest.app.core.designsystem.modalCardExit
import com.talkqquest.app.core.designsystem.Primary600
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.White
import com.talkqquest.app.core.designsystem.coverStatusBarCompensation
import com.talkqquest.app.core.designsystem.component.TqButton
import com.talkqquest.app.core.designsystem.component.TqButtonSize
import com.talkqquest.app.core.designsystem.softShadow
import com.talkqquest.app.feature.mission.data.model.ChatMessage
import com.talkqquest.app.feature.mission.viewmodel.ConversationUiState
import com.talkqquest.app.feature.mission.viewmodel.ConversationViewModel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ── 대화 진행 (UI 1차 v3 "미션(대화 시작)/(대화 길어진 경우)/(추천 답변 선택함)/미션 종료 팝업" 전사) ──
// 대화 준비 "미션 시작하기"에서 들어옴. 채팅(목록류)이라 FitDesign 대신 스크롤로 대응.
// 말풍선 텍스트 색은 CSS가 #1C1C1C(디자인시스템 Gray900 #1E293B와 다른 별도 값)라 그대로 둠.
private val ChatText = Color(0xFF1C1C1C)
private val TimeText = Color(0xFF999999) // CSS "Gray 400(푸터)" — 시스템 Gray400(#94A3B8)과 다른 값

// 아래 스크롤 마스크: 메시지가 입력창 뒤로 내려갈 때만 배경에 자연스럽게 녹인다.
// 시작점에 알파가 있으면 실기기에서 마스크 경계가 흐린 가로 띠처럼 보이므로
// 완전 투명에서 시작해 아래로 갈수록 부드럽게 Gray50이 덮이도록 한다.
private fun scrollMaskBrush(): Brush = Brush.verticalGradient(
    0f to Gray50.copy(alpha = 0f),
    0.25f to Gray50.copy(alpha = 0.12f),
    0.65f to Gray50.copy(alpha = 0.52f),
    1f to Gray50.copy(alpha = 0.8f),
)

// 위 스크롤 페이드 (CSS "대화 길어졌을 때_ 스크롤시 위 흐림 효과" Frame 427320986).
//
// CSS 원본은 직선 3토막이다 — 0~24% 알파 0.8 평평 / 24~62% 0.8→0.45 / 62~100% 0.45→0.
// 이대로 옮겼더니 두 가지가 눈에 걸렸다(사용자 지적, 스크린샷 실측으로 확인):
//   ① 24%·62%에서 기울기가 꺾여 그 지점이 경계선처럼 보인다(마흐 밴드)
//   ② 맨 위 24%는 알파가 변하지 않아 "위쪽만 덜 흐린" 단계처럼 읽힌다
// 실측 알파는 이론값과 소수점 셋째 자리까지 일치했으므로 구현 오류가 아니라 곡선 모양 문제다.
//
// 그래서 양 끝(시작 0.8 / 끝 0)은 CSS 그대로 두고 사이를 smoothstep(3p²-2p³)으로 잇는다.
// 꺾이는 점도, 평평한 구간도 없어진다. 11스텝이면 계단이 눈에 보이지 않는다.
private fun topFadeBrush(): Brush {
    val stops = Array(11) { i ->
        val p = i / 10f
        val s = 3f * p * p - 2f * p * p * p
        p to Gray50.copy(alpha = 0.8f * (1f - s))
    }
    return Brush.verticalGradient(*stops)
}

@Composable
fun ConversationScreen(
    viewModel: ConversationViewModel = hiltViewModel(),
    onExitConfirm: (durationSec: Long) -> Unit = {}, // 대화 완료 → 미션 완료&XP (대화 시간 전달)
    onBack: () -> Unit = {},                         // 뒤로가기 → 저장하지 않고 종료
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // 기기 뒤로가기도 헤더 뒤로가기와 같은 "대화 이탈" 흐름을 탄다.
    // 확인 팝업이 떠 있을 때는 화면을 나가지 않고 해당 팝업만 닫는다.
    BackHandler {
        when {
            uiState.showCompleteDialog -> viewModel.setCompleteDialogVisible(false)
            uiState.showLeaveDialog -> viewModel.setLeaveDialogVisible(false)
            else -> viewModel.setLeaveDialogVisible(true)
        }
    }
    ConversationScreen(
        uiState = uiState,
        onRetry = viewModel::startConversation,
        onInputChange = viewModel::onInputChange,
        onSend = viewModel::sendMessage,
        onToggleRecommendations = viewModel::toggleRecommendations,
        onSelectRecommendation = viewModel::selectRecommendation,
        onBackClick = { viewModel.setLeaveDialogVisible(true) },
        onCompleteClick = { viewModel.setCompleteDialogVisible(true) },
        onCompleteDismiss = { viewModel.setCompleteDialogVisible(false) },
        onCompleteConfirm = {
            if (uiState.messages.any { it.isFromUser }) {
                onExitConfirm(viewModel.elapsedSeconds())
            } else {
                // 사용자 발화가 없으면 완료·XP 화면으로 보내지 않고 대화만 종료한다.
                // 서버도 같은 조건에서 미션 기록과 mission_completed 알림 생성을 막지만,
                // 앱에서도 먼저 분기해 400 오류 화면이 잠깐 노출되지 않게 한다.
                viewModel.abandonConversation()
                onBack()
            }
        },
        onLeaveDismiss = { viewModel.setLeaveDialogVisible(false) },
        // 나가기 = 대화 포기. 서버에 열려 있는 대화를 abandoned로 닫고 화면을 벗어난다.
        // 예전엔 아무것도 안 보내서 서버엔 in_progress로 남았다.
        onLeaveConfirm = { viewModel.abandonConversation(); onBack() },
    )
}

@Composable
private fun ConversationScreen(
    uiState: ConversationUiState,
    onRetry: () -> Unit,
    onInputChange: (String) -> Unit = {},
    onSend: () -> Unit = {},
    onToggleRecommendations: () -> Unit = {},
    onSelectRecommendation: (String) -> Unit = {},
    onBackClick: () -> Unit = {},
    onCompleteClick: () -> Unit = {},
    onCompleteDismiss: () -> Unit = {},
    onCompleteConfirm: () -> Unit = {},
    onLeaveDismiss: () -> Unit = {},
    onLeaveConfirm: () -> Unit = {},
) = FitDesign { // 작은 화면에선 디자인(393x852) 통째 축소 — 다른 화면들과 크기감 통일
    var introAnimationFinished by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50), // 페이지 배경 Gray/50 (CSS)
        contentAlignment = Alignment.Center,
    ) {
        when {
            // 대화 세션이 만들어질 때까지 시안의 "대화 진입 애니메이션" 화면을 그대로 띄운다.
            // 별도 destination으로 끼우면 이 화면이 끝난 뒤 여기서 또 로딩이 돌아 두 번 기다리게 된다.
            // compensateStatusBar = false — 이 화면이 이미 FitDesign 안이라 보정이 두 번 먹지 않게.
            uiState.isLoading || !introAnimationFinished -> ConversationIntroScreen(
                onBack = onBackClick,
                compensateStatusBar = false,
                onAnimationFinished = { introAnimationFinished = true },
            )

            uiState.errorMessage != null -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = uiState.errorMessage, style = TqType.BodyM.figma(), color = Error)
                    Spacer(Modifier.height(16.dp))
                    TqButton(text = "다시 시도", onClick = onRetry, size = TqButtonSize.Medium)
                }
            }

            else -> ConversationContent(
                uiState = uiState,
                onInputChange = onInputChange,
                onSend = onSend,
                onToggleRecommendations = onToggleRecommendations,
                onSelectRecommendation = onSelectRecommendation,
                onBackClick = onBackClick,
                onCompleteClick = onCompleteClick,
            )
        }

        // 확인 팝업 2종.
        // ★ 별도 FitDesign으로 감싸지 않는다: 이미 이 화면 전체가 바깥 FitDesign 안이라
        //   여기서 또 감싸면 팝업만 다른 좌표계가 돼 위치가 어긋남(살짝 아래로 밀렸던 원인).
        //   메인 콘텐츠와 같은 프레임에 두면 CSS top 313이 다른 요소들과 동일 규칙으로 맞음.
        ModalDimOverlay(
            visible = uiState.showCompleteDialog || uiState.showLeaveDialog,
            modifier = Modifier.coverStatusBarCompensation(LocalStatusBarCompensation.current),
            onDismiss = if (uiState.showCompleteDialog) onCompleteDismiss else onLeaveDismiss,
        )
        AnimatedVisibility(
            visible = uiState.showCompleteDialog,
            enter = modalCardEnter(),
            exit = modalCardExit(),
        ) {
            // CSS "대화 종료 팝업(완료 버튼)" — 헤더 "대화 완료"에서 열림
            ConversationConfirmDialog(
                title = "대화를 종료하시겠어요?",
                description = "대화를 종료하면 현재 미션이 완료됩니다.",
                confirmText = "완료하기",
                confirmColor = Primary600,
                onContinue = onCompleteDismiss,
                onConfirm = onCompleteConfirm,
            )
        }
        AnimatedVisibility(
            visible = uiState.showLeaveDialog,
            enter = modalCardEnter(),
            exit = modalCardExit(),
        ) {
            // CSS "대화 이탈 팝업(뒤로가기)" — 헤더 뒤로가기에서 열림. 확인 버튼만 RED
            ConversationConfirmDialog(
                title = "정말 나가시겠습니까?",
                description = "여기서 나가면 대화 내역이 저장되지 않아요.",
                confirmText = "종료하기",
                confirmColor = Error,
                onContinue = onLeaveDismiss,
                onConfirm = onLeaveConfirm,
            )
        }
    }
}

@Composable
private fun ConversationContent(
    uiState: ConversationUiState,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onToggleRecommendations: () -> Unit,
    onSelectRecommendation: (String) -> Unit,
    onBackClick: () -> Unit,
    onCompleteClick: () -> Unit,
) {
    val recommendationTitleKorean = "톡깨의 추천 답변"
    val recommendationTitleCollapsed = "답장이 고민되시나요?"
    // 서버 응답과 실제 표시 목록을 분리한다. 새 응답이 도착해도 기존 문구를 즉시
    // 갈아 끼우지 않고, 기존 문구 퇴장 → 교체 → 새 문구 등장 순서로 보여 준다.
    var displayedRecommendations by remember { mutableStateOf<List<String>>(emptyList()) }
    var recommendationsPresented by remember { mutableStateOf(false) }
    val recommendationItemsProgress = remember { Animatable(0f) }
    val recommendationsReady = recommendationsPresented && displayedRecommendations.isNotEmpty()
    val recommendationTitleShineProgress = remember { Animatable(0f) }
    // 0 = 펼친 제목, 1 = 접힌 제목. 중간 탭은 현재 값에서 목표만 반대로 바꿔 자연스럽게 되감는다.
    val recommendationTitleTransitionProgress = remember {
        Animatable(if (recommendationsReady && uiState.recommendationsExpanded) 0f else 1f)
    }
    val recommendationTitleScope = rememberCoroutineScope()
    var recommendationTitleJob by remember { mutableStateOf<Job?>(null) }
    var recommendationTitleAnimationId by remember { mutableIntStateOf(0) }
    val recommendationRippleInteraction = remember { MutableInteractionSource() }
    // 화면 상태 반영보다 빠르게 연속 탭해도, 가장 마지막 탭 방향을 기준으로 삼는다.
    var requestedRecommendationsExpanded by remember {
        mutableStateOf(recommendationsReady && uiState.recommendationsExpanded)
    }

    LaunchedEffect(uiState.recommendations) {
        val incoming = uiState.recommendations
        if (incoming == displayedRecommendations) return@LaunchedEffect

        if (incoming.isEmpty()) {
            recommendationItemsProgress.animateTo(
                targetValue = 0f,
                animationSpec = tween(160, easing = FastOutSlowInEasing),
            )
            recommendationsPresented = false
            displayedRecommendations = emptyList()
            return@LaunchedEffect
        }

        if (displayedRecommendations.isEmpty()) {
            // 최초 응답: 접힌 바가 먼저 자리 잡은 다음 카드와 문구를 일정한 박자로 펼친다.
            displayedRecommendations = incoming
            recommendationItemsProgress.snapTo(0f)
            withFrameNanos { }
            recommendationsPresented = true
            delay(80)
        } else {
            // 갱신 응답: 이전 문구가 완전히 사라진 프레임에서만 실제 데이터를 교체한다.
            recommendationItemsProgress.animateTo(
                targetValue = 0f,
                animationSpec = tween(180, easing = FastOutSlowInEasing),
            )
            displayedRecommendations = incoming
            recommendationItemsProgress.snapTo(0f)
            withFrameNanos { }
        }

        recommendationItemsProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(420, easing = LinearEasing),
        )
    }

    LaunchedEffect(uiState.recommendationsExpanded) {
        if (recommendationsReady) {
            requestedRecommendationsExpanded = uiState.recommendationsExpanded
        }
    }

    fun animateRecommendationTitle(targetProgress: Float) {
        val animationId = ++recommendationTitleAnimationId
        // 글자 폭은 고정한 채 각 글자의 투명도만 기존 55ms 순서대로 바꾼다.
        recommendationTitleJob?.cancel()
        recommendationTitleJob = recommendationTitleScope.launch {
            try {
                recommendationTitleShineProgress.snapTo(0f)
                val fullDuration = (recommendationTitleKorean.length + recommendationTitleCollapsed.length) * 55
                val remainingFraction = kotlin.math.abs(
                    recommendationTitleTransitionProgress.value - targetProgress,
                )
                recommendationTitleTransitionProgress.animateTo(
                    targetValue = targetProgress,
                    animationSpec = tween(
                        durationMillis = (fullDuration * remainingFraction).roundToInt().coerceAtLeast(1),
                        easing = LinearEasing,
                    ),
                )
                // 펼친 제목이 모두 나타난 뒤에만 광택을 한 번 통과시킨다.
                if (targetProgress == 0f) {
                    recommendationTitleShineProgress.snapTo(0f)
                    recommendationTitleShineProgress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = 420, easing = LinearEasing),
                    )
                }
            } finally {
                // 취소된 이전 작업이 새 작업의 제목·광택 상태를 덮어쓰지 않도록 막는다.
                if (animationId == recommendationTitleAnimationId) {
                    recommendationTitleShineProgress.snapTo(0f)
                    recommendationTitleJob = null
                }
            }
        }
    }

    fun toggleRecommendationsWithTitleAnimation() {
        if (!recommendationsReady) return
        requestedRecommendationsExpanded = !requestedRecommendationsExpanded
        onToggleRecommendations()
        animateRecommendationTitle(if (requestedRecommendationsExpanded) 0f else 1f)
    }

    // 추천 데이터가 오기 전에는 접힌 바를 유지하고, 처음 도착한 순간 현재 카드 전환으로 자동 펼친다.
    LaunchedEffect(recommendationsReady) {
        if (recommendationsReady) {
            requestedRecommendationsExpanded = uiState.recommendationsExpanded
            if (uiState.recommendationsExpanded) animateRecommendationTitle(0f)
        } else {
            requestedRecommendationsExpanded = false
            recommendationTitleJob?.cancel()
            recommendationTitleTransitionProgress.snapTo(1f)
            recommendationTitleShineProgress.snapTo(0f)
        }
    }

    // ── 전송 비행 연출(카톡 신모션): 내 메시지가 입력창 왼쪽에서 출발해 오른쪽으로
    // 미끄러지다 벽 앞에서 회전하며 위로 빨려 올라가 제자리에 안착. 진짜 리스트 아이템은
    // 비행이 끝날 때까지 숨기고, 화면 위를 나는 오버레이 말풍선이 연기함. ──
    val flownIds = remember { mutableSetOf<String>() }
    var flightMessage by remember { androidx.compose.runtime.mutableStateOf<ChatMessage?>(null) }
    val flightProgress = remember { Animatable(1f) }
    // 좌표 실측(px): 출발점(입력창)·도착점(리스트 바닥) 계산용
    var contentOrigin by remember { androidx.compose.runtime.mutableStateOf(Offset.Zero) }
    var contentWidthPx by remember { mutableIntStateOf(0) }
    // 좌표는 비행 말풍선의 layout 단계에서만 읽는다. 일반 상태로 두면 추천 카드 높이가
    // 변하는 매 프레임 ConversationContent 전체가 재컴포지션된다.
    val listBottomGlobalY = remember { FloatArray(1) }
    var inputLeftGlobalX by remember { androidx.compose.runtime.mutableStateOf(0f) }
    var inputBottomGlobalY by remember { androidx.compose.runtime.mutableStateOf(0f) }
    val lastMessage = uiState.messages.lastOrNull()
    // 비행 연출을 끄고 기본 밀어올림(말풍선이 아래에서 자라나며 채팅을 밀어올리는 기존 애니)만
    // 쓰는 조건 (사용자 결정):
    // ①작은 화면(FitDesign 축소 중) — 키보드까지 뜨면 비행 거리가 너무 짧아 연출이 제대로 안 나옴
    //   축소율 공식이 (세로-140)/712 로 바뀌어(DesignFit 2026-07-11) 경계도 환산:
    //   0.89 = S25급 비행 유지(360x780→0.899), 진짜 작은 화면만 밀어올림(S8급 360x740→0.843).
    //   (구 공식 세로/900 때는 0.85가 같은 경계였음 — 의도 동일, 숫자만 환산)
    // ②API 31 미만 — 뭉개짐 블러(BlurEffect)가 안 돼서 비행 중 파란 박스가 또렷하게 보임
    val useFlight = LocalDesignScale.current >= 0.89f && android.os.Build.VERSION.SDK_INT >= 31
    LaunchedEffect(lastMessage?.id) {
        val m = lastMessage ?: return@LaunchedEffect
        if (useFlight && m.isFromUser && flownIds.add(m.id)) {
            flightMessage = m
            flightProgress.snapTo(0f)
            flightProgress.animateTo(1f, tween(500, easing = LinearEasing))
            flightMessage = null
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned {
                contentOrigin = it.positionInRoot()
                contentWidthPx = it.size.width
            },
    ) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .imePadding(), // 키보드가 올라오면 입력창·목록이 위로 (목업엔 없는 기본 처리)
    ) {
        Spacer(Modifier.height(8.dp)) // 상태바(40) → 헤더(top 48) (CSS Frame 427321717)
        // 헤더 (CSS Frame 427321717: width 380 left 0 → 오른쪽 여백 13, height 44)
        //   뒤로가기(44) 왼끝 · 미션 제목(x113~281, 중심 197 = 393/2이라 화면 정중앙) · "대화 완료" 오른끝
        // ★두 버튼의 동작이 다르다(사용자 확정 2026-08-10):
        //   뒤로가기  = 미션을 저장하지 않고 종료
        //   대화 완료 = 미션 완료 처리 + 저장 후 종료
        //   시안에도 "대화 이탈 팝업(뒤로가기)"과 "대화 종료 팝업(완료 버튼)"이 각각 따로 있다.
        Box(modifier = Modifier.fillMaxWidth().height(44.dp)) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape) // 눌림 효과 원형 (아이콘 버튼 관례)
                    .clickable(onClick = onBackClick),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_conversation_back),
                    contentDescription = "뒤로 가기",
                )
            }
            Text(
                text = uiState.missionTitle,
                style = TqType.BodyM.figma(),
                color = Gray800,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.Center)
                    .widthIn(max = 168.dp), // CSS 제목 폭. 좌우 버튼(44 / 78+13) 침범 방지
            )
            // "대화 완료" (CSS Frame 427321716): 78x30, padding 4/12, r8,
            // 흰 배경 + Primary600 테두리 1, Label/L Primary600
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 13.dp) // 헤더 폭 380 → 393-380
                    .clip(RoundedCornerShape(8.dp))
                    .background(White)
                    .border(1.dp, Primary600, RoundedCornerShape(8.dp))
                    .clickable(onClick = onCompleteClick)
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "대화 완료",
                    style = TqType.LabelL.figma(),
                    color = Primary600,
                    textAlign = TextAlign.Center,
                )
            }
        }

        // ── 메시지 영역 + 하단부(추천 답변·입력창) ──
        // 두 영역을 같은 Column에서 측정해야 추천 카드가 커지는 첫 프레임부터 메시지 영역도
        // 함께 줄어든다. 겹쳐 배치한 뒤 측정값을 다음 프레임에 전달하면 말풍선이 먼저 가려진다.
        val listState = rememberLazyListState()
        val density = LocalDensity.current
        Column(modifier = Modifier.fillMaxWidth().weight(1f)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            // 위로 스크롤해 옛 메시지를 보다가 새 메시지가 오면 바닥으로 복귀
            LaunchedEffect(uiState.messages.size) {
                if (uiState.messages.isNotEmpty()) listState.animateScrollToItem(0)
            }
            // 메시지별 등장 애니메이션을 "처음 나타날 때 한 번만" 돌리기 위한 기록
            val animatedMessageIds = remember { mutableSetOf<String>() }
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                // 인스타 DM 방식: 리스트를 바닥 기준으로 고정(reverseLayout, 0번 = 최신 메시지).
                // 새 메시지가 바닥에 끼면 기존 말풍선들이 animateItem으로 "밀려 올라가는" 이동을 함
                // (스크롤 보정이 아니라 항목 이동이라 뚝 끊기지 않음).
                reverseLayout = true,
                // reverseLayout에선 위/아래 의미가 뒤집혀서, 메시지가 적을 땐 Bottom이 "위부터 쌓임"
                // (목업 "대화 시작"의 위 정렬 유지)
                verticalArrangement = Arrangement.Bottom,
                // start 13 = 아바타 왼끝(CSS left 13). 아바타(40)+간격(8)을 더해 말풍선이 61에서 시작한다.
                // end 16 = 메시지 컬럼 오른끝 377 (393-16)
                // top 16 = 헤더 끝(92) → 메시지·아바타 시작(top 108) (CSS Frame 427320981)
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 13.dp, end = 16.dp, top = 16.dp, bottom = 16.dp,
                ),
            ) {
                items(count = uiState.messages.size, key = { uiState.messages[uiState.messages.size - 1 - it].id }) { reversedIndex ->
                    val index = uiState.messages.size - 1 - reversedIndex // reverseLayout: 0번 = 마지막 메시지
                    val message = uiState.messages[index]
                    val prev = uiState.messages.getOrNull(index - 1)
                    val next = uiState.messages.getOrNull(index + 1)
                    // 새 메시지: 제자리에서 아래→위로 스르륵 올라오며 나타남 (한 번만).
                    val firstAppearance = remember { animatedMessageIds.add(message.id) }
                    val enterState = remember {
                        MutableTransitionState(initialState = !firstAppearance).apply { targetState = true }
                    }
                    // 등장: 높이가 0에서 실제로 자라나며 위 대화를 밀어올림(앵커 점프 방지).
                    // 내 메시지는 비행 오버레이(FlyingBubble)가 연기하는 동안 진짜 아이템을 숨김.
                    // AI 메시지는 꼬리에서 살짝 커지며 정착.
                    // 전체 경로 지정: 바깥 Column 스코프의 동명 함수와 충돌 방지
                    androidx.compose.animation.AnimatedVisibility(
                        visibleState = enterState,
                        enter = if (message.isFromUser) {
                            // clip=false: 곡선 비행 중 말풍선이 자라나는 칸 밖에 있어도 보이게
                            expandVertically(
                                animationSpec = tween(300, easing = FastOutSlowInEasing),
                                expandFrom = Alignment.Top,
                                clip = false,
                            ) + fadeIn(tween(120))
                        } else {
                            expandVertically(
                                animationSpec = tween(300, easing = FastOutSlowInEasing),
                                expandFrom = Alignment.Top, // reverseLayout에서 위 대화를 밀어올리는 방향
                            ) +
                                slideInVertically(
                                    initialOffsetY = { with(density) { 8.dp.roundToPx() } },
                                    animationSpec = tween(300, easing = FastOutSlowInEasing),
                                ) +
                                fadeIn(tween(180))
                        },
                        // 다른 메시지가 추가돼 자리가 밀릴 때도 뚝 이동하지 않고 부드럽게
                        modifier = Modifier.animateItem(),
                    ) {
                        ChatBubbleRow(
                            message = message,
                            // 같은 발신자 연속이면 간격 8, 발신자가 바뀌면 16 (CSS 묶음 gap)
                            topGap = when {
                                prev == null -> 0.dp
                                prev.isFromUser == message.isFromUser -> 8.dp
                                else -> 16.dp
                            },
                            // 시각은 같은 발신자 묶음의 마지막 말풍선 옆에만 (CSS)
                            showTime = next == null || next.isFromUser != message.isFromUser,
                            // 아바타는 AI 묶음의 첫 말풍선 옆에만 (CSS: 아바타 프레임이 AI 묶음 수만큼 있음)
                            showAvatar = !message.isFromUser && (prev == null || prev.isFromUser),
                            hidden = flightMessage?.id == message.id, // 비행 오버레이가 대신 보이는 동안
                        )
                    }
                }
            }
            // 위 스크롤 마스크 (CSS "대화 길어졌을 때_ 스크롤시 위 흐림 효과" Frame 427320986):
            //   393x65, top 92(=헤더 끝), 위 0.8 → 아래로 갈수록 투명. 옛 메시지가 헤더 밑으로
            //   빨려 들어가며 배경색에 녹게 한다.
            // ★"대화 시작" 프레임에는 이 밴드가 없고 이 프레임(스크롤된 상태)에만 있다 →
            //   항상 깔지 않고 위쪽에 가려진 메시지가 있을 때만 보인다.
            //   reverseLayout이라 index가 커지는 쪽(=화면 위쪽)이 canScrollForward.
            // 나타나고 사라질 때 뚝 끊기지 않게 알파를 200ms로 보간 — CSS엔 없는 자작 처리.
            val topMaskAlpha by animateFloatAsState(
                targetValue = if (listState.canScrollForward) 1f else 0f,
                animationSpec = tween(200),
                label = "topMask",
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    // CSS 상자는 65지만 그라데이션이 130.71%에서야 0에 닿는다(65 x 1.3071 = 85).
                    // 65에서 자르면 알파 0.28이 남아 가로줄이 생기므로 페이드가 끝나는 85까지 그린다.
                    .height(85.dp)
                    .graphicsLayer { alpha = topMaskAlpha }
                    .background(topFadeBrush()),
            )
        }

        // ── 하단: 추천 답변 + 입력창 (시스템 네비 위 24 = CSS 입력창 바닥 780 → 네비존 804) ──
        Column(
            modifier = Modifier
                .fillMaxWidth()
                // 비행 도착점은 메시지 영역과 하단부가 맞닿는 현재 위치다.
                .onGloballyPositioned {
                    listBottomGlobalY[0] = it.positionInRoot().y
                }
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
                // 13차에서 하단 앱 네비가 이 화면에서 빠지고(헤더 "대화 완료"가 그 역할을 대신함)
                // 입력창이 시스템 네비 바로 위 24까지 내려왔다. 키보드가 뜨면 8만 남긴다(딱 붙지 않게).
                .padding(
                    bottom = if (WindowInsets.ime.getBottom(LocalDensity.current) > 0) 8.dp else 24.dp,
                ),
        ) {
            // 입력창은 두 상태 모두 맨 아래 같은 자리 → 아래 고정층으로 분리해 전환 때 움직이지 않게.
            // 카드↔바 전환: 목록이 두루마리 말리듯 클립되며 사라지고/펼쳐지고, 높이가 이어져
            // 바가 함께 내려오고 올라감 (한 번에 딱 바뀌지 않게 — 사용자 요청, 타이밍은 자작 근사).
            Box(modifier = Modifier.fillMaxWidth()) {
                RecommendationPanel(
                    expanded = recommendationsReady && uiState.recommendationsExpanded,
                    recommendations = displayedRecommendations,
                    itemsTransitionProgress = { recommendationItemsProgress.value },
                    titleTransitionProgress = { recommendationTitleTransitionProgress.value },
                    titleShineProgress = { recommendationTitleShineProgress.value },
                    rippleInteractionSource = recommendationRippleInteraction,
                    onToggle = ::toggleRecommendationsWithTitleAnimation,
                    onSelect = onSelectRecommendation,
                )
                MessageInputRow(
                    text = uiState.inputText,
                    canSend = uiState.canSend,
                    onTextChange = onInputChange,
                    onSend = onSend,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .onGloballyPositioned {
                            val p = it.positionInRoot()
                            inputLeftGlobalX = p.x
                            inputBottomGlobalY = p.y + it.size.height
                        },
                )
            }
        }
        } // 메시지+하단부 Column
    }
    // 비행 중인 말풍선 오버레이 (입력창·네비 위에 그려짐)
    flightMessage?.let { flying ->
        FlyingBubble(
            text = flying.text,
            progress = { flightProgress.value },
            contentOrigin = { contentOrigin },
            contentWidthPx = { contentWidthPx },
            listBottomGlobalY = { listBottomGlobalY[0] },
            inputLeftGlobalX = { inputLeftGlobalX },
            inputBottomGlobalY = { inputBottomGlobalY },
        )
    }
    }
}

// 전송 비행 진행 곡선: 출발이 극단적으로 빨라 가로(채팅바) 구간을 "횡" 지나가고,
// 코너·상승·안착에서 길게 감속 (경로 거리의 대부분을 앞 ~25% 시간에 소화)
private val FlightEasing = CubicBezierEasing(0.1f, 0.9f, 0.2f, 1f)

// 전송 비행 말풍선(카톡 신모션): 입력창 왼쪽(텍스트 자리)에서 보라 말풍선 상태로 출발 →
// 오른쪽으로 미끄러지다(앞 70%, 감속) → 벽 앞에서 살짝 기울며(회전) 위로 가속 상승해 제자리 안착.
@Composable
private fun FlyingBubble(
    text: String,
    progress: () -> Float,
    contentOrigin: () -> Offset,
    contentWidthPx: () -> Int,
    listBottomGlobalY: () -> Float,
    inputLeftGlobalX: () -> Float,
    inputBottomGlobalY: () -> Float,
) {
    var bubbleSize by remember { androidx.compose.runtime.mutableStateOf(IntSize.Zero) }
    // offset(레이아웃)에서 계산한 값을 graphicsLayer(그리기)에 넘기는 통로
    val motion = remember { FloatArray(3) } // [0]=코너 통과율 0~1, [1]=순간 속도 배율(1=평균), [2]=전체 상승 거리(px)
    // 상승률: 배경/글자 색을 매 프레임 다시 칠해야 해서 상태로 둠 (비행 500ms 동안만)
    var riseFraction by remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    // 색: 처음부터 카드색이 옅게 비치고(45%), 올라가면서 완전한 보라로 (상승 50%에 완성).
    // 글자는 처음부터 흰색 — 바 안을 지나는 뭉개짐이 "흰색+파란색 무언가"로 보이게 (검정 금지).
    val colorT = (riseFraction / 0.5f).coerceIn(0f, 1f)
    val bubbleColor = androidx.compose.ui.graphics.lerp(Color.White, Primary600, 0.45f + 0.55f * colorT)
    val flyingTextColor = Gray50
    Box(
        modifier = Modifier
            .offset {
                // 경로 = 직진(가로) → 사분원 코너(좌회전) → 직진(상승)의 연속 곡선.
                // 진행은 경로 "거리" 기준 + FlightEasing: 가로 구간은 순식간에 지나가고
                // 상승·안착에서 길게 감속.
                val t = progress()
                val p = FlightEasing.transform(t)
                // 순간 속도(곡선 기울기): 스미어(늘임)·모션 블러 강도 계산용
                motion[1] = (FlightEasing.transform((t + 0.02f).coerceAtMost(1f)) - p) / 0.02f
                val origin = contentOrigin()
                val startLeft = inputLeftGlobalX() - origin.x + 16.dp.toPx() // 입력 텍스트 시작점
                val endLeft = contentWidthPx() - 16.dp.toPx() - bubbleSize.width // 오른쪽 벽 - 여백 = 제자리
                val startBottom = inputBottomGlobalY() - origin.y // 입력창 바닥에서 출발
                val endBottom = listBottomGlobalY() - origin.y - 16.dp.toPx() // 리스트 바닥 패딩 위 = 제자리
                // 코너 반지름 (경로보다 크면 줄임)
                val r = 56.dp.toPx()
                    .coerceAtMost(((startBottom - endBottom) / 2f).coerceAtLeast(1f))
                    .coerceAtMost(((endLeft - startLeft) / 2f).coerceAtLeast(1f))
                val legA = (endLeft - r) - startLeft          // 가로 직진 길이
                val legB = (PI.toFloat() / 2f) * r            // 코너(사분원) 길이
                val legC = (startBottom - r) - endBottom      // 세로 상승 길이
                val d = p * (legA + legB + legC)              // 지금까지 간 거리
                val x: Float
                val bottom: Float
                when {
                    d <= legA -> { // 가로 직진
                        x = startLeft + d
                        bottom = startBottom
                        motion[0] = 0f
                    }
                    d <= legA + legB -> { // 둥근 좌회전
                        val phi = (d - legA) / r // 코너 안에서 돈 각도(라디안, 0~π/2)
                        x = (endLeft - r) + r * sin(phi)
                        bottom = (startBottom - r) + r * cos(phi)
                        motion[0] = phi / (PI.toFloat() / 2f)
                    }
                    else -> { // 세로 상승
                        x = endLeft
                        bottom = (startBottom - r) - (d - legA - legB)
                        motion[0] = 1f
                    }
                }
                motion[2] = (startBottom - endBottom).coerceAtLeast(1f)
                riseFraction = ((startBottom - bottom) / motion[2]).coerceIn(0f, 1f)
                IntOffset(x.roundToInt(), (bottom - bubbleSize.height).roundToInt())
            }
            .graphicsLayer {
                // 코너 도는 동안 진행 방향으로 살짝 기울었다 수평 복귀 (좌회전 감)
                rotationZ = -10f * sin(PI.toFloat() * motion[0])
                alpha = if (bubbleSize.width == 0) 0f else 1f // 크기 실측 전 첫 프레임만 숨김
                // 채팅바 안에선 말풍선을 글자만 한 "작은 알갱이"(35%)로 축소해 날림 —
                // 카드 형태·글자는 안 보이고 카드색+글자색이 섞인 작은 것만 휙 지나감.
                // 바를 벗어나 올라가면서 원래 크기로 자라나 또렷한 말풍선으로 복원.
                // 성장 구간 = 원래 튜닝(상승 35% ~ 85%) 유지. 단 시작점이 입력창 높이+여유(52dp)
                // 안쪽으로는 못 내려오게 바닥만 깖 — 상승 거리가 짧은 화면에서 35% 지점이
                // 아직 바 안일 때 기울어진 파란 박스가 입력칸에서 보이는 것 방지.
                // (절대 거리로만 바꿨더니 큰 화면의 모션 느낌까지 변해서 원복 — 2026-07-10)
                val totalRise = motion[2]
                val risePx = riseFraction * totalRise
                val growStart = maxOf(52.dp.toPx(), 0.35f * totalRise)
                val growEnd = (growStart + 0.5f * totalRise).coerceAtMost(totalRise)
                val grow = ((risePx - growStart) / (growEnd - growStart).coerceAtLeast(1f)).coerceIn(0f, 1f)
                val scale = 0.3f + 0.7f * grow
                scaleX = scale
                scaleY = scale
                // 작은 상태에선 블러를 강하게 얹어 "형태 없는 색 뭉개짐"으로만 보이게
                // (말풍선 윤곽·글자가 인식되면 안 됨). 자라나면서 블러가 풀려 또렷해짐. (안드12+)
                // - 강도는 scale로 나눔: 블러가 레이어 축소 전에 적용돼 화면에선 scale배로
                //   약해지기 때문 (0.3배 상태에서 22가 실제 6.6px이 돼 네모 윤곽이 비쳤음)
                // - TileMode.Decal: 기본(Clamp)은 경계 픽셀을 밖으로 늘려 직사각형 실루엣이
                //   남음("깨진 블록" 느낌) → 경계 밖을 투명으로 녹여 형태 자체를 없앰
                renderEffect = if (android.os.Build.VERSION.SDK_INT >= 31 && grow < 1f) {
                    val blur = (22f * (1f - grow) + 0.5f) / scale
                    androidx.compose.ui.graphics.BlurEffect(
                        blur,
                        blur * 0.6f,
                        androidx.compose.ui.graphics.TileMode.Decal,
                    )
                } else {
                    null
                }
            }
            .onSizeChanged { bubbleSize = it }
            .widthIn(max = 260.dp)
            .clip(RoundedCornerShape(24.dp, 24.dp, 2.dp, 24.dp))
            .background(bubbleColor)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Text(text = text, style = TqType.BodyM.figma(), color = flyingTextColor)
    }
}

// 말풍선 한 줄: AI = 왼쪽 흰색(왼아래 뾰족) / 나 = 오른쪽 보라(오른아래 뾰족), 시각은 바깥쪽 아래 정렬
// AI 쪽은 말풍선 왼쪽에 봇 아바타(40) + 간격 8이 붙어, 말풍선이 CSS의 x=61에서 시작한다.
@Composable
private fun ChatBubbleRow(
    message: ChatMessage,
    topGap: androidx.compose.ui.unit.Dp,
    showTime: Boolean,
    showAvatar: Boolean = false,
    hidden: Boolean = false, // 전송 비행 중엔 오버레이가 대신 보이므로 진짜 아이템은 투명 처리
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { alpha = if (hidden) 0f else 1f }
            .padding(top = topGap),
        horizontalArrangement = if (message.isFromUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
    ) {
        if (!message.isFromUser) {
            // 봇 아바타 (CSS Frame 427320975): 40 원, Gray200 바탕 + 보라 그림자, 이미지 34x40.
            // 묶음의 첫 말풍선에만 보이고, 이어지는 말풍선은 같은 폭을 비워 세로선을 맞춘다.
            // 말풍선 위쪽에 정렬 — CSS에서 아바타 top(108/286)이 그 묶음 첫 말풍선 top과 같다.
            if (showAvatar) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Top)
                        .size(40.dp)
                        .softShadow(
                            color = Color(0xFF9A73FF).copy(alpha = 0.08f), // CSS "봇 뒤" 그림자
                            offsetY = 6.dp,
                            blur = 12.dp,
                            cornerRadius = 20.dp,
                        )
                        .clip(CircleShape)
                        .background(Gray200),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(R.drawable.img_conversation_bot),
                        contentDescription = null,
                        modifier = Modifier.requiredSize(width = 34.dp, height = 40.dp),
                    )
                }
            } else {
                Spacer(Modifier.width(40.dp))
            }
            Spacer(Modifier.width(8.dp))
        }
        if (message.isFromUser && showTime) {
            TimeLabel(message.time)
            Spacer(Modifier.width(8.dp))
        }
        Box(
            modifier = Modifier
                .widthIn(max = if (message.isFromUser) 260.dp else 230.dp) // CSS max-width (패딩 포함)
                .let {
                    if (message.isFromUser) it
                    else it.softShadow( // AI 말풍선만 그림자 (CSS 0 2 6, 8%→3% 디자이너 변경 2026-07-22)
                        color = Color.Black.copy(alpha = 0.03f),
                        offsetY = 2.dp,
                        blur = 6.dp,
                        cornerRadius = 24.dp,
                    )
                }
                .clip(
                    if (message.isFromUser) {
                        RoundedCornerShape(24.dp, 24.dp, 2.dp, 24.dp) // 오른아래 뾰족
                    } else {
                        RoundedCornerShape(24.dp, 24.dp, 24.dp, 2.dp) // 왼아래 뾰족
                    },
                )
                .background(if (message.isFromUser) Primary600 else Color.White)
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Text(
                text = message.text,
                // 어절(띄어쓰기 덩어리) 중간에서 끊기지 않게 — "추천해 드/릴까요?" 방지.
                // 어절 하나가 말풍선 max-width보다 길면 그때만 어절 안에서 끊어 삐져나오지 않게 한다.
                // 같은 대화 텍스트를 다루는 보관함 대화 상세(ArchiveConversationDetailScreen)와 동일한 설정.
                style = TqType.BodyM.copy(
                    lineBreak = LineBreak(
                        strategy = LineBreak.Strategy.Simple,
                        strictness = LineBreak.Strictness.Normal,
                        wordBreak = LineBreak.WordBreak.Phrase,
                    ),
                ).figma(),
                color = if (message.isFromUser) Gray50 else ChatText,
            )
        }
        if (!message.isFromUser && showTime) {
            Spacer(Modifier.width(8.dp))
            TimeLabel(message.time)
        }
    }
}

@Composable
private fun TimeLabel(time: String) {
    // CSS: 10px 500, line-height 140%, #999999
    Text(
        text = time,
        style = TqType.Caption.figma().copy(
            fontSize = androidx.compose.ui.unit.TextUnit(10f, androidx.compose.ui.unit.TextUnitType.Sp),
            lineHeight = androidx.compose.ui.unit.TextUnit(14f, androidx.compose.ui.unit.TextUnitType.Sp),
            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
        ),
        color = TimeText,
    )
}

// 접힌 바와 펼친 카드를 같은 컴포넌트로 유지한다. 카드의 크기·모서리와 물결 clip이
// 같은 320ms 전환을 따라가므로 물결이 변형 중인 흰 카드 외곽을 벗어나지 않는다.
@Composable
private fun RecommendationPanel(
    expanded: Boolean,
    recommendations: List<String>,
    itemsTransitionProgress: () -> Float,
    titleTransitionProgress: () -> Float,
    titleShineProgress: () -> Float,
    rippleInteractionSource: MutableInteractionSource,
    onToggle: () -> Unit,
    onSelect: (String) -> Unit,
) {
    val topCorner by animateDpAsState(
        targetValue = if (expanded) 24.dp else 8.dp,
        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
        label = "recommendationTopCorner",
    )
    val bottomCorner by animateDpAsState(
        targetValue = if (expanded) 0.dp else 8.dp,
        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
        label = "recommendationBottomCorner",
    )
    val sideInset by animateDpAsState(
        targetValue = if (expanded) 2.dp else 0.dp,
        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
        label = "recommendationSideInset",
    )
    val headerHeight by animateDpAsState(
        targetValue = if (expanded) 44.dp else 42.dp,
        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
        label = "recommendationHeaderHeight",
    )
    val headerLeadingInset by animateDpAsState(
        targetValue = if (expanded) 4.dp else 0.dp,
        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
        label = "recommendationHeaderLeadingInset",
    )
    val bottomSpace by animateDpAsState(
        targetValue = if (expanded) 17.dp else 56.dp,
        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
        label = "recommendationBottomSpace",
    )
    val panelShape = RoundedCornerShape(
        topStart = topCorner,
        topEnd = topCorner,
        bottomStart = bottomCorner,
        bottomEnd = bottomCorner,
    )
    // softShadow는 draw마다 Paint와 BlurMaskFilter를 새로 만든다. 펼침 중에는 크기가
    // 매 프레임 바뀌므로 같은 모양의 Paint를 여기서 한 번만 만들어 재사용한다.
    val shadowBlurPx = with(LocalDensity.current) { 24.dp.toPx() }
    val shadowPaint = remember(shadowBlurPx) {
        Paint().apply {
            color = Gray1000.copy(alpha = 0.01f)
            val radius = ((shadowBlurPx / 2f - 0.5f) / 0.57735f).coerceAtLeast(0.1f)
            asFrameworkPaint().maskFilter = BlurMaskFilter(radius, BlurMaskFilter.Blur.NORMAL)
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = sideInset)
                .drawBehind {
                    drawIntoCanvas { canvas ->
                        canvas.drawRoundRect(
                            left = 0f,
                            top = 8.dp.toPx(),
                            right = size.width,
                            bottom = size.height + 8.dp.toPx(),
                            radiusX = topCorner.toPx(),
                            radiusY = topCorner.toPx(),
                            paint = shadowPaint,
                        )
                    }
                }
                .clip(panelShape)
                .background(Color.White),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(headerHeight)
                    .clip(panelShape)
                    .clickable(
                        interactionSource = rippleInteractionSource,
                        indication = ripple(bounded = true),
                        onClick = onToggle,
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(Modifier.width(headerLeadingInset))
                AnimatedRecommendationHeaderLabel(
                    transitionProgress = titleTransitionProgress,
                    shineProgress = titleShineProgress,
                )
                Spacer(Modifier.width(4.dp))
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier.size(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (expanded) {
                            Icons.Filled.KeyboardArrowDown
                        } else {
                            Icons.Filled.KeyboardArrowUp
                        },
                        contentDescription = if (expanded) "추천 답변 접기" else "추천 답변 펼치기",
                        tint = if (expanded) Gray700 else Gray600,
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(
                    animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
                    expandFrom = Alignment.Top,
                ) + fadeIn(tween(durationMillis = 150)),
                exit = shrinkVertically(
                    animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
                    shrinkTowards = Alignment.Top,
                ) + fadeOut(tween(durationMillis = 120, delayMillis = 200)),
            ) {
                Column {
                    Spacer(Modifier.height(8.dp))
                    Column(
                        modifier = Modifier
                            .animateContentSize(
                                animationSpec = tween(420, easing = FastOutSlowInEasing),
                                alignment = Alignment.BottomStart,
                            )
                            .padding(start = 16.dp, end = 16.dp, bottom = 39.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        recommendations.forEachIndexed { index, text ->
                            RecommendationChip(
                                text = text,
                                index = index,
                                transitionProgress = itemsTransitionProgress,
                                onSelect = onSelect,
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(bottomSpace))
    }
}

@Composable
private fun RecommendationChip(
    text: String,
    index: Int,
    transitionProgress: () -> Float,
    onSelect: (String) -> Unit,
) {
    val entranceOffset = with(LocalDensity.current) { 4.dp.toPx() }
    Box(
        modifier = Modifier
            .graphicsLayer {
                val start = (index * 0.12f).coerceAtMost(0.36f)
                val localProgress = ((transitionProgress() - start) / (1f - start)).coerceIn(0f, 1f)
                val easedProgress = FastOutSlowInEasing.transform(localProgress)
                alpha = easedProgress
                translationY = entranceOffset * (1f - easedProgress)
            }
            .clip(RoundedCornerShape(24.dp))
            .background(Gray100)
            .clickable { onSelect(text) }
            .padding(horizontal = 12.dp, vertical = 4.dp),
    ) {
        Text(text = text, style = TqType.BodyM.figma(), color = Gray600)
    }
}


// 추천 답변 카드 (펼침, CSS Frame 427320994): 흰색 r24(위만), 헤더줄 + 칩들.
// 아래 39(칩→카드끝) = 입력창과의 간격 12 + 입력창이 겹치는 구간 27 (CSS 좌표 검산).
@Composable
private fun RecommendationCard(
    recommendations: List<String>,
    title: String,
    titleShineProgress: Float,
    rippleInteractionSource: MutableInteractionSource,
    onToggle: () -> Unit,
    onSelect: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
            )
            .padding(horizontal = 2.dp) // 카드(357)가 입력창(361)보다 좌우 2씩 좁음 (CSS)
            .padding(bottom = 17.dp) // 입력창 바닥(716)이 카드 바닥(699)보다 17 아래 (CSS)
            .softShadow(color = Gray1000.copy(alpha = 0.01f), offsetY = 8.dp, blur = 24.dp, cornerRadius = 24.dp)
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(Color.White),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .clickable(
                    interactionSource = rippleInteractionSource,
                    indication = ripple(bounded = true),
                    onClick = onToggle,
                )
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // CSS Frame 427320989: padding 0 4px, gap 6, 전구 19x19
            Spacer(Modifier.width(4.dp))
            // ★13차 CSS는 펼침·접힘 모두 "답장이 고민되시나요?"로 통일했지만,
            //   펼친 카드는 "톡깨의 추천 답변"을 그대로 쓰기로 함(사용자 결정 2026-08-10).
            //   접힘 바는 CSS대로 "답장이 고민되시나요?" 유지 — 두 상태의 문구가 다른 것이 의도된 상태다.
            RecommendationHeaderLabel(
                text = title,
                shineProgress = titleShineProgress,
            )
            Spacer(Modifier.width(4.dp))
            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .size(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = "추천 답변 접기",
                    tint = Gray700, // CSS Gray/700 #334155
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 39.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp), // 칩 간격 8 (CSS)
        ) {
            recommendations.forEachIndexed { index, text ->
                val entranceProgress = remember(text) { Animatable(0f) }
                LaunchedEffect(text) {
                    entranceProgress.snapTo(0f)
                    delay(index * 50L)
                    entranceProgress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
                    )
                }
                val entranceOffset = with(LocalDensity.current) { 8.dp.toPx() }
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            alpha = entranceProgress.value
                            translationY = entranceOffset * (1f - entranceProgress.value)
                        }
                        .animateContentSize(
                            animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                        )
                        .clip(RoundedCornerShape(24.dp))
                        .background(Gray100)
                        .clickable { onSelect(text) }
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                ) {
                    Text(text = text, style = TqType.BodyM.figma(), color = Gray600) // CSS Gray/600 #475569
                }
            }
        }
    }
}

// 접힘 바 (CSS "답변 추천"): 42 높이, r8, "답장이 고민되시나요?" + chevron-up
@Composable
private fun CollapsedRecommendationBar(
    title: String,
    titleShineProgress: Float,
    rippleInteractionSource: MutableInteractionSource,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .softShadow(color = Gray1000.copy(alpha = 0.01f), offsetY = 8.dp, blur = 24.dp, cornerRadius = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .clickable(
                interactionSource = rippleInteractionSource,
                indication = ripple(bounded = true),
                onClick = onToggle,
            )
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .height(30.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // CSS Frame 427320989: padding 0 4px 0 0(오른쪽만), gap 6, 전구 19x19
        RecommendationHeaderLabel(
            text = title,
            shineProgress = titleShineProgress,
        )
        Spacer(Modifier.width(4.dp))
        Spacer(Modifier.weight(1f))
        Box(
            modifier = Modifier
                .size(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowUp,
                contentDescription = "추천 답변 펼치기",
                tint = Gray600, // CSS Gray/600 #475569
            )
        }
    }
}

@Composable
private fun AnimatedRecommendationHeaderLabel(
    transitionProgress: () -> Float,
    shineProgress: () -> Float,
) {
    Image(
        painter = painterResource(R.drawable.ic_conversation_lightbulb),
        contentDescription = null,
        colorFilter = ColorFilter.tint(Primary600),
        modifier = Modifier.size(19.dp),
    )
    Spacer(Modifier.width(6.dp))
    Box {
        // 두 제목을 항상 같은 컴포지션에 둔다. 진행값은 draw 단계에서만 읽고,
        // 28~72% 구간을 겹쳐 기존 문구가 사라진 뒤 새 문구가 탁 생기는 틈을 없앤다.
        Text(
            text = "톡깨의 추천 답변",
            style = TqType.BodyM.figma(),
            color = Primary600,
            modifier = Modifier.recommendationTitleTransitionLayer(
                transitionProgress = transitionProgress,
                expandedTitle = true,
            ),
        )
        Text(
            text = "답장이 고민되시나요?",
            style = TqType.BodyM.figma(),
            color = Primary600,
            modifier = Modifier.recommendationTitleTransitionLayer(
                transitionProgress = transitionProgress,
                expandedTitle = false,
            ),
        )
        AnimatedRecommendationShineOverlay(
            text = "톡깨의 추천 답변",
            shineProgress = shineProgress,
        )
    }
}

private fun Modifier.recommendationTitleTransitionLayer(
    transitionProgress: () -> Float,
    expandedTitle: Boolean,
): Modifier = this
    .graphicsLayer {
        val progress = transitionProgress().coerceIn(0f, 1f)
        val visibility = if (expandedTitle) {
            ((0.72f - progress) / 0.44f).coerceIn(0f, 1f)
        } else {
            ((progress - 0.28f) / 0.44f).coerceIn(0f, 1f)
        }
        alpha = FastOutSlowInEasing.transform(visibility)
    }
    .drawWithContent {
        val progress = transitionProgress().coerceIn(0f, 1f)
        val visibility = if (expandedTitle) {
            ((0.72f - progress) / 0.44f).coerceIn(0f, 1f)
        } else {
            ((progress - 0.28f) / 0.44f).coerceIn(0f, 1f)
        }
        if (visibility > 0f) {
            if (expandedTitle) {
                clipRect(right = size.width * visibility) { this@drawWithContent.drawContent() }
            } else {
                clipRect(left = size.width * (1f - visibility)) { this@drawWithContent.drawContent() }
            }
        }
    }

@Composable
private fun AnimatedRecommendationShineOverlay(
    text: String,
    shineProgress: () -> Float,
) {
    // 광택 값도 이 작은 레이어 안에서만 읽는다.
    RecommendationShineOverlay(text = text, shineProgress = shineProgress())
}

@Composable
private fun RecommendationHeaderLabel(
    text: String = "톡깨의 추천 답변",
    transitionProgress: Float? = null,
    shineProgress: Float,
) {
    val titleShine = ((shineProgress - 0.20f) / 0.80f).coerceIn(0f, 1f)
    Image(
        painter = painterResource(R.drawable.ic_conversation_lightbulb),
        contentDescription = null,
        colorFilter = ColorFilter.tint(Primary600),
        modifier = Modifier.size(19.dp),
    )
    Spacer(Modifier.width(6.dp))
    if (transitionProgress == null) {
        RecommendationTitle(text = text, shineProgress = titleShine)
    } else {
        // 전환 끝점에서도 같은 두 Text 레이어를 유지한다. 끝나는 순간 Sequential →
        // 일반 Text로 구조를 교체하면 실기기에서 한 프레임 갱신처럼 번쩍여 보인다.
        Box {
            SequentialRecommendationTitle(
                previousText = "톡깨의 추천 답변",
                targetText = "답장이 고민되시나요?",
                progress = transitionProgress,
            )
            RecommendationShineOverlay(
                text = "톡깨의 추천 답변",
                shineProgress = titleShine,
            )
        }
    }
}

@Composable
private fun SequentialRecommendationTitle(
    previousText: String,
    targetText: String,
    progress: Float,
) {
    // 기존 글자 수 × 55ms, 새 글자 수 × 55ms라는 전체 시간 비율은 유지한다.
    // 다만 각 글자 상태를 바꾸지 않고, 넓은 반투명 띠가 글자 모양 안을 지나가게 한다.
    val split = previousText.length.toFloat() / (previousText.length + targetText.length)
    Box {
        RecommendationWipeText(
            text = previousText,
            progress = (progress / split).coerceIn(0f, 1f),
            appearing = false,
        )
        RecommendationWipeText(
            text = targetText,
            progress = ((progress - split) / (1f - split)).coerceIn(0f, 1f),
            appearing = true,
        )
    }
}

@Composable
private fun RecommendationWipeText(
    text: String,
    progress: Float,
    appearing: Boolean,
) {
    var widthPx by remember(text) { mutableIntStateOf(1) }
    val width = widthPx.toFloat().coerceAtLeast(1f)
    // 띠 폭을 넓게 잡아 글자 하나가 55ms만에 탁 바뀌지 않고, 글자 획 안에서 흐려진다.
    val feather = width * 0.34f
    // 시작·끝 경계를 글자 바깥까지 밀어 첫 글자가 전환 전부터 반투명으로 남지 않게 한다.
    val travel = width + (feather * 2f)
    val edge = if (appearing) {
        -feather + (travel * progress)
    } else {
        width + feather - (travel * progress)
    }
    val visible = Primary600
    val hidden = Primary600.copy(alpha = 0f)
    val brush = Brush.linearGradient(
        colors = if (appearing) {
            listOf(visible, visible, hidden, hidden)
        } else {
            listOf(visible, visible, hidden, hidden)
        },
        start = Offset(edge - feather, 0f),
        end = Offset(edge + feather, 0f),
    )
    Text(
        text = text,
        style = TqType.BodyM.figma().copy(brush = brush),
        color = Color.Unspecified,
        modifier = Modifier.onSizeChanged { widthPx = it.width },
    )
}

@Composable
private fun RecommendationTitle(text: String, shineProgress: Float) {
    Box {
        Text(text = text, style = TqType.BodyM.figma(), color = Primary600)
        RecommendationShineOverlay(text = text, shineProgress = shineProgress)
    }
}

@Composable
private fun RecommendationShineOverlay(text: String, shineProgress: Float) {
    var titleWidthPx by remember(text) { mutableStateOf(0) }
    val sweepDistance = titleWidthPx.coerceAtLeast(1).toFloat() * 3f
    val brush = Brush.linearGradient(
        // 기본 글자는 아래 레이어가 계속 그린다. 이 레이어는 흰 광택만 투명하게
        // 지나가므로 진행값을 1→0으로 되돌려도 화면 모양이 바뀌지 않는다.
        colors = listOf(Color.Transparent, Color.Transparent, Color.White, Color.Transparent, Color.Transparent),
        // 광택 중심이 글자 시작 바깥(-0.5폭)에서 끝 바깥(1.5폭)까지 지나간다.
        start = Offset((shineProgress * sweepDistance) - (titleWidthPx * 1.5f), 0f),
        end = Offset((shineProgress * sweepDistance) - (titleWidthPx * 0.5f), 0f),
    )
    Text(
        text = text,
        style = TqType.BodyM.figma().copy(brush = brush),
        color = Color.Unspecified,
        modifier = Modifier.onSizeChanged { titleWidthPx = it.width },
    )
}

// 입력창 (CSS Textbox): 44 높이, r24, 테두리 0.8 Gray100, 오른쪽 보내기 원(36)
@Composable
private fun MessageInputRow(
    text: String,
    canSend: Boolean,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .border(0.8.dp, Gray100, RoundedCornerShape(24.dp))
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.weight(1f).padding(start = 12.dp), contentAlignment = Alignment.CenterStart) {
            BasicTextField(
                value = text,
                onValueChange = onTextChange,
                textStyle = TqType.BodyM.figma().copy(color = Gray800),
                // 엔터 = 전송. singleLine이 없으면 엔터가 줄바꿈으로 들어가
                // 글자가 위로 밀려 "입력이 사라진 것처럼" 보였음(실측) → 전송 액션으로 연결.
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { if (canSend) onSend() }),
                modifier = Modifier
                    .fillMaxWidth()
                    // 한글 조합 중이면 첫 엔터를 IME가 "조합 확정"에 써버려 엔터를 두 번 눌러야
                    // 전송됐음(실측) → IME보다 먼저 엔터를 가로채 한 번에 전송.
                    // (조합 중인 글자도 value에 이미 들어 있어 그대로 보내도 안전.
                    //  전송 후 입력창이 비워지며 조합도 함께 끊김. 이벤트 소비(true)로 이중 전송 방지)
                    .onPreviewKeyEvent { event ->
                        if (event.key == Key.Enter && event.type == KeyEventType.KeyDown) {
                            if (canSend) onSend()
                            true
                        } else {
                            false
                        }
                    },
            )
            if (text.isEmpty()) {
                // CSS "14/Medium": 14px / weight 500 / line-height 150%(=21) / letter-spacing -0.02em.
                // Body/M(400·22)과 달라 그대로 옮긴다.
                Text(
                    text = "메세지를 입력하세요...",
                    style = TqType.BodyM.figma().copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                        lineHeight = androidx.compose.ui.unit.TextUnit(21f, androidx.compose.ui.unit.TextUnitType.Sp),
                        letterSpacing = androidx.compose.ui.unit.TextUnit(-0.02f, androidx.compose.ui.unit.TextUnitType.Em),
                    ),
                    color = Gray300,
                )
            }
        }
        Spacer(Modifier.width(16.dp))
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (canSend) Primary600 else Gray300)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    enabled = canSend,
                    onClick = onSend,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Image(painter = painterResource(R.drawable.ic_conversation_send), contentDescription = "보내기")
        }
    }
}

// 대화 화면 확인 팝업 (CSS "탈퇴 모달" 프레임 — 두 팝업이 같은 껍데기를 쓴다).
//   오버레이 = op bg Gray700 #334155 23% 딤 / 카드 = 336 x 흰 배경 radius16(그림자 없음)
//   left 28 top 313, padding 24/24/20, 문구↔버튼 gap 16, 제목↔설명 gap 4, 버튼 간격 12
// 문구와 오른쪽 버튼만 달라진다 (UI 13차에서 팝업이 둘로 나뉨):
//   "대화 종료 팝업(완료 버튼)" — 완료하기 / Purple600
//   "대화 이탈 팝업(뒤로가기)"  — 종료하기 / RED #F14444(디자인시스템 Error와 같은 값)
@Composable
private fun ConversationConfirmDialog(
    title: String,
    description: String,
    confirmText: String,
    confirmColor: Color,
    onContinue: () -> Unit,
    onConfirm: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.TopCenter, // CSS는 화면 top 기준 절대 위치 → 위에서부터 정렬
    ) {
        Column(
            modifier = Modifier
                // 화면 콘텐츠와 동일한 상단 기준(상태바 아래 = 디자인 y40)에 맞춤 — 안 맞추면
                // FitDesign 상태바 보정분만큼 팝업만 아래로 밀림(살짝 내려앉던 원인).
                .statusBarsPadding()
                .offset(y = 273.dp) // CSS top 313 − 상태바 밴드 40 (그 40은 statusBarsPadding이 담당)
                .width(336.dp) // CSS 고정폭
                .clip(RoundedCornerShape(16.dp))
                .background(White) // CSS #FFFFFF (그림자 없음)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}, // 카드 자체 터치는 흡수
                )
                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 20.dp), // CSS padding 24 24 20
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp), // 문구↔버튼 gap 16 (CSS)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp), // 제목↔설명 gap 4 (CSS)
            ) {
                Text(text = title, style = TqType.HeadingM.figma(), color = Gray900) // CSS Heading/M 20
                Text(
                    text = description,
                    style = TqType.BodyM.figma(), // CSS Body/M 14 regular
                    color = Gray600,
                    textAlign = TextAlign.Center,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { // 버튼 간격 12 (CSS)
                Box(
                    modifier = Modifier
                        .size(width = 138.dp, height = 48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Gray200) // CSS Gray/200 #E2E8F0
                        .clickable(onClick = onContinue),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = "계속하기", style = TqType.TitleL.figma(), color = Gray500) // CSS Gray/500
                }
                Box(
                    modifier = Modifier
                        .size(width = 138.dp, height = 48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(confirmColor) // 완료=Purple600 / 이탈=RED #F14444 (CSS)
                        .clickable(onClick = onConfirm),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = confirmText, style = TqType.TitleL.figma(), color = Gray50)
                }
            }
        }
    }
}

// ── Preview ──
private val previewMessages = listOf(
    ChatMessage("1", "안녕하세요! 처음 뵙네요 🙂", false, "9:20"),
    ChatMessage("2", "오늘 여기 처음 오셨어요?", false, "9:20"),
    ChatMessage("3", "분위기가 좋아보여서요!", true, "9:21"),
    ChatMessage("4", "오, 그러셨구나. 저는 여기 몇 번 와봤는데 생각보다 괜찮더라고요", false, "9:21"),
    ChatMessage("5", "오 그렇군요!", true, "9:21"),
)

@Preview(name = "대화 시작 (393dp)", showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun ConversationScreenPreview() {
    TalkQQuestTheme {
        ConversationScreen(
            uiState = ConversationUiState(
                missionTitle = "처음보는 사람과 짧게 인사하기",
                messages = previewMessages,
                recommendations = listOf(
                    "그렇군요! 저도 생각보다 편해서 놀랐어요.",
                    "맞아요. 분위기가 좋네요.",
                    "다음에도 와보고 싶어요.",
                ),
            ),
            onRetry = {},
        )
    }
}

@Preview(name = "추천 접힘 + 입력됨", showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun ConversationCollapsedPreview() {
    TalkQQuestTheme {
        ConversationScreen(
            uiState = ConversationUiState(
                missionTitle = "처음보는 사람과 짧게 인사하기",
                messages = previewMessages,
                recommendations = listOf("그렇군요! 저도 생각보다 편해서 놀랐어요."),
                recommendationsExpanded = false,
                inputText = "그렇군요! 저도 생각보다 편해서 놀랐어요",
            ),
            onRetry = {},
        )
    }
}

@Preview(name = "대화 종료 팝업(완료 버튼)", showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun ConversationCompleteDialogPreview() {
    TalkQQuestTheme {
        ConversationScreen(
            uiState = ConversationUiState(
                missionTitle = "처음보는 사람과 짧게 인사하기",
                messages = previewMessages,
                showCompleteDialog = true,
            ),
            onRetry = {},
        )
    }
}

@Preview(name = "대화 이탈 팝업(뒤로가기)", showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun ConversationLeaveDialogPreview() {
    TalkQQuestTheme {
        ConversationScreen(
            uiState = ConversationUiState(
                missionTitle = "처음보는 사람과 짧게 인사하기",
                messages = previewMessages,
                showLeaveDialog = true,
            ),
            onRetry = {},
        )
    }
}
