package com.talkqquest.app.core.designsystem

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp

// ── 작은 화면 대응: 디자인 통째 축소 (사용자 결정) ──
// 피그마 기준 화면(393x852)보다 작은 기기에선 밀도를 낮춰 글자·카드·이미지·여백을
// 전부 같은 비율로 줄임 → 피그마 배치 그대로 스크롤 없이 한 화면에 담김.
// 큰 화면(비율 1)에선 아무 것도 안 함.
// (미션 상세·대화 준비·홈 등 전 화면 공용 — core/designsystem에 둠)

// 현재 적용된 축소 비율 (1 = 원본). 화면 밖 요소(하단 알약)와 맞닿는 여백 보정용.
val LocalDesignScale = staticCompositionLocalOf { 1f }

@Composable
fun FitDesign(content: @Composable () -> Unit) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        // 세로 기준 900 = 디자인 852 + 실기기 상태바·시스템네비가 축소 후 더 차지하는 몫의 여유(근사).
        val scale = minOf(maxWidth / 393.dp, maxHeight / 900.dp, 1f)
        if (scale < 1f) {
            val base = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(base.density * scale, base.fontScale),
                LocalDesignScale provides scale,
            ) {
                content()
            }
        } else {
            content()
        }
    }
}
