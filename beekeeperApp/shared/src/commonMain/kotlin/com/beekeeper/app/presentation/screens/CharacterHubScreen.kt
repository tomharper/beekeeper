// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/presentation/screens/CharacterProfileScreen.kt
package com.beekeeper.app.presentation.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beekeeper.app.presentation.viewmodels.CharacterProfileViewModel
import com.beekeeper.app.presentation.viewmodels.CharacterAnalysisViewModel
import com.beekeeper.app.domain.model.*
import com.beekeeper.app.presentation.components.SecondaryTopBar
import com.beekeeper.app.presentation.theme.rememberAppTheme
import com.beekeeper.app.presentation.theme.AppTheme
import kotlinx.coroutines.launch

// Helper function to get role colors based on theme
@Composable
private fun getRoleColor(role: CharacterRole): Color {
    val theme = rememberAppTheme()
    return when (role) {
        CharacterRole.PROTAGONIST -> theme.colors.primary
        CharacterRole.ANTAGONIST -> theme.colors.error
        CharacterRole.MENTOR -> theme.colors.success
        CharacterRole.LOVE_INTEREST -> Color(0xFFEC4899) // Pink - works in both modes
        else -> theme.colors.textSecondary
    }
}

/**
 * Character Hub Screen with improved tab structure
 * Summary view shows thumbnails and basic info
 * Detail view shows full character information in tabs
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterProfileScreen(
    projectId: String,
    viewModel: CharacterProfileViewModel,
    onNavigateBack: () -> Unit,
    onCharacterSelect: (CharacterProfile) -> Unit,
    onAddCharacter: () -> Unit,
    onEditCharacter: (CharacterProfile) -> Unit,
    onDeleteCharacter: (CharacterProfile) -> Unit,
    onAssignAvatar: (CharacterProfile) -> Unit,
    onAssignVoice: (CharacterProfile) -> Unit,
    onViewCharacterDetails: (CharacterProfile) -> Unit,
    onExtractFromScript: () -> Unit,
    onNavigateToAvatarStudio: () -> Unit,
    onNavigateToRelationshipDetails: (String, String) -> Unit,
    onNavigateToGalleryItem: (String) -> Unit,
    onNavigateToTimelineEvent: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedCharacter by viewModel.selectedCharacter.collectAsState()
    val scope = rememberCoroutineScope()
    val theme = rememberAppTheme()

    // Initialize with project ID on first composition
    LaunchedEffect(projectId) {
        viewModel.initializeForProject(projectId)
        viewModel.loadCharacters(projectId)
    }

    Scaffold(
        containerColor = theme.colors.background,
        topBar = {
            SecondaryTopBar(
                title = "Character Hub",
                onNavigateBack = onNavigateBack,
                actions = {
                    IconButton(onClick = onExtractFromScript) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = "Extract from Script",
                            tint = rememberAppTheme().colors.onSurface
                        )
                    }
                    IconButton(onClick = onAddCharacter) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Character",
                            tint = rememberAppTheme().colors.onSurface
                        )
                    }
                }
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (selectedCharacter == null) {
                // Summary View - Grid of characters
                CharacterSummaryView(
                    characters = uiState.characters,
                    isLoading = uiState.isLoading,
                    error = uiState.error,
                    onCharacterClick = { character ->
                        viewModel.selectCharacter(character)
                        onCharacterSelect(character)
                    },
                    onRefresh = {
                        scope.launch {
                            viewModel.loadCharacters(projectId)
                        }
                    }
                )
            } else {
                // Detail View - Selected character with tabs
                CharacterDetailView(
                    character = selectedCharacter!!,
                    characters = uiState.characters, // For relationships
                    onBackClick = { viewModel.clearSelection() },
                    onEditClick = { onEditCharacter(selectedCharacter!!) },
                    onDeleteClick = { onDeleteCharacter(selectedCharacter!!) },
                    onAssignAvatar = { onAssignAvatar(selectedCharacter!!) },
                    onAssignVoice = { onAssignVoice(selectedCharacter!!) },
                    onRelationshipClick = onNavigateToRelationshipDetails,
                    onGalleryItemClick = onNavigateToGalleryItem,
                    onTimelineEventClick = onNavigateToTimelineEvent,
                    onAddMedia = onNavigateToAvatarStudio
                )
            }
        }
    }
}

/**
 * Summary view showing list of characters organized by importance
 */
@Composable
fun CharacterSummaryView(
    characters: List<CharacterProfile>,
    isLoading: Boolean,
    error: String?,
    onCharacterClick: (CharacterProfile) -> Unit,
    onRefresh: () -> Unit
) {
    val theme = rememberAppTheme()

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = theme.colors.primary
                )
            }
            error != null -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Error loading characters",
                        color = theme.colors.error,
                        fontSize = 16.sp
                    )
                    Text(
                        error,
                        color = theme.colors.textSecondary,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Button(
                        onClick = onRefresh,
                        modifier = Modifier.padding(top = 16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = theme.colors.primary
                        )
                    ) {
                        Text("Retry")
                    }
                }
            }
            characters.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = theme.colors.textSecondary,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        "No characters yet",
                        color = theme.colors.textSecondary,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                    Text(
                        "Add characters or extract from script",
                        color = theme.colors.textSecondary.copy(alpha = 0.6f),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            else -> {
                // Sort characters by importance (role priority, then screen time)
                val sortedCharacters = remember(characters) {
                    characters.sortedWith(
                        compareBy(
                            {
                                // Priority order: Protagonist > Antagonist > Mentor > Supporting > Others
                                when (it.role) {
                                    CharacterRole.PROTAGONIST -> 0
                                    CharacterRole.ANTAGONIST -> 1
                                    CharacterRole.MENTOR -> 2
                                    CharacterRole.LOVE_INTEREST -> 3
                                    CharacterRole.SUPPORTING -> 4
                                    CharacterRole.SIDEKICK -> 5
                                    CharacterRole.COMIC_RELIEF -> 6
                                    CharacterRole.NARRATOR -> 7
                                    CharacterRole.MINOR -> 8
                                    CharacterRole.EXTRA -> 9
                                    CharacterRole.BETRAYOR -> 10
                                    CharacterRole.ORACULAR_FATES -> 11
                                    CharacterRole.BACKGROUND -> 12
                                    CharacterRole.DEUTERAGONIST -> 13
                                    CharacterRole.VICTIM -> 14
                                    CharacterRole.ALLY -> 15
                                    CharacterRole.EMOTIONAL_CORE -> 16
                                    CharacterRole.CATALYST -> 17
                                    CharacterRole.COMPANION -> 18
                                    CharacterRole.LEADER -> 19
                                    CharacterRole.GUIDE_ANTAGONIST -> 20
                                    CharacterRole.ANTAGONIST_VICTIM -> 21
                                    CharacterRole.MENTOR_SAVIOR -> 22
                                    CharacterRole.KEY_CHILD -> 23
                                }
                            },
                            { -it.screenTime } // Then by screen time (descending)
                        )
                    )
                }

                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Group characters by role category
                    val primaryCharacters = sortedCharacters.filter {
                        it.role in listOf(CharacterRole.PROTAGONIST, CharacterRole.ANTAGONIST, CharacterRole.MENTOR)
                    }
                    val secondaryCharacters = sortedCharacters.filter {
                        it.role in listOf(CharacterRole.LOVE_INTEREST, CharacterRole.SUPPORTING, CharacterRole.SIDEKICK, CharacterRole.COMIC_RELIEF)
                    }
                    val minorCharacters = sortedCharacters.filter {
                        it.role in listOf(CharacterRole.NARRATOR, CharacterRole.MINOR, CharacterRole.EXTRA)
                    }

                    // Primary Characters Section
                    if (primaryCharacters.isNotEmpty()) {
                        item {
                            Text(
                                "PRIMARY CHARACTERS",
                                color = theme.colors.textSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
                            )
                        }
                        items(primaryCharacters) { character ->
                            CharacterListItem(
                                character = character,
                                onClick = { onCharacterClick(character) },
                                isPrimary = true
                            )
                        }
                    }

                    // Secondary Characters Section
                    if (secondaryCharacters.isNotEmpty()) {
                        item {
                            Text(
                                "SUPPORTING CHARACTERS",
                                color = theme.colors.textSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(bottom = 8.dp, top = 16.dp)
                            )
                        }
                        items(secondaryCharacters) { character ->
                            CharacterListItem(
                                character = character,
                                onClick = { onCharacterClick(character) },
                                isPrimary = false
                            )
                        }
                    }

                    // Minor Characters Section
                    if (minorCharacters.isNotEmpty()) {
                        item {
                            Text(
                                "MINOR CHARACTERS",
                                color = theme.colors.textSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(bottom = 8.dp, top = 16.dp)
                            )
                        }
                        items(minorCharacters) { character ->
                            CharacterListItem(
                                character = character,
                                onClick = { onCharacterClick(character) },
                                isPrimary = false
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Character list item for improved readability
 */
@Composable
fun CharacterListItem(
    character: CharacterProfile,
    onClick: () -> Unit,
    isPrimary: Boolean = false
) {
    val theme = rememberAppTheme()

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isPrimary) theme.colors.surface else theme.colors.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (isPrimary) BorderStroke(1.dp, getRoleColor(character.role).copy(alpha = 0.3f)) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(if (isPrimary) 64.dp else 56.dp)
                    .clip(CircleShape)
                    .background(
                        when (character.role) {
                            CharacterRole.PROTAGONIST -> getRoleColor(character.role).copy(alpha = 0.2f)
                            CharacterRole.ANTAGONIST -> theme.colors.error.copy(alpha = 0.2f)
                            CharacterRole.MENTOR -> theme.colors.success.copy(alpha = 0.2f)
                            else -> Color(0xFF6B7280).copy(alpha = 0.2f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (character.imageUrl != null) {
                    // TODO: Load actual image
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = when (character.role) {
                            CharacterRole.PROTAGONIST -> getRoleColor(character.role)
                            CharacterRole.ANTAGONIST -> theme.colors.error
                            CharacterRole.MENTOR -> theme.colors.success
                            else -> Color(0xFF6B7280)
                        },
                        modifier = Modifier.size(if (isPrimary) 32.dp else 28.dp)
                    )
                } else {
                    Text(
                        text = character.name.split(" ")
                            .take(2)
                            .map { it.first() }
                            .joinToString(""),
                        color = when (character.role) {
                            CharacterRole.PROTAGONIST -> getRoleColor(character.role)
                            CharacterRole.ANTAGONIST -> theme.colors.error
                            CharacterRole.MENTOR -> theme.colors.success
                            else -> Color(0xFF6B7280)
                        },
                        fontSize = if (isPrimary) 20.sp else 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Character Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Name and Role Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = character.name,
                        color = theme.colors.textPrimary,
                        fontSize = if (isPrimary) 18.sp else 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    // Role Badge
                    Surface(
                        color = when (character.role) {
                            CharacterRole.PROTAGONIST -> getRoleColor(character.role)
                            CharacterRole.ANTAGONIST -> theme.colors.error
                            CharacterRole.MENTOR -> theme.colors.success
                            CharacterRole.LOVE_INTEREST -> Color(0xFFEC4899)
                            else -> Color(0xFF6B7280)
                        }.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = character.role.name.replace("_", " "),
                            color = when (character.role) {
                                CharacterRole.PROTAGONIST -> getRoleColor(character.role)
                                CharacterRole.ANTAGONIST -> theme.colors.error
                                CharacterRole.MENTOR -> theme.colors.success
                                CharacterRole.LOVE_INTEREST -> Color(0xFFEC4899)
                                else -> theme.colors.textSecondary
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                // Archetype and Description
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    character.archetype?.let { archetype ->
                        Text(
                            text = archetype,
                            color = theme.colors.textSecondary,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text("•", color = theme.colors.textSecondary.copy(alpha = 0.5f), fontSize = 12.sp)
                    }

                    // Brief description or personality trait
                    character.personality?.traits?.firstOrNull()?.let { trait ->
                        Text(
                            text = trait.name,
                            color = theme.colors.textSecondary,
                            fontSize = 13.sp,
                            fontStyle = FontStyle.Italic,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Stats Row
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Screen Time
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = getRoleColor(character.role),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${(character.screenTime * 100).toInt()}%",
                            color = theme.colors.textPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "screen",
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
                            tint = getRoleColor(character.role),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${character.dialogueCount}",
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

                    // Connections/Relationships
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Group,
                            contentDescription = null,
                            tint = getRoleColor(character.role),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${character.relationships.size}",
                            color = theme.colors.textPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "connections",
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

/**
 * Detailed view with tabs for selected character
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterDetailView(
    character: CharacterProfile,
    characters: List<CharacterProfile>,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onAssignAvatar: () -> Unit,
    onAssignVoice: () -> Unit,
    onRelationshipClick: (String, String) -> Unit,
    onGalleryItemClick: (String) -> Unit,
    onTimelineEventClick: (String) -> Unit,
    onAddMedia: () -> Unit
) {
    val theme = rememberAppTheme()
    var selectedTabIndex by remember { mutableStateOf(0) }

    val tabs = listOf(
        "Overview",
        "Psychological Profile",
        "Relationships",
        "Character Arcs",
        "Gallery"
    )

    Column(modifier = Modifier.fillMaxSize()) {
        // Header with back button and character name
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = theme.colors.textPrimary
                )
            }
            Text(
                text = character.name,
                color = theme.colors.textPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onEditClick) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = theme.colors.textPrimary
                )
            }
            IconButton(onClick = onDeleteClick) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = theme.colors.error
                )
            }
        }

        // Tab Row
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = theme.colors.background,
            contentColor = theme.colors.textPrimary,
            divider = {
                HorizontalDivider(
                    color = theme.colors.textSecondary.copy(alpha = 0.2f)
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTabIndex == index)
                                FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTabIndex == index)
                                getRoleColor(character.role) else theme.colors.textSecondary,
                            fontSize = 14.sp
                        )
                    }
                )
            }
        }

        // Tab Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when (selectedTabIndex) {
                0 -> CharacterOverviewTab(character, onAssignAvatar, onAssignVoice)
                1 -> PsychologicalProfileTab(character)
                2 -> RelationshipsTab(character, characters, onRelationshipClick)
                3 -> CharacterArcsTab(character, onTimelineEventClick)
                4 -> GalleryTab(character, onGalleryItemClick, onAddMedia)
            }
        }
    }
}

/**
 * Overview Tab - Basic info and summary
 */
@Composable
fun CharacterOverviewTab(
    character: CharacterProfile,
    onAssignAvatar: () -> Unit,
    onAssignVoice: () -> Unit
) {
    val theme = rememberAppTheme()

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Avatar and basic info
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(theme.colors.surface)
                        .clickable { onAssignAvatar() },
                    contentAlignment = Alignment.Center
                ) {
                    if (character.imageUrl != null) {
                        // TODO: Load actual image
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = getRoleColor(character.role),
                            modifier = Modifier.size(60.dp)
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                tint = theme.colors.textSecondary,
                                modifier = Modifier.size(40.dp)
                            )
                            Text(
                                "Add Avatar",
                                color = theme.colors.textSecondary,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }

                // Basic Info
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InfoRow("Role", character.role.name.replace("_", " "))
                    InfoRow("Age", if (character.age > 0) "${character.age}" else "Unknown")
                    InfoRow("Archetype", character.archetype ?: "Not set")
                    InfoRow("Screen Time", "${(character.screenTime * 100).toInt()}%")
                    InfoRow("Dialogue", "${character.dialogueCount} lines")
                }
            }
        }

        // Physical Attributes
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = theme.colors.surface
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        "Physical Attributes",
                        color = theme.colors.textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            InfoRow("Height", character.height)
                            InfoRow("Build", character.build)
                        }
                        Column {
                            InfoRow("Hair", character.hairColor)
                            InfoRow("Eyes", character.eyeColor)
                        }
                    }

                    if (character.distinctiveFeatures.isNotEmpty()) {
                        Text(
                            "Distinctive Features",
                            color = theme.colors.textSecondary,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                        )
                        character.distinctiveFeatures.forEach { feature ->
                            Text(
                                "• $feature",
                                color = theme.colors.textPrimary,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // TODO: Backstory- this will crash
        if (character.personality.backstory.isNotBlank()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = theme.colors.surface
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            "Backstory",
                            color = theme.colors.textPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            character.personality.backstory,
                            color = theme.colors.textSecondary,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }

        // Voice Assignment
        item {
            Card(
                onClick = onAssignVoice,
                colors = CardDefaults.cardColors(
                    containerColor = theme.colors.surface
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Voice",
                            color = theme.colors.textPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            if (character.assignedAvatarId != null) "Voice assigned" else "No voice assigned",
                            color = theme.colors.textSecondary,
                            fontSize = 14.sp
                        )
                    }
                    Icon(
                        Icons.Default.KeyboardArrowRight,
                        contentDescription = null,
                        tint = theme.colors.textSecondary
                    )
                }
            }
        }
    }
}

/**
 * Psychological Profile Tab
 */
@Composable
fun PsychologicalProfileTab(character: CharacterProfile) {
    val theme = rememberAppTheme()

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // OCEAN Personality
        character.personality?.oceanScores?.let { ocean ->
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = theme.colors.surface
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            Icon(
                                Icons.Default.Psychology,
                                contentDescription = null,
                                tint = getRoleColor(character.role),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "OCEAN Personality Model",
                                color = theme.colors.textPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        OceanScoreBar("Openness", ocean.openness,
                            "Imagination, curiosity, and openness to new experiences", character)
                        OceanScoreBar("Conscientiousness", ocean.conscientiousness,
                            "Organization, dependability, and self-discipline", character)
                        OceanScoreBar("Extraversion", ocean.extraversion,
                            "Sociability, assertiveness, and positive emotions", character)
                        OceanScoreBar("Agreeableness", ocean.agreeableness,
                            "Trust, cooperation, and compassion", character)
                        OceanScoreBar("Neuroticism", ocean.neuroticism,
                            "Emotional instability, anxiety, and moodiness", character)
                    }
                }
            }
        }

        // Personality Traits
        if (character.personality?.traits?.isNotEmpty() == true) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = theme.colors.surface
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            Icon(
                                Icons.Default.Stars,
                                contentDescription = null,
                                tint = getRoleColor(character.role),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Personality Traits",
                                color = theme.colors.textPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        character.personality.traits.forEach { trait ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        trait.name,
                                        color = theme.colors.textPrimary,
                                        fontSize = 15.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        "${(trait.strength * 100).toInt()}%",
                                        color = getRoleColor(character.role),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = trait.strength,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 6.dp)
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = getRoleColor(character.role),
                                    trackColor = theme.colors.background
                                )
                            }
                        }
                    }
                }
            }
        }

        // Motivations
        if (character.personality?.motivations?.isNotEmpty() == true) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = theme.colors.surface
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Icon(
                                Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = theme.colors.success,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Motivations",
                                color = theme.colors.textPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        character.personality.motivations.forEach { motivation ->
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(theme.colors.success)
                                        .align(Alignment.CenterVertically)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    motivation,
                                    color = theme.colors.textSecondary,
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Fears
        if (character.personality?.fears?.isNotEmpty() == true) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = theme.colors.surface
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = theme.colors.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Fears",
                                color = theme.colors.textPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        character.personality.fears.forEach { fear ->
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(theme.colors.error)
                                        .align(Alignment.CenterVertically)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    fear,
                                    color = theme.colors.textSecondary,
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // AI Insights
        character.personality?.aiInsights?.let { insights ->
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = theme.colors.surface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, getRoleColor(character.role).copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = getRoleColor(character.role),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "AI Insights",
                                color = theme.colors.textPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            insights,
                            color = theme.colors.textSecondary,
                            fontSize = 14.sp,
                            lineHeight = 22.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Relationships Tab
 */
@Composable
fun RelationshipsTab(
    character: CharacterProfile,
    allCharacters: List<CharacterProfile>,
    onRelationshipClick: (String, String) -> Unit
) {
    val theme = rememberAppTheme()

    if (character.relationships.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Group,
                    contentDescription = null,
                    tint = theme.colors.textSecondary,
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    "No relationships defined",
                    color = theme.colors.textSecondary,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(character.relationships) { relationship ->
                val targetCharacter = allCharacters.find { it.id == relationship.targetCharacterId }

                Card(
                    onClick = {
                        onRelationshipClick(character.id, relationship.targetCharacterId)
                    },
                    colors = CardDefaults.cardColors(
                        containerColor = theme.colors.surface
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Target character avatar
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(getRoleColor(character.role).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = relationship.targetCharacterName.take(2).uppercase(),
                                color = getRoleColor(character.role),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 12.dp)
                        ) {
                            Text(
                                relationship.targetCharacterName,
                                color = theme.colors.textPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                relationship.relationshipType.replace("_", " "),
                                color = getRoleColor(character.role),
                                fontSize = 12.sp
                            )
                            Text(
                                relationship.description,
                                color = theme.colors.textSecondary,
                                fontSize = 12.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        // Relationship strength
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                progress = relationship.strength,
                                modifier = Modifier.size(32.dp),
                                color = getRoleColor(character.role),
                                strokeWidth = 3.dp
                            )
                            Text(
                                "${(relationship.strength * 100).toInt()}%",
                                color = theme.colors.textSecondary,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Character Arcs Tab
 */
@Composable
fun CharacterArcsTab(
    character: CharacterProfile,
    onTimelineEventClick: (String) -> Unit
) {
    val theme = rememberAppTheme()

    // TODO: Implement character arc timeline with real data
    // For now, showing placeholder
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.ShowChart,
                contentDescription = null,
                tint = theme.colors.textSecondary,
                modifier = Modifier.size(64.dp)
            )
            Text(
                "Character arc visualization coming soon",
                color = theme.colors.textSecondary,
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                "Will show character's journey and growth",
                color = theme.colors.textSecondary.copy(alpha = 0.6f),
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

/**
 * Gallery Tab
 */
@Composable
fun GalleryTab(
    character: CharacterProfile,
    onGalleryItemClick: (String) -> Unit,
    onAddMedia: () -> Unit
) {
    val theme = rememberAppTheme()

    // TODO: Implement gallery with real media items
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 120.dp),
            contentPadding = PaddingValues(4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Add media button
            item {
                Card(
                    onClick = onAddMedia,
                    colors = CardDefaults.cardColors(
                        containerColor = theme.colors.surface
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .aspectRatio(1f)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                tint = theme.colors.textSecondary,
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                "Add Media",
                                color = theme.colors.textSecondary,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            // Placeholder gallery items
            items(6) { index ->
                Card(
                    onClick = { onGalleryItemClick("media_$index") },
                    colors = CardDefaults.cardColors(
                        containerColor = theme.colors.surfaceVariant
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.aspectRatio(1f)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = null,
                            tint = theme.colors.textSecondary.copy(alpha = 0.3f),
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }
        }
    }
}

// Helper Components

@Composable
fun InfoRow(label: String, value: String) {
    val theme = rememberAppTheme()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            color = theme.colors.textSecondary,
            fontSize = 12.sp
        )
        Text(
            value,
            color = theme.colors.textPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun OceanScoreBar(
    trait: String,
    score: Float,
    description: String = "",
    character: CharacterProfile
) {
    val theme = rememberAppTheme()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    trait,
                    color = theme.colors.textPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
                if (description.isNotEmpty()) {
                    Text(
                        description,
                        color = theme.colors.textSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            Text(
                "${(score * 100).toInt()}%",
                color = getRoleColor(character.role),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
        LinearProgressIndicator(
            progress = score,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = when {
                score > 0.7f -> theme.colors.success // Green for high
                score > 0.4f -> getRoleColor(character.role) // Purple for medium
                else -> Color(0xFFF59E0B) // Amber for low
            },
            trackColor = theme.colors.background
        )
    }
}