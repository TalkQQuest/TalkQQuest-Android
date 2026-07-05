package com.talkqquest.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

// 앱 셸. 하단 네비 바 + 화면 영역(NavGraph).
@Composable
fun MainScreen() {
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // 하단바는 탭 4개 route에서만 표시(그 외 자동 숨김). 예외로 보이게 하려면 route를 이 목록에 추가.
    val bottomBarRoutes = BottomNavItem.entries.map { it.route }
    val showBottomBar = currentRoute in bottomBarRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                TqBottomBar(navController)
            }
        },
    ) { innerPadding ->
        NavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
        )
    }
}
