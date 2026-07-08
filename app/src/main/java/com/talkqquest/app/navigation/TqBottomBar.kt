package com.talkqquest.app.navigation

import android.graphics.BlurMaskFilter
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.talkqquest.app.core.designsystem.Gray300
import com.talkqquest.app.core.designsystem.Primary500
import com.talkqquest.app.core.designsystem.Primary600
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.White
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

// CSS box-shadow 그대로 그리는 커스텀 소프트 그림자(오프셋·흐림·색·투명도 정확히).
// Modifier.shadow(elevation)는 무조건 아래로+높이기반이라 이런 값을 못 맞춰서 직접 그림.
private fun Modifier.softShadow(
    color: Color,
    offsetX: Dp,
    offsetY: Dp,
    blur: Dp,
    cornerRadius: Dp,
): Modifier = this.drawBehind {
    val paint = Paint().apply { this.color = color }
    val blurPx = blur.toPx()
    if (blurPx > 0f) {
        paint.asFrameworkPaint().maskFilter = BlurMaskFilter(blurPx, BlurMaskFilter.Blur.NORMAL)
    }
    drawIntoCanvas { canvas ->
        canvas.drawRoundRect(
            left = offsetX.toPx(),
            top = offsetY.toPx(),
            right = size.width + offsetX.toPx(),
            bottom = size.height + offsetY.toPx(),
            radiusX = cornerRadius.toPx(),
            radiusY = cornerRadius.toPx(),
            paint = paint,
        )
    }
}

// 하단 네비 — 떠 있는 유리 알약(디자인 CSS 값 그대로).
// 알약: 높이 64 / radius 32 / 흰색 0.8 + 블러 10 / 테두리 흰 0.3 / 그림자 0 -2 12 검정6%
// 선택 칩: 92x44 / radius 22 / 흰 0.28 + 블러 10 / 테두리 흰 0.4 / 글로우 0 6 24 보라(114,100,248) 14%
// 블러: Haze(안드12+ 진짜 블러 / 그 미만 틴트 fallback).

// route가 속한 탭. 탭의 하위 화면(예: 미션 목록 = 홈 플로우)에서도 소속 탭이 계속 하이라이트되게 함.
private fun tabRouteOf(route: String?): String? = when (route) {
    Screen.MISSION_LIST -> Screen.HOME
    else -> route
}

@Composable
fun TqBottomBar(
    navController: NavHostController,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    TqBottomBarContent(
        currentRoute = currentRoute,
        onTabClick = { route ->
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        },
        hazeState = hazeState,
        modifier = modifier,
    )
}

@Composable
private fun TqBottomBarContent(
    currentRoute: String?,
    onTabClick: (String) -> Unit,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .navigationBarsPadding()
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .height(64.dp)
            // 알약 그림자: 위로 2px, 흐림 12, 검정 6% (CSS 0 -2 12 rgba(0,0,0,0.06))
            .softShadow(
                color = Color.Black.copy(alpha = 0.06f),
                offsetX = 0.dp,
                offsetY = (-2).dp,
                blur = 12.dp,
                cornerRadius = 32.dp,
            ),
        contentAlignment = Alignment.Center,
    ) {
        // 유리 배경 층: 여기만 둥글게 clip. (그래서 아래 콘텐츠의 칩 글로우는 안 잘림)
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(32.dp))
                .hazeEffect(state = hazeState) {
                    blurRadius = 10.dp
                    backgroundColor = White
                    tints = listOf(HazeTint(White.copy(alpha = 0.8f)))
                }
                .border(1.dp, White.copy(alpha = 0.3f), RoundedCornerShape(32.dp)),
        )
        // 아이콘/칩 층: clip 없음 → 칩 보라 글로우가 알약 밖으로도 자연스럽게 번짐(피그마처럼).
        // BoxWithConstraints로 알약 실제 폭을 알아 선택 칩 폭을 정함(좁은 화면에선 칩 축소).
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            // 탭 간격 gap = (알약폭 - 좌우인셋 65 - 아이콘4개(4*44=176)) / 3.
            // 선택 칩 폭 = 44 + 오버행. 오버행이 (gap-4) 이하가 되게 잡아 옆 아이콘과 안 겹침.
            // 393 등 넉넉하면 상한 92(디자인값), 좁으면 축소(하한 56).
            val tabGap = (maxWidth - 65.dp - 176.dp) / 3f
            val chipWidth = (44.dp + (tabGap - 4.dp) * 2f).coerceIn(56.dp, 92.dp)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    // 좌우 32.5 고정 인셋(디자인). 인셋 > 칩 오버행이라 맨끝 탭 선택돼도 칩이 알약 밖으로 안 나감.
                    .padding(horizontal = 32.5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                BottomNavItem.entries.forEach { item ->
                    val selected = tabRouteOf(currentRoute) == item.route
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) {
                                // 하이라이트 여부가 아니라 "실제 현재 화면"으로 판단:
                                // 같은 화면이면 무시, 탭의 하위 화면(예: 미션 목록)이면 탭 루트로 복귀.
                                if (currentRoute != item.route) onTabClick(item.route)
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        // 선택 칩(아이콘보다 넓은 둥근 직사각형, 44 박스를 넘어 그려짐).
                        // 칩 유리(haze)가 알약(0.8)보다 덜 하얘 어둡게 보여, 밝은 흰색(0.9) 오버레이로 처리.
                        if (selected) {
                            Box(
                                modifier = Modifier
                                    .requiredSize(width = chipWidth, height = 44.dp)
                                    // 보라 글로우: 아래 6, 흐림 24, 보라 14% (CSS 0 6 24 rgba(114,100,248,0.14))
                                    .softShadow(
                                        color = Primary500.copy(alpha = 0.14f),
                                        offsetX = 0.dp,
                                        offsetY = 6.dp,
                                        blur = 24.dp,
                                        cornerRadius = 22.dp,
                                    )
                                    .background(White.copy(alpha = 0.9f), RoundedCornerShape(22.dp))
                                    .border(1.dp, White.copy(alpha = 0.4f), RoundedCornerShape(22.dp)),
                            )
                        }
                        Icon(
                            painter = painterResource(item.iconRes),
                            contentDescription = item.label,
                            tint = if (selected) Primary600 else Gray300,
                            // 아이콘 벡터가 44프레임에 CSS inset대로 배치돼 44dp로 채우면 크기·위치 정확.
                            modifier = Modifier.size(44.dp),
                        )
                    }
                }
            }
        }
    }
}

// Preview: 뒤 콘텐츠가 없어 블러는 안 보이지만(틴트만) 배치/칩/아이콘 확인용.
// 배경은 실제 앱 배경 Gray50(#F8FAFC)로 맞춤 → 실기기/에뮬과 같은 톤으로 보임.
// 폭별 미리보기: 393=디자인 기준폭, 360=흔한 폰(넘침 검증), 320=최소 폭. 세 개 다 안 넘쳐야 정상.
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
                currentRoute = Screen.HOME,
                onTabClick = {},
                hazeState = remember { HazeState() },
            )
        }
    }
}

// 맨 왼쪽(아카이브) 선택 시 칩이 알약 밖으로 안 삐져나오는지 확인용.
@Preview(name = "왼끝 선택 393dp", widthDp = 393, showBackground = true, backgroundColor = 0xFFF8FAFC)
@Preview(name = "왼끝 선택 360dp", widthDp = 360, showBackground = true, backgroundColor = 0xFFF8FAFC)
@Preview(name = "왼끝 선택 320dp", widthDp = 320, showBackground = true, backgroundColor = 0xFFF8FAFC)
@Composable
private fun TqBottomBarEdgePreview() {
    TalkQQuestTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
        ) {
            TqBottomBarContent(
                currentRoute = Screen.ARCHIVE_HOME,
                onTabClick = {},
                hazeState = remember { HazeState() },
            )
        }
    }
}
