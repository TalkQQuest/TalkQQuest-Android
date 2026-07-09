package com.talkqquest.app.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    val bottomBarRoutes = BottomNavItem.entries.map { it.route } + Screen.MISSION_LIST + Screen.MISSION_DETAIL
    // 화면 오버레이(예: 저장 바텀시트)가 하단 네비 자리를 덮는 동안 네비 숨김 (디자인: 시트가 네비 위)
    var overlayVisible by remember { mutableStateOf(false) }
    val showBottomBar = (currentRoute == null || currentRoute in bottomBarRoutes) && !overlayVisible

    val hazeState = remember { HazeState() }

    // 앱 페이지 배경(Gray50 = 디자인시스템 '페이지 배경'). 탭 화면들이 같은 톤을 공유하도록 루트에서 한 번 깖.
    Box(modifier = Modifier.fillMaxSize().background(Gray50)) {
        NavGraph(
            navController = navController,
            // hazeSource: 이 영역(화면 콘텐츠)이 유리에 흐리게 비칠 '원본'.
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = hazeState),
            onOverlayVisibleChange = { overlayVisible = it },
        )
        if (showBottomBar) {
            TqBottomBar(
                navController = navController,
                hazeState = hazeState,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}
