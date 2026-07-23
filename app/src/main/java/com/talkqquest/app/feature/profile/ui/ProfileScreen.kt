package com.talkqquest.app.feature.profile.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.talkqquest.app.R
import com.talkqquest.app.core.designsystem.FitDesign
import com.talkqquest.app.core.designsystem.Gray100
import com.talkqquest.app.core.designsystem.Gray300
import com.talkqquest.app.core.designsystem.Gray400
import com.talkqquest.app.core.designsystem.Gray50
import com.talkqquest.app.core.designsystem.Gray500
import com.talkqquest.app.core.designsystem.Gray600
import com.talkqquest.app.core.designsystem.Gray800
import com.talkqquest.app.core.designsystem.Gray900
import com.talkqquest.app.core.designsystem.PretendardFamily
import com.talkqquest.app.core.designsystem.Primary100
import com.talkqquest.app.core.designsystem.Primary50
import com.talkqquest.app.core.designsystem.Primary600
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.White
import com.talkqquest.app.core.designsystem.softShadow

private val ProfileTitleStyle = TextStyle(
    fontFamily = PretendardFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 18.sp,
    lineHeight = 28.sp,
    letterSpacing = (-0.01).em,
)

private val HeadingStyle = TextStyle(
    fontFamily = PretendardFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 20.sp,
    lineHeight = 30.sp,
    letterSpacing = (-0.01).em,
)

private val BodyLargeMediumStyle = TextStyle(
    fontFamily = PretendardFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 16.sp,
    lineHeight = 24.sp,
)

private val BodyMediumStyle = TextStyle(
    fontFamily = PretendardFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 22.sp,
)

private val BodySmallStyle = TextStyle(
    fontFamily = PretendardFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 13.sp,
    lineHeight = 20.sp,
)

private val LabelMediumStyle = TextStyle(
    fontFamily = PretendardFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    lineHeight = 18.sp,
)

@Composable
fun ProfileScreen(
    onSettingsClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onBadgesClick: () -> Unit = {},
    onRecentMissionClick: () -> Unit = {},
    onArchiveClick: () -> Unit = {},
) = FitDesign(compensateStatusBar = false) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50),
    ) {
        Text(
            text = "마이페이지",
            style = ProfileTitleStyle,
            color = Gray900,
            modifier = Modifier
                .offset(x = 16.dp, y = 69.dp)
                .size(width = 78.dp, height = 28.dp),
        )

        Box(
            modifier = Modifier
                .offset(x = 343.dp, y = 48.dp)
                .size(44.dp)
                .clickable(onClick = onSettingsClick),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_profile_settings),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
        }

        ProfileHeader(
            onEditProfileClick = onEditProfileClick,
            modifier = Modifier
                .offset(x = 150.5.dp, y = 126.dp)
                .size(width = 93.dp, height = 172.dp),
        )

        LevelCard(
            modifier = Modifier
                .offset(x = 16.dp, y = 322.dp)
                .size(width = 362.dp, height = 89.dp),
        )

        WeeklyMissionCard(
            modifier = Modifier
                .offset(x = 16.dp, y = 427.dp)
                .size(width = 362.dp, height = 121.dp),
        )

        ProfileMenuCard(
            badgeCount = 5,
            onBadgesClick = onBadgesClick,
            onRecentMissionClick = onRecentMissionClick,
            onArchiveClick = onArchiveClick,
            modifier = Modifier
                .offset(x = 16.dp, y = 564.dp)
                .size(width = 362.dp, height = 151.dp),
        )
    }
}

@Composable
private fun ProfileHeader(
    onEditProfileClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        AvatarImage(modifier = Modifier.size(93.dp))
        Text(
            text = "다민 님",
            style = HeadingStyle,
            color = Gray900,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .offset(y = 104.dp)
                .size(width = 93.dp, height = 30.dp),
        )
        Box(
            modifier = Modifier
                .offset(y = 140.dp)
                .size(width = 93.dp, height = 32.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, Gray300, RoundedCornerShape(24.dp))
                .clickable(onClick = onEditProfileClick),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "내 정보 수정",
                style = BodySmallStyle,
                color = Gray500,
            )
        }
    }
}

@Composable
private fun AvatarImage(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.img_profile_avatar),
        contentDescription = null,
        modifier = modifier,
    )
}

@Composable
private fun ProfileCard(
    modifier: Modifier = Modifier,
    radius: Int = 20,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .softShadow(
                color = Color(0xFF0F172A).copy(alpha = 0.01f),
                offsetY = 8.dp,
                blur = 24.dp,
                cornerRadius = radius.dp,
            )
            .clip(RoundedCornerShape(radius.dp))
            .background(White),
        content = content,
    )
}

@Composable
private fun LevelCard(modifier: Modifier = Modifier) {
    ProfileCard(modifier = modifier) {
        Text(
            text = "대화 진행 레벨",
            style = BodyLargeMediumStyle,
            color = Gray800,
            modifier = Modifier
                .offset(x = 16.dp, y = 12.dp)
                .size(width = 110.dp, height = 24.dp),
        )
        Text(
            text = "Lv.2",
            style = LabelMediumStyle,
            color = Primary600,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .offset(x = 16.dp, y = 41.dp)
                .size(width = 23.dp, height = 22.dp),
        )
        Text(
            text = "30 /100XP",
            style = LabelMediumStyle,
            color = Gray400,
            modifier = Modifier
                .offset(x = 279.dp, y = 41.dp)
                .size(width = 67.dp, height = 22.dp),
        )
        Box(
            modifier = Modifier
                .offset(x = 16.dp, y = 67.dp)
                .size(width = 327.dp, height = 10.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Primary100),
        )
        Box(
            modifier = Modifier
                .offset(x = 16.dp, y = 67.dp)
                .size(width = 111.dp, height = 10.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Primary600),
        )
    }
}

@Composable
private fun WeeklyMissionCard(modifier: Modifier = Modifier) {
    ProfileCard(modifier = modifier) {
        Row(
            modifier = Modifier
                .offset(x = 16.dp, y = 12.dp)
                .size(width = 136.dp, height = 25.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = "이번 주 연속 미션",
                style = BodyLargeMediumStyle,
                color = Gray800,
                modifier = Modifier.size(width = 109.dp, height = 24.dp),
            )
            Image(
                painter = painterResource(R.drawable.ic_profile_fire),
                contentDescription = null,
                modifier = Modifier.size(25.dp),
            )
        }

        val days = listOf("일", "월", "화", "수", "목", "금", "토")
        val completed = setOf(0, 1, 2, 5, 6)
        days.forEachIndexed { index, day ->
            MissionDay(
                day = day,
                completed = index in completed,
                modifier = Modifier
                    .offset(x = (16 + index * 50).dp, y = 53.dp)
                    .size(width = 26.dp, height = 56.dp),
            )
        }
    }
}

@Composable
private fun MissionDay(
    day: String,
    completed: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        Text(
            text = day,
            style = BodyMediumStyle,
            color = Gray600,
            textAlign = TextAlign.Center,
            modifier = Modifier.size(width = 26.dp, height = 22.dp),
        )
        CheckCircle(
            completed = completed,
            modifier = Modifier
                .offset(y = 30.dp)
                .size(26.dp),
        )
    }
}

@Composable
private fun CheckCircle(
    completed: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(if (completed) Primary600 else Gray100),
        contentAlignment = Alignment.Center,
    ) {
        if (completed) {
            Canvas(modifier = Modifier.size(15.dp)) {
                drawLine(
                    color = Primary50,
                    start = Offset(size.width * 0.08f, size.height * 0.52f),
                    end = Offset(size.width * 0.4f, size.height * 0.82f),
                    strokeWidth = 2.dp.toPx(),
                )
                drawLine(
                    color = Primary50,
                    start = Offset(size.width * 0.4f, size.height * 0.82f),
                    end = Offset(size.width * 0.94f, size.height * 0.18f),
                    strokeWidth = 2.dp.toPx(),
                )
            }
        }
    }
}

@Composable
private fun ProfileMenuCard(
    badgeCount: Int,
    onBadgesClick: () -> Unit,
    onRecentMissionClick: () -> Unit,
    onArchiveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ProfileCard(modifier = modifier) {
        ProfileMenuRow(
            title = "획득한 배지",
            trailing = badgeCount.toString(),
            onClick = onBadgesClick,
            modifier = Modifier
                .offset(x = 16.dp, y = 10.dp)
                .size(width = 345.dp, height = 43.dp),
        )
        ProfileMenuRow(
            title = "최근 활동 요약",
            onClick = onRecentMissionClick,
            modifier = Modifier
                .offset(x = 16.dp, y = 53.dp)
                .size(width = 346.dp, height = 44.dp),
        )
        ProfileMenuRow(
            title = "보관함",
            onClick = onArchiveClick,
            modifier = Modifier
                .offset(x = 16.dp, y = 97.dp)
                .size(width = 346.dp, height = 44.dp),
        )
    }
}

@Composable
private fun ProfileMenuRow(
    title: String,
    trailing: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.clickable(onClick = onClick),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = title,
            style = BodyLargeMediumStyle,
            color = Gray800,
            modifier = Modifier.size(width = 150.dp, height = 24.dp),
        )
        if (trailing != null) {
            Text(
                text = trailing,
                style = BodyLargeMediumStyle,
                color = Gray900,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 60.dp),
            )
            Image(
                painter = painterResource(R.drawable.ic_profile_medal),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 38.dp)
                    .size(22.dp),
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Gray600,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(width = 44.dp, height = 44.dp)
                .padding(horizontal = 7.dp, vertical = 6.dp),
        )
    }
}

@Preview(showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun ProfileScreenPreview() {
    TalkQQuestTheme {
        ProfileScreen()
    }
}






