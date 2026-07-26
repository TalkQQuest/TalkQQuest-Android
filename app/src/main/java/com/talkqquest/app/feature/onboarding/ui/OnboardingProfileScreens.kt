package com.talkqquest.app.feature.onboarding.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
import com.talkqquest.app.core.designsystem.Primary100
import com.talkqquest.app.core.designsystem.Primary500
import com.talkqquest.app.core.designsystem.Primary600
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.White
import com.talkqquest.app.core.designsystem.component.TqButton

private const val MaxMultiSelectCount = 2
private const val CustomDifficultyMaxLength = 30

@Composable
fun OnboardingPersonalityScreen(
    nickname: String,
    onBack: () -> Unit = {},
    onNextClick: (String) -> Unit = {},
) = FitDesign(compensateStatusBar = false) {
    var selectedIndex by remember { mutableStateOf(0) }
    val displayNickname = nickname.ifBlank { "\uB2E4\uBBFC" }
    val personalityTypes = listOf("introvert", "extrovert", "ambivert")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50),
    ) {
        IconButton(
            onClick = onBack,
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
                style = TqType.HeadingL,
                color = Gray800,
            )
            Text(
                text = "\uBA87 \uAC00\uC9C0\uB9CC \uC54C\uB824\uC8FC\uC2DC\uBA74\n\uB9DE\uCDA4\uD615 \uB300\uD654\uB97C \uC900\uBE44\uD574\uB4DC\uB9B4\uAC8C\uC694.",
                style = TqType.BodyM,
                color = Gray500,
                modifier = Modifier.offset(y = 76.dp),
            )
        }

        Box(
            modifier = Modifier
                .offset(x = 16.dp, y = 297.dp)
                .size(width = 361.dp, height = 288.dp),
        ) {
            PersonalityOptionCardFixed(
                title = "\uC870\uC6A9\uD558\uACE0 \uC2E0\uC911\uD574\uC694",
                description = "\uB9D0\uBCF4\uB2E4 \uC0DD\uAC01\uC774 \uBA3C\uC800\uC608\uC694",
                selected = selectedIndex == 0,
                onClick = { selectedIndex = 0 },
                modifier = Modifier.offset(y = 0.dp),
            )
            PersonalityOptionCardFixed(
                title = "\uC0AC\uAD50\uC801\uC774\uACE0 \uD65C\uBC1C\uD55C \uD3B8\uC774\uC5D0\uC694",
                description = "\uB300\uD654\uC640 \uB9CC\uB0A8\uC744 \uC990\uACA8\uC694",
                selected = selectedIndex == 1,
                onClick = { selectedIndex = 1 },
                modifier = Modifier.offset(y = 100.dp),
            )
            PersonalityOptionCardFixed(
                title = "\uC0C1\uD669\uC5D0 \uB530\uB77C \uB2EC\uB77C\uC694",
                description = "\uADF8\uB54C\uADF8\uB54C \uB2E4\uB974\uAC8C \uD589\uB3D9\uD574\uC694",
                selected = selectedIndex == 2,
                onClick = { selectedIndex = 2 },
                modifier = Modifier.offset(y = 200.dp),
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
    onBack: () -> Unit = {},
    onNextClick: (List<String>) -> Unit = {},
) = FitDesign(compensateStatusBar = false) {
    var selected by remember { mutableStateOf(emptySet<String>()) }
    var customText by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50),
    ) {
        IconButton(
            onClick = onBack,
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
                style = TqType.HeadingL,
                color = Gray800,
            )
            Text(
                text = "\uCD5C\uB300 2\uAC1C\uAE4C\uC9C0 \uC120\uD0DD\uD560 \uC218 \uC788\uC5B4\uC694.",
                style = TqType.BodyM,
                color = Gray500,
                modifier = Modifier.offset(y = 76.dp),
            )
        }

        Box(
            modifier = Modifier
                .offset(x = 23.dp, y = 279.dp)
                .size(width = 322.dp, height = 144.dp),
        ) {
            OnboardingChoiceChipFixed(
                text = "\uB0AF\uAC00\uB9BC",
                selected = "\uB0AF\uAC00\uB9BC" in selected,
                onClick = { selected = selected.toggleMax("\uB0AF\uAC00\uB9BC") },
                modifier = Modifier.offset(x = 0.dp, y = 0.dp),
                width = 74,
            )
            OnboardingChoiceChipFixed(
                text = "\uC8FC\uC81C\uACE0\uBBFC",
                selected = "\uC8FC\uC81C\uACE0\uBBFC" in selected,
                onClick = { selected = selected.toggleMax("\uC8FC\uC81C\uACE0\uBBFC") },
                modifier = Modifier.offset(x = 82.dp, y = 0.dp),
                width = 88,
            )
            OnboardingChoiceChipFixed(
                text = "\uB9D0\uBB38 \uB9C9\uD798",
                selected = "\uB9D0\uBB38 \uB9C9\uD798" in selected,
                onClick = { selected = selected.toggleMax("\uB9D0\uBB38 \uB9C9\uD798") },
                modifier = Modifier.offset(x = 0.dp, y = 52.dp),
                width = 92,
            )
            OnboardingChoiceChipFixed(
                text = "\uC2DC\uC120 \uBD80\uB2F4",
                selected = "\uC2DC\uC120 \uBD80\uB2F4" in selected,
                onClick = { selected = selected.toggleMax("\uC2DC\uC120 \uBD80\uB2F4") },
                modifier = Modifier.offset(x = 100.dp, y = 52.dp),
                width = 92,
            )
            OnboardingChoiceChipFixed(
                text = "\uAE34\uC7A5\uB428",
                selected = "\uAE34\uC7A5\uB428" in selected,
                onClick = { selected = selected.toggleMax("\uAE34\uC7A5\uB428") },
                modifier = Modifier.offset(x = 200.dp, y = 52.dp),
                width = 74,
            )
            OnboardingChoiceChipFixed(
                text = "\uAC71\uC815/\uBD88\uC548",
                selected = "\uAC71\uC815/\uBD88\uC548" in selected,
                onClick = { selected = selected.toggleMax("\uAC71\uC815/\uBD88\uC548") },
                modifier = Modifier.offset(x = 0.dp, y = 104.dp),
                width = 93,
            )
            OnboardingChoiceChipFixed(
                text = "\uC0C1\uB300 \uD30C\uC545 \uC5B4\uB824\uC6C0",
                selected = "\uC0C1\uB300 \uD30C\uC545 \uC5B4\uB824\uC6C0" in selected,
                onClick = { selected = selected.toggleMax("\uC0C1\uB300 \uD30C\uC545 \uC5B4\uB824\uC6C0") },
                modifier = Modifier.offset(x = 101.dp, y = 104.dp),
                width = 137,
            )
            OnboardingChoiceChipFixed(
                text = "\uC5B4\uC0C9\uD568",
                selected = "\uC5B4\uC0C9\uD568" in selected,
                onClick = { selected = selected.toggleMax("\uC5B4\uC0C9\uD568") },
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
                style = TqType.BodyM,
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
    onBack: () -> Unit = {},
    onCompleteClick: (List<String>) -> Unit = {},
) = FitDesign(compensateStatusBar = false) {
    var selected by remember { mutableStateOf(emptySet<String>()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50),
    ) {
        IconButton(
            onClick = onBack,
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
                style = TqType.HeadingL,
                color = Gray800,
            )
            Text(
                text = "\uCD5C\uB300 2\uAC1C\uAE4C\uC9C0 \uC120\uD0DD\uD560 \uC218 \uC788\uC5B4\uC694.",
                style = TqType.BodyM,
                color = Gray500,
                modifier = Modifier.offset(y = 76.dp),
            )
        }

        Box(
            modifier = Modifier
                .offset(x = 23.dp, y = 279.dp)
                .size(width = 333.dp, height = 144.dp),
        ) {
            OnboardingChoiceChipFixed(
                text = "\uC790\uC2E0\uAC10 \uD0A4\uC6B0\uAE30",
                selected = "\uC790\uC2E0\uAC10 \uD0A4\uC6B0\uAE30" in selected,
                onClick = { selected = selected.toggleMax("\uC790\uC2E0\uAC10 \uD0A4\uC6B0\uAE30") },
                modifier = Modifier.offset(x = 0.dp, y = 0.dp),
                width = 119,
            )
            OnboardingChoiceChipFixed(
                text = "\uB9D0\uBB38 \uD2B8\uAE30",
                selected = "\uB9D0\uBB38 \uD2B8\uAE30" in selected,
                onClick = { selected = selected.toggleMax("\uB9D0\uBB38 \uD2B8\uAE30") },
                modifier = Modifier.offset(x = 127.dp, y = 0.dp),
                width = 92,
            )
            OnboardingChoiceChipFixed(
                text = "\uCE68\uBB35 \uC904\uC774\uAE30",
                selected = "\uCE68\uBB35 \uC904\uC774\uAE30" in selected,
                onClick = { selected = selected.toggleMax("\uCE68\uBB35 \uC904\uC774\uAE30") },
                modifier = Modifier.offset(x = 227.dp, y = 0.dp),
                width = 106,
            )
            OnboardingChoiceChipFixed(
                text = "\uC790\uC5F0\uC2A4\uB7FD\uAC8C \uB9D0\uD558\uAE30",
                selected = "\uC790\uC5F0\uC2A4\uB7FD\uAC8C \uB9D0\uD558\uAE30" in selected,
                onClick = { selected = selected.toggleMax("\uC790\uC5F0\uC2A4\uB7FD\uAC8C \uB9D0\uD558\uAE30") },
                modifier = Modifier.offset(x = 0.dp, y = 52.dp),
                width = 147,
            )
            OnboardingChoiceChipFixed(
                text = "\uC0C1\uD669\uC5D0 \uB9DE\uB294 \uB300\uD654",
                selected = "\uC0C1\uD669\uC5D0 \uB9DE\uB294 \uB300\uD654" in selected,
                onClick = { selected = selected.toggleMax("\uC0C1\uD669\uC5D0 \uB9DE\uB294 \uB300\uD654") },
                modifier = Modifier.offset(x = 155.dp, y = 52.dp),
                width = 137,
            )
            OnboardingChoiceChipFixed(
                text = "\uCE5C\uD574\uC9C0\uB294 \uB300\uD654",
                selected = "\uCE5C\uD574\uC9C0\uB294 \uB300\uD654" in selected,
                onClick = { selected = selected.toggleMax("\uCE5C\uD574\uC9C0\uB294 \uB300\uD654") },
                modifier = Modifier.offset(x = 0.dp, y = 104.dp),
                width = 119,
            )
            OnboardingChoiceChipFixed(
                text = "\uCCAB\uC778\uC0C1 \uAC1C\uC120\uD558\uAE30",
                selected = "\uCCAB\uC778\uC0C1 \uAC1C\uC120\uD558\uAE30" in selected,
                onClick = { selected = selected.toggleMax("\uCCAB\uC778\uC0C1 \uAC1C\uC120\uD558\uAE30") },
                modifier = Modifier.offset(x = 127.dp, y = 104.dp),
                width = 133,
            )
        }

        TqButton(
            text = "\uC644\uB8CC",
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
private fun PersonalityOptionCardFixed(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (selected) Primary600 else Gray200
    val backgroundColor = if (selected) Primary100 else Gray50

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(88.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
    ) {
        Text(
            text = title,
            style = TqType.TitleL,
            color = Gray600,
            modifier = Modifier.offset(x = 20.dp, y = 18.dp),
        )
        Text(
            text = description,
            style = TqType.BodyS,
            color = Gray500,
            modifier = Modifier.offset(x = 20.dp, y = 50.dp),
        )
        Box(
            modifier = Modifier
                .offset(x = 319.dp, y = 31.dp)
                .size(26.dp)
                .clip(CircleShape)
                .background(if (selected) Primary600 else Gray50)
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
private fun OnboardingChoiceChipFixed(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    width: Int,
) {
    Text(
        text = text,
        style = TqType.BodyL.copy(fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal),
        color = if (selected) Primary600 else Gray800,
        textAlign = TextAlign.Center,
        modifier = modifier
            .size(width = width.dp, height = 40.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) Primary100 else White)
            .border(1.dp, if (selected) Primary600 else Gray200, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    )
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
        textStyle = TqType.BodyL.copy(color = Gray800),
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
                        style = TqType.BodyL,
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
                    onClick = onBack,
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
        style = TqType.HeadingL.copy(fontWeight = FontWeight.Bold),
        color = Gray800,
    )
}

@Composable
private fun OnboardingHelperText(text: String) {
    Text(
        text = text,
        style = TqType.BodyS,
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
                style = TqType.TitleL.copy(fontWeight = FontWeight.Bold),
                color = Gray700,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = description,
                style = TqType.BodyS,
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
        style = TqType.BodyM.copy(fontWeight = FontWeight.Medium),
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
        textStyle = TqType.BodyM.copy(color = Gray800),
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
                        style = TqType.BodyM,
                        color = Gray300,
                    )
                }
                innerTextField()
            }
        },
    )
}

private fun Set<String>.toggleMax(value: String): Set<String> = when {
    value in this -> this - value
    size < MaxMultiSelectCount -> this + value
    else -> this
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






