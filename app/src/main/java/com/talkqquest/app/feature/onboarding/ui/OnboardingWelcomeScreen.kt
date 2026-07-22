package com.talkqquest.app.feature.onboarding.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.talkqquest.app.R
import com.talkqquest.app.core.designsystem.FitDesign
import com.talkqquest.app.core.designsystem.Gray50
import com.talkqquest.app.core.designsystem.Gray800
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.TqType
import kotlinx.coroutines.delay

@Composable
fun OnboardingWelcomeScreen(
    nickname: String,
    modifier: Modifier = Modifier,
    onFinished: (String) -> Unit = {},
) = FitDesign(compensateStatusBar = false) {
    val displayNickname = nickname.ifBlank { "\uB2E4\uBBFC" }

    LaunchedEffect(displayNickname) {
        delay(1800)
        onFinished(displayNickname)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Gray50),
    ) {
        WelcomeHandImage(
            modifier = Modifier
                .offset(x = 118.dp, y = 268.dp)
                .size(158.dp),
        )

        Text(
            text = "\uBC18\uAC00\uC6CC\uC694,\n${displayNickname}\uB2D8!",
            style = TqType.HeadingXL,
            color = Gray800,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = 416.dp),
        )
    }
}

@Composable
private fun WelcomeHandImage(
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "welcomeHandWave")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -14.33f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 420),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "welcomeHandRotation",
    )

    Image(
        painter = painterResource(R.drawable.img_onboarding_welcome_hand_1),
        contentDescription = null,
        modifier = modifier
            .graphicsLayer {
                transformOrigin = TransformOrigin(0.55f, 0.82f)
            }
            .rotate(rotation),
    )
}

@Preview(showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun OnboardingWelcomeScreenPreview() {
    TalkQQuestTheme {
        OnboardingWelcomeScreen(nickname = "\uC18C\uB2E4123")
    }
}