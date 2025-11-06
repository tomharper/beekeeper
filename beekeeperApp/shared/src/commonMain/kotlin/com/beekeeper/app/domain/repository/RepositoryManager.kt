// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/domain/repository/RepositoryManager.kt
package com.beekeeper.app.domain.repository

import com.beekeeper.app.config.FeatureFlags
import com.beekeeper.app.data.api.ApiConfig
import com.beekeeper.app.data.api.ApiService
import com.beekeeper.app.domain.repository.*
import com.beekeeper.app.domain.auth.AuthManager
import com.beekeeper.app.domain.auth.TokenStorage
import com.beekeeper.app.presentation.viewmodels.AuthViewModel

/**
 * Centralized repository management for dependency injection
 * Provides singleton instances of all repositories
 * ALL repositories use models from domain.model package
 *
 * Supports multiple backends via FeatureFlags:
 * - SQLDelight (recommended, cross-platform)
 * - ObjectBox (legacy, Android only)
 * - In-memory (no persistence)
 */
object RepositoryManager {

    /**
     * Token storage instance (platform-specific)
     * Must be initialized from platform code
     */
    private var tokenStorage: TokenStorage? = null

    /**
     * Shared API service instance
     */
    private val apiService: ApiService by lazy {
        ApiService()
    }

    /**
     * Auth manager instance
     */
    val authManager: AuthManager by lazy {
        if (tokenStorage == null) {
            throw IllegalStateException("TokenStorage must be initialized before using AuthManager. Call RepositoryManager.initializeTokenStorage() from platform code.")
        }
        AuthManager(tokenStorage!!)
    }

    /**
     * Auth view model instance
     */
    val authViewModel: AuthViewModel by lazy {
        AuthViewModel(authManager, apiService)
    }

    /**
     * Initialize token storage (must be called from platform-specific code)
     */
    fun initializeTokenStorage(storage: TokenStorage) {
        tokenStorage = storage
        println("🔐 Token storage initialized")
    }

    /**
     * ObjectBox repository instance (only created when needed)
     */
    private var objectBoxRepository: ObjectBoxRepository? = null

    /**
     * SQLDelight repository instance (only created when needed)
     */
    private var sqlDelightFactoryRepository: SQLDelightProjectFactoryRepository? = null

    /**
     * SQLDelight database instance (for accessing all queries)
     */
    private var sqlDelightDatabase: com.beekeeper.app.data.sqldelight.CineFillerDatabase? = null

    /**
     * Initialize ObjectBox repository
     * Must be called from platform-specific code (Android MainActivity, etc.)
     */
    fun initializeObjectBox(repository: ObjectBoxRepository) {
        objectBoxRepository = repository
        println("📦 ObjectBox repository initialized")
    }

    /**
     * Initialize SQLDelight repository
     * Must be called from platform-specific code (Android MainActivity, etc.)
     */
    fun initializeSQLDelight(repository: SQLDelightProjectFactoryRepository) {
        sqlDelightFactoryRepository = repository
        // Extract database from repository - we'll need to pass it separately
        println("💾 SQLDelight repository initialized")
    }

    /**
     * Initialize SQLDelight with both factory repository and database
     * Must be called from platform-specific code (Android MainActivity, etc.)
     */
    fun initializeSQLDelight(repository: SQLDelightProjectFactoryRepository, database: com.beekeeper.app.data.sqldelight.CineFillerDatabase) {
        sqlDelightFactoryRepository = repository
        sqlDelightDatabase = database
        println("💾 SQLDelight repository and database initialized")
    }

    /**
     * Get the ObjectBox repository (if initialized)
     */
    fun getObjectBoxRepository(): ObjectBoxRepository? = objectBoxRepository

    /**
     * Get the SQLDelight repository (if initialized)
     */
    fun getSQLDelightRepository(): SQLDelightProjectFactoryRepository? = sqlDelightFactoryRepository

    // Repository implementations - switch based on FeatureFlags
    val contentRepository: ContentRepository by lazy {
        if (ApiConfig.offlineMode) {
            // Offline mode - use SQLDelight or in-memory
            if (FeatureFlags.useSQLDelight && sqlDelightFactoryRepository != null && sqlDelightDatabase != null) {
                println("💾 Using SQLDelight-backed ContentRepository (offline mode)")
                SQLDelightContentRepositoryImpl(sqlDelightFactoryRepository!!, sqlDelightDatabase!!)
            } else {
                println("📝 Using in-memory ContentRepository (offline mode)")
                ContentRepositoryImpl()
            }
        } else {
            // Online mode - use SQLDelight-backed repository if available (for stories/scripts)
            if (FeatureFlags.useSQLDelight && sqlDelightFactoryRepository != null && sqlDelightDatabase != null) {
                println("💾 Using SQLDelight-backed ContentRepository (online mode with database)")
                SQLDelightContentRepositoryImpl(sqlDelightFactoryRepository!!, sqlDelightDatabase!!)
            } else {
                println("🌐 Using in-memory ContentRepository (online mode)")
                ContentRepositoryImpl()
            }
        }
    }

    val characterRepository: CharacterRepository by lazy {
        if (ApiConfig.offlineMode) {
            // Offline mode - use SQLDelight or in-memory
            if (FeatureFlags.useSQLDelight && sqlDelightFactoryRepository != null) {
                println("💾 Using SQLDelight-backed CharacterRepository (offline mode)")
                SQLDelightCharacterRepositoryImpl(sqlDelightFactoryRepository!!)
            } else {
                println("📝 Using in-memory CharacterRepository (offline mode)")
                CharacterRepositoryImpl()
            }
        } else {
            // Online mode - use SQLDelight-backed repository if available (for characters from factories)
            if (FeatureFlags.useSQLDelight && sqlDelightFactoryRepository != null) {
                println("💾 Using SQLDelight-backed CharacterRepository (online mode with database)")
                SQLDelightCharacterRepositoryImpl(sqlDelightFactoryRepository!!)
            } else {
                println("🌐 Using in-memory CharacterRepository (online mode)")
                CharacterRepositoryImpl()
            }
        }
    }

    val projectRepository: ProjectRepository by lazy {
        if (ApiConfig.offlineMode) {
            // Offline mode - use SQLDelight or in-memory
            if (FeatureFlags.useSQLDelight && sqlDelightFactoryRepository != null) {
                println("💾 Using SQLDelight-backed ProjectRepository (offline mode)")
                SQLDelightProjectRepositoryImpl(sqlDelightFactoryRepository!!)
            } else {
                println("📝 Using in-memory ProjectRepository (offline mode)")
                ProjectRepositoryImpl(apiService = apiService)
            }
        } else {
            // Online mode - use API-backed repository with optional database persistence
            if (sqlDelightFactoryRepository != null) {
                println("🌐 Using API-backed ProjectRepository with database persistence (online mode)")
                ProjectRepositoryImpl(apiService = apiService, databaseRepository = sqlDelightFactoryRepository)
            } else {
                println("🌐 Using API-backed ProjectRepository in-memory only (online mode)")
                ProjectRepositoryImpl(apiService = apiService)
            }
        }
    }

    val scriptRepository: ScriptRepository by lazy {
        ScriptRepositoryImpl()
    }

    val storyPatternsRepository: StoryPatternsRepository by lazy {
        StoryPatternsRepositoryImpl()
    }

    val distributionRepository: DistributionRepository by lazy {
        if (FeatureFlags.useSQLDelight && sqlDelightDatabase != null) {
            println("💾 Using SQLDelight-backed DistributionRepository")
            SQLDelightDistributionRepositoryImpl(sqlDelightDatabase!!)
        } else {
            println("📝 Using in-memory DistributionRepository")
            DistributionRepositoryImpl()
        }
    }

    val feedRepository: SQLDelightFeedRepository? by lazy {
        if (FeatureFlags.useSQLDelight && sqlDelightDatabase != null) {
            println("💾 Using SQLDelight-backed FeedRepository")
            SQLDelightFeedRepository(sqlDelightDatabase!!)
        } else {
            println("📝 Feed will use in-memory only (no persistence)")
            null
        }
    }

    /**
     * Reset repositories (useful for testing)
     */
    fun reset() {
        println("⚠️ Repository reset requested - restart app to apply")
    }
}