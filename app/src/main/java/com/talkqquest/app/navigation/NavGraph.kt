package com.talkqquest.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

// 네비게이션 그래프.
// TODO(각 담당): composable(Screen.XXX) { XxxScreen(navController) } 로 자기 화면 등록. route는 Screen.kt 참고.
@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Screen.HOME,
        modifier = modifier,
    ) {
        // 하단 네비 4탭 (임시 — 실제 화면으로 교체)
        composable(Screen.HOME) { PlaceholderScreen("홈") }
        composable(Screen.ARCHIVE_HOME) { PlaceholderScreen("아카이브") }
        composable(Screen.COMMUNITY_LIST) { PlaceholderScreen("모임") }
        composable(Screen.PROFILE) { PlaceholderScreen("프로필") }

        // 예) composable(Screen.LOGIN) { LoginScreen(navController) }
    }
}
