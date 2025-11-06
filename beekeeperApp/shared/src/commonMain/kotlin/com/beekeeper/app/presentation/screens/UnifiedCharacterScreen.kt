// shared/src/commonMain/kotlin/com/cinefiller/fillerapp/presentation/screens/UnifiedCharacterScreen.kt
@file:OptIn(ExperimentalMaterial3Api::class)

package com.beekeeper.app.presentation.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.beekeeper.app.domain.model.*
import com.beekeeper.app.presentation.components.ArcStage
import com.beekeeper.app.presentation.components.CharacterArcTimeline
import com.beekeeper.app.presentation.components.CharacterArcType
import com.beekeeper.app.presentation.components.SecondaryTopBar
import com.beekeeper.app.presentation.viewmodels.CharacterProfileViewModel
import kotlinx.coroutines.launch
import com.beekeeper.app.presentation.theme.rememberAppTheme

enum class CharacterScreenMode {
    CREATE,
    EDIT,
    VIEW
}

enum class CharacterTab {
    FEATURES,
    PROFILE,
    RELATIONSHIPS,
    GALLERY
}

/**
 * Unified Character Screen for Create/Edit/View modes
 * Replaces CharacterAvatarAssignmentScreen, AvatarCreationScreen, and CharacterDetailView
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnifiedCharacterScreen(
    mode: CharacterScreenMode,
    projectId: String,
    characterId: String? = null, // null for CREATE mode
    viewModel: CharacterProfileViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: ((String) -> Unit)? = null,
    onSaveComplete: (CharacterProfile) -> Unit,
    onNavigateToAvatarStudio: (String) -> Unit,
    onNavigateToRelationship: (String, String) -> Unit
) {
    val selectedCharacter by viewModel.selectedCharacter.collectAsState()
    val isLoading by viewModel.uiState.collectAsState()
    var isSaving by remember { mutableStateOf(false) }
    var isGenerating by remember { mutableStateOf(false) }
    val generatedAvatars by remember { mutableStateOf<List<Avatar>>(emptyList()) }
    val voiceSamples by remember { mutableStateOf<List<VoiceProfile>>(emptyList()) }

    // For CREATE mode, we'll use a local state for the new character
    var newCharacter by remember { mutableStateOf<CharacterProfile?>(null) }

    // Use either the selected character or the new character based on mode
    val currentCharacter = when (mode) {
        CharacterScreenMode.CREATE -> newCharacter
        else -> selectedCharacter
    }

    var selectedTab by remember { mutableStateOf(CharacterTab.FEATURES) }
    val isEditable = mode != CharacterScreenMode.VIEW
    val coroutineScope = rememberCoroutineScope()

// Update the LaunchedEffect in UnifiedCharacterScreen:
    LaunchedEffect(mode, characterId, projectId) {
        when (mode) {
            CharacterScreenMode.CREATE -> {
                newCharacter = createDefaultCharacter(projectId)
                viewModel.clearSelection()
            }
            CharacterScreenMode.EDIT, CharacterScreenMode.VIEW -> {
                if (!characterId.isNullOrEmpty()) {
                    // Step 1: Load ALL characters for the project
                    viewModel.loadCharacters(projectId) // Pass projectId, not characterId!

                    // Step 3: Find and select the specific character
                    val allCharacters = viewModel.characters.value
                    println("DEBUG: Loaded ${allCharacters.size} characters for project $projectId")

                    val character = allCharacters.find { it.id == characterId }
                    if (character != null) {
                        println("DEBUG: Found character - ${character.name}")
                        println("DEBUG: Character relationships: ${character.relationships.size}")
                        println("DEBUG: Character OCEAN scores: ${character.personality?.oceanScores}")
                        viewModel.selectCharacter(character)
                    } else {
                        println("ERROR: Character with id $characterId not found in project $projectId")
                        println("Available character IDs: ${allCharacters.map { it.id }}")
                    }
                }
            }
        }
    }

    var showCharacterDevelopment by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            SecondaryTopBar(
                title = when (mode) {
                    CharacterScreenMode.CREATE -> "Create Character"
                    CharacterScreenMode.EDIT -> "Edit Character"
                    CharacterScreenMode.VIEW -> currentCharacter?.name ?: "Character Details"
                },
                subtitle = if (currentCharacter != null && mode != CharacterScreenMode.CREATE) {
                    "${currentCharacter!!.role} • ${currentCharacter!!.archetype}"
                } else null,
                onNavigateBack = onNavigateBack,
                actions = {
                    if (mode == CharacterScreenMode.VIEW) {
                        IconButton(
                            onClick = {
                                onNavigateToEdit?.invoke(characterId!!)
                            }
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit Character",
                                tint = rememberAppTheme().colors.onSurface
                            )
                        }
                    }
                    if (mode == CharacterScreenMode.EDIT || mode == CharacterScreenMode.CREATE) {
                        TextButton(
                            onClick = {
                                coroutineScope.launch {
                                    isSaving = true
                                    try {
                                        when (mode) {
                                            CharacterScreenMode.CREATE -> {
                                                // For create mode, use the newCharacter
                                                newCharacter?.let { char ->
                                                    val createdChar = viewModel.createCharacter(char)
                                                    onSaveComplete(createdChar)
                                                }
                                            }
                                            else -> {
                                                // For edit mode, update the existing character
                                                currentCharacter?.let { char ->
                                                    viewModel.updateCharacter(char)
                                                    onSaveComplete(char)
                                                }
                                            }
                                        }
                                    } finally {
                                        isSaving = false
                                    }
                                }
                            },
                            enabled = !isSaving && currentCharacter != null
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    "Save",
                                    color = rememberAppTheme().colors.primary
                                )
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (mode == CharacterScreenMode.EDIT || mode == CharacterScreenMode.CREATE) {
                FloatingActionButton(
                    onClick = { showCharacterDevelopment = true },
                    containerColor = rememberAppTheme().colors.primary,
                    contentColor = rememberAppTheme().colors.onPrimary
                ) {
                    Icon(
                        Icons.Default.Psychology,
                        contentDescription = "Character Development"
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab.ordinal,
                modifier = Modifier.fillMaxWidth()
            ) {
                CharacterTab.values().forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = {
                            Text(
                                when (tab) {
                                    CharacterTab.FEATURES -> "Features"
                                    CharacterTab.PROFILE -> "Profile"
                                    CharacterTab.RELATIONSHIPS -> "Relationships"
                                    CharacterTab.GALLERY -> "Gallery"
                                }
                            )
                        }
                    )
                }
            }

            // Tab Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (selectedTab) {
                    CharacterTab.FEATURES -> FeaturesTab(
                        character = currentCharacter,
                        isEditable = isEditable,
                        isGenerating = isGenerating,
                        onUpdateCharacter = { updatedChar ->
                            when (mode) {
                                CharacterScreenMode.CREATE -> newCharacter = updatedChar
                                else -> viewModel.updateCharacter(updatedChar)
                            }
                        },
                        onGenerateAvatar = {
                            coroutineScope.launch {
                                isGenerating = true
                                // Call AI generation
                                isGenerating = false
                            }
                        },
                        onGenerateVoice = {
                            coroutineScope.launch {
                                isGenerating = true
                                // Call voice generation
                                isGenerating = false
                            }
                        }
                    )

                    CharacterTab.PROFILE -> ProfileTab(
                        character = currentCharacter,
                        isEditable = isEditable,
                        onUpdateCharacter = { updatedChar ->
                            when (mode) {
                                CharacterScreenMode.CREATE -> newCharacter = updatedChar
                                else -> viewModel.updateCharacter(updatedChar)
                            }
                        },
                        onGenerateBackstory = {
                            coroutineScope.launch {
                                isGenerating = true
                                // Call AI backstory generation
                                isGenerating = false
                            }
                        }
                    )

                    CharacterTab.RELATIONSHIPS -> RelationshipsTab(
                        character = currentCharacter,
                        projectId = projectId,
                        isEditable = isEditable,
                        onAddRelationship = { targetId, relationship ->
                            when (mode) {
                                CharacterScreenMode.CREATE -> {
                                    currentCharacter?.let { char ->
                                        val updatedRelationships = char.relationships + relationship
                                        newCharacter = char.copy(relationships = updatedRelationships)
                                    }
                                }
                                else -> {
                                    currentCharacter?.let { char ->
                                        val updatedRelationships = char.relationships + relationship
                                        viewModel.updateCharacter(char.copy(relationships = updatedRelationships))
                                    }
                                }
                            }
                        },
                        onUpdateRelationship = { relationship ->
                            when (mode) {
                                CharacterScreenMode.CREATE -> {
                                    currentCharacter?.let { char ->
                                        val updatedRelationships = char.relationships.map {
                                            if (it.targetCharacterId == relationship.targetCharacterId) relationship else it
                                        }
                                        newCharacter = char.copy(relationships = updatedRelationships)
                                    }
                                }
                                else -> {
                                    currentCharacter?.let { char ->
                                        val updatedRelationships = char.relationships.map {
                                            if (it.targetCharacterId == relationship.targetCharacterId) relationship else it
                                        }
                                        viewModel.updateCharacter(char.copy(relationships = updatedRelationships))
                                    }
                                }
                            }
                        },
                        onRemoveRelationship = { relationshipId ->
                            when (mode) {
                                CharacterScreenMode.CREATE -> {
                                    currentCharacter?.let { char ->
                                        val updatedRelationships = char.relationships.filter {
                                            it.targetCharacterId != relationshipId
                                        }
                                        newCharacter = char.copy(relationships = updatedRelationships)
                                    }
                                }
                                else -> {
                                    currentCharacter?.let { char ->
                                        val updatedRelationships = char.relationships.filter {
                                            it.targetCharacterId != relationshipId
                                        }
                                        viewModel.updateCharacter(char.copy(relationships = updatedRelationships))
                                    }
                                }
                            }
                        },
                        onNavigateToRelationship = onNavigateToRelationship
                    )

                    CharacterTab.GALLERY -> GalleryTab(
                        character = currentCharacter,
                        generatedAvatars = generatedAvatars,
                        voiceSamples = voiceSamples,
                        isEditable = isEditable,
                        onSelectAvatar = { avatarId ->
                            when (mode) {
                                CharacterScreenMode.CREATE -> {
                                    currentCharacter?.let { char ->
                                        newCharacter = char.copy(assignedAvatarId = avatarId)
                                    }
                                }
                                else -> {
                                    currentCharacter?.let { char ->
                                        viewModel.updateCharacter(char.copy(assignedAvatarId = avatarId))
                                    }
                                }
                            }
                        },
                        onRegenerateAvatar = {
                            coroutineScope.launch {
                                isGenerating = true
                                // Regenerate avatar
                                isGenerating = false
                            }
                        },
                        onNavigateToAvatarStudio = onNavigateToAvatarStudio
                    )
                }
            }

            // Bottom Action Bar
            if (isEditable) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                    ) {
                        if (mode == CharacterScreenMode.CREATE) {
                            OutlinedButton(
                                onClick = {
                                    // Save as draft
                                }
                            ) {
                                Text("Save as Draft")
                            }
                        }

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    isGenerating = true
                                    // Generate missing elements
                                    isGenerating = false
                                }
                            },
                            enabled = !isGenerating
                        ) {
                            if (isGenerating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.AutoAwesome, null)
                            }
                            Spacer(Modifier.width(8.dp))
                            Text("Generate Missing")
                        }

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    isSaving = true
                                    try {
                                        when (mode) {
                                            CharacterScreenMode.CREATE -> {
                                                // For create mode, save the new character
                                                newCharacter?.let { char ->
                                                    // In real implementation, save to repository
                                                    onSaveComplete(char)
                                                }
                                            }
                                            else -> {
                                                // For edit mode, save the current character
                                                currentCharacter?.let { char ->
                                                    viewModel.updateCharacter(char)
                                                    onSaveComplete(char)
                                                }
                                            }
                                        }
                                    } finally {
                                        isSaving = false
                                    }
                                }
                            },
                            enabled = !isSaving && currentCharacter != null
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Save, null)
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(if (mode == CharacterScreenMode.CREATE) "Create Character" else "Save Changes")
                        }
                    }
                }
            }
        }
    }

    // Character Development Modal
    if (showCharacterDevelopment) {
        ModalBottomSheet(
            onDismissRequest = { showCharacterDevelopment = false },
            containerColor = rememberAppTheme().colors.background
        ) {
            EnhancedCharacterDevelopmentScreen(
                characterName = currentCharacter?.name ?: "New Character",
                onNavigateBack = { showCharacterDevelopment = false },
                onSaveCharacter = { developmentData ->
                    // TODO: Save character development data to character profile
                    // Could extend CharacterProfile to include these fields
                    // or store as metadata
                    showCharacterDevelopment = false
                }
            )
        }
    }
}

// Helper function to create default character for CREATE mode
private fun createDefaultCharacter(projectId: String): CharacterProfile {
    val currentTime = kotlinx.datetime.Clock.System.now()
    return CharacterProfile(
        id = "new_${currentTime.toEpochMilliseconds()}",
        projectId = projectId,
        name = "New Character",
        role = CharacterRole.SUPPORTING,
        archetype = "The Hero",
        personality = PersonalityProfile(
            archetype = "The Hero",
            traits = emptyList(),
            motivations = emptyList(),
            fears = emptyList(),
            backstory = "",
            aiInsights = "",
            oceanScores = OceanPersonality(
                openness = 0.5f,
                conscientiousness = 0.5f,
                extraversion = 0.5f,
                agreeableness = 0.5f,
                neuroticism = 0.5f
            )
        ),
        relationships = emptyList(),
        screenTime = 0f,
        dialogueCount = 0,
        age = 30,
        height = "5'9\"",
        gender = Gender.UNSPECIFIED,
        build = "Average",
        hairColor = "Brown",
        eyeColor = "Brown",
        distinctiveFeatures = emptyList(),
        physicalAttributes = PhysicalAttributes(
            height = "5'9\"",
            build = "Average",
            hairColor = "Brown",
            eyeColor = "Brown",
            distinctiveFeatures = emptyList()
        ),
        createdAt = currentTime,
        updatedAt = currentTime,
        metadata = mapOf(
            "characterArc" to "Hero's Journey",
            "avatarStyle" to "Realistic",
            "gender" to "Not specified",
            "voiceType" to "Adult",
            "accent" to "Neutral",
            "voicePitch" to "0.5",
            "voiceSpeed" to "0.5"
        )
    )
}

// Additional helper component for showing arc progression in character cards
@Composable
fun CharacterArcProgressIndicator(
    character: CharacterProfile,
    modifier: Modifier = Modifier
) {
    val arcType = CharacterArcType.fromString(
        character.metadata["characterArc"] ?: "Hero's Journey"
    )
    val progress = character.screenTime.coerceIn(0f, 1f)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Arc type icon
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(arcType.color)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Progress bar
        Box(
            modifier = Modifier
                .weight(1f)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(arcType.color.copy(alpha = 0.2f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .background(arcType.color)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Progress percentage
        Text(
            "${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            color = arcType.color
        )
    }
}

// AvatarStudioDialog.kt - Integrated avatar customization dialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvatarStudioDialog(
    avatar: Avatar,
    onDismiss: () -> Unit,
    onSave: (Avatar) -> Unit
) {
    var customizedAvatar by remember { mutableStateOf(avatar) }
    var selectedCustomizationTab by remember { mutableStateOf(0) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                TopAppBar(
                    title = { Text("Customize Avatar") },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, "Close")
                        }
                    },
                    actions = {
                        Button(
                            onClick = { onSave(customizedAvatar) },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("Save")
                        }
                    }
                )

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Avatar Preview
                    Card(
                        modifier = Modifier
                            .weight(0.4f)
                            .fillMaxHeight(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // 3D Avatar Preview would go here
                            Box(
                                modifier = Modifier
                                    .size(200.dp)
                                    .clip(CircleShape)
                                    .background(Color.Gray.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(100.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(Modifier.height(16.dp))

                            Text(
                                customizedAvatar.name,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Text(
                                customizedAvatar.style.toString(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(Modifier.width(16.dp))

                    // Customization Options
                    Column(
                        modifier = Modifier
                            .weight(0.6f)
                            .fillMaxHeight()
                    ) {
                        // Customization Tabs
                        TabRow(selectedTabIndex = selectedCustomizationTab) {
                            Tab(
                                selected = selectedCustomizationTab == 0,
                                onClick = { selectedCustomizationTab = 0 },
                                text = { Text("Face") }
                            )
                            Tab(
                                selected = selectedCustomizationTab == 1,
                                onClick = { selectedCustomizationTab = 1 },
                                text = { Text("Body") }
                            )
                            Tab(
                                selected = selectedCustomizationTab == 2,
                                onClick = { selectedCustomizationTab = 2 },
                                text = { Text("Style") }
                            )
                            Tab(
                                selected = selectedCustomizationTab == 3,
                                onClick = { selectedCustomizationTab = 3 },
                                text = { Text("Clothing") }
                            )
                        }

                        // Tab Content
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            when (selectedCustomizationTab) {
                                0 -> {
                                    // Face Customization
                                    item {
                                        CustomizationSection(
                                            title = "Face Shape",
                                            options = listOf("Round", "Oval", "Square", "Heart"),
                                            selectedOption = "Oval",
                                            onOptionSelect = { /* Update */ }
                                        )
                                    }
                                    item {
                                        CustomizationSection(
                                            title = "Eyes",
                                            options = listOf("Almond", "Round", "Hooded", "Monolid"),
                                            selectedOption = "Almond",
                                            onOptionSelect = { /* Update */ }
                                        )
                                    }
                                    item {
                                        CustomizationSection(
                                            title = "Nose",
                                            options = listOf("Button", "Roman", "Aquiline", "Snub"),
                                            selectedOption = "Button",
                                            onOptionSelect = { /* Update */ }
                                        )
                                    }
                                }
                                1 -> {
                                    // Body Customization
                                    item {
                                        CustomizationSection(
                                            title = "Body Type",
                                            options = listOf("Slim", "Athletic", "Average", "Curvy"),
                                            selectedOption = "Average",
                                            onOptionSelect = { /* Update */ }
                                        )
                                    }
                                    item {
                                        SliderCustomization(
                                            title = "Height",
                                            value = 0.5f,
                                            onValueChange = { /* Update */ }
                                        )
                                    }
                                }
                                2 -> {
                                    // Style Customization
                                    item {
                                        CustomizationSection(
                                            title = "Art Style",
                                            options = AvatarStyle.values().map { it.toString() },
                                            selectedOption = customizedAvatar.style.toString(),
                                            onOptionSelect = { style ->
                                                customizedAvatar = customizedAvatar.copy(
                                                    style = AvatarStyle.values().first { it.toString() == style }
                                                )
                                            }
                                        )
                                    }
                                    item {
                                        ColorCustomization(
                                            title = "Skin Tone",
                                            selectedColor = Color(0xFFFFDBCE),
                                            onColorSelect = { /* Update */ }
                                        )
                                    }
                                }
                                3 -> {
                                    // Clothing Customization
                                    item {
                                        CustomizationSection(
                                            title = "Top",
                                            options = listOf("T-Shirt", "Shirt", "Blouse", "Hoodie"),
                                            selectedOption = "T-Shirt",
                                            onOptionSelect = { /* Update */ }
                                        )
                                    }
                                    item {
                                        CustomizationSection(
                                            title = "Bottom",
                                            options = listOf("Jeans", "Shorts", "Skirt", "Pants"),
                                            selectedOption = "Jeans",
                                            onOptionSelect = { /* Update */ }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomizationSection(
    title: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelect: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(options) { option ->
                FilterChip(
                    selected = option == selectedOption,
                    onClick = { onOptionSelect(option) },
                    label = { Text(option) }
                )
            }
        }
    }
}

@Composable
private fun SliderCustomization(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ColorCustomization(
    title: String,
    selectedColor: Color,
    onColorSelect: (Color) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val skinTones = listOf(
                Color(0xFFFFDBCE), // Light
                Color(0xFFF1C27D), // Medium Light
                Color(0xFFE0AC69), // Medium
                Color(0xFFC68642), // Medium Dark
                Color(0xFF8D5524), // Dark
                Color(0xFF6B4423)  // Very Dark
            )
            items(skinTones) { color ->
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            width = if (color == selectedColor) 2.dp else 0.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                        .clickable { onColorSelect(color) }
                )
            }
        }
    }
}