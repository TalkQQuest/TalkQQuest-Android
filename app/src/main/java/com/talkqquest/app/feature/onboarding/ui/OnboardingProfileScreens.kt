package com.talkqquest.app.feature.onboarding.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.animateIntSizeAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.talkqquest.app.R
import com.talkqquest.app.core.designsystem.FitDesign
import com.talkqquest.app.core.designsystem.Gray100
import com.talkqquest.app.core.designsystem.Gray200
import com.talkqquest.app.core.designsystem.Gray300
import com.talkqquest.app.core.designsystem.Gray400
import com.talkqquest.app.core.designsystem.Gray50
import com.talkqquest.app.core.designsystem.Gray500
import com.talkqquest.app.core.designsystem.Gray600
import com.talkqquest.app.core.designsystem.Gray700
import com.talkqquest.app.core.designsystem.Gray800
import com.talkqquest.app.core.designsystem.Gray900
import com.talkqquest.app.core.designsystem.Primary100
import com.talkqquest.app.core.designsystem.Primary500
import com.talkqquest.app.core.designsystem.Primary600
import com.talkqquest.app.core.designsystem.Primary700
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.White
import com.talkqquest.app.core.designsystem.figma
import com.talkqquest.app.core.designsystem.component.TqButton
import com.talkqquest.app.core.designsystem.component.rememberHapticTick
import kotlin.math.roundToInt

private const val MaxMultiSelectCount = 2
private const val CustomDifficultyMaxLength = 30

private data class FixedChoiceOption(val value: String, val x: Int, val y: Int, val width: Int)

private val DifficultyChoiceOptions = listOf(
    FixedChoiceOption("낯가림", 0, 0, 74), FixedChoiceOption("주제고민", 82, 0, 88),
    FixedChoiceOption("말문 막힘", 0, 52, 92), FixedChoiceOption("시선 부담", 100, 52, 92),
    FixedChoiceOption("긴장됨", 200, 52, 74), FixedChoiceOption("걱정/불안", 0, 104, 93),
    FixedChoiceOption("상대 파악 어려움", 101, 104, 137), FixedChoiceOption("어색함", 246, 104, 74),
)
private val GoalChoiceOptions = listOf(
    FixedChoiceOption("자신감 키우기", 0, 0, 119), FixedChoiceOption("말문 트기", 127, 0, 92),
    FixedChoiceOption("침묵 줄이기", 227, 0, 106), FixedChoiceOption("자연스럽게 말하기", 0, 52, 147),
    FixedChoiceOption("상황에 맞는 대화", 155, 52, 137), FixedChoiceOption("친해지는 대화", 0, 104, 119),
    FixedChoiceOption("첫인상 개선하기", 127, 104, 133),
)

@Composable
fun OnboardingPersonalityScreen(
    nickname: String,
    initialPersonalityType: String = "introvert",
    onBack: () -> Unit = {},
    onNextClick: (String) -> Unit = {},
) = FitDesign(compensateStatusBar = false) {
    val tick = rememberHapticTick()
    val personalityTypes = listOf("introvert", "extrovert", "ambivert")
    var selectedIndex by remember(initialPersonalityType) {
        mutableStateOf(personalityTypes.indexOf(initialPersonalityType).takeIf { it >= 0 } ?: 0)
    }
    var hasMovedSelection by remember { mutableStateOf(false) }
    val displayNickname = nickname.ifBlank { "다민" }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50),
    ) {
        IconButton(
            onClick = { tick(); onBack() },
            modifier = Modifier
                .offset(x = 0.dp, y = 48.dp)
                .size(44.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.ic_back_chevron),
                contentDescription = "back",
                modifier = Modifier.size(width = 30.dp, height = 32.dp),
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .offset(x = 101.dp, y = 70.dp)
                .size(width = 192.dp, height = 6.dp),
        ) {
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .size(width = 60.dp, height = 6.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (index == 0) Primary600 else Gray300),
                )
            }
        }

        OnboardingChatIconFixed(
            modifier = Modifier.offset(x = 23.dp, y = 107.dp),
        )

        Box(
            modifier = Modifier
                .offset(x = 23.dp, y = 157.dp)
                .size(width = 230.dp, height = 120.dp),
        ) {
            Text(
                text = "\uD3C9\uC18C \uB300\uD654\uD560 \uB54C\n${displayNickname}\uB2D8\uC758 \uBAA8\uC2B5\uC740",
                style = TqType.HeadingL.figma(),
                color = Gray800,
            )
            Text(
                text = "\uBA87 \uAC00\uC9C0\uB9CC \uC54C\uB824\uC8FC\uC2DC\uBA74\n\uB9DE\uCDA4\uD615 \uB300\uD654\uB97C \uC900\uBE44\uD574\uB4DC\uB9B4\uAC8C\uC694.",
                style = TqType.BodyM.figma(),
                color = Gray500,
                modifier = Modifier.offset(y = 76.dp),
            )
        }

        Box(
            modifier = Modifier
                .offset(x = 16.dp, y = 297.dp)
                .size(width = 361.dp, height = 288.dp),
        ) {
            var bounds by remember { mutableStateOf<Map<Int, Pair<IntOffset, IntSize>>>(emptyMap()) }
            PersonalitySelectionEffects(
                selectedIndex = selectedIndex,
                bounds = bounds,
                hasMovedSelection = hasMovedSelection,
            )
            PersonalityOptionCardFixed(
                title = "\uC870\uC6A9\uD558\uACE0 \uC2E0\uC911\uD574\uC694",
                description = "\uB9D0\uBCF4\uB2E4 \uC0DD\uAC01\uC774 \uBA3C\uC800\uC608\uC694",
                selected = selectedIndex == 0,
                onClick = {
                    if (selectedIndex != 0) {
                        hasMovedSelection = true
                        selectedIndex = 0
                        tick()
                    }
                },
                modifier = Modifier
                    .offset(y = 0.dp)
                    .onGloballyPositioned { coordinates ->
                        val position = coordinates.positionInParent()
                        bounds = bounds + (0 to (
                            IntOffset(position.x.roundToInt(), position.y.roundToInt()) to coordinates.size
                        ))
                    },
            )
            PersonalityOptionCardFixed(
                title = "\uC0AC\uAD50\uC801\uC774\uACE0 \uD65C\uBC1C\uD55C \uD3B8\uC774\uC5D0\uC694",
                description = "\uB300\uD654\uC640 \uB9CC\uB0A8\uC744 \uC990\uACA8\uC694",
                selected = selectedIndex == 1,
                onClick = {
                    if (selectedIndex != 1) {
                        hasMovedSelection = true
                        selectedIndex = 1
                        tick()
                    }
                },
                modifier = Modifier
                    .offset(y = 100.dp)
                    .onGloballyPositioned { coordinates ->
                        val position = coordinates.positionInParent()
                        bounds = bounds + (1 to (
                            IntOffset(position.x.roundToInt(), position.y.roundToInt()) to coordinates.size
                        ))
                    },
            )
            PersonalityOptionCardFixed(
                title = "\uC0C1\uD669\uC5D0 \uB530\uB77C \uB2EC\uB77C\uC694",
                description = "\uADF8\uB54C\uADF8\uB54C \uB2E4\uB974\uAC8C \uD589\uB3D9\uD574\uC694",
                selected = selectedIndex == 2,
                onClick = {
                    if (selectedIndex != 2) {
                        hasMovedSelection = true
                        selectedIndex = 2
                        tick()
                    }
                },
                modifier = Modifier
                    .offset(y = 200.dp)
                    .onGloballyPositioned { coordinates ->
                        val position = coordinates.positionInParent()
                        bounds = bounds + (2 to (
                            IntOffset(position.x.roundToInt(), position.y.roundToInt()) to coordinates.size
                        ))
                    },
            )
        }

        TqButton(
            text = "\uB2E4\uC74C",
            onClick = { onNextClick(personalityTypes[selectedIndex]) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .offset(y = 728.dp),
        )
    }
}
@Composable
fun OnboardingDifficultyScreen(
    initialSelected: List<String> = emptyList(),
    onBack: () -> Unit = {},
    onNextClick: (List<String>) -> Unit = {},
) = FitDesign(compensateStatusBar = false) {
    val tick = rememberHapticTick()
    // List preserves the selection order so a third tap can replace the oldest choice.
    var selected by remember(initialSelected) { mutableStateOf(initialSelected.distinct().take(MaxMultiSelectCount)) }
    var selectionSlots by remember(initialSelected) { mutableStateOf(initialSelected.distinct().take(MaxMultiSelectCount).padChoiceSlots()) }
    LaunchedEffect(selected) { selectionSlots = selectionSlots.retainChoiceSlots(selected) }
    var customText by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50),
    ) {
        IconButton(
            onClick = { tick(); onBack() },
            modifier = Modifier
                .offset(x = 0.dp, y = 48.dp)
                .size(44.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.ic_back_chevron),
                contentDescription = "back",
                modifier = Modifier.size(width = 30.dp, height = 32.dp),
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .offset(x = 101.dp, y = 70.dp)
                .size(width = 192.dp, height = 6.dp),
        ) {
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .size(width = 60.dp, height = 6.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (index < 2) Primary600 else Gray300),
                )
            }
        }

        OnboardingChatIconFixed(
            modifier = Modifier.offset(x = 23.dp, y = 107.dp),
        )

        Box(
            modifier = Modifier
                .offset(x = 23.dp, y = 157.dp)
                .size(width = 188.dp, height = 98.dp),
        ) {
            Text(
                text = "\uB300\uD654\uC5D0\uC11C \uAC00\uC7A5\n\uC5B4\uB824\uC6B4 \uC810\uC774 \uBB58\uAC00\uC694?",
                style = TqType.HeadingL.figma(),
                color = Gray800,
            )
            Text(
                text = "\uCD5C\uB300 2\uAC1C\uAE4C\uC9C0 \uC120\uD0DD\uD560 \uC218 \uC788\uC5B4\uC694.",
                style = TqType.BodyM.figma(),
                color = Gray500,
                modifier = Modifier.offset(y = 76.dp),
            )
        }

        Box(
            modifier = Modifier
                .offset(x = 23.dp, y = 279.dp)
                .size(width = 322.dp, height = 144.dp),
        ) {
            FixedChoiceSelectionEffects(DifficultyChoiceOptions, selectionSlots)
            OnboardingChoiceChipFixed(
                text = "\uB0AF\uAC00\uB9BC",
                selected = "\uB0AF\uAC00\uB9BC" in selected,
                onClick = { selected = selected.toggleMax("\uB0AF\uAC00\uB9BC", tick) },
                modifier = Modifier.offset(x = 0.dp, y = 0.dp),
                width = 74,
            )
            OnboardingChoiceChipFixed(
                text = "\uC8FC\uC81C\uACE0\uBBFC",
                selected = "\uC8FC\uC81C\uACE0\uBBFC" in selected,
                onClick = { selected = selected.toggleMax("\uC8FC\uC81C\uACE0\uBBFC", tick) },
                modifier = Modifier.offset(x = 82.dp, y = 0.dp),
                width = 88,
            )
            OnboardingChoiceChipFixed(
                text = "\uB9D0\uBB38 \uB9C9\uD798",
                selected = "\uB9D0\uBB38 \uB9C9\uD798" in selected,
                onClick = { selected = selected.toggleMax("\uB9D0\uBB38 \uB9C9\uD798", tick) },
                modifier = Modifier.offset(x = 0.dp, y = 52.dp),
                width = 92,
            )
            OnboardingChoiceChipFixed(
                text = "\uC2DC\uC120 \uBD80\uB2F4",
                selected = "\uC2DC\uC120 \uBD80\uB2F4" in selected,
                onClick = { selected = selected.toggleMax("\uC2DC\uC120 \uBD80\uB2F4", tick) },
                modifier = Modifier.offset(x = 100.dp, y = 52.dp),
                width = 92,
            )
            OnboardingChoiceChipFixed(
                text = "\uAE34\uC7A5\uB428",
                selected = "\uAE34\uC7A5\uB428" in selected,
                onClick = { selected = selected.toggleMax("\uAE34\uC7A5\uB428", tick) },
                modifier = Modifier.offset(x = 200.dp, y = 52.dp),
                width = 74,
            )
            OnboardingChoiceChipFixed(
                text = "\uAC71\uC815/\uBD88\uC548",
                selected = "\uAC71\uC815/\uBD88\uC548" in selected,
                onClick = { selected = selected.toggleMax("\uAC71\uC815/\uBD88\uC548", tick) },
                modifier = Modifier.offset(x = 0.dp, y = 104.dp),
                width = 93,
            )
            OnboardingChoiceChipFixed(
                text = "\uC0C1\uB300 \uD30C\uC545 \uC5B4\uB824\uC6C0",
                selected = "\uC0C1\uB300 \uD30C\uC545 \uC5B4\uB824\uC6C0" in selected,
                onClick = { selected = selected.toggleMax("\uC0C1\uB300 \uD30C\uC545 \uC5B4\uB824\uC6C0", tick) },
                modifier = Modifier.offset(x = 101.dp, y = 104.dp),
                width = 137,
            )
            OnboardingChoiceChipFixed(
                text = "\uC5B4\uC0C9\uD568",
                selected = "\uC5B4\uC0C9\uD568" in selected,
                onClick = { selected = selected.toggleMax("\uC5B4\uC0C9\uD568", tick) },
                modifier = Modifier.offset(x = 246.dp, y = 104.dp),
                width = 74,
            )
        }

        Box(
            modifier = Modifier
                .offset(x = 16.dp, y = 452.dp)
                .size(width = 361.dp, height = 86.dp),
        ) {
            Text(
                text = "\uC9C1\uC811 \uC785\uB825\uD574\uC8FC\uC2DC\uBA74 \uB354 \uC54C\uB9DE\uC740 \uC5F0\uC2B5\uC744 \uC900\uBE44\uD574\uB4DC\uB9B4\uAC8C\uC694. (\uC120\uD0DD)",
                style = TqType.BodyM.figma(),
                color = Gray500,
                modifier = Modifier.offset(x = 7.dp, y = 0.dp),
            )
            OnboardingOptionalInputFixed(
                value = customText,
                placeholder = "\uC608) \uB300\uD654\uC5D0 \uB07C\uB294 \uAC83\uC774 \uD798\uB4E4\uC5B4\uC694",
                onValueChange = { customText = it.take(CustomDifficultyMaxLength) },
                modifier = Modifier.offset(y = 34.dp),
            )
        }

        TqButton(
            text = "\uB2E4\uC74C",
            onClick = {
                val values = (selected.toList() + customText.trim())
                    .filter { it.isNotBlank() }
                    .distinct()
                    .take(MaxMultiSelectCount)
                onNextClick(values)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .offset(y = 728.dp),
        )
    }
}
@Composable
fun OnboardingGoalScreen(
    initialSelected: List<String> = emptyList(),
    completeButtonText: String = "완료",
    onBack: () -> Unit = {},
    onCompleteClick: (List<String>) -> Unit = {},
) = FitDesign(compensateStatusBar = false) {
    val tick = rememberHapticTick()
    // List preserves the selection order so a third tap can replace the oldest choice.
    var selected by remember(initialSelected) { mutableStateOf(initialSelected.distinct().take(MaxMultiSelectCount)) }
    var selectionSlots by remember(initialSelected) { mutableStateOf(initialSelected.distinct().take(MaxMultiSelectCount).padChoiceSlots()) }
    LaunchedEffect(selected) { selectionSlots = selectionSlots.retainChoiceSlots(selected) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50),
    ) {
        IconButton(
            onClick = { tick(); onBack() },
            modifier = Modifier
                .offset(x = 0.dp, y = 48.dp)
                .size(44.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.ic_back_chevron),
                contentDescription = "back",
                modifier = Modifier.size(width = 30.dp, height = 32.dp),
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .offset(x = 101.dp, y = 70.dp)
                .size(width = 192.dp, height = 6.dp),
        ) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .size(width = 60.dp, height = 6.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Primary600),
                )
            }
        }

        OnboardingChatIconFixed(
            modifier = Modifier.offset(x = 23.dp, y = 107.dp),
        )

        Box(
            modifier = Modifier
                .offset(x = 23.dp, y = 157.dp)
                .size(width = 203.dp, height = 98.dp),
        ) {
            Text(
                text = "\uC5B4\uB5A4 \uB300\uD654\uB97C\n\uC5F0\uC2B5\uD558\uACE0 \uC2F6\uC73C\uC2E0\uAC00\uC694?",
                style = TqType.HeadingL.figma(),
                color = Gray800,
            )
            Text(
                text = "\uCD5C\uB300 2\uAC1C\uAE4C\uC9C0 \uC120\uD0DD\uD560 \uC218 \uC788\uC5B4\uC694.",
                style = TqType.BodyM.figma(),
                color = Gray500,
                modifier = Modifier.offset(y = 76.dp),
            )
        }

        Box(
            modifier = Modifier
                .offset(x = 23.dp, y = 279.dp)
                .size(width = 333.dp, height = 144.dp),
        ) {
            FixedChoiceSelectionEffects(GoalChoiceOptions, selectionSlots)
            OnboardingChoiceChipFixed(
                text = "\uC790\uC2E0\uAC10 \uD0A4\uC6B0\uAE30",
                selected = "\uC790\uC2E0\uAC10 \uD0A4\uC6B0\uAE30" in selected,
                onClick = { selected = selected.toggleMax("\uC790\uC2E0\uAC10 \uD0A4\uC6B0\uAE30", tick) },
                modifier = Modifier.offset(x = 0.dp, y = 0.dp),
                width = 119,
            )
            OnboardingChoiceChipFixed(
                text = "\uB9D0\uBB38 \uD2B8\uAE30",
                selected = "\uB9D0\uBB38 \uD2B8\uAE30" in selected,
                onClick = { selected = selected.toggleMax("\uB9D0\uBB38 \uD2B8\uAE30", tick) },
                modifier = Modifier.offset(x = 127.dp, y = 0.dp),
                width = 92,
            )
            OnboardingChoiceChipFixed(
                text = "\uCE68\uBB35 \uC904\uC774\uAE30",
                selected = "\uCE68\uBB35 \uC904\uC774\uAE30" in selected,
                onClick = { selected = selected.toggleMax("\uCE68\uBB35 \uC904\uC774\uAE30", tick) },
                modifier = Modifier.offset(x = 227.dp, y = 0.dp),
                width = 106,
            )
            OnboardingChoiceChipFixed(
                text = "\uC790\uC5F0\uC2A4\uB7FD\uAC8C \uB9D0\uD558\uAE30",
                selected = "\uC790\uC5F0\uC2A4\uB7FD\uAC8C \uB9D0\uD558\uAE30" in selected,
                onClick = { selected = selected.toggleMax("\uC790\uC5F0\uC2A4\uB7FD\uAC8C \uB9D0\uD558\uAE30", tick) },
                modifier = Modifier.offset(x = 0.dp, y = 52.dp),
                width = 147,
            )
            OnboardingChoiceChipFixed(
                text = "\uC0C1\uD669\uC5D0 \uB9DE\uB294 \uB300\uD654",
                selected = "\uC0C1\uD669\uC5D0 \uB9DE\uB294 \uB300\uD654" in selected,
                onClick = { selected = selected.toggleMax("\uC0C1\uD669\uC5D0 \uB9DE\uB294 \uB300\uD654", tick) },
                modifier = Modifier.offset(x = 155.dp, y = 52.dp),
                width = 137,
            )
            OnboardingChoiceChipFixed(
                text = "\uCE5C\uD574\uC9C0\uB294 \uB300\uD654",
                selected = "\uCE5C\uD574\uC9C0\uB294 \uB300\uD654" in selected,
                onClick = { selected = selected.toggleMax("\uCE5C\uD574\uC9C0\uB294 \uB300\uD654", tick) },
                modifier = Modifier.offset(x = 0.dp, y = 104.dp),
                width = 119,
            )
            OnboardingChoiceChipFixed(
                text = "\uCCAB\uC778\uC0C1 \uAC1C\uC120\uD558\uAE30",
                selected = "\uCCAB\uC778\uC0C1 \uAC1C\uC120\uD558\uAE30" in selected,
                onClick = { selected = selected.toggleMax("\uCCAB\uC778\uC0C1 \uAC1C\uC120\uD558\uAE30", tick) },
                modifier = Modifier.offset(x = 127.dp, y = 104.dp),
                width = 133,
            )
        }

        TqButton(
            text = completeButtonText,
            onClick = { onCompleteClick(selected.toList()) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .offset(y = 728.dp),
        )
    }
}
@Composable
private fun OnboardingChatIconFixed(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.size(width = 30.dp, height = 38.dp),
    ) {
        Box(
            modifier = Modifier
                .offset(x = 2.dp, y = 30.dp)
                .size(width = 26.dp, height = 8.dp)
                .clip(CircleShape)
                .background(Primary500.copy(alpha = 0.04f)),
        )
        Image(
            painter = painterResource(R.drawable.ic_onboarding_chat_bubble),
            contentDescription = null,
            modifier = Modifier.size(30.dp),
        )
    }
}

@Composable
private fun PersonalitySelectionEffects(
    selectedIndex: Int,
    bounds: Map<Int, Pair<IntOffset, IntSize>>,
    hasMovedSelection: Boolean,
) {
    val density = LocalDensity.current
    val selectedBounds = bounds[selectedIndex]
    val selectedOffset by animateIntOffsetAsState(
        targetValue = selectedBounds?.first ?: IntOffset.Zero,
        animationSpec = if (hasMovedSelection) tween(240, easing = FastOutSlowInEasing) else snap(),
        label = "personalitySelectionOffset",
    )
    val selectedSize by animateIntSizeAsState(
        targetValue = selectedBounds?.second ?: IntSize.Zero,
        animationSpec = if (hasMovedSelection) tween(240, easing = FastOutSlowInEasing) else snap(),
        label = "personalitySelectionSize",
    )
    bounds.values.forEach { (offset, size) ->
        Box(
            Modifier
                .offset { offset }
                .size(with(density) { size.width.toDp() }, with(density) { size.height.toDp() })
                .clip(RoundedCornerShape(16.dp))
                .background(Gray50)
                .border(2.dp, Gray200, RoundedCornerShape(16.dp)),
        )
    }
    if (selectedSize != IntSize.Zero) {
        Box(
            Modifier
                .offset { selectedOffset }
                .size(with(density) { selectedSize.width.toDp() }, with(density) { selectedSize.height.toDp() })
                .clip(RoundedCornerShape(16.dp))
                .background(Primary100)
                .border(2.dp, Primary600, RoundedCornerShape(16.dp)),
        )
    }
}

@Composable
private fun FixedChoiceSelectionEffects(
    options: List<FixedChoiceOption>,
    selectionSlots: List<String?>,
) {
    val density = androidx.compose.ui.platform.LocalDensity.current
    options.forEach { option ->
        Box(
            Modifier
                .offset(option.x.dp, option.y.dp)
                .size(option.width.dp, 40.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(White)
                .border(1.dp, Gray200, RoundedCornerShape(20.dp)),
        )
    }
    selectionSlots.forEachIndexed { slot, value ->
        val option = options.firstOrNull { it.value == value }
        var displayedOption by remember(slot) { mutableStateOf(option) }
        var isVisible by remember(slot) { mutableStateOf(option != null) }
        var animateMovement by remember(slot) { mutableStateOf(false) }

        LaunchedEffect(option) {
            if (option == null) {
                animateMovement = false
                isVisible = false
                delay(220)
                displayedOption = null
            } else {
                // A newly occupied slot appears in place; only an existing selection moves.
                animateMovement = displayedOption != null && isVisible
                displayedOption = option
                isVisible = true
            }
        }

        val targetOffset = displayedOption?.let {
            with(density) { IntOffset(it.x.dp.roundToPx(), it.y.dp.roundToPx()) }
        } ?: IntOffset.Zero
        val targetSize = displayedOption?.let {
            with(density) { IntSize(it.width.dp.roundToPx(), 40.dp.roundToPx()) }
        } ?: IntSize.Zero
        val animatedOffset by animateIntOffsetAsState(
            targetValue = targetOffset,
            animationSpec = if (animateMovement) tween(240, easing = FastOutSlowInEasing) else snap(),
            label = "onboardingChoiceOffset$slot",
        )
        val animatedSize by animateIntSizeAsState(
            targetValue = targetSize,
            animationSpec = if (animateMovement) tween(240, easing = FastOutSlowInEasing) else snap(),
            label = "onboardingChoiceSize$slot",
        )
        val alpha by animateFloatAsState(
            targetValue = if (isVisible) 1f else 0f,
            animationSpec = tween(220, easing = FastOutSlowInEasing),
            label = "onboardingChoiceAlpha$slot",
        )
        if (animatedSize != IntSize.Zero) {
            Box(
                Modifier
                    .offset { animatedOffset }
                    .size(with(density) { animatedSize.width.toDp() }, with(density) { animatedSize.height.toDp() })
                    .graphicsLayer { this.alpha = alpha }
                    .clip(RoundedCornerShape(20.dp))
                    .background(Primary100)
                    .border(1.dp, Primary600, RoundedCornerShape(20.dp)),
            )
        }
    }
}

@Composable
private fun PersonalityOptionCardFixed(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val titleColor by animateColorAsState(
        targetValue = if (selected) Primary700 else Gray700,
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "personalityOptionTitleColor",
    )
    val descriptionColor by animateColorAsState(
        targetValue = if (selected) Gray700 else Gray600,
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "personalityOptionDescriptionColor",
    )
    val checkCircleColor by animateColorAsState(
        targetValue = if (selected) Primary600 else Gray50,
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "personalityOptionCheckCircleColor",
    )
    val checkBorderColor by animateColorAsState(
        targetValue = if (selected) Primary600 else Gray300,
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "personalityOptionCheckBorderColor",
    )
    val checkAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "personalityOptionCheckAlpha",
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(88.dp)
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
    ) {
        Text(
            text = title,
            style = TqType.TitleL.figma(),
            color = titleColor,
            modifier = Modifier.offset(x = 20.dp, y = 18.dp),
        )
        Text(
            text = description,
            style = TqType.BodyS.figma(),
            color = descriptionColor,
            modifier = Modifier.offset(x = 20.dp, y = 50.dp),
        )
        Box(
            modifier = Modifier
                .offset(x = 319.dp, y = 31.dp)
                .size(26.dp)
                .clip(CircleShape)
                .background(checkCircleColor)
                .border(2.dp, checkBorderColor, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = White,
                modifier = Modifier
                    .size(18.dp)
                    .graphicsLayer { alpha = checkAlpha },
            )
        }
    }
}

@Composable
private fun OnboardingChoiceChipFixed(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    width: Int,
) {
    val textColor by animateColorAsState(
        targetValue = if (selected) Primary700 else Gray900,
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "onboardingChoiceTextColor",
    )
    Box(
        modifier = modifier
            .size(width = width.dp, height = 40.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = TqType.BodyL.figma().copy(fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal),
            color = textColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
    }
}

@Composable
private fun OnboardingOptionalInputFixed(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = TqType.BodyL.figma().copy(color = Gray800),
        cursorBrush = SolidColor(Primary600),
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(White)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.CenterStart) {
                if (value.isBlank()) {
                    Text(
                        text = placeholder,
                        style = TqType.BodyL.figma(),
                        color = Gray400,
                    )
                }
                innerTextField()
            }
        },
    )
}
@Composable
private fun OnboardingStepFrame(
    step: Int,
    onBack: () -> Unit,
    bottomButtonText: String,
    onBottomButtonClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val tick = rememberHapticTick()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50)
            .statusBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(28.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = { tick(); onBack() },
                    modifier = Modifier.size(32.dp),
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_back_chevron),
                        contentDescription = "back",
                        modifier = Modifier.size(24.dp),
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    OnboardingProgress(step = step)
                }
            }
            Spacer(Modifier.height(31.dp))
            content()
            Spacer(Modifier.weight(1f))
            TqButton(
                text = bottomButtonText,
                onClick = onBottomButtonClick,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(68.dp))
        }
    }
}

@Composable
private fun OnboardingProgress(step: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(3) { index ->
            Box(
                modifier = Modifier
                    .size(width = 60.dp, height = 5.dp)
                    .clip(RoundedCornerShape(50))
                    .background(if (index < step) Primary600 else Gray300),
            )
        }
    }
}

@Composable
private fun OnboardingChatIcon() {
    Box(modifier = Modifier.size(width = 40.dp, height = 34.dp)) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 4.dp)
                .size(width = 24.dp, height = 8.dp)
                .clip(CircleShape)
                .background(Gray200.copy(alpha = 0.7f)),
        )
        Image(
            painter = painterResource(R.drawable.ic_onboarding_chat_bubble),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(30.dp),
        )
    }
}

@Composable
private fun OnboardingHeadline(text: String) {
    Text(
        text = text,
        style = TqType.HeadingL.figma().copy(fontWeight = FontWeight.Bold),
        color = Gray800,
    )
}

@Composable
private fun OnboardingHelperText(text: String) {
    Text(
        text = text,
        style = TqType.BodyS.figma(),
        color = Gray500,
    )
}

@Composable
private fun PersonalityOptionCard(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = if (selected) Primary600 else Gray200
    val backgroundColor = if (selected) Primary100 else Gray50

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(86.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .border(1.4.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = TqType.TitleL.figma().copy(fontWeight = FontWeight.Bold),
                color = Gray700,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = description,
                style = TqType.BodyS.figma(),
                color = Gray500,
            )
        }
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(if (selected) Primary600 else White)
                .border(2.dp, if (selected) Primary600 else Gray300, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = White,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun OnboardingChipGroup(
    options: List<String>,
    selected: Set<String>,
    onToggle: (String) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        options.forEach { option ->
            OnboardingChoiceChip(
                text = option,
                selected = option in selected,
                onClick = { onToggle(option) },
            )
        }
    }
}

@Composable
private fun OnboardingChoiceChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Text(
        text = text,
        style = TqType.BodyM.figma().copy(fontWeight = FontWeight.Medium),
        color = if (selected) Primary600 else Gray700,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .height(39.dp)
            .clip(RoundedCornerShape(50))
            .background(if (selected) Primary100 else Gray50)
            .border(1.dp, if (selected) Primary600 else Gray200, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 17.dp, vertical = 8.dp),
    )
}

@Composable
private fun OnboardingOptionalInput(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = TqType.BodyM.figma().copy(color = Gray800),
        cursorBrush = SolidColor(Primary600),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(White)
            .padding(horizontal = 14.dp, vertical = 15.dp),
        decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.CenterStart) {
                if (value.isBlank()) {
                    Text(
                        text = placeholder,
                        style = TqType.BodyM.figma(),
                        color = Gray300,
                    )
                }
                innerTextField()
            }
        },
    )
}

private fun List<String>.toggleMax(value: String, onChanged: () -> Unit): List<String> = when {
    value in this -> (this - value).also { onChanged() }
    size < MaxMultiSelectCount -> (this + value).also { onChanged() }
    else -> (drop(1) + value).also { onChanged() }
}

private fun List<String>.padChoiceSlots(): List<String?> = take(MaxMultiSelectCount) +
    List(MaxMultiSelectCount - size.coerceAtMost(MaxMultiSelectCount)) { null }

private fun List<String?>.retainChoiceSlots(selected: List<String>): List<String?> {
    val retained = map { value -> value?.takeIf { it in selected } }.toMutableList()
    selected.filterNot { it in retained }.forEach { added ->
        retained[retained.indexOfFirst { it == null }] = added
    }
    return retained
}

@Preview(showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun OnboardingPersonalityPreview() {
    TalkQQuestTheme {
        OnboardingPersonalityScreen(nickname = "\uB2E4\uBBFC")
    }
}

@Preview(showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun OnboardingDifficultyPreview() {
    TalkQQuestTheme {
        OnboardingDifficultyScreen()
    }
}

@Preview(showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun OnboardingGoalPreview() {
    TalkQQuestTheme {
        OnboardingGoalScreen()
    }
}
