// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/domain/factory/ProjectFactorySerializer.kt
package com.beekeeper.app.domain.factory

import kotlinx.serialization.json.Json

/**
 * Utility for serializing/deserializing ProjectFactory instances
 * Used for JSON blob storage in SQLDelight and backend sync
 */
object ProjectFactorySerializer {

    private val json = Json {
        prettyPrint = false // Compact for storage
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val prettyJson = Json {
        prettyPrint = true // For debugging/export
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    /**
     * Serialize factory to JSON (compact for storage)
     */
    fun serialize(factory: ProjectFactory): String {
        return json.encodeToString(ProjectFactory.serializer(), factory)
    }

    /**
     * Serialize factory to pretty JSON (for debugging/export)
     */
    fun serializePretty(factory: ProjectFactory): String {
        return prettyJson.encodeToString(ProjectFactory.serializer(), factory)
    }

    /**
     * Deserialize factory from JSON
     */
    fun deserialize(jsonString: String): ProjectFactory {
        return json.decodeFromString(ProjectFactory.serializer(), jsonString)
    }

    /**
     * Serialize multiple factories to JSON array
     */
    fun serializeList(factories: List<ProjectFactory>): String {
        return json.encodeToString(
            kotlinx.serialization.builtins.ListSerializer(ProjectFactory.serializer()),
            factories
        )
    }

    /**
     * Deserialize list of factories from JSON array
     */
    fun deserializeList(jsonString: String): List<ProjectFactory> {
        return json.decodeFromString(
            kotlinx.serialization.builtins.ListSerializer(ProjectFactory.serializer()),
            jsonString
        )
    }

    /**
     * Test serialization round-trip
     * Returns true if serialization/deserialization works correctly
     */
    fun testRoundTrip(factory: ProjectFactory): Boolean {
        return try {
            val json = serialize(factory)
            val deserialized = deserialize(json)
            deserialized.projectId == factory.projectId
        } catch (e: Exception) {
            println("❌ Serialization test failed: ${e.message}")
            false
        }
    }

    /**
     * Get size estimate of serialized factory (in bytes)
     */
    fun getSerializedSize(factory: ProjectFactory): Int {
        return serialize(factory).encodeToByteArray().size
    }

    /**
     * Get human-readable size
     */
    fun getSerializedSizeFormatted(factory: ProjectFactory): String {
        val bytes = getSerializedSize(factory)
        return when {
            bytes < 1024 -> "$bytes bytes"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> "${bytes / (1024 * 1024)} MB"
        }
    }

    // ===== COMPONENT SERIALIZATION =====
    // Individual component serializers to avoid 2MB CursorWindow limit

    fun serializeProject(project: com.beekeeper.app.domain.model.CreativeProject): String {
        return json.encodeToString(com.beekeeper.app.domain.model.CreativeProject.serializer(), project)
    }

    fun serializeCharacters(characters: List<com.beekeeper.app.domain.model.CharacterProfile>): String? {
        if (characters.isEmpty()) return null
        return json.encodeToString(
            kotlinx.serialization.builtins.ListSerializer(com.beekeeper.app.domain.model.CharacterProfile.serializer()),
            characters
        )
    }

    fun serializeStories(stories: List<com.beekeeper.app.domain.model.Story>): String? {
        if (stories.isEmpty()) return null
        return json.encodeToString(
            kotlinx.serialization.builtins.ListSerializer(com.beekeeper.app.domain.model.Story.serializer()),
            stories
        )
    }

    fun serializeScripts(scripts: List<com.beekeeper.app.domain.model.Script>): String? {
        if (scripts.isEmpty()) return null
        return json.encodeToString(
            kotlinx.serialization.builtins.ListSerializer(com.beekeeper.app.domain.model.Script.serializer()),
            scripts
        )
    }

    fun serializeStoryboards(storyboards: List<com.beekeeper.app.domain.model.Storyboard>): String? {
        if (storyboards.isEmpty()) return null
        return json.encodeToString(
            kotlinx.serialization.builtins.ListSerializer(com.beekeeper.app.domain.model.Storyboard.serializer()),
            storyboards
        )
    }

    fun serializeBible(bible: com.beekeeper.app.domain.model.ProjectBible?): String? {
        if (bible == null) return null
        return json.encodeToString(com.beekeeper.app.domain.model.ProjectBible.serializer(), bible)
    }

    fun serializePublishing(publishing: com.beekeeper.app.domain.model.PublishingProject?): String? {
        if (publishing == null) return null
        return json.encodeToString(com.beekeeper.app.domain.model.PublishingProject.serializer(), publishing)
    }

    fun serializeMetadata(metadata: ProjectFactoryMetadata): String {
        return json.encodeToString(ProjectFactoryMetadata.serializer(), metadata)
    }

    /**
     * Deserialize factory from separate component JSON strings
     */
    fun deserializeFromComponents(
        projectJson: String,
        charactersJson: String?,
        storiesJson: String?,
        scriptsJson: String?,
        storyboardsJson: String?,
        bibleJson: String?,
        publishingJson: String?,
        metadataJson: String?
    ): ProjectFactory {
        val project = json.decodeFromString(com.beekeeper.app.domain.model.CreativeProject.serializer(), projectJson)

        val characters = charactersJson?.let {
            json.decodeFromString(
                kotlinx.serialization.builtins.ListSerializer(com.beekeeper.app.domain.model.CharacterProfile.serializer()),
                it
            )
        } ?: emptyList()

        val stories = storiesJson?.let {
            json.decodeFromString(
                kotlinx.serialization.builtins.ListSerializer(com.beekeeper.app.domain.model.Story.serializer()),
                it
            )
        } ?: emptyList()

        val scripts = scriptsJson?.let {
            json.decodeFromString(
                kotlinx.serialization.builtins.ListSerializer(com.beekeeper.app.domain.model.Script.serializer()),
                it
            )
        } ?: emptyList()

        val storyboards = storyboardsJson?.let {
            json.decodeFromString(
                kotlinx.serialization.builtins.ListSerializer(com.beekeeper.app.domain.model.Storyboard.serializer()),
                it
            )
        } ?: emptyList()

        val bible = bibleJson?.let {
            json.decodeFromString(com.beekeeper.app.domain.model.ProjectBible.serializer(), it)
        }

        val publishing = publishingJson?.let {
            json.decodeFromString(com.beekeeper.app.domain.model.PublishingProject.serializer(), it)
        }

        val metadata = metadataJson?.let {
            json.decodeFromString(ProjectFactoryMetadata.serializer(), it)
        } ?: ProjectFactoryMetadata()

        return ProjectFactory(
            project = project,
            characters = characters,
            stories = stories,
            scripts = scripts,
            storyboards = storyboards,
            projectBible = bible,
            publishingProject = publishing,
            metadata = metadata
        )
    }
}
