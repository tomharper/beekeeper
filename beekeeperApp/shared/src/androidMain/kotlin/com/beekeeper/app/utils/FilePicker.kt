
// -----------------------------------------------------------
// File: shared/src/androidMain/kotlin/com/cinefiller/fillerapp/utils/FilePicker.kt
package com.beekeeper.app.utils

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.coroutines.resume

actual class FilePicker(private val activity: ComponentActivity) {
    
    actual suspend fun pickImage(config: FilePickerConfig): PickedFile? {
        return pickFile(listOf("image/*"), config)
    }
    
    actual suspend fun pickImages(config: FilePickerConfig): List<PickedFile> {
        return suspendCancellableCoroutine { continuation ->
            val launcher = activity.registerForActivityResult(
                ActivityResultContracts.GetMultipleContents()
            ) { uris ->
                if (uris.isNotEmpty()) {
                    val files = uris.mapNotNull { uri ->
                        readFileFromUri(activity, uri, config)
                    }
                    continuation.resume(files)
                } else {
                    continuation.resume(emptyList())
                }
            }
            launcher.launch("image/*")
        }
    }
    
    actual suspend fun pickVideo(config: FilePickerConfig): PickedFile? {
        return pickFile(listOf("video/*"), config)
    }
    
    actual suspend fun pickAudio(config: FilePickerConfig): PickedFile? {
        return pickFile(listOf("audio/*"), config)
    }
    
    actual suspend fun pickFile(
        mimeTypes: List<String>,
        config: FilePickerConfig
    ): PickedFile? = suspendCancellableCoroutine { continuation ->
        val launcher = activity.registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            if (uri != null) {
                val file = readFileFromUri(activity, uri, config)
                continuation.resume(file)
            } else {
                continuation.resume(null)
            }
        }
        
        // Android only supports single mime type in GetContent
        launcher.launch(mimeTypes.firstOrNull() ?: "*/*")
    }
    
    private fun readFileFromUri(
        context: Context,
        uri: Uri,
        config: FilePickerConfig
    ): PickedFile? {
        return try {
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"
            
            // Get filename
            val filename = uri.lastPathSegment ?: "file"
            val extension = MimeTypeMap.getSingleton()
                .getExtensionFromMimeType(mimeType) ?: "bin"
            
            // Read file bytes
            val bytes = contentResolver.openInputStream(uri)?.use { input ->
                ByteArrayOutputStream().apply {
                    input.copyTo(this)
                }.toByteArray()
            } ?: return null
            
            val pickedFile = PickedFile(
                name = filename,
                bytes = bytes,
                mimeType = mimeType,
                extension = extension
            )
            
            // Validate file
            when (val result = pickedFile.validate(config)) {
                is ValidationResult.Success -> pickedFile
                is ValidationResult.Error -> {
                    // You might want to show an error to the user
                    null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

