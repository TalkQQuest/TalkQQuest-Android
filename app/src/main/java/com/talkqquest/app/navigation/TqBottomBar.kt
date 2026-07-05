package com.talkqquest.app.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.talkqquest.app.core.designsystem.Gray300
import com.talkqquest.app.core.designsystem.Primary100
import com.talkqquest.app.core.designsystem.Primary600
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.White

// 하단 네비 바. 탭 이동 시 백스택 안 쌓고 상태 보존.
@Composable
fun TqBottomBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(containerColor = White) {
        BottomNavItem.entries.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label, style = TqType.LabelM) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Primary600,
                    selectedTextColor = Primary600,
                    unselectedIconColor = Gray300,
                    unselectedTextColor = Gray300,
                    indicatorColor = Primary100,
                ),
            )
        }
    }
}
