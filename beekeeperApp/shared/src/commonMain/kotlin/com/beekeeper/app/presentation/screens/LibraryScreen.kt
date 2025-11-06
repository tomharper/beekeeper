// File: fillerApp/shared/src/commonMain/kotlin/com/cinefiller/fillerapp/presentation/screens/LibraryScreen.kt
package com.beekeeper.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beekeeper.app.presentation.theme.ThemeManager

sealed class LibraryTab(val title: String) {
    object All : LibraryTab("All")
    object Images : LibraryTab("Images")
    object Videos : LibraryTab("Videos")
}

data class LibraryItem(
    val id: String,
    val type: LibraryItemType,
    val thumbnailUrl: String = ""
)

enum class LibraryItemType {
    IMAGE, VIDEO
}

@Composable
fun LibraryScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToExplore: () -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onItemClick: (LibraryItem) -> Unit,
    onAddNew: () -> Unit
) {
    val theme by ThemeManager.currentTheme.collectAsState()
    var selectedTab by remember { mutableStateOf<LibraryTab>(LibraryTab.All) }

    val libraryItems = remember {
        List(12) { index ->
            LibraryItem(
                id = "item_$index",
                type = if (index % 3 == 0) LibraryItemType.VIDEO else LibraryItemType.IMAGE
            )
        }
    }

    val filteredItems = when (selectedTab) {
        is LibraryTab.All -> libraryItems
        is LibraryTab.Images -> libraryItems.filter { it.type == LibraryItemType.IMAGE }
        is LibraryTab.Videos -> libraryItems.filter { it.type == LibraryItemType.VIDEO }
    }

    Scaffold(
        topBar = {
            LibraryTopBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                onAddNew = onAddNew,
                theme = theme
            )
        },
        containerColor = theme.colors.background
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = paddingValues.calculateTopPadding() + 16.dp,
                bottom = paddingValues.calculateBottomPadding() + 16.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(filteredItems) { item ->
                LibraryItemCard(
                    item = item,
                    onClick = { onItemClick(item) },
                    theme = theme
                )
            }
        }
    }
}

@Composable
fun LibraryTopBar(
    selectedTab: LibraryTab,
    onTabSelected: (LibraryTab) -> Unit,
    onAddNew: () -> Unit,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(theme.colors.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Library",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = theme.colors.textPrimary
            )

            IconButton(onClick = onAddNew) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add New",
                    tint = theme.colors.textPrimary
                )
            }
        }

        TabRow(
            selectedTabIndex = when (selectedTab) {
                is LibraryTab.All -> 0
                is LibraryTab.Images -> 1
                is LibraryTab.Videos -> 2
            },
            containerColor = theme.colors.background,
            contentColor = theme.colors.textPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[when (selectedTab) {
                        is LibraryTab.All -> 0
                        is LibraryTab.Images -> 1
                        is LibraryTab.Videos -> 2
                    }]),
                    color = theme.colors.primary
                )
            }
        ) {
            Tab(
                selected = selectedTab is LibraryTab.All,
                onClick = { onTabSelected(LibraryTab.All) },
                text = {
                    Text(
                        "All",
                        color = if (selectedTab is LibraryTab.All)
                            theme.colors.primary else theme.colors.textSecondary
                    )
                }
            )
            Tab(
                selected = selectedTab is LibraryTab.Images,
                onClick = { onTabSelected(LibraryTab.Images) },
                text = {
                    Text(
                        "Images",
                        color = if (selectedTab is LibraryTab.Images)
                            theme.colors.primary else theme.colors.textSecondary
                    )
                }
            )
            Tab(
                selected = selectedTab is LibraryTab.Videos,
                onClick = { onTabSelected(LibraryTab.Videos) },
                text = {
                    Text(
                        "Videos",
                        color = if (selectedTab is LibraryTab.Videos)
                            theme.colors.primary else theme.colors.textSecondary
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryItemCard(
    item: LibraryItem,
    onClick: () -> Unit,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        colors = CardDefaults.cardColors(
            containerColor = theme.colors.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Placeholder for thumbnail
            Icon(
                when (item.type) {
                    LibraryItemType.IMAGE -> Icons.Default.Image
                    LibraryItemType.VIDEO -> Icons.Default.VideoLibrary
                },
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = theme.colors.textSecondary.copy(alpha = 0.5f)
            )

            // Video indicator
            if (item.type == LibraryItemType.VIDEO) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(
                            theme.colors.background.copy(alpha = 0.8f),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = theme.colors.textPrimary
                        )
                        Text(
                            "Video",
                            fontSize = 10.sp,
                            color = theme.colors.textPrimary
                        )
                    }
                }
            }
        }
    }
}