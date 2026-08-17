package com.talkqquest.app.core.designsystem.component

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.talkqquest.app.core.designsystem.Gray600
import com.talkqquest.app.core.designsystem.ModalDimColor
import com.talkqquest.app.core.designsystem.ModalSystemBars
import com.talkqquest.app.core.designsystem.White
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

// ── 보관함 선택형 바텀시트 공용 스캐폴드 (TierPromotionSheet과 같은 메커니즘) ──
// Material3 ModalBottomSheet(=자기 Dialog 창)가 아니라 액티비티 창에 직접 그리는 커스텀 시트.
// 시트 본체 높이를 화면 바닥까지 고정해서 흰 배경이 시스템 네비바 자리까지 닿는다.
// (targetSdk 36에서 ModalBottomSheet는 자기 Dialog 창 색을 못 바꿔 그 자리에 딤 회색 띠가 남는 문제 해결)
// 내용은 옵션 리스트/달력처럼 화면마다 달라 content 람다로 받고, 콘텐츠 실측 높이만큼만 올라와 멈춘다
// (펼침 단계 없음 — 옛 M3 skipPartiallyExpanded와 같은 모양).
@Composable
fun TqChoiceBottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    cornerRadius: Dp = 36.dp,
    dragHandle: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        // 시트 위 끝(offsetY)이 오갈 정착점: 숨김(바닥) / 콘텐츠 실측 높이만큼 올라온 지점.
        val hiddenOffset = constraints.maxHeight.toFloat()
        // 위로 끌어올렸을 때 더 못 올라가는 한계(상태바 아래 28dp) — 콘텐츠가 화면을 꽉 채울 만큼 길 때의 안전판.
        val expandedOffset = WindowInsets.statusBars.getTop(density) + with(density) { 28.dp.toPx() }

        // 시트가 멈추는 위치 = 콘텐츠 실측 높이만큼 바닥에서 올라온 지점(측정 전엔 화면 밖).
        var contentHeightPx by remember { mutableFloatStateOf(0f) }
        val shownOffset = (hiddenOffset - contentHeightPx).coerceIn(expandedOffset, hiddenOffset)

        var offsetY by remember { mutableFloatStateOf(hiddenOffset) }
        var animJob by remember { mutableStateOf<Job?>(null) }

        fun animateSheetTo(target: Float, onArrived: () -> Unit = {}) {
            animJob?.cancel()
            animJob = scope.launch {
                val spec: AnimationSpec<Float> = tween(300, easing = FastOutSlowInEasing)
                animate(offsetY, target, animationSpec = spec) { value, _ -> offsetY = value }
                onArrived()
            }
        }

        // 손가락 이동만큼 시트 이동(펼침 한계~숨김 범위 안에서).
        fun dragBy(delta: Float): Float {
            val new = (offsetY + delta).coerceIn(expandedOffset, hiddenOffset)
            val consumed = new - offsetY
            if (consumed != 0f) offsetY = new
            return consumed
        }

        // 놓았을 때: 빠르게 튕기면 그 방향으로, 아니면 가장 가까운 정착점으로. 숨김이면 닫힘.
        fun settle(velocity: Float) {
            val target = when {
                velocity > 1000f -> hiddenOffset
                velocity < -1000f -> shownOffset
                else -> listOf(shownOffset, hiddenOffset).minByOrNull { abs(it - offsetY) } ?: shownOffset
            }
            animateSheetTo(target) { if (target == hiddenOffset) onDismiss() }
        }

        // 열고 닫기: 열리면 콘텐츠 높이만큼 올라오고, 닫히면 숨김으로 내려감.
        LaunchedEffect(visible, shownOffset) {
            animateSheetTo(if (visible) shownOffset else hiddenOffset)
        }

        // 배경 스크림 — 시트가 올라온 만큼 어두워짐. 앱 표준 모달 딤과 동일(Gray700 0.23).
        val dimFraction = if (hiddenOffset > shownOffset) {
            ((hiddenOffset - offsetY) / (hiddenOffset - shownOffset)).coerceIn(0f, 1f)
        } else {
            0f
        }
        ModalSystemBars(dimFraction, navigationBarColor = White)

        if (visible || offsetY < hiddenOffset - 0.5f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ModalDimColor.copy(alpha = ModalDimColor.alpha * dimFraction))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { animateSheetTo(hiddenOffset) { onDismiss() } },
                    ),
            )
        }

        // 시트 본체 — 펼침 한계까지 높이 고정(내용은 위쪽, 나머지는 시트 배경 → 화면 바닥까지 흰색이 닿음).
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(with(density) { (hiddenOffset - expandedOffset).toDp() })
                .offset { IntOffset(0, offsetY.roundToInt()) }
                .clip(RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius))
                .background(White)
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
                    .onSizeChanged { contentHeightPx = it.height.toFloat() },
            ) {
                if (dragHandle) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 20.dp, bottom = 12.dp)
                            .width(36.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(Gray600),
                    )
                }
                content()
            }
        }
    }
}
