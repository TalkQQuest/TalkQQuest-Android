package com.talkqquest.app.core.designsystem.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay

// 칩·필터처럼 목록 내용이 통째로 갈릴 때 쓰는 머티리얼 "페이드 스루" 규격.
// 기존 목록: alpha 1→0 90ms. 새 목록: alpha 0→1 + scale 0.92→1.0, 210ms, 90ms 지연 뒤 시작.
// 카드별 등장/퇴장이 아니라 전체를 한 덩어리로 다룬다(정렬 전환의 animateItem과 구분).
private const val ExitDurationMillis = 90
private const val EnterDurationMillis = 210
private const val EnterDelayMillis = 90

@Composable
fun <T> ChipContentCrossfade(
    targetState: T,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit,
) {
    AnimatedContent(
        targetState = targetState,
        modifier = modifier,
        transitionSpec = {
            val enter = fadeIn(
                animationSpec = tween(EnterDurationMillis, delayMillis = EnterDelayMillis, easing = FastOutSlowInEasing),
            ) + scaleIn(
                initialScale = 0.92f,
                animationSpec = tween(EnterDurationMillis, delayMillis = EnterDelayMillis, easing = FastOutSlowInEasing),
            )
            val exit = fadeOut(animationSpec = tween(ExitDurationMillis, easing = FastOutSlowInEasing))
            // 크기는 스펙에 없는 별도 모션이라 즉시 스냅 — 화면에 보이는 건 alpha·scale뿐이어야 한다.
            enter.togetherWith(exit).using(SizeTransform(clip = false, sizeAnimationSpec = { _, _ -> snap() }))
        },
        label = "chipContentCrossfade",
    ) { state ->
        content(state)
    }
}

// 칩 강조 색은 누르는 즉시 바뀌어야 하지만, 실제 목록 데이터 교체(= 위 크로스페이드 재생)는
// 100ms 디바운스해 연타 중간 결과가 렌더되지 않게 한다. 마지막에 누른 값만 반영되고 큐에 쌓이지 않는다.
// current는 외부(ViewModel) 진짜 값 — 클릭이 아닌 다른 경로(탭 전환 리셋 등)로 바뀌면 즉시 따라간다.
@Composable
fun <T> rememberDebouncedChipSelection(
    current: T,
    debounceMillis: Long = 100L,
    onCommit: (T) -> Unit,
): DebouncedChipSelection<T> {
    var displayed by remember { mutableStateOf(current) }
    var pending by remember { mutableStateOf<T?>(null) }

    LaunchedEffect(current) {
        if (pending == null) displayed = current
    }
    LaunchedEffect(pending) {
        val target = pending ?: return@LaunchedEffect
        delay(debounceMillis)
        onCommit(target)
        // 반영 직후 pending을 곧장 null로 되돌리면, 그 사이 새로 눌린 값까지 같이 지워질 수 있다.
        // pending이 여전히 target 그대로일 때만 비운다 — 이미 다른 값으로 바뀌었다면 그 값을 그대로
        // 남겨 두어 다음 recomposition에서 이 LaunchedEffect가 새 target으로 다시 돌게 한다.
        if (pending == target) pending = null
    }

    return remember(displayed) {
        DebouncedChipSelection(displayed) { value ->
            displayed = value
            pending = value
        }
    }
}

class DebouncedChipSelection<T>(val displayed: T, val select: (T) -> Unit)
