package com.talkqquest.app.feature.profile.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.White
import com.talkqquest.app.core.designsystem.component.TqButton


@Composable
fun ProfilePasswordChangeScreen(
    initialPassword: String = "",
    initialError: Boolean = false,
    currentPasswordError: Boolean = initialError,
    onBack: () -> Unit = {},
    onNextClick: (String) -> Unit = {},
) = FitDesign(contentAlignment = Alignment.TopCenter) {
    var password by remember { mutableStateOf(initialPassword) }
    var showPassword by remember { mutableStateOf(true) }
    var isError by remember { mutableStateOf(initialError) }

    LaunchedEffect(currentPasswordError) {
        if (currentPasswordError) isError = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50),
    ) {
        ProfileSimpleTopBar(title = "비밀번호 변경", onBack = onBack)

        Text(
            text = "현재 비밀번호를\n입력해주세요",
            style = TqType.HeadingL,
            color = Gray800,
            modifier = Modifier
                .offset(x = 23.dp, y = 122.dp)
                .size(width = 150.dp, height = 68.dp),
        )

        Box(
            modifier = Modifier
                .offset(x = 16.dp, y = 206.dp)
                .size(width = 361.dp, height = if (isError) 124.dp else 88.dp),
        ) {
            ProfilePasswordInputCard(
                value = password,
                placeholder = "비밀번호 입력",
                isError = isError,
                showPassword = showPassword,
                onTogglePassword = { showPassword = !showPassword },
                onValueChange = { input ->
                    password = input
                    if (isError) isError = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(88.dp),
            )

            if (isError) {
                Box(
                    modifier = Modifier
                        .offset(y = 100.dp)
                        .size(width = 242.dp, height = 24.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    ProfilePasswordErrorIcon(
                        modifier = Modifier
                            .offset(x = 0.dp)
                            .size(16.dp),
                    )
                    Text(
                        text = "현재 비밀번호와 일치하지 않습니다",
                        style = TqType.LabelL,
                        color = Gray500,
                        modifier = Modifier
                            .offset(x = 24.dp, y = 2.dp)
                            .size(width = 218.dp, height = 20.dp),
                    )
                }
            }
        }

        TqButton(
            text = "다음",
            onClick = {
                if (password.isBlank()) {
                    isError = true
                } else {
                    onNextClick(password)
                }
            },
            modifier = Modifier
                .offset(x = 16.dp, y = 728.dp)
                .size(width = 361.dp, height = 52.dp),
        )
    }
}

@Composable
private fun ProfilePasswordInputCard(
    value: String,
    placeholder: String,
    isError: Boolean,
    showPassword: Boolean,
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
                    isError -> Error
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
                .size(44.dp)
                .clickable(onClick = onTogglePassword),
            contentAlignment = Alignment.Center,
        ) {
            ProfilePasswordEyeIcon(
                tint = if (isError) Gray700 else Gray400,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
private fun ProfilePasswordEyeIcon(
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 1.6.dp.toPx()
        drawOval(
            color = tint,
            topLeft = Offset(size.width * 0.14f, size.height * 0.31f),
            size = androidx.compose.ui.geometry.Size(size.width * 0.72f, size.height * 0.38f),
            style = Stroke(width = strokeWidth),
        )
        drawCircle(
            color = tint,
            radius = size.minDimension * 0.12f,
            center = Offset(size.width / 2f, size.height / 2f),
            style = Stroke(width = strokeWidth),
        )
    }
}

@Composable
private fun ProfilePasswordErrorIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        drawCircle(color = Error, radius = size.minDimension / 2f)
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

@Preview(name = "기본", showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun ProfilePasswordChangeDefaultPreview() {
    TalkQQuestTheme {
        ProfilePasswordChangeScreen()
    }
}

@Preview(name = "현재 비번 입력", showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun ProfilePasswordChangeInputPreview() {
    TalkQQuestTheme {
        ProfilePasswordChangeScreen(initialPassword = "CurrentPass1234!")
    }
}

@Preview(name = "현재 비번 불일치", showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun ProfilePasswordChangeErrorPreview() {
    TalkQQuestTheme {
        ProfilePasswordChangeScreen(initialPassword = "talkqquest361", initialError = true)
    }
}