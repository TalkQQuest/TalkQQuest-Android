package com.talkqquest.app.core.designsystem.component

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.talkqquest.app.core.designsystem.Gray1000
import com.talkqquest.app.core.designsystem.Gray50
import com.talkqquest.app.core.designsystem.Gray600
import com.talkqquest.app.core.designsystem.softShadow
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

// ── 저장 바텀시트 공용 컨테이너 (CSS Frame 455 — 미션 저장 시트·리포트 저장 시트가 완전 동일) ──
// 화면 내용을 감싸서 저장 시트를 위에 겹쳐 그리는 틀. 내용(카드)만 화면별로 다르고
// 프레임·동작은 같아서 core로 올림 (CONVENTIONS: 2개 이상 feature 재사용 → core).
// 모달이 아님: 배경이 안 어두워지고 뒤 화면도 계속 만질 수 있음.
// 동작: 저장 직후 하단 333 높이로 살짝 올라옴(CSS) → 위로 끌면 화면 꼭대기 근처까지 펼쳐짐(CSS top 68)
//       → 아래로 쓸어내리면 닫힘 → 안 만지고 두면 잠시 후 자동으로 스윽 내려감.

// 시트를 안 만지면 자동으로 닫히기까지의 시간.
private const val AUTO_DISMISS_MILLIS = 2_000L

// 시트가 살짝 올라온 높이 (CSS: 시트 top 519 → 화면 852 기준 보이는 부분 333)
private val SheetPeekHeight = 333.dp

// 끌어올렸을 때 시트 위 여백 (CSS top 68 = 목업 상태바 40 + 28 → 실기기는 상태바 인셋 + 28)
private val ExpandedTopGap = 28.dp

// 올라가는 시간. 내려갈 땐 2배 느리게 (사용자 결정).
private const val ASCENT_MILLIS = 300
private val AscentSpec: AnimationSpec<Float> = tween(ASCENT_MILLIS, easing = FastOutSlowInEasing)
private val DescentSpec: AnimationSpec<Float> = tween(ASCENT_MILLIS * 2, easing = FastOutSlowInEasing)

// 놓았을 때 이 속도(px/s)보다 빠르면 위치와 무관하게 그 방향의 다음 정착점으로.
private const val SETTLE_VELOCITY = 1_000f

private val SheetShape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp)

// savedItem이 생기면 시트가 올라오고, null이 되면 내려감.
// itemKey: 항목 식별자(id) — 내려가는 도중 다른 항목을 저장해도 새 내용으로 다시 올라오는 기준.
// itemIsSaved: false가 되면(시트에 뜬 항목을 해제) 회색 아이콘을 잠깐 보여준 뒤 내려감.
@Composable
fun <T : Any> TqSaveSheetScaffold(
    savedItem: T?,
    itemKey: Any?,
    itemIsSaved: Boolean,
    onDismiss: () -> Unit,
    onSheetTopChange: (Float?) -> Unit = {}, // 시트 위 끝 y(px), null=시트 없음. 하단 네비가 이 선 아래를 안 그려 시트 뒤에 있던 것처럼 드러남
    sheetContent: @Composable (T) -> Unit,
    content: @Composable () -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        content()

        val density = LocalDensity.current
        // 시트 위 끝(offsetY)이 오갈 세 정착점(px): 숨김 = 화면 바닥 / 살짝 = 바닥에서 333 / 펼침 = 꼭대기 근처
        val hiddenOffset = constraints.maxHeight.toFloat()
        val peekOffset = hiddenOffset - with(density) { SheetPeekHeight.toPx() }
        val expandedOffset =
            WindowInsets.statusBars.getTop(density) + with(density) { ExpandedTopGap.toPx() }

        var offsetY by remember { mutableFloatStateOf(hiddenOffset) }
        // 자동 닫힘 판단용: 지금 손을 대고 있는지 + 마지막으로 만진 시각 (만질 때마다 2초 타이머 리셋)
        var sheetPressed by remember(itemKey) { mutableStateOf(false) }
        var lastTouchAt by remember(itemKey) { mutableLongStateOf(System.currentTimeMillis()) }

        val scope = rememberCoroutineScope()
        var animJob by remember { mutableStateOf<Job?>(null) }

        // 정착점으로 이동. 위로 갈 땐 빠르게, 아래로 갈 땐 2배 느리게.
        fun animateSheetTo(target: Float, onArrived: () -> Unit = {}) {
            animJob?.cancel()
            animJob = scope.launch {
                val spec = if (target > offsetY) DescentSpec else AscentSpec
                animate(offsetY, target, animationSpec = spec) { value, _ -> offsetY = value }
                onArrived()
            }
        }

        // 손가락 이동만큼 시트 이동(범위 제한).
        fun dragBy(delta: Float): Float {
            val new = (offsetY + delta).coerceIn(expandedOffset, hiddenOffset)
            val consumed = new - offsetY
            if (consumed != 0f) offsetY = new
            return consumed
        }

        // 놓았을 때: 빠르게 튕겼으면 그 방향의 다음 정착점, 아니면 가장 가까운 정착점으로.
        fun settle(velocity: Float) {
            val anchors = listOf(expandedOffset, peekOffset, hiddenOffset)
            val target = when {
                velocity < -SETTLE_VELOCITY -> anchors.filter { it < offsetY }.maxOrNull() ?: expandedOffset
                velocity > SETTLE_VELOCITY -> anchors.filter { it > offsetY }.minOrNull() ?: hiddenOffset
                else -> anchors.minByOrNull { abs(it - offsetY) } ?: peekOffset
            }
            animateSheetTo(target) { if (target == hiddenOffset) onDismiss() }
        }

        // 내려가는 애니메이션 동안 보여줄 마지막 내용 (상태가 먼저 비면 시트가 애니메이션 없이 뚝 사라짐)
        var displayedItem by remember { mutableStateOf(savedItem) }
        if (savedItem != null) displayedItem = savedItem

        // 화면 상태 → 시트 올리고 내리기. id 기준이라 내려가는 도중 다른 항목을 저장해도
        // 내려가던 걸 끊고 새 내용으로 다시 올라옴.
        LaunchedEffect(itemKey, itemIsSaved) {
            when {
                savedItem == null -> animateSheetTo(hiddenOffset)
                itemIsSaved -> animateSheetTo(peekOffset)
                else -> {
                    // 시트에 뜬 항목이 해제됨: 회색으로 바뀐 아이콘을 잠깐 보여준 뒤 내려감
                    delay(300)
                    animateSheetTo(hiddenOffset) { onDismiss() }
                }
            }
        }
        // 자동 닫힘: 손을 뗀 뒤 2초 동안 안 만지면 스윽 내려감. 만질 때마다 타이머 리셋.
        // 예외: 손을 대고 있는 동안 / 끝까지 펼쳐놓은 상태에선 안 닫힘.
        LaunchedEffect(itemKey) {
            if (savedItem == null) return@LaunchedEffect
            var closing = false
            while (true) {
                delay(100)
                val fullyExpanded = offsetY <= expandedOffset + 1f
                if (sheetPressed || fullyExpanded) {
                    closing = false
                    continue
                }
                val idleFor = System.currentTimeMillis() - lastTouchAt
                if (idleFor >= AUTO_DISMISS_MILLIS) {
                    if (!closing) {
                        closing = true
                        animateSheetTo(hiddenOffset) { onDismiss() }
                    }
                } else {
                    closing = false
                }
            }
        }

        // 시트를 화면에 그릴지 여부 (내려가는 애니 동안에도 그려야 하니 offsetY로 판단).
        val sheetTopForNav = if (savedItem != null || offsetY < hiddenOffset - 0.5f) offsetY else null
        val sheetVisible = sheetTopForNav != null
        // 하단 네비에 시트 위 끝 위치를 매 프레임 알림 → 네비는 시트에 안 덮인 부분만 그림.
        SideEffect { onSheetTopChange(sheetTopForNav) }
        DisposableEffect(Unit) {
            onDispose { onSheetTopChange(null) }
        }

        // 시트 안 카드 스크롤과 시트 끌기 연결: 위로 밀면 시트가 먼저 펼쳐지고, 아래로 당기면 시트가 내려감.
        // source == UserInput 조건: 손가락 스크롤만 시트 드래그로 연결한다. 목록에서 카드가 빠져
        // 내용 높이가 줄면 Compose가 스크롤 위치를 자동 보정(SideEffect)하는데, 이걸 사용자 입력으로
        // 오해하면 시트가 순간 위로 튀었다 내려간다 → UserInput일 때만 반응하게 걸러냄.
        val nestedScrollConnection = object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset =
                if (source == NestedScrollSource.UserInput && available.y < 0) Offset(0f, dragBy(available.y)) else Offset.Zero

            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset =
                if (source == NestedScrollSource.UserInput && available.y > 0) Offset(0f, dragBy(available.y)) else Offset.Zero

            override suspend fun onPreFling(available: Velocity): Velocity {
                val atAnchor = offsetY == expandedOffset || offsetY == peekOffset || offsetY == hiddenOffset
                return if (!atAnchor) {
                    settle(available.y)
                    available
                } else {
                    Velocity.Zero
                }
            }
        }

        if (sheetVisible) {
            displayedItem?.let { item ->
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        // 시트 높이 = 펼쳤을 때 보이는 만큼. 살짝 상태에선 아래가 화면 밖으로 잘림(디자인 동일).
                        .height(with(density) { (hiddenOffset - expandedOffset).toDp() })
                        .offset { IntOffset(0, offsetY.roundToInt()) }
                        .nestedScroll(nestedScrollConnection)
                        // 시트 그림자 = CSS `0px -8px 24px rgba(15,23,42,0.06)`를 두 겹으로 나눠 그림.
                        // CSS 한 겹(6%)을 정확히 그려보면(픽셀 실측으로 CSS 가우시안과 오차 1/255 미만
                        // 확인함) 넓게 퍼진 그림자 전체가 진해서 시트 윤곽이 통째로 떠 보인다.
                        // → 넓은 그림자는 4.5%로 낮춰 은은하게 두고(아래), 시트와 배경이 맞닿는
                        //   경계에만 좁은 그림자를 한 겹 더 얹어 선처럼 경계를 잡는다(사용자 결정).
                        // ※ 피그마 값에서 의도적으로 벗어난 유일한 항목 — 디자이너 확인거리.
                        //
                        // (1) 접촉 그림자: 경계에 바짝 붙어 4dp 안에서 사라짐 → 경계선 역할
                        .softShadow(
                            color = Gray1000.copy(alpha = 0.06f),
                            offsetY = (-1).dp,
                            blur = 4.dp,
                            cornerRadius = 36.dp,
                        )
                        // (2) 넓은 그림자: CSS의 위치·흐림(offset -8, blur 24) 그대로, 진하기만 4.5%
                        .softShadow(
                            color = Gray1000.copy(alpha = 0.045f),
                            offsetY = (-8).dp,
                            blur = 24.dp,
                            cornerRadius = 36.dp, // SheetShape 위 모서리와 동일
                        )
                        // softShadow는 API 28 미만에서 생략됨(BlurMaskFilter 무시 → 각진 판이 그려짐).
                        // 시트 배경이 페이지와 같은 Gray50이라 그림자가 없으면 경계가 아예 사라지므로,
                        // 구형에서만 elevation 그림자로 폴백(근사).
                        .then(
                            if (android.os.Build.VERSION.SDK_INT < 28) {
                                Modifier.shadow(elevation = 8.dp, shape = SheetShape)
                            } else {
                                Modifier
                            },
                        )
                        .clip(SheetShape)
                        .background(Gray50) // 시트 배경 Gray/50 (CSS Frame 455)
                        // 손이 닿아 있는 동안 + 뗀 순간부터 2초를 재기 위한 감지 (이벤트는 소비 안 함)
                        .pointerInput(itemKey) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    sheetPressed = event.changes.any { it.pressed }
                                    lastTouchAt = System.currentTimeMillis()
                                }
                            }
                        }
                        .draggable(
                            orientation = Orientation.Vertical,
                            state = rememberDraggableState { delta -> dragBy(delta) },
                            onDragStarted = { animJob?.cancel() },
                            onDragStopped = { velocity -> settle(velocity) },
                        ),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        SheetDragHandle()
                        sheetContent(item)
                    }
                }
            }
        }
    }
}

// 드래그 핸들 (CSS Frame 453): 36x4 Gray600 알약. 위 20 = 시트 위 패딩(CSS), 아래 12 = 내용과 간격(gap).
@Composable
private fun SheetDragHandle() {
    Box(
        modifier = Modifier
            .padding(top = 20.dp, bottom = 12.dp)
            .width(36.dp)
            .height(4.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(Gray600),
    )
}
