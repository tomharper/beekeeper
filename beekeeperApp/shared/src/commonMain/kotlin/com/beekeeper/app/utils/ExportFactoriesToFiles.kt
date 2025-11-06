// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/utils/ExportFactoriesToFiles.kt
package com.beekeeper.app.utils

import com.beekeeper.app.domain.repository.SampleProjectsFactory

/**
 * Main function to export all project factories to JSON files
 *
 * This can be called from:
 * - A JVM main function
 * - An Android debug menu
 * - A test case
 * - Command line
 */
object ExportFactoriesToFiles {

    /**
     * Export all factories and return the results
     * Caller is responsible for writing to files
     */
    fun exportAll(): Map<String, String> {
        return FactoryExporter.exportAllFactories()
    }

    /**
     * Print export instructions
     */
    fun printExportData() {
        val factories = FactoryExporter.exportAllFactories()

        println("\n" + "=" * 80)
        println("🏭 Project Factory JSON Export")
        println("=" * 80)
        println("\nTotal factories: ${factories.size}")
        println("\nTo save these, copy each JSON block to a file:\n")

        factories.forEach { (projectId, json) ->
            println("=" * 80)
            println("FILE: $projectId.json")
            println("=" * 80)
            println(json)
            println("\n")
        }

        println("=" * 80)
        println("✅ Export complete!")
        println("=" * 80)
    }
}

private operator fun String.times(n: Int) = repeat(n)

// For JVM/Desktop: uncomment this to make it runnable
/*
fun main() {
    ExportFactoriesToFiles.printExportData()
}
*/
