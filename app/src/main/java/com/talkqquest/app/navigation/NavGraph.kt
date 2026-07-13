package com.talkqquest.app.navigation

import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
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
import com.talkqquest.app.feature.auth.ui.SignupEmailScreen
import com.talkqquest.app.feature.auth.ui.SignupPasswordScreen
import com.talkqquest.app.feature.auth.ui.SignupNicknameScreen
import com.talkqquest.app.feature.auth.ui.SignupStartScreen
import com.talkqquest.app.feature.auth.ui.SignupVerifyScreen
import com.talkqquest.app.feature.auth.viewmodel.AuthViewModel
import com.talkqquest.app.feature.home.ui.HomeScreen
import com.talkqquest.app.feature.mission.ui.ConversationPrepScreen
import com.talkqquest.app.feature.mission.ui.ConversationScreen
import com.talkqquest.app.feature.mission.ui.FeedbackDetailScreen
import com.talkqquest.app.feature.mission.ui.FeedbackScreen
import com.talkqquest.app.feature.mission.ui.MissionCompleteScreen
import com.talkqquest.app.feature.mission.ui.MissionDetailScreen
import com.talkqquest.app.feature.mission.ui.MissionListScreen
import com.talkqquest.app.feature.mission.ui.SavedMissionsScreen
import com.talkqquest.app.feature.report.ui.ReportScreen
import com.talkqquest.app.feature.archive.ui.ArchiveHomeScreen
import com.talkqquest.app.navigation.Screen
import kotlinx.coroutines.launch

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
                onEmailClick = { navController.navigate(Screen.SIGNUP_EMAIL) },
            )
        }
        composable(Screen.SIGNUP_EMAIL) {
            SignupEmailScreen(
                onBack = { navController.popBackStack() },
                onSendClick = { navController.navigate(Screen.SIGNUP_VERIFY) },
            )
        }
        composable(Screen.SIGNUP_VERIFY) {
            SignupVerifyScreen(
                onBack = { navController.popBackStack() },
                onVerified = { navController.navigate(Screen.SIGNUP_PASSWORD) },
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
                onCompleteClick = { navController.navigate(Screen.ONBOARDING_WELCOME) },
            )
        }
        composable(Screen.ONBOARDING_WELCOME) { PlaceholderScreen("온보딩") }
        // 하단 네비 4탭 (임시 — 실제 화면으로 교체)
        // 홈은 화면↔데이터 연결 예시로 실제 구현됨(feature/home 참고). 나머지는 각 담당이 교체.
        composable(Screen.HOME) {
            HomeScreen(
                onStartMissionClick = { missionId -> navController.navigate("mission_detail/$missionId") },
                onOtherMissionsClick = { navController.navigate(Screen.MISSION_LIST) },
            )
        }
        composable(Screen.ARCHIVE_HOME) {
            val context = LocalContext.current

            ArchiveHomeScreen(
                onNavigateToSearch = {
                    Toast.makeText(context, "아카이브 검색: 준비 중인 기능입니다.", Toast.LENGTH_SHORT).show()
                    // navController.navigate(Screen.ARCHIVE_SEARCH)
                },
                onNavigateToList = { tabIndex ->
                    Toast.makeText(context, "아카이브 목록: 준비 중인 기능입니다.", Toast.LENGTH_SHORT).show()
                    // navController.navigate(Screen.ARCHIVE_LIST)
                },
                onNavigateToDetail = { activityId ->
                    Toast.makeText(context, "최근 활동 상세: 준비 중인 기능입니다.", Toast.LENGTH_SHORT).show()
                    // navController.navigate("상세 경로 추가 필요")
                }
            )
        }
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
        // B담당: 미션 완료&XP. 탭 → AI 피드백 (NAVIGATION.md: 미션 완료 → 다음 → 피드백 요약).
        composable(
            route = "${Screen.MISSION_COMPLETE}?durationSec={durationSec}",
            arguments = listOf(
                navArgument("missionId") { type = NavType.LongType },
                navArgument("durationSec") { type = NavType.LongType; defaultValue = 0L },
            ),
        ) { backStackEntry ->
            val missionId = backStackEntry.arguments?.getLong("missionId") ?: 0L
            MissionCompleteScreen(
                // stub은 missionId를 feedbackId로 그대로 씀 — 서버 연동 시 완료 응답의 feedbackId로 교체.
                onContinue = { navController.navigate("feedback/$missionId") },
            )
        }
        // B담당: AI 피드백 요약. 항목 행 탭 → 그 항목 배너로 피드백 상세 (NAVIGATION.md).
        // "상세 리포트" → 리포트 화면. "홈으로" → 홈 복귀.
        composable(
            route = Screen.FEEDBACK,
            arguments = listOf(navArgument("feedbackId") { type = NavType.LongType }),
        ) { backStackEntry ->
            val feedbackId = backStackEntry.arguments?.getLong("feedbackId") ?: 0L
            FeedbackScreen(
                onBack = { navController.popBackStack() },
                onItemClick = { index -> navController.navigate("feedback_detail/$feedbackId?item=$index") },
                // 리포트가 어느 미션 것인지 함께 넘김 — 저장 시트 카드 제목이 그 미션명이 됨.
                // 한글이라 route에 실으려면 인코딩 필요.
                onDetailReport = { missionTitle ->
                    navController.navigate("report?missionTitle=${Uri.encode(missionTitle)}")
                },
                onHome = { navController.popBackStack(Screen.HOME, inclusive = false) },
            )
        }
        // B담당: 리포트 (성장/주간 비교 탭 통합). 피드백 요약 "상세 리포트"에서 진입.
        composable(
            route = Screen.REPORT,
            arguments = listOf(
                navArgument("missionTitle") { type = NavType.StringType; defaultValue = "" },
            ),
        ) {
            ReportScreen(
                onBack = { navController.popBackStack() },
                onSheetTopChange = onOverlaySheetTop, // 리포트 저장 시트가 하단 네비를 덮는 동안 네비 가림
                // ↓ C담당(아카이브)이 채울 자리 — 아카이브 화면이 생기면 navigate만 넣으면 됨.
                //   "보관함 >" → 보관함 리포트 탭 (예: navController.navigate(Screen.ARCHIVE_LIST))
                onArchiveClick = { /* TODO(C): 아카이브 보관함(리포트 탭)으로 */ },
                //   저장된 리포트 카드 클릭 → 보관함 리포트 상세
                onReportClick = { reportId -> /* TODO(C): 보관함 리포트 상세로 (reportId) */ },
            )
        }
        // B담당: AI 피드백 상세. "다른 미션 보러가기" → 정산 흐름(완료·피드백)을 정리하고 미션 목록으로.
        composable(
            route = "${Screen.FEEDBACK_DETAIL}?item={item}",
            arguments = listOf(
                navArgument("feedbackId") { type = NavType.LongType },
                navArgument("item") { type = NavType.IntType; defaultValue = 0 },
            ),
        ) {
            FeedbackDetailScreen(
                onBack = { navController.popBackStack() },
                onOtherMissions = {
                    navController.navigate(Screen.MISSION_LIST) { popUpTo(Screen.HOME) }
                },
                // ↓ C담당(아카이브)이 채울 자리 — 문장 저장 시트에서 아카이브로 나가는 두 경로.
                //   "보관함 >" → 보관함 문장 탭 (예: navController.navigate(Screen.ARCHIVE_LIST))
                onArchiveClick = { /* TODO(C): 아카이브 보관함(문장 탭)으로 */ },
                //   저장된 문장 카드 클릭 → 보관함 문장 상세 (Screen.ARCHIVE_SAVED_PHRASE)
                onPhraseClick = { phraseId -> /* TODO(C): 보관함 문장 상세로 (phraseId) */ },
            )
        }
        composable(Screen.COMMUNITY_LIST) { PlaceholderScreen("모임") }
        composable(Screen.PROFILE) { PlaceholderScreen("프로필") }

        // 예) composable(Screen.LOGIN) { LoginScreen(navController) }
    }
}
