package com.beekeeper.app.utils

actual class FilePicker {
    actual suspend fun pickImage(config: FilePickerConfig): PickedFile? {
        // Desktop file picker stub
        return null
    }

    actual suspend fun pickImages(config: FilePickerConfig): List<PickedFile> {
        return emptyList()
    }

    actual suspend fun pickVideo(config: FilePickerConfig): PickedFile? {
        return null
    }

    actual suspend fun pickAudio(config: FilePickerConfig): PickedFile? {
        return null
    }

    actual suspend fun pickFile(mimeTypes: List<String>, config: FilePickerConfig): PickedFile? {
        return null
    }
}
