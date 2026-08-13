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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

// ?˜ë‹¨ ??4ê°??ˆÂ·ë??˜Â·ë³´ê´€?¨Â·í”„ë¡œí•„)ë¥??˜ë‚˜??HorizontalPager???´ì•„ ?ê???ì¶”ì¢… ?¬ë¼?´ë“œë¥??œê³µ?œë‹¤.
// - ?˜ì´ì§€ = BottomNavItem.entries ?œì„œ?€ 1:1 ë§¤ì¹­.
// - pagerState??MainScreen?ì„œ ?¸ì´?¤íŒ…???˜ê²¨ë°›ì•„ ?˜ë‹¨ë°??„ì—­)?€ ?íƒœë¥?ê³µìœ ?œë‹¤.
// - ê°???ì½˜í…ì¸?HomeScreen/MissionListScreen/ArchiveHomeScreen/ProfileScreen)??ê·¸ë?ë¡??ê³ ,
//   ê¸°ì¡´ NavGraph destination???ˆë˜ ì½œë°± ë°°ì„ ë§????Œì¼????ì»´í¬?€ë¸”ë¡œ ??²¼??
// - ?ì„¸ ?”ë©´(ë¯¸ì…˜ ?ì„¸Â·?€?”Â·ë³´ê´€???ì„¸ ???€ ?¬ì „??NavGraph??ë³„ë„ destination?´ë©° ?˜ì´?€ ?„ë¡œ push ?œë‹¤.
@Composable
fun MainTabsPager(
    navController: NavHostController,
    pagerState: PagerState,
    onOverlaySheetTop: (Float?) -> Unit,
    onShowWeeklyReportModal: (String?) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    // ?ˆì˜ ?°ì–´ ?¹ê¸‰ ?ˆë‚´ ?œíŠ¸ì²˜ëŸ¼ ?¤ì´ ê¹”ë¦° ëª¨ë‹¬?????ˆëŠ” ?™ì•ˆ?€ ???¤ì??´í”„ë¥??ˆë‹¤.
    // (?œíŠ¸ê°€ ?˜ì´?€ ?˜ì´ì§€ ?ˆì— ?¤ì–´ ?ˆì–´?? ???„ë©´ ëª¨ë‹¬ ?„ì—???¸ì—ˆ????????œ¼ë¡??˜ì–´ê°€ ë²„ë¦¼)
    var modalSheetOpen by remember { mutableStateOf(false) }
    var showHomeBadgeCollection by remember { mutableStateOf(false) }
    var homeResumeAnimationTrigger by remember { mutableIntStateOf(0) }
    var homeExitResetToken by remember { mutableIntStateOf(0) }
    // ?ˆì—??NavGraph??ë³„ë„ ?”ë©´????ê²½ìš°?ë§Œ trueê°€ ?œë‹¤.
    // ?˜ë‹¨ ???´ë™ê³???ë°±ê·¸?¼ìš´?œëŠ” ??ê°’ì„ ê±´ë“œë¦¬ì? ?Šì•„ XP ?íƒœë¥?ê·¸ë?ë¡?ë³´ì¡´?œë‹¤.
    var homeDetailExitPending by remember { mutableStateOf(false) }
    var homeDetailReplayPending by remember { mutableStateOf(false) }
    var badgeReplayPending by remember { mutableStateOf(false) }
    // HorizontalPagerê°€ ë©€?´ì§„ ???˜ì´ì§€ë¥??ê¸°?´ë„ ??ê°’ì? ?ìœ„ ?¸ì— ?¨ëŠ”??
    // true????ìµœì´ˆ ì§„ìž… ?ëŠ” ëª…ì‹œ?ì¸ ë³„ë„ ?”ë©´ ë³µê??ì„œë§??¬ìš©?œë‹¤.
    var animateHomeXpFromZero by remember { mutableStateOf(true) }
    // ìµœì´ˆ ON_RESUME?€ HomeViewModel??ìµœì´ˆ ?°ì´???„ì°©ê³?ê°™ì? ??ì§„ìž…?´ë‹¤.
    // ?´ë•Œ??HomeLevelCardê°€ ?ì²´?ìœ¼ë¡?0 ??XPë¥??¬ìƒ?˜ë?ë¡?ë³µê? ? í˜¸ë¥?ì¶”ê??˜ì? ?ŠëŠ”??
    var hasConsumedInitialHomeResume by remember { mutableStateOf(false) }
    val pagerScope = rememberCoroutineScope()
    // ê¸°ë³¸ê°??”ë©´ ??˜ 50%)?€ ì²œì²œ???œëž˜ê·¸í•  ???´ë™ ê±°ë¦¬ê°€ ê¸¸ê²Œ ?ê»´ì§„ë‹¤.
    // ì§§ì? ?¤ì??´í”„?ë„ ë°˜ì‘?˜ë˜ ?¤ìˆ˜ë¡???´ ë°”ë€Œì? ?ŠëŠ” 25% ì§€?ì—???¤ìŒ ?˜ì´ì§€ë¡??¤ëƒ…?œë‹¤.
    val tabFlingBehavior = PagerDefaults.flingBehavior(
        state = pagerState,
        snapPositionalThreshold = 0.25f,
    )
    val homePage = BottomNavItem.entries.indexOf(BottomNavItem.Home)
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
    // ë±ƒì? ì»¬ë ‰?˜ì—???˜ë‹¨ ????œ¼ë¡?ì§ì ‘ ?Œì•„?¤ëŠ” ê²½ë¡œ?? ?ˆì— ?„ì „???„ì°©?????¬ìƒ?œë‹¤.
    // ?¼ë°˜ ?˜ë‹¨ ???•ë³µ?ëŠ” badgeReplayPending???†ìœ¼ë¯€ë¡??„ë¬´ ?™ìž‘???˜ì? ?ŠëŠ”??
    LaunchedEffect(pagerState.settledPage, badgeReplayPending) {
        if (pagerState.settledPage == homePage && badgeReplayPending) {
            badgeReplayPending = false
            showHomeBadgeCollection = false
            homeResumeAnimationTrigger++
        }
    }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        if (homeDetailExitPending) {
            // ?”ë©´ ?„í™˜???ë‚˜ ?ˆì´ ë³´ì´ì§€ ?Šì„ ?Œë§Œ 0?¼ë¡œ ì¤€ë¹„í•´, ?˜ê????„ì¤‘ ë²ˆì©?„ì„ ë§‰ëŠ”??
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
        // ?¸ì ‘ ??„ ë¯¸ë¦¬ êµ¬ì„±???¤ì??´í”„ ì¤?ë¹??”ë©´ ?†ì´ ì½˜í…ì¸ ê? ?°ë¼?¤ê²Œ ?œë‹¤.
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
                        // ?ˆì´ ?„ì „???”ë©´ ë°–ìœ¼ë¡??˜ê°„ ?¤ì—ë§?0?¼ë¡œ ì¤€ë¹„í•œ??
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
        // "?¤ë¥¸ ë¯¸ì…˜ ë³´ê¸°" ?????Œì† ë¯¸ì…˜ ëª©ë¡(?ˆì „ ?¤ë”). ë¯¸ì…˜ ??œ¼ë¡??„í™˜?˜ì? ?Šê³  ????? ì?.
        onOtherMissionsClick = {
            onHomeDetailExit()
            navController.navigate(Screen.MISSION_LIST_HOME)
        },
        // ?Œë¦¼ ?„ì´ì½?ripple??ë¨¼ì? ë³´ì¸ ???”ë©´???„í™˜?˜ë„ë¡?ì§§ê²Œ ì§€?°í•©?ˆë‹¤.
        onNotificationClick = {
            homeScope.launch {
                delay(140)
                onHomeDetailExit()
                navController.navigate(Screen.NOTIFICATION)
            }
        },
        onShowWeeklyReportModal = onShowWeeklyReportModal,
        onBadgeCollectionClick = onBadgeCollectionClick,
        // ?°ì–´ ?¹ê¸‰ ?ˆë‚´ ?œíŠ¸ê°€ ?˜ë‹¨ ?¤ë¹„ë¥???Š” ?™ì•ˆ ?¤ë¹„ë¥?ê°€ë¦?
        onSheetTopChange = onOverlaySheetTop,
        // ê·??œíŠ¸ê°€ ???ˆëŠ” ?™ì•ˆ ???¤ì??´í”„ë¥???ëª¨ë‹¬?´ë¼ ???”ë©´?¼ë¡œ ëª??˜ì–´ê°€????.
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
        onSheetTopChange = onOverlaySheetTop, // ë°”í??œíŠ¸ê°€ ?¬ë¼?????¤ë²„?ˆì´ ì²˜ë¦¬ë¥??„í•œ ì½œë°±
        onSavedListClick = { navController.navigate("${Screen.ARCHIVE_LIST}/0") },
        // ?¤ë” ?´ë”??ë³´ê???ëª©ë¡??ë¯¸ì…˜ ??œ¼ë¡?ë°”ë¡œ ?´ë™?œë‹¤.
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
        // ?’¡ [?˜ì •?? isWeeklyCompare ?Œë¼ë¯¸í„° ì¶”ê? ë°??¼ìš°??ë¶„ê¸° ì²˜ë¦¬ ?ìš©
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
    val xpSummary = profileUiState.xpSummary
    val profile = profileUiState.profile
    val nickname = dashboard?.nickname?.takeIf { it.isNotBlank() }
        ?: profile?.nickname
        ?: profile?.name
        ?: "?¤ë?"
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
        onArchiveClick = { navController.navigate(Screen.ARCHIVE_HOME) },
    )
}

private fun Uri.toProfileImagePart(context: Context): MultipartBody.Part? {
    val resolver = context.contentResolver
    val mimeType = resolver.getType(this)?.takeIf { it == "image/jpeg" || it == "image/png" } ?: return null
    val bytes = resolver.openInputStream(this)?.use { it.readBytes() } ?: return null
    val extension = if (mimeType == "image/png") "png" else "jpg"
    val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
    return MultipartBody.Part.createFormData("image", "profile_image.$extension", requestBody)
}
