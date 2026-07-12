package com.talkqquest.app.feature.auth.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.talkqquest.app.core.designsystem.FitDesign
import com.talkqquest.app.core.designsystem.TalkQQuestTheme

@Composable
fun SignupEmailScreen(
    onBack: () -> Unit = {},
    onSendClick: (String) -> Unit = {},
) = FitDesign {
    var email by remember { mutableStateOf("") }

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
                placeholder = "Talkqquest1234@gmail.com",
                onValueChange = { email = it },
                actionText = "전송",
                onActionClick = { onSendClick(email) },
                keyboardType = KeyboardType.Email,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun SignupEmailEmptyPreview() {
    TalkQQuestTheme {
        SignupEmailScreen()
    }
}
