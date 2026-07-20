package com.talkqquest.app.feature.auth.ui

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.talkqquest.app.core.designsystem.FitDesign
import com.talkqquest.app.core.designsystem.Gray200
import com.talkqquest.app.core.designsystem.Gray300
import com.talkqquest.app.core.designsystem.Gray500
import com.talkqquest.app.core.designsystem.Gray800
import com.talkqquest.app.core.designsystem.Primary600
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.White

@Composable
fun SignupVerifyScreen(
    email: String = "Talkqquest1234@gmail.com",
    onBack: () -> Unit = {},
    onVerifyCode: (String) -> Unit = {},
    onResendClick: () -> Unit = {},
) {
    var code by remember { mutableStateOf("") }
    val isComplete = code.length == 6
    val codeColor = if (isComplete) Gray800 else Gray300

    Box(modifier = Modifier.fillMaxSize()) {
        FitDesign {
            AuthScreenFrame(title = "회원가입", onBack = onBack) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                ) {
                    Spacer(Modifier.height(113.dp))
                    AuthHeadline(text = "이메일을\n입력해주세요", modifier = Modifier.padding(start = 7.dp))
                    Spacer(Modifier.height(20.dp))
                    AuthInputCard(
                        label = "이메일",
                        value = email,
                        placeholder = "",
                        onValueChange = {},
                        actionText = "인증",
                        onActionClick = { if (isComplete) onVerifyCode(code) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
        Box(modifier = Modifier.fillMaxSize().background(Color(0x990F172A)))
        FitDesign {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                    .background(White)
                    .padding(start = 32.dp, end = 32.dp, top = 17.dp, bottom = 40.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = "6자리 전송됨", style = TqType.LabelM, color = Gray300)
                    Row(
                        modifier = Modifier
                            .height(36.dp)
                            .background(White)
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Text(text = "1:32", style = TqType.LabelL, color = Primary600)
                        Box(Modifier.height(18.dp).background(Gray200).padding(horizontal = 0.5.dp))
                        Text(
                            text = "재요청",
                            style = TqType.LabelL,
                            color = Gray500,
                            modifier = Modifier
                                .padding(start = 1.dp)
                                .clickable(onClick = onResendClick),
                        )
                    }
                }
                Spacer(Modifier.height(18.dp))
                BasicTextField(
                    value = code,
                    onValueChange = { input ->
                        val digits = input.filter { it.isDigit() }.take(6)
                        code = digits
                        if (digits.length == 6) onVerifyCode(digits)
                    },
                    singleLine = true,
                    textStyle = TqType.HeadingXL.copy(
                        color = codeColor,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Start,
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    decorationBox = { innerTextField ->
                        Box(modifier = Modifier.fillMaxWidth()) {
                            if (code.isBlank()) {
                                Text(
                                    text = "인증번호 입력",
                                    style = TqType.HeadingXL.copy(fontWeight = FontWeight.Bold),
                                    color = Gray300,
                                )
                            }
                            innerTextField()
                        }
                    },
                )
            }
        }
    }
}

@Preview(showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun SignupVerifyPreview() {
    TalkQQuestTheme {
        SignupVerifyScreen()
    }
}