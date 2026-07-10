package com.talkqquest.app.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.talkqquest.app.core.designsystem.Gray50
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

// 앱 셸. 하단 네비(떠 있는 유리 알약)가 화면 위에 겹쳐 뜬다.
// hazeState: 뒤 콘텐츠(NavGraph)를 유리(하단 네비)가 흐리게 비추도록 연결하는 상태.
@Composable
fun MainScreen() {
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // 하단바 표시 route: 탭 4개 + 디자인상 하단바가 있는 화면(미션 목록). 그 외 자동 숨김.
    // currentRoute == null = 첫 프레임(시작 화면 세팅 전) → 숨겼다 늦게 뜨지 않게 바로 표시.
    val bottomBarRoutes = BottomNavItem.entries.map { it.route } +
        Screen.MISSION_LIST + Screen.MISSION_DETAIL + Screen.CONVERSATION_PREP + Screen.CONVERSATION

    val hazeState = remember { HazeState() }

    // 앱 페이지 배경(Gray50 = 디자인시스템 '페이지 배경'). 탭 화면들이 같은 톤을 공유하도록 루트에서 한 번 깖.
    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(Gray50)) {
        val density = LocalDensity.current
        // 하단 네비 묶음(알약 64 + 위아래 여백 12x2 + 시스템 네비 인셋)의 위 끝 y(px)
        val navTopPx = with(density) { maxHeight.toPx() - 88.dp.toPx() } -
            WindowInsets.navigationBars.getBottom(density)

        // 화면 오버레이(저장 바텀시트)의 위 끝 y(px). null = 시트 없음.
        // 네비는 시트보다 위 레이어라 그냥 두면 시트 위에 겹쳐 보임 → 시트 위 끝 "선"보다
        // 아래쪽은 그리지 않아, 시트가 내려가는 만큼 네비가 뒤에 있던 것처럼 위에서부터 드러난다.
        var overlaySheetTop by remember { mutableStateOf<Float?>(null) }

        // 시트가 네비 영역을 완전히 덮는 동안엔 네비를 아예 빼서(그림+터치 모두) 시트 조작을 안 막음.
        val showBottomBar = (currentRoute == null || currentRoute in bottomBarRoutes) &&
            (overlaySheetTop?.let { it > navTopPx } ?: true)

        NavGraph(
            navController = navController,
            // hazeSource: 이 영역(화면 콘텐츠)이 유리에 흐리게 비칠 '원본'.
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = hazeState),
            onOverlaySheetTop = { overlaySheetTop = it },
        )
        if (showBottomBar) {
            TqBottomBar(
                navController = navController,
                hazeState = hazeState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .drawWithContent {
                        val sheetTop = overlaySheetTop
                        if (sheetTop == null) {
                            drawContent()
                        } else {
                            // 시트에 안 덮인 부분(시트 위 끝 선 위쪽)만 그림
                            clipRect(bottom = (sheetTop - navTopPx).coerceAtMost(size.height)) {
                                this@drawWithContent.drawContent()
                            }
                        }
                    },
            )
        }
    }
}
