package com.talkqquest.app.feature.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.talkqquest.app.core.designsystem.FitDesign
import com.talkqquest.app.core.designsystem.Gray400
import com.talkqquest.app.core.designsystem.Gray50
import com.talkqquest.app.core.designsystem.Gray500
import com.talkqquest.app.core.designsystem.Gray700
import com.talkqquest.app.core.designsystem.Gray800
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.component.TqButton

private const val NicknameMaxLength = 10

@Composable
fun SignupNicknameScreen(
    initialNickname: String = "",
    onBack: () -> Unit = {},
    onCompleteClick: (String) -> Unit = {},
) = FitDesign(compensateStatusBar = false) {
    var nickname by remember { mutableStateOf(initialNickname.take(NicknameMaxLength)) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50),
    ) {
        Text(
            text = "\uB2C9\uB124\uC784",
            style = TqType.BodyM,
            color = Gray700,
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = 59.dp),
            textAlign = TextAlign.Center,
        )

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
            text = "\uC5B4\uB5A4 \uC774\uB984\uC73C\uB85C\n\uBD88\uB7EC\uB4DC\uB9B4\uAE4C\uC694?",
            style = TqType.HeadingL,
            color = Gray800,
            modifier = Modifier
                .offset(x = 23.dp, y = 122.dp)
                .size(width = 220.dp, height = 68.dp),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .offset(y = 206.dp)
                .height(116.dp),
        ) {
            AuthInputCard(
                label = "\uB2C9\uB124\uC784",
                value = nickname,
                placeholder = "\uC18C\uB2E4123",
                onValueChange = { input -> nickname = input.take(NicknameMaxLength) },
                keyboardType = KeyboardType.Text,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(88.dp),
            )
            Text(
                text = "(${nickname.length}/$NicknameMaxLength)",
                style = TqType.LabelL,
                color = Gray400,
                modifier = Modifier
                    .offset(x = 12.dp, y = 96.dp)
                    .size(width = 69.dp, height = 20.dp),
            )
        }

        TqButton(
            text = "\uB2E4\uC74C",
            onClick = {
                if (nickname.isNotBlank()) {
                    onCompleteClick(nickname)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .offset(y = 728.dp)
                .height(52.dp),
        )
    }
}

@Preview(showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun SignupNicknameBeforePreview() {
    TalkQQuestTheme {
        SignupNicknameScreen()
    }
}

@Preview(name = "\uC785\uB825 \uD6C4", showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun SignupNicknameAfterPreview() {
    TalkQQuestTheme {
        SignupNicknameScreen(initialNickname = "\uC18C\uB2E4123")
    }
}