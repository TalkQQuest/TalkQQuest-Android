package com.talkqquest.app.core.designsystem

import android.graphics.BlurMaskFilter
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * CSS box-shadow를 그대로 그리는 소프트 그림자.
 *
 * 기본 `Modifier.shadow(elevation)`는 "높이" 하나만 받아서 피그마의
 * `box-shadow: x y blur color` 값(방향·흐림·색·투명도)을 못 담습니다.
 * 이 함수는 그 네 가지를 그대로 받아 직접 그립니다.
 *
 * 사용 예 — 피그마 카드 그림자 `0px 8px 24px rgba(15, 23, 42, 0.04)`:
 * ```
 * Modifier.softShadow(
 *     color = Gray1000.copy(alpha = 0.04f), // rgba(15,23,42,0.04)
 *     offsetY = 8.dp,                       // 두 번째 값 (아래로)
 *     blur = 24.dp,                         // 세 번째 값 (흐림)
 *     cornerRadius = 20.dp,                 // 대상의 radius와 동일하게
 * )
 * ```
 * 순서 주의: clip/background보다 **먼저** 붙여야 그림자가 밖으로 그려집니다.
 * (하단 네비·홈·미션 목록에서 로컬로 쓰던 것을 공통으로 올린 것 — 동작 동일)
 */
fun Modifier.softShadow(
    color: Color,
    offsetY: Dp,
    blur: Dp,
    cornerRadius: Dp,
    offsetX: Dp = 0.dp,
): Modifier = this.drawBehind {
    val paint = Paint().apply { this.color = color }
    val blurPx = blur.toPx()
    if (blurPx > 0f) {
        paint.asFrameworkPaint().maskFilter = BlurMaskFilter(blurPx, BlurMaskFilter.Blur.NORMAL)
    }
    drawIntoCanvas { canvas ->
        canvas.drawRoundRect(
            left = offsetX.toPx(),
            top = offsetY.toPx(),
            right = size.width + offsetX.toPx(),
            bottom = size.height + offsetY.toPx(),
            radiusX = cornerRadius.toPx(),
            radiusY = cornerRadius.toPx(),
            paint = paint,
        )
    }
}
