// File: desktopApp/src/jvmMain/kotlin/com/cinefiller/fillerapp/Main.kt
package com.beekeeper.app

import com.beekeeper.app.domain.repository.SampleProjectsFactory
import java.io.File

fun main(args: Array<String>) {
    // Always export - desktop UI not working yet
    exportFactories()
}

private fun exportFactories() {
    println("\n" + "=".repeat(80))
    println("🏭 Exporting Project Factories to JSON")
    println("=".repeat(80))
    println()

    val backendPath = "/Users/tomharper/projects/cinefiller/cinefiller/cinefiller/exported_factories"
    val outputDir = File(backendPath)

    if (!outputDir.exists()) {
        outputDir.mkdirs()
        println("✓ Created output directory: ${outputDir.absolutePath}")
    }
    println("📁 Output directory: ${outputDir.absolutePath}")
    println()

    val factories = SampleProjectsFactory.getAllSampleProjects()
    println("📦 Found ${factories.size} project factories to export")
    println()

    var successCount = 0
    var errorCount = 0

    factories.forEach { factory ->
        try {
            val projectId = factory.projectId
            val json = factory.toJson()

            val file = File(outputDir, "$projectId.json")
            file.writeText(json)

            println("✅ Exported: $projectId.json")
            println("   Title: ${factory.getTitle()}")
            println("   Size: ${json.length / 1024}KB")
            println()

            successCount++
        } catch (e: Exception) {
            println("❌ Failed to export factory: ${e.message}")
            e.printStackTrace()
            println()
            errorCount++
        }
    }

    println("=".repeat(80))
    println("📊 Export Summary")
    println("=".repeat(80))
    println("Total factories: ${factories.size}")
    println("Successfully exported: $successCount")
    println("Failed: $errorCount")
    println("Output directory: ${outputDir.absolutePath}")
    println()

    if (errorCount == 0) {
        println("✅ All factories exported successfully!")
        println()
        println("Next step: Import to backend database")
        println("  cd /Users/tomharper/projects/cinefiller/cinefiller/cinefiller")
        println("  python scripts/import_factories.py")
    } else {
        println("⚠️ Some factories failed to export. See errors above.")
    }
    println()
}
