// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/presentation/screens/CharacterCreationWizard.kt
package com.beekeeper.app.presentation.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beekeeper.app.domain.model.*
import com.beekeeper.app.presentation.components.SecondaryTopBar
import com.beekeeper.app.presentation.theme.rememberAppTheme
import com.beekeeper.app.utils.generateUniqueId

enum class WizardStep {
    START_POINT,
    AI_ANALYSIS,
    CORE_DETAILS,
    APPEARANCE,
    PERSONALITY,
    VOICE,
    REVIEW
}

enum class CreationMethod {
    FROM_IMAGE,
    FROM_DESCRIPTION,
    MANUAL
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterCreationWizard(
    projectId: String,
    onNavigateBack: () -> Unit,
    onComplete: (CharacterProfile) -> Unit
) {
    val theme = rememberAppTheme()
    var currentStep by remember { mutableStateOf(WizardStep.START_POINT) }
    var creationMethod by remember { mutableStateOf<CreationMethod?>(null) }

    // Character data
    var uploadedImageUrl by remember { mutableStateOf<String?>(null) }
    var description by remember { mutableStateOf("") }
    var aiSuggestions by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var isAnalyzing by remember { mutableStateOf(false) }

    // Character fields
    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(CharacterRole.SUPPORTING) }
    var age by remember { mutableStateOf(30) }
    var gender by remember { mutableStateOf(Gender.UNSPECIFIED) }
    var height by remember { mutableStateOf("") }
    var build by remember { mutableStateOf("") }
    var hairColor by remember { mutableStateOf("") }
    var eyeColor by remember { mutableStateOf("") }
    var distinctiveFeatures by remember { mutableStateOf(listOf<String>()) }

    // OCEAN personality
    var openness by remember { mutableStateOf(0.5f) }
    var conscientiousness by remember { mutableStateOf(0.5f) }
    var extraversion by remember { mutableStateOf(0.5f) }
    var agreeableness by remember { mutableStateOf(0.5f) }
    var neuroticism by remember { mutableStateOf(0.5f) }

    // Voice
    var selectedVoice by remember { mutableStateOf<VoiceProfile?>(null) }

    Scaffold(
        topBar = {
            SecondaryTopBar(
                title = "Create Character",
                subtitle = when (currentStep) {
                    WizardStep.START_POINT -> "Step 1 of 7: Choose Method"
                    WizardStep.AI_ANALYSIS -> "Step 2 of 7: AI Analysis"
                    WizardStep.CORE_DETAILS -> "Step 3 of 7: Core Details"
                    WizardStep.APPEARANCE -> "Step 4 of 7: Appearance"
                    WizardStep.PERSONALITY -> "Step 5 of 7: Personality"
                    WizardStep.VOICE -> "Step 6 of 7: Voice"
                    WizardStep.REVIEW -> "Step 7 of 7: Review"
                },
                onNavigateBack = {
                    when (currentStep) {
                        WizardStep.START_POINT -> onNavigateBack()
                        WizardStep.AI_ANALYSIS -> currentStep = WizardStep.START_POINT
                        WizardStep.CORE_DETAILS -> {
                            currentStep = if (creationMethod == CreationMethod.MANUAL)
                                WizardStep.START_POINT
                            else
                                WizardStep.AI_ANALYSIS
                        }
                        WizardStep.APPEARANCE -> currentStep = WizardStep.CORE_DETAILS
                        WizardStep.PERSONALITY -> currentStep = WizardStep.APPEARANCE
                        WizardStep.VOICE -> currentStep = WizardStep.PERSONALITY
                        WizardStep.REVIEW -> currentStep = WizardStep.VOICE
                    }
                }
            )
        },
        containerColor = theme.colors.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Progress Indicator
            LinearProgressIndicator(
                progress = when (currentStep) {
                    WizardStep.START_POINT -> 0.14f
                    WizardStep.AI_ANALYSIS -> 0.28f
                    WizardStep.CORE_DETAILS -> 0.42f
                    WizardStep.APPEARANCE -> 0.57f
                    WizardStep.PERSONALITY -> 0.71f
                    WizardStep.VOICE -> 0.85f
                    WizardStep.REVIEW -> 1.0f
                },
                modifier = Modifier.fillMaxWidth(),
                color = theme.colors.primary
            )

            // Step Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (currentStep) {
                    WizardStep.START_POINT -> StartPointStep(
                        onMethodSelected = { method ->
                            creationMethod = method
                            currentStep = if (method == CreationMethod.MANUAL) {
                                WizardStep.CORE_DETAILS
                            } else {
                                WizardStep.AI_ANALYSIS
                            }
                        }
                    )

                    WizardStep.AI_ANALYSIS -> AIAnalysisStep(
                        creationMethod = creationMethod!!,
                        uploadedImageUrl = uploadedImageUrl,
                        description = description,
                        isAnalyzing = isAnalyzing,
                        aiSuggestions = aiSuggestions,
                        onImageUpload = { url -> uploadedImageUrl = url },
                        onDescriptionChange = { description = it },
                        onAnalyze = {
                            isAnalyzing = true
                            // TODO: Call AI service
                            // Simulate AI response
                            aiSuggestions = mapOf(
                                "name" to "Aria Blackwood",
                                "age" to "28",
                                "role" to "PROTAGONIST",
                                "hairColor" to "Dark brown",
                                "eyeColor" to "Green",
                                "build" to "Athletic"
                            )
                            isAnalyzing = false
                        },
                        onNext = {
                            // Apply AI suggestions
                            aiSuggestions["name"]?.let { name = it }
                            aiSuggestions["age"]?.toIntOrNull()?.let { age = it }
                            aiSuggestions["hairColor"]?.let { hairColor = it }
                            aiSuggestions["eyeColor"]?.let { eyeColor = it }
                            aiSuggestions["build"]?.let { build = it }
                            currentStep = WizardStep.CORE_DETAILS
                        }
                    )

                    WizardStep.CORE_DETAILS -> CoreDetailsStep(
                        name = name,
                        role = role,
                        onNameChange = { name = it },
                        onRoleChange = { role = it },
                        onNext = { currentStep = WizardStep.APPEARANCE }
                    )

                    WizardStep.APPEARANCE -> AppearanceStep(
                        age = age,
                        gender = gender,
                        height = height,
                        build = build,
                        hairColor = hairColor,
                        eyeColor = eyeColor,
                        distinctiveFeatures = distinctiveFeatures,
                        onAgeChange = { age = it },
                        onGenderChange = { gender = it },
                        onHeightChange = { height = it },
                        onBuildChange = { build = it },
                        onHairColorChange = { hairColor = it },
                        onEyeColorChange = { eyeColor = it },
                        onDistinctiveFeaturesChange = { distinctiveFeatures = it },
                        onNext = { currentStep = WizardStep.PERSONALITY }
                    )

                    WizardStep.PERSONALITY -> PersonalityStep(
                        openness = openness,
                        conscientiousness = conscientiousness,
                        extraversion = extraversion,
                        agreeableness = agreeableness,
                        neuroticism = neuroticism,
                        onOpennessChange = { openness = it },
                        onConscientiousnessChange = { conscientiousness = it },
                        onExtraversionChange = { extraversion = it },
                        onAgreeablenessChange = { agreeableness = it },
                        onNeuroticismChange = { neuroticism = it },
                        onNext = { currentStep = WizardStep.VOICE }
                    )

                    WizardStep.VOICE -> VoiceStep(
                        selectedVoice = selectedVoice,
                        onVoiceSelected = { selectedVoice = it },
                        onNext = { currentStep = WizardStep.REVIEW }
                    )

                    WizardStep.REVIEW -> ReviewStep(
                        name = name,
                        role = role,
                        age = age,
                        gender = gender,
                        height = height,
                        build = build,
                        hairColor = hairColor,
                        eyeColor = eyeColor,
                        selectedVoice = selectedVoice,
                        onComplete = {
                            val character = createCharacterFromWizard(
                                projectId = projectId,
                                name = name,
                                role = role,
                                age = age,
                                gender = gender,
                                height = height,
                                build = build,
                                hairColor = hairColor,
                                eyeColor = eyeColor,
                                distinctiveFeatures = distinctiveFeatures,
                                openness = openness,
                                conscientiousness = conscientiousness,
                                extraversion = extraversion,
                                agreeableness = agreeableness,
                                neuroticism = neuroticism,
                                voiceProfile = selectedVoice
                            )
                            onComplete(character)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StartPointStep(
    onMethodSelected: (CreationMethod) -> Unit
) {
    val theme = rememberAppTheme()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "How would you like to create your character?",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = theme.colors.textPrimary
        )

        Text(
            "Choose the method that works best for you",
            fontSize = 16.sp,
            color = theme.colors.textSecondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        MethodCard(
            icon = Icons.Default.Image,
            title = "From Image",
            description = "Upload an image and let AI analyze it to create your character",
            iconColor = theme.colors.primary,
            onClick = { onMethodSelected(CreationMethod.FROM_IMAGE) }
        )

        MethodCard(
            icon = Icons.Default.Description,
            title = "From Description",
            description = "Describe your character and AI will help fill in the details",
            iconColor = theme.colors.success,
            onClick = { onMethodSelected(CreationMethod.FROM_DESCRIPTION) }
        )

        MethodCard(
            icon = Icons.Default.Edit,
            title = "Manual Entry",
            description = "Fill in all character details yourself",
            iconColor = theme.colors.textSecondary,
            onClick = { onMethodSelected(CreationMethod.MANUAL) }
        )
    }
}

@Composable
fun MethodCard(
    icon: ImageVector,
    title: String,
    description: String,
    iconColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    val theme = rememberAppTheme()

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = theme.colors.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = iconColor
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.colors.textPrimary
                )
                Text(
                    description,
                    fontSize = 14.sp,
                    color = theme.colors.textSecondary
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = theme.colors.textSecondary
            )
        }
    }
}

// Helper function to create character from wizard data
fun createCharacterFromWizard(
    projectId: String,
    name: String,
    role: CharacterRole,
    age: Int,
    gender: Gender,
    height: String,
    build: String,
    hairColor: String,
    eyeColor: String,
    distinctiveFeatures: List<String>,
    openness: Float,
    conscientiousness: Float,
    extraversion: Float,
    agreeableness: Float,
    neuroticism: Float,
    voiceProfile: VoiceProfile?
): CharacterProfile {
    return CharacterProfile(
        id = generateUniqueId(),
        projectId = projectId,
        name = name,
        role = role,
        archetype = "", // Will be filled later
        personality = PersonalityProfile(
            backstory = "", // Will be filled later
            oceanScores = OceanPersonality(
                openness = openness,
                conscientiousness = conscientiousness,
                extraversion = extraversion,
                agreeableness = agreeableness,
                neuroticism = neuroticism
            ),
            traits = emptyList()
        ),
        age = age,
        height = height,
        build = build,
        hairColor = hairColor,
        eyeColor = eyeColor,
        physicalAttributes = PhysicalAttributes(
            height = height,
            build = build,
            hairColor = hairColor,
            eyeColor = eyeColor,
            distinctiveFeatures = distinctiveFeatures
        ),
        distinctiveFeatures = distinctiveFeatures,
        gender = gender,
        voiceProfile = voiceProfile,
        createdAt = kotlinx.datetime.Clock.System.now(),
        updatedAt = kotlinx.datetime.Clock.System.now(),
        relationships = emptyList(),
        screenTime = 0f,
        dialogueCount = 0
    )
}
