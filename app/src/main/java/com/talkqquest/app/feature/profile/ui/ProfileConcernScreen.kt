package com.talkqquest.app.feature.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.talkqquest.app.core.designsystem.FitDesign
import com.talkqquest.app.core.designsystem.Gray50
import com.talkqquest.app.core.designsystem.Gray500
import com.talkqquest.app.core.designsystem.Gray600
import com.talkqquest.app.core.designsystem.Gray800
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.White

@Composable
fun ProfileConcernScreen(
    onBack: () -> Unit = {},
    onPersonalityClick: () -> Unit = {},
    onDifficultyClick: () -> Unit = {},
    onGoalClick: () -> Unit = {},
) = FitDesign {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50),
    ) {
        ProfileSimpleTopBar(title = "대화 고민", onBack = onBack)
        Box(
            modifier = Modifier
                .offset(x = 16.dp, y = 114.dp)
                .size(width = 361.dp, height = 226.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(White),
        ) {
            ProfileConcernRow(
                label = "평소 대화할 때 소다123님의 모습은",
                value = "사교적이고 활발한 편이에요",
                onClick = onPersonalityClick,
                modifier = Modifier
                    .offset(x = 16.dp, y = 14.dp)
                    .size(width = 345.dp, height = 50.dp),
            )
            ProfileConcernRow(
                label = "대화할 때 가장 어려운 점은 뭔가요?",
                value = "주제고민·말문막힘",
                onClick = onDifficultyClick,
                modifier = Modifier
                    .offset(x = 16.dp, y = 88.dp)
                    .size(width = 345.dp, height = 50.dp),
            )
            ProfileConcernRow(
                label = "어떤 대화를 연습하고 싶으신가요?",
                value = "침묵 줄이기·친해지는 대화",
                onClick = onGoalClick,
                modifier = Modifier
                    .offset(x = 16.dp, y = 162.dp)
                    .size(width = 345.dp, height = 50.dp),
            )
        }
    }
}

@Composable
private fun ProfileConcernRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Row(
        modifier = modifier.clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.size(width = 304.dp, height = 50.dp),
        ) {
            Text(
                text = label,
                style = TqType.BodyM,
                color = Gray500,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.size(width = 304.dp, height = 22.dp),
            )
            Text(
                text = value,
                style = TqType.BodyL.copy(fontWeight = FontWeight.Medium),
                color = Gray800,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .offset(y = 4.dp)
                    .size(width = 304.dp, height = 24.dp),
            )
        }
        Box(
            modifier = Modifier.size(width = 41.dp, height = 44.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Gray600,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

@Preview(showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun ProfileConcernScreenPreview() {
    TalkQQuestTheme {
        ProfileConcernScreen()
    }
}
