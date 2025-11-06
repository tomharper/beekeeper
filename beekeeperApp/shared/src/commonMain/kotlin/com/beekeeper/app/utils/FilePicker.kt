// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/utils/FilePicker.kt
package com.beekeeper.app.utils

/**
 * Data class representing a picked file
 */
data class PickedFile(
    val name: String,
    val bytes: ByteArray,
    val mimeType: String,
    val extension: String
) {
    val sizeInBytes: Long get() = bytes.size.toLong()
    val sizeInMB: Float get() = sizeInBytes / (1024f * 1024f)
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as PickedFile
        return name == other.name && bytes.contentEquals(other.bytes)
    }
    
    override fun hashCode(): Int {
        return 31 * name.hashCode() + bytes.contentHashCode()
    }
}

/**
 * File picker configuration
 */
data class FilePickerConfig(
    val allowMultiple: Boolean = false,
    val maxFileSize: Long = 100 * 1024 * 1024, // 100MB default
    val allowedMimeTypes: List<String> = emptyList()
)

/**
 * Multiplatform file picker interface
 */
expect class FilePicker {
    suspend fun pickImage(config: FilePickerConfig): PickedFile?
    suspend fun pickImages(config: FilePickerConfig = FilePickerConfig()): List<PickedFile>
    suspend fun pickVideo(config: FilePickerConfig = FilePickerConfig()): PickedFile?
    suspend fun pickAudio(config: FilePickerConfig = FilePickerConfig()): PickedFile?
    suspend fun pickFile(mimeTypes: List<String>, config: FilePickerConfig = FilePickerConfig()): PickedFile?
}

// Utility functions for file validation
fun PickedFile.isImage(): Boolean {
    return mimeType.startsWith("image/") || 
           extension in listOf("jpg", "jpeg", "png", "gif", "webp", "bmp")
}

fun PickedFile.isVideo(): Boolean {
    return mimeType.startsWith("video/") || 
           extension in listOf("mp4", "mov", "avi", "mkv", "webm")
}

fun PickedFile.isAudio(): Boolean {
    return mimeType.startsWith("audio/") || 
           extension in listOf("mp3", "wav", "aac", "m4a", "flac")
}

fun PickedFile.validate(config: FilePickerConfig): ValidationResult {
    return when {
        sizeInBytes > config.maxFileSize -> {
            ValidationResult.Error("File size exceeds ${config.maxFileSize / (1024 * 1024)}MB limit")
        }
        config.allowedMimeTypes.isNotEmpty() && mimeType !in config.allowedMimeTypes -> {
            ValidationResult.Error("File type $mimeType is not allowed")
        }
        else -> ValidationResult.Success
    }
}

sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}

