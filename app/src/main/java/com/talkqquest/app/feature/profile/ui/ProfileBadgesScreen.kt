package com.talkqquest.app.feature.profile.ui

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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.talkqquest.app.R
import com.talkqquest.app.core.designsystem.FitDesign
import com.talkqquest.app.core.designsystem.Gray200
import com.talkqquest.app.core.designsystem.Gray400
import com.talkqquest.app.core.designsystem.Gray50
import com.talkqquest.app.core.designsystem.Gray800
import com.talkqquest.app.core.designsystem.Primary100
import com.talkqquest.app.core.designsystem.Primary600
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.TqType

private data class ProfileBadge(val id: Int, val achieved: Boolean)

@Composable
fun ProfileBadgesScreen(
    onBack: () -> Unit = {},
) = FitDesign {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showDetailDim by remember { mutableStateOf(false) }
    val badges = remember {
        listOf(
            ProfileBadge(1, true), ProfileBadge(2, true), ProfileBadge(3, false),
            ProfileBadge(4, false), ProfileBadge(5, true), ProfileBadge(6, true),
            ProfileBadge(7, true), ProfileBadge(8, false), ProfileBadge(9, false),
        )
    }
    val visibleBadges = when (selectedTab) {
        1 -> emptyList()
        2 -> badges.filter { it.achieved }
        else -> badges
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Gray50),
        ) {
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                ProfileSimpleTopBar(title = "\uD68D\uB4DD\uD55C \uBC30\uC9C0", onBack = onBack)
            }
            BadgeTabs(selectedTab = selectedTab, onSelectTab = { selectedTab = it; showDetailDim = false })
            Spacer(Modifier.height(23.dp))
            BadgeNotice()
            Spacer(Modifier.height(29.dp))
            if (visibleBadges.isNotEmpty() && selectedTab != 1) {
                BadgeGrid(
                    badges = visibleBadges,
                    onBadgeClick = { badge -> if (badge.achieved) showDetailDim = true },
                )
            }
        }
        if (showDetailDim) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF273449).copy(alpha = 0.24f))
                    .clickable { showDetailDim = false },
            )
        }
    }
}

@Composable
private fun BadgeTabs(
    selectedTab: Int,
    onSelectTab: (Int) -> Unit,
) {
    val labels = listOf("\uC804\uCCB4", "\uC9C4\uD589\uC911", "\uB2EC\uC131")
    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            labels.forEachIndexed { index, label ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(57.dp)
                        .clickable { onSelectTab(index) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    Text(
                        text = label,
                        style = TqType.BodyL.copy(fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.SemiBold),
                        color = if (selectedTab == index) Gray800 else Gray400,
                    )
                    Spacer(Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .size(width = 52.dp, height = 3.dp)
                            .clip(RoundedCornerShape(50))
                            .background(if (selectedTab == index) Gray800 else Color.Transparent),
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Gray200),
        )
    }
}

@Composable
private fun BadgeNotice() {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(Primary100)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = "\uD83C\uDFC5", style = TqType.BodyL)
        Spacer(Modifier.size(9.dp))
        Text(
            text = "\uCD95\uD558\uD574\uC694! \uBC8C\uC368 \uBC30\uC9C0 5\uAC1C\uB97C \uD68D\uB4DD\uD588\uC5B4\uC694!",
            style = TqType.BodyM.copy(fontWeight = FontWeight.SemiBold),
            color = Gray800,
        )
    }
}

@Composable
private fun BadgeGrid(
    badges: List<ProfileBadge>,
    onBadgeClick: (ProfileBadge) -> Unit,
) {
    Column(
        modifier = Modifier.padding(horizontal = 36.dp),
        verticalArrangement = Arrangement.spacedBy(54.dp),
    ) {
        badges.chunked(3).forEach { rowBadges ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                rowBadges.forEach { badge ->
                    Image(
                        painter = painterResource(if (badge.achieved) R.drawable.img_profile_badge_unlocked else R.drawable.img_profile_badge_locked),
                        contentDescription = null,
                        modifier = Modifier
                            .size(78.dp)
                            .clickable { onBadgeClick(badge) },
                    )
                }
                repeat(3 - rowBadges.size) {
                    Spacer(Modifier.size(78.dp))
                }
            }
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