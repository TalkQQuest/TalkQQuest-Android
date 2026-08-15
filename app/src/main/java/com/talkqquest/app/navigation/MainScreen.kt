package com.talkqquest.app.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import androidx.compose.ui.unit.dp
import com.talkqquest.app.core.designsystem.A2ZFamily
import com.talkqquest.app.core.designsystem.PretendardFamily
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.talkqquest.app.core.designsystem.Gray50
import com.talkqquest.app.feature.home.ui.WeeklyReportModal
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

data class WeeklyReportLaunchRequest(
    val reportId: String?,
    val requestId: Long,
)

@Composable
fun MainScreen(
    weeklyReportLaunchRequest: WeeklyReportLaunchRequest? = null,
    onWeeklyReportLaunchConsumed: () -> Unit = {},
) {
    val navController = rememberNavController()

    // 콜드스타트 폰트 워밍 — 스플래시가 떠 있는 동안 Pretendard·A2Z를 미리 로드해 둔다.
    // 반드시 백그라운드(Default)에서: 메인 스레드에서 로드하면 4웨이트(약 10MB)가 한꺼번에 파싱되며
    // 오히려 큰 프레임 드랍이 생긴다. 백그라운드로 옮겨 홈 첫 텍스트 전에 캐시만 데워둔다(글리프는 그대로).
    val fontResolver = LocalFontFamilyResolver.current
    LaunchedEffect(Unit) {
        withContext(Dispatchers.Default) {
            runCatching { fontResolver.preload(PretendardFamily) }
            runCatching { fontResolver.preload(A2ZFamily) }
        }
    }

    var showWeeklyReportModal by remember { mutableStateOf(false) }
    // 도착 모달이 열어야 할 리포트 id(서버 newWeeklyCompareReport.reportId). 비면 가장 최근 주차.
    var weeklyReportModalId by remember { mutableStateOf<String?>(null) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // 하단 탭 4개(홈·미션·보관함·프로필)를 한 HorizontalPager에서 스와이프 전환. 상태는 여기서 호이스팅해
    // 페이저 셸(MainTabsPager)과 전역 하단바(TqBottomBar)가 공유한다.
    val pagerState = rememberPagerState(pageCount = { BottomNavItem.entries.size })
    val homePage = BottomNavItem.entries.indexOf(BottomNavItem.Home)

    // 시스템 알림을 눌렀다면 현재 상세 화면이나 하단 탭 위치와 관계없이 홈으로 돌아와
    // 주간 비교 리포트 모달을 연다. 콜드 스타트에서는 인증 확인이 끝나 HOME이 생길 때까지 기다린다.
    LaunchedEffect(weeklyReportLaunchRequest?.requestId, currentRoute) {
        val request = weeklyReportLaunchRequest ?: return@LaunchedEffect
        val homeExists = runCatching { navController.getBackStackEntry(Screen.HOME) }.isSuccess
        if (currentRoute != Screen.HOME && !homeExists) return@LaunchedEffect

        if (currentRoute != Screen.HOME) {
            navController.popBackStack(Screen.HOME, inclusive = false)
        }
        pagerState.scrollToPage(homePage)
        weeklyReportModalId = request.reportId
        showWeeklyReportModal = true
        onWeeklyReportLaunchConsumed()
    }

    // 다른 화면에서 특정 탭 route로 새로 진입할 때(예: 프로필→보관함) 그 탭 페이지로 점프시킨다.
    // back stack entry id 기준이라, 상세 화면에서 뒤로 돌아오는 pop은 이미 처리된 entry라 다시 점프하지 않는다
    // (사용자가 스와이프로 옮겨둔 탭을 유지).
    val syncedEntryIds = remember { mutableStateOf(setOf<String>()) }
    LaunchedEffect(navBackStackEntry) {
        val entry = navBackStackEntry ?: return@LaunchedEffect
        val page = BottomNavItem.entries.indexOfFirst { it.route == entry.destination.route }
        if (page >= 0 && entry.id !in syncedEntryIds.value) {
            syncedEntryIds.value = syncedEntryIds.value + entry.id
            pagerState.scrollToPage(page)
        }
    }

    val bottomBarRoutes = BottomNavItem.entries.map { it.route } +
        Screen.MISSION_LIST + Screen.MISSION_LIST_HOME + Screen.MISSION_DETAIL +
        Screen.PROFILE_BADGES + Screen.PROFILE_RECENT_MISSION
    // 시스템 네비(804~852)만 있고 입력창이 그 바로 위 24까지 내려온다. 나가는 길은 헤더의
    // REPORT 제외: 최신 시안에서 리포트(성장/주간)는 하단 네비 없는 단독 화면(뒤로가기로 이탈)

    val hazeState = remember { HazeState() }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(Gray50)) {
        val density = LocalDensity.current
        val navTopPx = with(density) { maxHeight.toPx() - 88.dp.toPx() } -
            WindowInsets.navigationBars.getBottom(density)

        var overlaySheetTop by remember { mutableStateOf<Float?>(null) }

        var splashSettled by remember { mutableStateOf(false) }
        LaunchedEffect(currentRoute) {
            if (!splashSettled && currentRoute != null && currentRoute != Screen.SPLASH) {
                delay(300) // 스플래시→홈 크로스페이드(~300ms)가 끝날 때까지 대기
                splashSettled = true
            }
        }

        val showBottomBar = splashSettled &&
            (currentRoute == null || currentRoute in bottomBarRoutes) &&
            (overlaySheetTop?.let { it > navTopPx } ?: true)

        NavGraph(
            navController = navController,
            pagerState = pagerState,
            // 탭 간 가로 스와이프는 MainTabsPager 내부 HorizontalPager가 손가락을 따라 처리한다.
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = hazeState),
            onOverlaySheetTop = { overlaySheetTop = it },
            onShowWeeklyReportModal = { reportId ->
                weeklyReportModalId = reportId
                showWeeklyReportModal = true
            },
        )
        if (showBottomBar) {
            TqBottomBar(
                navController = navController,
                pagerState = pagerState,
                hazeState = hazeState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .drawWithContent {
                        val sheetTop = overlaySheetTop
                        if (sheetTop == null) {
                            drawContent()
                        } else {
                            clipRect(bottom = (sheetTop - navTopPx).coerceAtMost(size.height)) {
                                this@drawWithContent.drawContent()
                            }
                        }
                    },
            )
        }
        // 별도 Dialog 창이 아니라 앱 최상위 레이어에 배치해, 카드 하단까지 퇴장 모션이 잘리지 않는다.
        WeeklyReportModal(
            visible = showWeeklyReportModal,
            onConfirm = {
                showWeeklyReportModal = false
                navController.navigate("weekly_compare?reportId=${weeklyReportModalId.orEmpty()}")
            },
            onDismiss = { showWeeklyReportModal = false },
        )
    }
}
