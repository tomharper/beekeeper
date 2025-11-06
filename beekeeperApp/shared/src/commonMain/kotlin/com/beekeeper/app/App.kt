package com.beekeeper.app

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.beekeeper.app.presentation.theme.BeekeeperTheme
import com.beekeeper.app.presentation.screens.HomeScreen
import com.beekeeper.app.presentation.screens.ApiaryListScreen
import com.beekeeper.app.presentation.screens.TaskListScreen
import com.beekeeper.app.presentation.screens.ProfileScreen

/**
 * Main Beekeeper App Entry Point
 * Sets up navigation and theme
 */
@Composable
fun App() {
    BeekeeperTheme {
        val navController = rememberNavController()

        Scaffold(
            bottomBar = {
                BottomNavigationBar(navController)
            }
        ) { paddingValues ->
            NavigationHost(
                navController = navController,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        }
    }
}

/**
 * Bottom Navigation Bar
 * Shows 5 navigation items: Dashboard, Hives, Advisor, Tasks, Profile
 */
@Composable
private fun BottomNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = com.beekeeper.app.presentation.theme.NavBarBackground
    ) {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title
                    )
                },
                label = {
                    Text(text = item.title)
                },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            // Pop up to start destination to avoid building large back stack
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            // Avoid multiple copies of same destination
                            launchSingleTop = true
                            // Restore state when reselecting previously selected item
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = com.beekeeper.app.presentation.theme.NavBarItemActive,
                    selectedTextColor = com.beekeeper.app.presentation.theme.NavBarItemActive,
                    unselectedIconColor = com.beekeeper.app.presentation.theme.NavBarItemInactive,
                    unselectedTextColor = com.beekeeper.app.presentation.theme.NavBarItemInactive,
                    indicatorColor = com.beekeeper.app.presentation.theme.DarkGreenSecondary
                )
            )
        }
    }
}

/**
 * Navigation Host
 * Manages navigation between screens
 */
@Composable
private fun NavigationHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Route.Home.route,
        modifier = modifier
    ) {
        composable(Route.Home.route) {
            HomeScreen(
                onNavigateToApiary = { apiaryId ->
                    navController.navigate("${Route.ApiaryDetail.route}/$apiaryId")
                }
            )
        }

        composable(Route.Apiaries.route) {
            ApiaryListScreen(
                onApiaryClick = { apiaryId ->
                    navController.navigate("${Route.ApiaryDetail.route}/$apiaryId")
                },
                onAddApiary = {
                    navController.navigate(Route.AddApiary.route)
                }
            )
        }

        composable(Route.Advisor.route) {
            // TODO: AIAdvisorScreen()
            PlaceholderScreen("AI Expert Advisor")
        }

        composable(Route.Tasks.route) {
            TaskListScreen(
                onTaskClick = { taskId ->
                    navController.navigate("${Route.TaskDetail.route}/$taskId")
                },
                onAddTask = {
                    navController.navigate(Route.AddTask.route)
                }
            )
        }

        composable(Route.Profile.route) {
            ProfileScreen(
                onNavigateToSettings = {
                    navController.navigate(Route.Settings.route)
                }
            )
        }

        // Detail screens
        composable("${Route.ApiaryDetail.route}/{apiaryId}") { backStackEntry ->
            val apiaryId = backStackEntry.arguments?.getString("apiaryId") ?: ""
            // TODO: ApiaryDetailScreen(apiaryId)
            PlaceholderScreen("Apiary Detail: $apiaryId")
        }

        composable("${Route.HiveDetail.route}/{hiveId}") { backStackEntry ->
            val hiveId = backStackEntry.arguments?.getString("hiveId") ?: ""
            // TODO: HiveDetailScreen(hiveId)
            PlaceholderScreen("Hive Detail: $hiveId")
        }

        composable("${Route.TaskDetail.route}/{taskId}") { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
            // TODO: TaskDetailScreen(taskId)
            PlaceholderScreen("Task Detail: $taskId")
        }

        // Add/Edit screens
        composable(Route.AddApiary.route) {
            // TODO: AddApiaryScreen()
            PlaceholderScreen("Add Apiary")
        }

        composable(Route.AddTask.route) {
            // TODO: AddTaskScreen()
            PlaceholderScreen("Add Task")
        }

        composable(Route.Settings.route) {
            // TODO: SettingsScreen()
            PlaceholderScreen("Settings")
        }
    }
}

/**
 * Placeholder screen for unimplemented screens
 */
@Composable
private fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text(
            text = "$title\n(Coming Soon)",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

/**
 * Navigation routes
 */
sealed class Route(val route: String) {
    object Home : Route("home")
    object Apiaries : Route("apiaries")
    object Advisor : Route("advisor")
    object Tasks : Route("tasks")
    object Profile : Route("profile")

    // Detail screens
    object ApiaryDetail : Route("apiary_detail")
    object HiveDetail : Route("hive_detail")
    object TaskDetail : Route("task_detail")

    // Add/Edit screens
    object AddApiary : Route("add_apiary")
    object AddTask : Route("add_task")
    object Settings : Route("settings")
}

/**
 * Bottom navigation item data
 */
private data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val title: String
)

/**
 * Bottom navigation items configuration
 */
private val bottomNavItems = listOf(
    BottomNavItem(
        route = Route.Home.route,
        icon = Icons.Default.Home,
        title = "Dashboard"
    ),
    BottomNavItem(
        route = Route.Apiaries.route,
        icon = Icons.Default.GridView,
        title = "Hives"
    ),
    BottomNavItem(
        route = Route.Advisor.route,
        icon = Icons.Default.Lightbulb,
        title = "Advisor"
    ),
    BottomNavItem(
        route = Route.Tasks.route,
        icon = Icons.Default.Task,
        title = "Tasks"
    ),
    BottomNavItem(
        route = Route.Profile.route,
        icon = Icons.Default.Person,
        title = "Profile"
    )
)
