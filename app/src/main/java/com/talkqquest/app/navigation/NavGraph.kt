package com.talkqquest.app.navigation

import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
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
import com.talkqquest.app.feature.profile.data.toProfileImagePart
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
import com.talkqquest.app.feature.home.viewmodel.HomeViewModel
import com.talkqquest.app.feature.report.ui.ReportScreen
import com.talkqquest.app.feature.report.ui.WeeklyCompareScreen
import com.talkqquest.app.feature.archive.ui.ArchiveListScreen
import com.talkqquest.app.feature.archive.ui.ArchiveSearchScreen
import com.talkqquest.app.feature.archive.ui.ArchiveConversationDetailScreen
import com.talkqquest.app.feature.archive.ui.ArchiveSavedPhraseScreen
import com.talkqquest.app.feature.archive.ui.ArchiveWeeklyCompareReportScreen
import com.talkqquest.app.feature.archive.viewmodel.ActivityType
import com.talkqquest.app.navigation.Screen
import com.talkqquest.app.core.designsystem.ModalDimDurationMillis
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val NOTIFICATION_READ_REFRESH_KEY = "notification_read_refresh"

@Composable
fun NavGraph(
    navController: NavHostController,
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    onOverlaySheetTop: (Float?) -> Unit = {},
    onShowWeeklyReportModal: (String?) -> Unit = {},
) {
    val tabRoutes = BottomNavItem.entries.map { it.route }.toSet()
    fun AnimatedContentTransitionScope<NavBackStackEntry>.isTabSwitch() =
        initialState.destination.route in tabRoutes && targetState.destination.route in tabRoutes
    val slideSpec = NavigationMotion.intOffsetSpec
    var isConcernEditMode by remember { mutableStateOf(false) }
    var concernPersonalityType by remember { mutableStateOf("introvert") }
    var concernRefreshKey by remember { mutableStateOf(0) }
    var concernDifficultSituations by remember { mutableStateOf(emptyList<String>()) }
    var concernPurposes by remember { mutableStateOf(emptyList<String>()) }

    NavHost(
        navController = navController,
        startDestination = Screen.SPLASH,
        modifier = modifier,
        enterTransition = {
            if (initialState.destination.route == Screen.SPLASH)
                fadeIn(NavigationMotion.floatSpec) + scaleIn(initialScale = 0.96f, animationSpec = NavigationMotion.floatSpec)
            else if (isTabSwitch()) fadeIn(NavigationMotion.floatSpec)
            else slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, slideSpec)
        },
        exitTransition = {
            if (initialState.destination.route == Screen.SPLASH) fadeOut(NavigationMotion.floatSpec)
            else if (isTabSwitch()) fadeOut(NavigationMotion.floatSpec)
            else if (targetState.destination.route == Screen.SIGNUP_VERIFY) ExitTransition.None
            else slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, slideSpec)
        },
        popEnterTransition = {
            if (isTabSwitch()) fadeIn(NavigationMotion.floatSpec)
            else if (initialState.destination.route == Screen.SIGNUP_VERIFY) EnterTransition.None
            else slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, slideSpec)
        },
        popExitTransition = {
            if (isTabSwitch()) fadeOut(NavigationMotion.floatSpec)
            else slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, slideSpec)
        },
    ) {
        composable(Screen.SPLASH) {
            val context = LocalContext.current
            val authViewModel: AuthViewModel = hiltViewModel()
            val minSplashMillis = 1000L
            val contentAlpha = remember { Animatable(0f) }
            var pendingDestination by remember { mutableStateOf<String?>(null) }
            val start = remember { System.currentTimeMillis() }
            fun navigateFromSplash(destination: String) {
                navController.navigate(destination) {
                    popUpTo(Screen.SPLASH) { inclusive = true }
                    launchSingleTop = true
                }
            }

            LaunchedEffect(Unit) {
                // checkStoredSession\uC740 \uB0B4\uBD80\uC5D0\uC11C viewModelScope.launch\uB85C \uB124\uD2B8\uC6CC\uD06C \uD638\uCD9C\uC744 \uB744\uC6B0\uACE0
                // \uC989\uC2DC \uBC18\uD658\uD55C\uB2E4 \u2014 \uBA3C\uC800 \uD638\uCD9C\uD574\uC57C \uC544\uB798 \uD398\uC774\uB4DC\uC778 \uB300\uAE30\uAC00 \uB124\uD2B8\uC6CC\uD06C \uC2DC\uC791\uC744 \uB2A6\uCD94\uC9C0 \uC54A\uB294\uB2E4.
                authViewModel.checkStoredSession(
                    onAuthenticated = { pendingDestination = Screen.HOME },
                    onUnauthenticated = { pendingDestination = Screen.LOGIN },
                    onNetworkError = {
                        Toast.makeText(context, "\uB124\uD2B8\uC6CC\uD06C \uC5F0\uACB0\uC744 \uD655\uC778\uD574\uC8FC\uC138\uC694.", Toast.LENGTH_SHORT).show()
                    },
                )
                contentAlpha.animateTo(1f, animationSpec = NavigationMotion.floatSpec)
            }

            LaunchedEffect(pendingDestination) {
                val destination = pendingDestination ?: return@LaunchedEffect
                val remaining = (minSplashMillis - (System.currentTimeMillis() - start)).coerceAtLeast(0L)
                delay(remaining)
                navigateFromSplash(destination)
            }
            SplashScreen(alpha = contentAlpha.value)
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
                isSending = authUiState.isLoading,
                onBack = { navController.popBackStack() },
                onSendClick = { email ->
                    authViewModel.requestEmailCode(email) {
                        navController.currentBackStackEntry?.savedStateHandle?.set("signup_email", email.trim())
                        navController.navigate(Screen.SIGNUP_VERIFY) {
                            launchSingleTop = true
                        }
                    }
                },
            )
        }

        composable(
            route = Screen.SIGNUP_VERIFY,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None },
        ) {
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
                onVerifyCode = { code, onSuccess ->
                    hasSubmittedVerification = true
                    isVerificationCodeError = false
                    authViewModel.verifyEmailCode(email, code) {
                        onSuccess()
                    }
                },
                onVerificationSuccess = {
                    navController.currentBackStackEntry?.savedStateHandle?.set("signup_email", email)
                    navController.navigate(Screen.SIGNUP_PASSWORD) {
                        popUpTo(Screen.SIGNUP_VERIFY) { inclusive = true }
                        launchSingleTop = true
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
                        navController.navigate(Screen.ONBOARDING_WELCOME) {
                            popUpTo(Screen.SIGNUP_NICKNAME) { inclusive = true }
                            launchSingleTop = true
                        }
                        navController.getBackStackEntry(Screen.ONBOARDING_WELCOME).savedStateHandle.set("onboarding_nickname", nickname.trim())
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
                        navController.navigate(Screen.ONBOARDING_WELCOME)
                        navController.getBackStackEntry(Screen.ONBOARDING_WELCOME).savedStateHandle.set("onboarding_nickname", nickname.trim())
                    }
                },
            )
        }

        composable(Screen.ONBOARDING_WELCOME) {
            val nickname = navController.currentBackStackEntry?.savedStateHandle?.get<String>("onboarding_nickname").orEmpty()
            OnboardingWelcomeScreen(
                nickname = nickname,
                onFinished = { displayNickname ->
                    isConcernEditMode = false
                    navController.navigate(Screen.ONBOARDING_PERSONALITY) {
                        popUpTo(Screen.ONBOARDING_WELCOME) { inclusive = true }
                        launchSingleTop = true
                    }
                    navController.getBackStackEntry(Screen.ONBOARDING_PERSONALITY).savedStateHandle.set("onboarding_nickname", displayNickname)
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
                        authViewModel.saveOnboardingStep(
                            OnboardingStepSaveRequest(step = 1, personalityType = personalityType),
                        ) {
                            concernRefreshKey += 1
                            isConcernEditMode = false
                            navController.popBackStack(Screen.PROFILE_CONCERN, inclusive = false)
                        }
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
                onBack = { isConcernEditMode = false; navController.popBackStack() },
                onNextClick = { difficultSituations ->
                    if (difficultSituations.isEmpty()) {
                        Toast.makeText(context, "어려운 점을 선택해주세요.", Toast.LENGTH_SHORT).show()
                    } else if (isConcernEditMode) {
                        concernDifficultSituations = difficultSituations
                        authViewModel.saveOnboardingStep(
                            OnboardingStepSaveRequest(step = 2, difficultSituations = difficultSituations),
                        ) {
                            concernRefreshKey += 1
                            isConcernEditMode = false
                            navController.popBackStack(Screen.PROFILE_CONCERN, inclusive = false)
                        }
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
            val profileViewModel: ProfileViewModel = hiltViewModel()
            val authUiState by authViewModel.uiState.collectAsState()

            authUiState.errorMessage?.let { message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                authViewModel.clearError()
            }

            OnboardingGoalScreen(
                initialSelected = if (isConcernEditMode) concernPurposes else emptyList(),
                completeButtonText = if (isConcernEditMode) "저장" else "완료",
                onBack = { isConcernEditMode = false; navController.popBackStack() },
                onCompleteClick = { purpose ->
                    if (purpose.isEmpty()) {
                        Toast.makeText(context, "연습 목표를 선택해주세요.", Toast.LENGTH_SHORT).show()
                    } else if (isConcernEditMode) {
                        concernPurposes = purpose
                        authViewModel.saveOnboardingStep(
                            OnboardingStepSaveRequest(step = 3, purpose = purpose),
                        ) {
                            concernRefreshKey += 1
                            profileViewModel.loadProfile()
                            isConcernEditMode = false
                            navController.popBackStack(Screen.PROFILE_CONCERN, inclusive = false)
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

        composable(Screen.HOME) { homeEntry ->
            val notificationReadRefresh by homeEntry.savedStateHandle
                .getStateFlow(NOTIFICATION_READ_REFRESH_KEY, 0L)
                .collectAsState()
            val homeViewModel: HomeViewModel = hiltViewModel(homeEntry)
            // 여기서의 LocalLifecycleOwner는 homeEntry(홈 백스택 엔트리)라서,
            // 다른 화면에 다녀와도 이 엔트리가 다시 RESUMED될 때마다 관측된다.
            // 미션 완료 등으로 바뀐 티어·별·XP를 홈 복귀 시점에 반영하기 위함.
            LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
                homeViewModel.onScreenResumed()
            }
            LaunchedEffect(notificationReadRefresh) {
                if (notificationReadRefresh != 0L) {
                    // 알림 종료 직후에는 XP 복귀 모션과 별개로 홈의 새 알림 상태만 다시 조회한다.
                    homeViewModel.loadHome(showLoading = false)
                }
            }
            MainTabsPager(navController, pagerState, onOverlaySheetTop, onShowWeeklyReportModal)
        }

        composable(Screen.NOTIFICATION) {
            NotificationScreen(
                onBack = {
                    // 상단 화살표와 시스템 뒤로가기가 모두 이 경로를 타며,
                    // 알림 Repository의 로컬 읽음 반영 뒤 홈에 독립적인 갱신 신호를 보낸다.
                    navController.previousBackStackEntry?.savedStateHandle?.set(
                        NOTIFICATION_READ_REFRESH_KEY,
                        System.nanoTime(),
                    )
                    navController.popBackStack()
                },
                // 알림이 가리키는 리포트로 바로 들어간다(서버 referenceId). 없으면 가장 최근 주차.
                onWeeklyReportClick = { reportId ->
                    navController.navigate("weekly_compare?reportId=${reportId.orEmpty()}")
                },
            )
        }

        composable(
            route = Screen.WEEKLY_COMPARE,
            arguments = listOf(
                navArgument("reportId") { type = NavType.StringType; defaultValue = "" },
            ),
        ) {
            WeeklyCompareScreen(
                onClose = { navController.popBackStack() },
                onCompletedMissionsClick = {
                    navController.navigate("${Screen.ARCHIVE_LIST}/1")
                },
                // 저장 시트의 "보관함 >" — 보관함 리포트 탭(3)으로 (성장 리포트 시트와 같은 목적지)
                onArchiveClick = { navController.navigate("${Screen.ARCHIVE_LIST}/3") },
                onReportClick = { reportId, isWeeklyCompare ->
                    if (isWeeklyCompare) {
                        navController.navigate("archive_weekly_compare_report/$reportId")
                    } else {
                        navController.navigate("archive_report/$reportId")
                    }
                },
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
                onBackClick = { navController.popBackStack() },
                onFeedbackDetailClick = { feedbackId, itemIndex ->
                    navController.navigate("feedback_detail/$feedbackId?item=$itemIndex")
                }
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
            // 12-A: 보관함·저장 시트로 여는 성장 리포트도 홈과 같은 ReportScreen을 쓴다(같은 화면·
            // 같은 모션·같은 마스터 만렙 규칙). route의 reportId를 ReportViewModel이 읽어 그 리포트를 연다.
            ReportScreen(
                onBack = { navController.popBackStack() },
                onSheetTopChange = onOverlaySheetTop,
                onArchiveClick = { navController.navigate("${Screen.ARCHIVE_LIST}/3") },
                // 시트 카드의 종류(성장/주간)에 따라 갈 상세가 다르다 — 주간 비교 화면과 같은 라우팅(B).
                onReportClick = { reportId, isWeeklyCompare ->
                    if (isWeeklyCompare) {
                        navController.navigate("archive_weekly_compare_report/$reportId")
                    } else {
                        navController.navigate("archive_report/$reportId")
                    }
                },
            )
        }

        composable(
            route = "archive_weekly_compare_report/{reportId}",
            arguments = listOf(navArgument("reportId") { type = NavType.StringType })
        ) {
            ArchiveWeeklyCompareReportScreen(
                onBackClick = { navController.popBackStack() },
                // 완료한 미션 > 은 보관함 대화 탭(1)으로 보낸다. 홈에서 들어가는 주간 비교 화면도
                // 같은 목적지라 두 화면이 일치한다(예전엔 여기만 미션 탭(0)이었다).
                onCompletedMissionsClick = {
                    navController.navigate("${Screen.ARCHIVE_LIST}/1")
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
            val selection by setupViewModel.selection.collectAsStateWithLifecycle()
            val guideline by setupViewModel.guideline.collectAsStateWithLifecycle()
            val isGuidelineReady by setupViewModel.isGuidelineReady.collectAsStateWithLifecycle()
            LaunchedEffect(missionId) { setupViewModel.loadGuideline(missionId) }
            ConversationSetup1Screen(
                onBack = { navController.popBackStack() },
                onNext = { navController.navigate("conversation_setup_2/$missionId") },
                onSelect = setupViewModel::selectEnvironment,
                initialSelection = selection.environment,
                disabledOptions = guideline?.disabled?.environment.orEmpty().toSet(),
                isLoading = !isGuidelineReady,
            )
        }

        composable(
            route = Screen.CONVERSATION_SETUP_2,
            arguments = listOf(navArgument("missionId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val missionId = backStackEntry.arguments?.getString("missionId").orEmpty()
            val setupViewModel = navController.conversationSetupViewModel(missionId)
            val selection by setupViewModel.selection.collectAsStateWithLifecycle()
            val guideline by setupViewModel.guideline.collectAsStateWithLifecycle()
            ConversationSetup2Screen(
                onBack = { navController.popBackStack() },
                onNext = { navController.navigate("conversation_setup_3/$missionId") },
                onSelect = setupViewModel::selectPartnerRole,
                initialSelection = selection.partnerRole,
                disabledOptions = guideline?.disabled?.partnerRole.orEmpty().toSet(),
            )
        }

        composable(
            route = Screen.CONVERSATION_SETUP_3,
            arguments = listOf(navArgument("missionId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val missionId = backStackEntry.arguments?.getString("missionId").orEmpty()
            val setupViewModel = navController.conversationSetupViewModel(missionId)
            val selection by setupViewModel.selection.collectAsStateWithLifecycle()
            val guideline by setupViewModel.guideline.collectAsStateWithLifecycle()
            ConversationSetup3Screen(
                onBack = { navController.popBackStack() },
                onNext = { navController.navigate("conversation_setup_4/$missionId") },
                onSelectGender = setupViewModel::selectGender,
                onSelectAgeGroup = setupViewModel::selectAgeGroup,
                initialGender = selection.partnerGender,
                initialAgeGroup = selection.partnerAgeGroup,
                disabledGenders = guideline?.disabled?.partnerGender.orEmpty().toSet(),
                disabledAgeGroups = guideline?.disabled?.partnerAgeGroup.orEmpty().toSet(),
            )
        }

        composable(
            route = Screen.CONVERSATION_SETUP_4,
            arguments = listOf(navArgument("missionId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val missionId = backStackEntry.arguments?.getString("missionId").orEmpty()
            val setupViewModel = navController.conversationSetupViewModel(missionId)
            val isSaving by setupViewModel.isSaving.collectAsStateWithLifecycle()
            val selection by setupViewModel.selection.collectAsStateWithLifecycle()
            val guideline by setupViewModel.guideline.collectAsStateWithLifecycle()
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
                initialIntimacy = selection.intimacyIndex,
                initialFormality = selection.formalityIndex,
                disabledIntimacy = guideline?.disabled?.intimacyLevel.orEmpty().map { it - 1 }.toSet(),
                disabledFormality = guideline?.disabled?.formalityLevel.orEmpty().map { it - 1 }.toSet(),
            )
        }

        composable(
            route = Screen.CONVERSATION,
            arguments = listOf(navArgument("conversationId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val missionId = backStackEntry.arguments?.getString("conversationId").orEmpty()
            ConversationScreen(
                onExitConfirm = { durationSec, missionTitle ->
                    // 미션 제목을 여기서부터 실어 보낸다 — 서버 피드백엔 제목이 없어 화면이 들고 있는 값을 넘겨야 한다.
                    navController.navigate(
                        "mission_complete/$missionId?durationSec=$durationSec" +
                                "&missionTitle=${Uri.encode(missionTitle)}",
                    ) {
                        popUpTo(Screen.HOME)
                    }
                },
                onBack = { navController.popBackStack(Screen.HOME, inclusive = false) },
            )
        }

        composable(
            route = "${Screen.MISSION_COMPLETE}?durationSec={durationSec}&missionTitle={missionTitle}",
            arguments = listOf(
                navArgument("missionId") { type = NavType.StringType },
                navArgument("durationSec") { type = NavType.LongType; defaultValue = 0L },
                navArgument("missionTitle") { type = NavType.StringType; defaultValue = "" },
            ),
        ) { backStackEntry ->
            val missionId = backStackEntry.arguments?.getString("missionId").orEmpty()
            // MissionCompleteScreen의 onContinue 시그니처는 그대로 두고, 이 화면 라우트 인자에서
            // 받은 제목을 다음 목적지로 이어 붙인다.
            val missionTitle = backStackEntry.arguments?.getString("missionTitle").orEmpty()
            MissionCompleteScreen(
                onContinue = { feedbackId ->
                    navController.navigate(
                        "feedback/${feedbackId ?: missionId}?missionTitle=${Uri.encode(missionTitle)}",
                    )
                },
            )
        }

        composable(
            route = "${Screen.FEEDBACK}?missionTitle={missionTitle}",
            arguments = listOf(
                navArgument("feedbackId") { type = NavType.StringType },
                navArgument("missionTitle") { type = NavType.StringType; defaultValue = "" },
            ),
        ) { backStackEntry ->
            val feedbackId = backStackEntry.arguments?.getString("feedbackId").orEmpty()
            val missionTitle = backStackEntry.arguments?.getString("missionTitle").orEmpty()
            FeedbackScreen(
                missionTitle = missionTitle,
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
                // 시트 카드의 종류(성장/주간)에 따라 갈 상세가 다르다 — 주간 비교 화면과 같은 라우팅(B).
                onReportClick = { reportId, isWeeklyCompare ->
                    if (isWeeklyCompare) {
                        navController.navigate("archive_weekly_compare_report/$reportId")
                    } else {
                        navController.navigate("archive_report/$reportId")
                    }
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
            val context = LocalContext.current
            val profileViewModel: ProfileViewModel = hiltViewModel()
            val profileUiState by profileViewModel.uiState.collectAsState()

            LaunchedEffect(Unit) {
                profileViewModel.loadArchiveSummary()
            }

            profileUiState.errorMessage?.let { message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                profileViewModel.clearError()
            }

            ProfileRecentMissionScreen(
                recentItems = profileUiState.archiveSummary?.recentItems.orEmpty(),
                onBack = { navController.popBackStack() },
                // 보관함 최근 활동과 같은 목적지로 보낸다. 리포트만 종류에 따라 갈린다.
                onActivityClick = { type, referenceId, reportType ->
                    when (type) {
                        "mission" -> navController.navigate("mission_detail/$referenceId")
                        "conversation" -> navController.navigate("archive_conversation_detail/$referenceId")
                        "phrase" -> navController.navigate("archive_saved_phrase/$referenceId")
                        "report" -> if (reportType == "weekly_compare") {
                            navController.navigate("archive_weekly_compare_report/$referenceId")
                        } else {
                            navController.navigate("archive_report/$referenceId")
                        }
                    }
                },
            )
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
                avatarUrl = dashboard?.avatarUrl ?: profile?.avatarUrl,
                connectedAccount = connectedAccount,
                initialPushEnabled = settings?.let { it.communityApproved || it.reportReady || it.marketing } ?: true,
                initialReminderEnabled = settings?.missionReminder ?: false,
                initialReminderTime = settings?.missionReminderTime ?: "09:00",
                onPushEnabledChange = profileViewModel::updatePushNotifications,
                onReminderEnabledChange = profileViewModel::updateMissionReminder,
                onReminderTimeChange = profileViewModel::updateMissionReminderTime,
                onBack = { navController.popBackStack() },
                onEditProfileClick = { navController.navigate(Screen.PROFILE_INFO) },
                onAvatarClick = { uri ->
                    val imagePart = uri.toProfileImagePart(context)
                    if (imagePart == null) {
                        Toast.makeText(context, "이미지를 불러오지 못했어요.", Toast.LENGTH_SHORT).show()
                    } else {
                        profileViewModel.uploadProfileImage(imagePart)
                    }
                },
                onConnectedAccountClick = { navController.navigate(Screen.PROFILE_CONNECTED_ACCOUNT) },
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
            var passwordChangedDismissStarted by remember { mutableStateOf(false) }
            val scope = rememberCoroutineScope()

            fun dismissPasswordChangedDialog() {
                if (passwordChangedDismissStarted) return
                passwordChangedDismissStarted = true
                showPasswordChangedDialog = false
                scope.launch {
                    delay(ModalDimDurationMillis.toLong())
                    navController.popBackStack(Screen.PROFILE_INFO, inclusive = false)
                }
            }

            profileUiState.errorMessage?.let { message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                profileViewModel.clearError()
            }

            ProfileNewPasswordScreen(
                onBack = { navController.popBackStack() },
                onConfirmClick = { newPassword ->
                    profileViewModel.changePassword(newPassword) {
                        passwordChangedDismissStarted = false
                        showPasswordChangedDialog = true
                    }
                },
                showCompletionDialog = showPasswordChangedDialog,
                onCompletionConfirm = ::dismissPasswordChangedDialog,
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
            val context = LocalContext.current
            val profileViewModel: ProfileViewModel = hiltViewModel()
            val profileUiState by profileViewModel.uiState.collectAsState()
            val profile = profileUiState.profile
            val dashboard = profileUiState.dashboard
            val nickname = dashboard?.nickname?.takeIf { it.isNotBlank() }
                ?: profile?.nickname?.takeIf { it.isNotBlank() }
                ?: profile?.name?.takeIf { it.isNotBlank() }
                ?: "사용자"
            LaunchedEffect(concernRefreshKey) {
                profileViewModel.loadProfile()
            }

            profileUiState.errorMessage?.let { message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                profileViewModel.clearError()
            }

            val personalityType = profile?.personalityType
            val difficultSituations = profile?.difficultSituations.orEmpty()
            val purposes = profile?.purpose.orEmpty()

            fun startConcernEdit(target: String) {
                concernPersonalityType = personalityType ?: "introvert"
                concernDifficultSituations = difficultSituations
                concernPurposes = purposes
                isConcernEditMode = true
                navController.navigate(target)
            }

            ProfileConcernScreen(
                nickname = nickname,
                personalityType = personalityType,
                difficultSituations = difficultSituations,
                purpose = purposes,
                onBack = { navController.popBackStack() },
                onPersonalityClick = { startConcernEdit(Screen.ONBOARDING_PERSONALITY) },
                onDifficultyClick = { startConcernEdit(Screen.ONBOARDING_DIFFICULTY) },
                onGoalClick = { startConcernEdit(Screen.ONBOARDING_GOAL) },
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
