package com.beekeeper.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.beekeeper.app.navigation.Screen

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.ApiaryList, "Apiaries", Icons.Default.Home),
    BottomNavItem(Screen.Tasks, "Tasks", Icons.Default.CheckCircle),
    BottomNavItem(Screen.Inspections, "Inspections", Icons.Default.Search),
    BottomNavItem(Screen.AIAdvisor, "AI Advisor", Icons.Default.SmartToy)
)

@Composable
fun BottomNavigationBar(
    currentRoute: String?,
    onNavigate: (Screen) -> Unit
) {
    NavigationBar(
        containerColor = Color(0xFF1A1A1A),
        contentColor = Color.White
    ) {
        bottomNavItems.forEach { item ->
            val isSelected = when (item.screen) {
                is Screen.ApiaryList -> currentRoute?.contains("ApiaryList") == true ||
                                        currentRoute?.contains("ApiaryDashboard") == true ||
                                        currentRoute?.contains("HiveDetails") == true
                is Screen.Tasks -> currentRoute?.contains("Tasks") == true
                is Screen.Inspections -> currentRoute?.contains("Inspections") == true
                is Screen.AIAdvisor -> currentRoute?.contains("AIAdvisor") == true
                else -> false
            }

            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.screen) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFFFFD700),
                    selectedTextColor = Color(0xFFFFD700),
                    unselectedIconColor = Color(0xFF888888),
                    unselectedTextColor = Color(0xFF888888),
                    indicatorColor = Color(0xFF2A2A2A)
                )
            )
        }
    }
}
