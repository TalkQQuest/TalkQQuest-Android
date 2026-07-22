package com.talkqquest.app.feature.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.talkqquest.app.core.designsystem.FitDesign
import com.talkqquest.app.core.designsystem.Gray50
import com.talkqquest.app.core.designsystem.Gray500
import com.talkqquest.app.core.designsystem.Gray700
import com.talkqquest.app.core.designsystem.Gray800
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.White
import com.talkqquest.app.core.designsystem.softShadow

@Composable
fun ProfileSupportScreen(
    onBack: () -> Unit = {},
) = FitDesign {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        ProfileSimpleTopBar(title = "문의하기", onBack = onBack)
        Spacer(Modifier.height(24.dp))
        Text(
            text = "무엇을 도와드릴까요?",
            style = TqType.TitleL.copy(fontWeight = FontWeight.Bold),
            color = Gray800,
        )
        Spacer(Modifier.height(22.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .softShadow(
                    color = Color.Black.copy(alpha = 0.025f),
                    offsetY = 8.dp,
                    blur = 24.dp,
                    cornerRadius = 14.dp,
                )
                .clip(RoundedCornerShape(14.dp))
                .background(White)
                .padding(horizontal = 16.dp, vertical = 18.dp),
        ) {
            Text("고객 지원", style = TqType.BodyM, color = Gray500)
            Spacer(Modifier.height(26.dp))
            SupportRow(title = "자주 묻는 질문")
            Spacer(Modifier.height(25.dp))
            SupportRow(title = "이메일 문의", trailing = "talkqquest@naver.com")
        }
    }
}

@Composable
private fun SupportRow(title: String, trailing: String? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = TqType.BodyL.copy(fontWeight = FontWeight.Medium),
            color = Gray800,
            modifier = Modifier.weight(1f),
        )
        if (trailing != null) {
            Text(text = trailing, style = TqType.BodyM, color = Gray500)
            Spacer(Modifier.padding(horizontal = 6.dp))
        }
        Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Gray700)
    }
}

@Preview(showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun ProfileSupportScreenPreview() {
    TalkQQuestTheme {
        ProfileSupportScreen()
    }
}