// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/domain/model/DistributionModels.kt
package com.beekeeper.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class DistributionAnalytics(
    val projectId: String,
    val totalRevenue: Float,
    val revenueByChannel: Map<SocialPlatform, Float>,
    val revenueByGeo: Map<String, Float>,
    val revenueByContentType: Map<ContentClassification, Float>,
    val revenueByDuration: Map<DurationBucket, Float>,
    val performanceMetrics: DistributionPerformanceMetrics,
    val optimizationSuggestions: List<OptimizationSuggestion>,
    val totalViews: Int,
    val averageEngagement: Float,
    val periodStart: Long,
    val periodEnd: Long,
    val generatedAt: Long,
    val topPerformingPlatform: SocialPlatform = SocialPlatform.YOUTUBE,
    val titleRevenues: List<TitleRevenue> = emptyList() // ADD THIS LINE
)

@Serializable
data class DistributionPerformanceMetrics(
    val totalViews: Long = 0,
    val engagementRate: Float = 0f,
    val completionRate: Float = 0f,
    val shareRate: Float = 0f,
    val likeRate: Float = 0f,
    val commentRate: Float = 0f,
    val revenueGenerated: Float = 0f,
    val avgViewDuration: Float = 0f,
    val clickThroughRate: Float = 0f
)

@Serializable
data class OptimizationSuggestion(
    val id: String,
    val type: RecommendationType,
    val title: String,
    val description: String,
    val estimatedRevenueImpact: Float,
    val priority: Priority,
    val actionItems: List<String>
)

@Serializable
enum class RecommendationType {
    EDIT_SUGGESTION,
    PLATFORM_OPTIMIZATION,
    TIMING_ADJUSTMENT,
    CONTENT_FORMAT,
    AUDIENCE_TARGETING,
    MONETIZATION_STRATEGY,
    CROSS_PROMOTION,
    THUMBNAIL_OPTIMIZATION,
    TITLE_OPTIMIZATION,
    HASHTAG_STRATEGY
}

@Serializable
enum class Priority {
    LOW, MEDIUM, HIGH, CRITICAL
}

@Serializable
data class ContentClassification(
    val format: ContentFormat,
    val duration: DurationBucket,
    val orientation: ContentOrientation,
    val style: ContentStyle
)

@Serializable
enum class ContentFormat {
    LIVE_INTERACTIVE_AVATAR,
    LIVE_INTERACTIVE_TWIN,
    STATIC_SCENE,
    CINEMATIC,
    SHORT_FORM,
    LONG_FORM,
    HYBRID
}

@Serializable
enum class ContentStyle {
    REALISTIC,
    ANIMATED,
    STYLIZED,
    DOCUMENTARY,
    TUTORIAL,
    ENTERTAINMENT,
    EDUCATIONAL,
    PROMOTIONAL
}

@Serializable
enum class DurationBucket {
    EIGHT_SECONDS,
    THIRTY_SECONDS,
    ONE_TO_THREE_MIN,
    THREE_TO_TEN_MIN,
    TEN_TO_TWENTY_ONE_MIN,
    TWENTY_ONE_MIN,
    FORTY_TWO_MIN,
    NINETY_MIN,
    OVER_NINETY_MIN
}

@Serializable
enum class ContentOrientation {
    PORTRAIT,
    LANDSCAPE,
    SQUARE
}

@Serializable
data class TitleRevenue(
    val titleId: String,
    val title: String,
    val totalRevenue: Float,
    val channelBreakdown: Map<SocialPlatform, ChannelPerformance>,
    val viewershipRetention: List<RetentionPoint>,
    val recommendations: List<MonetizationRecommendation>,
    val contentClassification: ContentClassification,
    val publishDate: Long,
    val lastUpdated: Long
)

@Serializable
data class ChannelPerformance(
    val platform: SocialPlatform,
    val revenue: Float,
    val views: Long,
    val engagement: Float,
    val averageWatchTime: Float,
    val shareCount: Long,
    val commentCount: Long,
    val likeCount: Long
)

@Serializable
data class RetentionPoint(
    val timestamp: Float, // percentage of video duration (0-100)
    val viewership: Float, // percentage of viewers remaining (0-100)
    val eventMarker: String? = null // optional marker for key events
)

@Serializable
data class MonetizationRecommendation(
    val type: RecommendationType,
    val description: String,
    val estimatedImpact: Float,
    val priority: Priority,
    val implementationSteps: List<String> = emptyList()
)

@Serializable
data class RevenueUpdate(
    val projectId: String,
    val titleId: String,
    val platform: SocialPlatform,
    val revenue: Float,
    val period: String,
    val metrics: ChannelPerformance
)

@Serializable
enum class SortOrder {
    REVENUE_DESC,
    REVENUE_ASC,
    VIEWS_DESC,
    VIEWS_ASC,
    ENGAGEMENT_DESC,
    ENGAGEMENT_ASC,
    DATE_DESC,
    DATE_ASC,
    TITLE_ASC,
    TITLE_DESC
}
