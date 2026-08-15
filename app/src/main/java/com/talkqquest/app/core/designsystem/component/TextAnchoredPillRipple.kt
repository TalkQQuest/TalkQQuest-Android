package com.talkqquest.app.core.designsystem.component

import android.graphics.Typeface
import android.text.TextPaint
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
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
import androidx.compose.ui.unit.dp
import kotlin.math.ceil
import kotlin.math.floor

data class TextPillRippleBounds(
    val positionInParent: Offset,
    val positionInRoot: Offset,
    val size: IntSize,
)

data class TextPillRippleGlyphBounds(
    val rect: Rect,
)

@Composable
fun rememberTextPillRippleBounds(): MutableState<TextPillRippleBounds?> =
    remember { mutableStateOf(null) }

@Composable
fun rememberTextPillRippleGlyphBounds(): MutableState<TextPillRippleGlyphBounds?> =
    remember { mutableStateOf(null) }

@Composable
fun rememberTextPillRippleParentPosition(): MutableState<Offset?> =
    remember { mutableStateOf(null) }

fun Modifier.textPillRippleAnchor(bounds: MutableState<TextPillRippleBounds?>): Modifier =
    onGloballyPositioned { coordinates ->
        bounds.value = TextPillRippleBounds(
            positionInParent = coordinates.positionInParent(),
            positionInRoot = coordinates.positionInRoot(),
            size = coordinates.size,
        )
    }

fun Modifier.textPillRippleParentPosition(parentPosition: MutableState<Offset?>): Modifier =
    onGloballyPositioned { coordinates ->
        parentPosition.value = coordinates.positionInRoot()
    }

@Composable
fun rememberTextPillRippleGlyphBoundsUpdater(
    glyphBounds: MutableState<TextPillRippleGlyphBounds?>,
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

    return remember(glyphBounds, text, style, density, typeface) {
        { textLayoutResult ->
            val updatedBounds = typeface?.let {
                textLayoutResult.inkGlyphBounds(text, style, density, it)
            }?.let(::TextPillRippleGlyphBounds)
            if (glyphBounds.value != updatedBounds) {
                glyphBounds.value = updatedBounds
            }
        }
    }
}

private fun TextLayoutResult.inkGlyphBounds(
    text: String,
    style: TextStyle,
    density: androidx.compose.ui.unit.Density,
    typeface: Typeface,
): Rect? {
    val horizontalBounds = nonWhitespaceCharacterHorizontalBounds(text) ?: return null
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
        left = horizontalBounds.left,
        top = firstBaseline + paintBounds.top,
        right = horizontalBounds.right,
        bottom = firstBaseline + paintBounds.bottom,
    )
}

private fun TextLayoutResult.nonWhitespaceCharacterHorizontalBounds(text: String): Rect? {
    var union: Rect? = null

    text.forEachIndexed { offset, character ->
        if (!character.isWhitespace()) {
            val characterBounds = getBoundingBox(offset)
            union = union?.let { current ->
                Rect(
                    left = minOf(current.left, characterBounds.left),
                    top = 0f,
                    right = maxOf(current.right, characterBounds.right),
                    bottom = 0f,
                )
            } ?: Rect(
                left = characterBounds.left,
                top = 0f,
                right = characterBounds.right,
                bottom = 0f,
            )
        }
    }

    return union
}

/** Draw this before the anchored text inside the same [Box]. It has no pointer input. */
@Composable
fun BoxScope.TextAnchoredPillRipple(
    bounds: TextPillRippleBounds?,
    glyphBounds: TextPillRippleGlyphBounds?,
    parentPositionInRoot: Offset?,
    interactionSource: MutableInteractionSource,
) {
    if (bounds == null || parentPositionInRoot == null) return

    with(LocalDensity.current) {
        val horizontalInset = 18.dp.roundToPx()
        val verticalInset = 6.dp.roundToPx()
        val visibleRect = glyphBounds?.rect ?: Rect(
            left = 0f,
            top = 0f,
            right = bounds.size.width.toFloat(),
            bottom = bounds.size.height.toFloat(),
        )
        val textLeftInParent = bounds.positionInRoot.x - parentPositionInRoot.x
        val textTopInParent = bounds.positionInRoot.y - parentPositionInRoot.y
        val left = floor(textLeftInParent + visibleRect.left - horizontalInset).toInt()
        val top = floor(textTopInParent + visibleRect.top - verticalInset).toInt()
        val right = ceil(textLeftInParent + visibleRect.right + horizontalInset).toInt()
        val bottom = ceil(textTopInParent + visibleRect.bottom + verticalInset).toInt()

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset {
                    IntOffset(
                        x = left,
                        y = top,
                    )
                }
                .requiredSize((right - left).toDp(), (bottom - top).toDp())
                .clip(RoundedCornerShape(percent = 50))
                .indication(
                    interactionSource = interactionSource,
                    indication = ripple(bounded = true),
                ),
        )
    }
}
