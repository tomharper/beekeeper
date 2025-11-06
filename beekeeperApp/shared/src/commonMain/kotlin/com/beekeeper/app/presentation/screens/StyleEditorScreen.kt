// File: fillerApp/shared/src/commonMain/kotlin/com/example/fillerapp/presentation/screens/StyleEditorScreen.kt
package com.beekeeper.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class VisualEffect(val id: String, val name: String)
data class StyleOption(val id: String, val name: String)

@Composable
fun StyleEditorScreen(
    onNavigateBack: () -> Unit,
    onApplyStyle: (visualEffect: String?, style: String?) -> Unit
) {
    var selectedVisualEffect by remember { mutableStateOf<String?>(null) }
    var selectedStyle by remember { mutableStateOf<String?>(null) }
    
    val visualEffects = listOf(
        VisualEffect("none", "None"),
        VisualEffect("glitch", "Glitch"),
        VisualEffect("vhs", "VHS"),
        VisualEffect("film", "Film"),
        VisualEffect("retro", "Retro")
    )
    
    val styleOptions = listOf(
        StyleOption("none", "None"),
        StyleOption("cinematic", "Cinematic"),
        StyleOption("vibrant", "Vibrant"),
        StyleOption("vintage", "Vintage"),
        StyleOption("monochrome", "Monochrome")
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1A))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
                
                Text(
                    "Style",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.width(48.dp))
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Visual Effects Section
            Text(
                "Visual Effects",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(visualEffects) { effect ->
                    EffectChip(
                        text = effect.name,
                        isSelected = selectedVisualEffect == effect.id,
                        onClick = { 
                            selectedVisualEffect = if (selectedVisualEffect == effect.id) null else effect.id
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Style Section
            Text(
                "Style",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // First row of styles
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    styleOptions.take(3).forEach { style ->
                        EffectChip(
                            text = style.name,
                            isSelected = selectedStyle == style.id,
                            onClick = { 
                                selectedStyle = if (selectedStyle == style.id) null else style.id
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                // Second row of styles
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    styleOptions.drop(3).forEach { style ->
                        EffectChip(
                            text = style.name,
                            isSelected = selectedStyle == style.id,
                            onClick = { 
                                selectedStyle = if (selectedStyle == style.id) null else style.id
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Add empty space to align properly
                    if (styleOptions.size > 3 && styleOptions.size % 3 != 0) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Apply Button
            Button(
                onClick = { onApplyStyle(selectedVisualEffect, selectedStyle) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4A90E2)
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    "Apply",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun EffectChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .background(
                color = if (isSelected) Color(0xFF4A90E2) else Color(0xFF2A2A2A),
                shape = RoundedCornerShape(24.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 14.sp
        )
    }
}
