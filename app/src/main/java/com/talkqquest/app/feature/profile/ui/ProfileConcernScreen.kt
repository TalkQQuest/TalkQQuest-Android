package com.talkqquest.app.feature.profile.ui

import androidx.compose.foundation.background
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.talkqquest.app.core.designsystem.figma
import com.talkqquest.app.core.designsystem.FitDesign
import com.talkqquest.app.core.designsystem.Gray50
import com.talkqquest.app.core.designsystem.Gray500
import com.talkqquest.app.core.designsystem.Gray600
import com.talkqquest.app.core.designsystem.Gray800
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.White
import com.talkqquest.app.core.designsystem.component.MenuRippleGroupState
import com.talkqquest.app.core.designsystem.component.MenuRippleLayer
import com.talkqquest.app.core.designsystem.component.TqAnchoredMenuRow
import com.talkqquest.app.core.designsystem.component.menuRowTextRippleAnchor
import com.talkqquest.app.core.designsystem.component.rememberMenuRippleGroup
import com.talkqquest.app.core.designsystem.component.rememberMenuRowTextLayoutCallback

@Composable
fun ProfileConcernScreen(
    nickname: String = "사용자",
    personalityType: String? = null,
    difficultSituations: List<String> = emptyList(),
    purpose: List<String> = emptyList(),
    onBack: () -> Unit = {},
    onPersonalityClick: () -> Unit = {},
    onDifficultyClick: () -> Unit = {},
    onGoalClick: () -> Unit = {},
) = FitDesign(contentAlignment = Alignment.TopCenter) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50),
    ) {
        ProfileSimpleTopBar(title = "대화 고민", onBack = onBack)
        val concernGroup = rememberMenuRippleGroup()
        Box(
            modifier = Modifier
                .offset(x = 16.dp, y = 114.dp)
                .size(width = 361.dp, height = 226.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(White)
                .onGloballyPositioned {
                    val top = it.positionInRoot().y
                    concernGroup.setEdges(top, top + it.size.height, fillFirst = true, fillLast = true)
                },
        ) {
            MenuRippleLayer(concernGroup, Modifier.matchParentSize())
            ProfileConcernRow(
                label = "평소 대화할 때 ${nickname}님의 모습은",
                value = personalityType.toPersonalityDisplay(),
                onClick = onPersonalityClick,
                modifier = Modifier
                    .offset(y = 2.dp)
                    .size(width = 361.dp, height = 74.dp),
                contentModifier = Modifier
                    .offset(x = 16.dp, y = 12.dp)
                    .size(width = 345.dp, height = 50.dp),
                group = concernGroup,
            )
            ProfileConcernRow(
                label = "대화할 때 가장 어려운 점은 뭔가요?",
                value = difficultSituations.toConcernDisplay("아직 선택한 어려운 점이 없어요"),
                onClick = onDifficultyClick,
                modifier = Modifier
                    .offset(y = 76.dp)
                    .size(width = 361.dp, height = 74.dp),
                contentModifier = Modifier
                    .offset(x = 16.dp, y = 12.dp)
                    .size(width = 345.dp, height = 50.dp),
                group = concernGroup,
            )
            ProfileConcernRow(
                label = "어떤 대화를 연습하고 싶으신가요?",
                value = purpose.toConcernDisplay("아직 선택한 연습 목표가 없어요"),
                onClick = onGoalClick,
                modifier = Modifier
                    .offset(y = 150.dp)
                    .size(width = 361.dp, height = 74.dp),
                contentModifier = Modifier
                    .offset(x = 16.dp, y = 12.dp)
                    .size(width = 345.dp, height = 50.dp),
                group = concernGroup,
            )
        }
    }
}

@Composable
private fun ProfileConcernRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    group: MenuRippleGroupState? = null,
) {
    TqAnchoredMenuRow(modifier = modifier, group = group, onClick = onClick) { anchor ->
        val labelLayout = rememberMenuRowTextLayoutCallback(anchor, "label", label, TqType.BodyM.figma())
        val valueStyle = TqType.BodyL.figma().copy(fontWeight = FontWeight.Medium)
        val valueLayout = rememberMenuRowTextLayoutCallback(anchor, "value", value, valueStyle)
        Row(
            modifier = contentModifier,
            verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.size(width = 304.dp, height = 50.dp),
        ) {
            Text(
                text = label,
                style = TqType.BodyM.figma(),
                color = Gray500,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                onTextLayout = labelLayout,
                modifier = Modifier
                    .menuRowTextRippleAnchor(anchor, "label")
                    .size(width = 304.dp, height = 22.dp),
            )
            Text(
                text = value,
                style = valueStyle,
                color = Gray800,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                onTextLayout = valueLayout,
                modifier = Modifier
                    .menuRowTextRippleAnchor(anchor, "value")
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
}

private fun String?.toPersonalityDisplay(): String = when (this) {
    "introvert" -> "조용하고 신중해요"
    "extrovert" -> "사교적이고 활발한 편이에요"
    "ambivert" -> "상황에 따라 달라요"
    else -> "아직 선택한 모습이 없어요"
}

private fun List<String>.toConcernDisplay(emptyText: String): String =
    filter { it.isNotBlank() }.joinToString("·").ifBlank { emptyText }

@Preview(showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun ProfileConcernScreenPreview() {
    TalkQQuestTheme {
        ProfileConcernScreen()
    }
}
