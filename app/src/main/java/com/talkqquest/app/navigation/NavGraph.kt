package com.talkqquest.app.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.talkqquest.app.feature.home.ui.HomeScreen
import com.talkqquest.app.feature.mission.ui.ConversationPrepScreen
import com.talkqquest.app.feature.mission.ui.ConversationScreen
import com.talkqquest.app.feature.mission.ui.MissionCompleteScreen
import com.talkqquest.app.feature.mission.ui.MissionDetailScreen
import com.talkqquest.app.feature.mission.ui.MissionListScreen
import com.talkqquest.app.feature.mission.ui.SavedMissionsScreen

// 네비게이션 그래프.
// TODO(각 담당): composable(Screen.XXX) { XxxScreen(navController) } 로 자기 화면 등록. route는 Screen.kt 참고.
@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onOverlaySheetTop: (Float?) -> Unit = {}, // 화면 오버레이(바텀시트) 위 끝 y(px), null=없음 — 네비 가림 처리
) {
    // 화면 전환 모션: 안쪽으로 들어갈 땐 새 화면이 오른쪽에서 밀려 들어오고,
    // 뒤로 갈 땐 현재 화면이 오른쪽으로 밀려 나감 (순간이동처럼 뚝 바뀌지 않게).
    // 단, 하단 탭끼리 오가는 건 안/밖 위계가 아니라 병렬 이동이라 페이드로 교체.
    val tabRoutes = BottomNavItem.entries.map { it.route }.toSet()
    fun AnimatedContentTransitionScope<NavBackStackEntry>.isTabSwitch() =
        initialState.destination.route in tabRoutes && targetState.destination.route in tabRoutes
    val slideSpec = tween<IntOffset>(300)
    NavHost(
        navController = navController,
        startDestination = Screen.HOME,
        modifier = modifier,
        enterTransition = {
            if (isTabSwitch()) fadeIn(tween(300))
            else slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, slideSpec)
        },
        exitTransition = {
            if (isTabSwitch()) fadeOut(tween(300))
            else slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, slideSpec)
        },
        popEnterTransition = {
            if (isTabSwitch()) fadeIn(tween(300))
            else slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, slideSpec)
        },
        popExitTransition = {
            if (isTabSwitch()) fadeOut(tween(300))
            else slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, slideSpec)
        },
    ) {
        // 하단 네비 4탭 (임시 — 실제 화면으로 교체)
        // 홈은 화면↔데이터 연결 예시로 실제 구현됨(feature/home 참고). 나머지는 각 담당이 교체.
        composable(Screen.HOME) {
            HomeScreen(
                onStartMissionClick = { missionId -> navController.navigate("mission_detail/$missionId") },
                onOtherMissionsClick = { navController.navigate(Screen.MISSION_LIST) },
            )
        }
        composable(Screen.ARCHIVE_HOME) { PlaceholderScreen("아카이브") }
        // B담당: 미션 목록 (홈 → 다른 미션 보기). 카드 클릭 → 미션 상세({missionId}는 실제 값으로 치환).
        composable(Screen.MISSION_LIST) {
            MissionListScreen(
                onBack = { navController.popBackStack() },
                onMissionClick = { missionId -> navController.navigate("mission_detail/$missionId") },
                onSheetTopChange = onOverlaySheetTop, // 저장 시트가 하단 네비를 덮는 동안 네비 가림
                onSavedListClick = { navController.navigate(Screen.SAVED_MISSIONS) },
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
                onSheetTopChange = onOverlaySheetTop,
                onSavedListClick = { navController.navigate(Screen.SAVED_MISSIONS) },
            )
        }
        // B담당: 저장 목록 (저장 시트 "저장 목록 >"에서 진입). 카드 클릭 → 미션 상세.
        composable(Screen.SAVED_MISSIONS) {
            SavedMissionsScreen(
                onBack = { navController.popBackStack() },
                onMissionClick = { missionId -> navController.navigate("mission_detail/$missionId") },
            )
        }
        // B담당: 대화 준비(미션 진입). "미션 시작하기" → 대화 화면(아직 없어서 임시, 다음 작업에서 교체).
        composable(
            route = Screen.CONVERSATION_PREP,
            arguments = listOf(navArgument("missionId") { type = NavType.LongType }),
        ) { backStackEntry ->
            val missionId = backStackEntry.arguments?.getLong("missionId") ?: 0L
            ConversationPrepScreen(
                onBack = { navController.popBackStack() },
                onStartClick = { navController.navigate("conversation/$missionId") },
            )
        }
        // B담당: 대화 진행. 종료하기 → 미션 완료&XP (대화 시간을 인자로 전달).
        composable(
            route = Screen.CONVERSATION,
            arguments = listOf(navArgument("conversationId") { type = NavType.LongType }),
        ) { backStackEntry ->
            val missionId = backStackEntry.arguments?.getLong("conversationId") ?: 0L
            ConversationScreen(
                onExitConfirm = { durationSec ->
                    // 끝난 대화(및 준비·상세)로 뒤로 못 돌아가게 홈 위를 정리하고 완료 화면으로.
                    navController.navigate("mission_complete/$missionId?durationSec=$durationSec") {
                        popUpTo(Screen.HOME)
                    }
                },
            )
        }
        // B담당: 미션 완료&XP. NAVIGATION.md: 미션 완료 → AI 피드백(FeedbackScreen).
        // 피드백 화면이 아직 없어 임시로 홈 복귀 — TODO: FeedbackScreen 생기면 navigate(feedback)로 교체.
        composable(
            route = "${Screen.MISSION_COMPLETE}?durationSec={durationSec}",
            arguments = listOf(
                navArgument("missionId") { type = NavType.LongType },
                navArgument("durationSec") { type = NavType.LongType; defaultValue = 0L },
            ),
        ) {
            MissionCompleteScreen(
                onContinue = { navController.popBackStack() },
            )
        }
        composable(Screen.COMMUNITY_LIST) { PlaceholderScreen("모임") }
        composable(Screen.PROFILE) { PlaceholderScreen("프로필") }

        // 예) composable(Screen.LOGIN) { LoginScreen(navController) }
    }
}
