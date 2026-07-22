package com.talkqquest.app.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.talkqquest.app.R
import com.talkqquest.app.core.designsystem.Gray100
import com.talkqquest.app.core.designsystem.Gray300
import com.talkqquest.app.core.designsystem.Primary500
import com.talkqquest.app.core.designsystem.Primary600
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.White
import com.talkqquest.app.core.designsystem.softShadow
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

// ?섎떒 ?ㅻ퉬 ?????덈뒗 ?좊━ ?뚯빟(?붿옄??CSS 媛?洹몃?濡?.
// ?뚯빟: ?믪씠 64 / radius 32 / ?곗깋 0.8 + 釉붾윭 10 / ?뚮몢由???0.3 / 洹몃┝??0 -2 12 寃??%
// ?좏깮 移? 92x44 / radius 22 / ??0.28 + 釉붾윭 10 / ?뚮몢由???0.4 / 湲濡쒖슦 0 6 24 蹂대씪(114,100,248) 14%
// 釉붾윭: Haze(?덈뱶12+ 吏꾩쭨 釉붾윭 / 洹?誘몃쭔 ?댄듃 fallback).

// route媛 ?랁븳 ?? ??쓽 ?섏쐞 ?붾㈃(?? 誘몄뀡 紐⑸줉 = ???뚮줈???먯꽌???뚯냽 ??씠 怨꾩냽 ?섏씠?쇱씠?몃릺寃???
private fun tabRouteOf(route: String?): String? = when (route) {
    Screen.MISSION_LIST -> Screen.HOME
    Screen.MISSION_DETAIL -> Screen.HOME
    Screen.CONVERSATION_PREP -> Screen.HOME
    Screen.CONVERSATION -> Screen.HOME
    Screen.REPORT -> Screen.HOME // ???뚮줈???쇰뱶諛????곸꽭 由ы룷??濡?吏꾩엯 ?????좎? (?ъ슜??寃곗젙)
    Screen.PROFILE_BADGES -> Screen.PROFILE
    Screen.PROFILE_RECENT_MISSION -> Screen.PROFILE
    else -> route
}

@Composable
fun TqBottomBar(
    navController: NavHostController,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    TqBottomBarContent(
        currentRoute = currentRoute,
        onTabClick = { route ->
            // ??쓣 ?꾨Ⅴ硫? 洹????뚮줈???덉そ ?붾㈃(?? ?댿넂誘몄뀡 ?곸꽭?믩???以鍮????덈뜑?쇰룄
            // 洹???쓽 ?쒖옉 ?붾㈃?쇰줈 ?섎룎?꾩삤寃??? (restoreState=true瑜??곕㈃ ?ㅼ뼱媛붾뜕 ?섏쐞 ?붾㈃??
            // ?섏궡?ㅼ꽌 "???뚮윭??硫붿씤?쇰줈 ???ㅻ뒗" 臾몄젣媛 ?앷꺼 類?????= ??긽 洹???猷⑦듃濡?)
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                launchSingleTop = true
            }
        },
        hazeState = hazeState,
        modifier = modifier,
    )
}

@Composable
private fun TqBottomBarContent(
    currentRoute: String?,
    onTabClick: (String) -> Unit,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .navigationBarsPadding()
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .height(64.dp)
            // ?뚯빟 洹몃┝?? ?꾨줈 2px, ?먮┝ 12, 寃??6% (CSS 0 -2 12 rgba(0,0,0,0.06))
            .softShadow(
                color = Color.Black.copy(alpha = 0.06f),
                offsetX = 0.dp,
                offsetY = (-2).dp,
                blur = 12.dp,
                cornerRadius = 32.dp,
            ),
        contentAlignment = Alignment.Center,
    ) {
        // ?좊━ 諛곌꼍 痢? ?ш린留??κ?寃?clip. (洹몃옒???꾨옒 肄섑뀗痢좎쓽 移?湲濡쒖슦?????섎┝)
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(32.dp))
                .hazeEffect(state = hazeState) {
                    blurRadius = 10.dp
                    backgroundColor = White
                    tints = listOf(HazeTint(White.copy(alpha = 0.8f)))
                }
                .border(1.dp, White.copy(alpha = 0.3f), RoundedCornerShape(32.dp)),
        )
        // ?꾩씠肄?移?痢? clip ?놁쓬 ??移?蹂대씪 湲濡쒖슦媛 ?뚯빟 諛뽰쑝濡쒕룄 ?먯뿰?ㅻ읇寃?踰덉쭚(?쇨렇留덉쿂??.
        // BoxWithConstraints濡??뚯빟 ?ㅼ젣 ??쓣 ?뚯븘 ?좏깮 移???쓣 ?뺥븿(醫곸? ?붾㈃?먯꽑 移?異뺤냼).
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            // ??媛꾧꺽 gap = (?뚯빟??- 醫뚯슦?몄뀑 65 - ?꾩씠肄?媛?4*44=176)) / 3.
            // ?좏깮 移???= 44 + ?ㅻ쾭?? ?ㅻ쾭?됱씠 (gap-4) ?댄븯媛 ?섍쾶 ?≪븘 ???꾩씠肄섍낵 ??寃뱀묠.
            // 393 ???됰꼮?섎㈃ ?곹븳 92(?붿옄?멸컪), 醫곸쑝硫?異뺤냼(?섑븳 56).
            val tabGap = (maxWidth - 65.dp - 176.dp) / 3f
            val chipWidth = (44.dp + (tabGap - 4.dp) * 2f).coerceIn(56.dp, 92.dp)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    // 醫뚯슦 32.5 怨좎젙 ?몄뀑(?붿옄??. ?몄뀑 > 移??ㅻ쾭?됱씠??留⑤걹 ???좏깮?쇰룄 移⑹씠 ?뚯빟 諛뽰쑝濡????섍컧.
                    .padding(horizontal = 32.5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                BottomNavItem.entries.forEach { item ->
                    val selected = tabRouteOf(currentRoute) == item.route
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) {
                                // ?섏씠?쇱씠???щ?媛 ?꾨땲??"?ㅼ젣 ?꾩옱 ?붾㈃"?쇰줈 ?먮떒:
                                // 媛숈? ?붾㈃?대㈃ 臾댁떆, ??쓽 ?섏쐞 ?붾㈃(?? 誘몄뀡 紐⑸줉)?대㈃ ??猷⑦듃濡?蹂듦?.
                                if (currentRoute != item.route) onTabClick(item.route)
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        // ?좏깮 移??꾩씠肄섎낫???볦? ?κ렐 吏곸궗媛곹삎, 44 諛뺤뒪瑜??섏뼱 洹몃젮吏?.
                        // 移??좊━(haze)媛 ?뚯빟(0.8)蹂대떎 ???섏뼐 ?대몼寃?蹂댁뿬, 諛앹? ?곗깋(0.9) ?ㅻ쾭?덉씠濡?泥섎━.
                        // API 28 誘몃쭔: 湲濡쒖슦(softShadow)媛 ??洹몃젮吏怨??뚯빟??遺덊닾紐????댄듃??
                        // ??移⑹씠 ??蹂댁엫 ???고쉶??Gray100) 梨꾩??쇰줈 ?좏깮 ?쒖떆瑜???좏븿.
                        if (selected) {
                            val legacyChip = android.os.Build.VERSION.SDK_INT < 28
                            Box(
                                modifier = Modifier
                                    .requiredSize(width = chipWidth, height = 44.dp)
                                    // 蹂대씪 湲濡쒖슦: ?꾨옒 6, ?먮┝ 24, 蹂대씪 14% (CSS 0 6 24 rgba(114,100,248,0.14))
                                    .softShadow(
                                        color = Primary500.copy(alpha = 0.14f),
                                        offsetX = 0.dp,
                                        offsetY = 6.dp,
                                        blur = 24.dp,
                                        cornerRadius = 22.dp,
                                    )
                                    .background(
                                        if (legacyChip) Gray100 else White.copy(alpha = 0.9f),
                                        RoundedCornerShape(22.dp),
                                    )
                                    .border(1.dp, White.copy(alpha = 0.4f), RoundedCornerShape(22.dp)),
                            )
                        }
                        // ?꾨줈?꾨쭔 ?좏깮 ??'蹂대씪 諛곌꼍 + ?고쉶???щ엺'(?쇨렇留?諛섏쟾). ?됱씠 baked??
                        // ?꾩슜 ?쒕줈?대툝??tint ?놁씠 洹몃┝. ?섎㉧吏 ??룸??좏깮? ?⑥깋 tint 洹몃?濡?
                        val profileSelected = selected && item == BottomNavItem.Profile
                        Icon(
                            painter = painterResource(
                                if (profileSelected) R.drawable.ic_nav_profile_selected else item.iconRes,
                            ),
                            contentDescription = item.label,
                            tint = when {
                                profileSelected -> Color.Unspecified
                                selected -> Primary600
                                else -> Gray300
                            },
                            // ?꾩씠肄?踰≫꽣媛 44?꾨젅?꾩뿉 CSS inset?濡?諛곗튂??44dp濡?梨꾩슦硫??ш린쨌?꾩튂 ?뺥솗.
                            modifier = Modifier.size(44.dp),
                        )
                    }
                }
            }
        }
    }
}

// Preview: ??肄섑뀗痢좉? ?놁뼱 釉붾윭????蹂댁씠吏留??댄듃留? 諛곗튂/移??꾩씠肄??뺤씤??
// 諛곌꼍? ?ㅼ젣 ??諛곌꼍 Gray50(#F8FAFC)濡?留욎땄 ???ㅺ린湲??먮?怨?媛숈? ?ㅼ쑝濡?蹂댁엫.
// ??퀎 誘몃━蹂닿린: 393=?붿옄??湲곗??? 360=?뷀븳 ???섏묠 寃利?, 320=理쒖냼 ?? ??媛??????섏퀜???뺤긽.
@Preview(name = "?ㅻ퉬 393dp", widthDp = 393, showBackground = true, backgroundColor = 0xFFF8FAFC)
@Preview(name = "?ㅻ퉬 360dp", widthDp = 360, showBackground = true, backgroundColor = 0xFFF8FAFC)
@Preview(name = "?ㅻ퉬 320dp", widthDp = 320, showBackground = true, backgroundColor = 0xFFF8FAFC)
@Composable
private fun TqBottomBarPreview() {
    TalkQQuestTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
        ) {
            TqBottomBarContent(
                currentRoute = Screen.HOME,
                onTabClick = {},
                hazeState = remember { HazeState() },
            )
        }
    }
}

// 留??쇱そ(?꾩뭅?대툕) ?좏깮 ??移⑹씠 ?뚯빟 諛뽰쑝濡????먯졇?섏삤?붿? ?뺤씤??
@Preview(name = "?쇰걹 ?좏깮 393dp", widthDp = 393, showBackground = true, backgroundColor = 0xFFF8FAFC)
@Preview(name = "?쇰걹 ?좏깮 360dp", widthDp = 360, showBackground = true, backgroundColor = 0xFFF8FAFC)
@Preview(name = "?쇰걹 ?좏깮 320dp", widthDp = 320, showBackground = true, backgroundColor = 0xFFF8FAFC)
@Composable
private fun TqBottomBarEdgePreview() {
    TalkQQuestTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
        ) {
            TqBottomBarContent(
                currentRoute = Screen.ARCHIVE_HOME,
                onTabClick = {},
                hazeState = remember { HazeState() },
            )
        }
    }
}
