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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.talkqquest.app.feature.mission.viewmodel.ConversationSetupViewModel
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
import com.talkqquest.app.feature.auth.ui.SignupTermsScreen
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
import com.talkqquest.app.feature.report.ui.WeeklyCompareScreen
import com.talkqquest.app.feature.archive.ui.ArchiveListScreen
import com.talkqquest.app.feature.archive.ui.ArchiveSearchScreen
import com.talkqquest.app.feature.archive.ui.ArchiveConversationDetailScreen
import com.talkqquest.app.feature.archive.ui.ArchiveSavedPhraseScreen
import com.talkqquest.app.feature.archive.ui.ArchiveReportScreen
import com.talkqquest.app.feature.archive.ui.ArchiveWeeklyCompareReportScreen
import com.talkqquest.app.feature.archive.viewmodel.ActivityType
import com.talkqquest.app.navigation.Screen
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

@Composable
fun NavGraph(
    navController: NavHostController,
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    onOverlaySheetTop: (Float?) -> Unit = {},
) {
    val tabRoutes = BottomNavItem.entries.map { it.route }.toSet()
    fun AnimatedContentTransitionScope<NavBackStackEntry>.isTabSwitch() =
        initialState.destination.route in tabRoutes && targetState.destination.route in tabRoutes
    val slideSpec = tween<IntOffset>(300)
    var isConcernEditMode by remember { mutableStateOf(false) }
    var concernPersonalityType by remember { mutableStateOf("introvert") }
    var concernDifficultSituations by remember { mutableStateOf(listOf("주제고민", "말문 막힘")) }
    var concernPurposes by remember { mutableStateOf(listOf("침묵 줄이기", "친해지는 대화")) }

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
                        Toast.makeText(context, "\uB124\uD2B8\uC6CC\uD06C \uC5F0\uACB0\uC744 \uD655\uC778\uD574\uC8FC\uC138\uC694.", Toast.LENGTH_SHORT).show()
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

            fun navigateAfterSocialLogin(isNewUser: Boolean) {
                val destination = if (isNewUser) Screen.SIGNUP_TERMS_SOCIAL else Screen.HOME
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
                                    navigateAfterSocialLogin(data.isNewUser)
                                }
                            }
                            .onFailure { error ->
                                Toast.makeText(context, error.message ?: "Kakao login failed.", Toast.LENGTH_SHORT).show()
                            }
                    }
                },
                onNaverClick = {
                    scope.launch {
                        NaverLoginClient.login(context)
                            .onSuccess { providerAccessToken ->
                                authViewModel.loginWithNaver(providerAccessToken) { data ->
                                    navigateAfterSocialLogin(data.isNewUser)
                                }
                            }
                            .onFailure { error ->
                                Toast.makeText(context, error.message ?: "Naver login failed.", Toast.LENGTH_SHORT).show()
                            }
                    }
                },
                onEmailSignupClick = { navController.navigate(Screen.SIGNUP_TERMS) },
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
                    Toast.makeText(context, "\uBE44\uBC00\uBC88\uD638 \uCC3E\uAE30\uB294 \uC900\uBE44 \uC911\uC785\uB2C8\uB2E4.", Toast.LENGTH_SHORT).show()
                },
                errorMessage = authUiState.errorMessage,
            )
        }

        composable(Screen.SIGNUP_TERMS) {
            val authViewModel: AuthViewModel = hiltViewModel()
            val authUiState by authViewModel.uiState.collectAsState()

            LaunchedEffect(Unit) {
                authViewModel.loadSignupLegalDocuments()
            }

            SignupTermsScreen(
                serviceTermsContent = authUiState.serviceTerms?.content,
                privacyPolicyContent = authUiState.privacyPolicy?.content,
                isLoading = authUiState.isLoading,
                errorMessage = authUiState.errorMessage,
                onCloseClick = { navController.popBackStack() },
                onAgreeClick = {
                    navController.navigate(Screen.SIGNUP_EMAIL) {
                        popUpTo(Screen.SIGNUP_TERMS) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }

        composable(Screen.SIGNUP_TERMS_SOCIAL) {
            val authViewModel: AuthViewModel = hiltViewModel()
            val authUiState by authViewModel.uiState.collectAsState()

            LaunchedEffect(Unit) {
                authViewModel.loadSignupLegalDocuments()
            }

            SignupTermsScreen(
                serviceTermsContent = authUiState.serviceTerms?.content,
                privacyPolicyContent = authUiState.privacyPolicy?.content,
                isLoading = authUiState.isLoading,
                errorMessage = authUiState.errorMessage,
                onCloseClick = {
                    navController.navigate(Screen.LOGIN) {
                        popUpTo(Screen.SIGNUP_TERMS_SOCIAL) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onAgreeClick = {
                    navController.navigate(Screen.SIGNUP_NICKNAME_SOCIAL) {
                        popUpTo(Screen.SIGNUP_TERMS_SOCIAL) { inclusive = true }
                        launchSingleTop = true
                    }
                },
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
                        navController.currentBackStackEntry?.savedStateHandle?.set("signup_email", email.trim())
                        navController.navigate(Screen.SIGNUP_VERIFY)
                    }
                },
            )
        }

        composable(Screen.SIGNUP_VERIFY) {
            val context = LocalContext.current
            val authViewModel: AuthViewModel = hiltViewModel()
            val authUiState by authViewModel.uiState.collectAsState()
            val email = navController.previousBackStackEntry?.savedStateHandle?.get<String>("signup_email").orEmpty()
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
                        navController.currentBackStackEntry?.savedStateHandle?.set("signup_email", email)
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
                        Toast.makeText(context, "\uC778\uC99D \uCF54\uB4DC\uAC00 \uC7AC\uBC1C\uC1A1\uB418\uC5C8\uC2B5\uB2C8\uB2E4.", Toast.LENGTH_SHORT).show()
                    }
                },
            )
        }

        composable(Screen.SIGNUP_PASSWORD) {
            val email = navController.previousBackStackEntry?.savedStateHandle?.get<String>("signup_email").orEmpty()

            SignupPasswordScreen(
                onBack = { navController.popBackStack() },
                onNextClick = { password ->
                    navController.currentBackStackEntry?.savedStateHandle?.set("signup_email", email)
                    navController.currentBackStackEntry?.savedStateHandle?.set("signup_password", password)
                    navController.navigate(Screen.SIGNUP_NICKNAME)
                },
            )
        }

        composable(Screen.SIGNUP_NICKNAME) {
            val context = LocalContext.current
            val authViewModel: AuthViewModel = hiltViewModel()
            val authUiState by authViewModel.uiState.collectAsState()
            val email = navController.previousBackStackEntry?.savedStateHandle?.get<String>("signup_email").orEmpty()
            val password = navController.previousBackStackEntry?.savedStateHandle?.get<String>("signup_password").orEmpty()

            authUiState.errorMessage?.let { message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                authViewModel.clearError()
            }

            SignupNicknameScreen(
                onBack = { navController.popBackStack() },
                onCompleteClick = { nickname ->
                    authViewModel.signupWithEmail(email = email, password = password, nickname = nickname) {
                        navController.currentBackStackEntry?.savedStateHandle?.set("onboarding_nickname", nickname.trim())
                        navController.navigate(Screen.ONBOARDING_WELCOME) {
                            popUpTo(Screen.SIGNUP_NICKNAME) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                },
            )
        }

        composable(Screen.SIGNUP_NICKNAME_SOCIAL) {
            val context = LocalContext.current
            val authViewModel: AuthViewModel = hiltViewModel()
            val authUiState by authViewModel.uiState.collectAsState()

            authUiState.errorMessage?.let { message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                authViewModel.clearError()
            }

            SignupNicknameScreen(
                onBack = {
                    navController.navigate(Screen.LOGIN) {
                        popUpTo(Screen.SIGNUP_NICKNAME_SOCIAL) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onCompleteClick = { nickname ->
                    authViewModel.updateSocialNickname(nickname) {
                        navController.currentBackStackEntry?.savedStateHandle?.set("onboarding_nickname", nickname.trim())
                        navController.navigate(Screen.ONBOARDING_WELCOME)
                    }
                },
            )
        }

        composable(Screen.ONBOARDING_WELCOME) {
            val nickname = navController.previousBackStackEntry?.savedStateHandle?.get<String>("onboarding_nickname").orEmpty()
            OnboardingWelcomeScreen(
                nickname = nickname,
                onFinished = { displayNickname ->
                    isConcernEditMode = false
                    navController.navigate(Screen.ONBOARDING_PERSONALITY) {
                        popUpTo(Screen.ONBOARDING_WELCOME) { inclusive = true }
                        launchSingleTop = true
                    }
                    navController.currentBackStackEntry?.savedStateHandle?.set("onboarding_nickname", displayNickname)
                },
            )
        }

        composable(Screen.ONBOARDING_PERSONALITY) {
            val context = LocalContext.current
            val authViewModel: AuthViewModel = hiltViewModel()
            val authUiState by authViewModel.uiState.collectAsState()
            val profileViewModel: ProfileViewModel = hiltViewModel()
            val profileUiState by profileViewModel.uiState.collectAsState()
            val nickname = if (isConcernEditMode) {
                profileUiState.profile?.nickname?.takeIf { it.isNotBlank() }
                    ?: profileUiState.dashboard?.nickname?.takeIf { it.isNotBlank() }
                    ?: profileUiState.profile?.name?.takeIf { it.isNotBlank() }
                    ?: navController.currentBackStackEntry?.savedStateHandle?.get<String>("onboarding_nickname").orEmpty()
            } else {
                navController.currentBackStackEntry?.savedStateHandle?.get<String>("onboarding_nickname").orEmpty()
            }

            authUiState.errorMessage?.let { message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                authViewModel.clearError()
            }

            OnboardingPersonalityScreen(
                nickname = nickname,
                initialPersonalityType = if (isConcernEditMode) concernPersonalityType else "introvert",
                onBack = {
                    if (isConcernEditMode) isConcernEditMode = false
                    navController.popBackStack()
                },
                onNextClick = { personalityType ->
                    if (isConcernEditMode) {
                        concernPersonalityType = personalityType
                        navController.navigate(Screen.ONBOARDING_DIFFICULTY)
                    } else {
                        authViewModel.saveOnboardingStep(
                            OnboardingStepSaveRequest(
                                step = 1,
                                personalityType = personalityType,
                            ),
                        ) {
                            navController.navigate(Screen.ONBOARDING_DIFFICULTY)
                        }
                    }
                },
            )
        }

        composable(Screen.ONBOARDING_DIFFICULTY) {
            val context = LocalContext.current
            val authViewModel: AuthViewModel = hiltViewModel()
            val authUiState by authViewModel.uiState.collectAsState()

            authUiState.errorMessage?.let { message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                authViewModel.clearError()
            }

            OnboardingDifficultyScreen(
                initialSelected = if (isConcernEditMode) concernDifficultSituations else emptyList(),
                onBack = { navController.popBackStack() },
                onNextClick = { difficultSituations ->
                    if (difficultSituations.isEmpty()) {
                        Toast.makeText(context, "어려운 점을 선택해주세요.", Toast.LENGTH_SHORT).show()
                    } else if (isConcernEditMode) {
                        concernDifficultSituations = difficultSituations
                        navController.navigate(Screen.ONBOARDING_GOAL)
                    } else {
                        authViewModel.saveOnboardingStep(
                            OnboardingStepSaveRequest(
                                step = 2,
                                difficultSituations = difficultSituations,
                            ),
                        ) {
                            navController.navigate(Screen.ONBOARDING_GOAL)
                        }
                    }
                },
            )
        }

        composable(Screen.ONBOARDING_GOAL) {
            val context = LocalContext.current
            val authViewModel: AuthViewModel = hiltViewModel()
            val authUiState by authViewModel.uiState.collectAsState()

            authUiState.errorMessage?.let { message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                authViewModel.clearError()
            }

            OnboardingGoalScreen(
                initialSelected = if (isConcernEditMode) concernPurposes else emptyList(),
                completeButtonText = if (isConcernEditMode) "저장" else "완료",
                onBack = { navController.popBackStack() },
                onCompleteClick = { purpose ->
                    if (purpose.isEmpty()) {
                        Toast.makeText(context, "연습 목표를 선택해주세요.", Toast.LENGTH_SHORT).show()
                    } else if (isConcernEditMode) {
                        concernPurposes = purpose
                        authViewModel.saveOnboardingStep(
                            OnboardingStepSaveRequest(step = 1, personalityType = concernPersonalityType),
                        ) {
                            authViewModel.saveOnboardingStep(
                                OnboardingStepSaveRequest(step = 2, difficultSituations = concernDifficultSituations),
                            ) {
                                authViewModel.saveOnboardingStep(
                                    OnboardingStepSaveRequest(step = 3, purpose = concernPurposes),
                                ) {
                                    isConcernEditMode = false
                                    navController.popBackStack(Screen.PROFILE_CONCERN, inclusive = false)
                                }
                            }
                        }
                    } else {
                        authViewModel.saveOnboardingStep(
                            OnboardingStepSaveRequest(
                                step = 3,
                                purpose = purpose,
                            ),
                        ) {
                            navController.navigate(Screen.ONBOARDING_COMPLETE)
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

        composable(Screen.HOME) {
            MainTabsPager(navController, pagerState, onOverlaySheetTop)
        }

        composable(Screen.NOTIFICATION) {
            NotificationScreen(
                onBack = { navController.popBackStack() },
                onWeeklyReportClick = { navController.navigate(Screen.WEEKLY_COMPARE) },
            )
        }

        composable(Screen.WEEKLY_COMPARE) {
            WeeklyCompareScreen(
                onClose = { navController.popBackStack() },
                onCompletedMissionsClick = {},
            )
        }

        composable(Screen.ARCHIVE_HOME) {
            MainTabsPager(navController, pagerState, onOverlaySheetTop)
        }

        composable(Screen.ARCHIVE_SEARCH) {
            ArchiveSearchScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                // 💡 [수정] ActivityType.REPORT인 경우 isWeeklyCompare 정보를 추가로 받아 분기 처리합니다.
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

        composable(
            route = "${Screen.ARCHIVE_LIST}/{tabIndex}",
            arguments = listOf(navArgument("tabIndex") { type = NavType.IntType; defaultValue = 0 })
        ) { backStackEntry ->
            val tabIndex = backStackEntry.arguments?.getInt("tabIndex") ?: 0
            ArchiveListScreen(
                initialTabIndex = tabIndex,
                onBackClick = { navController.popBackStack() },
                onMissionClick = { missionId: String ->
                    navController.navigate("mission_detail/$missionId")
                },
                onConversationClick = { conversationId: String ->
                    navController.navigate("archive_conversation_detail/$conversationId")
                },
                onSentenceClick = { phraseId: String ->
                    navController.navigate("archive_saved_phrase/$phraseId")
                },
                onReportClick = { reportId: String, isWeeklyCompare: Boolean ->
                    if (isWeeklyCompare) {
                        navController.navigate("archive_weekly_compare_report/$reportId")
                    } else {
                        navController.navigate("archive_report/$reportId")
                    }
                }
            )
        }

        composable(
            route = "archive_conversation_detail/{conversationId}",
            arguments = listOf(navArgument("conversationId") { type = NavType.StringType })
        ) {
            ArchiveConversationDetailScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

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

        composable(
            route = "archive_report/{reportId}",
            arguments = listOf(navArgument("reportId") { type = NavType.StringType })
        ) {
            ArchiveReportScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = "archive_weekly_compare_report/{reportId}",
            arguments = listOf(navArgument("reportId") { type = NavType.StringType })
        ) {
            ArchiveWeeklyCompareReportScreen(
                onBackClick = { navController.popBackStack() },
                onCompletedMissionsClick = {
                    navController.navigate("${Screen.ARCHIVE_LIST}/0")
                }
            )
        }

        composable(Screen.MISSION_LIST) {
            MainTabsPager(navController, pagerState, onOverlaySheetTop)
        }

        composable(Screen.MISSION_LIST_HOME) {
            MissionListScreen(
                onBack = { navController.popBackStack() },
                onMissionClick = { missionId -> navController.navigate("mission_detail/$missionId") },
                onSheetTopChange = onOverlaySheetTop,
                onSavedListClick = { navController.navigate("${Screen.ARCHIVE_LIST}/0") },
                homeContext = true,
            )
        }

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

        // ── 대화 설정 1~4단계 ──
        // 네 화면이 하나의 ConversationSetupViewModel을 공유한다. 1단계의 backStackEntry에
        // 묶어 두면 2~4단계가 같은 인스턴스를 받고, 설정 흐름을 벗어나 그 entry가 사라질 때
        // 같이 정리된다. 각 화면이 자기 remember만 들고 있던 예전엔 고른 값이 다음 화면으로도
        // 서버로도 가지 않았다.
        composable(
            route = Screen.CONVERSATION_SETUP_1,
            arguments = listOf(navArgument("missionId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val missionId = backStackEntry.arguments?.getString("missionId").orEmpty()
            val setupViewModel: ConversationSetupViewModel = hiltViewModel(backStackEntry)
            ConversationSetup1Screen(
                onBack = { navController.popBackStack() },
                onNext = { navController.navigate("conversation_setup_2/$missionId") },
                onSelect = setupViewModel::selectEnvironment,
            )
        }

        composable(
            route = Screen.CONVERSATION_SETUP_2,
            arguments = listOf(navArgument("missionId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val missionId = backStackEntry.arguments?.getString("missionId").orEmpty()
            val setupViewModel = navController.conversationSetupViewModel(missionId)
            ConversationSetup2Screen(
                onBack = { navController.popBackStack() },
                onNext = { navController.navigate("conversation_setup_3/$missionId") },
                onSelect = setupViewModel::selectPartnerRole,
            )
        }

        composable(
            route = Screen.CONVERSATION_SETUP_3,
            arguments = listOf(navArgument("missionId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val missionId = backStackEntry.arguments?.getString("missionId").orEmpty()
            val setupViewModel = navController.conversationSetupViewModel(missionId)
            ConversationSetup3Screen(
                onBack = { navController.popBackStack() },
                onNext = { navController.navigate("conversation_setup_4/$missionId") },
                onSelectGender = setupViewModel::selectGender,
                onSelectAgeGroup = setupViewModel::selectAgeGroup,
            )
        }

        composable(
            route = Screen.CONVERSATION_SETUP_4,
            arguments = listOf(navArgument("missionId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val missionId = backStackEntry.arguments?.getString("missionId").orEmpty()
            val setupViewModel = navController.conversationSetupViewModel(missionId)
            val isSaving by setupViewModel.isSaving.collectAsStateWithLifecycle()
            ConversationSetup4Screen(
                onBack = { navController.popBackStack() },
                // 고른 6축을 서버에 저장한 뒤 대화로 넘어간다. 저장이 실패해도 넘어간다 —
                // 준비 정보는 AI가 참고하는 값이지 대화의 전제가 아니다.
                onNext = {
                    setupViewModel.saveAndStart(missionId) {
                        navController.navigate("conversation/$missionId")
                    }
                },
                onIntimacyChange = setupViewModel::selectIntimacy,
                onFormalityChange = setupViewModel::selectFormality,
                isSaving = isSaving,
            )
        }

        composable(
            route = Screen.CONVERSATION,
            arguments = listOf(navArgument("conversationId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val missionId = backStackEntry.arguments?.getString("conversationId").orEmpty()
            ConversationScreen(
                onExitConfirm = { durationSec ->
                    navController.navigate("mission_complete/$missionId?durationSec=$durationSec") {
                        popUpTo(Screen.HOME)
                    }
                },
                onBack = { navController.popBackStack(Screen.HOME, inclusive = false) },
            )
        }

        composable(
            route = "${Screen.MISSION_COMPLETE}?durationSec={durationSec}",
            arguments = listOf(
                navArgument("missionId") { type = NavType.StringType },
                navArgument("durationSec") { type = NavType.LongType; defaultValue = 0L },
            ),
        ) { backStackEntry ->
            val missionId = backStackEntry.arguments?.getString("missionId").orEmpty()
            MissionCompleteScreen(
                onContinue = { feedbackId -> navController.navigate("feedback/${feedbackId ?: missionId}") },
            )
        }

        composable(
            route = Screen.FEEDBACK,
            arguments = listOf(navArgument("feedbackId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val feedbackId = backStackEntry.arguments?.getString("feedbackId").orEmpty()
            FeedbackScreen(
                onBack = { navController.popBackStack() },
                onItemClick = { index -> navController.navigate("feedback_detail/$feedbackId?item=$index") },
                onDetailReport = { missionTitle, conversationId, scores ->
                    navController.navigate(
                        "report?missionTitle=${Uri.encode(missionTitle)}" +
                                "&conversationId=${Uri.encode(conversationId)}" +
                                "&gains=${scores.joinToString(",")}",
                    )
                },
                onHome = { navController.popBackStack(Screen.HOME, inclusive = false) },
            )
        }

        composable(
            route = Screen.REPORT,
            arguments = listOf(
                navArgument("missionTitle") { type = NavType.StringType; defaultValue = "" },
                navArgument("conversationId") { type = NavType.StringType; defaultValue = "" },
                navArgument("gains") { type = NavType.StringType; defaultValue = "" },
            ),
        ) {
            ReportScreen(
                onBack = { navController.popBackStack() },
                onSheetTopChange = onOverlaySheetTop,
                onArchiveClick = { navController.navigate("${Screen.ARCHIVE_LIST}/3") },
                onReportClick = { reportId ->
                    navController.navigate("archive_report/$reportId")
                },
            )
        }

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
            val profile = profileUiState.profile
            val dashboard = profileUiState.dashboard
            val nickname = dashboard?.nickname?.takeIf { it.isNotBlank() }
                ?: profile?.nickname
                ?: profile?.name
                ?: "다민"
            val connectedAccount = dashboard?.email?.takeIf { it.isNotBlank() }
                ?: profile?.name?.takeIf { it.contains("@") }
                ?: "이메일 정보 없음"
            LaunchedEffect(Unit) {
                profileViewModel.loadProfile()
                profileViewModel.loadDashboard()
                profileViewModel.loadSettings()
            }

            profileUiState.errorMessage?.let { message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                profileViewModel.clearError()
            }

            ProfileSettingsScreen(
                nickname = nickname,
                connectedAccount = connectedAccount,
                initialPushEnabled = settings?.let { it.communityApproved || it.reportReady || it.marketing } ?: true,
                initialReminderEnabled = settings?.missionReminder ?: false,
                initialReminderTime = settings?.missionReminderTime ?: "09:00",
                onPushEnabledChange = profileViewModel::updatePushNotifications,
                onReminderEnabledChange = profileViewModel::updateMissionReminder,
                onReminderTimeChange = profileViewModel::updateMissionReminderTime,
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
            val dashboard = profileUiState.dashboard
            val nickname = dashboard?.nickname?.takeIf { it.isNotBlank() }
                ?: profile?.nickname
                ?: profile?.name
                ?: "다민"
            val connectedAccount = dashboard?.email?.takeIf { it.isNotBlank() }
                ?: profile?.name?.takeIf { it.contains("@") }
                ?: "이메일 정보 없음"

            LaunchedEffect(Unit) {
                profileViewModel.loadDashboard()
            }

            profileUiState.errorMessage?.let { message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                profileViewModel.clearError()
            }

            ProfileInfoScreen(
                nickname = nickname,
                avatarUrl = dashboard?.avatarUrl ?: profile?.avatarUrl,
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
            val connectedAccount = profileUiState.dashboard?.email?.takeIf { it.isNotBlank() }
                ?: profileUiState.profile?.name?.takeIf { it.contains("@") }
                ?: "이메일 정보 없음"

            LaunchedEffect(Unit) {
                profileViewModel.loadDashboard()
            }

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
            fun startConcernEdit() {
                isConcernEditMode = true
                navController.navigate(Screen.ONBOARDING_PERSONALITY)
            }

            ProfileConcernScreen(
                onBack = { navController.popBackStack() },
                onPersonalityClick = { startConcernEdit() },
                onDifficultyClick = { startConcernEdit() },
                onGoalClick = { startConcernEdit() },
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

// 대화 설정 2~4단계가 1단계와 같은 ViewModel을 보게 하는 헬퍼.
// 1단계의 backStackEntry를 owner로 넘기면 그 entry가 살아 있는 동안(=설정 흐름을 도는 동안)
// 네 화면이 한 인스턴스를 공유하고, 흐름을 벗어나면 함께 정리된다.
@Composable
private fun NavHostController.conversationSetupViewModel(missionId: String): ConversationSetupViewModel {
    val firstStep = remember(missionId) { getBackStackEntry("conversation_setup_1/$missionId") }
    return hiltViewModel(firstStep)
}

private fun Uri.toProfileImagePart(context: Context): MultipartBody.Part? {
    val resolver = context.contentResolver
    val mimeType = resolver.getType(this)?.takeIf { it == "image/jpeg" || it == "image/png" } ?: return null
    val bytes = resolver.openInputStream(this)?.use { it.readBytes() } ?: return null
    val extension = if (mimeType == "image/png") "png" else "jpg"
    val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
    return MultipartBody.Part.createFormData("image", "profile_image.$extension", requestBody)
}
