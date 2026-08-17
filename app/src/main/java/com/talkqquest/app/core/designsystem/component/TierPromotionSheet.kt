package com.talkqquest.app.core.designsystem.component

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.talkqquest.app.R
import com.talkqquest.app.core.designsystem.Gray50
import com.talkqquest.app.core.designsystem.Gray600
import com.talkqquest.app.core.designsystem.Gray700
import com.talkqquest.app.core.designsystem.Gray800
import com.talkqquest.app.core.designsystem.Gray1000
import com.talkqquest.app.core.designsystem.LocalStatusBarCompensation
import com.talkqquest.app.core.designsystem.ModalDimColor
import com.talkqquest.app.core.designsystem.ModalSystemBars
import com.talkqquest.app.core.designsystem.Primary600
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.coverStatusBarCompensation
import com.talkqquest.app.core.designsystem.softShadow
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

// 실전 티어 승급 안내 바텀시트 — 성장 리포트와 홈의 "실전 티어 ⓘ"에서 공용으로 띄운다.
// 내용은 정적(3단계 안내)이라 데이터 인자 없이 visible/onDismiss만 받는다.
// 동작은 미션 북마크 시트와 같은 방식: 열면 살짝(peek) 올라오고, 위로 끌면 화면 꼭대기 근처까지 펼쳐지고
//        (펼침 상태에선 자동으로 안 내려감), 아래로 끌면 닫힘, 안 만지고 두면 3초 뒤 자동으로 내려감.
// 단 저장 시트와 두 가지가 다르다 — ①CSS에 딤(op bg = Gray/700 0.23)이 있어 모달로 뜬다
//   ②살짝 올라온 높이가 342다(저장 시트는 333). 값을 서로 옮겨 쓰지 말 것.

private val StarYellow = Color(0xFFF9AC17) // YELLOW_star

// 시트가 살짝 올라온 높이 (CSS 바텀시트: top 510 · height 342 · 화면 852 기준).
// 저장 시트(TqSaveSheetScaffold)는 333이라 값이 다르다 — 서로 복사해 오지 말 것.
private val SheetPeekHeight = 342.dp

private val FullLeading = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)
private fun TextStyle.figma(): TextStyle = copy(lineHeightStyle = FullLeading)

@Composable
fun TierPromotionSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    // 시트 위 끝 y(px), null=시트 없음. 하단 네비가 이 선 아래를 안 그려 시트 뒤로 가려지게 한다.
    onSheetTopChange: (Float?) -> Unit = {},
    // 시트가 화면에 떠 있는 동안 true. 딤이 깔린 모달이라 뒤의 탭 페이저 스와이프를 꺼야 한다.
    // (딤에서 드래그를 삼키는 방식은 여는 첫 프레임에 페이저가 제스처를 먼저 잡아가서 새는 경우가 있음)
    onModalChange: (Boolean) -> Unit = {},
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        // 시트 위 끝(offsetY)이 오갈 세 정착점: 숨김(바닥) / 살짝(바닥에서 342) / 펼침(꼭대기 근처).
        val hiddenOffset = constraints.maxHeight.toFloat()
        // 342 = 이 시트 CSS 값(시트 top 510 · height 342 · 화면 852). 저장 시트(333)와 다르니 옮겨 쓰지 말 것.
        val peekOffset = hiddenOffset - with(density) { SheetPeekHeight.toPx() }
        val expandedOffset = WindowInsets.statusBars.getTop(density) + with(density) { 28.dp.toPx() }

        var offsetY by remember { mutableFloatStateOf(hiddenOffset) }
        // 자동 닫힘 판단용: 지금 손을 대고 있는지 + 마지막으로 만진 시각(만질 때마다 타이머 리셋).
        var sheetPressed by remember { mutableStateOf(false) }
        var lastTouchAt by remember { mutableLongStateOf(System.currentTimeMillis()) }
        var animJob by remember { mutableStateOf<Job?>(null) }

        // 정착점으로 이동 — 올라올 땐 tween(300), 내려갈 땐 tween(600) (미션 북마크 시트와 동일).
        fun animateSheetTo(target: Float, onArrived: () -> Unit = {}) {
            animJob?.cancel()
            animJob = scope.launch {
                val spec: AnimationSpec<Float> =
                    if (target > offsetY) tween(600, easing = FastOutSlowInEasing)
                    else tween(300, easing = FastOutSlowInEasing)
                animate(offsetY, target, animationSpec = spec) { value, _ -> offsetY = value }
                onArrived()
            }
        }
        // 손가락 이동만큼 시트 이동(펼침~숨김 범위 안에서).
        fun dragBy(delta: Float): Float {
            val new = (offsetY + delta).coerceIn(expandedOffset, hiddenOffset)
            val consumed = new - offsetY
            if (consumed != 0f) offsetY = new
            return consumed
        }
        // 놓았을 때: 빠르게 튕기면 그 방향 다음 정착점, 아니면 가장 가까운 정착점으로. 숨김이면 닫힘.
        fun settle(velocity: Float) {
            val anchors = listOf(expandedOffset, peekOffset, hiddenOffset)
            val target = when {
                velocity < -1000f -> anchors.filter { it < offsetY }.maxOrNull() ?: expandedOffset
                velocity > 1000f -> anchors.filter { it > offsetY }.minOrNull() ?: hiddenOffset
                else -> anchors.minByOrNull { abs(it - offsetY) } ?: peekOffset
            }
            animateSheetTo(target) { if (target == hiddenOffset) onDismiss() }
        }

        // 열고 닫기: 열리면 살짝(peek) 위치로 올라오고, 닫히면 숨김으로 내려감.
        LaunchedEffect(visible) {
            if (visible) {
                lastTouchAt = System.currentTimeMillis()
                animateSheetTo(peekOffset)
            } else {
                animateSheetTo(hiddenOffset)
            }
        }
        // 자동 닫힘: 안 만지고 + 완전히 펼치지 않은 상태로 3초 지나면 스윽 내려감. 펼침 상태면 안 닫힘.
        LaunchedEffect(visible) {
            if (!visible) return@LaunchedEffect
            while (true) {
                delay(100)
                val fullyExpanded = offsetY <= expandedOffset + 1f
                if (sheetPressed || fullyExpanded) continue
                if (System.currentTimeMillis() - lastTouchAt >= 3000L) {
                    animateSheetTo(hiddenOffset) { onDismiss() }
                    break
                }
            }
        }

        // 하단 네비에 시트 위 끝 위치를 매 프레임 알림 → 네비는 시트에 안 덮인 부분만 그림.
        // 내려가는 애니메이션 동안에도 offsetY로 판단(숨김이면 null).
        //
        // ★탭 페이저가 인접 탭을 미리 구성해(MainTabsPager beyondViewportPageCount = 1) 홈 탭은
        //   미션 탭에 있을 때도 살아 있다. 이 콜백은 미션 저장 시트와 같은 것을 쓰기 때문에,
        //   숨김일 때 무조건 null을 쏘면 남이 올려둔 시트 위로 하단 네비가 튀어나온다.
        //   그래서 "내 시트가 떠 있는 동안"과 "내가 방금 내린 순간"에만 알리고 평소엔 건드리지 않는다.
        val sheetTopForNav = if (offsetY < hiddenOffset - 0.5f) offsetY else null
        var isReporting by remember { mutableStateOf(false) }
        // 화면에 떠 있는 동안(= 여는 순간부터 내려앉을 때까지) 페이저 스와이프를 끈다.
        // visible이 아니라 offsetY 기준이라 닫히는 애니메이션 중에도 계속 꺼져 있다.
        val onScreen = visible || sheetTopForNav != null
        LaunchedEffect(onScreen) { onModalChange(onScreen) }

        SideEffect {
            if (sheetTopForNav != null) {
                onSheetTopChange(sheetTopForNav)
                isReporting = true
            } else if (isReporting) {
                onSheetTopChange(null)
                isReporting = false
            }
        }
        DisposableEffect(Unit) {
            onDispose {
                if (isReporting) onSheetTopChange(null)
                onModalChange(false)
            }
        }

        // 배경 스크림 — 시트가 올라온 만큼 어두워짐(peek에서 최대, 내려가며 페이드아웃). 앱 표준 모달 딤과 동일(Gray700 0.23).
        val dimFraction = ((hiddenOffset - offsetY) / (hiddenOffset - peekOffset)).coerceIn(0f, 1f)
        ModalSystemBars(dimFraction, navigationBarColor = Gray50)
        // ★visible이 켜진 프레임부터 깐다(아직 offsetY가 바닥이라 안 보여도). 올라온 뒤에 깔면
        //   탭하자마자 쓸었을 때 딤이 없는 첫 프레임을 탭 페이저가 낚아채 옆 탭으로 넘어가 버린다.
        if (visible || offsetY < hiddenOffset - 0.5f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .coverStatusBarCompensation(LocalStatusBarCompensation.current)
                    .background(ModalDimColor.copy(alpha = ModalDimColor.alpha * dimFraction))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { animateSheetTo(hiddenOffset) { onDismiss() } },
                    ),
            )
        }

        // 시트 본체 — 펼쳤을 때 바닥까지 닿도록 높이 고정(내용은 위쪽, 나머지는 시트 배경). 위로 끌면 펼쳐짐.
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(with(density) { (hiddenOffset - expandedOffset).toDp() })
                .offset { IntOffset(0, offsetY.roundToInt()) }
                // 시트 그림자 = CSS `0px -8px 24px rgba(15,23,42,0.06)` 단일 레이어(저장 시트와 동일).
                .softShadow(
                    color = Gray1000.copy(alpha = 0.06f),
                    offsetY = (-8).dp,
                    blur = 24.dp,
                    cornerRadius = 36.dp,
                )
                .clip(RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp))
                .background(Gray50)
                // 손이 닿아 있는 동안 + 뗀 순간부터 3초를 재기 위한 감지(이벤트는 소비 안 함).
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            sheetPressed = event.changes.any { it.pressed }
                            lastTouchAt = System.currentTimeMillis()
                        }
                    }
                }
                // 위로 끌면 펼쳐지고 아래로 끌면 내려감 — 놓으면 가장 가까운 정착점으로.
                .draggable(
                    orientation = Orientation.Vertical,
                    state = rememberDraggableState { delta -> dragBy(delta) },
                    onDragStarted = { animJob?.cancel() },
                    onDragStopped = { velocity -> settle(velocity) },
                ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp), // 핸들 ↔ 콘텐츠
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // 핸들 Frame 453 (36x4, Gray/600)
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(Gray600),
                )

                // 콘텐츠 Frame 427321757 (gap 16, align center)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "티어 승급 안내",
                        style = TqType.TitleL.figma(), // 18/600
                        color = Gray800,
                    )

                    // 항목 Frame 427321756 (내용폭만큼 hug → 시트 가운데로 들여쓰기, gap 7).
                    // 행은 블록 안 좌측 정렬(아이콘 정렬), chevron은 블록 폭 기준 가운데.
                    Column(
                        verticalArrangement = Arrangement.spacedBy(7.dp),
                        horizontalAlignment = Alignment.Start,
                    ) {
                        HelpRow(
                            title = "핵심 역량 채우기",
                            subtitle = "미션을 통해 대화 역량을 넓혀요",
                        ) {
                            Box(modifier = Modifier.size(50.dp), contentAlignment = Alignment.Center) {
                                Image(
                                    painter = painterResource(R.drawable.img_help_radar),
                                    contentDescription = null,
                                    modifier = Modifier.size(50.dp),
                                )
                            }
                        }
                        ChevronDown(Modifier.align(Alignment.CenterHorizontally))
                        HelpRow(
                            title = "별 획득하기",
                            subtitle = "핵심 역량 당 300점을 모두 채우면 별을 얻어요",
                        ) {
                            // Star 4 (44x44) in 50x50
                            Box(modifier = Modifier.size(50.dp), contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_tier_star),
                                    contentDescription = null,
                                    tint = StarYellow,
                                    modifier = Modifier.size(44.dp),
                                )
                            }
                        }
                        ChevronDown(Modifier.align(Alignment.CenterHorizontally))
                        HelpRow(
                            title = "티어 승급하기",
                            subtitle = "별 3개를 다 모으면 다음 티어로!",
                        ) {
                            // 챌린저(마스터) 뱃지 — Figma 44x44(사용자_티어) in 50 박스
                            Box(modifier = Modifier.size(50.dp), contentAlignment = Alignment.Center) {
                                Image(
                                    painter = painterResource(R.drawable.img_tier_master_s),
                                    contentDescription = null,
                                    modifier = Modifier.size(44.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// 티어 승급 안내 항목 행 (height 50, gap 8): 아이콘50 + 텍스트콜럼. 폭은 hug(부제 한 줄 안 잘림)
@Composable
private fun HelpRow(
    title: String,
    subtitle: String,
    leading: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier.height(50.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        leading()
        Column {
            Text(
                text = title,
                style = TqType.BodyL.figma().copy(fontWeight = FontWeight.Medium), // 16/500
                color = Gray800,
            )
            Text(
                text = subtitle,
                style = TqType.BodyM.figma(), // 14/400
                color = Gray600,
                maxLines = 1,
                softWrap = false,
            )
        }
    }
}

// 항목 사이 chevron-down (24, Purple/600). 가운데 정렬은 호출부에서 Modifier.align으로.
@Composable
private fun ChevronDown(modifier: Modifier = Modifier) {
    Icon(
        painter = painterResource(R.drawable.ic_chevron_down),
        contentDescription = null,
        tint = Primary600,
        modifier = modifier.size(24.dp),
    )
}
