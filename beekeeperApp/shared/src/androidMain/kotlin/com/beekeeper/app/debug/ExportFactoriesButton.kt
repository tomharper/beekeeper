package com.beekeeper.app.debug

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.beekeeper.app.utils.AndroidFactoryExporter
import kotlinx.coroutines.launch

/**
 * Debug button to export factories to disk
 * Add this to any screen for quick export
 */
@Composable
fun ExportFactoriesButton(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isExporting by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var exportResult by remember { mutableStateOf<AndroidFactoryExporter.ExportResult?>(null) }

    Button(
        onClick = {
            scope.launch {
                isExporting = true
                try {
                    val result = AndroidFactoryExporter.exportFactoriesToDisk(context)
                    exportResult = result
                    showDialog = true

                    Toast.makeText(
                        context,
                        if (result.success) "Exported ${result.exportedCount} factories!"
                        else "Export failed!",
                        Toast.LENGTH_LONG
                    ).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                } finally {
                    isExporting = false
                }
            }
        },
        enabled = !isExporting,
        modifier = modifier
    ) {
        if (isExporting) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(Modifier.width(8.dp))
        }
        Text(if (isExporting) "Exporting..." else "Export Factories to Disk")
    }

    // Results dialog
    if (showDialog && exportResult != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Export Complete") },
            text = {
                Column {
                    Text("Exported: ${exportResult!!.exportedCount}")
                    Text("Failed: ${exportResult!!.failedCount}")
                    Text("\nLocation:")
                    Text(
                        exportResult!!.outputPath,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("\nTo retrieve files:")
                    Text(
                        "adb pull ${exportResult!!.outputPath}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}

/**
 * Simple floating action button version
 */
@Composable
fun ExportFactoriesFab(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isExporting by remember { mutableStateOf(false) }

    FloatingActionButton(
        onClick = {
            scope.launch {
                isExporting = true
                try {
                    val result = AndroidFactoryExporter.exportFactoriesToDisk(context)
                    Toast.makeText(
                        context,
                        "Exported to:\n${result.outputPath}",
                        Toast.LENGTH_LONG
                    ).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                } finally {
                    isExporting = false
                }
            }
        },
        modifier = modifier
    ) {
        if (isExporting) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        } else {
            Text("💾")
        }
    }
}
