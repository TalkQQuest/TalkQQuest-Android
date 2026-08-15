package com.talkqquest.app.feature.profile.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.talkqquest.app.core.designsystem.Error
import com.talkqquest.app.core.designsystem.FitDesign
import com.talkqquest.app.core.designsystem.Gray300
import com.talkqquest.app.core.designsystem.Gray400
import com.talkqquest.app.core.designsystem.Gray50
import com.talkqquest.app.core.designsystem.Gray500
import com.talkqquest.app.core.designsystem.Gray700
import com.talkqquest.app.core.designsystem.Gray800
import com.talkqquest.app.core.designsystem.Gray900
import com.talkqquest.app.core.designsystem.Primary600
import com.talkqquest.app.core.designsystem.Success
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.White
import com.talkqquest.app.core.designsystem.component.TqButton
import com.talkqquest.app.core.designsystem.component.PasswordVisibilityToggle

private const val ProfileCurrentPasswordSample = "Talkee3643@@"

@Composable
fun ProfileNewPasswordScreen(
    initialPassword: String = "",
    initialSamePasswordError: Boolean = false,
    onBack: () -> Unit = {},
    onConfirmClick: (String) -> Unit = {},
    showCompletionDialog: Boolean = false,
    onCompletionConfirm: () -> Unit = {},
) = FitDesign(contentAlignment = Alignment.TopCenter) {
    var password by remember { mutableStateOf(initialPassword) }
    var showPassword by remember { mutableStateOf(false) }
    var samePasswordError by remember { mutableStateOf(initialSamePasswordError) }
    val lengthSatisfied = password.length in 8..16
    val hasLetter = password.any { it.isLetter() }
    val hasDigit = password.any { it.isDigit() }
    val formatSatisfied = hasLetter && hasDigit

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50),
    ) {
        ProfileSimpleTopBar(title = "비밀번호 변경", onBack = onBack)

        Text(
            text = "새로운 비밀번호를\n설정해주세요",
            style = TqType.HeadingL,
            color = Gray800,
            modifier = Modifier
                .offset(x = 23.dp, y = 122.dp)
                .size(width = 170.dp, height = 68.dp),
        )

        Box(
            modifier = Modifier
                .offset(x = 16.dp, y = 206.dp)
                .size(width = 361.dp, height = 124.dp),
        ) {
            ProfileNewPasswordInputCard(
                value = password,
                placeholder = "비밀번호 입력",
                showPassword = showPassword,
                samePasswordError = samePasswordError,
                onTogglePassword = { showPassword = !showPassword },
                onValueChange = { input ->
                    password = input
                    if (samePasswordError) samePasswordError = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(88.dp),
            )

            if (samePasswordError) {
                ProfileNewPasswordMessage(
                    text = "현재 비밀번호와 다른 비밀번호를 입력해주세요",
                    modifier = Modifier
                        .offset(y = 100.dp)
                        .size(width = 306.dp, height = 24.dp),
                )
            } else {
                ProfilePasswordRequirement(
                    text = "8-16자",
                    satisfied = lengthSatisfied,
                    modifier = Modifier
                        .offset(y = 100.dp)
                        .size(width = 68.dp, height = 24.dp),
                )
                ProfilePasswordRequirement(
                    text = "영문, 숫자 포함",
                    satisfied = formatSatisfied,
                    modifier = Modifier
                        .offset(x = 86.dp, y = 100.dp)
                        .size(width = 110.dp, height = 24.dp),
                )
            }
        }

        TqButton(
            text = "확인",
            onClick = {
                samePasswordError = password == ProfileCurrentPasswordSample
                if (!samePasswordError && lengthSatisfied && formatSatisfied) {
                    onConfirmClick(password)
                }
            },
            modifier = Modifier
                .offset(x = 16.dp, y = 728.dp)
                .size(width = 361.dp, height = 52.dp),
        )

        if (showCompletionDialog) {
            ProfileNewPasswordCompleteDialog(onConfirmClick = onCompletionConfirm)
        }
    }
}

@Composable
private fun ProfileNewPasswordCompleteDialog(
    onConfirmClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray700.copy(alpha = 0.23f)),
    ) {
        Box(
            modifier = Modifier
                .offset(x = 28.dp, y = 313.dp)
                .size(width = 336.dp, height = 166.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(White)
                .padding(horizontal = 24.dp, vertical = 24.dp),
        ) {
            Text(
                text = "비밀번호 변경 완료",
                style = TqType.HeadingM,
                color = Gray900,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .size(width = 288.dp, height = 30.dp),
            )
            Text(
                text = "새로운 비밀번호가 저장되었습니다.",
                style = TqType.BodyL,
                color = Gray500,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .offset(y = 34.dp)
                    .align(Alignment.TopCenter)
                    .size(width = 288.dp, height = 24.dp),
            )
            Box(
                modifier = Modifier
                    .offset(y = 78.dp)
                    .align(Alignment.TopCenter)
                    .size(width = 143.dp, height = 48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Primary600)
                    .profileItemClick(onClick = onConfirmClick),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "확인",
                    style = TqType.TitleL,
                    color = Gray50,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun ProfileNewPasswordInputCard(
    value: String,
    placeholder: String,
    showPassword: Boolean,
    samePasswordError: Boolean,
    onTogglePassword: () -> Unit,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(White)
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        Text(
            text = "비밀번호",
            style = TqType.Caption,
            color = Gray500,
            modifier = Modifier.size(width = 263.dp, height = 18.dp),
        )

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TqType.TitleL.copy(
                color = when {
                    samePasswordError -> Gray300
                    value.isBlank() -> Gray300
                    else -> Gray900
                },
            ),
            cursorBrush = SolidColor(Primary600),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier
                .offset(y = 28.dp)
                .size(width = 263.dp, height = 28.dp),
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (value.isBlank()) {
                        Text(
                            text = placeholder,
                            style = TqType.TitleL,
                            color = Gray300,
                        )
                    }
                    innerTextField()
                }
            },
        )

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(44.dp),
            contentAlignment = Alignment.Center,
        ) {
            PasswordVisibilityToggle(
                passwordVisible = showPassword,
                onToggle = onTogglePassword,
            )
        }
    }
}

@Composable
private fun ProfilePasswordRequirement(
    text: String,
    satisfied: Boolean,
    modifier: Modifier = Modifier,
) {
    val stateColor = if (satisfied) Success else Error
    Box(modifier = modifier, contentAlignment = Alignment.CenterStart) {
        if (satisfied) {
            ProfileNewPasswordCheckIcon(
                color = stateColor,
                modifier = Modifier.size(16.dp),
            )
        } else {
            ProfileNewPasswordErrorIcon(
                color = stateColor,
                modifier = Modifier.size(16.dp),
            )
        }
        Text(
            text = text,
            style = TqType.LabelL,
            color = Gray500,
            modifier = Modifier
                .offset(x = 24.dp, y = 2.dp)
                .size(width = if (text.length > 5) 84.dp else 42.dp, height = 20.dp),
        )
    }
}

@Composable
private fun ProfileNewPasswordMessage(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier, contentAlignment = Alignment.CenterStart) {
        ProfileNewPasswordErrorIcon(
            color = Error,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = text,
            style = TqType.LabelL,
            color = Gray500,
            modifier = Modifier
                .offset(x = 24.dp, y = 2.dp)
                .size(width = 282.dp, height = 20.dp),
        )
    }
}

@Composable
private fun ProfileNewPasswordErrorIcon(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        drawCircle(color = color, radius = size.minDimension / 2f)
        val strokeWidth = 1.4.dp.toPx()
        val start = size.minDimension * 0.32f
        val end = size.minDimension * 0.68f
        drawLine(
            color = White,
            start = Offset(start, start),
            end = Offset(end, end),
            strokeWidth = strokeWidth,
        )
        drawLine(
            color = White,
            start = Offset(end, start),
            end = Offset(start, end),
            strokeWidth = strokeWidth,
        )
    }
}

@Composable
private fun ProfileNewPasswordCheckIcon(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        drawCircle(color = color, radius = size.minDimension / 2f)
        val strokeWidth = 1.5.dp.toPx()
        drawLine(
            color = White,
            start = Offset(size.width * 0.28f, size.height * 0.52f),
            end = Offset(size.width * 0.43f, size.height * 0.66f),
            strokeWidth = strokeWidth,
        )
        drawLine(
            color = White,
            start = Offset(size.width * 0.43f, size.height * 0.66f),
            end = Offset(size.width * 0.72f, size.height * 0.35f),
            strokeWidth = strokeWidth,
        )
    }
}

@Preview(name = "기본", showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun ProfileNewPasswordDefaultPreview() {
    TalkQQuestTheme {
        ProfileNewPasswordScreen()
    }
}

@Preview(name = "동일 비번 입력", showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun ProfileNewPasswordSamePasswordPreview() {
    TalkQQuestTheme {
        ProfileNewPasswordScreen(initialPassword = "Talkee3643@@", initialSamePasswordError = true)
    }
}

@Preview(name = "조건", showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun ProfileNewPasswordConditionPreview() {
    TalkQQuestTheme {
        ProfileNewPasswordScreen(initialPassword = "Talkee3643@@")
    }
}
