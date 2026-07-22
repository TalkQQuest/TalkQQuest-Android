package com.talkqquest.app.feature.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.talkqquest.app.core.designsystem.Error
import com.talkqquest.app.core.designsystem.FitDesign
import com.talkqquest.app.core.designsystem.Gray300
import com.talkqquest.app.core.designsystem.Gray50
import com.talkqquest.app.core.designsystem.Gray500
import com.talkqquest.app.core.designsystem.Gray700
import com.talkqquest.app.core.designsystem.Gray800
import com.talkqquest.app.core.designsystem.Gray900
import com.talkqquest.app.core.designsystem.Primary600
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.White
import com.talkqquest.app.core.designsystem.component.TqButton

@Composable
fun EmailLoginScreen(
    initialEmail: String = "",
    initialPassword: String = "",
    onBack: () -> Unit = {},
    onLoginClick: (String, String) -> Unit = { _, _ -> },
    onFindPasswordClick: () -> Unit = {},
    errorMessage: String? = null,
) = FitDesign(compensateStatusBar = false) {
    var email by remember { mutableStateOf(initialEmail) }
    var password by remember { mutableStateOf(initialPassword) }

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
            text = "\uB85C\uADF8\uC778",
            style = TqType.BodyM,
            color = Gray700,
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = 59.dp),
            textAlign = TextAlign.Center,
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .offset(y = 122.dp)
                .height(334.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(width = 164.dp, height = 54.dp)
                    .padding(start = 7.dp, top = 10.dp),
            ) {
                Text(
                    text = "\uC774\uBA54\uC77C\uB85C \uB85C\uADF8\uC778",
                    style = TqType.HeadingL,
                    color = Gray800,
                )
            }

            LoginTextField(
                value = email,
                placeholder = "\uC774\uBA54\uC77C \uC8FC\uC18C",
                onValueChange = { email = it },
                keyboardType = KeyboardType.Email,
                textColor = Gray900,
                modifier = Modifier.offset(y = 66.dp),
            )

            LoginTextField(
                value = password,
                placeholder = "\uBE44\uBC00\uBC88\uD638(\uC601\uBB38+\uC22B\uC790, 8-16\uC790)",
                onValueChange = { password = it.take(16) },
                keyboardType = KeyboardType.Password,
                textColor = Gray800,
                modifier = Modifier.offset(y = 139.dp),
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    style = TqType.BodyM,
                    color = Error,
                    modifier = Modifier
                        .offset(x = 8.dp, y = 208.dp),
                )
            }

            TqButton(
                text = "\uB85C\uADF8\uC778",
                onClick = { onLoginClick(email, password) },
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = 248.dp),
            )

            Text(
                text = "\uBE44\uBC00\uBC88\uD638 \uCC3E\uAE30",
                style = TqType.BodyM,
                color = Gray500,
                modifier = Modifier
                    .offset(x = 7.dp, y = 312.dp)
                    .clickable(onClick = onFindPasswordClick),
            )
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
    textColor: Color = Gray900,
    modifier: Modifier = Modifier,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .height(61.dp),
        singleLine = true,
        textStyle = TqType.TitleL.copy(color = textColor, fontWeight = FontWeight.SemiBold),
        cursorBrush = SolidColor(Primary600),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = visualTransformation,
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(61.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(White)
                    .border(width = 1.dp, color = Gray300, shape = RoundedCornerShape(8.dp))
                    .padding(start = 22.dp, end = 22.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (value.isBlank()) {
                    Text(
                        text = placeholder,
                        style = TqType.TitleL,
                        color = Gray300,
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
private fun EmailLoginFilledPreview() {
    TalkQQuestTheme {
        EmailLoginScreen(
            initialEmail = "talkqquest@gmail.com",
            initialPassword = "Talkee3643@@",
        )
    }
}

@Preview(showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun EmailLoginErrorPreview() {
    TalkQQuestTheme {
        EmailLoginScreen(
            initialEmail = "talkqquest@gmail.com",
            initialPassword = "Talkee3643@@",
            errorMessage = "\uC774\uBA54\uC77C \uB610\uB294 \uBE44\uBC00\uBC88\uD638\uB97C \uD655\uC778\uD574\uC8FC\uC138\uC694.",
        )
    }
}