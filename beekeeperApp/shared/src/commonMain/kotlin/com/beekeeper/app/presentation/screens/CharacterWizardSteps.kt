// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/presentation/screens/CharacterWizardSteps.kt
@file:OptIn(ExperimentalMaterial3Api::class)

package com.beekeeper.app.presentation.screens

import androidx.compose.foundation.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beekeeper.app.domain.model.*
import com.beekeeper.app.presentation.theme.rememberAppTheme

@Composable
fun AIAnalysisStep(
    creationMethod: CreationMethod,
    uploadedImageUrl: String?,
    description: String,
    isAnalyzing: Boolean,
    aiSuggestions: Map<String, String>,
    onImageUpload: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onAnalyze: () -> Unit,
    onNext: () -> Unit
) {
    val theme = rememberAppTheme()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                if (creationMethod == CreationMethod.FROM_IMAGE)
                    "Upload Character Image"
                else
                    "Describe Your Character",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = theme.colors.textPrimary
            )
        }

        item {
            if (creationMethod == CreationMethod.FROM_IMAGE) {
                // Image Upload Section
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .clickable { /* TODO: Open image picker */ },
                    colors = CardDefaults.cardColors(
                        containerColor = theme.colors.surface
                    ),
                    border = BorderStroke(2.dp, theme.colors.primary.copy(alpha = 0.3f))
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (uploadedImageUrl != null) {
                            // TODO: Display image
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = theme.colors.success
                            )
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Upload,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = theme.colors.primary
                                )
                                Text(
                                    "Tap to upload image",
                                    fontSize = 16.sp,
                                    color = theme.colors.textSecondary
                                )
                            }
                        }
                    }
                }
            } else {
                // Description Input
                OutlinedTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    label = { Text("Character Description") },
                    placeholder = {
                        Text("Describe your character's appearance, personality, and background...")
                    },
                    minLines = 8,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = theme.colors.primary,
                        unfocusedBorderColor = theme.colors.textSecondary.copy(alpha = 0.3f)
                    )
                )
            }
        }

        item {
            Button(
                onClick = onAnalyze,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isAnalyzing && (
                    uploadedImageUrl != null || description.isNotBlank()
                ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = theme.colors.primary
                )
            ) {
                if (isAnalyzing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = theme.colors.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Analyzing...")
                } else {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Analyze with AI")
                }
            }
        }

        // AI Suggestions
        if (aiSuggestions.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "AI Suggestions",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.colors.textPrimary
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = theme.colors.primary.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        aiSuggestions.forEach { (key, value) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    key.replaceFirstChar { it.uppercase() },
                                    fontWeight = FontWeight.Medium,
                                    color = theme.colors.textPrimary
                                )
                                Text(
                                    value,
                                    color = theme.colors.textSecondary
                                )
                            }
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = onNext,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = theme.colors.success
                    )
                ) {
                    Text("Use Suggestions")
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null)
                }
            }
        }
    }
}

@Composable
fun CoreDetailsStep(
    name: String,
    role: CharacterRole,
    onNameChange: (String) -> Unit,
    onRoleChange: (CharacterRole) -> Unit,
    onNext: () -> Unit
) {
    val theme = rememberAppTheme()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Core Details",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = theme.colors.textPrimary
            )
        }

        item {
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Character Name") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = theme.colors.primary,
                    unfocusedBorderColor = theme.colors.textSecondary.copy(alpha = 0.3f)
                )
            )
        }

        item {
            var roleExpanded by remember { mutableStateOf(false) }

            Text(
                "Character Role",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = theme.colors.textPrimary
            )

            ExposedDropdownMenuBox(
                expanded = roleExpanded,
                onExpandedChange = { roleExpanded = it }
            ) {
                OutlinedTextField(
                    value = role.name.replace("_", " "),
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = theme.colors.primary,
                        unfocusedBorderColor = theme.colors.textSecondary.copy(alpha = 0.3f)
                    )
                )
                ExposedDropdownMenu(
                    expanded = roleExpanded,
                    onDismissRequest = { roleExpanded = false }
                ) {
                    CharacterRole.values().forEach { characterRole ->
                        DropdownMenuItem(
                            text = { Text(characterRole.name.replace("_", " ")) },
                            onClick = {
                                onRoleChange(characterRole)
                                roleExpanded = false
                            }
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = theme.colors.primary
                )
            ) {
                Text("Next: Appearance")
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null)
            }
        }
    }
}

@Composable
fun AppearanceStep(
    age: Int,
    gender: Gender,
    height: String,
    build: String,
    hairColor: String,
    eyeColor: String,
    distinctiveFeatures: List<String>,
    onAgeChange: (Int) -> Unit,
    onGenderChange: (Gender) -> Unit,
    onHeightChange: (String) -> Unit,
    onBuildChange: (String) -> Unit,
    onHairColorChange: (String) -> Unit,
    onEyeColorChange: (String) -> Unit,
    onDistinctiveFeaturesChange: (List<String>) -> Unit,
    onNext: () -> Unit
) {
    val theme = rememberAppTheme()
    var newFeature by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Physical Appearance",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = theme.colors.textPrimary
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = age.toString(),
                    onValueChange = { it.toIntOrNull()?.let(onAgeChange) },
                    label = { Text("Age") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = theme.colors.primary
                    )
                )

                var genderExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = genderExpanded,
                    onExpandedChange = { genderExpanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = gender.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Gender") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                        modifier = Modifier.menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = theme.colors.primary
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = genderExpanded,
                        onDismissRequest = { genderExpanded = false }
                    ) {
                        Gender.values().forEach { g ->
                            DropdownMenuItem(
                                text = { Text(g.name) },
                                onClick = {
                                    onGenderChange(g)
                                    genderExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = height,
                    onValueChange = onHeightChange,
                    label = { Text("Height") },
                    placeholder = { Text("5'10\"") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = theme.colors.primary
                    )
                )

                OutlinedTextField(
                    value = build,
                    onValueChange = onBuildChange,
                    label = { Text("Build") },
                    placeholder = { Text("Athletic") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = theme.colors.primary
                    )
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = hairColor,
                    onValueChange = onHairColorChange,
                    label = { Text("Hair Color") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = theme.colors.primary
                    )
                )

                OutlinedTextField(
                    value = eyeColor,
                    onValueChange = onEyeColorChange,
                    label = { Text("Eye Color") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = theme.colors.primary
                    )
                )
            }
        }

        item {
            Text(
                "Distinctive Features",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = theme.colors.textPrimary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newFeature,
                    onValueChange = { newFeature = it },
                    label = { Text("Add feature") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = theme.colors.primary
                    )
                )

                IconButton(
                    onClick = {
                        if (newFeature.isNotBlank()) {
                            onDistinctiveFeaturesChange(distinctiveFeatures + newFeature)
                            newFeature = ""
                        }
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }

            if (distinctiveFeatures.isNotEmpty()) {
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    distinctiveFeatures.forEach { feature ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = theme.colors.surface
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(feature, modifier = Modifier.weight(1f))
                                IconButton(
                                    onClick = {
                                        onDistinctiveFeaturesChange(distinctiveFeatures - feature)
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Remove")
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = theme.colors.primary
                )
            ) {
                Text("Next: Personality")
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null)
            }
        }
    }
}

@Composable
fun PersonalityStep(
    openness: Float,
    conscientiousness: Float,
    extraversion: Float,
    agreeableness: Float,
    neuroticism: Float,
    onOpennessChange: (Float) -> Unit,
    onConscientiousnessChange: (Float) -> Unit,
    onExtraversionChange: (Float) -> Unit,
    onAgreeablenessChange: (Float) -> Unit,
    onNeuroticismChange: (Float) -> Unit,
    onNext: () -> Unit
) {
    val theme = rememberAppTheme()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Text(
                "OCEAN Personality",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = theme.colors.textPrimary
            )
            Text(
                "Adjust the sliders to define your character's personality traits",
                fontSize = 14.sp,
                color = theme.colors.textSecondary
            )
        }

        item {
            PersonalitySlider(
                label = "Openness",
                description = "Imagination, curiosity, openness to new experiences",
                value = openness,
                onValueChange = onOpennessChange,
                enabled = true
            )
        }

        item {
            PersonalitySlider(
                label = "Conscientiousness",
                description = "Organization, dependability, self-discipline",
                value = conscientiousness,
                onValueChange = onConscientiousnessChange,
                enabled = true
            )
        }

        item {
            PersonalitySlider(
                label = "Extraversion",
                description = "Sociability, assertiveness, positive emotions",
                value = extraversion,
                onValueChange = onExtraversionChange,
                enabled = true
            )
        }

        item {
            PersonalitySlider(
                label = "Agreeableness",
                description = "Trust, cooperation, compassion",
                value = agreeableness,
                onValueChange = onAgreeablenessChange,
                enabled = true
            )
        }

        item {
            PersonalitySlider(
                label = "Neuroticism",
                description = "Emotional instability, anxiety, moodiness",
                value = neuroticism,
                onValueChange = onNeuroticismChange,
                enabled = true
            )
        }

        item {
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = theme.colors.primary
                )
            ) {
                Text("Next: Voice Selection")
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null)
            }
        }
    }
}

@Composable
fun VoiceStep(
    selectedVoice: VoiceProfile?,
    onVoiceSelected: (VoiceProfile) -> Unit,
    onNext: () -> Unit
) {
    val theme = rememberAppTheme()

    // Sample voices
    val sampleVoices = remember {
        listOf(
            VoiceProfile(
                voiceId = "voice1",
                voiceModelType = "Neural",
                accent = "American",
                speakingStyle = "Professional",
                pitch = 0.6f,
                speed = 1.0f
            ),
            VoiceProfile(
                voiceId = "voice2",
                voiceModelType = "Neural",
                accent = "British",
                speakingStyle = "Authoritative",
                pitch = 0.4f,
                speed = 0.9f
            ),
            VoiceProfile(
                voiceId = "voice3",
                voiceModelType = "Neural",
                accent = "Irish",
                speakingStyle = "Warm",
                pitch = 0.5f,
                speed = 0.8f
            )
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Voice Selection",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = theme.colors.textPrimary
            )
            Text(
                "Choose a voice for your character",
                fontSize = 14.sp,
                color = theme.colors.textSecondary
            )
        }

        items(sampleVoices.size) { index ->
            val voice = sampleVoices[index]
            VoiceCard(
                voice = voice,
                isSelected = selectedVoice?.voiceId == voice.voiceId,
                onSelect = { onVoiceSelected(voice) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = theme.colors.primary
                )
            ) {
                Text("Next: Review")
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null)
            }
        }
    }
}

@Composable
fun VoiceCard(
    voice: VoiceProfile,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val theme = rememberAppTheme()

    Card(
        onClick = onSelect,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                theme.colors.primary.copy(alpha = 0.1f)
            else
                theme.colors.surface
        ),
        border = if (isSelected)
            BorderStroke(2.dp, theme.colors.primary)
        else
            null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.RecordVoiceOver,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = if (isSelected) theme.colors.primary else theme.colors.textSecondary
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    voice.voiceModelType ?: "Voice",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.colors.textPrimary
                )
                Text(
                    "${voice.accent ?: "Neutral"} accent • ${voice.speakingStyle ?: "Default"} style",
                    fontSize = 14.sp,
                    color = theme.colors.textSecondary
                )
            }

            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = theme.colors.primary
                )
            }
        }
    }
}

@Composable
fun ReviewStep(
    name: String,
    role: CharacterRole,
    age: Int,
    gender: Gender,
    height: String,
    build: String,
    hairColor: String,
    eyeColor: String,
    selectedVoice: VoiceProfile?,
    onComplete: () -> Unit
) {
    val theme = rememberAppTheme()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Review Character",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = theme.colors.textPrimary
            )
            Text(
                "Review your character details before creating",
                fontSize = 14.sp,
                color = theme.colors.textSecondary
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = theme.colors.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ReviewRow("Name", name)
                    ReviewRow("Role", role.name.replace("_", " "))
                    ReviewRow("Age", age.toString())
                    ReviewRow("Gender", gender.name)
                    if (height.isNotBlank()) ReviewRow("Height", height)
                    if (build.isNotBlank()) ReviewRow("Build", build)
                    if (hairColor.isNotBlank()) ReviewRow("Hair", hairColor)
                    if (eyeColor.isNotBlank()) ReviewRow("Eyes", eyeColor)
                    if (selectedVoice != null) {
                        ReviewRow(
                            "Voice",
                            "${selectedVoice.accent ?: "Neutral"} accent - ${selectedVoice.speakingStyle ?: "Default"} style"
                        )
                    }
                }
            }
        }

        item {
            Button(
                onClick = onComplete,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = theme.colors.success
                )
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create Character")
            }
        }
    }
}

@Composable
fun ReviewRow(label: String, value: String) {
    val theme = rememberAppTheme()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            fontWeight = FontWeight.Medium,
            color = theme.colors.textSecondary
        )
        Text(
            value,
            color = theme.colors.textPrimary
        )
    }
}
