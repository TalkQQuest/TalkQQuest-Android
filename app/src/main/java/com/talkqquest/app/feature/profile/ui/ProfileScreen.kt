package com.talkqquest.app.feature.profile.ui

import android.net.Uri

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import com.talkqquest.app.R
import com.talkqquest.app.core.designsystem.FitDesign
import com.talkqquest.app.core.designsystem.figma
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
import com.talkqquest.app.core.designsystem.component.MenuRippleGroupState
import com.talkqquest.app.core.designsystem.component.TqAnchoredMenuRow
import com.talkqquest.app.core.designsystem.component.menuRowTextRippleAnchor
import com.talkqquest.app.core.designsystem.component.MenuRippleLayer
import com.talkqquest.app.core.designsystem.component.rememberMenuRippleGroup
import com.talkqquest.app.core.designsystem.component.rememberMenuRowTextLayoutCallback
import com.talkqquest.app.core.designsystem.softShadow

private val ProfileTitleStyle = TextStyle(
    fontFamily = PretendardFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 18.sp,
    lineHeight = 28.sp,
    letterSpacing = (-0.01).em,
).figma()

private val HeadingStyle = TextStyle(
    fontFamily = PretendardFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 20.sp,
    lineHeight = 30.sp,
    letterSpacing = (-0.01).em,
).figma()

private val BodyLargeMediumStyle = TextStyle(
    fontFamily = PretendardFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 16.sp,
    lineHeight = 24.sp,
).figma()

private val BodyMediumStyle = TextStyle(
    fontFamily = PretendardFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 22.sp,
).figma()

private val BodySmallStyle = TextStyle(
    fontFamily = PretendardFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 13.sp,
    lineHeight = 20.sp,
).figma()

private val LabelMediumStyle = TextStyle(
    fontFamily = PretendardFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    lineHeight = 18.sp,
).figma()

// 홈 화면과 동일하게, 모든 내용이 들어오는 기기에서도 마이페이지를 조금 위로 올릴 수 있게 한다.
private val ProfileScrollableContentHeight = 902.dp

@Composable
fun ProfileScreen(
    nickname: String = "다민",
    avatarUrl: String? = null,
    level: Int = 2,
    xp: Int = 30,
    nextLevelXp: Int = 100,
    earnedBadgeCount: Int = 5,
    weeklyCompletedCount: Int = 5,
    weeklyTotalCount: Int = 7,
    weeklyDays: List<Boolean> = emptyList(),
    onSettingsClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onAvatarClick: (Uri) -> Unit = {},
    onBadgesClick: () -> Unit = {},
    onRecentMissionClick: () -> Unit = {},
    onArchiveClick: () -> Unit = {},
    // 탭 페이저에서 이 화면이 지금 현재 페이지인지. 스와이프로 다른 탭에 가면 false가 되어
    // 스크롤 위치를 맨 위로 되돌리는 신호로 쓴다(마이페이지 자체 백스택 이동과는 별개 경로).
    isTabVisible: Boolean = true,
) {
    var showPhotoPicker by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    // 화면이 보이지 않게 되는 두 경로 모두에서 스크롤을 애니메이션 없이 즉시 맨 위로 되돌린다.
    // 1) 다른 화면으로 이동: 이 컴포저블의 NavBackStackEntry가 RESUMED 밑으로 내려갈 때(ON_PAUSE).
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
        scope.launch { scrollState.scrollTo(0) }
    }
    // 2) 탭 스와이프로 다른 탭이 보일 때.
    LaunchedEffect(isTabVisible) {
        if (!isTabVisible) scrollState.scrollTo(0)
    }
    Box(Modifier.fillMaxSize()) {
        FitDesign(
            compensateStatusBar = false,
            scrollableContentHeight = ProfileScrollableContentHeight,
            scrollState = scrollState,
        ) {
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
                .profileCircleClick(onClick = onSettingsClick),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_profile_settings),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
        }

        ProfileHeader(
            nickname = nickname,
            avatarUrl = avatarUrl,
            onEditProfileClick = onEditProfileClick,
            onAvatarClick = {
                showPhotoPicker = true
            },
            modifier = Modifier
                .offset(x = 150.5.dp, y = 126.dp)
                .size(width = 93.dp, height = 172.dp),
        )

        LevelCard(
            level = level,
            xp = xp,
            nextLevelXp = nextLevelXp,
            modifier = Modifier
                .offset(x = 16.dp, y = 322.dp)
                .size(width = 362.dp, height = 89.dp),
        )

        WeeklyMissionCard(
            completedCount = weeklyCompletedCount,
            totalCount = weeklyTotalCount,
            weeklyDays = weeklyDays,
            modifier = Modifier
                .offset(x = 16.dp, y = 427.dp)
                .size(width = 362.dp, height = 121.dp),
        )

        ProfileMenuCard(
            badgeCount = earnedBadgeCount,
            onBadgesClick = onBadgesClick,
            onRecentMissionClick = onRecentMissionClick,
            onArchiveClick = onArchiveClick,
            modifier = Modifier
                .offset(x = 16.dp, y = 564.dp)
                .size(width = 362.dp, height = 151.dp),
        )

            }
        }
        // FitDesign의 902dp 스크롤 콘텐츠 밖 viewport에만 picker를 배치한다.
        ProfilePhotoPickerSheet(
            visible = showPhotoPicker,
            onDismiss = { showPhotoPicker = false },
            onPhotoConfirmed = onAvatarClick,
        )
    }
}

@Composable
private fun ProfileHeader(
    nickname: String,
    avatarUrl: String?,
    onEditProfileClick: () -> Unit,
    onAvatarClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        AvatarImage(
            avatarUrl = avatarUrl,
            modifier = Modifier
                .size(93.dp)
                .profileCircleClick(onClick = onAvatarClick),
        )
        Text(
            text = "$nickname 님",
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
                .profileItemClick(onClick = onEditProfileClick),
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
private fun AvatarImage(
    avatarUrl: String? = null,
    modifier: Modifier = Modifier,
) {
    if (avatarUrl.isNullOrBlank()) {
        Image(
            painter = painterResource(R.drawable.img_profile_avatar),
            contentDescription = null,
            modifier = modifier,
        )
    } else {
        AsyncImage(
            model = avatarUrl,
            contentDescription = null,
            modifier = modifier,
            error = painterResource(R.drawable.img_profile_avatar),
            placeholder = painterResource(R.drawable.img_profile_avatar),
        )
    }
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
private fun LevelCard(
    level: Int,
    xp: Int,
    nextLevelXp: Int,
    modifier: Modifier = Modifier,
) {
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
            text = "Lv.$level",
            style = LabelMediumStyle,
            color = Primary600,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .offset(x = 16.dp, y = 41.dp)
                .size(width = 23.dp, height = 22.dp),
        )
        // CSS Frame 331: row, gap 6 — "30" 과 "/ 100XP" 가 별도 텍스트 노드다("100"과 "XP" 사이 공백 없음).
        Row(
            modifier = Modifier
                .offset(x = 236.dp, y = 41.dp)
                .size(width = 110.dp, height = 22.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "$xp", style = LabelMediumStyle, color = Gray400)
            Text(text = "/ ${nextLevelXp}XP", style = LabelMediumStyle, color = Gray400)
        }
        Box(
            modifier = Modifier
                .offset(x = 16.dp, y = 67.dp)
                .size(width = 327.dp, height = 10.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Primary100),
        )
        val progressWidth = (327f * xp / nextLevelXp.coerceAtLeast(1)).coerceIn(0f, 327f)
        Box(
            modifier = Modifier
                .offset(x = 16.dp, y = 67.dp)
                .size(width = progressWidth.dp, height = 10.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Primary600),
        )
    }
}

@Composable
private fun WeeklyMissionCard(
    completedCount: Int,
    totalCount: Int,
    weeklyDays: List<Boolean>,
    modifier: Modifier = Modifier,
) {
    ProfileCard(modifier = modifier) {
        Row(
            modifier = Modifier
                .offset(x = 16.dp, y = 12.dp)
                .height(25.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            // CSS Frame 427321273: row, align-items: flex-end, gap 2 — 글자(109x24)와 불꽃(25x25)의
            // 바닥을 맞춘다. 다만 Compose는 line-height 24의 남는 여백을 위쪽에 더 많이 주기 때문에
            // 글자가 상자 안에서 피그마보다 위로 뜨고, 그만큼 옆의 불꽃이 아래로 내려간 것처럼 보인다
            // (사용자 신고 "살짝 아래로 내려가 있어 보여"). 다른 화면들이 쓰는 FullLeading과 같은 설정으로
            // 글자를 line-height 상자 한가운데 놓아 피그마와 같은 위치가 되게 한다.
            Text(
                text = "이번 주 연속 미션",
                style = BodyLargeMediumStyle.copy(
                    lineHeightStyle = LineHeightStyle(
                        alignment = LineHeightStyle.Alignment.Center,
                        trim = LineHeightStyle.Trim.None,
                    ),
                ),
                color = Gray800,
                modifier = Modifier.height(24.dp),
            )
            Image(
                painter = painterResource(R.drawable.ic_profile_fire),
                contentDescription = null,
                modifier = Modifier.size(25.dp),
            )
        }

        val days = listOf("일", "월", "화", "수", "목", "금", "토")
        val normalizedWeeklyDays = weeklyDays.take(days.size)
        val shouldUseWeeklyDays = normalizedWeeklyDays.isNotEmpty()
        val safeCompletedCount = completedCount.coerceIn(0, totalCount.coerceAtLeast(0)).coerceAtMost(days.size)
        days.forEachIndexed { index, day ->
            MissionDay(
                day = day,
                completed = if (shouldUseWeeklyDays) {
                    normalizedWeeklyDays.getOrElse(index) { false }
                } else {
                    index < safeCompletedCount
                },
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
    if (completed) {
        Image(
            painter = painterResource(R.drawable.ic_mission_day_checked),
            contentDescription = null,
            modifier = modifier,
        )
    } else {
        Box(
            modifier = modifier
                .clip(CircleShape)
                .background(Gray100),
        )
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
    val menuGroup = rememberMenuRippleGroup()
    ProfileCard(
        modifier = modifier.onGloballyPositioned {
            val top = it.positionInRoot().y
            menuGroup.setEdges(top, top + it.size.height, fillFirst = true, fillLast = true)
        },
    ) {
        MenuRippleLayer(menuGroup, Modifier.matchParentSize())
        ProfileMenuRow(
            title = "획득한 배지",
            trailing = badgeCount.toString(),
            onClick = onBadgesClick,
            modifier = Modifier
                .offset(y = 10.dp)
                .size(width = 362.dp, height = 43.dp),
            contentModifier = Modifier
                .offset(x = 16.dp)
                .size(width = 345.dp, height = 43.dp),
            group = menuGroup,
        )
        ProfileMenuRow(
            title = "최근 활동 요약",
            onClick = onRecentMissionClick,
            modifier = Modifier
                .offset(y = 53.dp)
                .size(width = 362.dp, height = 44.dp),
            contentModifier = Modifier
                .offset(x = 16.dp)
                .size(width = 346.dp, height = 44.dp),
            group = menuGroup,
        )
        ProfileMenuRow(
            title = "보관함",
            onClick = onArchiveClick,
            modifier = Modifier
                .offset(y = 97.dp)
                .size(width = 362.dp, height = 44.dp),
            contentModifier = Modifier
                .offset(x = 16.dp)
                .size(width = 346.dp, height = 44.dp),
            group = menuGroup,
        )
    }
}

@Composable
private fun ProfileMenuRow(
    title: String,
    trailing: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier,
    group: MenuRippleGroupState? = null,
) {
    TqAnchoredMenuRow(
        modifier = modifier,
        group = group,
        onClick = onClick,
    ) { anchor ->
        val titleLayout = rememberMenuRowTextLayoutCallback(anchor, "title", title, BodyLargeMediumStyle)
        val trailingLayout = trailing?.let {
            rememberMenuRowTextLayoutCallback(anchor, "trailing", it, BodyLargeMediumStyle)
        }
        Box(
            modifier = contentModifier,
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                text = title,
                style = BodyLargeMediumStyle,
                color = Gray800,
                onTextLayout = titleLayout,
                modifier = Modifier
                    .menuRowTextRippleAnchor(anchor, "title")
                    .size(width = 150.dp, height = 24.dp),
            )
            if (trailing != null) {
                Text(
                    text = trailing,
                style = BodyLargeMediumStyle,
                color = Gray900,
                onTextLayout = trailingLayout!!,
                modifier = Modifier
                    .menuRowTextRippleAnchor(anchor, "trailing")
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
}

@Preview(showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun ProfileScreenPreview() {
    TalkQQuestTheme {
        ProfileScreen()
    }
}
