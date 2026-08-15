package com.talkqquest.app.feature.profile.ui

import android.net.Uri

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.mutableStateOf
import coil3.compose.AsyncImage
import com.talkqquest.app.R
import com.talkqquest.app.core.designsystem.FitDesign
import com.talkqquest.app.core.designsystem.Gray50
import com.talkqquest.app.core.designsystem.Gray500
import com.talkqquest.app.core.designsystem.Gray600
import com.talkqquest.app.core.designsystem.Gray800
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.component.MenuRippleGroupState
import com.talkqquest.app.core.designsystem.component.TqAnchoredMenuRow
import com.talkqquest.app.core.designsystem.component.menuRowTextRippleAnchor
import com.talkqquest.app.core.designsystem.component.rememberMenuRippleGroup
import com.talkqquest.app.core.designsystem.component.rememberMenuRowTextLayoutCallback

@Composable
fun ProfileInfoScreen(
    nickname: String = "소다123",
    avatarUrl: String? = null,
    connectedAccount: String = "이메일 정보 없음",
    onBack: () -> Unit = {},
    onNicknameClick: () -> Unit = {},
    onAvatarClick: (Uri) -> Unit = {},
    onPasswordClick: () -> Unit = {},
    onConnectedAccountClick: () -> Unit = {},
    onConcernClick: () -> Unit = {},
    isEmailMember: Boolean = true,
) = FitDesign(contentAlignment = Alignment.TopCenter) {
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        if (uri != null) onAvatarClick(uri)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50),
    ) {
        ProfileSimpleTopBar(title = "내 정보", onBack = onBack)
        Row(
            modifier = Modifier
                .offset(x = 16.dp, y = 116.dp)
                .size(width = 202.dp, height = 60.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .profileCircleClick { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center,
            ) {
                if (avatarUrl.isNullOrBlank()) {
                    Image(
                        painter = painterResource(R.drawable.img_profile_avatar),
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
                    )
                } else {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
                        error = painterResource(R.drawable.img_profile_avatar),
                        placeholder = painterResource(R.drawable.img_profile_avatar),
                    )
                }
            }
            Spacer(Modifier.size(width = 12.dp, height = 1.dp))
            NicknamePillClick(
                nickname = nickname,
                onClick = onNicknameClick,
            )
        }

        Box(
            modifier = Modifier
                .offset(y = 199.dp)
                .size(width = 393.dp, height = if (isEmailMember) 138.dp else 92.dp),
        ) {
            val infoGroup = rememberMenuRippleGroup()
            ProfileInfoRow(
                title = "연결된 계정",
                trailing = connectedAccount,
                modifier = Modifier
                    .offset(y = 0.dp)
                    .size(width = 393.dp, height = 46.dp),
                contentModifier = Modifier
                    .offset(x = 16.dp, y = 1.dp)
                    .size(width = 371.dp, height = 44.dp),
                onClick = onConnectedAccountClick,
                group = infoGroup,
            )
            if (isEmailMember) {
                ProfileInfoRow(
                    title = "비밀번호 변경",
                modifier = Modifier
                    .offset(y = 46.dp)
                    .size(width = 393.dp, height = 46.dp),
                    contentModifier = Modifier
                        .offset(x = 16.dp, y = 1.dp)
                        .size(width = 371.dp, height = 44.dp),
                    onClick = onPasswordClick,
                    group = infoGroup,
                )
            }
            ProfileInfoRow(
                title = "${nickname}님의 대화 고민",
                titleWidth = null,
                modifier = Modifier
                    .offset(y = if (isEmailMember) 92.dp else 46.dp)
                    .size(width = 393.dp, height = 46.dp),
                contentModifier = Modifier
                    .offset(x = 16.dp, y = 1.dp)
                    .size(width = 371.dp, height = 44.dp),
                onClick = onConcernClick,
                group = infoGroup,
            )
        }
    }
}

@Composable
private fun NicknamePillClick(
    nickname: String,
    onClick: () -> Unit,
) {
    val density = LocalDensity.current
    val interactionSource = remember { MutableInteractionSource() }
    var contentSize by remember { mutableStateOf(IntSize.Zero) }
    val padH = 12.dp
    val padV = 9.dp
    // 글자·연필은 피그마 위치 그대로. 리플만 측정된 콘텐츠(글자+연필) 경계에 상하좌우 동일 패딩으로 감싼다.

    Box(modifier = Modifier.size(width = 130.dp, height = 44.dp)) {
        Row(
            modifier = Modifier.onGloballyPositioned { contentSize = it.size },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "$nickname 님",
                style = TqType.HeadingM.copy(fontWeight = FontWeight.SemiBold),
                color = Color(0xFF1E293B),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 109.dp),
            )
            Spacer(Modifier.width(6.dp))
            Image(
                painter = painterResource(R.drawable.ic_profile_info_edit),
                contentDescription = null,
                modifier = Modifier.size(15.dp),
            )
        }
        if (contentSize != IntSize.Zero) {
            Box(
                modifier = Modifier
                    .offset(x = -padH, y = -padV)
                    .size(
                        width = with(density) { contentSize.width.toDp() } + padH * 2,
                        height = with(density) { contentSize.height.toDp() } + padV * 2,
                    )
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(percent = 50))
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick,
                    )
                    .indication(interactionSource, ripple(bounded = true)),
            )
        }
    }
}

@Composable
private fun ProfileInfoRow(
    title: String,
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier,
    trailing: String? = null,
    titleWidth: Int? = 129,
    onClick: () -> Unit = {},
    group: MenuRippleGroupState? = null,
) {
    TqAnchoredMenuRow(
        modifier = modifier,
        group = group,
        onClick = onClick,
    ) { anchor ->
        val titleLayout = rememberMenuRowTextLayoutCallback(anchor, "title", title, TqType.BodyL)
        val trailingLayout = trailing?.let {
            rememberMenuRowTextLayoutCallback(anchor, "trailing", it, TqType.BodyL)
        }
        Row(
            modifier = contentModifier,
        verticalAlignment = Alignment.CenterVertically,
        ) {
        val titleModifier = if (titleWidth == null) {
            Modifier
                .weight(1f)
                .height(24.dp)
        } else {
            Modifier.size(width = titleWidth.dp, height = 24.dp)
        }
        Text(
            text = title,
            style = TqType.BodyL,
            color = Gray800,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = titleLayout,
            modifier = titleModifier.menuRowTextRippleAnchor(anchor, "title"),
        )
        if (titleWidth != null) {
            Spacer(Modifier.weight(1f))
        }
        if (trailing != null) {
            Text(
                text = trailing,
                style = TqType.BodyL,
                color = Gray500,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.End,
                onTextLayout = trailingLayout!!,
                modifier = Modifier
                    .menuRowTextRippleAnchor(anchor, "trailing")
                    .size(width = 195.dp, height = 24.dp),
            )
        }
        Box(
            modifier = Modifier.size(width = 44.dp, height = 44.dp),
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

@Preview(showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun ProfileInfoScreenPreview() {
    TalkQQuestTheme {
        ProfileInfoScreen()
    }
}

@Preview(showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun ProfileInfoSocialMemberScreenPreview() {
    TalkQQuestTheme {
        ProfileInfoScreen(isEmailMember = false)
    }
}
