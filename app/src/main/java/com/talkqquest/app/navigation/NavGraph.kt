package com.talkqquest.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.talkqquest.app.feature.home.ui.HomeScreen
import com.talkqquest.app.feature.mission.ui.MissionDetailScreen
import com.talkqquest.app.feature.mission.ui.MissionListScreen

// 네비게이션 그래프.
// TODO(각 담당): composable(Screen.XXX) { XxxScreen(navController) } 로 자기 화면 등록. route는 Screen.kt 참고.
@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onOverlayVisibleChange: (Boolean) -> Unit = {}, // 화면 오버레이(바텀시트 등)가 하단 네비를 덮을 때 알림
) {
    NavHost(
        navController = navController,
        startDestination = Screen.HOME,
        modifier = modifier,
    ) {
        // 하단 네비 4탭 (임시 — 실제 화면으로 교체)
        // 홈은 화면↔데이터 연결 예시로 실제 구현됨(feature/home 참고). 나머지는 각 담당이 교체.
        composable(Screen.HOME) { HomeScreen() }
        composable(Screen.ARCHIVE_HOME) { PlaceholderScreen("아카이브") }
        // B담당: 미션 목록 (홈 → 다른 미션 보기). 카드 클릭 → 미션 상세({missionId}는 실제 값으로 치환).
        composable(Screen.MISSION_LIST) {
            MissionListScreen(
                onBack = { navController.popBackStack() },
                onMissionClick = { missionId -> navController.navigate("mission_detail/$missionId") },
                onSheetVisibleChange = onOverlayVisibleChange, // 저장 시트가 하단 네비를 덮는 동안 네비 숨김
            )
        }
        // B담당: 미션 상세. "다음" → 대화 준비(아직 없어서 임시 화면, 다음 작업에서 교체).
        composable(
            route = Screen.MISSION_DETAIL,
            arguments = listOf(navArgument("missionId") { type = NavType.LongType }),
        ) {
            MissionDetailScreen(
                onBack = { navController.popBackStack() },
                onNextClick = { missionId -> navController.navigate("conversation_prep/$missionId") },
                onMissionClick = { missionId -> navController.navigate("mission_detail/$missionId") },
                onSheetVisibleChange = onOverlayVisibleChange,
            )
        }
        composable(Screen.CONVERSATION_PREP) { PlaceholderScreen("대화 준비") }
        composable(Screen.COMMUNITY_LIST) { PlaceholderScreen("모임") }
        composable(Screen.PROFILE) { PlaceholderScreen("프로필") }

        // 예) composable(Screen.LOGIN) { LoginScreen(navController) }
    }
}
