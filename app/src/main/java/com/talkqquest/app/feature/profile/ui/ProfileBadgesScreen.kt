package com.talkqquest.app.feature.profile.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
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

data class ProfileBadgeUi(
    val id: String,
    val name: String,
    val description: String? = null,
    val isEarned: Boolean,
    val earnedAt: String? = null,
    val current: Int? = null,
    val target: Int? = null,
)

private val DefaultProfileBadges = listOf(
    ProfileBadgeUi("badge-001", "\uC124\uB808\uB294 \uCCAB \uAC78\uC74C", "\uB300\uD654 \uBBF8\uC158\uC744 \uCC98\uC74C\uC73C\uB85C 1\uD68C \uC644\uB8CC", true, "2026.07.22"),
    ProfileBadgeUi("badge-002", "\uB300\uD654 \uC0C8\uC2F9", "\uB300\uD654 \uBBF8\uC158\uC744 \uB204\uC801 5\uD68C \uC644\uB8CC", false, current = 0, target = 5),
    ProfileBadgeUi("badge-003", "\uBA3C\uC800 \uAC74\uB128 \uC778\uC0AC", "\uB300\uD654 \uBBF8\uC158\uC744 \uCC98\uC74C\uC73C\uB85C 1\uD68C \uC644\uB8CC", true, "2026.07.22"),
    ProfileBadgeUi("badge-004", "\uB300\uD654 \uD0D0\uD5D8\uAC00", "\uB300\uD654 \uBBF8\uC158\uC744 \uB204\uC801 15\uD68C \uC644\uB8CC", false, current = 9, target = 15),
    ProfileBadgeUi("badge-005", "\uC0C8\uB85C\uC6B4 \uB3C4\uC804", "\uC0C8\uB85C\uC6B4 \uB300\uD654 \uC720\uD615\uC5D0 \uB3C4\uC804", true, "2026.07.22"),
    ProfileBadgeUi("badge-006", "\uB300\uD654 \uB9C8\uC2A4\uD130", "\uB300\uD654 \uBBF8\uC158\uC744 \uB204\uC801 30\uD68C \uC644\uB8CC", false, current = 0, target = 30),
    ProfileBadgeUi("badge-007", "\uAFB8\uC900\uD55C \uB300\uD654 \uC2B5\uAD00", "7\uC77C \uC5F0\uC18D \uBBF8\uC158 \uC644\uB8CC", true, "2026.07.22"),
    ProfileBadgeUi("badge-008", "\uC77C\uC8FC\uC77C\uC758 \uBCC0\uD654", "\uC77C\uC8FC\uC77C \uB3D9\uC548 \uB300\uD654 \uC2B5\uAD00 \uC720\uC9C0", true, "2026.07.22"),
    ProfileBadgeUi("badge-009", "\uCE5C\uC808\uD55C \uD55C\uB9C8\uB514", "\uCE5C\uC808\uD55C \uD45C\uD604 \uC5F0\uC2B5 \uC644\uB8CC", false, current = 0, target = 1),
    ProfileBadgeUi("badge-010", "\uACF5\uAC10\uC758 \uADC0", "\uACF5\uAC10 \uD45C\uD604 \uBBF8\uC158 \uC9C4\uD589", false, current = 0, target = 1),
    ProfileBadgeUi("badge-011", "\uB300\uD654\uC758 \uB9AC\uB354", "\uB300\uD654 \uC8FC\uB3C4 \uBBF8\uC158 \uC9C4\uD589", false, current = 0, target = 1),
    ProfileBadgeUi("badge-012", "\uC9C8\uBB38\uC758 \uB2EC\uC778", "\uC9C8\uBB38\uD558\uAE30 \uBBF8\uC158 \uC9C4\uD589", false, current = 0, target = 1),
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
    badges: List<ProfileBadgeUi> = DefaultProfileBadges,
    onBack: () -> Unit = {},
) = FitDesign(compensateStatusBar = false, contentAlignment = Alignment.TopCenter) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedBadge by remember { mutableStateOf<ProfileBadgeUi?>(null) }
    val visibleBadges = when (selectedTab) {
        1 -> badges.filterNot { it.isEarned }
        2 -> badges.filter { it.isEarned }
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
        BadgeNotice(earnedCount = badges.count { it.isEarned })
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
            text = "\uD68D\uB4DD\uD55C \uBC30\uC9C0",
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
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
private fun BadgeTabs(
    selectedTab: Int,
    onSelectTab: (Int) -> Unit,
) {
    val labels = listOf("\uC804\uCCB4", "\uC9C4\uD589\uC911", "\uB2EC\uC131")
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
                .offset(
                    x = when (selectedTab) {
                        1 -> 171.dp
                        2 -> 300.dp
                        else -> 43.dp
                    },
                    y = 36.dp,
                )
                .size(width = 52.dp, height = 3.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Gray800),
        )
    }
}

@Composable
private fun BadgeNotice(earnedCount: Int) {
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
                    append("\uCD95\uD558\uD574\uC694! \uBC8C\uC368 \uBC30\uC9C0 ")
                    withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, fontSize = 18.sp)) {
                        append("${earnedCount}\uAC1C")
                    }
                    append("\uB97C \uD68D\uB4DD\uD588\uC5B4\uC694!")
                },
                style = BadgeBodyLargeMediumStyle,
                color = Gray700,
            )
        }
    }
}

@Composable
private fun BadgeGrid(
    badges: List<ProfileBadgeUi>,
    onBadgeClick: (ProfileBadgeUi) -> Unit,
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
    badge: ProfileBadgeUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.clickable(onClick = onClick)) {
        Image(
            painter = painterResource(
                if (badge.isEarned) R.drawable.img_profile_badge_unlocked else R.drawable.img_profile_badge_locked,
            ),
            contentDescription = null,
            modifier = Modifier.size(100.dp),
        )
        val labelWidth = badge.labelWidthDp().dp
        Text(
            text = badge.name,
            style = BadgeBodyLargeMediumStyle,
            color = if (badge.isEarned) Gray900 else Gray500,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .offset(x = ((100 - badge.labelWidthDp()) / 2f).dp, y = 100.dp)
                .size(width = labelWidth, height = 24.dp),
        )
    }
}

@Composable
private fun BadgeDetailDialog(
    badge: ProfileBadgeUi,
    onClose: () -> Unit,
) {
    val dialogHeight = if (badge.isEarned) 446.dp else 401.dp
    val descriptionText = badge.description ?: if (badge.isEarned) {
        "\uB300\uD654 \uBBF8\uC158\uC744 \uCC98\uC74C\uC73C\uB85C 1\uD68C \uC644\uB8CC"
    } else {
        "\uB300\uD654 \uBBF8\uC158\uC744 \uB204\uC801 15\uD68C \uC644\uB8CC"
    }
    val isLongDescription = descriptionText.length > 18
    val descriptionWidth = if (isLongDescription) 220.dp else if (badge.isEarned) 189.dp else 171.dp
    val descriptionX = (284.dp - descriptionWidth) / 2
    val detailMetaY = if (isLongDescription) 336.dp else 316.dp
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
                contentDescription = "\uB2EB\uAE30",
                modifier = Modifier.size(12.dp),
            )
        }
        Image(
            painter = painterResource(
                if (badge.isEarned) R.drawable.img_profile_badge_unlocked else R.drawable.img_profile_badge_locked,
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
            text = descriptionText,
            style = if (isLongDescription) {
                BadgeBodyLargeMediumStyle.copy(fontSize = 13.sp, lineHeight = 20.sp)
            } else {
                BadgeBodyLargeMediumStyle
            },
            color = Gray700,
            textAlign = TextAlign.Center,
            maxLines = if (isLongDescription) 2 else 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .offset(x = descriptionX, y = 286.dp)
                .width(descriptionWidth)
                .heightIn(min = if (isLongDescription) 42.dp else 28.dp),
        )
        if (badge.isEarned) {
            Text(
                text = badge.earnedAt?.substringBefore("T")?.replace("-", ".") ?: "2026.07.22",
                style = BadgeBodyMediumStyle,
                color = Gray500,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .offset(x = 82.dp, y = detailMetaY)
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
                    text = "\uACF5\uC720\uD558\uAE30",
                    style = BadgeTabStyle,
                    color = Gray50,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.size(width = 70.dp, height = 28.dp),
                )
            }
        } else {
            Text(
                text = "( ${badge.current ?: 0} / ${badge.target ?: 0} )",
                style = BadgeBodyLargeStyle,
                color = Gray500,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .offset(x = 82.dp, y = detailMetaY)
                    .width(120.dp)
                    .heightIn(min = 28.dp),
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

private fun ProfileBadgeUi.labelWidthDp(): Int = when (name) {
    "\uAFB8\uC900\uD55C \uB300\uD654 \uC2B5\uAD00" -> 105
    "\uC77C\uC8FC\uC77C\uC758 \uBCC0\uD654" -> 87
    else -> 100
}