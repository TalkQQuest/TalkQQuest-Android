package com.talkqquest.app.navigation

import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import com.talkqquest.app.feature.home.ui.HomeScreen
import com.talkqquest.app.feature.onboarding.ui.OnboardingDifficultyScreen
import com.talkqquest.app.feature.onboarding.ui.OnboardingGoalScreen
import com.talkqquest.app.feature.onboarding.ui.OnboardingPersonalityScreen
import com.talkqquest.app.feature.onboarding.ui.OnboardingWelcomeScreen
import com.talkqquest.app.feature.onboarding.ui.OnboardingCompleteScreen
import com.talkqquest.app.feature.notification.ui.NotificationScreen
import com.talkqquest.app.feature.profile.ui.ProfileBadgesScreen
import com.talkqquest.app.feature.profile.ui.ProfileRecentMissionScreen
import com.talkqquest.app.feature.profile.ui.ProfileSettingsScreen
import com.talkqquest.app.feature.profile.ui.ProfileSupportScreen
import com.talkqquest.app.feature.profile.ui.ProfileWithdrawScreen
import com.talkqquest.app.feature.profile.ui.PrivacyPolicySections
import com.talkqquest.app.feature.profile.ui.ProfileTermsDetailScreen
import com.talkqquest.app.feature.profile.ui.ProfileTermsScreen
import com.talkqquest.app.feature.profile.ui.ServiceTermsSections
import com.talkqquest.app.feature.profile.ui.ProfileScreen
import com.talkqquest.app.feature.mission.ui.ConversationPrepScreen
import com.talkqquest.app.feature.mission.ui.ConversationScreen
import com.talkqquest.app.feature.mission.ui.FeedbackDetailScreen
import com.talkqquest.app.feature.mission.ui.FeedbackScreen
import com.talkqquest.app.feature.mission.ui.MissionCompleteScreen
import com.talkqquest.app.feature.mission.ui.MissionDetailScreen
import com.talkqquest.app.feature.mission.ui.MissionListScreen
import com.talkqquest.app.feature.report.ui.ReportScreen
import com.talkqquest.app.feature.archive.ui.ArchiveHomeScreen
import com.talkqquest.app.feature.archive.ui.ArchiveListScreen
import com.talkqquest.app.feature.archive.ui.ArchiveSearchScreen
import com.talkqquest.app.feature.archive.ui.ArchiveConversationDetailScreen
import com.talkqquest.app.feature.archive.ui.ArchiveSavedPhraseScreen
import com.talkqquest.app.feature.archive.ui.ArchiveReportScreen
import com.talkqquest.app.feature.archive.viewmodel.ActivityType
import com.talkqquest.app.navigation.Screen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// 네비게이션 그래프.
// TODO(각 담당): Screen.kt에 route를 정의한 뒤 NavGraph.kt에 composable을 등록합니다.
@Composable
fun NavGraph(
    navController: NavHostController,
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
        composable(Screen.SPLASH) {
            LaunchedEffect(Unit) {
                delay(1500)
                navController.navigate(Screen.LOGIN) {
                    popUpTo(Screen.SPLASH) { inclusive = true }
                    launchSingleTop = true
                }
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
            val nickname = navController.currentBackStackEntry
                ?.savedStateHandle
                ?.get<String>("onboarding_nickname")
                .orEmpty()

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
                        navController.navigate(Screen.ONBOARDING_DIFFICULTY)
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
        // 하단 네비게이션 4개 진입 화면입니다.
        // HOME 이후 미션/아카이브/리포트 상세 route는 각 담당 화면 구현에 맞춰 연결합니다.
        composable(Screen.HOME) {
            val homeScope = rememberCoroutineScope()
            HomeScreen(
                onStartMissionClick = { missionId -> navController.navigate("mission_detail/$missionId") },
                onOtherMissionsClick = { navController.navigate(Screen.MISSION_LIST) },
                // 알림 아이콘 ripple이 먼저 보인 후 화면이 전환되도록 짧게 지연합니다.
                onNotificationClick = {
                    homeScope.launch {
                        delay(140)
                        navController.navigate(Screen.NOTIFICATION)
                    }
                },
            )
        }
        // 알림창(종 모양 진입). 디자인 미완성이므로 빈 상태 placeholder.
        composable(Screen.NOTIFICATION) {
            NotificationScreen(onBack = { navController.popBackStack() })
        }
        // C담당: 아카이브 홈 화면
        composable(Screen.ARCHIVE_HOME) {
            val context = LocalContext.current

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

        // B담당: 미션 리스트 (다른 사람의 미션 둘러보기). 클릭 시 해당 미션의 상세({missionId})로 이동합니다.
        composable(Screen.MISSION_LIST) {
            MissionListScreen(
                onBack = { navController.popBackStack() },
                onMissionClick = { missionId -> navController.navigate("mission_detail/$missionId") },
                onSheetTopChange = onOverlaySheetTop, // 바텀시트가 올라올 때 오버레이 처리를 위한 콜백
                onSavedListClick = { navController.navigate("${Screen.ARCHIVE_LIST}/0") },
            )
        }
        // B담당: 미션 상세 화면. "시작" 버튼 클릭 시 대화 준비 화면으로, "저장" 클릭 시 보관함으로 이동.
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
        // B담당: 대화 준비 화면. "대화 시작하기" 버튼 클릭 시 대화(진행) 화면으로 이동.
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
                // stub의 missionId를 feedbackId로 임시 사용. 실제 연동 시 응답받은 feedbackId로 이동
                onContinue = { navController.navigate("feedback/$missionId") },
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
        composable(Screen.PROFILE) {
            ProfileScreen(
                onSettingsClick = { navController.navigate(Screen.PROFILE_SETTINGS) },
                onBadgesClick = { navController.navigate(Screen.PROFILE_BADGES) },
                onRecentMissionClick = { navController.navigate(Screen.PROFILE_RECENT_MISSION) },
                onArchiveClick = { navController.navigate(Screen.ARCHIVE_HOME) },
            )
        }
        composable(Screen.PROFILE_BADGES) {
            ProfileBadgesScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.PROFILE_RECENT_MISSION) {
            ProfileRecentMissionScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.PROFILE_SETTINGS) {
            ProfileSettingsScreen(
                onBack = { navController.popBackStack() },
                onTermsClick = { navController.navigate(Screen.PROFILE_TERMS) },
                onSupportClick = { navController.navigate(Screen.PROFILE_SUPPORT) },
                onWithdrawClick = { navController.navigate(Screen.PROFILE_WITHDRAW) },
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
            ProfileTermsDetailScreen(
                title = "이용약관",
                sections = ServiceTermsSections,
                onBack = { navController.popBackStack() },
            )
        }
        composable(Screen.PROFILE_PRIVACY_POLICY) {
            ProfileTermsDetailScreen(
                title = "개인정보 처리 방침",
                sections = PrivacyPolicySections,
                onBack = { navController.popBackStack() },
            )
        }
        composable(Screen.PROFILE_SUPPORT) {
            ProfileSupportScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.PROFILE_WITHDRAW) {
            ProfileWithdrawScreen(onBack = { navController.popBackStack() })
        }
    }
}
