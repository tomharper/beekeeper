import com.beekeeper.app.utils.FactoryExporter
import kotlin.test.Test

/**
 * Test to export all factories to console
 * Run this test and copy the output to create JSON files
 */
class ExportFactoriesTest {

    @Test
    fun exportAllFactoriesToConsole() {
        val factories = FactoryExporter.exportAllFactories()

        println("\n" + "=".repeat(80))
        println("🏭 Exporting ${factories.size} Project Factories")
        println("=".repeat(80))
        println()

        factories.forEach { (projectId, json) ->
            println("=" * 80)
            println("FILE: $projectId.json")
            println("SIZE: ${json.length / 1024}KB")
            println("=" * 80)
            println(json)
            println("\n")
        }

        println("=" * 80)
        println("✅ Export complete! Copy each JSON block above to files.")
        println("=" * 80)
    }

    @Test
    fun exportSingleFactoryAsExample() {
        // Export just one factory as an example
        val json = FactoryExporter.exportFactoryById("proj_mahabharata_epic")

        if (json != null) {
            println("\n" + "=".repeat(80))
            println("📦 Mahabharata Factory JSON")
            println("=".repeat(80))
            println(json)
            println("\n")
        } else {
            println("❌ Factory not found")
        }
    }
}

private operator fun String.times(n: Int) = repeat(n)
