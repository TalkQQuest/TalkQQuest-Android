package com.talkqquest.app.navigation

import androidx.annotation.DrawableRes
import com.talkqquest.app.R

// 하단 네비 4탭. 아이콘은 디자인 SVG에서 뽑은 벡터 드로어블(res/drawable/ic_nav_*).
enum class BottomNavItem(
    val route: String,
    val label: String,
    @DrawableRes val iconRes: Int,
) {
    Home(Screen.HOME, "홈", R.drawable.ic_nav_home),
    Mission(Screen.MISSION_LIST, "미션", R.drawable.ic_nav_mission),
    Archive(Screen.ARCHIVE_HOME, "아카이브", R.drawable.ic_nav_archive),
    Profile(Screen.PROFILE, "프로필", R.drawable.ic_nav_profile),
}
