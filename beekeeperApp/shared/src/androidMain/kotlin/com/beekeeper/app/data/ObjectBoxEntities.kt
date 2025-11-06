// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/data/ObjectBoxEntities.kt
package com.beekeeper.app.data

import io.objectbox.annotation.*
import io.objectbox.relation.ToMany
import io.objectbox.relation.ToOne
import com.beekeeper.app.domain.model.*

/**
 * Complete ObjectBox Entities that exactly match the domain models
 * Using superset approach - includes ALL fields from domain models
 *
 * CRITICAL: Frames reference DialogueLines via dialogueLineId for audio generation
 * Scene has 1:1 mapping with SceneScript via sceneScriptId
 */

// ===== PROJECT ENTITIES =====

@Entity
data class ProjectEntity(
    @Id var id: Long = 0,
    @Unique var projectId: String = "",
    var title: String = "",
    var description: String = "",
    var type: Int = 0, // ProjectType enum ordinal
    var status: Int = 0, // ProjectStatus enum ordinal
    var timeline: String = "", // JSON ProjectTimeline
    var team: String = "", // JSON ProjectTeam
    var budget: String = "", // JSON ProjectBudget
    var platformTargets: String = "", // JSON List<StreamingPlatform>
    var currentPhase: Int = 0, // ProductionPhase enum ordinal
    var createdAt: Long = 0,
    var updatedAt: Long = 0,
    var clientId: String = "",
    var priority: Int = 0, // ProjectPriority enum ordinal
    var metadata: String = "" // JSON Map<String, String>
) {
    lateinit var deliverables: ToMany<DeliverableEntity>
    lateinit var stories: ToMany<StoryEntity>
    lateinit var characters: ToMany<CharacterEntity>
    lateinit var scripts: ToMany<ScriptEntity>
    lateinit var storyboards: ToMany<StoryboardEntity>
    lateinit var contents: ToMany<ContentEntity>
}

@Entity
data class DeliverableEntity(
    @Id var id: Long = 0,
    @Unique var deliverableId: String = "",
    var projectIdString: String = "",
    var title: String = "",
    var description: String = "",
    var type: String = "", // DeliverableType enum name
    var format: String = "",
    var duration: Float? = null,
    var targetPlatforms: String = "", // JSON List<StreamingPlatform>
    var status: String = "", // DeliverableStatus enum name
    var dueDate: Long = 0,
    var deliveredDate: Long? = null,
    var filePath: String? = null,
    var fileSize: Long? = null,
    var qualityMetrics: String? = null // JSON QualityMetrics
)

// ===== STORY ENTITIES =====

@Entity
data class StoryEntity(
    @Id var id: Long = 0,
    @Unique var storyId: String = "",
    var projectIdString: String = "",
    var scriptId: String = "", // CHANGED: Now required (non-nullable)
    var title: String = "",
    var logline: String = "", // JSON Map<String, String> for multi-language
    var synopsis: String = "",
    var genre: String = "",
    var themes: String = "", // JSON List<String>
    var setting: String = "",
    var targetAudience: String = "", // JSON List<String>
    var status: Int = 0, // ContentStatus enum ordinal
    var createdAt: Long = 0,
    var updatedAt: Long = 0,
    var acts: String = "", // JSON List<StoryAct>
    var mainCharacters: String = "", // JSON List<String>
    var storyboardIds: String = "", // JSON List<String>
    var duration: Int? = null,
    var lastEditedBy: String? = null,
    var episodeNumber: Int? = null,
    var seasonNumber: Int? = null,
    var episodeCode: String? = null,
    var episodeTitle: String? = null,
    var isStandalone: Boolean = false,
    var sequenceOrder: Int? = null,
    var storyCategory: String = "EPISODE", // StoryCategory enum name
    var estimatedBudget: Float? = null,
    var metadata: String = "", // JSON Map<String, PlatformSettings>
    var exportFormats: String = "" // JSON List<ExportFormat>
)

// ===== SCRIPT ENTITIES =====

@Entity
data class ScriptEntity(
    @Id var id: Long = 0,
    @Unique var scriptId: String = "",
    var projectIdString: String = "",
    var storyIdString: String = "",
    var title: String = "",
    var content: String = "",
    var format: String = "", // ScriptFormat enum name
    var writtenBy: String = "",
    var lastEditedBy: String = "",
    var createdAt: Long = 0,
    var updatedAt: Long = 0,
    var draft: Int = 1,
    var sceneCount: Int = 0,
    var characterCount: Int = 0,
    var pageCount: Int = 0,
    var estimatedDuration: Int = 0,

    // NEW FIELDS FROM DOMAIN MODEL
    var version: String = "1.0",
    var pages: Int = 0,
    var wordCount: Int = 0,
    var duration: Int = 0,
    var collaborators: String = "", // JSON List<String>
    var isLocked: Boolean = false,
    var status: Int = 0, // ContentStatus enum ordinal
    var structure: String = "THREE_ACT",
    var draftNumber: Int = 1,
    var language: String = "en",
    var genre: String = "", // JSON List<String>
    var targetAudience: String? = null,
    var rating: String? = null,
    var logline: String? = null,
    var synopsis: String? = null,
    var treatment: String? = null,
    var locationCount: Int = 0,
    var dayScenes: Int = 0,
    var nightScenes: Int = 0,
    var interiorScenes: Int = 0,
    var exteriorScenes: Int = 0,
    var revisionHistory: String = "", // JSON List<ScriptRevision>
    var colorRevision: String? = null,
    var shootingScriptNumber: String? = null,
    var registrationNumber: String? = null,
    var copyrightInfo: String? = null,
    var contactInfo: String? = null,
    var agent: String? = null,
    var notes: String = "", // JSON List<ScriptNote>
    var breakdowns: String? = null, // JSON ScriptBreakdowns
    var budget: String? = null, // JSON BudgetInfo
    var schedule: String? = null, // JSON ProductionSchedule
    var metadata: String = "", // JSON Map<String, String>
    var revisionNotes: String = "",
    var themes: String = "", // JSON List<String>
    var props: String = "" // JSON List<String>
) {
    lateinit var acts: ToMany<ActEntity>
    lateinit var scenes: ToMany<SceneScriptEntity> // Direct scenes (no acts)
}

@Entity
data class ActEntity(
    @Id var id: Long = 0,
    @Unique var actId: String = "",
    var scriptIdString: String = "",
    var actNumber: Int = 0,
    var title: String = "",
    var description: String = "",

    // NEW FIELDS FROM DOMAIN MODEL
    var duration: Int = 0,
    var purpose: String? = null,
    var turningPoint: String? = null,
    var emotionalArc: String? = null,
    var themes: String = "", // JSON List<String>
    var notes: String? = null,
    var colorCode: String? = null,
    var isFlashback: Boolean = false,
    var isFlashforward: Boolean = false,
    var parallelAction: String = "", // JSON List<String>
    var metadata: String = "", // JSON Map<String, String>
    var pageStart: Int = 0,
    var pageEnd: Int = 0,
    var sceneCount: Int = 0
) {
    lateinit var scenes: ToMany<SceneScriptEntity>
}

@Entity
data class SceneScriptEntity(
    @Id var id: Long = 0,
    @Unique var sceneScriptId: String = "",
    var scriptIdString: String = "",
    var actIdString: String? = null,
    var sceneNumber: Int = 0,
    var sceneNumberStr: String = "",
    var heading: String? = null,
    var action: String? = null,
    var emotionalTone: Int = 0, // EmotionalTone enum ordinal
    var narrativeFunction: Int = 0, // NarrativeFunction enum ordinal
    var title: String = "",
    var description: String = "",
    var duration: Int = 0,
    var characters: String = "", // JSON List<String> character names- not required
    var characterIds: String = "", // JSON List<String> character IDs- required!!!
    var isCompleted: Boolean = false,
    var location: String? = null,
    var timeOfDay: String? = null,
    var transitions: String? = null, // JSON TransitionInfo
    var metadata: String = "", // JSON Map<String, String>

    // NEW FIELDS FROM DOMAIN MODEL
    var pageStart: Float? = null,
    var pageEnd: Float? = null,
    var beatSheet: String = "", // JSON List<StoryBeat>
    var subheading: String? = null,
    var propIds: String = "", // JSON List<String>
    var cameraDirections: String = "", // JSON List<String>
    var notes: String? = null
) {
    lateinit var dialogueLines: ToMany<DialogueLineEntity>
}

@Entity
data class DialogueLineEntity(
    @Id var id: Long = 0,
    @Unique var dialogueId: String = "",
    var sceneScriptIdString: String = "",
    var characterName: String = "",
    var dialogue: String = "",
    var characterId: String = "",
    var parenthetical: String? = null,
    var isDualDialogue: Boolean = false,
    var emotion: Int = 0, // EmotionalTone enum ordinal
    var timestamp: Float = 0f,
    var lineNumber: Int? = null,
    var revisionColor: String? = null,
    var isVoiceOver: Boolean = false,
    var isOffScreen: Boolean = false,
    var isPreLap: Boolean = false,
    var isContinued: Boolean = false,
    var isAdLib: Boolean = false,
    var deliveryNote: String? = null,
    var dialect: String? = null,
    var language: String? = null,
    var subtitleRequired: Boolean = false,
    var alternateLines: String = "", // JSON List<String>
    var audioFileId: String? = null,
    var duration: Float? = null,
    var overlapping: Boolean = false,
    var emphasis: String = "", // JSON List<EmphasisInfo>
    var pronunciation: String = "", // JSON Map<String, String>
    var culturalNote: String? = null,
    var censorshipNote: String? = null,
    var metadata: String = "" // JSON Map<String, String>
)

// ===== VISUAL ENTITIES (Storyboard/Scene/Frame) =====

@Entity
data class StoryboardEntity(
    @Id var id: Long = 0,
    @Unique var storyboardId: String = "",
    var projectIdString: String = "",
    var storyIdString: String = "",
    var scriptIdString: String = "",
    var title: String = "",
    var description: String? = null,
    var sceneCount: Int = 0,
    var duration: Int = 0,
    var createdAt: Long = 0,
    var updatedAt: Long = 0,
    var createdBy: String = "",
    var version: Int = 1,
    var isLocked: Boolean = false,
    var thumbnailUrl: String? = null,
    var completionPercentage: Int = 0,
    var storyboardType: Int = 0, // StoryboardType enum ordinal
    var aspectRatio: String = "", // AspectRatio enum name
    var resolution: String = "", // Resolution enum name
    var platformSettings: String = "", // JSON Map<String, StoryboardPlatformSettings>

    // NEW FIELD
    var sceneScriptIds: String = "" // JSON List<String> - tracks which written scenes are covered
) {
    lateinit var scenes: ToMany<SceneEntity>
}

@Entity
data class SceneEntity(
    @Id var id: Long = 0,
    @Unique var sceneId: String = "",
    var storyboardIdString: String = "",
    var sceneNumber: Int = 0,
    var title: String = "",
    var description: String = "",
    var sceneScriptId: String = "", // Required 1:1 mapping with SceneScript
    var duration: Int = 0,
    var dialogue: String? = null,
    var notes: String? = null,
    var cameraInstructions: String? = null,
    var soundEffects: String = "", // JSON List<String>
    var musicCues: String = "", // JSON List<String>
    var imageUrl: String? = null,
    var videoUrl: String? = null,
    var dialogueSnippet: String? = null,
    var cameraDirection: String? = null,
    var location: String? = null,
    var timeOfDay: String? = null,
    var shotType: String? = null, // ShotType enum name
    var cameraAngle: String? = null, // CameraAngle enum name
    var cameraMovement: String? = null, // CameraMovement enum name
    var transitionIn: String? = null, // TransitionType enum name
    var transitionOut: String? = null, // TransitionType enum name
    var vfxNotes: String? = null,
    var audioNotes: String? = null,
    var generationSettings: String? = null, // JSON GenerationSettings

    // NEW FIELDS FROM DOMAIN MODEL
    var transitionType: String = "NONE", // TransitionType enum name (default NONE)
    var isKeyScene: Boolean = false,
    var aiSuggestions: String = "", // JSON List<String>
    var lightingNotes: String? = null,
    var propIds: String = "", // JSON List<String>
    var soundNotes: String? = null
) {
    lateinit var frames: ToMany<FrameEntity>
}

@Entity
data class FrameEntity(
    @Id var id: Long = 0,
    @Unique var frameId: String = "",
    var sceneIdString: String = "",
    var frameNumber: Int = 0,
    var duration: Float = 0f,
    var shotType: String = "", // ShotType enum name
    var cameraAngle: String = "", // CameraAngle enum name
    var cameraMovement: String = "", // CameraMovement enum name
    var description: String = "",
    var imageUrl: String = "",
    var thumbnailUrl: String? = null,
    var dialogueLineId: String = "",
    var action: String = "",
    var vfxNotes: String? = null,
    var audioNotes: String? = null,
    var transitionIn: String? = null, // TransitionType enum name
    var transitionOut: String? = null, // TransitionType enum name
    var generationSettings: String? = null, // JSON GenerationSettings
    var createdAt: Long = 0,
    var updatedAt: Long = 0
)

// ===== CHARACTER ENTITIES =====

@Entity
data class CharacterEntity(
    @Id var id: Long = 0,
    @Unique var characterId: String = "",
    var projectIdString: String = "",
    var name: String = "",
    var fullName: String = "",
    var role: String = "",
    var description: String = "",
    var traits: String = "", // JSON List<String>
    var goals: String = "", // JSON List<String>
    var conflicts: String = "", // JSON List<String>
    var arc: String = "",
    var assignedActorId: String? = null,
    var voiceActorId: String? = null,
    var archetype: String = "",
    var personality: String = "", // JSON PersonalityProfile
    var assignedAvatarId: String? = null,
    var screenTime: Float = 0f,
    var dialogueCount: Int = 0,
    var age: Int = 0,
    var height: String = "",
    var gender: String = "",
    var build: String = "",
    var hairColor: String = "",
    var eyeColor: String = "",
    var connections: Int = 0,
    var distinctiveFeatures: String = "",
    var voiceProfile: String? = null,
    var backstory: String = "",
    var physicalAttributes: String? = null, // JSON PhysicalAttributes
    var physicalDescription: String = "",
    var relationshipStatus: String = "",
    var characterRole: Int = 0, // CharacterRole enum ordinal
    var importance: Int = 0,
    var firstAppearance: String = "",
    var totalAppearances: Int = 0,
    var occupation: String = "",
    var imageUrl: String? = null,
    var createdAt: Long = 0,
    var updatedAt: Long = 0,
    var metadata: String = "" // JSON Map<String, String>
) {
    lateinit var relationships: ToMany<CharacterRelationshipEntity>
}

@Entity
data class CharacterRelationshipEntity(
    @Id var id: Long = 0,
    var sourceCharacterIdString: String = "",
    var targetCharacterId: String = "",
    var targetCharacterName: String = "",
    var relationshipType: Int = 0, // RelationshipType enum ordinal
    var description: String = "",
    var strength: Float = 0f
)

// ===== AVATAR ENTITIES =====

@Entity
data class AvatarEntity(
    @Id var id: Long = 0,
    @Unique var avatarId: String = "",
    var name: String = "",
    var description: String = "",
    var thumbnailUrl: String = "",
    var fullImageUrl: String = "",
    var videoPreviewUrl: String? = null,
    var style: String = "",
    var generationPlatform: String = "", // GenerationPlatform enum name
    var generationParams: String = "", // JSON Map<String, String>
    var tags: String = "", // JSON List<String>
    var createdAt: Long = 0,
    var updatedAt: Long = 0,
    var createdBy: String = "",
    var isPublic: Boolean = false
)

// ===== CONTENT ENTITIES =====

@Entity
data class ContentEntity(
    @Id var id: Long = 0,
    @Unique var contentId: String = "",
    var projectIdString: String = "",
    var title: String = "",
    var format: String = "", // ContentType
    var url: String = "",
    var thumbnailUrl: String? = null,
    var size: Long = 0,
    var duration: Float? = null,
    var dimensions: String? = null, // JSON Dimensions
    var metadata: String? = null, // JSON ContentMetadata
    var tags: String = "", // JSON List<String>
    var createdAt: Long = 0,
    var updatedAt: Long = 0,
    var uploadedBy: String = "",
    var isProcessed: Boolean = false,
    var isPublic: Boolean = false,
    var description: String? = null,
    var version: String = "",
    var checksum: String? = null,
    var processingStatus: String = ""
)

// ===== USER ENTITIES =====

// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/data/ObjectBoxEntities.kt
// UPDATE UserEntity to include all fields from User.kt

@Entity
data class UserEntity(
    @Id var id: Long = 0,
    @Unique var userId: String = "",
    var username: String = "",
    var name: String = "John Doe",
    var email: String = "john.doe@example.com",
    var role: String = "Content Creator",
    var joinDate: String = "Member since Jan 2024",
    var avatarUrl: String? = null,
    var subscription: String = "PRO", // SubscriptionType name
    var projectsCreated: Int = 42,
    var videosPublished: Int = 128,
    var totalViews: String = "1.2M",
    var storageUsed: String = "45.3 GB",
    var displayName: String = "",
    var bio: String? = null,
    var isVerified: Boolean = false,
    var followerCount: Int = 0,
    var followingCount: Int = 0,
    var postCount: Int = 0,
    var enableNotifications: Boolean = true,
    var privateAccount: Boolean = false,
    var showAiContent: Boolean = true,
    var autoplayVideos: Boolean = true,
    var feedPreference: String = "FOR_YOU", // FeedType name
    var createdAt: Long = 0,
    var updatedAt: Long = 0,
    var followingIds: String = "", // Pipe-separated list for social features
    var followerIds: String = "" // Pipe-separated list for social features
)

// ===== PUBLISHING ENTITIES =====

@Entity
data class PublishingProjectEntity(
    @Id var id: Long = 0,
    @Unique var projectId: String = "",
    var title: String = "",
    var description: String = "",
    var contentIds: String = "", // Pipe-separated list
    var platforms: String = "", // JSON List<PlatformConfig>
    var exportSettings: String = "", // JSON ExportSettings
    var metadata: String = "", // JSON ProjectMetadata
    var status: Int = 0, // PublishingStatus enum ordinal
    var createdAt: Long = 0,
    var updatedAt: Long = 0,
    var publishedAt: Long? = null
)

// ===== FEED/NOTIFICATION ENTITIES =====

@Entity
data class FeedPostEntity(
    @Id var id: Long = 0,
    @Unique var postId: String = "",
    var author: String = "", // JSON FeedAuthor
    var content: String = "", // JSON FeedContent
    var stats: String = "", // JSON PostStats
    var createdAt: Long = 0,
    var updatedAt: Long = 0,
    var visibility: String = "",
    var hashtags: String = "", // JSON List<String>
    var mentions: String = "", // JSON List<String>
    var isPromoted: Boolean = false,
    var metadata: String = "" // JSON Map<String, Any>
)

@Entity
data class NotificationEntity(
    @Id var id: Long = 0,
    @Unique var notificationId: String = "",
    var userId: String = "",
    var type: String = "",
    var title: String = "",
    var message: String = "",
    var targetId: String? = null,
    var targetType: String? = null,
    var timestamp: Long = 0,
    var isRead: Boolean = false,
    var actor: String? = null,
    var metadata: String = "" // JSON Map<String, Any>
)