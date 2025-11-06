// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/domain/repository/FrameRepository.kt
package com.beekeeper.app.domain.repository

import com.beekeeper.app.domain.model.Frame
import kotlinx.coroutines.flow.Flow

interface FrameRepository {
    suspend fun getFrameById(frameId: String): Frame?
    suspend fun getFramesBySceneId(sceneId: String): List<Frame>
    suspend fun getFramesByStoryboardId(storyboardId: String): List<Frame>
    suspend fun addFrame(frame: Frame)
    suspend fun updateFrame(frame: Frame)
    suspend fun deleteFrame(frameId: String)
    suspend fun deleteFramesBySceneId(sceneId: String)
    fun observeFramesBySceneId(sceneId: String): Flow<List<Frame>>
    fun observeFrame(frameId: String): Flow<Frame?>
}
