package com.talkqquest.app.feature.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.talkqquest.app.core.designsystem.FitDesign
import com.talkqquest.app.core.designsystem.Gray50
import com.talkqquest.app.core.designsystem.Gray500
import com.talkqquest.app.core.designsystem.Gray700
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
import com.talkqquest.app.core.designsystem.softShadow

@Composable
fun ProfileSupportScreen(
    onBack: () -> Unit = {},
) = FitDesign(contentAlignment = Alignment.TopCenter) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50),
    ) {
        ProfileSimpleTopBar(title = "문의하기", onBack = onBack)
        Text(
            text = "무엇을 도와드릴까요?",
            style = TqType.TitleL.copy(fontWeight = FontWeight.SemiBold),
            color = Color(0xFF1E293B),
            modifier = Modifier
                .offset(x = 20.dp, y = 111.dp)
                .size(width = 152.dp, height = 28.dp),
        )
        val supportGroup = rememberMenuRippleGroup()
        Box(
            modifier = Modifier
                .offset(x = 16.dp, y = 155.dp)
                .size(width = 362.dp, height = 143.dp)
                .softShadow(
                    color = Color(0xFF0F172A).copy(alpha = 0.01f),
                    offsetY = 8.dp,
                    blur = 24.dp,
                    cornerRadius = 16.dp,
                )
                .clip(RoundedCornerShape(16.dp))
                .background(White)
                .onGloballyPositioned {
                    val top = it.positionInRoot().y
                    supportGroup.setEdges(top, top + it.size.height, fillFirst = false, fillLast = true)
                },
        ) {
            MenuRippleLayer(supportGroup, Modifier.matchParentSize())
            Text(
                text = "고객 지원",
                style = TqType.BodyM,
                color = Gray500,
                modifier = Modifier
                    .offset(x = 16.dp, y = 12.dp)
                    .size(width = 330.dp, height = 22.dp),
            )
            SupportRow(
                title = "자주 묻는 질문",
                modifier = Modifier
                    .offset(y = 46.dp)
                    .size(width = 362.dp, height = 44.dp),
                contentModifier = Modifier
                    .offset(x = 16.dp)
                    .size(width = 330.dp, height = 44.dp),
                group = supportGroup,
            )
            SupportRow(
                title = "이메일 문의",
                trailing = "talkqquest@naver.com",
                modifier = Modifier
                    .offset(y = 90.dp)
                    .size(width = 362.dp, height = 44.dp),
                contentModifier = Modifier
                    .offset(x = 16.dp)
                    .size(width = 330.dp, height = 44.dp),
                group = supportGroup,
            )
        }
    }
}
@Composable
private fun SupportRow(
    title: String,
    trailing: String? = null,
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier,
    group: MenuRippleGroupState? = null,
) {
    TqAnchoredMenuRow(modifier = modifier, group = group, onClick = {}) { anchor ->
        val titleLayout = rememberMenuRowTextLayoutCallback(anchor, "title", title, TqType.BodyL)
        val trailingLayout = trailing?.let {
            rememberMenuRowTextLayoutCallback(anchor, "trailing", it, TqType.BodyM)
        }
        Row(
            modifier = contentModifier,
            verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = TqType.BodyL,
            color = Gray800,
            onTextLayout = titleLayout,
            modifier = Modifier
                .menuRowTextRippleAnchor(anchor, "title")
                .weight(1f),
        )
        if (trailing != null) {
            Text(
                text = trailing,
                style = TqType.BodyM,
                color = Gray500,
                onTextLayout = trailingLayout!!,
                modifier = Modifier.menuRowTextRippleAnchor(anchor, "trailing"),
            )
            Spacer(Modifier.size(width = 10.dp, height = 1.dp))
        }
        Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Gray700)
        }
    }
}

@Preview(showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun ProfileSupportScreenPreview() {
    TalkQQuestTheme {
        ProfileSupportScreen()
    }
}


