package com.talkqquest.app.feature.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
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

@Composable
fun ProfileConnectedAccountScreen(
    onBack: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
) = FitDesign {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50),
    ) {
        ProfileSimpleTopBar(title = "연결된 계정", onBack = onBack)
        Row(
            modifier = Modifier
                .offset(x = 16.dp, y = 115.dp)
                .size(width = 361.dp, height = 48.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(White),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(Modifier.size(width = 16.dp, height = 1.dp))
            Text(
                text = "연결된 계정",
                style = TqType.BodyL,
                color = Gray800,
                modifier = Modifier.size(width = 74.dp, height = 24.dp),
            )
            Spacer(Modifier.size(width = 24.dp, height = 1.dp))
            Box(
                modifier = Modifier.size(width = 231.dp, height = 24.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Text(
                    text = "talkqquest@naver.com",
                    style = TqType.BodyL,
                    color = Gray500,
                    modifier = Modifier.size(width = 163.dp, height = 24.dp),
                )
            }
        }

        Column(
            modifier = Modifier
                .offset(x = 16.dp, y = 701.dp)
                .size(width = 361.dp, height = 82.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(width = 361.dp, height = 52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Primary600)
                    .clickable(onClick = onLogoutClick),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "로그아웃",
                    style = TqType.BodyL.copy(fontWeight = FontWeight.SemiBold),
                    color = Gray50,
                )
            }
            Spacer(Modifier.size(width = 1.dp, height = 12.dp))
            Text(
                text = "계정 변경이 필요하다면 로그아웃 후 새로운 계정으로 로그인할 수 있어요",
                style = TqType.LabelM,
                color = Gray400,
                modifier = Modifier
                    .offset(x = 4.dp)
                    .size(width = 357.dp, height = 18.dp),
            )
        }
    }
}

@Preview(showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun ProfileConnectedAccountScreenPreview() {
    TalkQQuestTheme {
        ProfileConnectedAccountScreen()
    }
}



