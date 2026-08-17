package com.talkqquest.app.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.IntOffset

/** Shared timing for user-triggered screen and pager navigation. */
object NavigationMotion {
    const val DurationMillis = 300

    val floatSpec: FiniteAnimationSpec<Float> = tween(
        durationMillis = DurationMillis,
        easing = FastOutSlowInEasing,
    )
    val intOffsetSpec: FiniteAnimationSpec<IntOffset> = tween(
        durationMillis = DurationMillis,
        easing = FastOutSlowInEasing,
    )

    // 스와이프를 놓았을 때 손가락 속도를 이어받아 감속하도록 no-bounce 스프링으로 스냅한다.
    // tween과 달리 fling 속도를 인계받아 세련된 페이저 감각을 낸다.
    val pagerSnapSpec: FiniteAnimationSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow,
    )
}
