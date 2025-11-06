// File: fillerApp/shared/src/commonMain/kotlin/com/example/fillerapp/presentation/screens/ExploreScreen.kt
package com.beekeeper.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

sealed class ExploreTab(val title: String) {
    object ForYou : ExploreTab("For you")
    object Following : ExploreTab("Following")
    object Trending : ExploreTab("Trending")
}

data class ExploreContent(
    val id: String,
    val imageUrl: String,
    val description: String = ""
)

@Composable
fun ExploreScreen(
    onNavigateBack: () -> Unit,
    onContentClick: (ExploreContent) -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    var selectedTab by remember { mutableStateOf<ExploreTab>(ExploreTab.ForYou) }
    var searchQuery by remember { mutableStateOf("") }
    
    val forYouContent = remember {
        List(6) { index ->
            ExploreContent(
                id = "fy_$index",
                imageUrl = "",
                description = "Content ${index + 1}"
            )
        }
    }
    
    Scaffold(
        containerColor = Color(0xFF1A1A1A)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                
                Text(
                    "Explore",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.width(48.dp))
            }
            
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text("Search", color = Color.Gray)
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.Gray
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4A90E2),
                    unfocusedBorderColor = Color(0xFF3A3A3A),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White,
                    focusedContainerColor = Color(0xFF2A2A2A),
                    unfocusedContainerColor = Color(0xFF2A2A2A)
                ),
                shape = RoundedCornerShape(24.dp),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Tabs
            ExploreTabs(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
            
            // Content Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(forYouContent) { content ->
                    ExploreContentCard(
                        content = content,
                        onClick = { onContentClick(content) }
                    )
                }
            }
        }
    }
}

@Composable
fun ExploreTabs(
    selectedTab: ExploreTab,
    onTabSelected: (ExploreTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        listOf(ExploreTab.ForYou, ExploreTab.Following, ExploreTab.Trending).forEach { tab ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTabSelected(tab) },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    tab.title,
                    color = if (selectedTab == tab) Color.White else Color.Gray,
                    fontSize = 16.sp,
                    fontWeight = if (selectedTab == tab) FontWeight.Medium else FontWeight.Normal
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (selectedTab == tab) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(Color.White)
                    )
                } else {
                    Spacer(modifier = Modifier.height(2.dp))
                }
            }
        }
    }
}

@Composable
fun ExploreContentCard(
    content: ExploreContent,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .aspectRatio(0.75f)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2A2A2A)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Placeholder for image
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFE8D4B0))
            )
            
            // Optional overlay text
            if (content.description.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .background(
                            Color.Black.copy(alpha = 0.5f)
                        )
                        .padding(8.dp)
                ) {
                    Text(
                        content.description,
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
