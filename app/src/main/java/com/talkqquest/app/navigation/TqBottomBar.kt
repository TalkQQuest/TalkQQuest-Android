package com.talkqquest.app.navigation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.talkqquest.app.R
import com.talkqquest.app.core.designsystem.Gray100
import com.talkqquest.app.core.designsystem.Gray300
import com.talkqquest.app.core.designsystem.Primary500
import com.talkqquest.app.core.designsystem.Primary600
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.White
import com.talkqquest.app.core.designsystem.component.rememberHapticTick
import com.talkqquest.app.core.designsystem.softShadow
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import kotlinx.coroutines.launch

// 하단 네비 바. 디자인 CSS 값 그대로:
// 바: 높이 64 / radius 36 / 흰색 0.8 + 블러 10 / 테두리 흰 0.3 / 그림자 0 -2 12 검정 6%
// 선택 칩: 최대 92x44 / radius 22 / 흰 0.9 + 테두리 흰 0.4 / 그림자 0 6 24 보라(114,100,248) 14%
// 블러: Haze(안드12+ 진짜 블러 / 구버전 틴트 fallback).

// route가 속한 탭. 하위 화면(예: 미션 상세)은 부모 탭이 계속 선택돼 보이게 매핑.
private fun tabRouteOf(route: String?): String? = when (route) {
    // 미션 목록은 이제 독립 탭(else로 떨어져 자기 자신=미션 탭 선택). 하위 화면은 미션 탭 유지.
    Screen.MISSION_LIST_HOME -> Screen.HOME // 홈 "다른 미션 보기"로 띄운 목록은 홈 탭 유지
    Screen.MISSION_DETAIL -> Screen.MISSION_LIST
    Screen.CONVERSATION_SETUP_1, Screen.CONVERSATION_SETUP_2,
    Screen.CONVERSATION_SETUP_3, Screen.CONVERSATION_SETUP_4 -> Screen.MISSION_LIST
    Screen.CONVERSATION -> Screen.MISSION_LIST
    Screen.REPORT -> Screen.HOME
    Screen.PROFILE_BADGES -> Screen.PROFILE
    Screen.PROFILE_RECENT_MISSION -> Screen.PROFILE
    else -> route
}

@Composable
fun TqBottomBar(
    navController: NavHostController,
    pagerState: PagerState,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val scope = rememberCoroutineScope()
    val entries = BottomNavItem.entries

    // 탭 4개 셸(MainTabsPager) 위: 선택 표시는 페이저 현재 페이지, 탭 클릭은 페이저 슬라이드.
    // 상세 화면 위: tabRouteOf로 부모 탭 표시 + 클릭 시 그 탭으로 navigate.
    val onShell = currentRoute in entries.map { it.route }.toSet()
    val selectedRoute = if (onShell) entries[pagerState.currentPage].route else tabRouteOf(currentRoute)
    // 칩 위치(0f..3f). 셸에선 페이저 스크롤을 그대로 물려 스와이프엔 실시간 추종,
    // 탭 클릭엔 페이저 애니메이션과 동일한 시간으로 슬라이드. 상세 화면에선 부모 탭 인덱스.
    val parentIndex = entries.indexOfFirst { it.route == tabRouteOf(currentRoute) }.coerceAtLeast(0)
    val selectedPos: () -> Float = {
        if (onShell) pagerState.currentPage + pagerState.currentPageOffsetFraction
        else parentIndex.toFloat()
    }

    TqBottomBarContent(
        selectedRoute = selectedRoute,
        selectedPos = selectedPos,
        onTabClick = { route ->
            val page = entries.indexOfFirst { it.route == route }
            if (onShell) {
                // 이미 페이저 위: 탭도 애니메이션 슬라이드로(네비게이션 없이 페이지만 이동).
                if (page >= 0 && page != pagerState.currentPage) {
                    scope.launch {
                        pagerState.animateScrollToPage(page, animationSpec = NavigationMotion.floatSpec)
                    }
                }
            } else {
                // 상세 화면에서 탭을 누르면 해당 탭의 시작 화면으로 복귀.
                navController.navigate(route) {
                    popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                    launchSingleTop = true
                }
            }
        },
        hazeState = hazeState,
        modifier = modifier,
    )
}

@Composable
private fun TqBottomBarContent(
    selectedRoute: String?,
    selectedPos: () -> Float,
    onTabClick: (String) -> Unit,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
) {
    val tick = rememberHapticTick()
    Box(
        modifier = modifier
            .navigationBarsPadding()
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .height(64.dp)
            // 바 그림자: 위로 2px, 블러 12, 검정 6% (CSS 0 -2 12 rgba(0,0,0,0.06))
            .softShadow(
                color = Color.Black.copy(alpha = 0.06f),
                offsetX = 0.dp,
                offsetY = (-2).dp,
                blur = 12.dp,
                cornerRadius = 36.dp,
            ),
        contentAlignment = Alignment.Center,
    ) {
        // 바 배경 층. 여기만 모서리 clip.
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(36.dp))
                .hazeEffect(state = hazeState) {
                    blurRadius = 10.dp
                    backgroundColor = White
                    tints = listOf(HazeTint(White.copy(alpha = 0.8f)))
                }
                .border(1.dp, White.copy(alpha = 0.3f), RoundedCornerShape(36.dp)),
        )
        // 아이콘·칩 층: clip 없음. BoxWithConstraints로 바 실제 폭을 알아 선택 칩 폭을 계산.
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            // 탭 간격 gap = (바폭 - 좌우인셋 65 - 아이콘 4*44=176) / 3.
            // 선택 칩 폭 = 44 + 오버플로*2. 393 기준 최대 92, 좁으면 최소 56.
            val tabGap = (maxWidth - 65.dp - 176.dp) / 3f
            val chipWidth = (44.dp + (tabGap - 4.dp) * 2f).coerceIn(56.dp, 92.dp)

            // 탭별 interaction을 끌어올려(각 탭·칩이 공유) 선택된 탭이 눌리면 칩도 같이 눌리게.
            val interactions = remember { List(BottomNavItem.entries.size) { MutableInteractionSource() } }
            // 칩은 선택된 탭 위에 얹혀 있으니, 그 탭의 press를 칩에 물려 아이콘과 통째로 스케일(예전 ①번 동작).
            val selectedIndex = BottomNavItem.entries.indexOfFirst { it.route == selectedRoute }.coerceAtLeast(0)
            val chipPressed by interactions[selectedIndex].collectIsPressedAsState()
            val chipPress by animateFloatAsState(
                targetValue = if (chipPressed) 0.9f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
                label = "chipPress",
            )

            // 선택 칩(흰 알약) — 하나만 두고 선택 위치로 미끄러진다. Row(아이콘)보다 먼저 그려 뒤에 깔림.
            // 위치는 페이저 스크롤(selectedPos)에 직접 물려 offset 람다(레이아웃 단계)에서 계산 → 스와이프 실시간 추종·리컴포즈 없음.
            // 탭 i 중심 X = 인셋 32.5 + 22 + i*(44+gap). 칩 좌변 = 중심 - 칩폭/2.
            val legacyChip = android.os.Build.VERSION.SDK_INT < 28
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset {
                        val centerX = 54.5.dp + (44.dp + tabGap) * selectedPos()
                        IntOffset((centerX - chipWidth / 2f).roundToPx(), 0)
                    }
                    .requiredSize(width = chipWidth, height = 44.dp)
                    // 누르면 아이콘과 함께 0.9로 줄었다 복귀(같은 spring이라 동기화)
                    .graphicsLayer { scaleX = chipPress; scaleY = chipPress }
                    // 보라 그림자: 아래 6, 블러 24, 14% (CSS 0 6 24 rgba(114,100,248,0.14))
                    .softShadow(
                        color = Primary500.copy(alpha = 0.14f),
                        offsetX = 0.dp,
                        offsetY = 6.dp,
                        blur = 24.dp,
                        cornerRadius = 22.dp,
                    )
                    .background(
                        if (legacyChip) Gray100 else White.copy(alpha = 0.9f),
                        RoundedCornerShape(22.dp),
                    )
                    .border(1.dp, White.copy(alpha = 0.4f), RoundedCornerShape(22.dp)),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    // 좌우 32.5 고정 인셋(디자인).
                    .padding(horizontal = 32.5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                BottomNavItem.entries.forEachIndexed { index, item ->
                    val selected = selectedRoute == item.route
                    val interaction = interactions[index]
                    val pressed by interaction.collectIsPressedAsState()

                    // 탭 피드백: 누르는 동안 0.9로 줄었다 손 떼면 스프링으로 복귀 (칩+아이콘 전체 스케일)
                    val press by animateFloatAsState(
                        targetValue = if (pressed) 0.9f else 1f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
                        label = "tabPress",
                    )

                    // 44 레이아웃 박스(SpaceBetween 기준). press 스케일은 여기(칩+아이콘 전체가 눌림).
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .graphicsLayer { scaleX = press; scaleY = press },
                        contentAlignment = Alignment.Center,
                    ) {
                        // 클릭 표면 = 칩 크기(오버플로), 알약으로 clip. 칩 하이라이트는 위 슬라이딩 칩이 담당.
                        Box(
                            modifier = Modifier
                                .requiredSize(width = chipWidth, height = 44.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .clickable(
                                    interactionSource = interaction,
                                    indication = null,
                                ) { tick(); onTabClick(item.route) },
                            contentAlignment = Alignment.Center,
                        ) {
                            // 프로필만 선택 시 전용 이미지(보라 배경+연회색 사람), tint 없이 그림. 나머지는 단색 tint.
                            val profileSelected = selected && item == BottomNavItem.Profile
                            Icon(
                                painter = painterResource(
                                    if (profileSelected) R.drawable.ic_nav_profile_selected else item.iconRes,
                                ),
                                contentDescription = item.label,
                                tint = when {
                                    profileSelected -> Color.Unspecified
                                    selected -> Primary600
                                    else -> Gray300
                                },
                                // 아이콘 벡터가 44 프레임에 CSS inset으로 배치돼 44dp로 채우면 크기·위치 정확.
                                modifier = Modifier.size(44.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

// Preview: 뒤 콘텐츠가 없어 블러는 안 보이지만 틴트·배치·칩·아이콘 확인용.
@Preview(name = "네비 393dp", widthDp = 393, showBackground = true, backgroundColor = 0xFFF8FAFC)
@Preview(name = "네비 360dp", widthDp = 360, showBackground = true, backgroundColor = 0xFFF8FAFC)
@Preview(name = "네비 320dp", widthDp = 320, showBackground = true, backgroundColor = 0xFFF8FAFC)
@Composable
private fun TqBottomBarPreview() {
    TalkQQuestTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
        ) {
            TqBottomBarContent(
                selectedRoute = Screen.HOME,
                selectedPos = { 0f },
                onTabClick = {},
                hazeState = remember { HazeState() },
            )
        }
    }
}

// 맨 왼쪽/오른쪽(아카이브) 선택 시 칩이 바 밖으로 안 삐져나오는지 확인용.
@Preview(name = "가장자리 선택 393dp", widthDp = 393, showBackground = true, backgroundColor = 0xFFF8FAFC)
@Preview(name = "가장자리 선택 360dp", widthDp = 360, showBackground = true, backgroundColor = 0xFFF8FAFC)
@Preview(name = "가장자리 선택 320dp", widthDp = 320, showBackground = true, backgroundColor = 0xFFF8FAFC)
@Composable
private fun TqBottomBarEdgePreview() {
    TalkQQuestTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
        ) {
            TqBottomBarContent(
                selectedRoute = Screen.ARCHIVE_HOME,
                selectedPos = { 2f },
                onTabClick = {},
                hazeState = remember { HazeState() },
            )
        }
    }
}
