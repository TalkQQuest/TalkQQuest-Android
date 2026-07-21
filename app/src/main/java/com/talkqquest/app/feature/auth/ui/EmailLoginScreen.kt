package com.talkqquest.app.feature.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.talkqquest.app.core.designsystem.Error
import com.talkqquest.app.core.designsystem.FitDesign
import com.talkqquest.app.core.designsystem.Gray200
import com.talkqquest.app.core.designsystem.Gray300
import com.talkqquest.app.core.designsystem.Gray500
import com.talkqquest.app.core.designsystem.Gray800
import com.talkqquest.app.core.designsystem.Gray900
import com.talkqquest.app.core.designsystem.Primary600
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.White
import com.talkqquest.app.core.designsystem.component.TqButton

@Composable
fun EmailLoginScreen(
    onBack: () -> Unit = {},
    onLoginClick: (String, String) -> Unit = { _, _ -> },
    onFindIdClick: () -> Unit = {},
    onFindPasswordClick: () -> Unit = {},
    errorMessage: String? = null,
) = FitDesign {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    AuthScreenFrame(title = "로그인", onBack = onBack) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(113.dp))
            Text(
                text = "이메일로 로그인",
                style = TqType.HeadingL,
                color = Gray800,
                modifier = Modifier.padding(start = 7.dp),
            )
            Spacer(Modifier.height(26.dp))
            LoginTextField(
                value = email,
                placeholder = "이메일 주소",
                onValueChange = { email = it },
                keyboardType = KeyboardType.Email,
            )
            Spacer(Modifier.height(12.dp))
            LoginTextField(
                value = password,
                placeholder = "비밀번호(영문+숫자, 8-16자)",
                onValueChange = { password = it.take(16) },
                keyboardType = KeyboardType.Password,
                visualTransformation = PasswordVisualTransformation(),
            )
            if (errorMessage != null) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = errorMessage,
                    style = TqType.BodyS,
                    color = Error,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
            Spacer(Modifier.height(if (errorMessage == null) 49.dp else 16.dp))
            TqButton(
                text = "로그인",
                onClick = { onLoginClick(email, password) },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "아이디 찾기",
                    style = TqType.LabelL,
                    color = Gray500,
                    modifier = Modifier.clickable(onClick = onFindIdClick),
                )
                Spacer(Modifier.width(24.dp))
                Text(
                    text = "비밀번호 찾기",
                    style = TqType.LabelL,
                    color = Gray500,
                    modifier = Modifier.clickable(onClick = onFindPasswordClick),
                )
            }
        }
    }
}

@Composable
private fun LoginTextField(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = TqType.TitleL.copy(color = Gray900),
        cursorBrush = SolidColor(Primary600),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = visualTransformation,
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(61.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(White)
                    .border(width = 1.dp, color = Gray300, shape = RoundedCornerShape(6.dp))
                    .padding(horizontal = 22.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (value.isBlank()) {
                    Text(
                        text = placeholder,
                        style = TqType.TitleL,
                        color = Gray200,
                    )
                }
                innerTextField()
            }
        },
    )
}

@Preview(showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun EmailLoginEmptyPreview() {
    TalkQQuestTheme {
        EmailLoginScreen()
    }
}

@Preview(showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun EmailLoginErrorPreview() {
    TalkQQuestTheme {
        EmailLoginScreen(
            errorMessage = "이메일 또는 비밀번호를 확인해주세요.",
        )
    }
}