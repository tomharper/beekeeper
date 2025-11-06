// File: shared/src/wasmJsMain/kotlin/com/cinefiller/fillerapp/serialization/WasmJson.kt
package com.beekeeper.app.serialization

import kotlin.js.JSON

/**
 * Simple JSON serialization for WASM using browser's native JSON API
 * This replaces kotlinx-serialization which doesn't have WASM support yet
 */
object WasmJson {
    
    fun stringify(obj: Any?): String {
        return JSON.stringify(obj)
    }
    
    fun <T> parse(json: String): T {
        @Suppress("UNCHECKED_CAST")
        return JSON.parse(json) as T
    }
    
    inline fun <reified T> decodeFromString(json: String): T {
        return parse(json)
    }
    
    fun encodeToString(value: Any?): String {
        return stringify(value)
    }
    
    // Helper functions for common conversions
    fun toJsonObject(obj: Any): dynamic {
        val json = js("{}")
        val keys = js("Object.keys(obj)")
        for (i in 0 until keys.length as Int) {
            val key = keys[i] as String
            json[key] = obj.asDynamic()[key]
        }
        return json
    }
    
    fun fromJsonObject(json: dynamic): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>()
        val keys = js("Object.keys(json)")
        for (i in 0 until keys.length as Int) {
            val key = keys[i] as String
            map[key] = json[key]
        }
        return map
    }
}

// Extension functions for data classes
inline fun <reified T : Any> T.toJson(): String {
    return WasmJson.stringify(this)
}

inline fun <reified T> String.fromJson(): T {
    return WasmJson.parse(this)
}

// Simple data class serialization
fun Any.toJsObject(): dynamic {
    val obj = js("{}")
    
    // Use reflection-like approach for data classes
    val proto = this.asDynamic().constructor.prototype
    val props = js("Object.getOwnPropertyNames(this)")
    
    for (i in 0 until props.length as Int) {
        val prop = props[i] as String
        if (prop != "constructor") {
            obj[prop] = this.asDynamic()[prop]
        }
    }
    
    return obj
}

// Serialization for common domain models
fun serializeProject(project: dynamic): String {
    val obj = js("{}")
    obj.id = project.id
    obj.name = project.name
    obj.description = project.description
    obj.createdAt = project.createdAt
    obj.updatedAt = project.updatedAt
    obj.metadata = project.metadata
    return JSON.stringify(obj)
}

fun deserializeProject(json: String): dynamic {
    val obj = JSON.parse(json)
    val project = js("{}")
    project.id = obj.id
    project.name = obj.name
    project.description = obj.description
    project.createdAt = obj.createdAt
    project.updatedAt = obj.updatedAt
    project.metadata = obj.metadata
    return project
}

fun serializeStory(story: dynamic): String {
    val obj = js("{}")
    obj.id = story.id
    obj.projectId = story.projectId
    obj.title = story.title
    obj.content = story.content
    obj.updatedAt = story.updatedAt
    return JSON.stringify(obj)
}

fun deserializeStory(json: String): dynamic {
    val obj = JSON.parse(json)
    val story = js("{}")
    story.id = obj.id
    story.projectId = obj.projectId
    story.title = obj.title
    story.content = obj.content
    story.updatedAt = obj.updatedAt
    return story
}
