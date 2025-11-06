// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/presentation/viewmodels/DistributionDashboardViewModel.kt
package com.beekeeper.app.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beekeeper.app.domain.model.*
import com.beekeeper.app.domain.model.DocExportFormat.*
import com.beekeeper.app.domain.repository.ProjectRepository
import com.beekeeper.app.domain.repository.DistributionRepository
import com.beekeeper.app.domain.repository.DistributionRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Platform info for the publishing screen
data class PlatformInfo(
    val id: String,
    val name: String,
    val platform: SocialPlatform,
    val isConnected: Boolean,
    val followers: Int? = null,
    val username: String? = null,
    val lastPublished: Long? = null
)

data class DistributionDashboardUiState(
    // Analytics data
    val distributionAnalytics: DistributionAnalytics? = null,
    val selectedTitle: TitleRevenue? = null,
    val sortOrder: SortOrder = SortOrder.REVENUE_DESC,

    // Filter states
    val filterChannel: SocialPlatform? = null,
    val filterContentType: ContentFormat? = null,
    val dateRange: DateRange = DateRange.THIRTY_DAYS,

    // Platform connections (for publishing)
    val connectedPlatforms: List<PlatformInfo> = emptyList(),
    val availablePlatforms: List<PlatformInfo> = emptyList(),
    val selectedPlatformIds: Set<String> = emptySet(),

    // Publishing state
    val isPublishing: Boolean = false,
    val publishProgress: Float = 0f,
    val publishMessage: String? = null,

    // Export state
    val isExporting: Boolean = false,
    val exportProgress: Float = 0f,
    val lastExportResult: DocExportResult? = null,

    // Loading and error states
    val isLoading: Boolean = false,
    val error: String? = null
)

enum class DateRange {
    TWENTY_FOUR_HOURS,
    SEVEN_DAYS,
    THIRTY_DAYS,
    NINETY_DAYS,
    ONE_YEAR,
    ALL_TIME
}

class DistributionDashboardViewModel(
    private val projectId: String,
    private val projectRepository: ProjectRepository,
    private val distributionRepository: DistributionRepository = com.beekeeper.app.domain.repository.RepositoryManager.distributionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DistributionDashboardUiState())
    val uiState: StateFlow<DistributionDashboardUiState> = _uiState.asStateFlow()

    // Legacy state flows for backward compatibility
    private val _distributionAnalytics = MutableStateFlow<DistributionAnalytics?>(null)
    val distributionAnalytics: StateFlow<DistributionAnalytics?> = _distributionAnalytics

    private val _selectedTitle = MutableStateFlow<TitleRevenue?>(null)
    val selectedTitle: StateFlow<TitleRevenue?> = _selectedTitle

    private val _sortOrder = MutableStateFlow(SortOrder.REVENUE_DESC)
    val sortOrder: StateFlow<SortOrder> = _sortOrder

    init {
        loadDistributionData()
        loadPlatforms()
        observeAnalyticsUpdates()
    }

    fun loadDistributionData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // Load distribution analytics
                val analytics = distributionRepository.getDistributionAnalytics(projectId)

                _distributionAnalytics.value = analytics
                _uiState.update {
                    it.copy(
                        distributionAnalytics = analytics,
                        isLoading = false,
                        error = null
                    )
                }

                // Apply initial sorting
                sortTitles()

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load distribution data: ${e.message}"
                    )
                }
            }
        }
    }

    private fun loadPlatforms() {
        viewModelScope.launch {
            try {
                // Get all available platforms
                val allPlatforms = distributionRepository.getAllPlatforms()
                val connectedIds = distributionRepository.getConnectedPlatformIds(projectId)

                val platformInfos = allPlatforms.map { platform ->
                    val connectionInfo = if (connectedIds.contains(platform.name)) {
                        distributionRepository.getPlatformConnectionInfo(projectId, platform)
                    } else null

                    PlatformInfo(
                        id = platform.name,
                        name = platform.name,
                        platform = platform,
                        isConnected = connectedIds.contains(platform.name),
                        followers = connectionInfo?.followers,
                        username = connectionInfo?.username,
                        lastPublished = connectionInfo?.lastPublished
                    )
                }

                _uiState.update {
                    it.copy(
                        availablePlatforms = platformInfos,
                        connectedPlatforms = platformInfos.filter { it.isConnected }
                    )
                }
            } catch (e: Exception) {
                // Log error but don't fail the whole screen
                println("Failed to load platforms: ${e.message}")
            }
        }
    }

    private fun observeAnalyticsUpdates() {
        viewModelScope.launch {
            distributionRepository.observeDistributionAnalytics(projectId)
                .collect { analytics ->
                    _distributionAnalytics.value = analytics
                    _uiState.update {
                        it.copy(distributionAnalytics = analytics)
                    }
                    sortTitles()
                }
        }
    }

    fun selectTitle(title: TitleRevenue?) {
        _selectedTitle.value = title
        _uiState.update { it.copy(selectedTitle = title) }
    }

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
        _uiState.update { it.copy(sortOrder = order) }
        sortTitles()
    }

    fun setFilterChannel(platform: SocialPlatform?) {
        _uiState.update { it.copy(filterChannel = platform) }
        applyFilters()
    }

    fun setFilterContentType(format: ContentFormat?) {
        _uiState.update { it.copy(filterContentType = format) }
        applyFilters()
    }

    fun setDateRange(range: DateRange) {
        _uiState.update { it.copy(dateRange = range) }
        loadDistributionData() // Reload with new date range
    }

    private fun sortTitles() {
        val analytics = _uiState.value.distributionAnalytics ?: return
        val sortOrder = _uiState.value.sortOrder

        viewModelScope.launch {
            val sortedTitles = when (sortOrder) {
                SortOrder.REVENUE_DESC -> analytics.titleRevenues.sortedByDescending { it.totalRevenue }
                SortOrder.REVENUE_ASC -> analytics.titleRevenues.sortedBy { it.totalRevenue }
                SortOrder.VIEWS_DESC -> analytics.titleRevenues.sortedByDescending {
                    it.channelBreakdown.values.sumOf { channel -> channel.views }
                }
                SortOrder.VIEWS_ASC -> analytics.titleRevenues.sortedBy {
                    it.channelBreakdown.values.sumOf { channel -> channel.views }
                }
                SortOrder.ENGAGEMENT_DESC -> analytics.titleRevenues.sortedByDescending {
                    it.channelBreakdown.values.map { channel -> channel.engagement }.average()
                }
                SortOrder.ENGAGEMENT_ASC -> analytics.titleRevenues.sortedBy {
                    it.channelBreakdown.values.map { channel -> channel.engagement }.average()
                }
                SortOrder.DATE_DESC -> analytics.titleRevenues.sortedByDescending { it.publishDate }
                SortOrder.DATE_ASC -> analytics.titleRevenues.sortedBy { it.publishDate }
                SortOrder.TITLE_ASC -> analytics.titleRevenues.sortedBy { it.title }
                SortOrder.TITLE_DESC -> analytics.titleRevenues.sortedByDescending { it.title }
            }

            _uiState.update {
                it.copy(
                    distributionAnalytics = analytics.copy(titleRevenues = sortedTitles)
                )
            }
            _distributionAnalytics.value = analytics.copy(titleRevenues = sortedTitles)
        }
    }

    private fun applyFilters() {
        val analytics = _uiState.value.distributionAnalytics ?: return
        val filterChannel = _uiState.value.filterChannel
        val filterContentType = _uiState.value.filterContentType

        viewModelScope.launch {
            var filteredTitles = analytics.titleRevenues

            // Apply channel filter
            if (filterChannel != null) {
                filteredTitles = filteredTitles.filter { title ->
                    title.channelBreakdown.containsKey(filterChannel)
                }
            }

            // Apply content type filter
            if (filterContentType != null) {
                filteredTitles = filteredTitles.filter { title ->
                    title.contentClassification.format == filterContentType
                }
            }

            // Apply current sorting to filtered results
            val sortedFilteredTitles = when (_uiState.value.sortOrder) {
                SortOrder.REVENUE_DESC -> filteredTitles.sortedByDescending { it.totalRevenue }
                SortOrder.REVENUE_ASC -> filteredTitles.sortedBy { it.totalRevenue }
                SortOrder.VIEWS_DESC -> filteredTitles.sortedByDescending {
                    it.channelBreakdown.values.sumOf { channel -> channel.views }
                }
                SortOrder.VIEWS_ASC -> filteredTitles.sortedBy {
                    it.channelBreakdown.values.sumOf { channel -> channel.views }
                }
                SortOrder.ENGAGEMENT_DESC -> filteredTitles.sortedByDescending {
                    it.channelBreakdown.values.map { channel -> channel.engagement }.average()
                }
                SortOrder.ENGAGEMENT_ASC -> filteredTitles.sortedBy {
                    it.channelBreakdown.values.map { channel -> channel.engagement }.average()
                }
                SortOrder.DATE_DESC -> filteredTitles.sortedByDescending { it.publishDate }
                SortOrder.DATE_ASC -> filteredTitles.sortedBy { it.publishDate }
                SortOrder.TITLE_ASC -> filteredTitles.sortedBy { it.title }
                SortOrder.TITLE_DESC -> filteredTitles.sortedByDescending { it.title }
            }

            _uiState.update {
                it.copy(
                    distributionAnalytics = analytics.copy(titleRevenues = sortedFilteredTitles)
                )
            }
            _distributionAnalytics.value = analytics.copy(titleRevenues = sortedFilteredTitles)
        }
    }

    fun exportAnalytics(format: DocExportFormat) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isExporting = true, exportProgress = 0f) }

                // Update progress periodically
                _uiState.update { it.copy(exportProgress = 0.3f) }

                val result: com.beekeeper.app.domain.repository.DocExportResult? = distributionRepository.exportAnalytics(
                    projectId = projectId,
                    analytics = _uiState.value.distributionAnalytics,
                    format = format
                )

                _uiState.update { it.copy(exportProgress = 0.7f) }

                _uiState.update {
                    it.copy(
                        isExporting = false,
                        exportProgress = 1f,
                        lastExportResult = result as DocExportResult?
                    )
                }

                // Show success message or handle file download
                handleExportSuccess(result as DocExportResult?)

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        exportProgress = 0f,
                        error = "Export failed: ${e.message}"
                    )
                }
            }
        }
    }

    private fun handleExportSuccess(result: DocExportResult?) {
        // Handle the export result - show toast, trigger download, etc.
        try {
            when (result?.format) {
                CSV -> {
                    // Trigger CSV download
                    println("CSV exported: ${result.filePath}")
                }
                PDF -> {
                    // Trigger PDF download
                    println("PDF exported: ${result.filePath}")
                }
                EXCEL -> {
                    // Trigger Excel download
                    println("Excel exported: ${result.filePath}")
                }
                JSON -> {
                    // Trigger JSON download
                    println("JSON exported: ${result.filePath}")
                }

                DOCX -> {
                    // Trigger JSON download
                    println("DOCX exported: ${result.filePath}")
                }

                TXT -> {
                    // Trigger JSON download
                    println("TXT exported: ${result.filePath}")
                }

                HTML -> {
                    // Trigger JSON download
                    println("HTML exported: ${result.filePath}")
                }

                XML -> {
                    // Trigger JSON download
                    println("XML exported: ${result.filePath}")
                }

                null -> {
                    // Trigger JSON download
                }
            }
        } catch (e: Exception) {
            TODO("Not yet implemented")
        }
    }

    fun refreshData() {
        loadDistributionData()
        loadPlatforms()
    }

    fun publishToChannels(titleId: String, platformIds: Set<String>) {
        viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(
                        isPublishing = true,
                        publishProgress = 0f,
                        selectedPlatformIds = platformIds
                    )
                }

                // Simulate publishing progress
                for (i in 1..10) {
                    kotlinx.coroutines.delay(500)
                    _uiState.update {
                        it.copy(publishProgress = i / 10f)
                    }
                }

                _uiState.update {
                    it.copy(
                        isPublishing = false,
                        publishProgress = 1f,
                        publishMessage = "Successfully published to ${platformIds.size} platforms"
                    )
                }

                // Refresh data to show updated metrics
                loadDistributionData()

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isPublishing = false,
                        publishProgress = 0f,
                        error = "Publishing failed: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearExportResult() {
        _uiState.update { it.copy(lastExportResult = null) }
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up any resources if needed
    }
}

// Supporting data classes that might be missing

@kotlinx.serialization.Serializable
data class PlatformConnectionInfo(
    val platform: SocialPlatform,
    val username: String,
    val followers: Int,
    val isVerified: Boolean = false,
    val lastPublished: Long? = null,
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val expiresAt: Long? = null
)

@kotlinx.serialization.Serializable
enum class ExportFormat {
    CSV,
    PDF,
    EXCEL,
    JSON
}