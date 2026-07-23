package com.talkqquest.app.feature.profile.ui

import androidx.compose.foundation.background
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.talkqquest.app.core.designsystem.FitDesign
import com.talkqquest.app.core.designsystem.Gray400
import com.talkqquest.app.core.designsystem.Gray50
import com.talkqquest.app.core.designsystem.Gray500
import com.talkqquest.app.core.designsystem.Gray800
import com.talkqquest.app.core.designsystem.Primary600
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.White
import com.talkqquest.app.core.designsystem.component.TqButton

private const val ProfileNicknameMaxLength = 10

@Composable
fun ProfileNicknameEditScreen(
    initialNickname: String = "소다123",
    onBack: () -> Unit = {},
    onSaveClick: (String) -> Unit = {},
) = FitDesign(compensateStatusBar = false) {
    var nickname by remember { mutableStateOf(initialNickname.take(ProfileNicknameMaxLength)) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50),
    ) {
        ProfileSimpleTopBar(title = "닉네임", onBack = onBack)

        Text(
            text = "어떤 이름으로\n불러드릴까요?",
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
            ProfileNicknameInputCard(
                value = nickname,
                placeholder = "소다123",
                onValueChange = { input -> nickname = input.take(ProfileNicknameMaxLength) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(88.dp),
            )
            Text(
                text = "(${nickname.length}/$ProfileNicknameMaxLength)",
                style = TqType.LabelL,
                color = Gray400,
                modifier = Modifier
                    .offset(x = 12.dp, y = 96.dp)
                    .size(width = 69.dp, height = 20.dp),
            )
        }

        TqButton(
            text = "저장",
            onClick = {
                if (nickname.isNotBlank()) {
                    onSaveClick(nickname)
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

@Composable
private fun ProfileNicknameInputCard(
    value: String,
    placeholder: String,
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
            text = "닉네임",
            style = TqType.LabelM,
            color = Gray500,
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(width = 313.dp, height = 18.dp),
        )
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TqType.TitleL.copy(color = if (value.isBlank()) Gray400 else Gray800),
            cursorBrush = SolidColor(Primary600),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier
                .offset(y = 28.dp)
                .size(width = 263.dp, height = 28.dp),
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (value.isBlank()) {
                        Text(
                            text = placeholder,
                            style = TqType.TitleL,
                            color = Gray400,
                        )
                    }
                    innerTextField()
                }
            },
        )
    }
}

@Preview(showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun ProfileNicknameEditScreenPreview() {
    TalkQQuestTheme {
        ProfileNicknameEditScreen()
    }
}


