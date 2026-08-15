package com.talkqquest.app.core.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

// 카톡류 미세한 틱. 모든 "누름" 요소가 공유하는 가벼운 햅틱.
// 스크롤·스와이프·텍스트 입력에는 쓰지 않는다(텍스트 입력은 키보드가 담당).
@Composable
fun rememberHapticTick(): () -> Unit {
    val haptic = LocalHapticFeedback.current
    return remember(haptic) {
        { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove) }
    }
}
