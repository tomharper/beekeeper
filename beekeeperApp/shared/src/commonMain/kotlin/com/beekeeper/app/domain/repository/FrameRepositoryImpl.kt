// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/domain/repository/FrameRepositoryImpl.kt
package com.beekeeper.app.domain.repository

import com.beekeeper.app.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class FrameRepositoryImpl : FrameRepository {
    
    // In-memory storage for demo purposes
    private val framesMap = mutableMapOf<String, Frame>()
    private val framesFlow = MutableStateFlow<Map<String, Frame>>(emptyMap())
    
    init {
        // Initialize with sample data
        initializeSampleFrames()
    }
    
    override suspend fun getFrameById(frameId: String): Frame? {
        return framesMap[frameId]
    }
    
    override suspend fun getFramesBySceneId(sceneId: String): List<Frame> {
        return framesMap.values
            .filter { it.sceneId == sceneId }
            .sortedBy { it.frameNumber }
    }
    
    override suspend fun getFramesByStoryboardId(storyboardId: String): List<Frame> {
        // This would typically join with scenes table
        // For now, filter by a pattern in the frame ID
        return framesMap.values
            .filter { it.id.contains(storyboardId) }
            .sortedBy { it.frameNumber }
    }
    
    override suspend fun addFrame(frame: Frame) {
        framesMap[frame.id] = frame
        framesFlow.value = framesMap.toMap()
    }
    
    override suspend fun updateFrame(frame: Frame) {
        framesMap[frame.id] = frame
        framesFlow.value = framesMap.toMap()
    }
    
    override suspend fun deleteFrame(frameId: String) {
        framesMap.remove(frameId)
        framesFlow.value = framesMap.toMap()
    }
    
    override suspend fun deleteFramesBySceneId(sceneId: String) {
        val keysToRemove = framesMap.entries
            .filter { it.value.sceneId == sceneId }
            .map { it.key }
        
        keysToRemove.forEach { framesMap.remove(it) }
        framesFlow.value = framesMap.toMap()
    }
    
    override fun observeFramesBySceneId(sceneId: String): Flow<List<Frame>> {
        return framesFlow.map { frames ->
            frames.values
                .filter { it.sceneId == sceneId }
                .sortedBy { it.frameNumber }
        }
    }
    
    override fun observeFrame(frameId: String): Flow<Frame?> {
        return framesFlow.map { frames ->
            frames[frameId]
        }
    }
    
    private fun initializeSampleFrames() {
        // Sample frames for scene_lb_001
        val sampleFrames = listOf(
            Frame(
                id = "frame_scene_lb_001_1",
                frameNumber = 1,
                sceneId = "scene_lb_001",
                description = "Wide establishing shot of the abandoned warehouse exterior at dusk",
                timestamp = "00:00",
                shotType = ShotType.WIDE_SHOT,
                cameraAngle = CameraAngle.EYE_LEVEL,
                cameraMovement = CameraMovement.STATIC,
                duration = 3.0f,
                transitionIn = TransitionType.FADE_IN,
                transitionOut = TransitionType.CUT,
                dialogueLineId = null
            ),
            Frame(
                id = "frame_scene_lb_001_2",
                frameNumber = 2,
                sceneId = "scene_lb_001",
                description = "Medium shot of protagonist approaching the entrance",
                timestamp = "00:03",
                shotType = ShotType.MEDIUM_SHOT,
                cameraAngle = CameraAngle.LOW_ANGLE,
                cameraMovement = CameraMovement.DOLLY_IN,
                duration = 2.5f,
                transitionIn = TransitionType.CUT,
                transitionOut = TransitionType.CUT,
                dialogueLineId = null
            ),
            Frame(
                id = "frame_scene_lb_001_3",
                frameNumber = 3,
                sceneId = "scene_lb_001",
                description = "Close-up of protagonist's hand reaching for the door handle",
                timestamp = "00:05",
                shotType = ShotType.CLOSE_UP,
                cameraAngle = CameraAngle.EYE_LEVEL,
                cameraMovement = CameraMovement.STATIC,
                duration = 2.0f,
                transitionIn = TransitionType.CUT,
                transitionOut = TransitionType.CUT,
                dialogueLineId = null
            ),
            Frame(
                id = "frame_scene_lb_001_4",
                frameNumber = 4,
                sceneId = "scene_lb_001",
                description = "Over-the-shoulder shot as door creaks open",
                timestamp = "00:07",
                shotType = ShotType.OVER_THE_SHOULDER,
                cameraAngle = CameraAngle.EYE_LEVEL,
                cameraMovement = CameraMovement.PAN_RIGHT,
                duration = 3.0f,
                transitionIn = TransitionType.CUT,
                transitionOut = TransitionType.CUT,
                dialogueLineId = null
            ),
            Frame(
                id = "frame_scene_lb_001_5",
                frameNumber = 5,
                sceneId = "scene_lb_001",
                description = "POV shot entering the dark warehouse interior",
                timestamp = "00:10",
                shotType = ShotType.POV_SHOT,
                cameraAngle = CameraAngle.EYE_LEVEL,
                cameraMovement = CameraMovement.HANDHELD,
                duration = 4.0f,
                transitionIn = TransitionType.CUT,
                transitionOut = TransitionType.CUT,
                dialogueLineId = null
            ),
            Frame(
                id = "frame_scene_lb_001_6",
                frameNumber = 6,
                sceneId = "scene_lb_001",
                description = "Extreme close-up of protagonist's eyes widening in fear",
                timestamp = "00:14",
                shotType = ShotType.EXTREME_CLOSE_UP,
                cameraAngle = CameraAngle.EYE_LEVEL,
                cameraMovement = CameraMovement.STATIC,
                duration = 2.0f,
                transitionIn = TransitionType.CUT,
                transitionOut = TransitionType.FADE_OUT,
                dialogueLineId = null
            )
        )
        
        // Add sample frames to repository
        sampleFrames.forEach { frame ->
            framesMap[frame.id] = frame
        }
        
        // Add frames for other sample scenes
        val otherScenes = listOf("scene_lb_002", "scene_lb_003", "scene_lb_004")
        otherScenes.forEach { sceneId ->
            repeat(4) { index ->
                val minutes = index * 2 / 60
                val seconds = (index * 2) % 60
                val frame = Frame(
                    id = "frame_${sceneId}_${index + 1}",
                    frameNumber = index + 1,
                    sceneId = sceneId,
                    description = "Frame ${index + 1} for $sceneId",
                    timestamp = "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}",
                    shotType = ShotType.values().random(),
                    cameraAngle = CameraAngle.values().random(),
                    cameraMovement = CameraMovement.values().random(),
                    duration = 2.0f,
                    transitionIn = TransitionType.CUT,
                    transitionOut = TransitionType.CUT,
                    dialogueLineId = null
                )
                framesMap[frame.id] = frame
            }
        }
        
        framesFlow.value = framesMap.toMap()
    }
}
