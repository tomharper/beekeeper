// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/domain/model/ShotstackService.kt
package com.beekeeper.app.data.ai

/*
import com.beekeeper.app.domain.model.*
import com.beekeeper.app.domain.ai.interfaces.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class ShotstackService(
    private val apiKey: String,
    private val baseUrl: String = "https://api.shotstack.io"
) : VideoEditingService {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    override suspend fun stitchFrames(request: VideoStitchRequest): Result<VideoStitchResult> {
        return try {
            val timeline = createTimeline(request.frames)

            val response = client.post("$baseUrl/edit/stage/render") {
                header("x-api-key", apiKey)
                contentType(ContentType.Application.Json)
                setBody(ShotstackRenderRequest(
                    timeline = timeline,
                    output = OutputSettings(
                        format = request.outputFormat.lowercase(),
                        resolution = request.resolution,
                        fps = request.fps,
                        quality = request.quality
                    )
                ))
            }

            val renderResponse = response.body<ShotstackRenderResponse>()
            Result.success(VideoStitchResult(
                renderId = renderResponse.response.id,
                status = VideoStitchStatus.PROCESSING,
                videoUrl = null,
                duration = calculateTotalDuration(request.frames)
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getStitchStatus(renderId: String): Result<VideoStitchResult> {
        return try {
            val response = client.get("$baseUrl/edit/stage/render/$renderId") {
                header("x-api-key", apiKey)
            }

            val statusResponse = response.body<ShotstackStatusResponse>()
            val status = when (statusResponse.response.status) {
                "done" -> VideoStitchStatus.COMPLETED
                "rendering" -> VideoStitchStatus.PROCESSING
                "failed" -> VideoStitchStatus.FAILED
                else -> VideoStitchStatus.PENDING
            }

            Result.success(VideoStitchResult(
                renderId = renderId,
                status = status,
                videoUrl = statusResponse.response.url,
                duration = statusResponse.response.data?.duration ?: 0f
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun createTimeline(frames: List<FrameData>): Timeline {
        val tracks = mutableListOf<Track>()
        var currentTime = 0f

        // Video track
        val videoClips = frames.map { frame ->
            val clip = VideoClip(
                asset = AssetReference(src = frame.videoUrl),
                start = currentTime,
                length = frame.duration,
                fit = "cover"
            )
            currentTime += frame.duration
            clip
        }

        tracks.add(Track(clips = videoClips))

        // Audio track (if any frames have audio)
        val audioClips = frames.filter { it.audioUrl != null }.map { frame ->
            AudioClip(
                asset = AssetReference(src = frame.audioUrl!!),
                start = frame.startTime ?: 0f,
                length = frame.duration
            )
        }

        if (audioClips.isNotEmpty()) {
            tracks.add(Track(clips = audioClips))
        }

        return Timeline(tracks = tracks)
    }

    private fun calculateTotalDuration(frames: List<FrameData>): Float {
        return frames.sumOf { it.duration.toDouble() }.toFloat()
    }

    override fun getServiceName(): String = "Shotstack"
    override fun isAvailable(): Boolean = apiKey.isNotEmpty()
}

@Serializable
data class ShotstackRenderRequest(
    val timeline: Timeline,
    val output: OutputSettings
)

@Serializable
data class Timeline(
    val tracks: List<Track>
)

@Serializable
data class Track(
    val clips: List<Any> // Can be VideoClip, AudioClip, etc.
)

@Serializable
data class VideoClip(
    val asset: AssetReference,
    val start: Float,
    val length: Float,
    val fit: String = "cover"
)

@Serializable
data class AudioClip(
    val asset: AssetReference,
    val start: Float,
    val length: Float
)

@Serializable
data class AssetReference(
    val src: String
)

@Serializable
data class OutputSettings(
    val format: String,
    val resolution: String = "hd",
    val fps: Int = 24,
    val quality: String = "high"
)

@Serializable
data class ShotstackRenderResponse(
    val response: RenderResponse
)

@Serializable
data class RenderResponse(
    val id: String,
    val status: String
)

@Serializable
data class ShotstackStatusResponse(
    val response: StatusResponse
)

@Serializable
data class StatusResponse(
    val id: String,
    val status: String,
    val url: String? = null,
    val data: RenderData? = null
)

@Serializable
data class RenderData(
    val duration: Float? = null
)

 */