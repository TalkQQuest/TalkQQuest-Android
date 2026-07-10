package com.talkqquest.app.feature.mission.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
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
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.res.painterResource
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
import com.talkqquest.app.core.designsystem.Gray400
import com.talkqquest.app.core.designsystem.Gray50
import com.talkqquest.app.core.designsystem.Gray500
import com.talkqquest.app.core.designsystem.Gray600
import com.talkqquest.app.core.designsystem.Gray700
import com.talkqquest.app.core.designsystem.Gray800
import com.talkqquest.app.core.designsystem.Gray900
import com.talkqquest.app.core.designsystem.LocalDesignScale
import com.talkqquest.app.core.designsystem.Primary500
import com.talkqquest.app.core.designsystem.Primary600
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.TqType
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
import kotlinx.coroutines.launch

// ── 대화 진행 (UI 1차 v3 "미션(대화 시작)/(대화 길어진 경우)/(추천 답변 선택함)/미션 종료 팝업" 전사) ──
// 대화 준비 "미션 시작하기"에서 들어옴. 채팅(목록류)이라 FitDesign 대신 스크롤로 대응.
// 말풍선 텍스트 색은 CSS가 #1C1C1C(디자인시스템 Gray900 #1E293B와 다른 별도 값)라 그대로 둠.
private val ChatText = Color(0xFF1C1C1C)
private val TimeText = Color(0xFF999999) // CSS "Gray 400(푸터)" — 시스템 Gray400(#94A3B8)과 다른 값

// 스크롤 페이드 마스크 (CSS): Gray50 알파 0.8 → 0.45 → 0 그라데이션.
// CSS 정의역이 31.41%~130.71%라 화면 안(100%) 끝 알파는 보간값 0.28
// (검산: (100-81.06)/(130.71-81.06)=0.381 → 0.45x(1-0.381)=0.28). topDown=false면 위아래 반전.
private fun scrollMaskBrush(topDown: Boolean): Brush {
    val stops = if (topDown) {
        arrayOf(
            0f to Gray50.copy(alpha = 0.8f),
            0.3141f to Gray50.copy(alpha = 0.8f),
            0.8106f to Gray50.copy(alpha = 0.45f),
            1f to Gray50.copy(alpha = 0.28f),
        )
    } else {
        arrayOf(
            0f to Gray50.copy(alpha = 0.28f),
            0.1894f to Gray50.copy(alpha = 0.45f),
            0.6859f to Gray50.copy(alpha = 0.8f),
            1f to Gray50.copy(alpha = 0.8f),
        )
    }
    return Brush.verticalGradient(*stops)
}

@Composable
fun ConversationScreen(
    viewModel: ConversationViewModel = hiltViewModel(),
    onExitConfirm: () -> Unit = {}, // 종료하기 → 대화 종료 (TODO: 미션 완료&XP 화면 생기면 그리로)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ConversationScreen(
        uiState = uiState,
        onRetry = viewModel::startConversation,
        onInputChange = viewModel::onInputChange,
        onSend = viewModel::sendMessage,
        onToggleRecommendations = viewModel::toggleRecommendations,
        onSelectRecommendation = viewModel::selectRecommendation,
        onExitClick = { viewModel.setExitDialogVisible(true) },
        onExitDismiss = { viewModel.setExitDialogVisible(false) },
        onExitConfirm = onExitConfirm,
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
    onExitClick: () -> Unit = {},
    onExitDismiss: () -> Unit = {},
    onExitConfirm: () -> Unit = {},
) = FitDesign { // 작은 화면에선 디자인(393x852) 통째 축소 — 다른 화면들과 크기감 통일
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

            else -> ConversationContent(
                uiState = uiState,
                onInputChange = onInputChange,
                onSend = onSend,
                onToggleRecommendations = onToggleRecommendations,
                onSelectRecommendation = onSelectRecommendation,
                onExitClick = onExitClick,
            )
        }

        // 나가기 팝업 (CSS "미션 종료 팝업" — 배경을 어둡게 하는 층은 디자인에 없음.
        // 뒤 화면 오작동만 막게 투명 층으로 터치를 흡수하고, 카드만 화면 중앙에 띄움)
        // FitDesign: 카드가 고정폭(332)이라 320dp 같은 작은 화면에선 통째 비율 축소
        if (uiState.showExitDialog) {
            FitDesign {
                ExitDialog(onContinue = onExitDismiss, onExit = onExitConfirm)
            }
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
    onExitClick: () -> Unit,
) {
    // ── 전송 비행 연출(카톡 신모션): 내 메시지가 입력창 왼쪽에서 출발해 오른쪽으로
    // 미끄러지다 벽 앞에서 회전하며 위로 빨려 올라가 제자리에 안착. 진짜 리스트 아이템은
    // 비행이 끝날 때까지 숨기고, 화면 위를 나는 오버레이 말풍선이 연기함. ──
    val flownIds = remember { mutableSetOf<Long>() }
    var flightMessage by remember { androidx.compose.runtime.mutableStateOf<ChatMessage?>(null) }
    val flightProgress = remember { Animatable(1f) }
    // 좌표 실측(px): 출발점(입력창)·도착점(리스트 바닥) 계산용
    var contentOrigin by remember { androidx.compose.runtime.mutableStateOf(Offset.Zero) }
    var contentWidthPx by remember { mutableIntStateOf(0) }
    var listBottomGlobalY by remember { androidx.compose.runtime.mutableStateOf(0f) }
    var inputLeftGlobalX by remember { androidx.compose.runtime.mutableStateOf(0f) }
    var inputBottomGlobalY by remember { androidx.compose.runtime.mutableStateOf(0f) }
    val lastMessage = uiState.messages.lastOrNull()
    LaunchedEffect(lastMessage?.id) {
        val m = lastMessage ?: return@LaunchedEffect
        if (m.isFromUser && flownIds.add(m.id)) {
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
        Spacer(Modifier.height(8.dp)) // 상태바(40) → 헤더(top 48) (CSS Frame 427321191)
        // 헤더: 나가기 아이콘 왼끝 + 미션 제목 화면 정중앙 (Body/M Gray800, 가변 — 길면 말줄임)
        Box(modifier = Modifier.fillMaxWidth().height(44.dp)) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape) // 눌림 효과 원형 (아이콘 버튼 관례)
                    .clickable(onClick = onExitClick),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_conversation_exit),
                    contentDescription = "대화 종료",
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
                    .widthIn(max = 260.dp), // 나가기 아이콘(44) 침범 방지
            )
        }

        // ── 메시지 영역 ──
        val listState = rememberLazyListState()
        val scrollScope = rememberCoroutineScope()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                // 영역 높이가 변하는 동안(추천 카드 펼침/접힘 애니, 키보드) 매 프레임
                // 맨 아래로 붙잡아 마지막 메시지가 가려지지 않게 함. (reverseLayout이라 0 = 최신)
                .onSizeChanged {
                    if (uiState.messages.isNotEmpty()) {
                        scrollScope.launch { listState.scrollToItem(0) }
                    }
                }
                // 비행 도착점(리스트 바닥) 실측
                .onGloballyPositioned { listBottomGlobalY = it.positionInRoot().y + it.size.height },
        ) {
            // 위로 스크롤해 옛 메시지를 보다가 새 메시지가 오면 바닥으로 복귀
            LaunchedEffect(uiState.messages.size) {
                if (uiState.messages.isNotEmpty()) listState.animateScrollToItem(0)
            }
            // 메시지별 등장 애니메이션을 "처음 나타날 때 한 번만" 돌리기 위한 기록
            val animatedMessageIds = remember { mutableSetOf<Long>() }
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
                // top 88 = 아바타(104~174)를 지나 메시지 시작(top 180) - 헤더 끝(92) (CSS)
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 16.dp, end = 16.dp, top = 88.dp, bottom = 16.dp,
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
                                scaleIn(
                                    initialScale = 0.92f,
                                    // 커지는 기준점 = 말풍선 꼬리(AI는 왼쪽 아래)
                                    transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, 1f),
                                    animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow),
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
                            hidden = flightMessage?.id == message.id, // 비행 오버레이가 대신 보이는 동안
                        )
                    }
                }
            }
            // 위 스크롤 마스크 (CSS Frame 427320986): 헤더 아래 65, 메시지가 위로 사라질 때 페이드
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(65.dp)
                    .background(scrollMaskBrush(topDown = true)),
            )
            // (피그마의 하단 마스크는 절대좌표에서 메시지가 카드 밑으로 지나가는 구간용 —
            //  우리 구조는 메시지/카드가 분리돼 있어 최신 메시지만 흐려져서 뺌, 사용자 결정)
            // 봇 아바타 (CSS Frame 427320975): 70 원 + 보라 그림자, 이미지(70x81)가 원을 위아래로 살짝 벗어남
            Box(
                modifier = Modifier
                    .offset(x = 16.dp, y = 12.dp) // 헤더 끝(92) → 아바타(top 104) = 12
                    .size(70.dp)
                    .softShadow(
                        color = Color(0xFF9A73FF).copy(alpha = 0.08f), // CSS 봇 뒤 그림자
                        offsetY = 6.dp,
                        blur = 12.dp,
                        cornerRadius = 35.dp,
                    )
                    .clip(CircleShape)
                    .background(Gray100),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.img_conversation_bot),
                    contentDescription = null,
                    modifier = Modifier.requiredSize(width = 70.dp, height = 81.dp),
                )
            }
        }

        // ── 하단: 추천 답변 + 입력창 (하단 네비 알약 위 88 = CSS 입력창 바닥 716 → 네비존 804) ──
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
                // 하단 네비 알약 몫 88은 축소 대상 밖(MainScreen)이라 비율로 되돌려 실제 크기 유지
                .padding(bottom = 88.dp / LocalDesignScale.current),
        ) {
            // 입력창은 두 상태 모두 맨 아래 같은 자리 → 아래 고정층으로 분리해 전환 때 움직이지 않게.
            // 위의 추천 영역(카드↔바)만 높이가 부드럽게 변함(animateContentSize).
            Box(modifier = Modifier.fillMaxWidth()) {
                if (uiState.recommendations.isNotEmpty()) {
                    Column(modifier = Modifier.fillMaxWidth().animateContentSize()) {
                        if (uiState.recommendationsExpanded) {
                            // 카드(357)와 입력창(361)은 별개 층: 입력창이 카드 아래 모서리에 겹침
                            // (CSS: 카드 502~699, 입력창 672~716 — 27 겹침 + 17 삐져나옴)
                            RecommendationCard(
                                recommendations = uiState.recommendations,
                                onToggle = onToggleRecommendations,
                                onSelect = onSelectRecommendation,
                            )
                        } else {
                            CollapsedRecommendationBar(onToggle = onToggleRecommendations)
                            Spacer(Modifier.height(12.dp + 44.dp)) // 바→입력창 간격 12 + 아래 고정층(입력창 44) 자리
                        }
                    }
                }
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
    }
    // 비행 중인 말풍선 오버레이 (입력창·네비 위에 그려짐)
    flightMessage?.let { flying ->
        FlyingBubble(
            text = flying.text,
            progress = { flightProgress.value },
            contentOrigin = { contentOrigin },
            contentWidthPx = { contentWidthPx },
            listBottomGlobalY = { listBottomGlobalY },
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
    val motion = remember { FloatArray(2) } // [0]=코너 통과율 0~1, [1]=순간 속도 배율(1=평균)
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
                riseFraction = ((startBottom - bottom) / (startBottom - endBottom).coerceAtLeast(1f)).coerceIn(0f, 1f)
                IntOffset(x.roundToInt(), (bottom - bubbleSize.height).roundToInt())
            }
            .graphicsLayer {
                // 코너 도는 동안 진행 방향으로 살짝 기울었다 수평 복귀 (좌회전 감)
                rotationZ = -10f * sin(PI.toFloat() * motion[0])
                alpha = if (bubbleSize.width == 0) 0f else 1f // 크기 실측 전 첫 프레임만 숨김
                // 채팅바 안에선 말풍선을 글자만 한 "작은 알갱이"(35%)로 축소해 날림 —
                // 카드 형태·글자는 안 보이고 카드색+글자색이 섞인 작은 것만 휙 지나감.
                // 바를 벗어나 올라가면서 원래 크기로 자라나 또렷한 말풍선으로 복원.
                // 성장은 바를 확실히 벗어난 뒤(상승 35%~)에만 시작 — 바 높이(코너 구간)에선
                // 계속 형태 없는 뭉개짐 상태 유지 (기울어진 파란 박스가 바 안에서 인식되지 않게)
                val grow = ((riseFraction - 0.35f) / 0.5f).coerceIn(0f, 1f)
                val scale = 0.3f + 0.7f * grow
                scaleX = scale
                scaleY = scale
                // 작은 상태에선 블러를 강하게 얹어 "형태 없는 색 뭉개짐"으로만 보이게
                // (말풍선 윤곽·글자가 인식되면 안 됨). 자라나면서 블러가 풀려 또렷해짐. (안드12+)
                renderEffect = if (android.os.Build.VERSION.SDK_INT >= 31 && grow < 1f) {
                    val blur = 22f * (1f - grow) + 0.5f
                    androidx.compose.ui.graphics.BlurEffect(blur, blur * 0.6f)
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
@Composable
private fun ChatBubbleRow(
    message: ChatMessage,
    topGap: androidx.compose.ui.unit.Dp,
    showTime: Boolean,
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
        if (message.isFromUser && showTime) {
            TimeLabel(message.time)
            Spacer(Modifier.width(8.dp))
        }
        Box(
            modifier = Modifier
                .widthIn(max = if (message.isFromUser) 260.dp else 230.dp) // CSS max-width (패딩 포함)
                .let {
                    if (message.isFromUser) it
                    else it.softShadow( // AI 말풍선만 그림자 (CSS 0 2 6 8%)
                        color = Color.Black.copy(alpha = 0.08f),
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
                style = TqType.BodyM.figma(),
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

// 추천 답변 카드 (펼침, CSS Frame 427320994): 흰색 r24(위만), 헤더줄 + 칩들.
// 아래 39(칩→카드끝) = 입력창과의 간격 12 + 입력창이 겹치는 구간 27 (CSS 좌표 검산).
@Composable
private fun RecommendationCard(
    recommendations: List<String>,
    onToggle: () -> Unit,
    onSelect: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp) // 카드(357)가 입력창(361)보다 좌우 2씩 좁음 (CSS)
            .padding(bottom = 17.dp) // 입력창 바닥(716)이 카드 바닥(699)보다 17 아래 (CSS)
            .softShadow(color = Gray1000.copy(alpha = 0.04f), offsetY = 8.dp, blur = 24.dp, cornerRadius = 24.dp)
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(Color.White)
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 39.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp), // 헤더↔칩 gap 16 (CSS)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(painter = painterResource(R.drawable.ic_conversation_lightbulb), contentDescription = null)
            Spacer(Modifier.width(6.dp))
            Text(text = "톡깨의 추천 답변", style = TqType.BodyM.figma(), color = Primary600)
            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onToggle),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = "추천 답변 접기",
                    tint = Gray700,
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { // 칩 간격 8 (CSS)
            recommendations.forEach { text ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(Gray100)
                        .clickable { onSelect(text) }
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                ) {
                    Text(text = text, style = TqType.BodyM.figma(), color = Gray500)
                }
            }
        }
    }
}

// 접힘 바 (CSS "답변 추천"): 42 높이, r8, "답장이 고민되시나요?" + chevron-up
@Composable
private fun CollapsedRecommendationBar(onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .softShadow(color = Gray1000.copy(alpha = 0.04f), offsetY = 8.dp, blur = 24.dp, cornerRadius = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .clickable(onClick = onToggle)
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .height(30.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(painter = painterResource(R.drawable.ic_conversation_lightbulb), contentDescription = null)
        Spacer(Modifier.width(6.dp))
        Text(text = "답장이 고민되시나요?", style = TqType.BodyM.figma(), color = Primary500)
        Spacer(Modifier.weight(1f))
        Icon(
            imageVector = Icons.Filled.KeyboardArrowUp,
            contentDescription = "추천 답변 펼치기",
            tint = Gray600,
        )
    }
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
                maxLines = 1,
                modifier = Modifier.fillMaxWidth(),
            )
            if (text.isEmpty()) {
                Text(text = "메세지를 입력하세요...", style = TqType.BodyM.figma(), color = Gray300)
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

// 나가기 팝업 (CSS "미션 종료 팝업" Frame 427321198): 카드 332, r24, Gray50 + 카드그림자.
// 디자인에 어두운 배경 층이 없어 투명 층으로 뒤 터치만 막음.
@Composable
private fun ExitDialog(onContinue: () -> Unit, onExit: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onContinue, // 카드 밖 터치 = 계속하기와 동일(닫기)
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 332.dp)
                .softShadow(color = Gray1000.copy(alpha = 0.04f), offsetY = 8.dp, blur = 24.dp, cornerRadius = 24.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Gray50)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}, // 카드 자체 터치는 흡수
                )
                .padding(vertical = 24.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp), // 문구↔버튼 gap 24 (CSS)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp), // 제목↔설명 gap 8 (CSS)
            ) {
                Text(text = "대화를 종료하시겠어요?", style = TqType.TitleL.figma(), color = Gray900)
                Text(
                    text = "대화를 종료하면 현재 미션이 완료됩니다",
                    style = TqType.BodyL.figma().copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Medium),
                    color = Gray600,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(25.dp)) { // 버튼 간격 25 (CSS)
                Box(
                    modifier = Modifier
                        .size(width = 124.dp, height = 52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Gray200)
                        .clickable(onClick = onContinue),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = "계속하기", style = TqType.TitleL.figma(), color = Gray400)
                }
                Box(
                    modifier = Modifier
                        .size(width = 124.dp, height = 52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Error) // RED #F14444
                        .clickable(onClick = onExit),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = "종료하기", style = TqType.TitleL.figma(), color = Gray50)
                }
            }
        }
    }
}

// ── Preview ──
private val previewMessages = listOf(
    ChatMessage(1, "안녕하세요! 처음 뵙네요 🙂", false, "9:20"),
    ChatMessage(2, "오늘 여기 처음 오셨어요?", false, "9:20"),
    ChatMessage(3, "분위기가 좋아보여서요!", true, "9:21"),
    ChatMessage(4, "오, 그러셨구나. 저는 여기 몇 번 와봤는데 생각보다 괜찮더라고요", false, "9:21"),
    ChatMessage(5, "오 그렇군요!", true, "9:21"),
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

@Preview(name = "나가기 팝업", showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun ConversationExitPreview() {
    TalkQQuestTheme {
        ConversationScreen(
            uiState = ConversationUiState(
                missionTitle = "처음보는 사람과 짧게 인사하기",
                messages = previewMessages,
                showExitDialog = true,
            ),
            onRetry = {},
        )
    }
}
