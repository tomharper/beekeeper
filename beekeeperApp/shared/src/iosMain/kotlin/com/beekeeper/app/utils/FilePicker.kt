// -----------------------------------------------------------
// File: shared/src/iosMain/kotlin/com/cinefiller/fillerapp/utils/FilePicker.kt
package com.beekeeper.app.utils

import kotlinx.cinterop.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import platform.Foundation.*
import platform.UIKit.*
import platform.Photos.*
import platform.UniformTypeIdentifiers.*
import platform.darwin.NSObject
import kotlin.coroutines.resume

actual class FilePicker(private val viewController: UIViewController) {
    
    actual suspend fun pickImage(config: FilePickerConfig): PickedFile? {
        return pickMedia(
            mediaTypes = listOf(UTTypeImage.identifier),
            config = config
        )
    }
    
    actual suspend fun pickImages(config: FilePickerConfig): List<PickedFile> {
        // iOS doesn't support multiple selection in UIImagePickerController
        // You'd need to use PHPickerViewController for iOS 14+
        val singleImage = pickImage(config)
        return if (singleImage != null) listOf(singleImage) else emptyList()
    }
    
    actual suspend fun pickVideo(config: FilePickerConfig): PickedFile? {
        return pickMedia(
            mediaTypes = listOf(UTTypeMovie.identifier),
            config = config
        )
    }
    
    actual suspend fun pickAudio(config: FilePickerConfig): PickedFile? {
        return pickFile(
            mimeTypes = listOf("public.audio"),
            config = config
        )
    }
    
    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun pickFile(
        mimeTypes: List<String>,
        config: FilePickerConfig
    ): PickedFile? = suspendCancellableCoroutine { continuation ->
        val documentPicker = UIDocumentPickerViewController(
            forOpeningContentTypes = listOf(UTTypeData)
        )

        val delegate = object : NSObject(), UIDocumentPickerDelegateProtocol {
            override fun documentPicker(
                controller: UIDocumentPickerViewController,
                didPickDocumentsAtURLs: List<*>
            ) {
                val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL
                if (url != null) {
                    val file = readFileFromURL(url, config)
                    continuation.resume(file)
                } else {
                    continuation.resume(null)
                }
                controller.dismissViewControllerAnimated(true, null)
            }

            override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
                continuation.resume(null)
                controller.dismissViewControllerAnimated(true, null)
            }
        }

        documentPicker.delegate = delegate
        viewController.presentViewController(documentPicker, true, null)
    }
    
    @OptIn(ExperimentalForeignApi::class)
    private suspend fun pickMedia(
        mediaTypes: List<String>,
        config: FilePickerConfig
    ): PickedFile? = suspendCancellableCoroutine { continuation ->
        val delegate = object : NSObject(), UIImagePickerControllerDelegateProtocol,
            UINavigationControllerDelegateProtocol {

            override fun imagePickerController(
                picker: UIImagePickerController,
                didFinishPickingMediaWithInfo: Map<Any?, *>
            ) {
                var pickedFile: PickedFile? = null

                // Handle image
                val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
                if (image != null) {
                    val data = UIImageJPEGRepresentation(image, 0.8) ?: UIImagePNGRepresentation(image)
                    data?.let {
                        pickedFile = PickedFile(
                            name = "image.jpg",
                            bytes = it.toByteArray(),
                            mimeType = "image/jpeg",
                            extension = "jpg"
                        )
                    }
                }

                // Handle video
                val videoURL = didFinishPickingMediaWithInfo[UIImagePickerControllerMediaURL] as? NSURL
                if (videoURL != null) {
                    pickedFile = readFileFromURL(videoURL, config)
                }

                continuation.resume(pickedFile?.takeIf {
                    it.validate(config) is ValidationResult.Success
                })
                picker.dismissViewControllerAnimated(true, null)
            }

            override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
                continuation.resume(null)
                picker.dismissViewControllerAnimated(true, null)
            }
        }

        val imagePicker = UIImagePickerController().apply {
            sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
            this.mediaTypes = mediaTypes
            this.delegate = delegate
        }

        viewController.presentViewController(imagePicker, true, null)
    }
    
    private fun readFileFromURL(url: NSURL, config: FilePickerConfig): PickedFile? {
        return try {
            val data = NSData.dataWithContentsOfURL(url) ?: return null
            val filename = url.lastPathComponent ?: "file"
            val pathExtension = url.pathExtension ?: "bin"

            // Simple MIME type mapping
            val mimeType = when (pathExtension.lowercase()) {
                "jpg", "jpeg" -> "image/jpeg"
                "png" -> "image/png"
                "gif" -> "image/gif"
                "mp4" -> "video/mp4"
                "mov" -> "video/quicktime"
                "mp3" -> "audio/mpeg"
                "wav" -> "audio/wav"
                "pdf" -> "application/pdf"
                else -> "application/octet-stream"
            }

            PickedFile(
                name = filename,
                bytes = data.toByteArray(),
                mimeType = mimeType,
                extension = pathExtension
            )
        } catch (e: Exception) {
            null
        }
    }
}

// Extension to convert NSData to ByteArray
@OptIn(ExperimentalForeignApi::class)
fun NSData.toByteArray(): ByteArray {
    return ByteArray(length.toInt()).apply {
        usePinned {
            platform.posix.memcpy(it.addressOf(0), bytes, length.toULong())
        }
    }
}

