package com.talkqquest.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

// 하단 네비 4탭 (CONVENTIONS.md 8번). TODO: 아이콘을 피그마 아이콘으로 교체(지금은 Material 임시).
enum class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    Archive(Screen.ARCHIVE_HOME, "아카이브", Icons.AutoMirrored.Filled.List),
    Home(Screen.HOME, "홈", Icons.Filled.Home),
    Community(Screen.COMMUNITY_LIST, "모임", Icons.Filled.Person),
    Profile(Screen.PROFILE, "프로필", Icons.Filled.AccountBox),
}
