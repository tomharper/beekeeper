// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/presentation/screens/CreateProjectScreen.kt
package com.beekeeper.app.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beekeeper.app.domain.model.*
import com.beekeeper.app.presentation.components.PrimaryTopBar
import com.beekeeper.app.presentation.theme.ThemeManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProjectScreen(
    onNavigateBack: () -> Unit,
    onProjectCreated: (String) -> Unit
) {
    val theme by ThemeManager.currentTheme.collectAsState()

    var projectTitle by remember { mutableStateOf("") }
    var projectDescription by remember { mutableStateOf("") }
    var selectedProjectType by remember { mutableStateOf(ProjectType.SOCIAL_MEDIA_CONTENT) }
    var selectedPriority by remember { mutableStateOf(ProjectPriority.MEDIUM) }
    var selectedStatus by remember { mutableStateOf(ProjectStatus.DRAFT) }
    var selectedPhase by remember { mutableStateOf(ProductionPhase.DEVELOPMENT) }
    var targetBudget by remember { mutableStateOf("") }
    var genre by remember { mutableStateOf("") }
    var targetAudience by remember { mutableStateOf("") }
    var projectTheme by remember { mutableStateOf("") }
    var isCreating by remember { mutableStateOf(false) }
    var showTypeDropdown by remember { mutableStateOf(false) }
    var showPriorityDropdown by remember { mutableStateOf(false) }
    var showStatusDropdown by remember { mutableStateOf(false) }
    var showPhaseDropdown by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            PrimaryTopBar(
                title = "Create New Project",
                actions = {
                    TextButton(
                        onClick = onNavigateBack,
                        enabled = !isCreating
                    ) {
                        Text(
                            "Cancel",
                            color = theme.colors.textSecondary
                        )
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Project Basic Information
            ProjectBasicInfoSection(
                title = projectTitle,
                onTitleChange = { projectTitle = it },
                description = projectDescription,
                onDescriptionChange = { projectDescription = it },
                theme = theme
            )

            // Project Type Selection
            ProjectTypeSection(
                selectedType = selectedProjectType,
                onTypeSelected = { selectedProjectType = it },
                showDropdown = showTypeDropdown,
                onDropdownToggle = { showTypeDropdown = it },
                theme = theme
            )

            // Project Priority
            ProjectPrioritySection(
                selectedPriority = selectedPriority,
                onPrioritySelected = { selectedPriority = it },
                showDropdown = showPriorityDropdown,
                onDropdownToggle = { showPriorityDropdown = it },
                theme = theme
            )

            // Project Status
            ProjectStatusSection(
                selectedStatus = selectedStatus,
                onStatusSelected = { selectedStatus = it },
                showDropdown = showStatusDropdown,
                onDropdownToggle = { showStatusDropdown = it },
                theme = theme
            )

            // Production Phase
            ProductionPhaseSection(
                selectedPhase = selectedPhase,
                onPhaseSelected = { selectedPhase = it },
                showDropdown = showPhaseDropdown,
                onDropdownToggle = { showPhaseDropdown = it },
                theme = theme
            )

            // Metadata Section (genre, target audience, theme)
            ProjectMetadataSection(
                genre = genre,
                onGenreChange = { genre = it },
                targetAudience = targetAudience,
                onTargetAudienceChange = { targetAudience = it },
                themeText = projectTheme,
                onThemeChange = { projectTheme = it },
                theme = theme
            )

            // Budget Information
            ProjectBudgetSection(
                budget = targetBudget,
                onBudgetChange = { targetBudget = it },
                theme = theme
            )

            Spacer(modifier = Modifier.weight(1f))

            // Create Button
            Button(
                onClick = {
                    if (projectTitle.isNotBlank()) {
                        isCreating = true
                        // TODO: Create project via repository
                        // For now, simulate creation and navigate
                        val projectId = generateProjectId()
                        onProjectCreated(projectId)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = projectTitle.isNotBlank() && !isCreating,
                colors = ButtonDefaults.buttonColors(
                    containerColor = theme.colors.primary
                )
            ) {
                if (isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = theme.colors.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "Create Project",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun ProjectBasicInfoSection(
    title: String,
    onTitleChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Project Information",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = theme.colors.textPrimary
        )

        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text("Project Title") },
            placeholder = { Text("Enter your project title") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = theme.colors.primary,
                unfocusedBorderColor = theme.colors.textSecondary.copy(alpha = 0.5f)
            )
        )

        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text("Project Description") },
            placeholder = { Text("Describe your project...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            maxLines = 4,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = theme.colors.primary,
                unfocusedBorderColor = theme.colors.textSecondary.copy(alpha = 0.5f)
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProjectTypeSection(
    selectedType: ProjectType,
    onTypeSelected: (ProjectType) -> Unit,
    showDropdown: Boolean,
    onDropdownToggle: (Boolean) -> Unit,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Project Type",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = theme.colors.textPrimary
        )

        ExposedDropdownMenuBox(
            expanded = showDropdown,
            onExpandedChange = onDropdownToggle
        ) {
            OutlinedTextField(
                value = formatProjectTypeName(selectedType),
                onValueChange = { },
                readOnly = true,
                label = { Text("Content Type") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = showDropdown)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = theme.colors.primary,
                    unfocusedBorderColor = theme.colors.textSecondary.copy(alpha = 0.5f)
                )
            )

            ExposedDropdownMenu(
                expanded = showDropdown,
                onDismissRequest = { onDropdownToggle(false) }
            ) {
                ProjectType.values().forEach { type ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    text = formatProjectTypeName(type),
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = getProjectTypeDescription(type),
                                    fontSize = 12.sp,
                                    color = theme.colors.textSecondary
                                )
                            }
                        },
                        onClick = {
                            onTypeSelected(type)
                            onDropdownToggle(false)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProjectPrioritySection(
    selectedPriority: ProjectPriority,
    onPrioritySelected: (ProjectPriority) -> Unit,
    showDropdown: Boolean,
    onDropdownToggle: (Boolean) -> Unit,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Priority Level",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = theme.colors.textPrimary
        )

        ExposedDropdownMenuBox(
            expanded = showDropdown,
            onExpandedChange = onDropdownToggle
        ) {
            OutlinedTextField(
                value = selectedPriority.name,
                onValueChange = { },
                readOnly = true,
                label = { Text("Project Priority") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = showDropdown)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = theme.colors.primary,
                    unfocusedBorderColor = theme.colors.textSecondary.copy(alpha = 0.5f)
                )
            )

            ExposedDropdownMenu(
                expanded = showDropdown,
                onDismissRequest = { onDropdownToggle(false) }
            ) {
                ProjectPriority.values().forEach { priority ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = when (priority) {
                                        ProjectPriority.LOW -> Icons.Default.ArrowDownward
                                        ProjectPriority.MEDIUM -> Icons.Default.Remove
                                        ProjectPriority.HIGH -> Icons.Default.ArrowUpward
                                        ProjectPriority.CRITICAL -> Icons.Default.PriorityHigh
                                    },
                                    contentDescription = null,
                                    tint = when (priority) {
                                        ProjectPriority.LOW -> theme.colors.textSecondary
                                        ProjectPriority.MEDIUM -> theme.colors.textPrimary
                                        ProjectPriority.HIGH -> theme.colors.primary
                                        ProjectPriority.CRITICAL -> androidx.compose.ui.graphics.Color.Red
                                    }
                                )
                                Text(priority.name)
                            }
                        },
                        onClick = {
                            onPrioritySelected(priority)
                            onDropdownToggle(false)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProjectBudgetSection(
    budget: String,
    onBudgetChange: (String) -> Unit,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Budget Information",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = theme.colors.textPrimary
        )

        OutlinedTextField(
            value = budget,
            onValueChange = onBudgetChange,
            label = { Text("Target Budget (USD)") },
            placeholder = { Text("Enter estimated budget") },
            leadingIcon = {
                Text(
                    "$",
                    color = theme.colors.textSecondary,
                    fontSize = 16.sp
                )
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = theme.colors.primary,
                unfocusedBorderColor = theme.colors.textSecondary.copy(alpha = 0.5f)
            )
        )

        Text(
            text = "Optional: This helps us recommend appropriate tools and resources",
            fontSize = 12.sp,
            color = theme.colors.textSecondary
        )
    }
}

private fun formatProjectTypeName(type: ProjectType): String {
    return when (type) {
        ProjectType.SOCIAL_MEDIA_CONTENT -> "Social Media Content"
        ProjectType.MARKETING_CAMPAIGN -> "Marketing Campaign"
        ProjectType.MINI_SERIES -> "Mini Series"
        ProjectType.WEB_SERIES -> "Web Series"
        ProjectType.TV_EPISODE -> "TV Episode"
        ProjectType.TV_SERIES -> "TV Series"
        ProjectType.FEATURE_LENGTH -> "Feature Length"
        ProjectType.DOCUMENTARY -> "Documentary"
        ProjectType.INTERACTIVE_CONTENT -> "Interactive Content"
        ProjectType.LOCALIZED_CONTENT -> "Localized Content"
        ProjectType.FEATURE_FILM -> "Feature Film"
        ProjectType.SHORT_FILM -> "Short Film"
        ProjectType.ANIMATION -> "Animation"
        ProjectType.COMMERCIAL -> "Commercial"
        ProjectType.MUSIC_VIDEO -> "Music Video"
        ProjectType.EDUCATIONAL -> "Educational"
        ProjectType.EXPERIMENTAL -> "Experimental"
    }
}

private fun getProjectTypeDescription(type: ProjectType): String {
    return when (type) {
        ProjectType.SOCIAL_MEDIA_CONTENT -> "1-5 minute videos for social platforms"
        ProjectType.MARKETING_CAMPAIGN -> "Promotional content for existing IP"
        ProjectType.MINI_SERIES -> "5-10 minute episodic content"
        ProjectType.WEB_SERIES -> "Web-based episodic content"
        ProjectType.TV_EPISODE -> "30-60 minute episodes"
        ProjectType.TV_SERIES -> "Multi-episode series management"
        ProjectType.FEATURE_LENGTH -> "90+ minute content"
        ProjectType.DOCUMENTARY -> "Non-fiction content"
        ProjectType.INTERACTIVE_CONTENT -> "Branching narratives"
        ProjectType.LOCALIZED_CONTENT -> "Multi-language adaptations"
        ProjectType.FEATURE_FILM -> "Full-length theatrical release"
        ProjectType.SHORT_FILM -> "Short format content"
        ProjectType.ANIMATION -> "Animated content"
        ProjectType.COMMERCIAL -> "Advertisement content"
        ProjectType.MUSIC_VIDEO -> "Music accompanying visuals"
        ProjectType.EDUCATIONAL -> "Learning and training content"
        ProjectType.EXPERIMENTAL -> "Creative and artistic exploration"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProjectStatusSection(
    selectedStatus: ProjectStatus,
    onStatusSelected: (ProjectStatus) -> Unit,
    showDropdown: Boolean,
    onDropdownToggle: (Boolean) -> Unit,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Project Status",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = theme.colors.textPrimary
        )

        ExposedDropdownMenuBox(
            expanded = showDropdown,
            onExpandedChange = onDropdownToggle
        ) {
            OutlinedTextField(
                value = selectedStatus.name.replace("_", " "),
                onValueChange = { },
                readOnly = true,
                label = { Text("Status") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = showDropdown)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = theme.colors.primary,
                    unfocusedBorderColor = theme.colors.textSecondary.copy(alpha = 0.5f)
                )
            )

            ExposedDropdownMenu(
                expanded = showDropdown,
                onDismissRequest = { onDropdownToggle(false) }
            ) {
                ProjectStatus.values().forEach { status ->
                    DropdownMenuItem(
                        text = { Text(status.name.replace("_", " ")) },
                        onClick = {
                            onStatusSelected(status)
                            onDropdownToggle(false)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductionPhaseSection(
    selectedPhase: ProductionPhase,
    onPhaseSelected: (ProductionPhase) -> Unit,
    showDropdown: Boolean,
    onDropdownToggle: (Boolean) -> Unit,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Production Phase",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = theme.colors.textPrimary
        )

        ExposedDropdownMenuBox(
            expanded = showDropdown,
            onExpandedChange = onDropdownToggle
        ) {
            OutlinedTextField(
                value = selectedPhase.name.replace("_", " "),
                onValueChange = { },
                readOnly = true,
                label = { Text("Current Phase") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = showDropdown)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = theme.colors.primary,
                    unfocusedBorderColor = theme.colors.textSecondary.copy(alpha = 0.5f)
                )
            )

            ExposedDropdownMenu(
                expanded = showDropdown,
                onDismissRequest = { onDropdownToggle(false) }
            ) {
                ProductionPhase.values().forEach { phase ->
                    DropdownMenuItem(
                        text = { Text(phase.name.replace("_", " ")) },
                        onClick = {
                            onPhaseSelected(phase)
                            onDropdownToggle(false)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProjectMetadataSection(
    genre: String,
    onGenreChange: (String) -> Unit,
    targetAudience: String,
    onTargetAudienceChange: (String) -> Unit,
    themeText: String,
    onThemeChange: (String) -> Unit,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Additional Information",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = theme.colors.textPrimary
        )

        OutlinedTextField(
            value = genre,
            onValueChange = onGenreChange,
            label = { Text("Genre") },
            placeholder = { Text("e.g., Sci-Fi, Drama, Comedy") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = theme.colors.primary,
                unfocusedBorderColor = theme.colors.textSecondary.copy(alpha = 0.5f)
            )
        )

        OutlinedTextField(
            value = targetAudience,
            onValueChange = onTargetAudienceChange,
            label = { Text("Target Audience") },
            placeholder = { Text("e.g., Adults 18-35, Family-friendly") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = theme.colors.primary,
                unfocusedBorderColor = theme.colors.textSecondary.copy(alpha = 0.5f)
            )
        )

        OutlinedTextField(
            value = themeText,
            onValueChange = onThemeChange,
            label = { Text("Theme") },
            placeholder = { Text("e.g., Friendship, Adventure, Love") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = theme.colors.primary,
                unfocusedBorderColor = theme.colors.textSecondary.copy(alpha = 0.5f)
            )
        )

        Text(
            text = "Optional: These fields help categorize and organize your project",
            fontSize = 12.sp,
            color = theme.colors.textSecondary
        )
    }
}

private fun generateProjectId(): String {
    return "proj_${kotlinx.datetime.Clock.System.now().toEpochMilliseconds()}"
}