package com.talkqquest.app.feature.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.talkqquest.app.core.designsystem.FitDesign
import com.talkqquest.app.core.designsystem.Gray200
import com.talkqquest.app.core.designsystem.Gray300
import com.talkqquest.app.core.designsystem.Gray400
import com.talkqquest.app.core.designsystem.Gray50
import com.talkqquest.app.core.designsystem.Gray500
import com.talkqquest.app.core.designsystem.Gray700
import com.talkqquest.app.core.designsystem.Gray800
import com.talkqquest.app.core.designsystem.Primary500
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.White

private val VerificationCodeError = Color(0xFFF76161)

@Composable
fun SignupVerifyScreen(
    email: String = "Talkqquest1234@gmail.com",
    isCodeError: Boolean = false,
    onBack: () -> Unit = {},
    onVerifyCode: (String) -> Unit = {},
    onCodeChange: () -> Unit = {},
    onResendClick: () -> Unit = {},
) = FitDesign(compensateStatusBar = false) {
    var code by remember { mutableStateOf("") }
    val codeColor = when {
        code.isBlank() -> Gray300
        isCodeError -> VerificationCodeError
        else -> Gray700
    }

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
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "\uB4A4\uB85C\uAC00\uAE30",
                tint = Gray500,
                modifier = Modifier.size(width = 30.dp, height = 32.dp),
            )
        }
        Text(
            text = "\uD68C\uC6D0\uAC00\uC785",
            style = TqType.BodyM,
            color = Gray700,
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = 59.dp),
            textAlign = TextAlign.Center,
        )
        Text(
            text = "\uC774\uBA54\uC77C\uC744\n\uC785\uB825\uD574\uC8FC\uC138\uC694",
            style = TqType.HeadingL,
            color = Gray800,
            modifier = Modifier.offset(x = 23.dp, y = 122.dp),
        )
        AuthInputCard(
            label = "\uC774\uBA54\uC77C",
            value = email,
            placeholder = "",
            onValueChange = {},
            actionText = "\uC778\uC99D",
            onActionClick = { if (code.length == 6) onVerifyCode(code) },
            actionCornerRadius = 12.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .offset(y = 206.dp)
                .height(88.dp),
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF334155).copy(alpha = 0.23f)),
        )

        Box(
            modifier = Modifier
                .offset(y = 424.dp)
                .fillMaxWidth()
                .height(428.dp)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(White),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .offset(y = 17.dp)
                    .height(37.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "6\uC790\uB9AC \uC804\uC1A1\uB428",
                    style = TqType.BodyM,
                    color = Gray400,
                    modifier = Modifier.width(73.dp).height(22.dp),
                )
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(37.dp)
                        .border(width = 1.dp, color = Gray200)
                        .clickable(onClick = onResendClick),
                ) {
                    Row(
                        modifier = Modifier
                            .offset(x = 8.dp, y = (-4).dp)
                            .width(105.dp)
                            .height(44.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .width(44.dp)
                                .height(44.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(text = "1:32", style = TqType.BodyM, color = Primary500)
                        }
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(16.dp)
                                .background(Gray200),
                        )
                        Box(
                            modifier = Modifier
                                .width(44.dp)
                                .height(44.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(text = "\uC7AC\uC694\uCCAD", style = TqType.BodyM, color = Gray500)
                        }
                    }
                }
            }

            BasicTextField(
                value = code,
                onValueChange = { input ->
                    val digits = input.filter { it.isDigit() }.take(6)
                    if (digits != code) onCodeChange()
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .offset(y = 69.dp)
                    .height(40.dp),
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (code.isBlank()) {
                            Text(
                                text = "\uC778\uC99D\uBC88\uD638 \uC785\uB825",
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

@Preview(showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun SignupVerifyPreview() {
    TalkQQuestTheme {
        SignupVerifyScreen()
    }
}
