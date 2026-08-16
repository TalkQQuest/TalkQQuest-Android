package com.talkqquest.app.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
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
}
