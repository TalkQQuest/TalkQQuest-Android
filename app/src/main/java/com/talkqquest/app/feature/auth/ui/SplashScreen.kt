package com.talkqquest.app.feature.auth.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.talkqquest.app.R
import com.talkqquest.app.core.designsystem.A2ZFamily
import com.talkqquest.app.core.designsystem.FitDesign
import com.talkqquest.app.core.designsystem.TalkQQuestTheme

private val SplashBackground = Color(0xFF6353F0)
private val SplashContentColor = Color(0xFFF8FAFC)
private val SplashLogoTextStyle = TextStyle(
    fontFamily = A2ZFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 24.sp,
    lineHeight = 29.sp,
)

@Composable
fun SplashScreen() = FitDesign(compensateStatusBar = false) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SplashBackground),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 333.dp)
                .size(width = 183.dp, height = 171.71.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.img_auth_splash_logo),
                contentDescription = null,
                modifier = Modifier
                    .offset(x = 30.dp, y = 0.dp)
                    .size(width = 122.83.dp, height = 118.71.dp),
            )
            Text(
                text = "TALKQQUEST",
                style = SplashLogoTextStyle,
                color = SplashContentColor,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .offset(x = 0.dp, y = 142.71.dp)
                    .size(width = 183.dp, height = 29.dp),
            )
        }
    }
}

@Preview(showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun SplashScreenPreview() {
    TalkQQuestTheme {
        SplashScreen()
    }
}






