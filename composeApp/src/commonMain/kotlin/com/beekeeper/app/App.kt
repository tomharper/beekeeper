package com.beekeeper.app

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.beekeeper.app.navigation.Screen
import com.beekeeper.app.ui.screens.advisor.AIAdvisorScreen
import com.beekeeper.app.ui.screens.apiary.ApiaryListScreen
import com.beekeeper.app.ui.screens.dashboard.ApiaryDashboardScreen
import com.beekeeper.app.ui.screens.hive.HiveDetailsScreen
import com.beekeeper.app.ui.theme.BeekeeperTheme

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
        }
    }
}
