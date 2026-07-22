package com.talkqquest.app.feature.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.talkqquest.app.core.designsystem.FitDesign
import com.talkqquest.app.core.designsystem.Gray50
import com.talkqquest.app.core.designsystem.Gray500
import com.talkqquest.app.core.designsystem.Gray700
import com.talkqquest.app.core.designsystem.Gray800
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.TqType

@Composable
fun SignupEmailScreen(
    onBack: () -> Unit = {},
    onSendClick: (String) -> Unit = {},
) = FitDesign(compensateStatusBar = false) {
    var email by remember { mutableStateOf("") }
    val backButtonTop = if (email.isBlank()) 48.dp else 50.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50),
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .offset(x = 0.dp, y = backButtonTop)
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
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        Text(
            text = "이메일을\n입력해주세요",
            style = TqType.HeadingL,
            color = Gray800,
            modifier = Modifier.offset(x = 23.dp, y = 122.dp),
        )
        AuthInputCard(
            label = "이메일",
            value = email,
            placeholder = "Talkqquest1234@gmail.com",
            onValueChange = { email = it },
            actionText = "전송",
            onActionClick = { onSendClick(email) },
            keyboardType = KeyboardType.Email,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .offset(y = 206.dp)
                .height(88.dp),
        )
    }
}

@Preview(showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun SignupEmailEmptyPreview() {
    TalkQQuestTheme {
        SignupEmailScreen()
    }
}
