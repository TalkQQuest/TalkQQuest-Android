package com.talkqquest.app.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
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
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

// ???? ?섎떒 ?ㅻ퉬(???덈뒗 ?좊━ ?뚯빟)媛 ?붾㈃ ?꾩뿉 寃뱀퀜 ?щ떎.
// hazeState: ??肄섑뀗痢?NavGraph)瑜??좊━(?섎떒 ?ㅻ퉬)媛 ?먮━寃?鍮꾩텛?꾨줉 ?곌껐?섎뒗 ?곹깭.
@Composable
fun MainScreen() {
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // ?섎떒諛??쒖떆 route: ??4媛?+ ?붿옄?몄긽 ?섎떒諛붽? ?덈뒗 ?붾㈃(誘몄뀡 紐⑸줉). 洹????먮룞 ?④?.
    // currentRoute == null = 泥??꾨젅???쒖옉 ?붾㈃ ?명똿 ?? ???④꼈????쾶 ?⑥? ?딄쾶 諛붾줈 ?쒖떆.
    // ??CONVERSATION_PREP(誘몄뀡 吏꾩엯)? ?쒖쇅 ??UI 7李?"誘몄뀡 吏꾩엯" ?꾨젅?꾩뿉 ?섎떒 ?ㅻ퉬寃뚯씠?섏씠 ?놁쓬(2026-07-19 CSS ?뺤씤).
    val bottomBarRoutes = BottomNavItem.entries.map { it.route } +
        Screen.MISSION_LIST + Screen.MISSION_DETAIL + Screen.CONVERSATION +
        Screen.PROFILE_BADGES + Screen.PROFILE_RECENT_MISSION
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
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = hazeState),
            onOverlaySheetTop = { overlaySheetTop = it },
        )
        if (showBottomBar) {
            TqBottomBar(
                navController = navController,
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
    }
}
