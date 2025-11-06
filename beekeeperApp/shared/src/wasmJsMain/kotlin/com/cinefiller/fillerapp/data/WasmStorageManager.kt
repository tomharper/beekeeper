// File: shared/src/wasmJsMain/kotlin/com/cinefiller/fillerapp/data/WasmStorageManager.kt
package com.beekeeper.app.data

import com.juul.indexeddb.*
import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import com.beekeeper.app.domain.model.*

class WasmStorageManager {
    companion object {
        private const val DB_NAME = "CineFillerDB"
        private const val DB_VERSION = 1
        
        // Object stores
        private const val PROJECTS_STORE = "projects"
        private const val STORIES_STORE = "stories"
        private const val SCRIPTS_STORE = "scripts"
        private const val STORYBOARDS_STORE = "storyboards"
        private const val CHARACTERS_STORE = "characters"
        private const val ASSETS_STORE = "assets"
        private const val SETTINGS_STORE = "settings"
        private const val CACHE_STORE = "cache"
    }
    
    private val json = Json { 
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = false
    }
    
    private var database: Database? = null
    
    suspend fun initialize() {
        database = openDatabase(DB_NAME, DB_VERSION) { database, oldVersion, newVersion ->
            if (oldVersion < 1) {
                // Create all object stores
                database.createObjectStore(PROJECTS_STORE, KeyPath("id"))
                database.createObjectStore(STORIES_STORE, KeyPath("id"))
                database.createObjectStore(SCRIPTS_STORE, KeyPath("id"))
                database.createObjectStore(STORYBOARDS_STORE, KeyPath("id"))
                database.createObjectStore(CHARACTERS_STORE, KeyPath("id"))
                database.createObjectStore(ASSETS_STORE, KeyPath("id"))
                database.createObjectStore(SETTINGS_STORE, KeyPath("key"))
                database.createObjectStore(CACHE_STORE, KeyPath("key"))
                
                // Create indexes for efficient queries
                database.objectStore(STORIES_STORE).createIndex("projectId", KeyPath("projectId"))
                database.objectStore(SCRIPTS_STORE).createIndex("projectId", KeyPath("projectId"))
                database.objectStore(STORYBOARDS_STORE).createIndex("projectId", KeyPath("projectId"))
                database.objectStore(CHARACTERS_STORE).createIndex("projectId", KeyPath("projectId"))
                database.objectStore(ASSETS_STORE).createIndex("projectId", KeyPath("projectId"))
                database.objectStore(ASSETS_STORE).createIndex("type", KeyPath("type"))
            }
        }
    }
    
    // Project operations
    suspend fun saveProject(project: Project) {
        database?.writeTransaction(PROJECTS_STORE) {
            objectStore(PROJECTS_STORE).put(project.toJsObject())
        }
    }
    
    suspend fun getProject(id: String): Project? {
        return database?.readTransaction(PROJECTS_STORE) {
            val result = objectStore(PROJECTS_STORE).get(Key(id))
            result?.let { 
                json.decodeFromString<Project>(JSON.stringify(it))
            }
        }
    }
    
    suspend fun getAllProjects(): List<Project> {
        return database?.readTransaction(PROJECTS_STORE) {
            val results = objectStore(PROJECTS_STORE).getAll()
            results.map { 
                json.decodeFromString<Project>(JSON.stringify(it))
            }
        } ?: emptyList()
    }
    
    suspend fun deleteProject(id: String) {
        database?.writeTransaction(PROJECTS_STORE) {
            objectStore(PROJECTS_STORE).delete(Key(id))
        }
    }
    
    // Story operations
    suspend fun saveStory(story: Story) {
        database?.writeTransaction(STORIES_STORE) {
            objectStore(STORIES_STORE).put(story.toJsObject())
        }
    }
    
    suspend fun getStoriesByProject(projectId: String): List<Story> {
        return database?.readTransaction(STORIES_STORE) {
            val index = objectStore(STORIES_STORE).index("projectId")
            val results = index.getAll(Key(projectId))
            results.map { 
                json.decodeFromString<Story>(JSON.stringify(it))
            }
        } ?: emptyList()
    }
    
    // Script operations
    suspend fun saveScript(script: Script) {
        database?.writeTransaction(SCRIPTS_STORE) {
            objectStore(SCRIPTS_STORE).put(script.toJsObject())
        }
    }
    
    suspend fun getScriptsByProject(projectId: String): List<Script> {
        return database?.readTransaction(SCRIPTS_STORE) {
            val index = objectStore(SCRIPTS_STORE).index("projectId")
            val results = index.getAll(Key(projectId))
            results.map { 
                json.decodeFromString<Script>(JSON.stringify(it))
            }
        } ?: emptyList()
    }
    
    // Asset operations
    suspend fun saveAsset(asset: Asset) {
        database?.writeTransaction(ASSETS_STORE) {
            objectStore(ASSETS_STORE).put(asset.toJsObject())
        }
    }
    
    suspend fun getAssetsByType(projectId: String, type: AssetType): List<Asset> {
        return database?.readTransaction(ASSETS_STORE) {
            val projectIndex = objectStore(ASSETS_STORE).index("projectId")
            val allProjectAssets = projectIndex.getAll(Key(projectId))
            allProjectAssets
                .map { json.decodeFromString<Asset>(JSON.stringify(it)) }
                .filter { it.type == type }
        } ?: emptyList()
    }
    
    // Settings operations
    suspend fun saveSetting(key: String, value: String) {
        database?.writeTransaction(SETTINGS_STORE) {
            objectStore(SETTINGS_STORE).put(
                jso {
                    this.key = key
                    this.value = value
                }
            )
        }
    }
    
    suspend fun getSetting(key: String): String? {
        return database?.readTransaction(SETTINGS_STORE) {
            val result = objectStore(SETTINGS_STORE).get(Key(key))
            result?.let { 
                it.asDynamic().value as? String
            }
        }
    }
    
    // Cache operations
    suspend fun cacheData(key: String, data: Any) {
        database?.writeTransaction(CACHE_STORE) {
            objectStore(CACHE_STORE).put(
                jso {
                    this.key = key
                    this.data = JSON.stringify(data)
                    this.timestamp = js("Date.now()")
                }
            )
        }
    }
    
    suspend fun getCachedData(key: String, maxAge: Long = 3600000): String? {
        return database?.readTransaction(CACHE_STORE) {
            val result = objectStore(CACHE_STORE).get(Key(key))
            result?.let {
                val timestamp = it.asDynamic().timestamp as Long
                val now = js("Date.now()") as Long
                if (now - timestamp < maxAge) {
                    it.asDynamic().data as String
                } else {
                    null
                }
            }
        }
    }
    
    // Export/Import functionality
    suspend fun exportDatabase(): String {
        val exportData = mutableMapOf<String, List<Any>>()
        
        database?.let { db ->
            exportData[PROJECTS_STORE] = getAllProjects()
            exportData[STORIES_STORE] = db.readTransaction(STORIES_STORE) {
                objectStore(STORIES_STORE).getAll()
            }
            exportData[SCRIPTS_STORE] = db.readTransaction(SCRIPTS_STORE) {
                objectStore(SCRIPTS_STORE).getAll()
            }
            exportData[ASSETS_STORE] = db.readTransaction(ASSETS_STORE) {
                objectStore(ASSETS_STORE).getAll()
            }
        }
        
        return json.encodeToString(exportData)
    }
    
    suspend fun importDatabase(data: String) {
        // Implementation for importing data
        val importData = json.decodeFromString<Map<String, List<Any>>>(data)
        // Process and store imported data
    }
    
    // Utility extension functions
    private fun Any.toJsObject(): dynamic {
        return JSON.parse(json.encodeToString(this))
    }
    
    private external fun jso(init: dynamic.() -> Unit): dynamic
    private external object JSON {
        fun stringify(obj: Any?): String
        fun parse(str: String): Any?
    }
}

// Local storage helper for quick access
object WasmLocalStorage {
    fun setItem(key: String, value: String) {
        window.localStorage.setItem(key, value)
    }
    
    fun getItem(key: String): String? {
        return window.localStorage.getItem(key)
    }
    
    fun removeItem(key: String) {
        window.localStorage.removeItem(key)
    }
    
    fun clear() {
        window.localStorage.clear()
    }
}
