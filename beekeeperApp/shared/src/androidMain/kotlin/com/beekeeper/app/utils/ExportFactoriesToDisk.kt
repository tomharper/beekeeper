package com.beekeeper.app.utils

import android.content.Context
import com.beekeeper.app.domain.repository.SampleProjectsFactory
import java.io.File

/**
 * Android-specific function to export factories to disk
 * Files will be written to app's external files directory
 * Retrieve with: adb pull /sdcard/Android/data/com.beekeeper.app/files/factories/
 */
object AndroidFactoryExporter {

    fun exportFactoriesToDisk(context: Context): ExportResult {
        val factoriesDir = File(context.getExternalFilesDir(null), "factories")

        // Clean and recreate directory
        if (factoriesDir.exists()) {
            factoriesDir.deleteRecursively()
        }
        factoriesDir.mkdirs()

        val factories = SampleProjectsFactory.getAllSampleProjects()
        val results = mutableListOf<String>()
        var successCount = 0
        var errorCount = 0

        factories.forEach { factory ->
            try {
                val projectId = factory.projectId
                val json = factory.toJson()

                val file = File(factoriesDir, "$projectId.json")
                file.writeText(json)

                results.add("✅ Exported: $projectId.json (${json.length / 1024}KB)")
                successCount++
            } catch (e: Exception) {
                results.add("❌ Failed: ${e.message}")
                errorCount++
            }
        }

        return ExportResult(
            success = errorCount == 0,
            exportedCount = successCount,
            failedCount = errorCount,
            outputPath = factoriesDir.absolutePath,
            details = results
        )
    }

    data class ExportResult(
        val success: Boolean,
        val exportedCount: Int,
        val failedCount: Int,
        val outputPath: String,
        val details: List<String>
    )
}
