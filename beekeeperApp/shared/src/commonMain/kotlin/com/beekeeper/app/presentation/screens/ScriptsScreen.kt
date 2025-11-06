// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/presentation/screens/ScriptsScreen.kt
package com.beekeeper.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beekeeper.app.domain.model.*
import com.beekeeper.app.presentation.components.SecondaryTopBar
import com.beekeeper.app.presentation.theme.ThemeManager
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScriptsScreen(
    projectId: String,
    onNavigateBack: () -> Unit,
    onScriptClick: (Script) -> Unit
) {
    val theme by ThemeManager.currentTheme.collectAsState()
    val contentRepository = remember { com.beekeeper.app.domain.repository.ContentRepositoryImpl() }
    var scripts by remember { mutableStateOf<List<Script>>(emptyList()) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Load scripts for this project
    LaunchedEffect(projectId) {
        scripts = contentRepository.getScripts(projectId)
    }

    Scaffold(
        topBar = {
            SecondaryTopBar(
                title = "Script",
                onNavigateBack = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    scope.launch {
                        snackbarHostState.showSnackbar("Create new script feature coming soon!")
                    }
                },
                containerColor = theme.colors.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Script")
            }
        },
        containerColor = theme.colors.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            if (scripts.isEmpty()) {
                item {
                    Text(
                        text = "No scripts available",
                        fontSize = 14.sp,
                        color = theme.colors.textSecondary,
                        modifier = Modifier.padding(vertical = 32.dp)
                    )
                }
            } else {
                items(scripts.size) { index ->
                    val script = scripts[index]
                    ScriptCard(
                        title = script.title,
                        sceneCount = script.sceneScripts.size,
                        dialogueCount = script.sceneScripts.sumOf { it.dialogue.size },
                        status = script.status.name,
                        onClick = { onScriptClick(script) },
                        theme = theme
                    )
                }
            }
        }
    }
}

@Composable
private fun ScriptCard(
    title: String,
    sceneCount: Int,
    dialogueCount: Int,
    status: String,
    onClick: () -> Unit,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = theme.colors.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Script icon/avatar
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(theme.colors.primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Description,
                    contentDescription = null,
                    tint = theme.colors.primary,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Script Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Title
                Text(
                    text = title,
                    color = theme.colors.textPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Status Badge
                Surface(
                    color = theme.colors.primary.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = status.replace("_", " "),
                        color = theme.colors.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                // Stats Row
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Scene Count
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Movie,
                            contentDescription = null,
                            tint = theme.colors.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "$sceneCount",
                            color = theme.colors.textPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "scenes",
                            color = theme.colors.textSecondary,
                            fontSize = 12.sp
                        )
                    }

                    // Dialogue Count
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Chat,
                            contentDescription = null,
                            tint = theme.colors.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "$dialogueCount",
                            color = theme.colors.textPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "lines",
                            color = theme.colors.textSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Chevron
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = theme.colors.textSecondary.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
