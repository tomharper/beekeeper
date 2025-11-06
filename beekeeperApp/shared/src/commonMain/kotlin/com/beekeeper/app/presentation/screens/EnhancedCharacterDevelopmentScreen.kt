// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/presentation/screens/EnhancedCharacterDevelopmentScreen.kt
package com.beekeeper.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beekeeper.app.presentation.components.SecondaryTopBar
import com.beekeeper.app.presentation.theme.ThemeManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedCharacterDevelopmentScreen(
    characterName: String = "New Character",
    onNavigateBack: () -> Unit,
    onSaveCharacter: (CharacterDevelopmentData) -> Unit = {}
) {
    val theme by ThemeManager.currentTheme.collectAsState()

    // Character Questionnaire
    var coreValues by remember { mutableStateOf("") }
    var strengthsWeaknesses by remember { mutableStateOf("") }
    var motivationsGoals by remember { mutableStateOf("") }

    // Personality Traits
    var selectedTraits by remember { mutableStateOf(setOf<String>()) }

    // Physical Attributes
    var age by remember { mutableStateOf("") }
    var appearance by remember { mutableStateOf("") }
    var distinctiveFeatures by remember { mutableStateOf("") }

    // Backstory Builder
    var childhoodUpbringing by remember { mutableStateOf("") }
    var significantEvents by remember { mutableStateOf("") }
    var relationships by remember { mutableStateOf("") }

    // Additional Attributes
    var occupation by remember { mutableStateOf("") }
    var skills by remember { mutableStateOf("") }
    var fears by remember { mutableStateOf("") }
    var desires by remember { mutableStateOf("") }
    var speechPattern by remember { mutableStateOf("") }
    var quirksHabits by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            SecondaryTopBar(
                title = "Character Development",
                onNavigateBack = onNavigateBack
            )
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
            // Character Questionnaire Section
            item {
                SectionHeader("Character Questionnaire", theme)
            }

            item {
                QuestionField(
                    question = "What are the character's core values and beliefs?",
                    value = coreValues,
                    onValueChange = { coreValues = it },
                    theme = theme
                )
            }

            item {
                QuestionField(
                    question = "What are the character's strengths and weaknesses?",
                    value = strengthsWeaknesses,
                    onValueChange = { strengthsWeaknesses = it },
                    theme = theme
                )
            }

            item {
                QuestionField(
                    question = "What are the character's motivations and goals?",
                    value = motivationsGoals,
                    onValueChange = { motivationsGoals = it },
                    theme = theme
                )
            }

            // Physical Attributes Section
            item {
                SectionHeader("Physical Attributes", theme)
            }

            item {
                QuestionField(
                    question = "Age and physical appearance",
                    value = appearance,
                    onValueChange = { appearance = it },
                    theme = theme,
                    maxLines = 3
                )
            }

            item {
                QuestionField(
                    question = "Distinctive features or characteristics",
                    value = distinctiveFeatures,
                    onValueChange = { distinctiveFeatures = it },
                    theme = theme,
                    maxLines = 2
                )
            }

            // Personality Traits Section
            item {
                SectionHeader("Personality Traits", theme)
            }

            item {
                PersonalityTraitSelector(
                    selectedTraits = selectedTraits,
                    onTraitToggle = { trait ->
                        selectedTraits = if (selectedTraits.contains(trait)) {
                            selectedTraits - trait
                        } else {
                            selectedTraits + trait
                        }
                    },
                    theme = theme
                )
            }

            // Psychological Profile Section
            item {
                SectionHeader("Psychological Profile", theme)
            }

            item {
                QuestionField(
                    question = "Occupation and key skills",
                    value = skills,
                    onValueChange = { skills = it },
                    theme = theme,
                    maxLines = 3
                )
            }

            item {
                QuestionField(
                    question = "Deepest fears and anxieties",
                    value = fears,
                    onValueChange = { fears = it },
                    theme = theme,
                    maxLines = 3
                )
            }

            item {
                QuestionField(
                    question = "Greatest desires and aspirations",
                    value = desires,
                    onValueChange = { desires = it },
                    theme = theme,
                    maxLines = 3
                )
            }

            // Backstory Builder Section
            item {
                SectionHeader("Backstory Builder", theme)
            }

            item {
                QuestionField(
                    question = "Describe the character's childhood and upbringing",
                    value = childhoodUpbringing,
                    onValueChange = { childhoodUpbringing = it },
                    theme = theme,
                    maxLines = 5
                )
            }

            item {
                QuestionField(
                    question = "What significant events shaped the character's life?",
                    value = significantEvents,
                    onValueChange = { significantEvents = it },
                    theme = theme,
                    maxLines = 5
                )
            }

            item {
                QuestionField(
                    question = "What are the character's relationships with other characters?",
                    value = relationships,
                    onValueChange = { relationships = it },
                    theme = theme,
                    maxLines = 5
                )
            }

            // Voice & Mannerisms Section
            item {
                SectionHeader("Voice & Mannerisms", theme)
            }

            item {
                QuestionField(
                    question = "Speech patterns and dialogue style",
                    value = speechPattern,
                    onValueChange = { speechPattern = it },
                    theme = theme,
                    maxLines = 3
                )
            }

            item {
                QuestionField(
                    question = "Quirks, habits, and unique behaviors",
                    value = quirksHabits,
                    onValueChange = { quirksHabits = it },
                    theme = theme,
                    maxLines = 3
                )
            }

            // Save Button
            item {
                Button(
                    onClick = {
                        onSaveCharacter(
                            CharacterDevelopmentData(
                                coreValues = coreValues,
                                strengthsWeaknesses = strengthsWeaknesses,
                                motivationsGoals = motivationsGoals,
                                appearance = appearance,
                                distinctiveFeatures = distinctiveFeatures,
                                personalityTraits = selectedTraits.toList(),
                                skills = skills,
                                fears = fears,
                                desires = desires,
                                childhoodUpbringing = childhoodUpbringing,
                                significantEvents = significantEvents,
                                relationships = relationships,
                                speechPattern = speechPattern,
                                quirksHabits = quirksHabits
                            )
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = theme.colors.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "Save Character",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    Text(
        text = title,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = theme.colors.textPrimary,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
private fun QuestionField(
    question: String,
    value: String,
    onValueChange: (String) -> Unit,
    theme: com.beekeeper.app.presentation.theme.AppTheme,
    maxLines: Int = 4
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                question,
                fontSize = 14.sp,
                color = theme.colors.textSecondary
            )
        },
        modifier = Modifier
            .fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = theme.colors.primary,
            unfocusedBorderColor = theme.colors.textSecondary.copy(alpha = 0.3f),
            focusedTextColor = theme.colors.textPrimary,
            unfocusedTextColor = theme.colors.textPrimary,
            cursorColor = theme.colors.primary
        ),
        shape = RoundedCornerShape(12.dp),
        minLines = maxLines,
        maxLines = maxLines
    )
}

@Composable
private fun PersonalityTraitSelector(
    selectedTraits: Set<String>,
    onTraitToggle: (String) -> Unit,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    val traits = listOf(
        "Brave", "Loyal", "Intelligent", "Compassionate", "Determined",
        "Creative", "Ambitious", "Honest", "Mysterious", "Charismatic",
        "Cynical", "Optimistic", "Cautious", "Impulsive", "Analytical"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        traits.chunked(3).forEach { rowTraits ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowTraits.forEach { trait ->
                    FilterChip(
                        selected = selectedTraits.contains(trait),
                        onClick = { onTraitToggle(trait) },
                        label = {
                            Text(
                                trait,
                                fontSize = 13.sp
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = theme.colors.primary.copy(alpha = 0.2f),
                            selectedLabelColor = theme.colors.primary,
                            containerColor = theme.colors.surface,
                            labelColor = theme.colors.textSecondary
                        )
                    )
                }
                // Fill remaining space if row has less than 3 items
                repeat(3 - rowTraits.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

data class CharacterDevelopmentData(
    val coreValues: String,
    val strengthsWeaknesses: String,
    val motivationsGoals: String,
    val appearance: String,
    val distinctiveFeatures: String,
    val personalityTraits: List<String>,
    val skills: String,
    val fears: String,
    val desires: String,
    val childhoodUpbringing: String,
    val significantEvents: String,
    val relationships: String,
    val speechPattern: String,
    val quirksHabits: String
)
