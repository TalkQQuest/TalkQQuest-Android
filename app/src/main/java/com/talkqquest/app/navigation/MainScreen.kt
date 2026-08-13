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
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.talkqquest.app.core.designsystem.Gray50
import com.talkqquest.app.feature.home.ui.WeeklyReportModal
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

// ???? ?섎떒 ?ㅻ퉬(???덈뒗 ?좊━ ?뚯빟)媛 ?붾㈃ ?꾩뿉 寃뱀퀜 ?щ떎.
// hazeState: ??肄섑뀗痢?NavGraph)瑜??좊━(?섎떒 ?ㅻ퉬)媛 ?먮━寃?鍮꾩텛?꾨줉 ?곌껐?섎뒗 ?곹깭.
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    var showWeeklyReportModal by remember { mutableStateOf(false) }
    // 도착 모달이 열어야 할 리포트 id(서버 newWeeklyCompareReport.reportId). 비면 가장 최근 주차.
    var weeklyReportModalId by remember { mutableStateOf<String?>(null) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // 하단 탭 4개(홈·미션·보관함·프로필)를 한 HorizontalPager에서 스와이프 전환. 상태는 여기서 호이스팅해
    // 페이저 셸(MainTabsPager)과 전역 하단바(TqBottomBar)가 공유한다.
    val pagerState = rememberPagerState(pageCount = { BottomNavItem.entries.size })

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

    // ?섎떒諛??쒖떆 route: ??4媛?+ ?붿옄?몄긽 ?섎떒諛붽? ?덈뒗 ?붾㈃(誘몄뀡 紐⑸줉). 洹????먮룞 ?④?.
    // currentRoute == null = 泥??꾨젅???쒖옉 ?붾㈃ ?명똿 ?? ???④꼈????쾶 ?⑥? ?딄쾶 諛붾줈 ?쒖떆.
    // ??CONVERSATION_PREP(誘몄뀡 吏꾩엯)? ?쒖쇅 ??UI 7李?"誘몄뀡 吏꾩엯" ?꾨젅?꾩뿉 ?섎떒 ?ㅻ퉬寃뚯씠?섏씠 ?놁쓬(2026-07-19 CSS ?뺤씤).
    val bottomBarRoutes = BottomNavItem.entries.map { it.route } +
        Screen.MISSION_LIST + Screen.MISSION_LIST_HOME + Screen.MISSION_DETAIL +
        Screen.PROFILE_BADGES + Screen.PROFILE_RECENT_MISSION
    // CONVERSATION 제외(UI 13차): "대화 시작(추천 답변 열림)" 프레임에 하단 앱 네비가 없다.
    // 시스템 네비(804~852)만 있고 입력창이 그 바로 위 24까지 내려온다. 나가는 길은 헤더의
    // 뒤로가기(저장 안 함) / "대화 완료"(완료·저장) 두 버튼이 대신한다.
    // REPORT 제외: 최신 시안에서 리포트(성장/주간)는 하단 네비 없는 단독 화면(뒤로가기로 이탈)

    val hazeState = remember { HazeState() }

    // ???섏씠吏 諛곌꼍(Gray50 = ?붿옄?몄떆?ㅽ뀥 '?섏씠吏 諛곌꼍'). ???붾㈃?ㅼ씠 媛숈? ?ㅼ쓣 怨듭쑀?섎룄濡?猷⑦듃?먯꽌 ??踰?源?
    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(Gray50)) {
        val density = LocalDensity.current
        // ?섎떒 ?ㅻ퉬 臾띠쓬(?뚯빟 64 + ?꾩븘???щ갚 12x2 + ?쒖뒪???ㅻ퉬 ?몄뀑)??????y(px)
        val navTopPx = with(density) { maxHeight.toPx() - 88.dp.toPx() } -
            WindowInsets.navigationBars.getBottom(density)

        // ?붾㈃ ?ㅻ쾭?덉씠(???諛뷀??쒗듃)??????y(px). null = ?쒗듃 ?놁쓬.
        // ?ㅻ퉬???쒗듃蹂대떎 ???덉씠?대씪 洹몃깷 ?먮㈃ ?쒗듃 ?꾩뿉 寃뱀퀜 蹂댁엫 ???쒗듃 ????"??蹂대떎
        // ?꾨옒履쎌? 洹몃━吏 ?딆븘, ?쒗듃媛 ?대젮媛??留뚰겮 ?ㅻ퉬媛 ?ㅼ뿉 ?덈뜕 寃껋쿂???꾩뿉?쒕????쒕윭?쒕떎.
        var overlaySheetTop by remember { mutableStateOf<Float?>(null) }

        // ?쒗듃媛 ?ㅻ퉬 ?곸뿭???꾩쟾????뒗 ?숈븞???ㅻ퉬瑜??꾩삁 鍮쇱꽌(洹몃┝+?곗튂 紐⑤몢) ?쒗듃 議곗옉????留됱쓬.
        val showBottomBar = (currentRoute == null || currentRoute in bottomBarRoutes) &&
            (overlaySheetTop?.let { it > navTopPx } ?: true)

        NavGraph(
            navController = navController,
            // hazeSource: ???곸뿭(?붾㈃ 肄섑뀗痢????좊━???먮━寃?鍮꾩튌 '?먮낯'.
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
                            // ?쒗듃??????씤 遺遺??쒗듃 ???????꾩そ)留?洹몃┝
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
