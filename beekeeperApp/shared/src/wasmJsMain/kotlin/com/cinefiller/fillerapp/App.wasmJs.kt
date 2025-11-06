// File: shared/src/wasmJsMain/kotlin/com/cinefiller/fillerapp/App.wasmJs.kt
package com.beekeeper.app

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.beekeeper.app.presentation.screens.*
import com.beekeeper.app.presentation.components.*
import com.beekeeper.app.data.WasmStorageManager
import com.beekeeper.app.network.WasmNetworkClient
import kotlinx.browser.window

@Composable
actual fun App() {
    val navController = rememberNavController()
    val storageManager = LocalStorageManager.current
    val networkClient = LocalNetworkClient.current
    
    // Track online status
    var isOnline by remember { mutableStateOf(window.navigator.onLine) }
    
    DisposableEffect(Unit) {
        val onlineHandler = { _: dynamic -> isOnline = true }
        val offlineHandler = { _: dynamic -> isOnline = false }
        
        window.addEventListener("online", onlineHandler)
        window.addEventListener("offline", offlineHandler)
        
        onDispose {
            window.removeEventListener("online", onlineHandler)
            window.removeEventListener("offline", offlineHandler)
        }
    }
    
    MaterialTheme(
        colorScheme = darkColorScheme()
    ) {
        Scaffold(
            topBar = {
                WasmTopBar(
                    navController = navController,
                    isOnline = isOnline
                )
            },
            bottomBar = {
                WasmBottomNavigation(navController)
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                WasmNavHost(
                    navController = navController,
                    storageManager = storageManager,
                    networkClient = networkClient
                )
            }
        }
    }
}

@Composable
fun WasmTopBar(
    navController: NavHostController,
    isOnline: Boolean
) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    
    TopAppBar(
        title = {
            Text(getScreenTitle(currentRoute))
        },
        actions = {
            // Online/Offline indicator
            if (!isOnline) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(
                        "Offline",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onError
                    )
                }
            }
            
            // Install PWA button
            var showInstallButton by remember { mutableStateOf(false) }
            
            DisposableEffect(Unit) {
                val handler = { event: dynamic ->
                    event.preventDefault()
                    showInstallButton = true
                    window.asDynamic().deferredPrompt = event
                }
                
                window.addEventListener("beforeinstallprompt", handler)
                
                onDispose {
                    window.removeEventListener("beforeinstallprompt", handler)
                }
            }
            
            if (showInstallButton) {
                IconButton(
                    onClick = {
                        window.asDynamic().deferredPrompt?.prompt()
                        showInstallButton = false
                    }
                ) {
                    Icon(
                        Icons.Default.Download,
                        contentDescription = "Install App"
                    )
                }
            }
            
            // Sync status
            IconButton(onClick = { /* Trigger sync */ }) {
                Icon(
                    if (isOnline) Icons.Default.Cloud else Icons.Default.CloudOff,
                    contentDescription = if (isOnline) "Synced" else "Offline"
                )
            }
        }
    )
}

@Composable
fun WasmBottomNavigation(navController: NavHostController) {
    val items = listOf(
        NavigationItem("home", "Home", Icons.Default.Home),
        NavigationItem("projects", "Projects", Icons.Default.Folder),
        NavigationItem("create", "Create", Icons.Default.Add),
        NavigationItem("library", "Library", Icons.Default.Collections),
        NavigationItem("explore", "Explore", Icons.Default.Explore)
    )
    
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    
    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@Composable
fun WasmNavHost(
    navController: NavHostController,
    storageManager: WasmStorageManager,
    networkClient: WasmNetworkClient
) {
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                onNavigateToProject = { projectId ->
                    navController.navigate("project/$projectId")
                },
                onNavigateToCreate = {
                    navController.navigate("create")
                },
                onNavigateToProjects = {
                    navController.navigate("project")
                },
                onNavigateToCreateProject = {
                    navController.navigate("create")
                },
                onQuickAction = {
                    navController.navigate("create")
                },
            )
        }
        
        composable("projects") {
            ProjectListScreen(
                storageManager = storageManager,
                onProjectClick = { project ->
                    navController.navigate("project/${project.id}")
                }
            )
        }
        
        composable("create") {
            ContentCreationScreen(
                networkClient = networkClient,
                storageManager = storageManager,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable("library") {
            LibraryScreen(
                onNavigateToHome = TODO(),
                onNavigateToExplore = TODO(),
                onNavigateToCreate = TODO(),
                onNavigateToProfile = TODO(),
                onItemClick = TODO(),
                onAddNew = TODO()
            )
        }
        
        composable("explore") {
            ExploreScreen(
                onContentClick = { content ->
                    // Handle content click
                },
                onNavigateBack = TODO(),
                onNavigateToHome = TODO(),
                onNavigateToCreate = TODO(),
                onNavigateToProfile = TODO()
            )
        }
        
        composable("project/{projectId}") { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            ProjectDetailScreen(
                projectId = projectId,
                //onNavigateToEditor = { editorType ->
                //    navController.navigate("project/$projectId/$editorType")
                //},
                viewModel = TODO(),
                onNavigateBack = TODO(),
                onNavigateToPhase = TODO(),
                onNavigateToTeam = TODO(),
                onNavigateToAnalytics = TODO()
            )
        }
        
        composable("project/{projectId}/script-editor") { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            EnhancedScriptDevelopmentScreen(
                projectId = projectId,
                navController = navController
            )
        }
        
        composable("project/{projectId}/avatar-studio") { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            AvatarStudioScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSaveAvatar = TODO(),
                onResetAvatar = TODO()
            )
        }
        
        composable("project/{projectId}/publishing") { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            PublishingScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onConnectPlatform = TODO(),
                onNavigateToHome = TODO(),
                onNavigateToCreate = TODO(),
                onNavigateToContent = TODO(),
                onNavigateToProfile = TODO()
            )
        }
    }
}

@Composable
fun ContentCreationScreen(
    networkClient: WasmNetworkClient,
    storageManager: WasmStorageManager,
    onNavigateBack: () -> Boolean
) {
    TODO("Not yet implemented")
}

fun getScreenTitle(route: String?): String {
    return when {
        route == null -> "CineFiller"
        route == "home" -> "Home"
        route == "projects" -> "Projects"
        route == "create" -> "Create"
        route == "library" -> "Library"
        route == "explore" -> "Explore"
        route.startsWith("project/") -> {
            when {
                route.endsWith("/script-editor") -> "Script Editor"
                route.endsWith("/storyboard") -> "Storyboard"
                route.endsWith("/avatar-studio") -> "Avatar Studio"
                route.endsWith("/publishing") -> "Publishing"
                else -> "Project"
            }
        }
        else -> "CineFiller"
    }
}

data class NavigationItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)
