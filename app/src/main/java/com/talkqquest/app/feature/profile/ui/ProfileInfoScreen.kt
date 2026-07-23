package com.talkqquest.app.feature.profile.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.talkqquest.app.R
import com.talkqquest.app.core.designsystem.FitDesign
import com.talkqquest.app.core.designsystem.Gray50
import com.talkqquest.app.core.designsystem.Gray400
import com.talkqquest.app.core.designsystem.Gray500
import com.talkqquest.app.core.designsystem.Gray600
import com.talkqquest.app.core.designsystem.Gray800
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.TqType

@Composable
fun ProfileInfoScreen(
    onBack: () -> Unit = {},
    onNicknameClick: () -> Unit = {},
    onConnectedAccountClick: () -> Unit = {},
    onConcernClick: () -> Unit = {},
    isEmailMember: Boolean = true,
) = FitDesign {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50),
    ) {
        ProfileSimpleTopBar(title = "내 정보", onBack = onBack)
        Row(
            modifier = Modifier
                .offset(x = 16.dp, y = 116.dp)
                .size(width = 202.dp, height = 60.dp)
                .clickable(onClick = onNicknameClick),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(R.drawable.img_profile_avatar),
                contentDescription = null,
                modifier = Modifier.size(60.dp),
            )
            Spacer(Modifier.size(width = 12.dp, height = 1.dp))
            Row(
                modifier = Modifier.size(width = 130.dp, height = 44.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "소다123 님",
                    style = TqType.HeadingM.copy(fontWeight = FontWeight.SemiBold),
                    color = Color(0xFF1E293B),
                    modifier = Modifier.size(width = 90.dp, height = 30.dp),
                )
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = Gray400,
                    modifier = Modifier.size(15.dp),
                )
            }
        }

        Box(
            modifier = Modifier
                .offset(x = 16.dp, y = 200.dp)
                .size(width = 371.dp, height = if (isEmailMember) 136.dp else 90.dp),
        ) {
            ProfileInfoRow(
                title = "연결된 계정",
                trailing = "talkqquest@naver.com",
                modifier = Modifier
                    .offset(y = 0.dp)
                    .size(width = 371.dp, height = 44.dp),
                onClick = onConnectedAccountClick,
            )
            if (isEmailMember) {
                ProfileInfoRow(
                    title = "비밀번호 변경",
                    modifier = Modifier
                        .offset(y = 46.dp)
                        .size(width = 371.dp, height = 44.dp),
                )
            }
            ProfileInfoRow(
                title = "소다123님의 대화 고민",
                titleWidth = 145,
                modifier = Modifier
                    .offset(y = if (isEmailMember) 92.dp else 46.dp)
                    .size(width = 371.dp, height = 44.dp),
            onClick = onConcernClick,
            )
        }
    }
}

@Composable
private fun ProfileInfoRow(
    title: String,
    modifier: Modifier = Modifier,
    trailing: String? = null,
    titleWidth: Int = 129,
    onClick: () -> Unit = {},
) {
    Row(
        modifier = modifier.clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = TqType.BodyL,
            color = Gray800,
            modifier = Modifier.size(width = titleWidth.dp, height = 24.dp),
        )
        Spacer(Modifier.weight(1f))
        if (trailing != null) {
            Text(
                text = trailing,
                style = TqType.BodyL,
                color = Gray500,
                modifier = Modifier.size(width = 163.dp, height = 24.dp),
            )
        }
        Box(
            modifier = Modifier.size(width = 44.dp, height = 44.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Gray600,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

@Preview(showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun ProfileInfoScreenPreview() {
    TalkQQuestTheme {
        ProfileInfoScreen()
    }
}

@Preview(showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun ProfileInfoSocialMemberScreenPreview() {
    TalkQQuestTheme {
        ProfileInfoScreen(isEmailMember = false)
    }
}











