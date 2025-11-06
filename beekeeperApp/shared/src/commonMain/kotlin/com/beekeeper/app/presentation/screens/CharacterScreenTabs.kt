// This is the COMPLETE CharacterScreenTabs.kt file
// Replace your entire CharacterScreenTabs.kt file with this:

package com.beekeeper.app.presentation.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
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
import com.beekeeper.app.domain.model.*

/**
 * Features Tab - Physical attributes, OCEAN personality, avatar, and voice configuration
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeaturesTab(
    character: CharacterProfile?,
    isEditable: Boolean,
    isGenerating: Boolean,
    onUpdateCharacter: (CharacterProfile) -> Unit,
    onGenerateAvatar: () -> Unit,
    onGenerateVoice: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Physical Attributes Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Physical Attributes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = character?.age?.toString() ?: "",
                            onValueChange = { value ->
                                if (isEditable) {
                                    value.toIntOrNull()?.let { age ->
                                        character?.let { onUpdateCharacter(it.copy(age = age)) }
                                    }
                                }
                            },
                            label = { Text("Age") },
                            enabled = isEditable,
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = character?.height ?: "",
                            onValueChange = { value ->
                                if (isEditable) {
                                    character?.let { onUpdateCharacter(it.copy(height = value)) }
                                }
                            },
                            label = { Text("Height") },
                            enabled = isEditable,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = character?.build ?: "",
                            onValueChange = { value ->
                                if (isEditable) {
                                    character?.let { onUpdateCharacter(it.copy(build = value)) }
                                }
                            },
                            label = { Text("Build") },
                            enabled = isEditable,
                            modifier = Modifier.weight(1f)
                        )

                        // Gender dropdown
                        var genderExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = genderExpanded,
                            onExpandedChange = { if (isEditable) genderExpanded = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = character?.gender?.name ?: Gender.UNSPECIFIED.name,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Gender") },
                                enabled = isEditable,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                                modifier = Modifier.menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = genderExpanded,
                                onDismissRequest = { genderExpanded = false }
                            ) {
                                Gender.values().forEach { gender ->
                                    DropdownMenuItem(
                                        text = { Text(gender.name) },
                                        onClick = {
                                            character?.let { onUpdateCharacter(it.copy(gender = gender)) }
                                            genderExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = character?.hairColor ?: "",
                            onValueChange = { value ->
                                if (isEditable) {
                                    character?.let { onUpdateCharacter(it.copy(hairColor = value)) }
                                }
                            },
                            label = { Text("Hair Color") },
                            enabled = isEditable,
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = character?.eyeColor ?: "",
                            onValueChange = { value ->
                                if (isEditable) {
                                    character?.let { onUpdateCharacter(it.copy(eyeColor = value)) }
                                }
                            },
                            label = { Text("Eye Color") },
                            enabled = isEditable,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Distinctive Features
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Distinctive Features",
                                style = MaterialTheme.typography.labelLarge
                            )
                            if (isEditable) {
                                TextButton(onClick = {
                                    character?.let {
                                        val updatedFeatures = it.distinctiveFeatures + "New Feature"
                                        onUpdateCharacter(it.copy(distinctiveFeatures = updatedFeatures))
                                    }
                                }) {
                                    Icon(Icons.Default.Add, null, Modifier.size(16.dp))
                                    Text("Add")
                                }
                            }
                        }

                        character?.distinctiveFeatures?.forEach { feature ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("• $feature", modifier = Modifier.weight(1f))
                                if (isEditable) {
                                    IconButton(
                                        onClick = {
                                            character?.let {
                                                val updatedFeatures = it.distinctiveFeatures - feature
                                                onUpdateCharacter(it.copy(distinctiveFeatures = updatedFeatures))
                                            }
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Close, null)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // OCEAN Personality Scores Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "OCEAN Personality Model",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Get the OCEAN scores, providing default if null
                    val oceanScores = character?.personality?.oceanScores ?: OceanPersonality(
                        openness = 0.5f,
                        conscientiousness = 0.5f,
                        extraversion = 0.5f,
                        agreeableness = 0.5f,
                        neuroticism = 0.5f
                    )

                    // Openness
                    PersonalitySlider(
                        label = "Openness",
                        description = "Imagination, curiosity, openness to new experiences",
                        value = oceanScores.openness,
                        onValueChange = { value ->
                            if (isEditable && character != null) {
                                val updatedOcean = oceanScores.copy(openness = value)
                                val updatedPersonality = character.personality.copy(oceanScores = updatedOcean)
                                onUpdateCharacter(character.copy(personality = updatedPersonality))
                            }
                        },
                        enabled = isEditable
                    )

                    // Conscientiousness
                    PersonalitySlider(
                        label = "Conscientiousness",
                        description = "Organization, dependability, self-discipline",
                        value = oceanScores.conscientiousness,
                        onValueChange = { value ->
                            if (isEditable && character != null) {
                                val updatedOcean = oceanScores.copy(conscientiousness = value)
                                val updatedPersonality = character.personality.copy(oceanScores = updatedOcean)
                                onUpdateCharacter(character.copy(personality = updatedPersonality))
                            }
                        },
                        enabled = isEditable
                    )

                    // Extraversion
                    PersonalitySlider(
                        label = "Extraversion",
                        description = "Sociability, assertiveness, positive emotions",
                        value = oceanScores.extraversion,
                        onValueChange = { value ->
                            if (isEditable && character != null) {
                                val updatedOcean = oceanScores.copy(extraversion = value)
                                val updatedPersonality = character.personality.copy(oceanScores = updatedOcean)
                                onUpdateCharacter(character.copy(personality = updatedPersonality))
                            }
                        },
                        enabled = isEditable
                    )

                    // Agreeableness
                    PersonalitySlider(
                        label = "Agreeableness",
                        description = "Trust, cooperation, compassion",
                        value = oceanScores.agreeableness,
                        onValueChange = { value ->
                            if (isEditable && character != null) {
                                val updatedOcean = oceanScores.copy(agreeableness = value)
                                val updatedPersonality = character.personality.copy(oceanScores = updatedOcean)
                                onUpdateCharacter(character.copy(personality = updatedPersonality))
                            }
                        },
                        enabled = isEditable
                    )

                    // Neuroticism
                    PersonalitySlider(
                        label = "Neuroticism",
                        description = "Emotional instability, anxiety, moodiness",
                        value = oceanScores.neuroticism,
                        onValueChange = { value ->
                            if (isEditable && character != null) {
                                val updatedOcean = oceanScores.copy(neuroticism = value)
                                val updatedPersonality = character.personality.copy(oceanScores = updatedOcean)
                                onUpdateCharacter(character.copy(personality = updatedPersonality))
                            }
                        },
                        enabled = isEditable
                    )
                }
            }
        }

        // Avatar & Voice Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Avatar & Voice",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Avatar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (character?.assignedAvatarId != null) {
                            Card(
                                modifier = Modifier.size(100.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                )
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                if (character?.assignedAvatarId != null) "Avatar Assigned" else "No Avatar",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            if (isEditable) {
                                Button(
                                    onClick = onGenerateAvatar,
                                    enabled = !isGenerating
                                ) {
                                    if (isGenerating) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text("Generate Avatar")
                                    }
                                }
                            }
                        }
                    }

                    // Voice
                    Column {
                        Text(
                            "Voice Profile",
                            style = MaterialTheme.typography.labelLarge
                        )

                        val voiceProfile = character?.voiceProfile
                        if (voiceProfile != null) {
                            Text("Model: ${voiceProfile.voiceModelType}", style = MaterialTheme.typography.bodyMedium)
                            Text("Accent: ${voiceProfile.accent}", style = MaterialTheme.typography.bodyMedium)
                            Text("Style: ${voiceProfile.speakingStyle}", style = MaterialTheme.typography.bodyMedium)
                        } else {
                            Text("No voice profile assigned", style = MaterialTheme.typography.bodyMedium)
                        }

                        if (isEditable) {
                            Button(
                                onClick = onGenerateVoice,
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Text("Generate Voice")
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Profile Tab - Personality, traits, and background
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTab(
    character: CharacterProfile?,
    isEditable: Boolean,
    onUpdateCharacter: (CharacterProfile) -> Unit,
    onGenerateBackstory: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Basic Information
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Basic Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = character?.name ?: "",
                        onValueChange = { value ->
                            if (isEditable) {
                                character?.let { onUpdateCharacter(it.copy(name = value)) }
                            }
                        },
                        label = { Text("Name") },
                        enabled = isEditable,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = character?.description ?: "",
                        onValueChange = { value ->
                            if (isEditable) {
                                character?.let { onUpdateCharacter(it.copy(description = value)) }
                            }
                        },
                        label = { Text("Description") },
                        enabled = isEditable,
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )

                    OutlinedTextField(
                        value = character?.primaryIconMediaId ?: "",
                        onValueChange = { value ->
                            if (isEditable) {
                                character?.let { onUpdateCharacter(it.copy(primaryIconMediaId = value)) }
                            }
                        },
                        label = { Text("Primary Icon Media ID") },
                        enabled = isEditable,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Media ID for character's primary icon...") }
                    )

                    // Role dropdown
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        var roleExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = roleExpanded,
                            onExpandedChange = { if (isEditable) roleExpanded = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = character?.role?.toString() ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Role") },
                                enabled = isEditable,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleExpanded) },
                                modifier = Modifier.menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = roleExpanded,
                                onDismissRequest = { roleExpanded = false }
                            ) {
                                CharacterRole.values().forEach { role ->
                                    DropdownMenuItem(
                                        text = { Text(role.name) },
                                        onClick = {
                                            character?.let { onUpdateCharacter(it.copy(role = role)) }
                                            roleExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = character?.archetype ?: "",
                            onValueChange = { value ->
                                if (isEditable) {
                                    character?.let { onUpdateCharacter(it.copy(archetype = value)) }
                                }
                            },
                            label = { Text("Archetype") },
                            enabled = isEditable,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Personality Traits
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Personality Traits",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (isEditable) {
                            TextButton(onClick = {
                                character?.let {
                                    val traits = it.personality.traits
                                    val newTrait = PersonalityTrait("New Trait", 0.5f)
                                    val updatedPersonality = it.personality.copy(
                                        traits = traits + newTrait
                                    )
                                    onUpdateCharacter(it.copy(personality = updatedPersonality))
                                }
                            }) {
                                Icon(Icons.Default.Add, null, Modifier.size(16.dp))
                                Text("Add Trait")
                            }
                        }
                    }

                    character?.personality?.traits?.forEach { trait ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(trait.name, fontWeight = FontWeight.Medium)
                                    LinearProgressIndicator(
                                        progress = trait.strength,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 4.dp)
                                    )
                                }
                                if (isEditable) {
                                    IconButton(
                                        onClick = {
                                            character?.let {
                                                val updatedTraits = it.personality.traits.filter { t -> t.name != trait.name }
                                                val updatedPersonality = it.personality.copy(traits = updatedTraits)
                                                onUpdateCharacter(it.copy(personality = updatedPersonality))
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.Delete, "Remove")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Backstory
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Backstory",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (isEditable) {
                            TextButton(onClick = onGenerateBackstory) {
                                Icon(Icons.Default.AutoAwesome, null, Modifier.size(16.dp))
                                Text("Generate")
                            }
                        }
                    }

                    OutlinedTextField(
                        value = character?.personality?.backstory ?: "",
                        onValueChange = { value ->
                            if (isEditable) {
                                character?.let {
                                    it.personality?.let { personality ->
                                        onUpdateCharacter(it.copy(
                                            personality = personality.copy(backstory = value)
                                        ))
                                    }
                                }
                            }
                        },
                        enabled = isEditable,
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 5,
                        placeholder = { Text("Enter character backstory...") }
                    )
                }
            }
        }

        // Motivations & Fears
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Motivations
                    EditableListSection(
                        title = "Motivations",
                        items = character?.personality?.motivations ?: emptyList(),
                        isEditable = isEditable,
                        onAdd = {
                            character?.let {
                                val motivations = it.personality.motivations
                                val updatedPersonality = it.personality.copy(
                                    motivations = motivations + "New Motivation"
                                )
                                onUpdateCharacter(it.copy(personality = updatedPersonality))
                            }
                        },
                        onRemove = { motivation ->
                            character?.let {
                                val motivations = it.personality.motivations
                                val updatedPersonality = it.personality.copy(
                                    motivations = motivations - motivation
                                )
                                onUpdateCharacter(it.copy(personality = updatedPersonality))
                            }
                        }
                    )

                    Divider()

                    // Fears
                    EditableListSection(
                        title = "Fears",
                        items = character?.personality?.fears ?: emptyList(),
                        isEditable = isEditable,
                        onAdd = {
                            character?.let {
                                val fears = it.personality.fears
                                val updatedPersonality = it.personality.copy(
                                    fears = fears + "New Fear"
                                )
                                onUpdateCharacter(it.copy(personality = updatedPersonality))
                            }
                        },
                        onRemove = { fear ->
                            character?.let {
                                val fears = it.personality.fears
                                val updatedPersonality = it.personality.copy(
                                    fears = fears - fear
                                )
                                onUpdateCharacter(it.copy(personality = updatedPersonality))
                            }
                        }
                    )
                }
            }
        }

        // Skills
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    EditableListSection(
                        title = "Skills",
                        items = character?.skills ?: emptyList(),
                        isEditable = isEditable,
                        onAdd = {
                            character?.let {
                                val skills = it.skills
                                onUpdateCharacter(it.copy(skills = skills + "New Skill"))
                            }
                        },
                        onRemove = { skill ->
                            character?.let {
                                val skills = it.skills
                                onUpdateCharacter(it.copy(skills = skills - skill))
                            }
                        }
                    )
                }
            }
        }

        // AI Generation Prompts
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "AI Generation Prompts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = character?.visualPrompt ?: "",
                        onValueChange = { value ->
                            if (isEditable) {
                                character?.let { onUpdateCharacter(it.copy(visualPrompt = value)) }
                            }
                        },
                        label = { Text("Visual Prompt (Midjourney, Stable Diffusion)") },
                        enabled = isEditable,
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        placeholder = { Text("Detailed visual description for image generation...") }
                    )

                    OutlinedTextField(
                        value = character?.voicePrompt ?: "",
                        onValueChange = { value ->
                            if (isEditable) {
                                character?.let { onUpdateCharacter(it.copy(voicePrompt = value)) }
                            }
                        },
                        label = { Text("Voice Prompt (ElevenLabs)") },
                        enabled = isEditable,
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        placeholder = { Text("Voice characteristics for voice generation...") }
                    )

                    OutlinedTextField(
                        value = character?.ambientPrompt ?: "",
                        onValueChange = { value ->
                            if (isEditable) {
                                character?.let { onUpdateCharacter(it.copy(ambientPrompt = value)) }
                            }
                        },
                        label = { Text("Ambient/Theme Prompt") },
                        enabled = isEditable,
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        placeholder = { Text("Character theme music or ambient audio description...") }
                    )
                }
            }
        }
    }
}

/**
 * Relationships Tab - Character connections
 */
@Composable
fun RelationshipsTab(
    character: CharacterProfile?,
    projectId: String,
    isEditable: Boolean,
    onAddRelationship: (String, CharacterRelationship) -> Unit,
    onUpdateRelationship: (CharacterRelationship) -> Unit,
    onRemoveRelationship: (String) -> Unit,
    onNavigateToRelationship: (String, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (isEditable) {
            item {
                Button(
                    onClick = { /* TODO: Show add relationship dialog */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add Relationship")
                }
            }
        }

        val relationships = character?.relationships ?: emptyList()

        if (relationships.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Group,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No relationships defined yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(
                items = relationships,
                key = { it.targetCharacterId }
            ) { relationship ->
                RelationshipCard(
                    relationship = relationship,
                    isEditable = isEditable,
                    onEdit = { onUpdateRelationship(relationship) },
                    onRemove = { onRemoveRelationship(relationship.targetCharacterId) },
                    onClick = {
                        character?.let {
                            onNavigateToRelationship(it.id, relationship.targetCharacterId)
                        }
                    }
                )
            }
        }
    }
}

/**
 * Gallery Tab - Generated avatars and voice samples
 */
@Composable
fun GalleryTab(
    character: CharacterProfile?,
    generatedAvatars: List<Avatar>,
    voiceSamples: List<VoiceProfile>,
    isEditable: Boolean,
    onSelectAvatar: (String) -> Unit,
    onRegenerateAvatar: () -> Unit,
    onNavigateToAvatarStudio: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Avatar Gallery
        item {
            Text(
                "Avatar Gallery",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (generatedAvatars.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.ImageNotSupported,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "No avatars generated yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }
            }
        }

        // Voice Samples
        item {
            Text(
                "Voice Samples",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        if (voiceSamples.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.MicOff,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "No voice samples generated yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

// ============== HELPER COMPONENTS ==============

/**
 * Component for personality sliders with descriptions
 */
@Composable
fun PersonalitySlider(
    label: String,
    description: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    enabled: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                "${(value * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..1f,
            enabled = enabled,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )

        // Visual indicator
        LinearProgressIndicator(
            progress = value,
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = when {
                value < 0.3f -> MaterialTheme.colorScheme.error
                value < 0.7f -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.tertiary
            },
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))
    }
}

/**
 * Component for editable lists (motivations, fears, etc.)
 */
@Composable
fun EditableListSection(
    title: String,
    items: List<String>,
    isEditable: Boolean,
    onAdd: () -> Unit,
    onRemove: (String) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, style = MaterialTheme.typography.labelLarge)
            if (isEditable) {
                TextButton(onClick = onAdd) {
                    Icon(Icons.Default.Add, null, Modifier.size(16.dp))
                    Text("Add")
                }
            }
        }

        items.forEach { item ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("• $item", modifier = Modifier.weight(1f))
                if (isEditable) {
                    IconButton(
                        onClick = { onRemove(item) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Close, null)
                    }
                }
            }
        }
    }
}

/**
 * Card for displaying a relationship
 */
@Composable
fun RelationshipCard(
    relationship: CharacterRelationship,
    isEditable: Boolean,
    onEdit: () -> Unit,
    onRemove: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    relationship.targetCharacterName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    relationship.relationshipType.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    relationship.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isEditable) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, "Edit")
                }
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Delete, "Remove")
                }
            }
        }
    }
}