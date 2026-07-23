package com.talkqquest.app.feature.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.talkqquest.app.core.designsystem.FitDesign
import com.talkqquest.app.core.designsystem.Gray50
import com.talkqquest.app.core.designsystem.Gray500
import com.talkqquest.app.core.designsystem.Gray700
import com.talkqquest.app.core.designsystem.Gray800
import com.talkqquest.app.core.designsystem.Primary600
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.TqType

@Composable
fun SignupPasswordScreen(
    onBack: () -> Unit = {},
    onNextClick: (String) -> Unit = {},
) = FitDesign(compensateStatusBar = false) {
    var password by remember { mutableStateOf("") }
    val hasValidLength = password.length in 8..16
    val hasLetterAndDigit = password.any { it.isLetter() } && password.any { it.isDigit() }
    val canContinue = hasValidLength && hasLetterAndDigit

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50),
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .offset(x = 0.dp, y = 49.dp)
                .size(44.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "뒤로가기",
                tint = Gray500,
                modifier = Modifier.size(width = 30.dp, height = 32.dp),
            )
        }
        Text(
            text = "회원가입",
            style = TqType.BodyM,
            color = Gray700,
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = 59.dp),
            textAlign = TextAlign.Center,
        )
        Text(
            text = "비밀번호를\n설정해주세요",
            style = TqType.HeadingL,
            color = Gray800,
            modifier = Modifier.offset(x = 23.dp, y = 122.dp),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .offset(y = 206.dp)
                .height(124.dp),
        ) {
            AuthInputCard(
                label = "비밀번호",
                value = password,
                placeholder = "비밀번호 입력",
                onValueChange = { password = it.take(16) },
                keyboardType = KeyboardType.Password,
                trailing = {
                    Box(
                        modifier = Modifier
                            .width(44.dp)
                            .height(44.dp),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        EyeOffIcon(modifier = Modifier.size(24.dp))
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(88.dp),
            )
            Row(
                modifier = Modifier
                    .offset(x = 12.dp, y = 100.dp)
                    .width(218.dp)
                    .height(24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RequirementItem(text = "8-16자", satisfied = hasValidLength)
                RequirementItem(text = "영문, 숫자 포함", satisfied = hasLetterAndDigit)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .offset(y = 728.dp)
                .height(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Primary600)
                .clickable { if (canContinue) onNextClick(password) },
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "다음", style = TqType.BodyL, color = Gray50)
        }
    }
}

@Preview(showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun SignupPasswordPreview() {
    TalkQQuestTheme {
        SignupPasswordScreen()
    }
}