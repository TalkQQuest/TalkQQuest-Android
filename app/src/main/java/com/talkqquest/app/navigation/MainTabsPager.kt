package com.talkqquest.app.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.talkqquest.app.feature.archive.ui.ArchiveHomeScreen
import com.talkqquest.app.feature.archive.viewmodel.ActivityType
import com.talkqquest.app.feature.home.ui.HomeScreen
import com.talkqquest.app.feature.mission.ui.MissionListScreen
import com.talkqquest.app.feature.profile.ui.ProfileScreen
import com.talkqquest.app.feature.profile.viewmodel.ProfileViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// 하단 탭 4개(홈·미션·보관함·프로필)를 하나의 HorizontalPager에 담아 손가락 추종 슬라이드를 제공한다.
// - 페이지 = BottomNavItem.entries 순서와 1:1 매칭.
// - pagerState는 MainScreen에서 호이스팅해 넘겨받아 하단바(전역)와 상태를 공유한다.
// - 각 탭 콘텐츠(HomeScreen/MissionListScreen/ArchiveHomeScreen/ProfileScreen)는 그대로 두고,
//   기존 NavGraph destination에 있던 콜백 배선만 이 파일의 탭 컴포저블로 옮겼다.
// - 상세 화면(미션 상세·대화·보관함 상세 등)은 여전히 NavGraph의 별도 destination이며 페이저 위로 push 된다.
@Composable
fun MainTabsPager(
    navController: NavHostController,
    pagerState: PagerState,
    onOverlaySheetTop: (Float?) -> Unit,
    modifier: Modifier = Modifier,
) {
    HorizontalPager(
        state = pagerState,
        modifier = modifier.fillMaxSize(),
        // 인접 탭을 미리 구성해 스와이프 중 빈 화면 없이 콘텐츠가 따라오게 한다.
        beyondViewportPageCount = 1,
        key = { BottomNavItem.entries[it].route },
    ) { page ->
        when (BottomNavItem.entries[page]) {
            BottomNavItem.Home -> HomeTab(navController)
            BottomNavItem.Mission -> MissionTab(navController, onOverlaySheetTop)
            BottomNavItem.Archive -> ArchiveTab(navController)
            BottomNavItem.Profile -> ProfileTab(navController)
        }
    }
}

@Composable
private fun HomeTab(navController: NavHostController) {
    val homeScope = rememberCoroutineScope()
    HomeScreen(
        onStartMissionClick = { missionId -> navController.navigate("mission_detail/$missionId") },
        // "다른 미션 보기" → 홈 소속 미션 목록(예전 헤더). 미션 탭으로 전환하지 않고 홈 탭 유지.
        onOtherMissionsClick = { navController.navigate(Screen.MISSION_LIST_HOME) },
        // 알림 아이콘 ripple이 먼저 보인 후 화면이 전환되도록 짧게 지연합니다.
        onNotificationClick = {
            homeScope.launch {
                delay(140)
                navController.navigate(Screen.NOTIFICATION)
            }
        },
        // 주간 비교 리포트 도착 모달 "보러가기" → 주간 비교 리포트 (알림창 화살표와 같은 화면)
        onWeeklyReportClick = { navController.navigate(Screen.WEEKLY_COMPARE) },
    )
}

@Composable
private fun MissionTab(
    navController: NavHostController,
    onOverlaySheetTop: (Float?) -> Unit,
) {
    MissionListScreen(
        onBack = { navController.popBackStack() },
        onMissionClick = { missionId -> navController.navigate("mission_detail/$missionId") },
        onSheetTopChange = onOverlaySheetTop, // 바텀시트가 올라올 때 오버레이 처리를 위한 콜백
        onSavedListClick = { navController.navigate("${Screen.ARCHIVE_LIST}/0") },
        // 헤더 폴더 → 보관함 탭(ARCHIVE_HOME). 탭 route라 페이저가 보관함 페이지로 슬라이드된다.
        onArchiveClick = { navController.navigate(Screen.ARCHIVE_HOME) },
    )
}

@Composable
private fun ArchiveTab(navController: NavHostController) {
    ArchiveHomeScreen(
        onNavigateToSearch = {
            navController.navigate(Screen.ARCHIVE_SEARCH)
        },
        onNavigateToList = { tabIndex: Int ->
            navController.navigate("${Screen.ARCHIVE_LIST}/$tabIndex")
        },
        // 💡 C담당: 전달 파라미터 타입 명시 유지
        onNavigateToDetail = { activityId: String, type: ActivityType ->
            when (type) {
                ActivityType.CONVERSATION -> navController.navigate("archive_conversation_detail/$activityId")
                ActivityType.SENTENCE -> navController.navigate("archive_saved_phrase/$activityId")
                ActivityType.REPORT -> navController.navigate("archive_report/$activityId")
                ActivityType.MISSION -> navController.navigate("mission_detail/$activityId")
            }
        }
    )
}

@Composable
private fun ProfileTab(navController: NavHostController) {
    val context = LocalContext.current
    val profileViewModel: ProfileViewModel = hiltViewModel()
    val profileUiState by profileViewModel.uiState.collectAsState()
    val dashboard = profileUiState.dashboard
    val profile = profileUiState.profile
    val nickname = dashboard?.nickname?.takeIf { it.isNotBlank() }
        ?: profile?.nickname
        ?: profile?.name
        ?: "다민"
    val earnedBadgeCount = dashboard?.badges?.size
        ?: profileUiState.badges.count { it.isEarned }.takeIf { it > 0 }
        ?: 5
    val weeklyMissionStatus = dashboard?.weeklyMissionStatus

    LaunchedEffect(Unit) {
        profileViewModel.loadDashboard()
    }

    profileUiState.errorMessage?.let { message ->
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        profileViewModel.clearError()
    }

    ProfileScreen(
        nickname = nickname,
        level = dashboard?.level ?: profile?.level ?: 2,
        xp = dashboard?.xp ?: profile?.xp ?: 30,
        earnedBadgeCount = earnedBadgeCount,
        weeklyCompletedCount = weeklyMissionStatus?.completed ?: 5,
        weeklyTotalCount = weeklyMissionStatus?.total ?: 7,
        onEditProfileClick = { navController.navigate(Screen.PROFILE_INFO) },
        onSettingsClick = { navController.navigate(Screen.PROFILE_SETTINGS) },
        onBadgesClick = { navController.navigate(Screen.PROFILE_BADGES) },
        onRecentMissionClick = { navController.navigate(Screen.PROFILE_RECENT_MISSION) },
        onArchiveClick = { navController.navigate(Screen.ARCHIVE_HOME) },
    )
}
