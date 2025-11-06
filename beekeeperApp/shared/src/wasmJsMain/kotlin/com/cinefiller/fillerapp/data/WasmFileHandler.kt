// File: shared/src/wasmJsMain/kotlin/com/cinefiller/fillerapp/data/WasmFileHandler.kt
package com.beekeeper.app.data

import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.await
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.w3c.dom.*
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import org.w3c.files.File
import org.w3c.files.FileReader
import kotlin.js.Promise

class WasmFileHandler {
    
    companion object {
        // Maximum file size for different asset types (in MB)
        const val MAX_IMAGE_SIZE = 50
        const val MAX_VIDEO_SIZE = 500
        const val MAX_AUDIO_SIZE = 100
        const val MAX_DOCUMENT_SIZE = 25
        
        // Supported file types
        val IMAGE_TYPES = setOf("image/jpeg", "image/png", "image/gif", "image/webp", "image/svg+xml")
        val VIDEO_TYPES = setOf("video/mp4", "video/webm", "video/ogg")
        val AUDIO_TYPES = setOf("audio/mpeg", "audio/wav", "audio/ogg", "audio/webm")
        val DOCUMENT_TYPES = setOf("application/pdf", "text/plain", "application/json")
    }
    
    // File upload handling
    suspend fun selectFiles(
        accept: String = "*/*",
        multiple: Boolean = false,
        onFileSelected: suspend (List<FileData>) -> Unit
    ) {
        val input = document.createElement("input") as HTMLInputElement
        input.type = "file"
        input.accept = accept
        input.multiple = multiple
        
        input.onchange = { event ->
            val files = input.files
            if (files != null && files.length > 0) {
                window.setTimeout({
                    processFiles(files, onFileSelected)
                }, 0)
            }
        }
        
        input.click()
    }
    
    private fun processFiles(fileList: FileList, onFileSelected: suspend (List<FileData>) -> Unit) {
        val files = mutableListOf<FileData>()
        
        for (i in 0 until fileList.length) {
            fileList.item(i)?.let { file ->
                if (validateFile(file)) {
                    window.setTimeout({
                        readFile(file) { fileData ->
                            files.add(fileData)
                            if (files.size == fileList.length) {
                                window.setTimeout({
                                    kotlinx.coroutines.GlobalScope.launch {
                                        onFileSelected(files)
                                    }
                                }, 0)
                            }
                        }
                    }, 0)
                }
            }
        }
    }
    
    private fun validateFile(file: File): Boolean {
        val maxSize = when {
            IMAGE_TYPES.contains(file.type) -> MAX_IMAGE_SIZE
            VIDEO_TYPES.contains(file.type) -> MAX_VIDEO_SIZE
            AUDIO_TYPES.contains(file.type) -> MAX_AUDIO_SIZE
            DOCUMENT_TYPES.contains(file.type) -> MAX_DOCUMENT_SIZE
            else -> 100 // Default max size
        } * 1024 * 1024 // Convert to bytes
        
        if (file.size > maxSize) {
            window.alert("File ${file.name} is too large. Maximum size is ${maxSize / 1024 / 1024}MB")
            return false
        }
        
        return true
    }
    
    private fun readFile(file: File, onComplete: (FileData) -> Unit) {
        val reader = FileReader()
        
        reader.onload = { event ->
            val result = reader.result
            when (result) {
                is ArrayBuffer -> {
                    val fileData = FileData(
                        name = file.name,
                        type = file.type,
                        size = file.size.toLong(),
                        data = result,
                        lastModified = file.lastModified.toLong()
                    )
                    onComplete(fileData)
                }
                is String -> {
                    // Handle text files
                    val encoder = js("new TextEncoder()")
                    val data = encoder.encode(result) as ArrayBuffer
                    val fileData = FileData(
                        name = file.name,
                        type = file.type,
                        size = file.size.toLong(),
                        data = data,
                        lastModified = file.lastModified.toLong()
                    )
                    onComplete(fileData)
                }
            }
        }
        
        if (file.type.startsWith("text/")) {
            reader.readAsText(file)
        } else {
            reader.readAsArrayBuffer(file)
        }
    }
    
    // File download/export handling
    suspend fun downloadFile(
        data: ByteArray,
        filename: String,
        mimeType: String = "application/octet-stream"
    ) {
        val blob = Blob(arrayOf(data), BlobPropertyBag(type = mimeType))
        downloadBlob(blob, filename)
    }
    
    suspend fun downloadText(
        text: String,
        filename: String,
        mimeType: String = "text/plain"
    ) {
        val blob = Blob(arrayOf(text), BlobPropertyBag(type = mimeType))
        downloadBlob(blob, filename)
    }
    
    suspend fun downloadJson(
        data: Any,
        filename: String
    ) {
        val jsonString = JSON.stringify(data)
        downloadText(jsonString, filename, "application/json")
    }
    
    private fun downloadBlob(blob: Blob, filename: String) {
        val url = URL.createObjectURL(blob)
        val link = document.createElement("a") as HTMLAnchorElement
        link.href = url
        link.download = filename
        
        document.body?.appendChild(link)
        link.click()
        document.body?.removeChild(link)
        
        // Clean up the URL object
        window.setTimeout({
            URL.revokeObjectURL(url)
        }, 100)
    }
    
    // Image handling utilities
    suspend fun resizeImage(
        fileData: FileData,
        maxWidth: Int,
        maxHeight: Int,
        quality: Double = 0.85
    ): FileData? {
        return try {
            val img = document.createElement("img") as HTMLImageElement
            val canvas = document.createElement("canvas") as HTMLCanvasElement
            val ctx = canvas.getContext("2d") as CanvasRenderingContext2D
            
            // Create object URL for the image
            val blob = Blob(arrayOf(fileData.data), BlobPropertyBag(type = fileData.type))
            val url = URL.createObjectURL(blob)
            
            // Wait for image to load
            val loadPromise = Promise<Unit> { resolve, _ ->
                img.onload = {
                    resolve(Unit)
                }
            }
            
            img.src = url
            loadPromise.await()
            
            // Calculate new dimensions
            var width = img.naturalWidth
            var height = img.naturalHeight
            
            if (width > maxWidth || height > maxHeight) {
                val ratio = minOf(
                    maxWidth.toDouble() / width,
                    maxHeight.toDouble() / height
                )
                width = (width * ratio).toInt()
                height = (height * ratio).toInt()
            }
            
            // Resize the image
            canvas.width = width
            canvas.height = height
            ctx.drawImage(img, 0.0, 0.0, width.toDouble(), height.toDouble())
            
            // Convert to blob
            val resizedBlob = Promise<Blob> { resolve, _ ->
                canvas.toBlob({ blob ->
                    blob?.let { resolve(it) }
                }, fileData.type, quality)
            }.await()
            
            // Convert blob to ArrayBuffer
            val arrayBuffer = resizedBlob.arrayBuffer().await()
            
            // Clean up
            URL.revokeObjectURL(url)
            
            FileData(
                name = fileData.name,
                type = fileData.type,
                size = arrayBuffer.byteLength.toLong(),
                data = arrayBuffer,
                lastModified = js("Date.now()") as Long
            )
        } catch (e: Exception) {
            console.error("Error resizing image: ${e.message}")
            null
        }
    }
    
    // Video thumbnail extraction
    suspend fun extractVideoThumbnail(
        fileData: FileData,
        timeSeconds: Double = 1.0
    ): FileData? {
        return try {
            val video = document.createElement("video") as HTMLVideoElement
            val canvas = document.createElement("canvas") as HTMLCanvasElement
            val ctx = canvas.getContext("2d") as CanvasRenderingContext2D
            
            // Create object URL for the video
            val blob = Blob(arrayOf(fileData.data), BlobPropertyBag(type = fileData.type))
            val url = URL.createObjectURL(blob)
            
            // Set up video
            video.src = url
            video.currentTime = timeSeconds
            
            // Wait for video to seek
            val seekPromise = Promise<Unit> { resolve, _ ->
                video.onseeked = {
                    resolve(Unit)
                }
            }
            
            seekPromise.await()
            
            // Draw video frame to canvas
            canvas.width = video.videoWidth
            canvas.height = video.videoHeight
            ctx.drawImage(video, 0.0, 0.0)
            
            // Convert to blob
            val thumbnailBlob = Promise<Blob> { resolve, _ ->
                canvas.toBlob({ blob ->
                    blob?.let { resolve(it) }
                }, "image/jpeg", 0.85)
            }.await()
            
            // Convert blob to ArrayBuffer
            val arrayBuffer = thumbnailBlob.arrayBuffer().await()
            
            // Clean up
            URL.revokeObjectURL(url)
            
            FileData(
                name = "${fileData.name}_thumbnail.jpg",
                type = "image/jpeg",
                size = arrayBuffer.byteLength.toLong(),
                data = arrayBuffer,
                lastModified = js("Date.now()") as Long
            )
        } catch (e: Exception) {
            console.error("Error extracting thumbnail: ${e.message}")
            null
        }
    }
    
    // Drag and drop support
    fun setupDropZone(
        element: Element,
        onDrop: suspend (List<FileData>) -> Unit
    ) {
        element.addEventListener("dragover", { event ->
            event.preventDefault()
            event.stopPropagation()
            element.classList.add("drag-over")
        })
        
        element.addEventListener("dragleave", { event ->
            event.preventDefault()
            event.stopPropagation()
            element.classList.remove("drag-over")
        })
        
        element.addEventListener("drop", { event ->
            event.preventDefault()
            event.stopPropagation()
            element.classList.remove("drag-over")
            
            val dataTransfer = (event as DragEvent).dataTransfer
            dataTransfer?.files?.let { files ->
                processFiles(files, onDrop)
            }
        })
    }
}

data class FileData(
    val name: String,
    val type: String,
    val size: Long,
    val data: ArrayBuffer,
    val lastModified: Long
) {
    fun toByteArray(): ByteArray {
        return Uint8Array(data).unsafeCast<ByteArray>()
    }
    
    fun toBase64(): String {
        val bytes = Uint8Array(data)
        val binaryString = buildString {
            for (i in 0 until bytes.length) {
                append(bytes[i].toInt().toChar())
            }
        }
        return window.btoa(binaryString)
    }
}

// Extension functions
external fun HTMLCanvasElement.toBlob(
    callback: (Blob?) -> Unit,
    type: String = definedExternally,
    quality: Double = definedExternally
)

external fun Blob.arrayBuffer(): Promise<ArrayBuffer>
