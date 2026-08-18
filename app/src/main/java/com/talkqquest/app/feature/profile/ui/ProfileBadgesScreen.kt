package com.talkqquest.app.feature.profile.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
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
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import com.talkqquest.app.R
import com.talkqquest.app.core.designsystem.component.pagerAxisLock
import com.talkqquest.app.core.designsystem.component.rememberPagerAxisLockState
import com.talkqquest.app.core.designsystem.FitDesign
import com.talkqquest.app.core.designsystem.Gray300
import com.talkqquest.app.core.designsystem.Gray400
import com.talkqquest.app.core.designsystem.Gray50
import com.talkqquest.app.core.designsystem.Gray500
import com.talkqquest.app.core.designsystem.Gray700
import com.talkqquest.app.core.designsystem.Gray800
import com.talkqquest.app.core.designsystem.Gray900
import com.talkqquest.app.core.designsystem.ModalDimDurationMillis
import com.talkqquest.app.core.designsystem.ModalDimOverlay
import com.talkqquest.app.navigation.NavigationMotion
import com.talkqquest.app.core.designsystem.PretendardFamily
import com.talkqquest.app.core.designsystem.Primary100
import com.talkqquest.app.core.designsystem.Primary600
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.White
import com.talkqquest.app.core.designsystem.component.TextAnchoredPillRipple
import com.talkqquest.app.core.designsystem.component.rememberHapticTick
import com.talkqquest.app.core.designsystem.component.rememberTextPillRippleBounds
import com.talkqquest.app.core.designsystem.component.rememberTextPillRippleGlyphBounds
import com.talkqquest.app.core.designsystem.component.rememberTextPillRippleGlyphBoundsUpdater
import com.talkqquest.app.core.designsystem.component.rememberTextPillRippleParentPosition
import com.talkqquest.app.core.designsystem.component.textPillRippleAnchor
import com.talkqquest.app.core.designsystem.component.textPillRippleParentPosition

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
    val pagerState = rememberPagerState(pageCount = { 3 })
    val axisLock = rememberPagerAxisLockState()
    val scope = rememberCoroutineScope()
    var selectedBadge by remember { mutableStateOf<ProfileBadgeUi?>(null) }

    LaunchedEffect(pagerState.settledPage) {
        selectedBadge = null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50),
    ) {
        BadgesTopBar(onBack = onBack)
        BadgeTabs(
            selectedTab = pagerState.currentPage,
            pagePosition = pagerState.currentPage + pagerState.currentPageOffsetFraction,
            onSelectTab = {
                selectedBadge = null
                scope.launch {
                    pagerState.animateScrollToPage(it, animationSpec = NavigationMotion.floatSpec)
                }
            },
        )
        // 축 잠금: 뱃지 목록을 위아래로 밀 때 손가락이 대각선으로 흘러도 탭이 넘어가지 않게 한다.
        // 하단 네비 탭·보관함 탭과 같은 장치(core의 pagerAxisLock).
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .offset(y = 146.dp)
                .size(width = 393.dp, height = 706.dp)
                .clipToBounds()
                .pagerAxisLock(axisLock),
            userScrollEnabled = !axisLock.isVerticalDrag,
            flingBehavior = PagerDefaults.flingBehavior(
                state = pagerState,
                snapAnimationSpec = NavigationMotion.pagerSnapSpec,
            ),
        ) { page ->
            val pageBadges = when (page) {
                1 -> badges.filterNot { it.isEarned }
                2 -> badges.filter { it.isEarned }
                else -> badges
            }
            val scrollState = rememberScrollState()

            BadgeGrid(
                badges = pageBadges,
                scrollState = scrollState,
                onBadgeClick = { badge -> selectedBadge = badge },
            )
        }
        // This remains non-interactive so horizontal drags beginning on the notice
        // continue to be handled by the pager underneath it.
        BadgeNotice(
            earnedCount = badges.count { it.isEarned },
            modifier = Modifier
                .offset(x = 16.dp, y = 169.dp)
                .zIndex(1f),
        )

        val badge = selectedBadge
        ModalDimOverlay(visible = badge != null, onDismiss = { selectedBadge = null })
        // 퇴장 중에도 마지막으로 고른 배지 내용을 그대로 그리도록 별도로 기억해 둔다.
        // (selectedBadge가 바로 null이 되면 exit 애니메이션 도중 내용이 사라진다.)
        var lastBadge by remember { mutableStateOf<ProfileBadgeUi?>(null) }
        if (badge != null) lastBadge = badge
        val cardProgress by animateFloatAsState(
            targetValue = if (badge != null) 1f else 0f,
            animationSpec = tween(ModalDimDurationMillis, easing = FastOutSlowInEasing),
            label = "badgeDetailCard",
            finishedListener = { value -> if (value <= 0f && badge == null) lastBadge = null },
        )
        // 레이아웃 크기는 항상 고정하고 등장/퇴장은 그리기 단계(alpha·scale)에서만 표현한다.
        // AnimatedVisibility로 감싸면 그 노드 경계와 offset(55, 240)로 위치 잡는 실제 카드 위치가
        // 어긋나 확대 기준점이 카드 바깥으로 밀려나 엉뚱한 곳에서 커지는 문제가 있었다.
        lastBadge?.let {
            BadgeDetailDialog(
                badge = it,
                onClose = { selectedBadge = null },
                modifier = Modifier.graphicsLayer {
                    alpha = cardProgress
                    scaleX = 0.86f + 0.14f * cardProgress
                    scaleY = 0.86f + 0.14f * cardProgress
                    transformOrigin = TransformOrigin.Center
                },
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
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Visible,
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
    val tick = rememberHapticTick()
    Box(
        modifier = modifier.profileCircleClick(onClick = { tick(); onClick() }),
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
    pagePosition: Float,
    onSelectTab: (Int) -> Unit,
) {
    val labels = listOf("\uC804\uCCB4", "\uC9C4\uD589\uC911", "\uB2EC\uC131")
    val widths = listOf(31.dp, 47.dp, 31.dp)

    Box(
        modifier = Modifier
            .offset(y = 108.dp)
            .size(width = 393.dp, height = 38.dp),
    ) {
        labels.forEachIndexed { index, label ->
            BadgeTab(
                label = label,
                labelWidth = widths[index],
                selected = selectedTab == index,
                onClick = { onSelectTab(index) },
                modifier = Modifier
                    .offset(x = (index * 131).dp)
                    .size(width = 131.dp, height = 36.dp)
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
                    x = badgeTabIndicatorOffset(pagePosition),
                    y = 36.dp,
                )
                .size(width = 52.dp, height = 3.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Gray800),
        )
    }
}

@Composable
private fun BadgeTab(
    label: String,
    labelWidth: androidx.compose.ui.unit.Dp,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val textBounds = rememberTextPillRippleBounds()
    val glyphBounds = rememberTextPillRippleGlyphBounds()
    val onTextLayout = rememberTextPillRippleGlyphBoundsUpdater(glyphBounds, label, BadgeTabStyle)
    val parentPosition = rememberTextPillRippleParentPosition()

    Box(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .textPillRippleParentPosition(parentPosition),
        contentAlignment = Alignment.TopCenter,
    ) {
        TextAnchoredPillRipple(textBounds.value, glyphBounds.value, parentPosition.value, interactionSource, horizontalInset = 12.dp, verticalInset = 10.dp)
        Text(
            text = label,
            style = BadgeTabStyle,
            color = if (selected) Gray800 else Gray400,
            textAlign = TextAlign.Center,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Visible,
            onTextLayout = onTextLayout,
            modifier = Modifier
                .size(width = labelWidth, height = 28.dp)
                .textPillRippleAnchor(textBounds),
        )
    }
}

@Composable
private fun BadgeNotice(
    earnedCount: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
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
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Visible,
            )
        }
    }
}

@Composable
private fun BadgeGrid(
    badges: List<ProfileBadgeUi>,
    scrollState: androidx.compose.foundation.ScrollState,
    onBadgeClick: (ProfileBadgeUi) -> Unit,
    modifier: Modifier = Modifier,
) {
    val rowCount = ((badges.size + 2) / 3).coerceAtLeast(1)
    val contentHeight = ((rowCount - 1) * 146 + 124).dp
    val scrollContentHeight = maxOf(800.dp, 94.dp + contentHeight + 140.dp)

    Box(
        modifier = modifier
            .size(width = 393.dp, height = 706.dp)
            .verticalScroll(scrollState),
    ) {
        Box(modifier = Modifier.size(width = 393.dp, height = scrollContentHeight)) {
            badges.forEachIndexed { index, badge ->
                val row = index / 3
                val column = index % 3
                BadgeItem(
                    badge = badge,
                    onClick = { onBadgeClick(badge) },
                    modifier = Modifier
                        .offset(x = (23 + column * 122).dp, y = (94 + row * 146).dp)
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
    // 클릭 영역(배지 그림 + 이름 라벨 100x124 전체)을 앱 공용 모서리 반경으로 잘라
    // 리플이 사각형 각으로 퍼지지 않고 카드처럼 둥글게 퍼지게 한다.
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .profileItemClick(onClick = onClick),
    ) {
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
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Visible,
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
    modifier: Modifier = Modifier,
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
            .then(modifier)
            .size(width = 284.dp, height = dialogHeight)
            .clip(RoundedCornerShape(16.dp))
            .background(White)
            .clickable(enabled = false) {},
    ) {
        Box(
            modifier = Modifier
                .offset(x = 232.dp, y = 8.dp)
                .size(44.dp)
                .profileCircleClick(onClick = onClose),
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
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Visible,
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
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Visible,
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

private fun badgeTabIndicatorOffset(pagePosition: Float) = when {
    pagePosition <= 0f -> 43.dp
    pagePosition < 1f -> (43f + 128f * pagePosition).dp
    pagePosition < 2f -> (171f + 129f * (pagePosition - 1f)).dp
    else -> 300.dp
}
