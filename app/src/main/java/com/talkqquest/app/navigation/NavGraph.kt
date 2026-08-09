package com.talkqquest.app.navigation

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.talkqquest.app.feature.auth.data.KakaoLoginClient
import com.talkqquest.app.feature.auth.data.NaverLoginClient
import com.talkqquest.app.feature.auth.data.OnboardingStepSaveRequest
import com.talkqquest.app.feature.auth.ui.EmailLoginScreen
import com.talkqquest.app.feature.auth.ui.SignupEmailScreen
import com.talkqquest.app.feature.auth.ui.SignupPasswordScreen
import com.talkqquest.app.feature.auth.ui.SignupNicknameScreen
import com.talkqquest.app.feature.auth.ui.SignupStartScreen
import com.talkqquest.app.feature.auth.ui.SignupVerifyScreen
import com.talkqquest.app.feature.auth.ui.SplashScreen
import com.talkqquest.app.feature.auth.viewmodel.AuthViewModel
import com.talkqquest.app.feature.onboarding.ui.OnboardingDifficultyScreen
import com.talkqquest.app.feature.onboarding.ui.OnboardingGoalScreen
import com.talkqquest.app.feature.onboarding.ui.OnboardingPersonalityScreen
import com.talkqquest.app.feature.onboarding.ui.OnboardingWelcomeScreen
import com.talkqquest.app.feature.onboarding.ui.OnboardingCompleteScreen
import com.talkqquest.app.feature.notification.ui.NotificationScreen
import com.talkqquest.app.feature.profile.ui.ProfileBadgesScreen
import com.talkqquest.app.feature.profile.ui.ProfileBadgeUi
import com.talkqquest.app.feature.profile.ui.ProfileConnectedAccountScreen
import com.talkqquest.app.feature.profile.ui.ProfileConcernScreen
import com.talkqquest.app.feature.profile.ui.ProfileInfoScreen
import com.talkqquest.app.feature.profile.ui.ProfileNicknameEditScreen
import com.talkqquest.app.feature.profile.ui.ProfileNewPasswordScreen
import com.talkqquest.app.feature.profile.ui.ProfilePasswordChangeScreen
import com.talkqquest.app.feature.profile.ui.ProfileRecentMissionScreen
import com.talkqquest.app.feature.profile.ui.ProfileSettingsScreen
import com.talkqquest.app.feature.profile.ui.ProfileSupportScreen
import com.talkqquest.app.feature.profile.ui.ProfileWithdrawScreen
import com.talkqquest.app.feature.profile.viewmodel.ProfileViewModel
import com.talkqquest.app.feature.profile.ui.PrivacyPolicySections
import com.talkqquest.app.feature.profile.ui.ProfileTermsDetailScreen
import com.talkqquest.app.feature.profile.ui.ProfileTermsScreen
import com.talkqquest.app.feature.profile.ui.ServiceTermsSections
import com.talkqquest.app.feature.mission.ui.ConversationSetup1Screen
import com.talkqquest.app.feature.mission.ui.ConversationSetup2Screen
import com.talkqquest.app.feature.mission.ui.ConversationSetup3Screen
import com.talkqquest.app.feature.mission.ui.ConversationSetup4Screen
import com.talkqquest.app.feature.mission.ui.ConversationScreen
import com.talkqquest.app.feature.mission.ui.FeedbackDetailScreen
import com.talkqquest.app.feature.mission.ui.FeedbackScreen
import com.talkqquest.app.feature.mission.ui.MissionCompleteScreen
import com.talkqquest.app.feature.mission.ui.MissionDetailScreen
import com.talkqquest.app.feature.mission.ui.MissionListScreen
import com.talkqquest.app.feature.report.ui.ReportScreen
import com.talkqquest.app.feature.archive.ui.ArchiveListScreen
import com.talkqquest.app.feature.archive.ui.ArchiveSearchScreen
import com.talkqquest.app.feature.archive.ui.ArchiveConversationDetailScreen
import com.talkqquest.app.feature.archive.ui.ArchiveSavedPhraseScreen
import com.talkqquest.app.feature.archive.ui.ArchiveReportScreen
import com.talkqquest.app.feature.archive.viewmodel.ActivityType
import com.talkqquest.app.navigation.Screen
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

// 네비게이션 그래프.
// TODO(각 담당): Screen.kt에 route를 정의한 뒤 NavGraph.kt에 composable을 등록합니다.
@Composable
fun NavGraph(
    navController: NavHostController,
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    onOverlaySheetTop: (Float?) -> Unit = {}, // 화면 위에 겹치는 바텀시트의 top y(px), null이면 없음
) {
    // 화면 전환 모션: 탭 전환은 fade, 일반 push/pop은 좌우 slide를 사용합니다.
    // 하단 탭끼리 이동할 때는 같은 레벨 이동처럼 보이도록 fade로 처리합니다.
    val tabRoutes = BottomNavItem.entries.map { it.route }.toSet()
    fun AnimatedContentTransitionScope<NavBackStackEntry>.isTabSwitch() =
        initialState.destination.route in tabRoutes && targetState.destination.route in tabRoutes
    val slideSpec = tween<IntOffset>(300)
    NavHost(
        navController = navController,
        startDestination = Screen.SPLASH,
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
        composable(Screen.SPLASH) {
            val context = LocalContext.current
            val authViewModel: AuthViewModel = hiltViewModel()
            fun navigateFromSplash(destination: String) {
                navController.navigate(destination) {
                    popUpTo(Screen.SPLASH) { inclusive = true }
                    launchSingleTop = true
                }
            }

            LaunchedEffect(Unit) {
                authViewModel.checkStoredSession(
                    onAuthenticated = { navigateFromSplash(Screen.HOME) },
                    onUnauthenticated = { navigateFromSplash(Screen.LOGIN) },
                    onNetworkError = {
                        Toast.makeText(context, "네트워크 연결을 확인해주세요.", Toast.LENGTH_SHORT).show()
                    },
                )
            }
            SplashScreen()
        }
        composable(Screen.LOGIN) {
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            val authViewModel: AuthViewModel = hiltViewModel()
            val authUiState by authViewModel.uiState.collectAsState()

            authUiState.errorMessage?.let { message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                authViewModel.clearError()
            }

            fun navigateAfterSocialLogin(isNewUser: Boolean, nickname: String?) {
                val destination = if (isNewUser || nickname.isNullOrBlank()) {
                    Screen.SIGNUP_NICKNAME
                } else {
                    Screen.HOME
                }
                navController.navigate(destination) {
                    popUpTo(Screen.LOGIN) { inclusive = true }
                    launchSingleTop = true
                }
            }

            SignupStartScreen(
                onKakaoClick = {
                    scope.launch {
                        KakaoLoginClient.login(context)
                            .onSuccess { providerAccessToken ->
                                authViewModel.loginWithKakao(providerAccessToken) { data ->
                                    navigateAfterSocialLogin(data.isNewUser, data.user.nickname)
                                }
                            }
                            .onFailure { error ->
                                Toast.makeText(
                                    context,
                                    error.message ?: "Kakao login failed.",
                                    Toast.LENGTH_SHORT,
                                ).show()
                            }
                    }
                },
                onNaverClick = {
                    scope.launch {
                        NaverLoginClient.login(context)
                            .onSuccess { providerAccessToken ->
                                authViewModel.loginWithNaver(providerAccessToken) { data ->
                                    navigateAfterSocialLogin(data.isNewUser, data.user.nickname)
                                }
                            }
                            .onFailure { error ->
                                Toast.makeText(
                                    context,
                                    error.message ?: "Naver login failed.",
                                    Toast.LENGTH_SHORT,
                                ).show()
                            }
                    }
                },
                onEmailSignupClick = { navController.navigate(Screen.SIGNUP_EMAIL) },
                onEmailLoginClick = { navController.navigate(Screen.EMAIL_LOGIN) },
            )
        }
        composable(Screen.EMAIL_LOGIN) {
            val context = LocalContext.current
            val authViewModel: AuthViewModel = hiltViewModel()
            val authUiState by authViewModel.uiState.collectAsState()

            EmailLoginScreen(
                onBack = { navController.popBackStack() },
                onLoginClick = { email, password ->
                    authViewModel.loginWithEmail(email, password) {
                        navController.navigate(Screen.HOME) {
                            popUpTo(Screen.LOGIN) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                },
                onFindPasswordClick = {
                    Toast.makeText(context, "비밀번호 찾기는 준비 중입니다.", Toast.LENGTH_SHORT).show()
                },
                errorMessage = authUiState.errorMessage,
            )
        }
        composable(Screen.SIGNUP_EMAIL) {
            val context = LocalContext.current
            val authViewModel: AuthViewModel = hiltViewModel()
            val authUiState by authViewModel.uiState.collectAsState()

            authUiState.errorMessage?.let { message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                authViewModel.clearError()
            }

            SignupEmailScreen(
                onBack = { navController.popBackStack() },
                onSendClick = { email ->
                    authViewModel.requestEmailCode(email) {
                        navController.currentBackStackEntry
                            ?.savedStateHandle
                            ?.set("signup_email", email.trim())
                        navController.navigate(Screen.SIGNUP_VERIFY)
                    }
                },
            )
        }
        composable(Screen.SIGNUP_VERIFY) {
            val context = LocalContext.current
            val authViewModel: AuthViewModel = hiltViewModel()
            val authUiState by authViewModel.uiState.collectAsState()
            val email = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<String>("signup_email")
                .orEmpty()
            var hasSubmittedVerification by remember { mutableStateOf(false) }
            var isVerificationCodeError by remember { mutableStateOf(false) }

            authUiState.errorMessage?.let { message ->
                if (hasSubmittedVerification) isVerificationCodeError = true
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                authViewModel.clearError()
            }

            SignupVerifyScreen(
                email = email.ifBlank { "Talkqquest1234@gmail.com" },
                isCodeError = isVerificationCodeError,
                onBack = { navController.popBackStack() },
                onVerifyCode = { code ->
                    hasSubmittedVerification = true
                    isVerificationCodeError = false
                    authViewModel.verifyEmailCode(email, code) {
                        navController.currentBackStackEntry
                            ?.savedStateHandle
                            ?.set("signup_email", email)
                        navController.navigate(Screen.SIGNUP_PASSWORD)
                    }
                },
                onCodeChange = {
                    isVerificationCodeError = false
                },
                onResendClick = {
                    hasSubmittedVerification = false
                    isVerificationCodeError = false
                    authViewModel.requestEmailCode(email) {
                        Toast.makeText(context, "인증 코드가 재발송되었습니다.", Toast.LENGTH_SHORT).show()
                    }
                },
            )
        }
        composable(Screen.SIGNUP_PASSWORD) {
            SignupPasswordScreen(
                onBack = { navController.popBackStack() },
                onNextClick = { navController.navigate(Screen.SIGNUP_NICKNAME) },
            )
        }
        composable(Screen.SIGNUP_NICKNAME) {
            SignupNicknameScreen(
                onBack = { navController.popBackStack() },
                onCompleteClick = { nickname ->
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set("onboarding_nickname", nickname.trim())
                    navController.navigate(Screen.ONBOARDING_WELCOME)
                },
            )
        }
        composable(Screen.ONBOARDING_WELCOME) {
            val nickname = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<String>("onboarding_nickname")
                .orEmpty()
            OnboardingWelcomeScreen(
                nickname = nickname,
                onFinished = { displayNickname ->
                    navController.navigate(Screen.ONBOARDING_PERSONALITY) {
                        popUpTo(Screen.ONBOARDING_WELCOME) { inclusive = true }
                        launchSingleTop = true
                    }
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set("onboarding_nickname", displayNickname)
                },
            )
        }
        composable(Screen.ONBOARDING_PERSONALITY) {
            val context = LocalContext.current
            val authViewModel: AuthViewModel = hiltViewModel()
            val authUiState by authViewModel.uiState.collectAsState()
            val isConcernEditMode = navController.previousBackStackEntry?.destination?.route == Screen.PROFILE_CONCERN
            val nickname = if (isConcernEditMode) {
                "소다123"
            } else {
                navController.currentBackStackEntry
                    ?.savedStateHandle
                    ?.get<String>("onboarding_nickname")
                    .orEmpty()
            }

            authUiState.errorMessage?.let { message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                authViewModel.clearError()
            }

            OnboardingPersonalityScreen(
                nickname = nickname,
                onBack = { navController.popBackStack() },
                onNextClick = { personalityType ->
                    authViewModel.saveOnboardingStep(
                        OnboardingStepSaveRequest(
                            step = 1,
                            personalityType = personalityType,
                        ),
                    ) {
                        if (isConcernEditMode) {
                            navController.popBackStack(Screen.PROFILE_CONCERN, inclusive = false)
                        } else {
                            navController.navigate(Screen.ONBOARDING_DIFFICULTY)
                        }
                    }
                },
            )
        }
        composable(Screen.ONBOARDING_DIFFICULTY) {
            val isConcernEditMode = navController.previousBackStackEntry?.destination?.route == Screen.PROFILE_CONCERN
            val context = LocalContext.current
            val authViewModel: AuthViewModel = hiltViewModel()
            val authUiState by authViewModel.uiState.collectAsState()

            authUiState.errorMessage?.let { message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                authViewModel.clearError()
            }

            OnboardingDifficultyScreen(
                onBack = { navController.popBackStack() },
                onNextClick = { difficultSituations ->
                    if (difficultSituations.isEmpty()) {
                        Toast.makeText(context, "어려운 점을 선택해주세요.", Toast.LENGTH_SHORT).show()
                    } else {
                        authViewModel.saveOnboardingStep(
                            OnboardingStepSaveRequest(
                                step = 2,
                                difficultSituations = difficultSituations,
                            ),
                        ) {
                            if (isConcernEditMode) {
                                navController.popBackStack(Screen.PROFILE_CONCERN, inclusive = false)
                            } else {
                                navController.navigate(Screen.ONBOARDING_GOAL)
                            }
                        }
                    }
                },
            )
        }
        composable(Screen.ONBOARDING_GOAL) {
            val isConcernEditMode = navController.previousBackStackEntry?.destination?.route == Screen.PROFILE_CONCERN
            val context = LocalContext.current
            val authViewModel: AuthViewModel = hiltViewModel()
            val authUiState by authViewModel.uiState.collectAsState()

            authUiState.errorMessage?.let { message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                authViewModel.clearError()
            }

            OnboardingGoalScreen(
                onBack = { navController.popBackStack() },
                onCompleteClick = { purpose ->
                    if (purpose.isEmpty()) {
                        Toast.makeText(context, "연습 목표를 선택해주세요.", Toast.LENGTH_SHORT).show()
                    } else {
                        authViewModel.saveOnboardingStep(
                            OnboardingStepSaveRequest(
                                step = 3,
                                purpose = purpose,
                            ),
                        ) {
                            if (isConcernEditMode) {
                                navController.popBackStack(Screen.PROFILE_CONCERN, inclusive = false)
                            } else {
                                navController.navigate(Screen.ONBOARDING_COMPLETE)
                            }
                        }
                    }
                },
            )
        }
        composable(Screen.ONBOARDING_COMPLETE) {
            val context = LocalContext.current
            val authViewModel: AuthViewModel = hiltViewModel()
            val authUiState by authViewModel.uiState.collectAsState()

            authUiState.errorMessage?.let { message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                authViewModel.clearError()
            }

            OnboardingCompleteScreen(
                onFinished = {
                    authViewModel.completeOnboarding {
                        navController.navigate(Screen.HOME) {
                            popUpTo(Screen.ONBOARDING_COMPLETE) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                },
            )
        }
        // 하단 네비게이션 4개 탭(홈·미션·보관함·프로필)은 하나의 HorizontalPager(MainTabsPager)에서
        // 손가락 추종 스와이프로 전환됩니다. 4개 route 모두 같은 페이저 셸을 렌더하며,
        // 실제 표시 페이지는 MainScreen이 pagerState로 제어합니다(진입 시 해당 탭으로 이동).
        composable(Screen.HOME) {
            MainTabsPager(navController, pagerState, onOverlaySheetTop)
        }
        // 알림창(종 모양 진입). 디자인 미완성이므로 빈 상태 placeholder.
        composable(Screen.NOTIFICATION) {
            NotificationScreen(onBack = { navController.popBackStack() })
        }
        // C담당: 아카이브 홈 화면 (하단 탭 = MainTabsPager의 페이저 페이지)
        composable(Screen.ARCHIVE_HOME) {
            MainTabsPager(navController, pagerState, onOverlaySheetTop)
        }
        // C담당: 아카이브 검색 화면
        composable(Screen.ARCHIVE_SEARCH) {
            ArchiveSearchScreen(
                onBackClick = {
                    navController.popBackStack()
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

        // C담당: 아카이브 탭 목록 화면 (미션/대화/문장/리포트)
        composable(
            route = "${Screen.ARCHIVE_LIST}/{tabIndex}",
            arguments = listOf(navArgument("tabIndex") { type = NavType.IntType; defaultValue = 0 })
        ) { backStackEntry ->
            val tabIndex = backStackEntry.arguments?.getInt("tabIndex") ?: 0
            ArchiveListScreen(
                initialTabIndex = tabIndex,
                onBackClick = { navController.popBackStack() },
                // 💡 [수정] 보관함 리스트 화면에서 미션 카드 클릭 시 미션 상세 화면으로 이동하도록 연결 완료!
                onMissionClick = { missionId: String ->
                    navController.navigate("mission_detail/$missionId")
                },
                // 💡 C담당: 전달 파라미터 타입 명시 유지
                onConversationClick = { conversationId: String ->
                    navController.navigate("archive_conversation_detail/$conversationId")
                },
                onSentenceClick = { phraseId: String ->
                    navController.navigate("archive_saved_phrase/$phraseId")
                },
                onReportClick = { reportId: String ->
                    navController.navigate("archive_report/$reportId")
                }
            )
        }

        // C담당: 보관함 대화 기록(상세) 화면
        composable(
            route = "archive_conversation_detail/{conversationId}",
            arguments = listOf(navArgument("conversationId") { type = NavType.StringType })
        ) {
            ArchiveConversationDetailScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // C담당: 보관함 베스트 문장 상세 화면
        composable(
            route = "archive_saved_phrase/{phraseId}",
            arguments = listOf(navArgument("phraseId") { type = NavType.StringType })
        ) {
            ArchiveSavedPhraseScreen(
                onBackClick = { navController.popBackStack() },
                onConversationClick = { conversationId: String ->
                    navController.navigate("archive_conversation_detail/$conversationId")
                }
            )
        }

        // C?대떦: 蹂닿???由ы룷???곸꽭 ?붾㈃
        composable(
            route = "archive_report/{reportId}",
            arguments = listOf(navArgument("reportId") { type = NavType.StringType })
        ) {
            ArchiveReportScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // B담당: 미션 리스트 (하단 탭 = MainTabsPager의 페이저 페이지). 미션 카드 클릭 시 상세로 이동.
        composable(Screen.MISSION_LIST) {
            MainTabsPager(navController, pagerState, onOverlaySheetTop)
        }
        // B담당: 홈 "다른 미션 보기" 전용 미션 목록. 미션 탭과 같은 본문(북마크는 공유 저장소로 자동 동기화)이되
        // 헤더는 예전 CSS(뒤로가기 + "미션 목록")이고, 홈 위로 push 되어 홈 탭을 유지한다(TqBottomBar.tabRouteOf).
        composable(Screen.MISSION_LIST_HOME) {
            MissionListScreen(
                onBack = { navController.popBackStack() },
                onMissionClick = { missionId -> navController.navigate("mission_detail/$missionId") },
                onSheetTopChange = onOverlaySheetTop,
                onSavedListClick = { navController.navigate("${Screen.ARCHIVE_LIST}/0") },
                homeContext = true,
            )
        }
        // B담당: 미션 상세 화면. "시작" 버튼 클릭 시 대화 준비 화면으로, "저장" 클릭 시 보관함으로 이동.
        composable(
            route = Screen.MISSION_DETAIL,
            arguments = listOf(navArgument("missionId") { type = NavType.StringType }),
        ) {
            MissionDetailScreen(
                onBack = { navController.popBackStack() },
                onNextClick = { missionId -> navController.navigate("conversation_setup_1/$missionId") },
                onMissionClick = { missionId -> navController.navigate("mission_detail/$missionId") },
                onSheetTopChange = onOverlaySheetTop,
                onSavedListClick = { navController.navigate("${Screen.ARCHIVE_LIST}/0") },
            )
        }
        // B담당: 미션 진입 · 대화 설정 4스텝. 상세 "다음" → 1(장소)→2(상대)→3(성별·나이)→4(친밀도·말투) → 대화.
        composable(
            route = Screen.CONVERSATION_SETUP_1,
            arguments = listOf(navArgument("missionId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val missionId = backStackEntry.arguments?.getString("missionId").orEmpty()
            ConversationSetup1Screen(
                onBack = { navController.popBackStack() },
                onNext = { navController.navigate("conversation_setup_2/$missionId") },
            )
        }
        composable(
            route = Screen.CONVERSATION_SETUP_2,
            arguments = listOf(navArgument("missionId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val missionId = backStackEntry.arguments?.getString("missionId").orEmpty()
            ConversationSetup2Screen(
                onBack = { navController.popBackStack() },
                onNext = { navController.navigate("conversation_setup_3/$missionId") },
            )
        }
        composable(
            route = Screen.CONVERSATION_SETUP_3,
            arguments = listOf(navArgument("missionId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val missionId = backStackEntry.arguments?.getString("missionId").orEmpty()
            ConversationSetup3Screen(
                onBack = { navController.popBackStack() },
                onNext = { navController.navigate("conversation_setup_4/$missionId") },
            )
        }
        composable(
            route = Screen.CONVERSATION_SETUP_4,
            arguments = listOf(navArgument("missionId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val missionId = backStackEntry.arguments?.getString("missionId").orEmpty()
            ConversationSetup4Screen(
                onBack = { navController.popBackStack() },
                onNext = { navController.navigate("conversation/$missionId") },
            )
        }
        // B담당: 대화 진행 화면. 종료 시 미션 완료&XP 화면으로 넘어감.
        composable(
            route = Screen.CONVERSATION,
            arguments = listOf(navArgument("conversationId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val missionId = backStackEntry.arguments?.getString("conversationId").orEmpty()
            ConversationScreen(
                onExitConfirm = { durationSec ->
                    // 대화 종료 시 소요 시간 등을 파라미터로 넘겨 미션 완료 화면으로 전달.
                    navController.navigate("mission_complete/$missionId?durationSec=$durationSec") {
                        popUpTo(Screen.HOME)
                    }
                },
            )
        }
        // B담당: 미션 완료&XP. 이후 AI 피드백 화면으로 진입 (NAVIGATION.md 기준).
        composable(
            route = "${Screen.MISSION_COMPLETE}?durationSec={durationSec}",
            arguments = listOf(
                navArgument("missionId") { type = NavType.StringType },
                navArgument("durationSec") { type = NavType.LongType; defaultValue = 0L },
            ),
        ) { backStackEntry ->
            val missionId = backStackEntry.arguments?.getString("missionId").orEmpty()
            MissionCompleteScreen(
                // 완료 시 생성된 feedbackId로 이동(POST /feedback). 미생성(데모/실패)이면 missionId 폴백(stub 경로).
                onContinue = { feedbackId -> navController.navigate("feedback/${feedbackId ?: missionId}") },
            )
        }
        // B담당: AI 피드백 화면. 항목 클릭 시 피드백 상세 보기 (NAVIGATION.md).
        // "상세 리포트" 버튼 클릭 시 리포트 화면으로 이동. "홈으로" 클릭 시 홈으로 복귀.
        composable(
            route = Screen.FEEDBACK,
            arguments = listOf(navArgument("feedbackId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val feedbackId = backStackEntry.arguments?.getString("feedbackId").orEmpty()
            FeedbackScreen(
                onBack = { navController.popBackStack() },
                onItemClick = { index -> navController.navigate("feedback_detail/$feedbackId?item=$index") },
                // 피드백 진입 시 기존 뷰모델 스택 유지를 위해 홈 팝업 처리 고려
                // URI 인코딩 처리를 통해 파라미터 전달.
                onDetailReport = { missionTitle ->
                    navController.navigate("report?missionTitle=${Uri.encode(missionTitle)}")
                },
                onHome = { navController.popBackStack(Screen.HOME, inclusive = false) },
            )
        }
        // B담당: 리포트(성장/주간 등). 피드백 화면에서 상세 리포트 클릭 시 진입.
        composable(
            route = Screen.REPORT,
            arguments = listOf(
                navArgument("missionTitle") { type = NavType.StringType; defaultValue = "" },
            ),
        ) {
            ReportScreen(
                onBack = { navController.popBackStack() },
                onSheetTopChange = onOverlaySheetTop, // 리포트 바텀시트가 하단 탭을 덮는 동안 탭 가림
                onArchiveClick = { navController.navigate("${Screen.ARCHIVE_LIST}/3") },
                // 💡 [수정] 보관함 리포트 상세로 이동 연동 완료
                onReportClick = { reportId ->
                    navController.navigate("archive_report/$reportId")
                },
            )
        }
        // B담당: AI 피드백 상세 화면. "다른 미션 둘러보기" 클릭 시 미션 목록으로, "보관함" 클릭 시 보관함으로 이동.
        composable(
            route = "${Screen.FEEDBACK_DETAIL}?item={item}",
            arguments = listOf(
                navArgument("feedbackId") { type = NavType.StringType },
                navArgument("item") { type = NavType.IntType; defaultValue = 0 },
            ),
        ) {
            FeedbackDetailScreen(
                onBack = { navController.popBackStack() },
                onOtherMissions = {
                    navController.navigate(Screen.MISSION_LIST) { popUpTo(Screen.HOME) }
                },
                onArchiveClick = { navController.navigate("${Screen.ARCHIVE_LIST}/2") },
                onPhraseClick = { phraseId -> navController.navigate("archive_saved_phrase/$phraseId") },
            )
        }
        composable(Screen.COMMUNITY_LIST) { PlaceholderScreen("모임") }
        // A\uB2F4\uB2F9: \uD504\uB85C\uD544 (\uD558\uB2E8 \uD0ED = MainTabsPager\uC758 \uD398\uC774\uC800 \uD398\uC774\uC9C0)
        composable(Screen.PROFILE) {
            MainTabsPager(navController, pagerState, onOverlaySheetTop)
        }
        composable(Screen.PROFILE_BADGES) {
            val context = LocalContext.current
            val profileViewModel: ProfileViewModel = hiltViewModel()
            val profileUiState by profileViewModel.uiState.collectAsState()
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

            LaunchedEffect(Unit) {
                profileViewModel.loadBadges()
            }

            profileUiState.errorMessage?.let { message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                profileViewModel.clearError()
            }

            ProfileBadgesScreen(
                badges = badges,
                onBack = { navController.popBackStack() },
            )
        }
        composable(Screen.PROFILE_RECENT_MISSION) {
            ProfileRecentMissionScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.PROFILE_SETTINGS) {
            val context = LocalContext.current
            val profileViewModel: ProfileViewModel = hiltViewModel()
            val profileUiState by profileViewModel.uiState.collectAsState()
            val settings = profileUiState.settings

            LaunchedEffect(Unit) {
                profileViewModel.loadSettings()
            }

            profileUiState.errorMessage?.let { message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                profileViewModel.clearError()
            }

            ProfileSettingsScreen(
                initialPushEnabled = settings?.let { it.communityApproved || it.reportReady || it.marketing } ?: true,
                initialReminderEnabled = settings?.missionReminder ?: false,
                onPushEnabledChange = profileViewModel::updatePushNotifications,
                onReminderEnabledChange = profileViewModel::updateMissionReminder,
                onBack = { navController.popBackStack() },
                onEditProfileClick = { navController.navigate(Screen.PROFILE_INFO) },
                onTermsClick = { navController.navigate(Screen.PROFILE_TERMS) },
                onSupportClick = { navController.navigate(Screen.PROFILE_SUPPORT) },
                onWithdrawClick = { navController.navigate(Screen.PROFILE_WITHDRAW) },
            )

        }
        composable(Screen.PROFILE_INFO) {
            val context = LocalContext.current
            val profileViewModel: ProfileViewModel = hiltViewModel()
            val profileUiState by profileViewModel.uiState.collectAsState()
            val profile = profileUiState.profile
            val nickname = profile?.nickname ?: profile?.name ?: "\uB2E4\uBBFC"
            val connectedAccount = profile?.name?.takeIf { it.contains("@") } ?: "talkqquest@naver.com"

            profileUiState.errorMessage?.let { message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                profileViewModel.clearError()
            }

            ProfileInfoScreen(
                nickname = nickname,
                connectedAccount = connectedAccount,
                onBack = { navController.popBackStack() },
                onNicknameClick = { navController.navigate(Screen.PROFILE_NICKNAME_EDIT) },
                onAvatarClick = { uri ->
                    val imagePart = uri.toProfileImagePart(context)
                    if (imagePart == null) {
                        Toast.makeText(context, "\uC774\uBBF8\uC9C0\uB97C \uBD88\uB7EC\uC624\uC9C0 \uBABB\uD588\uC5B4\uC694.", Toast.LENGTH_SHORT).show()
                    } else {
                        profileViewModel.uploadProfileImage(imagePart)
                    }
                },
                onPasswordClick = { navController.navigate(Screen.PROFILE_PASSWORD_CHANGE) },
                onConnectedAccountClick = { navController.navigate(Screen.PROFILE_CONNECTED_ACCOUNT) },
                onConcernClick = { navController.navigate(Screen.PROFILE_CONCERN) },
            )
        }
        composable(Screen.PROFILE_NICKNAME_EDIT) {
            val context = LocalContext.current
            val profileViewModel: ProfileViewModel = hiltViewModel()
            val profileUiState by profileViewModel.uiState.collectAsState()
            val currentNickname = profileUiState.profile?.nickname ?: profileUiState.profile?.name ?: "\uC18C\uB2E4123"

            profileUiState.errorMessage?.let { message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                profileViewModel.clearError()
            }

            ProfileNicknameEditScreen(
                initialNickname = currentNickname,
                onBack = { navController.popBackStack() },
                onSaveClick = { nickname ->
                    profileViewModel.updateNickname(nickname.trim()) {
                        navController.popBackStack()
                    }
                },
            )
        }
        composable(Screen.PROFILE_PASSWORD_CHANGE) {
            val context = LocalContext.current
            val profileViewModel: ProfileViewModel = hiltViewModel()
            val profileUiState by profileViewModel.uiState.collectAsState()
            var currentPasswordError by remember { mutableStateOf(false) }

            profileUiState.errorMessage?.let { message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                profileViewModel.clearError()
            }

            ProfilePasswordChangeScreen(
                currentPasswordError = currentPasswordError,
                onBack = { navController.popBackStack() },
                onNextClick = { currentPassword ->
                    currentPasswordError = false
                    profileViewModel.verifyCurrentPassword(
                        currentPassword = currentPassword,
                        onSuccess = { navController.navigate(Screen.PROFILE_NEW_PASSWORD) },
                        onInvalidPassword = { currentPasswordError = true },
                    )
                },
            )
        }
        composable(Screen.PROFILE_NEW_PASSWORD) {
            val context = LocalContext.current
            val profileViewModel: ProfileViewModel = hiltViewModel()
            val profileUiState by profileViewModel.uiState.collectAsState()
            var showPasswordChangedDialog by remember { mutableStateOf(false) }

            profileUiState.errorMessage?.let { message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                profileViewModel.clearError()
            }

            ProfileNewPasswordScreen(
                onBack = { navController.popBackStack() },
                onConfirmClick = { newPassword ->
                    profileViewModel.changePassword(newPassword) {
                        showPasswordChangedDialog = true
                    }
                },
                showCompletionDialog = showPasswordChangedDialog,
                onCompletionConfirm = {
                    showPasswordChangedDialog = false
                    navController.popBackStack(Screen.PROFILE_INFO, inclusive = false)
                },
            )
        }
        composable(Screen.PROFILE_CONNECTED_ACCOUNT) {
            val context = LocalContext.current
            val authViewModel: AuthViewModel = hiltViewModel()
            val authUiState by authViewModel.uiState.collectAsState()

            authUiState.errorMessage?.let { message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                authViewModel.clearError()
            }

            val profileViewModel: ProfileViewModel = hiltViewModel()
            val profileUiState by profileViewModel.uiState.collectAsState()
            val connectedAccount = profileUiState.profile?.name?.takeIf { it.contains("@") } ?: "talkqquest@naver.com"

            ProfileConnectedAccountScreen(
                connectedAccount = connectedAccount,
                onBack = { navController.popBackStack() },
                onLogoutClick = {
                    authViewModel.logout {
                        navController.navigate(Screen.LOGIN) {
                            popUpTo(Screen.HOME) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                },
            )
        }
        composable(Screen.PROFILE_CONCERN) {
            ProfileConcernScreen(
                onBack = { navController.popBackStack() },
                onPersonalityClick = { navController.navigate(Screen.ONBOARDING_PERSONALITY) },
                onDifficultyClick = { navController.navigate(Screen.ONBOARDING_DIFFICULTY) },
                onGoalClick = { navController.navigate(Screen.ONBOARDING_GOAL) },
            )
        }
        composable(Screen.PROFILE_TERMS) {
            ProfileTermsScreen(
                onBack = { navController.popBackStack() },
                onTermsClick = { navController.navigate(Screen.PROFILE_SERVICE_TERMS) },
                onPrivacyClick = { navController.navigate(Screen.PROFILE_PRIVACY_POLICY) },
            )
        }
        composable(Screen.PROFILE_SERVICE_TERMS) {
            val context = LocalContext.current
            val profileViewModel: ProfileViewModel = hiltViewModel()
            val profileUiState by profileViewModel.uiState.collectAsState()

            LaunchedEffect(Unit) {
                profileViewModel.loadServiceTerms()
            }

            profileUiState.errorMessage?.let { message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                profileViewModel.clearError()
            }

            ProfileTermsDetailScreen(
                title = "\uC774\uC6A9\uC57D\uAD00",
                sections = ServiceTermsSections,
                content = profileUiState.serviceTerms?.content,
                onBack = { navController.popBackStack() },
            )
        }
        composable(Screen.PROFILE_PRIVACY_POLICY) {
            val context = LocalContext.current
            val profileViewModel: ProfileViewModel = hiltViewModel()
            val profileUiState by profileViewModel.uiState.collectAsState()

            LaunchedEffect(Unit) {
                profileViewModel.loadPrivacyPolicy()
            }

            profileUiState.errorMessage?.let { message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                profileViewModel.clearError()
            }

            ProfileTermsDetailScreen(
                title = "\uAC1C\uC778\uC815\uBCF4 \uCC98\uB9AC \uBC29\uCE68",
                sections = PrivacyPolicySections,
                content = profileUiState.privacyPolicy?.content,
                onBack = { navController.popBackStack() },
            )
        }
        composable(Screen.PROFILE_SUPPORT) {
            ProfileSupportScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.PROFILE_WITHDRAW) {
            val context = LocalContext.current
            val authViewModel: AuthViewModel = hiltViewModel()
            val authUiState by authViewModel.uiState.collectAsState()

            authUiState.errorMessage?.let { message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                authViewModel.clearError()
            }

            ProfileWithdrawScreen(
                onBack = { navController.popBackStack() },
                onWithdrawConfirm = {
                    authViewModel.withdraw {
                        navController.navigate(Screen.LOGIN) {
                            popUpTo(Screen.HOME) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                },
            )
        }
    }
}















private fun Uri.toProfileImagePart(context: Context): MultipartBody.Part? {
    val resolver = context.contentResolver
    val mimeType = resolver.getType(this)?.takeIf { it == "image/jpeg" || it == "image/png" } ?: return null
    val bytes = resolver.openInputStream(this)?.use { it.readBytes() } ?: return null
    val extension = if (mimeType == "image/png") "png" else "jpg"
    val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
    return MultipartBody.Part.createFormData("image", "profile_image.$extension", requestBody)
}

