package com.talkqquest.app.feature.profile.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.talkqquest.app.R
import com.talkqquest.app.core.designsystem.FitDesign
import com.talkqquest.app.core.designsystem.Gray100
import com.talkqquest.app.core.designsystem.Gray200
import com.talkqquest.app.core.designsystem.Gray300
import com.talkqquest.app.core.designsystem.Gray400
import com.talkqquest.app.core.designsystem.Gray50
import com.talkqquest.app.core.designsystem.Gray500
import com.talkqquest.app.core.designsystem.Gray600
import com.talkqquest.app.core.designsystem.Gray700
import com.talkqquest.app.core.designsystem.Gray800
import com.talkqquest.app.core.designsystem.Primary100
import com.talkqquest.app.core.designsystem.Primary600
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.White
import com.talkqquest.app.core.designsystem.softShadow

@Composable
fun ProfileScreen(
    onSettingsClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onBadgesClick: () -> Unit = {},
    onRecentMissionClick: () -> Unit = {},
    onArchiveClick: () -> Unit = {},
) = FitDesign {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        Spacer(Modifier.height(58.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "\uB9C8\uC774\uD398\uC774\uC9C0",
                style = TqType.TitleL.copy(fontWeight = FontWeight.Bold),
                color = Gray800,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                tint = Gray500,
                modifier = Modifier
                    .size(25.dp)
                    .clickable(onClick = onSettingsClick),
            )
        }
        Spacer(Modifier.height(34.dp))
        ProfileHeader(onEditProfileClick = onEditProfileClick)
        Spacer(Modifier.height(24.dp))
        LevelCard()
        Spacer(Modifier.height(16.dp))
        WeeklyMissionCard()
        Spacer(Modifier.height(16.dp))
        ProfileMenuCard(
            badgeCount = 5,
            onBadgesClick = onBadgesClick,
            onRecentMissionClick = onRecentMissionClick,
            onArchiveClick = onArchiveClick,
        )
        Spacer(Modifier.height(112.dp))
    }
}

@Composable
private fun ProfileHeader(onEditProfileClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AvatarImage(modifier = Modifier.size(94.dp))
        Spacer(Modifier.height(12.dp))
        Text(
            text = "\uB2E4\uBBFC \uB2D8",
            style = TqType.HeadingM.copy(fontWeight = FontWeight.Bold),
            color = Gray800,
        )
        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(Gray50)
                .clickable(onClick = onEditProfileClick)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "\uB0B4 \uC815\uBCF4 \uC218\uC815",
                style = TqType.LabelM,
                color = Gray500,
            )
        }
    }
}

@Composable
private fun AvatarImage(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        drawCircle(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF806FF8), Primary600),
                start = Offset(0f, 0f),
                end = Offset(size.width, size.height),
            ),
        )
        drawCircle(color = Color(0xFFE9E6FF), radius = size.minDimension * 0.31f, center = Offset(size.width / 2f, size.height * 0.47f))
        drawOval(
            color = Color(0xFFE2DEFF),
            topLeft = Offset(size.width * 0.22f, size.height * 0.52f),
            size = Size(size.width * 0.56f, size.height * 0.44f),
        )
        drawCircle(color = Gray600, radius = 2.6.dp.toPx(), center = Offset(size.width * 0.42f, size.height * 0.43f))
        drawCircle(color = Gray600, radius = 2.6.dp.toPx(), center = Offset(size.width * 0.58f, size.height * 0.43f))
    }
}

@Composable
private fun ProfileCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .softShadow(
                color = Color.Black.copy(alpha = 0.03f),
                offsetY = 8.dp,
                blur = 24.dp,
                cornerRadius = 18.dp,
            )
            .clip(RoundedCornerShape(18.dp))
            .background(White)
            .padding(16.dp),
        content = content,
    )
}

@Composable
private fun LevelCard() {
    ProfileCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "\uB300\uD654 \uC9C4\uD589 \uB808\uBCA8",
            style = TqType.TitleL.copy(fontWeight = FontWeight.Bold),
            color = Gray800,
        )
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Lv.2", style = TqType.BodyS, color = Primary600, modifier = Modifier.weight(1f))
            Text(text = "30 /100XP", style = TqType.BodyS, color = Gray400)
        }
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(9.dp)
                .clip(RoundedCornerShape(50))
                .background(Primary100),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.34f)
                    .height(9.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Primary600),
            )
        }
    }
}

@Composable
private fun WeeklyMissionCard() {
    ProfileCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "\uC774\uBC88 \uC8FC \uC5F0\uC18D \uBBF8\uC158 \uD83D\uDD25",
            style = TqType.TitleL.copy(fontWeight = FontWeight.Bold),
            color = Gray800,
        )
        Spacer(Modifier.height(20.dp))
        val days = listOf("\uC77C", "\uC6D4", "\uD654", "\uC218", "\uBAA9", "\uAE08", "\uD1A0")
        val completed = setOf(0, 1, 2, 5, 6)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            days.forEachIndexed { index, day ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = day, style = TqType.BodyS, color = Gray600)
                    Spacer(Modifier.height(9.dp))
                    CheckCircle(completed = index in completed)
                }
            }
        }
    }
}

@Composable
private fun CheckCircle(completed: Boolean) {
    Box(
        modifier = Modifier
            .size(27.dp)
            .clip(CircleShape)
            .background(if (completed) Primary600 else Gray100),
        contentAlignment = Alignment.Center,
    ) {
        if (completed) {
            Canvas(modifier = Modifier.size(15.dp)) {
                drawLine(White, Offset(size.width * 0.08f, size.height * 0.52f), Offset(size.width * 0.4f, size.height * 0.82f), strokeWidth = 3.dp.toPx())
                drawLine(White, Offset(size.width * 0.4f, size.height * 0.82f), Offset(size.width * 0.94f, size.height * 0.18f), strokeWidth = 3.dp.toPx())
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
) {
    ProfileCard(modifier = Modifier.fillMaxWidth()) {
        ProfileMenuRow(
            title = "\uD68D\uB4DD\uD55C \uBC30\uC9C0",
            trailing = "$badgeCount \uD83C\uDFC5",
            onClick = onBadgesClick,
        )
        Spacer(Modifier.height(20.dp))
        ProfileMenuRow(
            title = "\uCD5C\uADFC \uBBF8\uC158 \uC694\uC57D",
            onClick = onRecentMissionClick,
        )
        Spacer(Modifier.height(20.dp))
        ProfileMenuRow(
            title = "\uBCF4\uAD00\uD568",
            onClick = onArchiveClick,
        )
    }
}

@Composable
private fun ProfileMenuRow(
    title: String,
    trailing: String? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = TqType.BodyL.copy(fontWeight = FontWeight.SemiBold),
            color = Gray800,
            modifier = Modifier.weight(1f),
        )
        if (trailing != null) {
            Text(text = trailing, style = TqType.TitleL.copy(fontWeight = FontWeight.Bold), color = Gray800)
            Spacer(Modifier.size(8.dp))
        }
        Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Gray700, modifier = Modifier.size(26.dp))
    }
}

@Preview(showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun ProfileScreenPreview() {
    TalkQQuestTheme {
        ProfileScreen()
    }
}