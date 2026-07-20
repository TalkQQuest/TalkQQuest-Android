package com.talkqquest.app.feature.auth.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.talkqquest.app.R
import com.talkqquest.app.core.designsystem.FitDesign
import com.talkqquest.app.core.designsystem.Gray200
import com.talkqquest.app.core.designsystem.Gray300
import com.talkqquest.app.core.designsystem.Gray600
import com.talkqquest.app.core.designsystem.Gray800
import com.talkqquest.app.core.designsystem.Primary600
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.White


private val SignupStartBackground = Color(0xFFF8F6FF)
@Composable
fun SignupStartScreen(
    onKakaoClick: () -> Unit = {},
    onNaverClick: () -> Unit = {},
    onEmailSignupClick: () -> Unit = {},
    onEmailLoginClick: () -> Unit = {},
) = FitDesign {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SignupStartBackground)
            .statusBarsPadding()
            .padding(horizontal = 16.dp),
    ) {
        Spacer(Modifier.height(251.dp))
        Text(text = "톡깨", style = TqType.Display, color = Primary600)
        Spacer(Modifier.height(12.dp))
        Text(text = "어색함을 깨고,\n대화를 시작해보세요.", style = TqType.HeadingL, color = Gray800)
        Spacer(Modifier.weight(1f))
        SocialStartButton(
            text = "카카오로 시작하기",
            icon = {
                Image(
                    painter = painterResource(R.drawable.ic_auth_kakao_logo),
                    contentDescription = null,
                    modifier = Modifier.size(width = 22.dp, height = 21.dp),
                )
            },
            containerColor = Color(0xFFFEE500),
            contentColor = Color.Black,
            onClick = onKakaoClick,
        )
        Spacer(Modifier.height(12.dp))
        SocialStartButton(
            text = "네이버로 시작하기",
            icon = {
                Image(
                    painter = painterResource(R.drawable.ic_auth_naver_logo),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
            },
            containerColor = Color(0xFF28D111),
            contentColor = White,
            onClick = onNaverClick,
        )
        Spacer(Modifier.height(20.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(modifier = Modifier.weight(1f).height(1.dp).background(Gray200))
            Text(text = "또는", style = TqType.LabelM, color = Gray300)
            Box(modifier = Modifier.weight(1f).height(1.dp).background(Gray200))
        }
        Spacer(Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            EmailStartButton(
                text = "이메일로 회원가입",
                onClick = onEmailSignupClick,
                modifier = Modifier.weight(1f),
            )
            EmailStartButton(
                text = "이메일로 로그인",
                onClick = onEmailLoginClick,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(92.dp))
    }
}

@Composable
private fun SocialStartButton(
    text: String,
    icon: @Composable () -> Unit,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(containerColor)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon()
        Spacer(Modifier.width(10.dp))
        Text(text = text, style = TqType.BodyL.copy(fontWeight = FontWeight.SemiBold), color = contentColor)
    }
}

@Composable
private fun EmailStartButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(White)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, style = TqType.BodyL.copy(fontWeight = FontWeight.SemiBold), color = Gray600)
    }
}

@Preview(showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun SignupStartScreenPreview() {
    TalkQQuestTheme {
        SignupStartScreen()
    }
}