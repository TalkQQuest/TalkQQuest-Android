package com.talkqquest.app.core.designsystem.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.talkqquest.app.R
import com.talkqquest.app.core.designsystem.FitDesign
import com.talkqquest.app.core.designsystem.Gray50
import com.talkqquest.app.core.designsystem.Gray500
import com.talkqquest.app.core.designsystem.Gray800
import com.talkqquest.app.core.designsystem.Primary500
import com.talkqquest.app.core.designsystem.TqType
import kotlin.math.PI
import kotlin.math.sin
import kotlinx.coroutines.isActive

// ── 공용 대기 화면 ──
// 시안의 "대화 진입 애니메이션"(B담당)과 "온보딩->홈 애니메이션"(A담당)이 같은 프레임을 쓴다.
// 두 화면은 문구와 뒤로가기 유무만 다르고 나머지가 전부 같아서 여기 하나로 합쳤다.
// 쓰는 쪽은 message만 넘기면 된다.
//
// 시안 값(CSS Frame 427321613 기준)
//  · 배경 Gray/50 (#F8FAFC)
//  · 콘텐츠 블록 167x222, 가로 정중앙(left 113) / top 300
//  · 문구 167x80, Heading/XL 28/40 Bold, 자간 -0.02em, 가운데, Gray/800 → 위에서 442
//  · 뒤로가기 44x44, left 0 / top 48 (상태바 40 + 8)
//
// 망치 표시 높이와 문구 사이 간격으로 문구의 CSS y=442를 맞춘다(310 + 108 + 24).
private val SPINNER_TEXT_GAP = 24.dp
private val CONTENT_TOP = 310.dp
private val TEXT_BLOCK_WIDTH = 167.dp
private val TEXT_BLOCK_HEIGHT = 80.dp

private val FullLeading = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)

/**
 * 무언가를 준비하는 동안 잠깐 보여주는 화면.
 *
 * @param message 가운데 문구. 시안이 2줄이라 줄바꿈(\n)을 직접 넣어 쓴다.
 * @param onBack 뒤로가기를 둘 화면만 넘긴다. null이면 버튼을 그리지 않는다.
 * @param onAnimationFinished 망치 애니메이션 한 사이클이 끝난 뒤 필요한 화면만 넘기는 콜백.
 * @param compensateStatusBar 이미 FitDesign 안에서 부를 땐 false. 상태바 보정이 두 번 먹어
 *        좌표계가 어긋나는 걸 막는다(다른 화면의 로딩 상태로 끼워 쓸 때).
 */
@Composable
fun TqLoadingScreen(
    message: String,
    onBack: (() -> Unit)? = null,
    onAnimationFinished: () -> Unit = {},
    compensateStatusBar: Boolean = true,
) = FitDesign(compensateStatusBar = compensateStatusBar) {
    val tick = rememberHapticTick()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50),
    ) {
        if (onBack != null) {
            Box(
                modifier = Modifier
                    .statusBarsPadding()
                    .offset(y = 8.dp)
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable(onClick = { tick(); onBack() }),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_back_chevron),
                    contentDescription = "뒤로가기",
                    tint = Gray500, // CSS Icon border #64748B(Gray/500)
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = CONTENT_TOP)
                .width(TEXT_BLOCK_WIDTH),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SPINNER_TEXT_GAP),
        ) {
            TqLoadingHammer(onFinished = onAnimationFinished)
            Text(
                text = message,
                style = TqType.HeadingXL.copy(lineHeightStyle = FullLeading),
                color = Gray800,
                textAlign = TextAlign.Center,
                modifier = Modifier.height(TEXT_BLOCK_HEIGHT),
            )
        }
    }
}

@Composable
private fun TqLoadingHammer(onFinished: () -> Unit) {
    val fillProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        var didFinishFirstCycle = false
        while (isActive) {
            fillProgress.snapTo(0f)
            fillProgress.animateTo(
                targetValue = 1f,
                animationSpec = keyframes {
                    durationMillis = 1_500
                    0f at 0
                    1f at 1_000 using FastOutSlowInEasing
                    1f at 1_300
                    1f at 1_500
                },
            )
            if (!didFinishFirstCycle) {
                didFinishFirstCycle = true
                onFinished()
            }
        }
    }

    val progress = fillProgress.value
    val glowStart = ((progress - 0.08f) / 0.92f).coerceIn(0f, 1f)
    val glowAlpha = sin(glowStart * PI).toFloat().coerceAtLeast(0f) * 0.9f

    Box(modifier = Modifier.size(width = 156.dp, height = 108.dp)) {
        TqLoadingHammerCanvas(
            fillProgress = progress,
            modifier = Modifier.graphicsLayer {
                alpha = glowAlpha
                scaleX = 1.04f
                scaleY = 1.04f
                renderEffect = BlurEffect(18f, 18f, TileMode.Decal)
            },
        )
        TqLoadingHammerCanvas(fillProgress = progress)
    }
}

@Composable
private fun TqLoadingHammerCanvas(
    fillProgress: Float,
    modifier: Modifier = Modifier,
) {
    val body = painterResource(R.drawable.img_onboarding_complete_logo_body)
    val head = painterResource(R.drawable.img_onboarding_complete_logo_head)

    Canvas(modifier = modifier.size(width = 156.dp, height = 108.dp)) {
        val scale = size.width / 156f
        val bodyTopLeft = Offset(37f * scale, 4f * scale)
        val bodySize = Size(75f * scale, 97f * scale)
        val headTopLeft = Offset(107f * scale, 32f * scale)
        val headSize = Size(24f * scale, 27f * scale)

        fun drawHammer(colorFilter: ColorFilter) {
            translate(bodyTopLeft.x, bodyTopLeft.y) {
                with(body) { draw(size = bodySize, colorFilter = colorFilter) }
            }
            translate(headTopLeft.x, headTopLeft.y) {
                with(head) { draw(size = headSize, colorFilter = colorFilter) }
            }
        }

        val center = Offset(54f * scale, 96f * scale)
        val radius = 116f * scale * fillProgress
        val reveal = Path().apply {
            addOval(
                Rect(
                    left = center.x - radius,
                    top = center.y - radius,
                    right = center.x + radius,
                    bottom = center.y + radius,
                ),
            )
        }
        clipPath(reveal) {
            drawHammer(ColorFilter.tint(Primary500))
        }
    }
}

// ── 로딩 스피너 ──
// 호가 퍼졌다 모였다 하면서 돈다 — 안드로이드 기본 로딩 원과 같은 움직임.
//
// 원리: 머리와 꼬리를 같은 완급(FastOutSlowIn)으로 돌리되 꼬리를 반 주기 늦게 출발시킨다.
//  · 앞 절반 — 머리만 달려 나가 호가 길게 퍼진다
//  · 뒤 절반 — 꼬리가 쫓아와 호가 한 점으로 모인다
// 여기에 등속 바탕 회전을 얹으면 빨라졌다 느려지는 것처럼 읽힌다. 실제 가감속이 아니라
// 머리·꼬리의 속도 차가 만드는 착시다.
//
// ★끝나지 않고 계속 돈다. 서버 응답을 기다리는 화면에서 연출이 먼저 끝나
//   화면이 멈춘 것처럼 보이는 문제를 피하려고 무한 반복으로 뒀다.

// 전체가 한 바퀴 도는 시간(바탕 회전).
private const val SPIN_MILLIS = 2_800

// 호가 퍼졌다 모이는 한 주기. 회전 주기와 어긋나게 둬야 같은 장면이 반복되지 않는다.
private const val SWEEP_CYCLE_MILLIS = 2_000

// 호가 가장 길게 퍼졌을 때의 각도. 360에 가까우면 링이 돼버려 280에서 끊는다.
private const val SWEEP_SPAN = 280f

// 기본값은 화면 가운데 띄우는 일반 로딩용(48). 대기 화면처럼 크게 쓸 땐 인자로 키운다.
// 지름:두께 = 12:1을 지키면 어느 크기든 같은 인상이 난다.
@Composable
fun TqLoadingSpinner(
    modifier: Modifier = Modifier,
    diameter: Dp = 48.dp,
    strokeWidth: Dp = 4.dp,
    color: Color = Primary500, // 시안 일러스트의 보라(#7264F8)
) {
    val transition = rememberInfiniteTransition(label = "spinner")
    // 바탕 회전 — 등속으로 계속 돈다.
    val base by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(SPIN_MILLIS, easing = LinearEasing)),
        label = "base",
    )
    // 퍼짐/모임 주기 안에서의 진행도(0~1). 완급은 아래에서 머리·꼬리에 따로 먹인다.
    val cycle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(SWEEP_CYCLE_MILLIS, easing = LinearEasing)),
        label = "cycle",
    )

    // 머리는 앞 절반(0~0.5), 꼬리는 뒤 절반(0.5~1)에 움직인다.
    val head = FastOutSlowInEasing.transform((cycle * 2f).coerceIn(0f, 1f)) * SWEEP_SPAN
    val tail = FastOutSlowInEasing.transform((cycle * 2f - 1f).coerceIn(0f, 1f)) * SWEEP_SPAN

    Canvas(modifier = modifier.size(diameter)) {
        val stroke = strokeWidth.toPx()
        val inset = stroke / 2f
        drawArc(
            color = color,
            // 꼬리가 이번 주기에 전진한 만큼을 바탕 회전에 더해 다음 주기가 이어 돌게 한다.
            startAngle = base + tail,
            sweepAngle = head - tail,
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = Size(size.width - stroke, size.height - stroke),
            // 둥근 끝이라 호가 0으로 모여도 사라지지 않고 동그란 점으로 남는다.
            style = Stroke(width = stroke, cap = StrokeCap.Round),
        )
    }
}
