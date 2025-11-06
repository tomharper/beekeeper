package com.beekeeper.app.data

import android.content.Context
import io.github.aakira.napier.Napier
import io.objectbox.BoxStore
import io.objectbox.model.ExternalPropertyType.Json

/**
 * ObjectBox database initialization and management
 * Singleton pattern for cross-platform database access
 */
class ObjectBoxDatabase {
    private var _boxStore: BoxStore? = null
    private var _isInitialized = false

    /**
     * Safe boxStore access with proper initialization check
     */
    val boxStore: BoxStore
        get() {
            if (_boxStore == null || !_isInitialized) {
                throw IllegalStateException("ObjectBox database not initialized. Call initialize() first.")
            }
            return _boxStore!!
        }

    /**
     * Check if database is ready to use
     */
    val isInitialized: Boolean
        get() = _isInitialized && _boxStore != null

    /**
     * Proper initialization with fallback for missing generated classes
     */
    fun initialize(context: Any? = null): Boolean {
        return try {
            _boxStore = createBoxStore(context)
            _isInitialized = true
            logDatabaseStats()
            true
        } catch (e: Exception) {
            _isInitialized = false
            _boxStore = null
            false
        }
    }

    /**
     * Create BoxStore based on platform
     */
    private fun createBoxStore(context: Any?): BoxStore {
        return when {
            // Android platform
            context != null -> {
                MyObjectBox.builder()
                    .androidContext(context as Context)
                    .build()
            }
            // Desktop/JVM platform
            else -> {
                MyObjectBox.builder()
                    .name("cinefiller-objectbox-db")
                    .build()
            }
        }
    }

    /**
     * Log database statistics
     */
    private fun logDatabaseStats() {
        try {
            _boxStore?.let { store ->
                Napier.i { "Database size: ${store.sizeOnDisk() / 1024}KB" }
            }
        } catch (e: Exception) {
            Napier.e { "Error logging database stats: ${e.message}" }
        }
    }

    /**
     * Close the database
     */
    fun close() {
        try {
            _boxStore?.close()
            _boxStore = null
            _isInitialized = false
            Napier.i { "ObjectBox database closed" }
        } catch (e: Exception) {
            Napier.e { "Error closing database: ${e.message}" }
        }
    }

    /**
     * Clear all data (use with caution!)
     */
    fun clearAllData() {
        if (!isInitialized) {
            throw IllegalStateException("Database not initialized")
        }

        try {
            _boxStore?.removeAllObjects()
            Napier.w { "All data cleared from ObjectBox database" }
        } catch (e: Exception) {
            Napier.e { "Error clearing data: ${e.message}" }
            throw e
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: ObjectBoxDatabase? = null

        /**
         * Get singleton instance
         */
        fun getInstance(): ObjectBoxDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ObjectBoxDatabase().also { INSTANCE = it }
            }
        }
    }
}