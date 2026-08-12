package com.talkqquest.app.navigation

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.talkqquest.app.feature.archive.ui.ArchiveHomeScreen
import com.talkqquest.app.feature.archive.viewmodel.ActivityType
import com.talkqquest.app.feature.home.ui.HomeScreen
import com.talkqquest.app.feature.mission.ui.MissionListScreen
import com.talkqquest.app.feature.profile.ui.ProfileScreen
import com.talkqquest.app.feature.profile.ui.ProfileBadgesScreen
import com.talkqquest.app.feature.profile.ui.ProfileBadgeUi
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
    // 홈의 티어 승급 안내 시트처럼 딤이 깔린 모달이 떠 있는 동안은 탭 스와이프를 끈다.
    // (시트가 페이저 페이지 안에 들어 있어서, 안 끄면 모달 위에서 쓸었을 때 옆 탭으로 넘어가 버림)
    var modalSheetOpen by remember { mutableStateOf(false) }
    var showHomeBadgeCollection by remember { mutableStateOf(false) }
    var homeResumeAnimationTrigger by remember { mutableIntStateOf(0) }
    var homeExitResetToken by remember { mutableIntStateOf(0) }
    // 최초 ON_RESUME은 HomeViewModel의 최초 데이터 도착과 같은 홈 진입이다.
    // 이때는 HomeLevelCard가 자체적으로 0 → XP를 재생하므로 복귀 신호를 추가하지 않는다.
    var hasConsumedInitialHomeResume by remember { mutableStateOf(false) }
    val pagerScope = rememberCoroutineScope()
    val homePage = BottomNavItem.entries.indexOf(BottomNavItem.Home)
    val profilePage = BottomNavItem.entries.indexOf(BottomNavItem.Profile)
    val returnFromHomeBadgeCollection: () -> Unit = {
        pagerScope.launch {
            pagerState.animateScrollToPage(homePage)
            showHomeBadgeCollection = false
        }
    }
    BackHandler(enabled = showHomeBadgeCollection, onBack = returnFromHomeBadgeCollection)
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != homePage) {
            // 페이저는 홈을 composition에 보존하므로 탭 이동 시 게이지 상태를 명시적으로 비운다.
            homeExitResetToken++
        }
    }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (pagerState.currentPage == BottomNavItem.entries.indexOf(BottomNavItem.Home)) {
            if (hasConsumedInitialHomeResume) {
                homeResumeAnimationTrigger++
            } else {
                hasConsumedInitialHomeResume = true
            }
        }
    }
    HorizontalPager(
        state = pagerState,
        modifier = modifier.fillMaxSize(),
        // 인접 탭을 미리 구성해 스와이프 중 빈 화면 없이 콘텐츠가 따라오게 한다.
        beyondViewportPageCount = 1,
        userScrollEnabled = !modalSheetOpen,
        key = { BottomNavItem.entries[it].route },
    ) { page ->
        when (BottomNavItem.entries[page]) {
            BottomNavItem.Home -> HomeTab(
                navController = navController,
                resumeAnimationTrigger = homeResumeAnimationTrigger,
                xpResetToken = homeExitResetToken,
                onOverlaySheetTop = onOverlaySheetTop,
                onBadgeCollectionClick = {
                    showHomeBadgeCollection = true
                    pagerScope.launch { pagerState.animateScrollToPage(profilePage) }
                },
            ) { modalSheetOpen = it }
            BottomNavItem.Mission -> MissionTab(navController, pagerState, onOverlaySheetTop)
            BottomNavItem.Archive -> ArchiveTab(navController)
            BottomNavItem.Profile -> ProfileTab(
                navController = navController,
                showHomeBadgeCollection = showHomeBadgeCollection,
                onHomeBadgeCollectionBack = returnFromHomeBadgeCollection,
            )
        }
    }
}

@Composable
private fun HomeTab(
    navController: NavHostController,
    resumeAnimationTrigger: Int,
    xpResetToken: Int,
    onOverlaySheetTop: (Float?) -> Unit,
    onBadgeCollectionClick: () -> Unit,
    onModalSheetChange: (Boolean) -> Unit,
) {
    val homeScope = rememberCoroutineScope()
    HomeScreen(
        resumeAnimationTrigger = resumeAnimationTrigger,
        xpResetToken = xpResetToken,
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
        onBadgeCollectionClick = onBadgeCollectionClick,
        // 티어 승급 안내 시트가 하단 네비를 덮는 동안 네비를 가림.
        onSheetTopChange = onOverlaySheetTop,
        // 그 시트가 떠 있는 동안 탭 스와이프를 끔(모달이라 뒤 화면으로 못 넘어가야 함).
        onModalSheetChange = onModalSheetChange,
    )
}

@Composable
private fun MissionTab(
    navController: NavHostController,
    pagerState: PagerState,
    onOverlaySheetTop: (Float?) -> Unit,
) {
    val missionScope = rememberCoroutineScope()
    val archivePage = BottomNavItem.entries.indexOf(BottomNavItem.Archive)
    MissionListScreen(
        onBack = { navController.popBackStack() },
        onMissionClick = { missionId -> navController.navigate("mission_detail/$missionId") },
        onSheetTopChange = onOverlaySheetTop, // 바텀시트가 올라올 때 오버레이 처리를 위한 콜백
        onSavedListClick = { navController.navigate("${Screen.ARCHIVE_LIST}/0") },
        // 헤더 폴더도 하단 보관함 탭을 누른 것과 같은 페이저 전환을 사용한다.
        // 선택 칩은 pagerState를 따라 오른쪽으로 이동하고 화면은 인접 페이지로 함께 슬라이드된다.
        onArchiveClick = {
            if (archivePage >= 0 && archivePage != pagerState.currentPage) {
                missionScope.launch { pagerState.animateScrollToPage(archivePage) }
            }
        },
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
        // 💡 [수정됨] isWeeklyCompare 파라미터 추가 및 라우팅 분기 처리 적용
        onNavigateToDetail = { activityId: String, type: ActivityType, isWeeklyCompare: Boolean ->
            when (type) {
                ActivityType.CONVERSATION -> navController.navigate("archive_conversation_detail/$activityId")
                ActivityType.SENTENCE -> navController.navigate("archive_saved_phrase/$activityId")
                ActivityType.REPORT -> {
                    if (isWeeklyCompare) {
                        navController.navigate("archive_weekly_compare_report/$activityId")
                    } else {
                        navController.navigate("archive_report/$activityId")
                    }
                }
                ActivityType.MISSION -> navController.navigate("mission_detail/$activityId")
            }
        }
    )
}

@Composable
private fun ProfileTab(
    navController: NavHostController,
    showHomeBadgeCollection: Boolean = false,
    onHomeBadgeCollectionBack: () -> Unit = {},
) {
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

    LaunchedEffect(showHomeBadgeCollection) {
        profileViewModel.loadDashboard()
        if (showHomeBadgeCollection) profileViewModel.loadBadges()
    }

    profileUiState.errorMessage?.let { message ->
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        profileViewModel.clearError()
    }

    if (showHomeBadgeCollection) {
        val badges = profileUiState.badges.map { badge ->
            ProfileBadgeUi(
                id = badge.id,
                name = badge.name,
                description = badge.description,
                isEarned = badge.isEarned,
                earnedAt = badge.earnedAt,
                current = badge.progress?.current,
                target = badge.progress?.target,
            )
        }
        ProfileBadgesScreen(
            badges = badges,
            onBack = onHomeBadgeCollectionBack,
        )
    } else ProfileScreen(
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
