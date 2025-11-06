import com.beekeeper.app.utils.FactoryExporter
import java.io.File

/**
 * Main function to export all project factories to JSON files
 * Run with: ./gradlew :shared:runExportFactories
 */
fun main() {
    println("\n" + "=".repeat(80))
    println("🏭 Exporting Project Factories to JSON")
    println("=".repeat(80))
    println()

    // Get output directory - use the backend's exported_factories directory
    val backendPath = "/Users/tomharper/projects/cinefiller/cinefiller/cinefiller/exported_factories"
    val outputDir = File(backendPath)

    if (!outputDir.exists()) {
        outputDir.mkdirs()
        println("✓ Created output directory: ${outputDir.absolutePath}")
    }
    println("📁 Output directory: ${outputDir.absolutePath}")
    println()

    // Export all factories
    val factories = FactoryExporter.exportAllFactories()

    println("📦 Found ${factories.size} project factories to export")
    println()

    var successCount = 0
    var errorCount = 0

    factories.forEach { (projectId, json) ->
        try {
            val file = File(outputDir, "$projectId.json")
            file.writeText(json)

            println("✅ Exported: $projectId.json")
            println("   Size: ${json.length / 1024}KB")
            println()

            successCount++
        } catch (e: Exception) {
            println("❌ Failed to export $projectId: ${e.message}")
            println()
            errorCount++
        }
    }

    // Summary
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
