package com.talkqquest.app.core.designsystem.component

import android.graphics.Typeface
import android.text.TextPaint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnitType
import kotlin.math.roundToInt

/**
 * Collects the visible ink bounds of the text that makes up one menu row.
 *
 * The row's Figma geometry remains untouched: this only tells [TqMenuRow] where
 * to draw its indication layer. A separate key is used for each text node so
 * title, description and trailing value form one visual anchor.
 */
@Stable
class TqMenuRowRippleAnchor internal constructor() {
    private val textBounds = mutableStateMapOf<Any, TextAnchorBounds>()

    internal fun updatePosition(key: Any, positionInRoot: Offset) {
        val current = textBounds[key] ?: TextAnchorBounds()
        if (current.positionInRoot != positionInRoot) {
            textBounds[key] = current.copy(positionInRoot = positionInRoot)
        }
    }

    internal fun updateInkBounds(key: Any, inkBounds: Rect) {
        val current = textBounds[key] ?: TextAnchorBounds()
        if (current.inkBounds != inkBounds) {
            textBounds[key] = current.copy(inkBounds = inkBounds)
        }
    }

    internal fun centerInRow(rowPositionInRoot: Offset?): Float? {
        if (rowPositionInRoot == null) return null

        var top = Float.POSITIVE_INFINITY
        var bottom = Float.NEGATIVE_INFINITY
        textBounds.values.forEach { bounds ->
            val position = bounds.positionInRoot ?: return@forEach
            val ink = bounds.inkBounds ?: return@forEach
            top = minOf(top, position.y - rowPositionInRoot.y + ink.top)
            bottom = maxOf(bottom, position.y - rowPositionInRoot.y + ink.bottom)
        }
        return if (top.isFinite() && bottom.isFinite()) (top + bottom) / 2f else null
    }
}

private data class TextAnchorBounds(
    val positionInRoot: Offset? = null,
    val inkBounds: Rect? = null,
)

/** Attach this to the existing [Text] modifier. It does not affect layout. */
fun Modifier.menuRowTextRippleAnchor(
    anchor: TqMenuRowRippleAnchor,
    key: Any,
): Modifier = onGloballyPositioned { coordinates ->
    anchor.updatePosition(key, coordinates.positionInRoot())
}

/**
 * Pass this to the existing [Text]'s onTextLayout. It measures glyph ink, not
 * Compose's line box, so Pretendard's baseline padding cannot bias the ripple.
 */
@Composable
fun rememberMenuRowTextLayoutCallback(
    anchor: TqMenuRowRippleAnchor,
    key: Any,
    text: String,
    style: TextStyle,
): (TextLayoutResult) -> Unit {
    val density = LocalDensity.current
    val fontFamilyResolver = LocalFontFamilyResolver.current
    val typeface = fontFamilyResolver.resolve(
        style.fontFamily,
        style.fontWeight ?: FontWeight.Normal,
        style.fontStyle ?: FontStyle.Normal,
        style.fontSynthesis ?: FontSynthesis.All,
    ).value as? Typeface

    return remember(anchor, key, text, style, density, typeface) {
        { layoutResult ->
            typeface?.let { resolvedTypeface ->
                layoutResult.menuRowInkBounds(text, style, density, resolvedTypeface)
            }?.let { inkBounds ->
                anchor.updateInkBounds(key, inkBounds)
            }
        }
    }
}

private fun TextLayoutResult.menuRowInkBounds(
    text: String,
    style: TextStyle,
    density: androidx.compose.ui.unit.Density,
    typeface: Typeface,
): Rect? {
    var left = Float.POSITIVE_INFINITY
    var right = Float.NEGATIVE_INFINITY
    text.forEachIndexed { index, character ->
        if (!character.isWhitespace()) {
            val bounds = getBoundingBox(index)
            left = minOf(left, bounds.left)
            right = maxOf(right, bounds.right)
        }
    }
    if (!left.isFinite() || !right.isFinite()) return null

    val paintBounds = android.graphics.Rect()
    TextPaint().apply {
        this.typeface = typeface
        textSize = with(density) { style.fontSize.toPx() }
        letterSpacing = when (style.letterSpacing.type) {
            TextUnitType.Em -> style.letterSpacing.value
            else -> 0f
        }
    }.getTextBounds(text, 0, text.length, paintBounds)
    if (paintBounds.isEmpty) return null

    return Rect(
        left = left,
        top = firstBaseline + paintBounds.top,
        right = right,
        bottom = firstBaseline + paintBounds.bottom,
    )
}

/**
 * Coordinates ripple geometry across a whole list of [TqAnchoredMenuRow]s so that
 * neighboring rows' indication layers meet at the midpoint between their text ink
 * centers, instead of each row guessing its own symmetric bounds independently.
 */
@Stable
class MenuRippleGroupState {
    private data class RowGeom(val inkCenterRoot: Float, val boxTopRoot: Float, val height: Float)
    private val rows = mutableStateMapOf<Any, RowGeom>()

    internal fun update(id: Any, inkCenterRoot: Float, boxTopRoot: Float, height: Float) {
        val g = RowGeom(inkCenterRoot, boxTopRoot, height)
        if (rows[id] != g) rows[id] = g
    }
    internal fun remove(id: Any) { rows.remove(id) }

    // 행의 리플 세로 범위 [topRoot, bottomRoot]. 자기 글자 중심을 기준으로 위·아래 대칭.
    // 이웃이 있으면 이웃 글자 중심과의 중점의 절반(=간격 절반)을 확장량으로, 없으면 반대쪽과 동일(미러).
    // 위·아래 확장량이 다르면 작은 쪽으로 맞춰 대칭 우선.
    internal fun rippleRange(id: Any): Pair<Float, Float>? {
        val self = rows[id] ?: return null
        val centers = rows.values.map { it.inkCenterRoot }.sorted()
        val c = self.inkCenterRoot
        val idx = centers.indexOf(c)
        if (idx < 0) return null
        val up = if (idx > 0) (c - centers[idx - 1]) / 2f else null
        val down = if (idx < centers.size - 1) (centers[idx + 1] - c) / 2f else null
        val h = when {
            up != null && down != null -> minOf(up, down)
            up != null -> up
            down != null -> down
            else -> minOf(c - self.boxTopRoot, self.boxTopRoot + self.height - c) // 단독 행: 박스 안 대칭
        }
        return (c - h) to (c + h)
    }
}

@Composable
fun rememberMenuRippleGroup(): MenuRippleGroupState = remember { MenuRippleGroupState() }

@Composable
fun TqAnchoredMenuRow(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    group: MenuRippleGroupState? = null,
    onClick: () -> Unit,
    content: @Composable BoxScope.(TqMenuRowRippleAnchor) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val tick = rememberHapticTick()
    val anchor = remember { TqMenuRowRippleAnchor() }
    val rowId = remember { Any() }
    var rowPositionInRoot by remember { mutableStateOf<Offset?>(null) }
    var rowSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current

    Box(
        modifier = modifier
            .onGloballyPositioned { coordinates ->
                rowPositionInRoot = coordinates.positionInRoot()
                rowSize = coordinates.size
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = { tick(); onClick() },
            ),
    ) {
        val centerLocal: Float? = if (rowSize.height > 0) rowSize.height / 2f else null
        val rowTopRoot = rowPositionInRoot?.y

        LaunchedEffect(group, centerLocal, rowTopRoot) {
            if (group != null && centerLocal != null && rowTopRoot != null) {
                group.update(rowId, rowTopRoot + centerLocal, rowTopRoot, rowSize.height.toFloat())
            }
        }
        DisposableEffect(group) { onDispose { group?.remove(rowId) } }

        val range = if (group != null && rowTopRoot != null) group.rippleRange(rowId) else null
        val overlayTopLocal: Float
        val overlayHeightPx: Float
        if (range != null && rowTopRoot != null) {
            overlayTopLocal = range.first - rowTopRoot
            overlayHeightPx = range.second - range.first
        } else {
            val ic = centerLocal ?: (rowSize.height / 2f)
            val h = minOf(ic, rowSize.height - ic)
            overlayTopLocal = ic - h
            overlayHeightPx = 2f * h
        }

        // The interaction hit area remains the Figma row rectangle. Only the
        // indication layer follows the actual rendered text ink, symmetric about
        // the midpoint between neighboring rows' text centers when part of a group.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .requiredHeight(with(density) { overlayHeightPx.coerceAtLeast(0f).toDp() })
                .offset { IntOffset(0, overlayTopLocal.roundToInt()) }
                .indication(
                    interactionSource = interactionSource,
                    indication = ripple(bounded = true),
                ),
        )

        content(anchor)
    }
}

/** Existing menu rows that do not opt into an ink anchor keep their prior behavior. */
@Composable
fun TqMenuRow(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val tick = rememberHapticTick()

    Box(
        modifier = modifier.clickable(
            interactionSource = interactionSource,
            indication = ripple(bounded = true),
            enabled = enabled,
            onClick = { tick(); onClick() },
        ),
        content = content,
    )
}
