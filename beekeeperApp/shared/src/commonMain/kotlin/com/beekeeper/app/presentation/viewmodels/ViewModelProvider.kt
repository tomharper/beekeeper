// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/presentation/viewmodels/ViewModelProvider.kt
package com.beekeeper.app.presentation.viewmodels

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.beekeeper.app.domain.repository.RepositoryManager
import com.beekeeper.app.domain.repository.ProjectRepositoryImpl

/**
 * Provides singleton ViewModels with proper repository injection
 * This ensures all screens use the same data source
 */

@Composable
fun rememberProjectsViewModel(): ProjectsViewModel {
    return remember {
        ProjectsViewModel(RepositoryManager.projectRepository)
    }
}

@Composable
fun rememberHomeViewModel(): HomeViewModel {
    return remember {
        HomeViewModel(RepositoryManager.projectRepository)
    }
}

@Composable
fun rememberProjectDetailViewModel(projectId: String): ProjectDetailViewModel {
    return remember(projectId) {
        ProjectDetailViewModel(
            repository = RepositoryManager.projectRepository,
            projectId = projectId
        )
    }
}

@Composable
fun rememberCharacterProfileViewModel(projectId: String? = null): CharacterProfileViewModel {
    return remember(projectId) {
        CharacterProfileViewModel(
            characterRepository = RepositoryManager.characterRepository,
            projectRepository = RepositoryManager.projectRepository
        ).apply {
            initializeForProject(projectId!!)
            loadCharacters(projectId)
        }
    }
}

@Composable
fun rememberStoryHubViewModel(projectId: String): StoryHubViewModel {
    return remember(projectId) {
        StoryHubViewModel(
            projectId = projectId,
            contentRepository = RepositoryManager.contentRepository,
            characterRepository = RepositoryManager.characterRepository
        )
    }
}

@Composable
fun rememberScriptDevelopmentViewModel(projectId: String, storyId: String): ScriptDevelopmentViewModel {
    return remember(projectId) {
        ScriptDevelopmentViewModel(
            projectId = projectId,
            storyId = storyId,
            contentRepository = RepositoryManager.contentRepository,
            patternsRepository = RepositoryManager.storyPatternsRepository,
        )
    }
}

@Composable
fun rememberContentQualityReviewViewModel(projectId: String): ContentQualityReviewViewModel {
    return remember(projectId) {
        ContentQualityReviewViewModel(
            projectId = projectId,
            projectRepository = RepositoryManager.projectRepository,
            contentRepository = RepositoryManager.contentRepository
        )
    }
}

@Composable
fun rememberStoryPatternsViewModel(projectId: String): StoryPatternsViewModel {
    return remember(projectId) {
        StoryPatternsViewModel(
            projectId = projectId,
            repository = RepositoryManager.storyPatternsRepository,
        )
    }
}

@Composable
fun rememberDistributionViewModel(projectId: String): DistributionDashboardViewModel {
    return remember {
        DistributionDashboardViewModel(
            projectId = projectId,
            projectRepository = RepositoryManager.projectRepository,
            distributionRepository = RepositoryManager.distributionRepository
        )
    }
}
