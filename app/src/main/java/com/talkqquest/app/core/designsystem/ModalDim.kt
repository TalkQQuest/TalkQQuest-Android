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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat

const val ModalDimDurationMillis = 360
val ModalDimColor = Gray700.copy(alpha = 0.23f)

// 시스템 상단·하단 바 영역에 딤을 직접 칠하기 위한 전역 상태.
// Android 15(targetSdk 36) 이상에서 window.statusBarColor/navigationBarColor가 no-op이라
// 그 대신 MainScreen.kt 최상위에서 인셋 높이만큼의 띠를 직접 그린다(안내는 MainScreen.kt 참고).
// 여러 딤이 동시에 뜰 수 있으므로 호출자(키)별로 progress를 들고 있다가 가장 큰 값이 이기게 한다.
object ModalDimBars {
    var progress by mutableFloatStateOf(0f)
        private set
    var navigationBarColor by mutableStateOf<Color?>(null)
        private set

    private val entries = mutableMapOf<Any, Pair<Float, Color?>>()

    // 부동소수점 잔차(예: 인셋이 프레임 사이에 미세하게 흔들려 0으로 정확히 떨어지지 않는 경우) 때문에
    // 딤이 사실상 끝났는데도 항목이 계속 등록돼 있는 것을 막기 위한 최소 임계값.
    // 이 값 이하는 "닫힘"으로 보고 등록 대신 해제한다.
    private const val MinVisibleProgress = 0.001f

    fun update(key: Any, progress: Float, navigationBarColor: Color?) {
        if (progress > MinVisibleProgress) {
            entries[key] = progress to navigationBarColor
        } else {
            entries.remove(key)
        }
        val winner = entries.values.maxByOrNull { it.first }
        this.progress = winner?.first ?: 0f
        this.navigationBarColor = winner?.second
    }
}

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
fun ModalSystemBars(visible: Boolean, navigationBarColor: Color? = null) {
    val progress = androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = ModalDimAnimation,
        label = "modalSystemBarDim",
    ).value
    ModalSystemBars(progress, navigationBarColor)
}

@Composable
fun ModalSystemBars(progress: Float, navigationBarColor: Color? = null) {
    val activity = LocalContext.current.findActivity() ?: return
    val window = activity.window
    val controller = remember(window) { WindowCompat.getInsetsController(window, window.decorView) }
    val originalLightStatus = remember(controller) { controller.isAppearanceLightStatusBars }
    val originalLightNavigation = remember(controller) { controller.isAppearanceLightNavigationBars }
    val ownsSystemBars = remember(window) { booleanArrayOf(false) }
    // 이 호출부(다이얼로그·시트마다 하나씩)를 식별하는 키. 전역 딤 상태에 자기 progress를 등록·해제할 때 쓴다.
    val dimKey = remember { Any() }
    SideEffect {
        // window.statusBarColor / navigationBarColor 대입은 Android 15(API 35) 이상에서 no-op이라 삭제했다.
        // 실제 딤 색은 MainScreen.kt가 ModalDimBars.progress를 읽어 인셋 영역에 직접 그린다.
        if (progress > 0f) {
            ownsSystemBars[0] = true
            if (navigationBarColor != null) {
                controller.isAppearanceLightNavigationBars = false
            } else {
                controller.isAppearanceLightNavigationBars = originalLightNavigation
            }
            controller.isAppearanceLightStatusBars = originalLightStatus
        } else if (ownsSystemBars[0]) {
            controller.isAppearanceLightStatusBars = originalLightStatus
            controller.isAppearanceLightNavigationBars = originalLightNavigation
            ownsSystemBars[0] = false
        }
        ModalDimBars.update(dimKey, progress.coerceIn(0f, 1f), navigationBarColor)
    }
    DisposableEffect(window) {
        onDispose {
            if (ownsSystemBars[0]) {
                controller.isAppearanceLightStatusBars = originalLightStatus
                controller.isAppearanceLightNavigationBars = originalLightNavigation
                ownsSystemBars[0] = false
            }
            ModalDimBars.update(dimKey, 0f, null)
        }
    }
}

// Material3 ModalBottomSheet draws into its own window (a Dialog), not the activity window,
// so ModalSystemBars(navigationBarColor = ...) on the activity never reaches its nav buttons.
// Call this from inside the sheet's content lambda to set the sheet's own window instead.
@Composable
fun DialogWindowLightNavigationButtons(light: Boolean) {
    val dialogWindow = (LocalView.current.parent as? DialogWindowProvider)?.window
    DisposableEffect(dialogWindow, light) {
        if (dialogWindow == null) {
            onDispose { }
        } else {
            val controller = WindowCompat.getInsetsController(dialogWindow, dialogWindow.decorView)
            val original = controller.isAppearanceLightNavigationBars
            controller.isAppearanceLightNavigationBars = light
            onDispose { controller.isAppearanceLightNavigationBars = original }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
