package com.talkqquest.app.feature.auth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.talkqquest.app.core.designsystem.FitDesign
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.component.TqButton

@Composable
fun SignupPasswordScreen(
    onBack: () -> Unit = {},
    onNextClick: (String) -> Unit = {},
) = FitDesign {
    var password by remember { mutableStateOf("") }
    val hasValidLength = password.length in 8..16
    val hasLetterAndDigit = password.any { it.isLetter() } && password.any { it.isDigit() }

    AuthScreenFrame(title = "회원가입", onBack = onBack) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(113.dp))
            AuthHeadline(text = "비밀번호를\n설정해주세요", modifier = Modifier.padding(start = 7.dp))
            Spacer(Modifier.height(20.dp))
            AuthInputCard(
                label = "비밀번호",
                value = password,
                placeholder = "비밀번호 입력",
                onValueChange = { password = it.take(16) },
                keyboardType = KeyboardType.Password,
                visualTransformation = PasswordVisualTransformation(),
                trailing = { EyeOffIcon() },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.padding(start = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RequirementItem(text = "8-16자", satisfied = hasValidLength)
                RequirementItem(text = "영문, 숫자 포함", satisfied = hasLetterAndDigit)
            }
            Spacer(Modifier.weight(1f))
            TqButton(
                text = "다음",
                onClick = { onNextClick(password) },
                enabled = hasValidLength && hasLetterAndDigit,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(68.dp))
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
