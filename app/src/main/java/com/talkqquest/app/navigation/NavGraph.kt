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
import com.talkqquest.app.feature.mission.ui.ConversationPrepScreen
import com.talkqquest.app.feature.mission.ui.ConversationScreen
import com.talkqquest.app.feature.mission.ui.FeedbackDetailScreen
import com.talkqquest.app.feature.mission.ui.FeedbackScreen
import com.talkqquest.app.feature.mission.ui.MissionCompleteScreen
import com.talkqquest.app.feature.mission.ui.MissionDetailScreen
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
        startDestination = Screen.LOGIN,
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
                val destination = if (isNewUser) {
                    Screen.SIGNUP_TERMS_SOCIAL
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
                                    navigateAfterSocialLogin(data.isNewUser)
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
                                    navigateAfterSocialLogin(data.isNewUser)
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
                        Toast.makeText(context, "\uC778\uC99D \uCF54\uB4DC\uAC00 \uC7AC\uBC1C\uC1A1\uB418\uC5C8\uC2B5\uB2C8\uB2E4.", Toast.LENGTH_SHORT).show()
                    }
                },
            )
        }
        composable(Screen.SIGNUP_PASSWORD) {
            val email = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<String>("signup_email")
                .orEmpty()

            SignupPasswordScreen(
                onBack = { navController.popBackStack() },
                onNextClick = { password ->
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set("signup_email", email)
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set("signup_password", password)
                    navController.navigate(Screen.SIGNUP_NICKNAME)
                },
            )
        }
        composable(Screen.SIGNUP_NICKNAME) {
            val context = LocalContext.current
            val authViewModel: AuthViewModel = hiltViewModel()
            val authUiState by authViewModel.uiState.collectAsState()
            val email = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<String>("signup_email")
                .orEmpty()
            val password = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<String>("signup_password")
                .orEmpty()

            authUiState.errorMessage?.let { message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                authViewModel.clearError()
            }

            SignupNicknameScreen(
                onBack = { navController.popBackStack() },
                onCompleteClick = { nickname ->
                    authViewModel.signupWithEmail(
                        email = email,
                        password = password,
                        nickname = nickname,
                    ) {
                        navController.currentBackStackEntry
                            ?.savedStateHandle
                            ?.set("onboarding_nickname", nickname.trim())
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
                        navController.currentBackStackEntry
                            ?.savedStateHandle
                            ?.set("onboarding_nickname", nickname.trim())
                        navController.navigate(Screen.ONBOARDING_WELCOME)
                    }
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
                "\uC18C\uB2E4123"
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
                        Toast.makeText(context, "\uC5B4\uB824\uC6B4 \uC810\uC744 \uC120\uD0DD\uD574\uC8FC\uC138\uC694.", Toast.LENGTH_SHORT).show()
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
                        Toast.makeText(context, "\uC5F0\uC2B5 \uBAA9\uD45C\uB97C \uC120\uD0DD\uD574\uC8FC\uC138\uC694.", Toast.LENGTH_SHORT).show()
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
        // 하단 네비게이션 4개 탭(미션, 홈, 보관함, 프로필)은 하나의 HorizontalPager(MainTabsPager)에서
        // 상태를 공유하며 전환합니다. 4개 route 모두 같은 페이저를 렌더링하고,
        // 실제 표시 페이지는 MainScreen의 pagerState로 제어합니다.
        composable(Screen.HOME) {
            MainTabsPager(navController, pagerState, onOverlaySheetTop)
        }
        // 알림 화면 진입. 디자인 미완성으로 현재는 빈 상태 placeholder입니다.
        composable(Screen.NOTIFICATION) {
            NotificationScreen(onBack = { navController.popBackStack() })
        }
        // C담당: 아카이브 홈 화면(하단 탭 = MainTabsPager의 페이저 페이지).
        composable(Screen.ARCHIVE_HOME) {
            MainTabsPager(navController, pagerState, onOverlaySheetTop)
        }
        // C담당: 아카이브 검색 화면.
        composable(Screen.ARCHIVE_SEARCH) {
            ArchiveSearchScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                // C담당: 전달 파라미터 저장.
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

        // C담당: 아카이브 목록 화면(미션/저장 문장/리포트).
        composable(
            route = "${Screen.ARCHIVE_LIST}/{tabIndex}",
            arguments = listOf(navArgument("tabIndex") { type = NavType.IntType; defaultValue = 0 })
        ) { backStackEntry ->
            val tabIndex = backStackEntry.arguments?.getInt("tabIndex") ?: 0
            ArchiveListScreen(
                initialTabIndex = tabIndex,
                onBackClick = { navController.popBackStack() },
                // C담당: 보관함 리스트에서 미션 카드 클릭 시 미션 상세 화면으로 이동합니다.
                onMissionClick = { missionId: String ->
                    navController.navigate("mission_detail/$missionId")
                },
                // C담당: 전달 파라미터 저장.
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

        // C담당: 보관함 대화 기록 상세 화면.
        composable(
            route = "archive_conversation_detail/{conversationId}",
            arguments = listOf(navArgument("conversationId") { type = NavType.StringType })
        ) {
            ArchiveConversationDetailScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // C담당: 보관함 베스트 문장 상세 화면.
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

        // C담당: 보관함 리포트 상세 화면.
        composable(
            route = "archive_report/{reportId}",
            arguments = listOf(navArgument("reportId") { type = NavType.StringType })
        ) {
            ArchiveReportScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // B담당: 미션 리스트 화면(하단 탭 = MainTabsPager의 페이저 페이지). 미션 카드 클릭 시 상세로 이동합니다.
        composable(Screen.MISSION_LIST) {
            MainTabsPager(navController, pagerState, onOverlaySheetTop)
        }
        // B담당: 미션 상세 화면. 시작 버튼은 대화 준비 화면으로, 저장 버튼은 보관함으로 이동합니다.
        composable(
            route = Screen.MISSION_DETAIL,
            arguments = listOf(navArgument("missionId") { type = NavType.StringType }),
        ) {
            MissionDetailScreen(
                onBack = { navController.popBackStack() },
                onNextClick = { missionId -> navController.navigate("conversation_prep/$missionId") },
                onMissionClick = { missionId -> navController.navigate("mission_detail/$missionId") },
                onSheetTopChange = onOverlaySheetTop,
                onSavedListClick = { navController.navigate("${Screen.ARCHIVE_LIST}/0") },
            )
        }
        // B담당: 대화 준비 화면. 대화 시작 버튼 클릭 시 대화 진행 화면으로 이동합니다.
        composable(
            route = Screen.CONVERSATION_PREP,
            arguments = listOf(navArgument("missionId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val missionId = backStackEntry.arguments?.getString("missionId").orEmpty()
            ConversationPrepScreen(
                onBack = { navController.popBackStack() },
                onStartClick = { navController.navigate("conversation/$missionId") },
            )
        }
        // B담당: 대화 진행 화면. 종료 시 미션 완료 및 XP 화면으로 이동합니다.
        composable(
            route = Screen.CONVERSATION,
            arguments = listOf(navArgument("conversationId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val missionId = backStackEntry.arguments?.getString("conversationId").orEmpty()
            ConversationScreen(
                onExitConfirm = { durationSec ->
                    // 대화 종료 후 소요 시간을 파라미터로 넘겨 미션 완료 화면에 전달합니다.
                    navController.navigate("mission_complete/$missionId?durationSec=$durationSec") {
                        popUpTo(Screen.HOME)
                    }
                },
            )
        }
        // B담당: 미션 완료 및 XP 화면. 이후 AI 피드백 화면으로 진입합니다.
        composable(
            route = "${Screen.MISSION_COMPLETE}?durationSec={durationSec}",
            arguments = listOf(
                navArgument("missionId") { type = NavType.StringType },
                navArgument("durationSec") { type = NavType.LongType; defaultValue = 0L },
            ),
        ) { backStackEntry ->
            val missionId = backStackEntry.arguments?.getString("missionId").orEmpty()
            MissionCompleteScreen(
                // 완료 후 생성된 feedbackId로 이동합니다. 미생성 상태면 missionId를 fallback으로 사용합니다.
                onContinue = { feedbackId -> navController.navigate("feedback/${feedbackId ?: missionId}") },
            )
        }
        // B담당: AI 피드백 화면. 항목 클릭 시 피드백 상세 화면으로 이동합니다.
        // 상세 리포트 버튼은 리포트 화면으로, 다음 버튼은 홈으로 이동합니다.
        composable(
            route = Screen.FEEDBACK,
            arguments = listOf(navArgument("feedbackId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val feedbackId = backStackEntry.arguments?.getString("feedbackId").orEmpty()
            FeedbackScreen(
                onBack = { navController.popBackStack() },
                onItemClick = { index -> navController.navigate("feedback_detail/$feedbackId?item=$index") },
                // 피드백 진입 전 기존 ViewModel 선택 상태를 위한 백업 처리입니다.
                // URI 인코딩을 통해 파라미터를 전달합니다.
                onDetailReport = { missionTitle ->
                    navController.navigate("report?missionTitle=${Uri.encode(missionTitle)}")
                },
                onHome = { navController.popBackStack(Screen.HOME, inclusive = false) },
            )
        }
        // B담당: 리포트 성장/주간 화면. 피드백 화면에서 상세 리포트 클릭 시 진입합니다.
        composable(
            route = Screen.REPORT,
            arguments = listOf(
                navArgument("missionTitle") { type = NavType.StringType; defaultValue = "" },
            ),
        ) {
            ReportScreen(
                onBack = { navController.popBackStack() },
                onSheetTopChange = onOverlaySheetTop, // 리포트 바텀시트가 하단 탭을 덮는 동안 영역을 가립니다.
                onArchiveClick = { navController.navigate("${Screen.ARCHIVE_LIST}/3") },
                // 보관함 리포트 상세로 이동합니다.
                onReportClick = { reportId ->
                    navController.navigate("archive_report/$reportId")
                },
            )
        }
        // B담당: AI 피드백 상세 화면. 다른 미션 보러가기는 미션 목록으로, 보관함 버튼은 보관함으로 이동합니다.
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
        // A담당: 프로필(하단 탭 = MainTabsPager의 페이저 페이지).
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
