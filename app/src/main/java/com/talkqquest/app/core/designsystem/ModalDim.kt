package com.talkqquest.app.core.designsystem

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat

const val ModalDimDurationMillis = 360
val ModalDimColor = Gray700.copy(alpha = 0.23f)

private val ModalDimAnimation = tween<Float>(ModalDimDurationMillis, easing = FastOutSlowInEasing)

fun modalDimEnter(): EnterTransition = fadeIn(ModalDimAnimation)
fun modalDimExit(): ExitTransition = fadeOut(ModalDimAnimation)
fun modalCardEnter(): EnterTransition = modalDimEnter() + scaleIn(initialScale = 0.86f, animationSpec = ModalDimAnimation)
fun modalCardExit(): ExitTransition = modalDimExit() + scaleOut(targetScale = 0.86f, animationSpec = ModalDimAnimation)

@Composable
fun ModalDimOverlay(
    visible: Boolean,
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null,
) {
    ModalSystemBars(visible)
    AnimatedVisibility(visible = visible, enter = modalDimEnter(), exit = modalDimExit(), modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ModalDimColor)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { onDismiss?.invoke() },
                ),
        )
    }
}

@Composable
fun ModalSystemBars(visible: Boolean) {
    val progress = androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = ModalDimAnimation,
        label = "modalSystemBarDim",
    ).value
    ModalSystemBars(progress)
}

@Composable
fun ModalSystemBars(progress: Float) {
    val activity = LocalContext.current.findActivity() ?: return
    val window = activity.window
    val controller = remember(window) { WindowCompat.getInsetsController(window, window.decorView) }
    val originalStatusColor = remember(window) { window.statusBarColor }
    val originalNavigationColor = remember(window) { window.navigationBarColor }
    val originalLightStatus = remember(controller) { controller.isAppearanceLightStatusBars }
    val originalLightNavigation = remember(controller) { controller.isAppearanceLightNavigationBars }
    val ownsSystemBars = remember(window) { booleanArrayOf(false) }
    SideEffect {
        if (progress > 0f) {
            ownsSystemBars[0] = true
            val color = ModalDimColor.copy(alpha = ModalDimColor.alpha * progress.coerceIn(0f, 1f)).compositeOver(Color(0xFFF8FAFC))
            window.statusBarColor = color.toArgb()
            window.navigationBarColor = color.toArgb()
            controller.isAppearanceLightStatusBars = originalLightStatus
            controller.isAppearanceLightNavigationBars = originalLightNavigation
        } else if (ownsSystemBars[0]) {
            window.statusBarColor = originalStatusColor
            window.navigationBarColor = originalNavigationColor
            controller.isAppearanceLightStatusBars = originalLightStatus
            controller.isAppearanceLightNavigationBars = originalLightNavigation
            ownsSystemBars[0] = false
        }
    }
    DisposableEffect(window) {
        onDispose {
            if (ownsSystemBars[0]) {
                window.statusBarColor = originalStatusColor
                window.navigationBarColor = originalNavigationColor
                controller.isAppearanceLightStatusBars = originalLightStatus
                controller.isAppearanceLightNavigationBars = originalLightNavigation
                ownsSystemBars[0] = false
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
