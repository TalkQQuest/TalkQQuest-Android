package com.talkqquest.app.navigation

import android.graphics.BlurMaskFilter
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
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
    Row(
        modifier = modifier
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .height(64.dp)
            // 그림자: 위로 2px, 흐림 12, 검정 6% (CSS 0 -2 12 rgba(0,0,0,0.06))
            .softShadow(
                color = Color.Black.copy(alpha = 0.06f),
                offsetX = 0.dp,
                offsetY = (-2).dp,
                blur = 12.dp,
                cornerRadius = 32.dp,
            )
            .clip(RoundedCornerShape(32.dp))
            // 유리: 뒤 콘텐츠 블러 10 + 흰색 0.8 틴트.
            .hazeEffect(state = hazeState) {
                blurRadius = 10.dp
                backgroundColor = White
                tints = listOf(HazeTint(White.copy(alpha = 0.8f)))
            }
            .border(1.dp, White.copy(alpha = 0.3f), RoundedCornerShape(32.dp))
            .padding(horizontal = 32.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(40.dp, Alignment.CenterHorizontally),
    ) {
        BottomNavItem.entries.forEach { item ->
            val selected = currentRoute == item.route
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {
                        if (!selected) onTabClick(item.route)
                    },
                contentAlignment = Alignment.Center,
            ) {
                // 선택 칩(아이콘보다 넓은 둥근 직사각형, 44 박스를 넘어 그려짐).
                if (selected) {
                    Box(
                        modifier = Modifier
                            .requiredSize(width = 92.dp, height = 44.dp)
                            // 보라 글로우: 아래 6, 흐림 24, 보라 14% (CSS 0 6 24 rgba(114,100,248,0.14))
                            // 프로스티드 칩 뒤에 깔려 바깥 후광으로만 보임(칩 안으로 안 비침).
                            .softShadow(
                                color = Primary500.copy(alpha = 0.14f),
                                offsetX = 0.dp,
                                offsetY = 6.dp,
                                blur = 24.dp,
                                cornerRadius = 22.dp,
                            )
                            .clip(RoundedCornerShape(22.dp))
                            // 칩도 유리(프로스티드): 블러 10 + 흰색 0.28. 알약 위 밝은 하이라이트.
                            .hazeEffect(state = hazeState) {
                                blurRadius = 10.dp
                                backgroundColor = White
                                tints = listOf(HazeTint(White.copy(alpha = 0.28f)))
                            }
                            .border(1.dp, White.copy(alpha = 0.4f), RoundedCornerShape(22.dp)),
                    )
                }
                Icon(
                    painter = painterResource(item.iconRes),
                    contentDescription = item.label,
                    tint = if (selected) Primary600 else Gray300,
                    modifier = Modifier.size(28.dp),
                )
            }
        }
    }
}

// Preview: 뒤 콘텐츠가 없어 블러는 안 보이지만(틴트만) 배치/칩/아이콘 확인용.
@Preview(name = "하단 네비 - 홈 선택", showBackground = true, backgroundColor = 0xFFF1EEFF)
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
