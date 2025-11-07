package com.beekeeper.app.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable
    data object ApiaryList : Screen

    @Serializable
    data class ApiaryDashboard(val apiaryId: String) : Screen

    @Serializable
    data class HiveDetails(val hiveId: String) : Screen

    @Serializable
    data object AIAdvisor : Screen

    @Serializable
    data object Tasks : Screen

    @Serializable
    data object Map : Screen

    @Serializable
    data object Profile : Screen
}
