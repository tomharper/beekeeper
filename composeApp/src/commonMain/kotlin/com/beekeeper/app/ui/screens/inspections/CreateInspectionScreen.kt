package com.beekeeper.app.ui.screens.inspections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beekeeper.app.domain.model.*
import com.beekeeper.app.ui.viewmodel.InspectionsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateInspectionScreen(
    viewModel: InspectionsViewModel,
    hiveId: String?,
    onBackClick: () -> Unit,
    onInspectionCreated: () -> Unit
) {
    var selectedHiveId by remember { mutableStateOf(hiveId ?: "") }
    var queenSeen by remember { mutableStateOf(false) }
    var queenMarked by remember { mutableStateOf(false) }
    var healthStatus by remember { mutableStateOf(InspectionHealthStatus.HEALTHY) }
    var population by remember { mutableStateOf(ColonyPopulation.MEDIUM) }
    var temperament by remember { mutableStateOf(ColonyTemperament.CALM) }
    var broodPattern by remember { mutableStateOf(BroodPattern.GOOD) }
    var honeyStores by remember { mutableStateOf(ResourceLevel.MEDIUM) }
    var pollenStores by remember { mutableStateOf(ResourceLevel.MEDIUM) }
    var varroaMitesDetected by remember { mutableStateOf(false) }
    var diseaseDetected by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }
    var showCameraSheet by remember { mutableStateOf(false) }
    var capturedPhotos by remember { mutableStateOf<List<String>>(emptyList()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Inspection") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A1A1A),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF0F0F0F)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section: Photos
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Photos",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Button(
                        onClick = { showCameraSheet = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFD700),
                            contentColor = Color.Black
                        )
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Take Photo (${capturedPhotos.size})")
                    }

                    if (capturedPhotos.isNotEmpty()) {
                        Text(
                            text = "${capturedPhotos.size} photo(s) captured",
                            color = Color(0xFF4CAF50),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Section: Queen
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Queen",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Checkbox(
                                checked = queenSeen,
                                onCheckedChange = { queenSeen = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color(0xFFFFD700)
                                )
                            )
                            Text("Queen Seen", color = Color.White, fontSize = 14.sp)
                        }

                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Checkbox(
                                checked = queenMarked,
                                onCheckedChange = { queenMarked = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color(0xFFFFD700)
                                )
                            )
                            Text("Queen Marked", color = Color.White, fontSize = 14.sp)
                        }
                    }
                }
            }

            // Section: Colony Assessment
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Colony Assessment",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    EnumSelector(
                        label = "Health Status",
                        options = InspectionHealthStatus.entries,
                        selected = healthStatus,
                        onSelect = { healthStatus = it }
                    )

                    EnumSelector(
                        label = "Population",
                        options = ColonyPopulation.entries,
                        selected = population,
                        onSelect = { population = it }
                    )

                    EnumSelector(
                        label = "Temperament",
                        options = ColonyTemperament.entries,
                        selected = temperament,
                        onSelect = { temperament = it }
                    )

                    EnumSelector(
                        label = "Brood Pattern",
                        options = BroodPattern.entries,
                        selected = broodPattern,
                        onSelect = { broodPattern = it }
                    )
                }
            }

            // Section: Resources
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Resources",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    EnumSelector(
                        label = "Honey Stores",
                        options = ResourceLevel.entries,
                        selected = honeyStores,
                        onSelect = { honeyStores = it }
                    )

                    EnumSelector(
                        label = "Pollen Stores",
                        options = ResourceLevel.entries,
                        selected = pollenStores,
                        onSelect = { pollenStores = it }
                    )
                }
            }

            // Section: Issues
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Issues Detected",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(
                            checked = varroaMitesDetected,
                            onCheckedChange = { varroaMitesDetected = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFFFF6B6B)
                            )
                        )
                        Text("Varroa Mites", color = Color.White, fontSize = 14.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(
                            checked = diseaseDetected,
                            onCheckedChange = { diseaseDetected = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFFFF6B6B)
                            )
                        )
                        Text("Disease", color = Color.White, fontSize = 14.sp)
                    }
                }
            }

            // Section: Notes
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Notes",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Add inspection notes...", color = Color(0xFF888888)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFF444444),
                            focusedBorderColor = Color(0xFFFFD700),
                            unfocusedTextColor = Color.White,
                            focusedTextColor = Color.White
                        ),
                        minLines = 3
                    )
                }
            }

            // Save Button
            Button(
                onClick = {
                    // TODO: Call viewModel to create inspection
                    // For now, just navigate back
                    onInspectionCreated()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFD700),
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Inspection", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Camera Bottom Sheet
    if (showCameraSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCameraSheet = false },
            containerColor = Color(0xFF1A1A1A)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Camera feature available on Android",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Use the mobile app to capture photos during inspection",
                    color = Color(0xFF888888),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { showCameraSheet = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFD700),
                        contentColor = Color.Black
                    )
                ) {
                    Text("Close")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun <T : Enum<T>> EnumSelector(
    label: String,
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(
            text = label,
            color = Color(0xFF888888),
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF444444))
                )
            ) {
                Text(
                    text = selected.name.replace("_", " "),
                    modifier = Modifier.weight(1f)
                )
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.name.replace("_", " ")) },
                        onClick = {
                            onSelect(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
