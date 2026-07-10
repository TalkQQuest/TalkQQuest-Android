package com.talkqquest.app.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.talkqquest.app.core.designsystem.Primary300
import com.talkqquest.app.core.designsystem.Primary400
import com.talkqquest.app.core.designsystem.Primary600
import com.talkqquest.app.core.designsystem.Success
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// 레벨업 순간 레벨 표시 주변에서 작게 터지는 폭죽 (미션 완료 XP 카드·홈 레벨 카드 공용).
// progress 0→1: 입자 12개가 중심에서 사방으로 퍼지며 작아지고, 후반 40%에서 흐려져 사라짐.
// 감싼 대상(레벨 칩/글자)의 중심에서 바깥으로 그림 — 배치 공간은 안 차지함(반경 26, 과하지 않게).
// 색은 디자인 팔레트(Purple 600/400/300 + Success)만 사용. 크기·타이밍은 디자인에 없는
// 자작 연출 — 칩이 튀는 모션과 같은 순간에 600ms 재생.
@Composable
fun LevelUpBurst(progress: Float, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        if (progress <= 0f || progress >= 1f) return@Canvas
        val colors = listOf(Primary600, Primary400, Primary300, Success)
        val spread = 1f - (1f - progress) * (1f - progress) * (1f - progress) // 확 퍼지고 끝에서 감속
        val alpha = if (progress < 0.6f) 1f else (1f - (progress - 0.6f) / 0.4f).coerceIn(0f, 1f)
        val startDist = 8.dp.toPx()
        val endDist = 26.dp.toPx()
        val count = 12
        repeat(count) { i ->
            // 각도: 균등 분배 + 홀짝 지그재그로 살짝 흐트러뜨림(너무 기계적으로 안 보이게)
            val angle = (i.toFloat() / count) * 2f * PI.toFloat() + if (i % 2 == 0) 0.22f else -0.13f
            val dist = startDist + (endDist - startDist) * spread
            val radius = (if (i % 3 == 0) 2.5f else 1.8f).dp.toPx() * (1f - 0.45f * spread)
            drawCircle(
                color = colors[i % colors.size],
                radius = radius,
                center = center + Offset(cos(angle) * dist, sin(angle) * dist),
                alpha = alpha,
            )
        }
    }
}
