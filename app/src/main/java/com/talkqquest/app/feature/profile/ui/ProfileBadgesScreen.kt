package com.talkqquest.app.feature.profile.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.talkqquest.app.R
import com.talkqquest.app.core.designsystem.FitDesign
import com.talkqquest.app.core.designsystem.Gray300
import com.talkqquest.app.core.designsystem.Gray400
import com.talkqquest.app.core.designsystem.Gray50
import com.talkqquest.app.core.designsystem.Gray500
import com.talkqquest.app.core.designsystem.Gray700
import com.talkqquest.app.core.designsystem.Gray800
import com.talkqquest.app.core.designsystem.Gray900
import com.talkqquest.app.core.designsystem.PretendardFamily
import com.talkqquest.app.core.designsystem.Primary100
import com.talkqquest.app.core.designsystem.Primary600
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.White

private data class ProfileBadge(
    val name: String,
    val achieved: Boolean,
)

private val BadgeTitleStyle = TextStyle(
    fontFamily = PretendardFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp,
)

private val BadgeTabStyle = TextStyle(
    fontFamily = PretendardFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 18.sp,
    lineHeight = 28.sp,
    letterSpacing = (-0.01).em,
)

private val BadgeBodyLargeMediumStyle = TextStyle(
    fontFamily = PretendardFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 16.sp,
    lineHeight = 24.sp,
)

private val BadgeBodyLargeStyle = TextStyle(
    fontFamily = PretendardFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp,
)

private val BadgeHeadingStyle = TextStyle(
    fontFamily = PretendardFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 20.sp,
    lineHeight = 30.sp,
    letterSpacing = (-0.01).em,
)

private val BadgeBodyMediumStyle = TextStyle(
    fontFamily = PretendardFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 22.sp,
)

@Composable
fun ProfileBadgesScreen(
    onBack: () -> Unit = {},
) = FitDesign(compensateStatusBar = false) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedBadge by remember { mutableStateOf<ProfileBadge?>(null) }
    val badges = remember {
        listOf(
            ProfileBadge("설레는 첫 걸음", true),
            ProfileBadge("대화 새싹", false),
            ProfileBadge("먼저 건넨 인사", true),
            ProfileBadge("대화 탐험가", false),
            ProfileBadge("새로운 도전", true),
            ProfileBadge("대화 마스터", false),
            ProfileBadge("꾸준한 대화 습관", true),
            ProfileBadge("일주일의 변화", true),
            ProfileBadge("친절한 한마디", false),
            ProfileBadge("공감의 귀", false),
            ProfileBadge("대화의 리더", false),
            ProfileBadge("질문의 달인", false),
        )
    }
    val visibleBadges = when (selectedTab) {
        1 -> badges.filterNot { it.achieved }
        2 -> badges.filter { it.achieved }
        else -> badges
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50),
    ) {
        BadgesTopBar(onBack = onBack)
        BadgeTabs(
            selectedTab = selectedTab,
            onSelectTab = {
                selectedTab = it
                selectedBadge = null
            },
        )
        BadgeNotice()
        BadgeGrid(
            badges = visibleBadges,
            onBadgeClick = { badge -> selectedBadge = badge },
        )

        selectedBadge?.let { badge ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF334155).copy(alpha = 0.23f))
                    .clickable { selectedBadge = null },
            )
            BadgeDetailDialog(
                badge = badge,
                onClose = { selectedBadge = null },
            )
        }
    }
}

@Composable
private fun BadgesTopBar(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .offset(x = 0.dp, y = 48.dp)
            .size(width = 234.dp, height = 44.dp),
    ) {
        BadgeBackButton(onClick = onBack, modifier = Modifier.size(44.dp))
        Text(
            text = "획득한 배지",
            style = BadgeTitleStyle,
            color = Gray800,
            modifier = Modifier
                .offset(x = 160.dp, y = 10.dp)
                .size(width = 74.dp, height = 24.dp),
        )
    }
}

@Composable
private fun BadgeBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_back_chevron),
            contentDescription = null,
            tint = Gray500,
            modifier = Modifier.size(width = 30.dp, height = 32.dp),
        )
    }
}

@Composable
private fun BadgeTabs(
    selectedTab: Int,
    onSelectTab: (Int) -> Unit,
) {
    val labels = listOf("전체", "진행중", "달성")
    val xPositions = listOf(52.dp, 173.dp, 310.dp)
    val widths = listOf(31.dp, 47.dp, 31.dp)

    Box(
        modifier = Modifier
            .offset(y = 108.dp)
            .size(width = 393.dp, height = 38.dp),
    ) {
        labels.forEachIndexed { index, label ->
            Text(
                text = label,
                style = BadgeTabStyle,
                color = if (selectedTab == index) Gray800 else Gray400,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .offset(x = xPositions[index])
                    .size(width = widths[index], height = 28.dp)
                    .clickable { onSelectTab(index) },
            )
        }
        Box(
            modifier = Modifier
                .offset(y = 37.dp)
                .size(width = 393.dp, height = 1.dp)
                .background(Gray300),
        )
        Box(
            modifier = Modifier
                .offset(x = when (selectedTab) {
                    1 -> 171.dp
                    2 -> 300.dp
                    else -> 43.dp
                }, y = 36.dp)
                .size(width = 52.dp, height = 3.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Gray800),
        )
    }
}

@Composable
private fun BadgeNotice() {
    Box(
        modifier = Modifier
            .offset(x = 16.dp, y = 169.dp)
            .size(width = 361.dp, height = 48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Primary100),
    ) {
        Image(
            painter = painterResource(R.drawable.ic_profile_medal),
            contentDescription = null,
            modifier = Modifier
                .offset(x = 18.dp, y = 15.dp)
                .size(18.dp),
        )
        Box(
            modifier = Modifier
                .offset(x = 46.dp, y = 10.dp)
                .size(width = 245.dp, height = 28.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                text = buildAnnotatedString {
                    append("축하해요! 벌써 배지 ")
                    withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, fontSize = 18.sp)) { append("5개") }
                    append("를 획득했어요!")
                },
                style = BadgeBodyLargeMediumStyle,
                color = Gray700,
            )
        }
    }
}

@Composable
private fun BadgeGrid(
    badges: List<ProfileBadge>,
    onBadgeClick: (ProfileBadge) -> Unit,
) {
    val rowCount = ((badges.size + 2) / 3).coerceAtLeast(1)
    val contentHeight = ((rowCount - 1) * 146 + 124).dp

    Box(
        modifier = Modifier
            .offset(x = 0.dp, y = 240.dp)
            .size(width = 393.dp, height = 488.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Box(modifier = Modifier.size(width = 393.dp, height = contentHeight)) {
            badges.forEachIndexed { index, badge ->
                val row = index / 3
                val column = index % 3
                BadgeItem(
                    badge = badge,
                    onClick = { onBadgeClick(badge) },
                    modifier = Modifier
                        .offset(x = (23 + column * 122).dp, y = (row * 146).dp)
                        .size(width = 100.dp, height = 124.dp),
                )
            }
        }
    }
}

@Composable
private fun BadgeItem(
    badge: ProfileBadge,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.clickable(onClick = onClick)) {
        Image(
            painter = painterResource(
                if (badge.achieved) R.drawable.img_profile_badge_unlocked else R.drawable.img_profile_badge_locked,
            ),
            contentDescription = null,
            modifier = Modifier.size(100.dp),
        )
        val labelWidth = badge.labelWidthDp().dp
        Text(
            text = badge.name,
            style = BadgeBodyLargeMediumStyle,
            color = if (badge.achieved) Gray900 else Gray500,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .offset(x = ((100 - badge.labelWidthDp()) / 2f).dp, y = 100.dp)
                .size(width = labelWidth, height = 24.dp),
        )
    }
}

@Composable
private fun BadgeDetailDialog(
    badge: ProfileBadge,
    onClose: () -> Unit,
) {
    val dialogHeight = if (badge.achieved) 446.dp else 401.dp
    Box(
        modifier = Modifier
            .offset(x = 55.dp, y = 240.dp)
            .size(width = 284.dp, height = dialogHeight)
            .clip(RoundedCornerShape(16.dp))
            .background(White)
            .clickable(enabled = false) {},
    ) {
        Box(
            modifier = Modifier
                .offset(x = 232.dp, y = 8.dp)
                .size(44.dp)
                .clickable(onClick = onClose),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_profile_close),
                contentDescription = "닫기",
                modifier = Modifier.size(12.dp),
            )
        }
        Image(
            painter = painterResource(
                if (badge.achieved) R.drawable.img_profile_badge_unlocked else R.drawable.img_profile_badge_locked,
            ),
            contentDescription = null,
            modifier = Modifier
                .offset(x = 72.dp, y = 76.dp)
                .size(140.dp),
        )
        Text(
            text = badge.name,
            style = BadgeHeadingStyle,
            color = Gray900,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .offset(x = 82.dp, y = 252.dp)
                .size(width = 120.dp, height = 30.dp),
        )
        Text(
            text = if (badge.achieved) "대화 미션을 처음으로 1회 완료" else "대화 미션을 누적 15회 완료",
            style = BadgeBodyLargeMediumStyle,
            color = Gray700,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .offset(x = if (badge.achieved) 47.5.dp else 56.5.dp, y = 286.dp)
                .size(width = if (badge.achieved) 189.dp else 171.dp, height = 24.dp),
        )
        if (badge.achieved) {
            Text(
                text = "2026.07.22",
                style = BadgeBodyMediumStyle,
                color = Gray500,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .offset(x = 82.dp, y = 316.dp)
                    .size(width = 120.dp, height = 22.dp),
            )
            Box(
                modifier = Modifier
                    .offset(x = 24.dp, y = 374.dp)
                    .size(width = 236.dp, height = 48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Primary600),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "공유하기",
                    style = BadgeTabStyle,
                    color = Gray50,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.size(width = 70.dp, height = 28.dp),
                )
            }
        } else {
            Text(
                text = "( 9 / 15 )",
                style = BadgeBodyLargeStyle,
                color = Gray500,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .offset(x = 82.dp, y = 316.dp)
                    .size(width = 120.dp, height = 24.dp),
            )
        }
    }
}

@Preview(showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun ProfileBadgesScreenPreview() {
    TalkQQuestTheme {
        ProfileBadgesScreen()
    }
}

private fun ProfileBadge.labelWidthDp(): Int = when (name) {
    "꾸준한 대화 습관" -> 105
    "일주일의 변화" -> 87
    else -> 100
}








