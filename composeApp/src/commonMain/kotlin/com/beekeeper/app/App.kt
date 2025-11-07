package com.beekeeper.app

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.beekeeper.app.navigation.Screen
import com.beekeeper.app.ui.screens.advisor.AIAdvisorScreen
import com.beekeeper.app.ui.screens.apiary.ApiaryListScreen
import com.beekeeper.app.ui.screens.dashboard.ApiaryDashboardScreen
import com.beekeeper.app.ui.screens.hive.HiveDetailsScreen
import com.beekeeper.app.ui.screens.tasks.TasksScreen
import com.beekeeper.app.ui.screens.inspections.InspectionsScreen
import com.beekeeper.app.ui.viewmodel.InspectionsViewModel
import com.beekeeper.app.ui.theme.BeekeeperTheme
import com.beekeeper.app.ui.navigation.BottomNavigationBar
import org.koin.compose.koinInject

@Composable
fun App() {
    BeekeeperTheme {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        Scaffold(
            containerColor = Color(0xFF0F0F0F),
            bottomBar = {
                BottomNavigationBar(
                    currentRoute = currentRoute,
                    onNavigate = { screen ->
                        navController.navigate(screen) {
                            // Pop up to start destination to avoid building up a large stack
                            popUpTo(Screen.ApiaryList) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    }
                )
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = Screen.ApiaryList
            ) {
                composable<Screen.ApiaryList> {
                    ApiaryListScreen(
                        onApiaryClick = { apiaryId ->
                            navController.navigate(Screen.ApiaryDashboard(apiaryId))
                        }
                    )
                }

            composable<Screen.ApiaryDashboard> { backStackEntry ->
                val args = backStackEntry.toRoute<Screen.ApiaryDashboard>()
                ApiaryDashboardScreen(
                    apiaryId = args.apiaryId,
                    onHiveClick = { hiveId ->
                        navController.navigate(Screen.HiveDetails(hiveId))
                    },
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onNavigateToAdvisor = {
                        navController.navigate(Screen.AIAdvisor)
                    }
                )
            }

            composable<Screen.HiveDetails> { backStackEntry ->
                val args = backStackEntry.toRoute<Screen.HiveDetails>()
                HiveDetailsScreen(
                    hiveId = args.hiveId,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable<Screen.AIAdvisor> {
                AIAdvisorScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable<Screen.Tasks> {
                TasksScreen(
                    viewModel = koinInject()
                )
            }

            composable<Screen.Inspections> {
                InspectionsScreen(
                    viewModel = koinInject(),
                    onCreateInspection = {
                        navController.navigate(Screen.CreateInspection())
                    }
                )
            }

            composable<Screen.InspectionsByHive> { backStackEntry ->
                val args = backStackEntry.toRoute<Screen.InspectionsByHive>()
                val viewModel: InspectionsViewModel = koinInject()

                // Load inspections for specific hive
                LaunchedEffect(args.hiveId) {
                    viewModel.loadInspections(args.hiveId)
                }

                InspectionsScreen(
                    viewModel = viewModel,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onCreateInspection = {
                        navController.navigate(Screen.CreateInspection(hiveId = args.hiveId))
                    }
                )
            }

            composable<Screen.CreateInspection> { backStackEntry ->
                val args = backStackEntry.toRoute<Screen.CreateInspection>()
                com.beekeeper.app.ui.screens.inspections.CreateInspectionScreen(
                    viewModel = koinInject(),
                    hiveId = args.hiveId,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onInspectionCreated = {
                        navController.popBackStack()
                    }
                )
            }
            }
        }
    }
}
