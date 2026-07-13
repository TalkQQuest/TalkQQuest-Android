package com.talkqquest.app.feature.auth.ui

import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.talkqquest.app.core.designsystem.FitDesign
import com.talkqquest.app.core.designsystem.Gray400
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.component.TqButton

private const val NicknameMaxLength = 10

@Composable
fun SignupNicknameScreen(
    initialNickname: String = "",
    onBack: () -> Unit = {},
    onCompleteClick: (String) -> Unit = {},
) = FitDesign {
    var nickname by remember { mutableStateOf(initialNickname.take(NicknameMaxLength)) }

    AuthScreenFrame(title = "닉네임", onBack = onBack) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(113.dp))
            AuthHeadline(text = "톡! 깨실\n준비가 되셨나요?", modifier = Modifier.padding(start = 7.dp))
            Spacer(Modifier.height(20.dp))
            AuthInputCard(
                label = "닉네임",
                value = nickname,
                placeholder = "소다123",
                onValueChange = { input -> nickname = input.take(NicknameMaxLength) },
                keyboardType = KeyboardType.Text,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "(${nickname.length}/$NicknameMaxLength)",
                style = TqType.BodyL,
                color = Gray400,
                modifier = Modifier.padding(start = 13.dp),
            )
            Spacer(Modifier.weight(1f))
            TqButton(
                text = if (nickname.isBlank()) "다음" else "완료",
                onClick = { onCompleteClick(nickname) },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(68.dp))
        }
    }
}

@Preview(showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun SignupNicknameBeforePreview() {
    TalkQQuestTheme {
        SignupNicknameScreen()
    }
}

@Preview(name = "입력 후", showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun SignupNicknameAfterPreview() {
    TalkQQuestTheme {
        SignupNicknameScreen(initialNickname = "소다123")
    }
}