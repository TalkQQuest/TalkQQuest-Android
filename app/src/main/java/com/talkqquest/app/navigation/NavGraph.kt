package com.talkqquest.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController

/**
 * 전체 네비게이션 그래프. 뼈대만 있는 상태.
 * 화면 구현이 준비되면 아래 NavHost 블록에 composable(route) { ... } 를 추가해주세요.
 */
@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.SPLASH
    ) {
        // TODO(지니/전준호): composable(Screen.SPLASH) { SplashScreen(...) }
        // TODO(지니/전준호): composable(Screen.LOGIN) { LoginScreen(...) }
        // TODO: 담당자별 화면이 준비되는 대로 여기에 연결
    }
}
