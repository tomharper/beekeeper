// File: shared/src/wasmJsMain/kotlin/com/cinefiller/fillerapp/ui/WasmImageLoader.kt
package com.beekeeper.app.ui

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.browser.document
import kotlinx.coroutines.await
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLImageElement
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import kotlin.js.Promise

/**
 * WASM image loader implementation to replace Kamel
 */
@Composable
fun rememberAsyncImagePainter(url: String): ImageLoadingState {
    var state by remember { mutableStateOf<ImageLoadingState>(ImageLoadingState.Loading) }
    
    LaunchedEffect(url) {
        state = ImageLoadingState.Loading
        loadImage(url)
            .onSuccess { bitmap ->
                state = ImageLoadingState.Success(bitmap)
            }
            .onFailure { error ->
                state = ImageLoadingState.Error(error)
            }
    }
    
    return state
}

sealed class ImageLoadingState {
    object Loading : ImageLoadingState()
    data class Success(val bitmap: ImageBitmap) : ImageLoadingState()
    data class Error(val throwable: Throwable) : ImageLoadingState()
}

private suspend fun loadImage(url: String): Result<ImageBitmap> {
    return try {
        val img = document.createElement("img") as HTMLImageElement
        
        val loadPromise = Promise<Unit> { resolve, reject ->
            img.onload = {
                resolve(Unit)
            }
            img.onerror = { _, _, _, _, _ ->
                reject(Exception("Failed to load image"))
            }
        }
        
        img.src = url
        loadPromise.await()
        
        // Create canvas and draw image
        val canvas = document.createElement("canvas") as HTMLCanvasElement
        canvas.width = img.naturalWidth
        canvas.height = img.naturalHeight
        
        val ctx = canvas.getContext("2d")
        ctx?.drawImage(img, 0.0, 0.0)
        
        // Convert to ImageBitmap
        val imageData = ctx?.getImageData(0.0, 0.0, canvas.width.toDouble(), canvas.height.toDouble())
        if (imageData != null) {
            val bitmap = createImageBitmap(
                width = canvas.width,
                height = canvas.height,
                data = imageData.data
            )
            Result.success(bitmap)
        } else {
            Result.failure(Exception("Failed to get image data"))
        }
    } catch (e: Throwable) {
        Result.failure(e)
    }
}

private fun createImageBitmap(width: Int, height: Int, data: Uint8Array): ImageBitmap {
    // Convert Uint8Array to ByteArray
    val byteArray = ByteArray(data.length)
    for (i in 0 until data.length) {
        byteArray[i] = data[i].toByte()
    }
    
    return org.jetbrains.skia.Bitmap().apply {
        allocN32Pixels(width, height)
        val buffer = this.peekPixels()?.buffer
        if (buffer != null) {
            // Copy RGBA data
            for (i in byteArray.indices) {
                buffer.put(i, byteArray[i])
            }
        }
    }.toComposeImageBitmap()
}

// Extension function for Canvas 2D Context
private external interface CanvasRenderingContext2D {
    fun drawImage(image: HTMLImageElement, dx: Double, dy: Double)
    fun getImageData(sx: Double, sy: Double, sw: Double, swidth: Double): ImageData
}

private external interface ImageData {
    val data: Uint8Array
    val width: Int
    val height: Int
}

private fun HTMLCanvasElement.getContext(contextId: String): CanvasRenderingContext2D? =
    asDynamic().getContext(contextId) as? CanvasRenderingContext2D
