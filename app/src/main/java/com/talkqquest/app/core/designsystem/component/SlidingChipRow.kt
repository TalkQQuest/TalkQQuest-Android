package com.talkqquest.app.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.animateIntSizeAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.talkqquest.app.core.designsystem.Gray100
import com.talkqquest.app.core.designsystem.Gray200
import com.talkqquest.app.core.designsystem.Gray300
import com.talkqquest.app.core.designsystem.Gray400
import com.talkqquest.app.core.designsystem.Gray900
import com.talkqquest.app.core.designsystem.Primary50
import com.talkqquest.app.core.designsystem.Primary600
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.feature.mission.ui.figma
import kotlin.math.roundToInt

// 단일 선택 칩 로우 + 슬라이딩 인디케이터.
// ConversationSetupScreens.kt "화면 3 · 성별·나이"의 3-레이어 기법(①모든 칩 위치에 항상 그려지는
// 테두리 ②선택된 칩 위로 이동하는 Primary600 인디케이터 ③테두리·배경 없는 투명 텍스트 칩)을
// 재사용 가능한 컴포넌트로 뽑은 것. 원본은 건드리지 않음.
@Composable
fun SlidingChipRow(
    options: List<String>,
    selectedIndex: Int?,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    scrollable: Boolean = false,
    enabled: (Int) -> Boolean = { true },
    interactionEnabled: Boolean = true,
) {
    val density = LocalDensity.current
    val tick = rememberHapticTick()
    var bounds by remember { mutableStateOf<Map<Int, Pair<IntOffset, IntSize>>>(emptyMap()) }
    var moved by remember { mutableStateOf(false) }
    val parentOffset = remember { mutableStateOf(IntOffset.Zero) }
    val scrollState = rememberScrollState()

    // 선택값이 비활성 칩을 가리키면(외부 상태 불일치) 인디케이터는 움직이지 않는다.
    val effectiveSelectedIndex = selectedIndex?.takeIf { enabled(it) }
    val selectedBounds = effectiveSelectedIndex?.let { bounds[it] }

    val indicatorOffset by animateIntOffsetAsState(
        selectedBounds?.first ?: IntOffset.Zero,
        if (moved) tween(240, easing = FastOutSlowInEasing) else snap(),
        label = "slidingChipOffset",
    )
    val indicatorSize by animateIntSizeAsState(
        selectedBounds?.second ?: IntSize.Zero,
        if (moved) tween(240, easing = FastOutSlowInEasing) else snap(),
        label = "slidingChipSize",
    )
    val indicatorAlpha by animateFloatAsState(
        if (effectiveSelectedIndex != null) 1f else 0f,
        tween(220, easing = FastOutSlowInEasing),
        label = "slidingChipAlpha",
    )

    Box(
        modifier = modifier
            .then(if (scrollable) Modifier.horizontalScroll(scrollState) else Modifier),
    ) {
        Box(
            modifier = Modifier.onGloballyPositioned { c ->
                val p = c.positionInWindow()
                parentOffset.value = IntOffset(p.x.roundToInt(), p.y.roundToInt())
            },
        ) {
            // Layer 1: 모든 칩 위치에 항상 그려지는 테두리 (선택 여부와 무관)
            bounds.forEach { (index, measured) ->
                val (offset, size) = measured
                val disabled = !enabled(index)
                Box(
                    Modifier
                        .offset { offset }
                        .size(
                            with(density) { size.width.toDp() },
                            with(density) { size.height.toDp() },
                        )
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (disabled) Gray100 else Color.Transparent)
                        .border(1.dp, if (disabled) Gray200 else Gray300, RoundedCornerShape(20.dp)),
                )
            }
            // Layer 2: 선택된 칩 위로 슬라이드하는 Purple 인디케이터 (테두리를 덮는다)
            if (indicatorSize != IntSize.Zero) {
                Box(
                    Modifier
                        .offset { indicatorOffset }
                        .size(
                            with(density) { indicatorSize.width.toDp() },
                            with(density) { indicatorSize.height.toDp() },
                        )
                        .graphicsLayer { alpha = indicatorAlpha }
                        .clip(RoundedCornerShape(20.dp))
                        .background(Primary600),
                )
            }
            // Layer 3: 테두리·배경 없는 투명 텍스트 칩
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                options.forEachIndexed { i, label ->
                    val isSelected = selectedIndex == i
                    val isEnabled = enabled(i)
                    val textColor by animateColorAsState(
                        targetValue = when {
                            !isEnabled -> Gray400
                            isSelected -> Primary50
                            else -> Gray900
                        },
                        animationSpec = tween(220, easing = FastOutSlowInEasing),
                        label = "slidingChipTextColor",
                    )
                    Box(
                        modifier = Modifier
                            .onGloballyPositioned { c ->
                                val p = c.positionInWindow()
                                bounds = bounds + (
                                    i to (
                                        IntOffset(
                                            p.x.roundToInt() - parentOffset.value.x,
                                            p.y.roundToInt() - parentOffset.value.y,
                                        ) to c.size
                                    )
                                )
                            }
                            .height(34.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.Transparent)
                            .clickable(
                                enabled = interactionEnabled && isEnabled,
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) {
                                tick()
                                if (selectedIndex != null && selectedIndex != i) moved = true
                                onSelect(i)
                            }
                            .padding(horizontal = 18.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = label,
                            style = TqType.LabelL.figma(),
                            color = textColor,
                        )
                    }
                }
            }
        }
    }
}
