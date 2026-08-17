package com.talkqquest.app.feature.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.talkqquest.app.core.designsystem.FitDesign
import com.talkqquest.app.core.designsystem.Gray200
import com.talkqquest.app.core.designsystem.Gray50
import com.talkqquest.app.core.designsystem.Gray500
import com.talkqquest.app.core.designsystem.Gray800
import com.talkqquest.app.core.designsystem.Gray900
import com.talkqquest.app.core.designsystem.PretendardFamily
import com.talkqquest.app.core.designsystem.Primary500
import com.talkqquest.app.core.designsystem.Primary600
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.White
import com.talkqquest.app.core.designsystem.component.ContentAnchoredPillRipple
import com.talkqquest.app.navigation.NavigationMotion

private val SignupTermsTitleStyle = TextStyle(
    fontFamily = PretendardFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 20.sp,
    lineHeight = 30.sp,
    letterSpacing = (-0.2).sp,
)

private val SignupTermsBodyStyle = TextStyle(
    fontFamily = PretendardFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp,
)

private val SignupTermsButtonStyle = TextStyle(
    fontFamily = PretendardFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 18.sp,
    lineHeight = 28.sp,
    letterSpacing = (-0.18).sp,
)

private val SignupTermsTopBarStyle = TextStyle(
    fontFamily = PretendardFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp,
)

private val SignupTermsDetailTitleStyle = TextStyle(
    fontFamily = PretendardFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 18.sp,
    lineHeight = 28.sp,
)

private val SignupTermsDetailBodyStyle = TextStyle(
    fontFamily = PretendardFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    lineHeight = 18.sp,
)

@Composable
fun SignupTermsScreen(
    serviceTermsContent: String? = null,
    privacyPolicyContent: String? = null,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onCloseClick: () -> Unit = {},
    onAgreeClick: () -> Unit = {},
) = FitDesign(compensateStatusBar = false) {
    var showDetail by remember { mutableStateOf(false) }

    if (showDetail) {
        SignupLegalDetailScreen(
            serviceTermsContent = serviceTermsContent,
            privacyPolicyContent = privacyPolicyContent,
            isLoading = isLoading,
            errorMessage = errorMessage,
            onBack = { showDetail = false },
        )
    } else {
        SignupTermsConfirmScreen(
            onTitleClick = { showDetail = true },
            onCloseClick = onCloseClick,
            onAgreeClick = onAgreeClick,
        )
    }
}

@Composable
private fun SignupTermsConfirmScreen(
    onTitleClick: () -> Unit,
    onCloseClick: () -> Unit,
    onAgreeClick: () -> Unit,
) {
    val titleInteractionSource = remember { MutableInteractionSource() }
    var titleContentPos by remember { mutableStateOf(Offset.Zero) }
    var titleContentSize by remember { mutableStateOf(IntSize.Zero) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-19).dp)
                .size(width = 336.dp, height = 190.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(White),
        ) {
            ContentAnchoredPillRipple(titleContentPos, titleContentSize, titleInteractionSource)
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = 24.dp)
                    .height(30.dp)
                    .onGloballyPositioned {
                        titleContentPos = it.positionInParent()
                        titleContentSize = it.size
                    }
                    .clickable(
                        interactionSource = titleInteractionSource,
                        indication = null,
                        onClick = onTitleClick,
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "\uC774\uC6A9\uC57D\uAD00",
                    style = SignupTermsTitleStyle.copy(textDecoration = TextDecoration.Underline),
                    color = Gray900,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "\uC5D0 \uB3D9\uC758\uD574\uC8FC\uC138\uC694",
                    style = SignupTermsTitleStyle,
                    color = Gray900,
                    textAlign = TextAlign.Center,
                )
            }
            Text(
                text = "\uC11C\uBE44\uC2A4\uB97C \uC774\uC6A9\uD558\uB824\uBA74 \uC774\uC6A9 \uC57D\uAD00\uACFC \uAC1C\uC778\uC815\uBCF4 \uCC98\uB9AC\uBC29\uCE68\uC5D0 \uB300\uD55C \uB3D9\uC758\uAC00 \uD544\uC694\uD569\uB2C8\uB2E4.",
                style = SignupTermsBodyStyle,
                color = Gray500,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = 58.dp)
                    .size(width = 288.dp, height = 48.dp),
            )
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = 122.dp)
                    .size(width = 288.dp, height = 48.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SignupTermsButton(
                    text = "\uB2EB\uAE30",
                    containerColor = Gray200,
                    contentColor = Gray500,
                    onClick = onCloseClick,
                )
                SignupTermsButton(
                    text = "\uB3D9\uC758\uD558\uACE0 \uACC4\uC18D",
                    containerColor = Primary600,
                    contentColor = Gray50,
                    onClick = onAgreeClick,
                )
            }
        }
    }
}

@Composable
private fun SignupLegalDetailScreen(
    serviceTermsContent: String?,
    privacyPolicyContent: String?,
    isLoading: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50),
    ) {
        SignupTermsTopBar(onBack = onBack)
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 100.dp)
                .size(width = 28.dp, height = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SignupTermsPageDot(
                selected = pagerState.currentPage == 0,
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(0, animationSpec = NavigationMotion.floatSpec)
                    }
                },
            )
            SignupTermsPageDot(
                selected = pagerState.currentPage == 1,
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(1, animationSpec = NavigationMotion.floatSpec)
                    }
                },
            )
        }
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 118.dp)
                .size(width = 360.dp, height = 734.dp),
            flingBehavior = PagerDefaults.flingBehavior(
                state = pagerState,
                snapAnimationSpec = NavigationMotion.pagerSnapSpec,
            ),
        ) { page ->
            val title = if (page == 0) "\uC774\uC6A9\uC57D\uAD00" else "\uAC1C\uC778\uC815\uBCF4 \uCC98\uB9AC \uBC29\uCE68"
            val content = if (page == 0) serviceTermsContent else privacyPolicyContent
            val bodyText = when {
                !content.isNullOrBlank() -> content
                isLoading -> "\uC57D\uAD00\uC744 \uBD88\uB7EC\uC624\uB294 \uC911\uC785\uB2C8\uB2E4."
                !errorMessage.isNullOrBlank() -> errorMessage
                else -> "\uD45C\uC2DC\uD560 \uC57D\uAD00 \uB0B4\uC6A9\uC774 \uC5C6\uC2B5\uB2C8\uB2E4."
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                Text(
                    text = title,
                    style = SignupTermsDetailTitleStyle,
                    color = Color.Black,
                    modifier = Modifier.size(width = 360.dp, height = 28.dp),
                )
                Spacer(Modifier.height(16.dp))

                Text(
                    text = bodyText,
                    style = SignupTermsDetailBodyStyle,
                    color = Gray500,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(120.dp))
            }
        }
    }
}

@Composable
private fun SignupTermsTopBar(onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .offset(x = 0.dp, y = 48.dp)
                .size(44.dp)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = null,
                tint = Gray500,
                modifier = Modifier.size(30.dp),
            )
        }
        Text(
            text = "\uC57D\uAD00 \uBC0F \uAC1C\uC778\uC815\uBCF4 \uCC98\uB9AC \uBC29\uCE68",
            style = SignupTermsTopBarStyle,
            color = Gray800,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 58.dp)
                .size(width = 169.dp, height = 24.dp),
        )
    }
}

@Composable
private fun SignupTermsPageDot(
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(if (selected) Primary500 else Gray200)
            .clickable(onClick = onClick),
    )
}

@Composable
private fun SignupTermsButton(
    text: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(width = 138.dp, height = 48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = SignupTermsButtonStyle,
            color = contentColor,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun SignupTermsScreenPreview() {
    TalkQQuestTheme {
        SignupTermsScreen(
            serviceTermsContent = "\uC11C\uBE44\uC2A4 \uC774\uC6A9 \uBC0F \uD68C\uC6D0\uAC00\uC785\n\uD68C\uC6D0\uC740 \uC11C\uBE44\uC2A4 \uC774\uC6A9\uC57D\uAD00 \uBC0F \uAC1C\uC778\uC815\uBCF4 \uCC98\uB9AC\uBC29\uCE68\uC5D0 \uB3D9\uC758\uD55C \uD6C4 \uD68C\uC6D0\uAC00\uC785\uC744 \uC9C4\uD589\uD560 \uC218 \uC788\uC2B5\uB2C8\uB2E4.",
            privacyPolicyContent = "\uC218\uC9D1\uD558\uB294 \uAC1C\uC778\uC815\uBCF4\n\uD68C\uC0AC\uB294 \uD68C\uC6D0\uAC00\uC785 \uBC0F \uC11C\uBE44\uC2A4 \uC81C\uACF5\uC744 \uC704\uD574 \uD544\uC694\uD55C \uAC1C\uC778\uC815\uBCF4\uB97C \uC218\uC9D1\uD560 \uC218 \uC788\uC2B5\uB2C8\uB2E4.",
        )
    }
}
