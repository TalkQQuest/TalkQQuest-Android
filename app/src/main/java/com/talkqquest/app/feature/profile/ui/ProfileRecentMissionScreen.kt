package com.talkqquest.app.feature.profile.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.talkqquest.app.R
import com.talkqquest.app.core.designsystem.FitDesign
import com.talkqquest.app.core.designsystem.Gray100
import com.talkqquest.app.core.designsystem.Gray200
import com.talkqquest.app.core.designsystem.Gray300
import com.talkqquest.app.core.designsystem.Gray50
import com.talkqquest.app.core.designsystem.Gray500
import com.talkqquest.app.core.designsystem.Gray600
import com.talkqquest.app.core.designsystem.Gray700
import com.talkqquest.app.core.designsystem.Gray800
import com.talkqquest.app.core.designsystem.Gray900
import com.talkqquest.app.core.designsystem.PretendardFamily
import com.talkqquest.app.core.designsystem.Primary600
import com.talkqquest.app.core.designsystem.Success
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.White

private enum class ProfileActivityType {
    Mission,
    Conversation,
    Sentence,
    Report,
}

private data class ProfileActivityItem(
    val type: ProfileActivityType,
    val title: String,
    val status: String,
    val date: String? = null,
    val difficulty: String? = null,
    val category: String? = null,
    val minutes: Int? = null,
    val xp: Int? = null,
)

private val RecentTitleStyle = TextStyle(
    fontFamily = PretendardFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp,
)

private val RecentBodyMediumStyle = TextStyle(
    fontFamily = PretendardFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 16.sp,
    lineHeight = 24.sp,
)

private val RecentSummaryValueStyle = TextStyle(
    fontFamily = PretendardFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 18.sp,
    lineHeight = 28.sp,
)

private val RecentCaptionStyle = TextStyle(
    fontFamily = PretendardFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    lineHeight = 18.sp,
)

private val RecentLabelStyle = TextStyle(
    fontFamily = PretendardFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    lineHeight = 18.sp,
)

private val RecentActivities = listOf(
    ProfileActivityItem(
        type = ProfileActivityType.Mission,
        title = "처음 보는 사람에게 짧게 인사하기",
        status = "",
        difficulty = "쉬움",
        category = "짧은 대화",
        minutes = 2,
        xp = 20,
    ),
    ProfileActivityItem(
        type = ProfileActivityType.Conversation,
        title = "처음 보는 사람에게 짧게 인사하기",
        status = "대화 완료",
        date = "2026.08.20",
    ),
    ProfileActivityItem(
        type = ProfileActivityType.Sentence,
        title = "“그렇군요! 저도 편해서 놀랐 ...",
        status = "문장 저장",
        date = "2026.08.20",
    ),
    ProfileActivityItem(
        type = ProfileActivityType.Report,
        title = "처음 보는 사람에게 짧게 인사하기",
        status = "리포트 열람",
        date = "2026.08.20",
    ),
)

@Composable
fun ProfileRecentMissionScreen(
    onBack: () -> Unit = {},
) = FitDesign {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50),
    ) {
        ProfileSimpleTopBar(
            title = "최근 활동 요약",
            onBack = onBack,
            backIconStartPadding = 0.dp,
        )

        RecentSummaryCard(
            modifier = Modifier
                .offset(x = 16.dp, y = 104.dp)
                .size(width = 361.dp, height = 80.dp),
        )

        Text(
            text = "최근 활동",
            style = RecentBodyMediumStyle,
            color = Gray700,
            modifier = Modifier
                .offset(x = 16.dp, y = 208.dp)
                .size(width = 361.dp, height = 24.dp),
        )

        Column(
            modifier = Modifier
                .offset(x = 16.dp, y = 248.dp)
                .size(width = 361.dp, height = 349.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            RecentActivities.forEach { activity ->
                RecentActivitySummaryCard(activity = activity)
            }
        }
    }
}

@Composable
private fun RecentSummaryCard(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(White),
    ) {
        SummaryMetric(
            label = "최근 완료한 미션",
            value = "5",
            modifier = Modifier
                .offset(x = 35.dp, y = 12.dp)
                .size(width = 105.dp, height = 56.dp),
        )
        Box(
            modifier = Modifier
                .offset(x = 180.dp, y = 17.dp)
                .size(width = 1.dp, height = 46.dp)
                .background(Gray200),
        )
        SummaryMetric(
            label = "획득한 경험치",
            value = "+ 160 XP",
            modifier = Modifier
                .offset(x = 221.dp, y = 12.dp)
                .size(width = 105.dp, height = 56.dp),
        )
    }
}

@Composable
private fun SummaryMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier.size(width = 105.dp, height = 24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                style = RecentTitleStyle,
                color = Gray500,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
        Box(
            modifier = Modifier
                .offset(y = 28.dp)
                .size(width = 105.dp, height = 28.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = value,
                style = RecentSummaryValueStyle,
                color = Primary600,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun RecentActivitySummaryCard(
    activity: ProfileActivityItem,
    modifier: Modifier = Modifier,
) {
    val isMission = activity.type == ProfileActivityType.Mission
    Row(
        modifier = modifier
            .size(width = 361.dp, height = if (isMission) 85.dp else 72.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(White)
            .clickable { },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ActivityIcon(
            type = activity.type,
            modifier = Modifier
                .padding(start = 16.dp)
                .size(48.dp),
        )
        Column(
            modifier = Modifier
                .padding(start = if (isMission) 8.dp else 12.dp)
                .width(if (isMission) 236.dp else 214.dp),
            verticalArrangement = Arrangement.spacedBy(if (isMission) 7.dp else 0.dp),
        ) {
            Text(
                text = activity.title,
                style = RecentBodyMediumStyle,
                color = Gray900,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (isMission) {
                Row(
                    modifier = Modifier.height(26.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ActivityChip(
                        text = activity.difficulty.orEmpty(),
                        textColor = Success,
                        backgroundColor = Color(0xFFE3F4E0),
                        width = 45.dp,
                    )
                    ActivityChip(
                        text = activity.category.orEmpty(),
                        textColor = Gray500,
                        backgroundColor = Gray100,
                        width = 69.dp,
                    )
                    ActivityMeta(text = "${activity.minutes ?: 0}분")
                    ActivityMeta(text = "+ ${activity.xp ?: 0}XP")
                }
            } else {
                Row(
                    modifier = Modifier.height(22.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = activity.status,
                        style = RecentCaptionStyle,
                        color = Gray500,
                        maxLines = 1,
                    )
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .size(width = 1.dp, height = 9.dp)
                            .background(Gray300),
                    )
                    Text(
                        text = activity.date.orEmpty(),
                        style = RecentCaptionStyle,
                        color = Gray500,
                        maxLines = 1,
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .padding(start = if (isMission) 3.dp else 21.dp)
                .size(44.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_forward_chevron),
                contentDescription = null,
                tint = Gray600,
            )
        }
    }
}

@Composable
private fun ActivityIcon(
    type: ProfileActivityType,
    modifier: Modifier = Modifier,
) {
    val iconRes = when (type) {
        ProfileActivityType.Mission -> R.drawable.img_profile_activity_mission
        ProfileActivityType.Conversation -> R.drawable.img_profile_activity_conversation
        ProfileActivityType.Sentence -> R.drawable.img_profile_activity_sentence
        ProfileActivityType.Report -> R.drawable.img_profile_activity_report
    }
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(40.dp),
        )
    }
}

@Composable
private fun ActivityChip(
    text: String,
    textColor: Color,
    backgroundColor: Color,
    width: Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(width = width, height = 26.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = RecentLabelStyle,
            color = textColor,
            maxLines = 1,
        )
    }
}

@Composable
private fun ActivityMeta(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = RecentCaptionStyle,
        color = Gray500,
        maxLines = 1,
        modifier = modifier,
    )
}

@Composable
internal fun ProfileSimpleTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    backIconStartPadding: Dp = 0.dp,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(92.dp),
    ) {
        Box(
            modifier = Modifier
                .offset(x = backIconStartPadding, y = 48.dp)
                .size(44.dp)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_back_chevron),
                contentDescription = null,
                tint = Gray500,
                modifier = Modifier.size(24.dp),
            )
        }
        Text(
            text = title,
            style = RecentTitleStyle.copy(letterSpacing = (-0.01).em),
            color = Gray800,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 58.dp)
                .height(24.dp),
        )
    }
}

@Preview(showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun ProfileRecentMissionScreenPreview() {
    TalkQQuestTheme {
        ProfileRecentMissionScreen()
    }
}






