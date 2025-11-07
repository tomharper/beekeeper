package com.beekeeper.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
import org.koin.compose.koinInject

@Composable
fun App() {
    BeekeeperTheme {
        val navController = rememberNavController()

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
                    viewModel = koinInject(),
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable<Screen.Inspections> {
                InspectionsScreen(
                    viewModel = koinInject(),
                    onBackClick = {
                        navController.popBackStack()
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
                    }
                )
            }
        }
    }
}
