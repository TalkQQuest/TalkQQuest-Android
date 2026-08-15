package com.talkqquest.app.navigation

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
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
import com.talkqquest.app.feature.profile.data.toProfileImagePart
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// 하단 탭 4개(홈, 미션, 보관함, 프로필)를 HorizontalPager로 묶어 스와이프 가능한 탭 화면을 구성한다.
// - 페이지 순서는 BottomNavItem.entries와 1:1로 맞춘다.
// - pagerState는 MainScreen에서 내려받아 하단바 선택 상태와 공유한다.
// - 각 탭의 실제 콘텐츠는 HomeScreen/MissionListScreen/ArchiveHomeScreen/ProfileScreen으로 분리한다.
// - 상세 화면은 기존 NavGraph destination을 사용하고, 탭 화면에서는 navigate 콜백만 연결한다.
// - 미션 상세, 알림, 보관함 상세처럼 하단바 밖 화면은 NavGraph에서 별도 destination으로 push한다.
@Composable
fun MainTabsPager(
    navController: NavHostController,
    pagerState: PagerState,
    onOverlaySheetTop: (Float?) -> Unit,
    onShowWeeklyReportModal: (String?) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    // 하단 시트처럼 화면 위에 올라오는 모달이 열려 있을 때는 탭 스와이프를 잠근다.
    // 시트가 열린 상태에서 페이지가 움직이면 모달과 배경 화면의 위치가 어긋날 수 있다.
    var modalSheetOpen by remember { mutableStateOf(false) }
    var showHomeBadgeCollection by remember { mutableStateOf(false) }
    var homeResumeAnimationTrigger by remember { mutableIntStateOf(0) }
    var homeExitResetToken by remember { mutableIntStateOf(0) }
    // 홈에서 별도 상세 화면으로 나간 경우에만 true로 바뀐다.
    // 일반 탭 이동에서는 홈 XP 애니메이션 상태를 보존해 불필요한 재시작을 막는다.
    var homeDetailExitPending by remember { mutableStateOf(false) }
    var homeDetailReplayPending by remember { mutableStateOf(false) }
    var badgeReplayPending by remember { mutableStateOf(false) }
    // HorizontalPager는 멀어진 페이지를 버릴 수 있으므로, 초기화 플래그는 필요한 상황에서만 켠다.
    // 최초 진입이나 명시적인 상세 화면 복귀에서만 XP를 0부터 다시 보여준다.
    var animateHomeXpFromZero by remember { mutableStateOf(true) }
    // 첫 ON_RESUME에서는 HomeViewModel의 최초 데이터 로딩과 함께 들어오므로 재생 트리거를 소비만 한다.
    // 이후 상세 화면에서 돌아올 때만 홈 카드 애니메이션을 다시 실행한다.
    var hasConsumedInitialHomeResume by remember { mutableStateOf(false) }
    val pagerScope = rememberCoroutineScope()
    // 기본 임계값(50%)보다 짧게 스와이프해도 탭 전환이 자연스럽게 반응하도록 조정한다.
    // 너무 민감해지지 않도록 25% 지점에서 다음 페이지로 스냅한다.
    val tabFlingBehavior = PagerDefaults.flingBehavior(
        state = pagerState,
        snapPositionalThreshold = 0.25f,
    )
    val homePage = BottomNavItem.entries.indexOf(BottomNavItem.Home)
    val archivePage = BottomNavItem.entries.indexOf(BottomNavItem.Archive)
    val profilePage = BottomNavItem.entries.indexOf(BottomNavItem.Profile)
    val markHomeDetailExit: () -> Unit = {
        homeDetailExitPending = true
    }
    val returnFromHomeBadgeCollection: () -> Unit = {
        pagerScope.launch {
            pagerState.animateScrollToPage(homePage)
            showHomeBadgeCollection = false
            if (badgeReplayPending) {
                badgeReplayPending = false
                homeResumeAnimationTrigger++
            }
        }
    }
    BackHandler(enabled = showHomeBadgeCollection, onBack = returnFromHomeBadgeCollection)
    // 홈의 배지 컬렉션 진입은 프로필 탭을 배경으로 사용하므로 뒤로가기 동작을 별도로 처리한다.
    // 일반 탭 복귀와 구분하기 위해 badgeReplayPending으로 홈 애니메이션 재생 여부를 관리한다.
    LaunchedEffect(pagerState.settledPage, badgeReplayPending) {
        if (pagerState.settledPage == homePage && badgeReplayPending) {
            badgeReplayPending = false
            showHomeBadgeCollection = false
            homeResumeAnimationTrigger++
        }
    }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        if (homeDetailExitPending) {
            // 상세 화면으로 나가 앱이 보이지 않을 때만 0부터 다시 준비해 중간 프레임 깜빡임을 막는다.
            homeDetailExitPending = false
            homeDetailReplayPending = true
            animateHomeXpFromZero = true
            homeExitResetToken++
        }
    }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (pagerState.currentPage == homePage) {
            if (!hasConsumedInitialHomeResume) {
                hasConsumedInitialHomeResume = true
            } else if (homeDetailReplayPending) {
                homeDetailReplayPending = false
                homeResumeAnimationTrigger++
            }
        }
    }
    HorizontalPager(
        state = pagerState,
        modifier = modifier.fillMaxSize(),
        // 인접 페이지를 미리 구성해 탭 이동 시 콘텐츠가 늦게 그려지는 일을 줄인다.
        beyondViewportPageCount = 1,
        userScrollEnabled = !modalSheetOpen,
        flingBehavior = tabFlingBehavior,
        key = { BottomNavItem.entries[it].route },
    ) { page ->
        when (BottomNavItem.entries[page]) {
            BottomNavItem.Home -> HomeTab(
                navController = navController,
                resumeAnimationTrigger = homeResumeAnimationTrigger,
                xpResetToken = homeExitResetToken,
                animateXpFromZero = animateHomeXpFromZero,
                onXpAnimationStarted = { animateHomeXpFromZero = false },
                onOverlaySheetTop = onOverlaySheetTop,
                onBadgeCollectionClick = {
                    showHomeBadgeCollection = true
                    pagerScope.launch {
                        pagerState.animateScrollToPage(profilePage)
                        // 홈이 화면 밖으로 나간 뒤 돌아올 때 XP 애니메이션을 다시 준비한다.
                        animateHomeXpFromZero = true
                        homeExitResetToken++
                        badgeReplayPending = true
                    }
                },
                onHomeDetailExit = markHomeDetailExit,
                onShowWeeklyReportModal = onShowWeeklyReportModal,
            ) { modalSheetOpen = it }
            BottomNavItem.Mission -> MissionTab(navController, onOverlaySheetTop)
            BottomNavItem.Archive -> ArchiveTab(navController)
            BottomNavItem.Profile -> ProfileTab(
                navController = navController,
                showHomeBadgeCollection = showHomeBadgeCollection,
                onHomeBadgeCollectionBack = returnFromHomeBadgeCollection,
                onArchiveClick = {
                    pagerScope.launch {
                        pagerState.animateScrollToPage(archivePage)
                    }
                },
            )
        }
    }
}

@Composable
private fun HomeTab(
    navController: NavHostController,
    resumeAnimationTrigger: Int,
    xpResetToken: Int,
    animateXpFromZero: Boolean,
    onXpAnimationStarted: () -> Unit,
    onOverlaySheetTop: (Float?) -> Unit,
    onBadgeCollectionClick: () -> Unit,
    onHomeDetailExit: () -> Unit,
    onShowWeeklyReportModal: (String?) -> Unit,
    onModalSheetChange: (Boolean) -> Unit,
) {
    val homeScope = rememberCoroutineScope()
    HomeScreen(
        resumeAnimationTrigger = resumeAnimationTrigger,
        xpResetToken = xpResetToken,
        animateXpFromZero = animateXpFromZero,
        onXpAnimationStarted = onXpAnimationStarted,
        onStartMissionClick = { missionId ->
            onHomeDetailExit()
            navController.navigate("mission_detail/$missionId")
        },
        // "다른 미션 보기"는 연속 미션 목록으로 이동한다. 미션 상세와 동일하게 홈 복귀 애니메이션을 준비한다.
        onOtherMissionsClick = {
            onHomeDetailExit()
            navController.navigate(Screen.MISSION_LIST_HOME)
        },
        // 알림 아이콘 ripple이 먼저 보인 뒤 화면이 전환되도록 짧게 지연한다.
        onNotificationClick = {
            homeScope.launch {
                delay(140)
                onHomeDetailExit()
                navController.navigate(Screen.NOTIFICATION)
            }
        },
        onShowWeeklyReportModal = onShowWeeklyReportModal,
        onBadgeCollectionClick = onBadgeCollectionClick,
        // 하단 시트가 열릴 때 바텀 네비가 자연스럽게 가려지도록 상단 위치를 전달한다.
        onSheetTopChange = onOverlaySheetTop,
        // 시트가 떠 있는 동안 탭 스와이프를 잠그기 위한 모달 상태 콜백이다.
        onModalSheetChange = onModalSheetChange,
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
        onSheetTopChange = onOverlaySheetTop, // 바텀 시트 위치 변화는 상위 레이아웃이 처리한다.
        onSavedListClick = { navController.navigate("${Screen.ARCHIVE_LIST}/0") },
        // 저장 목록 진입은 보관함 목록의 미션 탭으로 바로 이동한다.
        onArchiveClick = { navController.navigate("${Screen.ARCHIVE_LIST}/0") },
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
        // 리포트 상세는 주간 비교 여부에 따라 다른 destination으로 분기한다.
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
    onArchiveClick: () -> Unit,
) {
    val context = LocalContext.current
    val profileViewModel: ProfileViewModel = hiltViewModel()
    val profileUiState by profileViewModel.uiState.collectAsState()
    val dashboard = profileUiState.dashboard
    val xpSummary = profileUiState.xpSummary
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
        profileViewModel.loadXpSummary()
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
        avatarUrl = dashboard?.avatarUrl ?: profile?.avatarUrl,
        level = xpSummary?.level ?: dashboard?.level ?: profile?.level ?: 2,
        xp = xpSummary?.currentXp ?: dashboard?.xp ?: profile?.xp ?: 30,
        nextLevelXp = xpSummary?.nextLevelXp ?: 100,
        earnedBadgeCount = earnedBadgeCount,
        weeklyCompletedCount = weeklyMissionStatus?.completed ?: 5,
        weeklyTotalCount = weeklyMissionStatus?.total ?: 7,
        weeklyDays = weeklyMissionStatus?.weeklyDays.orEmpty(),
        onEditProfileClick = { navController.navigate(Screen.PROFILE_INFO) },
        onAvatarClick = { uri ->
            val imagePart = uri.toProfileImagePart(context)
            if (imagePart == null) {
                Toast.makeText(context, "\uC774\uBBF8\uC9C0\uB97C \uBD88\uB7EC\uC624\uC9C0 \uBABB\uD588\uC5B4\uC694.", Toast.LENGTH_SHORT).show()
            } else {
                profileViewModel.uploadProfileImage(imagePart)
            }
        },
        onSettingsClick = { navController.navigate(Screen.PROFILE_SETTINGS) },
        onBadgesClick = { navController.navigate(Screen.PROFILE_BADGES) },
        onRecentMissionClick = { navController.navigate(Screen.PROFILE_RECENT_MISSION) },
        onArchiveClick = onArchiveClick,
    )
}
