// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/presentation/screens/EnhancedVideoEditorScreen.kt
package com.beekeeper.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.beekeeper.app.domain.model.ContentCreationState
import com.beekeeper.app.domain.model.ContentCreationStep
import com.beekeeper.app.presentation.viewmodels.ContentCreationViewModel
import kotlinx.datetime.Clock

// Avatar Template Data Classes
data class AvatarTemplate(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val emoji: String
)

data class Character(
    val id: String,
    val name: String,
    val description: String,
    val avatar: String,
    val voice: String,
    val template: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedVideoEditorScreen(
    viewModel: ContentCreationViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToContent: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val availableProviders by viewModel.availableProviders.collectAsState()
    val currentProvider by viewModel.currentProvider.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    var showAvatarStudio by remember { mutableStateOf(false) }
    var selectedTemplate by remember { mutableStateOf<AvatarTemplate?>(null) }

    // Sample Avatar Templates
    val avatarTemplates = remember {
        listOf(
            AvatarTemplate("business-woman", "Business Woman", "Professional", "Professional with navy suit, confident demeanor", "👩‍💼"),
            AvatarTemplate("tech-expert", "Tech Expert", "Professional", "Casual professional with glasses, tech-savvy", "👨‍💻"),
            AvatarTemplate("educator", "Educator", "Educational", "Warm teacher with patient, encouraging presence", "👩‍🏫"),
            AvatarTemplate("medical-expert", "Medical Expert", "Healthcare", "Healthcare professional in white coat", "👩‍⚕️"),
            AvatarTemplate("creative-artist", "Creative Artist", "Creative", "Artistic individual with expressive energy", "🎨")
        )
    }

    // Sample Characters
    var characters by remember {
        mutableStateOf(listOf(
            Character(
                id = "1",
                name = "Jackie Johnson",
                description = "Professional business consultant with expertise in strategy",
                avatar = "👩‍💼",
                voice = "Professional Female",
                template = "business"
            )
        ))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Content Studio") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAvatarStudio = true }) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Avatar Studio")
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
            // Progress indicator
            if (state.isProcessing) {
                LinearProgressIndicator(
                    progress = state.progress,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Tab row
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Script") },
                    icon = { Icon(Icons.Default.Description, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Characters") },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Production") },
                    icon = { Icon(Icons.Default.Movie, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("Settings") },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) }
                )
            }

            // Tab content
            when (selectedTab) {
                0 -> ScriptGenerationTab(
                    state = state,
                    onGenerateScript = { prompt ->
                        viewModel.generateScript(prompt, "social_media")
                    }
                )
                1 -> CharacterCreationTab(
                    characters = characters,
                    avatarTemplates = avatarTemplates,
                    selectedTemplate = selectedTemplate,
                    onTemplateSelected = { selectedTemplate = it },
                    onShowAvatarStudio = { showAvatarStudio = true },
                    onCharacterCreated = { character ->
                        characters = characters + character
                    }
                )
                2 -> ProductionTab(
                    state = state,
                    onGenerateStoryboard = { viewModel.generateStoryboard() },
                    onGenerateScenes = { viewModel.generateScenes() },
                    onGenerateAudio = { viewModel.generateAudio() },
                    onAssembleVideo = { viewModel.assembleVideo() },
                    onExportVideo = { viewModel.exportVideo("MP4", "1080p") }
                )
                3 -> SettingsTab(
                    availableProviders = availableProviders,
                    currentProvider = currentProvider,
                    onSwitchProvider = { provider ->
                        viewModel.switchAIProvider(provider)
                    },
                    onReset = { viewModel.resetCreation() }
                )
            }
        }
    }

    // Avatar Studio Dialog
    if (showAvatarStudio) {
        AvatarStudioDialog(
            templates = avatarTemplates,
            onTemplateSelected = { template ->
                selectedTemplate = template
                showAvatarStudio = false
            },
            onDismiss = { showAvatarStudio = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CharacterCreationTab(
    characters: List<Character>,
    avatarTemplates: List<AvatarTemplate>,
    selectedTemplate: AvatarTemplate?,
    onTemplateSelected: (AvatarTemplate) -> Unit,
    onShowAvatarStudio: () -> Unit,
    onCharacterCreated: (Character) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header with Avatar Studio button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Character Creation",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            FilledTonalButton(
                onClick = onShowAvatarStudio,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Avatar Studio")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Templates Section
        Text(
            text = "Quick Templates",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(avatarTemplates.take(4)) { template ->
                QuickTemplateCard(
                    template = template,
                    isSelected = selectedTemplate?.id == template.id,
                    onClick = { onTemplateSelected(template) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Character Creation Form
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Create New Character",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Left Column - Form Fields
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        var characterName by remember { mutableStateOf(selectedTemplate?.name ?: "") }
                        var characterDescription by remember { mutableStateOf(selectedTemplate?.description ?: "") }
                        var voiceStyle by remember { mutableStateOf("Professional Female") }

                        OutlinedTextField(
                            value = characterName,
                            onValueChange = { characterName = it },
                            label = { Text("Character Name") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.outline)
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = characterDescription,
                            onValueChange = { characterDescription = it },
                            label = { Text("Description") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 4,
                            trailingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.outline)
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        ExposedDropdownMenuBox(
                            expanded = false,
                            onExpandedChange = { }
                        ) {
                            OutlinedTextField(
                                value = voiceStyle,
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("Voice Style") },
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) }
                            )
                        }
                    }

                    // Right Column - Avatar Preview
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(120.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(
                                    brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.secondary
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = selectedTemplate?.emoji ?: "👤",
                                style = MaterialTheme.typography.headlineLarge
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { /* Edit avatar */ },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = {
                            val newCharacter = Character(
                                id = Clock.System.now().toString(),
                                name = "New Character",
                                description = "Generated character",
                                avatar = selectedTemplate?.emoji ?: "👤",
                                voice = "Professional Female",
                                template = selectedTemplate?.id ?: "default"
                            )
                            onCharacterCreated(newCharacter)
                        }
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Generate Character")
                    }

                    OutlinedButton(onClick = { /* Save draft */ }) {
                        Text("Save Draft")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Existing Characters
        if (characters.isNotEmpty()) {
            Text(
                text = "Your Characters",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            characters.forEach { character ->
                CharacterCard(
                    character = character,
                    onUseCharacter = { /* Use character in production */ },
                    onEditCharacter = { /* Edit character */ }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun QuickTemplateCard(
    template: AvatarTemplate,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) CardDefaults.outlinedCardBorder() else null
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = template.emoji,
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = template.name,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun CharacterCard(
    character: Character,
    onUseCharacter: () -> Unit,
    onEditCharacter: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = character.avatar,
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Character Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = character.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = character.voice,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = character.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }

            // Action Buttons
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                FilledTonalButton(
                    onClick = onUseCharacter,
                    modifier = Modifier.width(120.dp)
                ) {
                    Text("Use Character", style = MaterialTheme.typography.bodySmall)
                }

                OutlinedButton(
                    onClick = onEditCharacter,
                    modifier = Modifier.width(120.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun AvatarStudioDialog(
    templates: List<AvatarTemplate>,
    onTemplateSelected: (AvatarTemplate) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(600.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Avatar Studio",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Divider()

                // Content
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            text = "Choose a Template",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Category Pills
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(listOf("Professional", "Educational", "Healthcare", "Creative")) { category ->
                                FilterChip(
                                    onClick = { },
                                    label = { Text(category) },
                                    selected = false
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    items(templates) { template ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onTemplateSelected(template) },
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(
                                            brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                                colors = listOf(
                                                    MaterialTheme.colorScheme.primary,
                                                    MaterialTheme.colorScheme.secondary
                                                )
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = template.emoji,
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = template.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.secondaryContainer
                                        ) {
                                            Text(
                                                text = template.category,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    }
                                    Text(
                                        text = template.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
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

// Keep existing tabs and functions from the original file
@Composable
private fun ScriptGenerationTab(
    state: ContentCreationState,
    onGenerateScript: (String) -> Unit
) {
    var prompt by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Script Generation",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = prompt,
            onValueChange = { prompt = it },
            label = { Text("Script Prompt") },
            placeholder = { Text("Describe the content you want to create...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 6
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onGenerateScript(prompt) },
            enabled = prompt.isNotBlank() && !state.isProcessing,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.isProcessing && state.currentStep == ContentCreationStep.SCRIPT_GENERATION) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("Generate Script")
        }

        if (state.script.isNotBlank()) {
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Generated Script",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = state.script,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductionTab(
    state: ContentCreationState,
    onGenerateStoryboard: () -> Unit,
    onGenerateScenes: () -> Unit,
    onGenerateAudio: () -> Unit,
    onAssembleVideo: () -> Unit,
    onExportVideo: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Production Pipeline",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Production steps would go here - keep existing implementation
        // This is simplified for the example
        Button(
            onClick = onGenerateStoryboard,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isProcessing
        ) {
            Text("Generate Storyboard")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onGenerateScenes,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isProcessing && state.storyboard.isNotEmpty()
        ) {
            Text("Generate Scenes")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onGenerateAudio,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isProcessing && state.scenes.isNotEmpty()
        ) {
            Text("Generate Audio")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onAssembleVideo,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isProcessing && state.audioTracks.isNotEmpty()
        ) {
            Text("Assemble Video")
        }

        if (state.finalVideo != null) {
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onExportVideo,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isProcessing
            ) {
                Text("Export Video")
            }
        }
    }
}

@Composable
private fun SettingsTab(
    availableProviders: List<String>,
    currentProvider: String?,
    onSwitchProvider: (String) -> Unit,
    onReset: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "AI Provider",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                availableProviders.forEach { provider ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSwitchProvider(provider) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentProvider == provider,
                            onClick = { onSwitchProvider(provider) }
                        )
                        Text(
                            text = provider,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onReset,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("Reset Creation")
        }
    }
}