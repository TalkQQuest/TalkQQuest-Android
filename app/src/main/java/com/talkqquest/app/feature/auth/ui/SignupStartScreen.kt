package com.talkqquest.app.feature.auth.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.talkqquest.app.R
import com.talkqquest.app.core.designsystem.A2ZFamily
import com.talkqquest.app.core.designsystem.FitDesign
import com.talkqquest.app.core.designsystem.Gray200
import com.talkqquest.app.core.designsystem.Gray300
import com.talkqquest.app.core.designsystem.Gray400
import com.talkqquest.app.core.designsystem.Gray500
import com.talkqquest.app.core.designsystem.Gray600
import com.talkqquest.app.core.designsystem.Gray800
import com.talkqquest.app.core.designsystem.PretendardFamily
import com.talkqquest.app.core.designsystem.Primary600
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.White

private val SignupStartBackground = Color(0xFFF8F6FF)
private val SignupLogoTextStyle = TextStyle(
    fontFamily = A2ZFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 36.sp,
    lineHeight = 43.sp,
    letterSpacing = 0.72.sp,
)
private val SignupDescriptionStyle = TextStyle(
    fontFamily = PretendardFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 24.sp,
    lineHeight = 34.sp,
    letterSpacing = (-0.24).sp,
)

@Composable
fun SignupStartScreen(
    onKakaoClick: () -> Unit = {},
    onNaverClick: () -> Unit = {},
    onEmailSignupClick: () -> Unit = {},
    onEmailLoginClick: () -> Unit = {},
) = FitDesign(compensateStatusBar = false) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(White, SignupStartBackground))),
    ) {
        Image(
            painter = painterResource(R.drawable.img_auth_signup_logo),
            contentDescription = null,
            modifier = Modifier
                .offset(x = 24.dp, y = 208.dp)
                .size(120.dp),
        )
        Text(
            text = "톡깨",
            style = SignupLogoTextStyle,
            color = Primary600,
            modifier = Modifier.offset(x = 24.dp, y = 340.dp),
        )
        Text(
            text = "어색함을 깨고,\n대화를 시작해보세요.",
            style = SignupDescriptionStyle,
            color = Gray800,
            modifier = Modifier.offset(x = 24.dp, y = 395.dp),
        )
        SocialStartButton(
            text = "카카오로 시작하기",
            icon = {
                Image(
                    painter = painterResource(R.drawable.ic_auth_kakao_logo),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                )
            },
            containerColor = Color(0xFFFEE500),
            contentColor = Gray800,
            onClick = onKakaoClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .offset(y = 543.dp),
        )
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
            contentColor = Gray800,
            onClick = onNaverClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .offset(y = 611.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 44.dp)
                .offset(y = 683.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(modifier = Modifier.weight(1f).height(1.dp).background(Gray300))
            Text(text = "또는", style = TqType.Caption, color = Gray400)
            Box(modifier = Modifier.weight(1f).height(1.dp).background(Gray300))
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .offset(y = 717.dp),
            horizontalArrangement = Arrangement.spacedBy(17.dp),
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
    }
}

@Composable
private fun SocialStartButton(
    text: String,
    icon: @Composable () -> Unit,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(containerColor)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon()
        Box(Modifier.width(12.dp))
        Text(text = text, style = TqType.TitleL, color = contentColor)
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
        Text(text = text, style = TqType.TitleL, color = Gray500)
    }
}

@Preview(showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun SignupStartScreenPreview() {
    TalkQQuestTheme {
        SignupStartScreen()
    }
}



