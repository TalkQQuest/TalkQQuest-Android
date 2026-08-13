package com.talkqquest.app.feature.report.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import com.talkqquest.app.core.designsystem.TqType
import kotlin.math.cos
import kotlin.math.sin

// ── 승급 모션 ─────────────────────────────────────────────────────────────
// 마름모 4꼭짓점이 중앙으로 모여 별 하나가 되고, 그 별이 위 티어 카드의 빈 별 자리로 날아가 박힌다.
// 그 별로 3개가 차면(= 티어 승급) 딤이 깔리고 새 휘장이 등장한다.
//
// 딤은 "대화 이탈 팝업"과 같은 결(360ms · FastOutSlowInEasing · Gray700)을 쓰되,
// 그 위에 휘장을 세워야 해서 알파만 0.23 → 0.78 로 올렸다. 팝업 딤을 그대로 쓰면
// 뒤의 흰 카드가 비쳐서 휘장도 문구도 묻힌다.
private val DimColor = Color(0xFF1E293B) // Gray800 — 팝업 딤보다 한 단계 어두운 쪽
private const val DIM_ALPHA = 0.78f

private val MotionStarYellow = Color(0xFFF9AC17)   // 티어 카드 별과 같은 값
private val MotionStarLight = Color(0xFFFFD874)

private val MotionFullLeading = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)

private fun TextStyle.motionLine(): TextStyle = copy(lineHeightStyle = MotionFullLeading)

// 모션 전체가 공유하는 진행값. 마름모(CompetencyCard)·별 자리(TierCard)·오버레이가
// 같은 인스턴스를 보고 그린다.
internal class TierMotion {
    val converge = Animatable(0f)      // 0 = 마름모 그대로, 1 = 네 꼭짓점이 중앙 집결
    val starBirth = Animatable(0f)     // 중앙에서 별이 태어나는 진행
    val fly = Animatable(0f)           // 0 = 마름모 중앙, 1 = 티어 카드 별 자리
    val landPunch = Animatable(0f)     // 별이 박히는 순간의 튕김
    val dim = Animatable(0f)
    val emblem = Animatable(0f)        // 새 휘장 등장
    val celebrateText = Animatable(0f)

    // 슬롯에 실제로 불이 들어온 별 개수. -1이면 모션 전이라 원래 값을 그대로 쓴다.
    var litStars by mutableStateOf(-1)
    var targetSlot by mutableStateOf(0)
    var flyingStarVisible by mutableStateOf(false)

    // 좌표(루트 기준 px). 레이아웃이 잡히면 화면이 채워 준다.
    var radarCenter by mutableStateOf(Offset.Unspecified)
    var starSlots by mutableStateOf<List<Offset>>(emptyList())

    val ready: Boolean
        get() = radarCenter.isSpecified && starSlots.isNotEmpty()

    // 처음 상태로 되돌린다(다시 보기).
    suspend fun reset() {
        converge.snapTo(0f)
        starBirth.snapTo(0f)
        fly.snapTo(0f)
        landPunch.snapTo(0f)
        dim.snapTo(0f)
        emblem.snapTo(0f)
        celebrateText.snapTo(0f)
        litStars = -1
        flyingStarVisible = false
    }

    // 꼭짓점 집결 → 별 탄생 → 비행 → 착지. 승급이면 이어서 딤 + 휘장.
    suspend fun play(slot: Int, isTierUp: Boolean, onTierSwap: () -> Unit) {
        targetSlot = slot
        converge.animateTo(1f, tween(560, easing = FastOutSlowInEasing))
        flyingStarVisible = true
        starBirth.animateTo(1f, tween(420, easing = LinearOutSlowInEasing))
        fly.animateTo(1f, tween(660, easing = FastOutSlowInEasing))
        flyingStarVisible = false
        litStars = slot + 1
        landPunch.animateTo(1f, tween(300, easing = LinearOutSlowInEasing))
        landPunch.animateTo(0f, tween(220, easing = FastOutSlowInEasing))

        if (!isTierUp) return

        dim.animateTo(1f, tween(360, easing = FastOutSlowInEasing))
        onTierSwap()
        emblem.animateTo(1f, tween(760, easing = LinearOutSlowInEasing))
        celebrateText.animateTo(1f, tween(420, easing = FastOutSlowInEasing))
    }

    suspend fun dismissCelebration() {
        celebrateText.animateTo(0f, tween(200, easing = LinearEasing))
        emblem.animateTo(0f, tween(240, easing = FastOutSlowInEasing))
        dim.animateTo(0f, tween(360, easing = FastOutSlowInEasing))
    }

    // 마무리 — 중심에 모여 있던 마름모가 새 마름모(리셋된 점수)로 다시 피어난다.
    // 이 단계가 없으면 왜 점수가 줄었는지가 화면에서 끊겨 보인다.
    suspend fun settle() {
        converge.animateTo(0f, tween(520, easing = FastOutSlowInEasing))
    }
}

private val Offset.isSpecified: Boolean
    get() = this != Offset.Unspecified

@Composable
internal fun rememberTierMotion(): TierMotion = remember { TierMotion() }

// ── 오버레이 ──
// 화면 루트에 깔린다. 날아가는 별 → 딤 → 새 휘장 순서로 겹친다.
@Composable
internal fun TierPromotionOverlay(
    motion: TierMotion,
    newTierName: String,
    newTierEmblemRes: Int,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val flyingStarPx = with(density) { 34.dp.toPx() }
    val slotStarPx = with(density) { 15.dp.toPx() }

    Box(modifier = modifier.fillMaxSize()) {
        // 1) 딤 — 승급일 때만. 대화 팝업과 같은 결로 스르륵 올라온다.
        if (motion.dim.value > 0f) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(color = DimColor.copy(alpha = DIM_ALPHA * motion.dim.value))
            }
        }

        // 2) 날아가는 별 — 딤보다 위. 중앙에서 태어나 별 자리로 포물선을 그린다.
        if (motion.flyingStarVisible && motion.ready) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val from = motion.radarCenter
                val to = motion.starSlots.getOrNull(motion.targetSlot) ?: return@Canvas
                val t = motion.fly.value
                // 위로 볼록한 2차 베지어 — 직선으로 가면 사이의 카드를 관통해 지나가 보인다.
                val control = Offset(
                    x = (from.x + to.x) / 2f - 30f,
                    y = minOf(from.y, to.y) - 70f,
                )
                val pos = quadBezier(from, control, to, t)
                val birth = motion.starBirth.value
                // 태어날 때 살짝 커졌다가(1.28) 제자리로, 날아가면서 슬롯 크기까지 줄어든다.
                val birthScale = if (birth < 0.62f) birth / 0.62f * 1.28f
                else 1.28f - (birth - 0.62f) / 0.38f * 0.28f
                val flyScale = 1f - t * (1f - slotStarPx / flyingStarPx)
                val r = flyingStarPx / 2f * birthScale * flyScale
                if (r <= 0f) return@Canvas

                // 잔광 — 지나온 자리에 옅은 꼬리를 남긴다.
                if (t > 0.04f) {
                    listOf(0.10f, 0.20f, 0.32f).forEachIndexed { i, back ->
                        val tp = (t - back).coerceAtLeast(0f)
                        val p = quadBezier(from, control, to, tp)
                        drawStar(
                            center = p,
                            outerRadius = r * (0.72f - i * 0.16f),
                            color = MotionStarLight.copy(alpha = 0.30f - i * 0.09f),
                            rotationDeg = t * 300f,
                        )
                    }
                }

                // 발광 헤일로
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(MotionStarLight.copy(alpha = 0.55f), Color.Transparent),
                        center = pos,
                        radius = r * 2.4f,
                    ),
                    radius = r * 2.4f,
                    center = pos,
                )
                drawStar(pos, r, MotionStarYellow, rotationDeg = t * 300f)
            }
        }

        // 3) 승급 축하 — 딤 위에 새 휘장 + 문구.
        if (motion.dim.value > 0.01f) {
            val e = motion.emblem.value
            val textProgress = motion.celebrateText.value
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                // 문구까지 합쳐 화면 한가운데 오면 아래 범례 줄과 겹쳐 읽힌다.
                // 묶음을 조금 올려 문구가 빈 자리에 놓이게 한다.
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.offset(y = (-56).dp),
                ) {
                    // 문구 영역은 처음부터 확보해 레이아웃 높이가 도중에 바뀌지 않게 한다.
                    // 문구가 나타나는 동안 휘장이 중앙에서 최종 위치로 자연스럽게 올라간다.
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.graphicsLayer {
                            translationY = with(density) {
                                ((1f - textProgress) * 34f).dp.toPx()
                            }
                        },
                    ) {
                        // 뒤로 퍼지는 광선 — 휘장이 자리를 잡는 동안 천천히 돈다.
                        Canvas(modifier = Modifier.size(240.dp)) {
                            if (e <= 0f) return@Canvas
                            val cx = size.width / 2f
                            val cy = size.height / 2f
                            // 은은한 후광 — 광선보다 이쪽이 주가 되게 한다.
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        MotionStarLight.copy(alpha = 0.30f * e),
                                        Color.Transparent,
                                    ),
                                    center = Offset(cx, cy),
                                    radius = size.minDimension * 0.46f,
                                ),
                                radius = size.minDimension * 0.46f,
                                center = Offset(cx, cy),
                            )
                            // 가는 광선 — 길이를 짧게, 알파를 낮춰 장식 정도로만 둔다.
                            rotate(degrees = e * 18f, pivot = Offset(cx, cy)) {
                                repeat(16) { i ->
                                    val a = (Math.PI * 2 / 16 * i).toFloat()
                                    val inner = size.minDimension * 0.30f
                                    val outer = size.minDimension * (0.33f + 0.10f * e)
                                    val w = size.minDimension * 0.006f
                                    val ray = Path().apply {
                                        moveTo(cx + cos(a) * inner - sin(a) * w, cy + sin(a) * inner + cos(a) * w)
                                        lineTo(cx + cos(a) * inner + sin(a) * w, cy + sin(a) * inner - cos(a) * w)
                                        lineTo(cx + cos(a) * outer, cy + sin(a) * outer)
                                        close()
                                    }
                                    drawPath(ray, color = MotionStarLight.copy(alpha = 0.34f * e))
                                }
                            }
                            // 바깥으로 한 번 퍼지고 사라지는 링
                            drawCircle(
                                color = MotionStarLight.copy(alpha = (1f - e) * 0.45f),
                                radius = size.minDimension * (0.20f + 0.26f * e),
                                center = Offset(cx, cy),
                                style = Stroke(width = 2.dp.toPx()),
                            )
                        }
                        // 휘장 — 작게 시작해 살짝 넘겼다가 제자리로.
                        val emblemScale = if (e < 0.72f) 0.34f + e / 0.72f * 0.78f
                        else 1.12f - (e - 0.72f) / 0.28f * 0.12f
                        Image(
                            painter = painterResource(newTierEmblemRes),
                            contentDescription = null,
                            modifier = Modifier
                                .size(148.dp)
                                .graphicsLayer {
                                    scaleX = emblemScale
                                    scaleY = emblemScale
                                    rotationZ = (1f - e) * -22f
                                    alpha = (e * 2.2f).coerceAtMost(1f)
                                },
                        )
                    }

                    Spacer(Modifier.height(10.dp))
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.graphicsLayer {
                            alpha = textProgress
                            translationY = (1f - textProgress) * 22f
                        },
                    ) {
                        Text(
                            text = "$newTierName 승급!",
                            style = TqType.HeadingL.motionLine(),
                            color = Color.White,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "별 3개를 모아 티어가 올랐어요",
                            style = TqType.BodyM.motionLine(),
                            color = Color.White.copy(alpha = 0.82f),
                        )
                    }
                }
            }
        }
    }
}

// ── 그리기 유틸 ──

// 5각 별. 위 꼭짓점에서 시작해 바깥·안쪽 반지름을 번갈아 잇는다.
internal fun DrawScope.drawStar(
    center: Offset,
    outerRadius: Float,
    color: Color,
    rotationDeg: Float = 0f,
    innerRatio: Float = 0.46f,
) {
    if (outerRadius <= 0f) return
    val path = Path()
    val start = (-Math.PI / 2).toFloat() + Math.toRadians(rotationDeg.toDouble()).toFloat()
    repeat(10) { i ->
        val r = if (i % 2 == 0) outerRadius else outerRadius * innerRatio
        val a = start + (Math.PI / 5).toFloat() * i
        val x = center.x + cos(a) * r
        val y = center.y + sin(a) * r
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, color = color)
}

internal fun quadBezier(p0: Offset, p1: Offset, p2: Offset, t: Float): Offset {
    val u = 1f - t
    return Offset(
        x = u * u * p0.x + 2f * u * t * p1.x + t * t * p2.x,
        y = u * u * p0.y + 2f * u * t * p1.y + t * t * p2.y,
    )
}
