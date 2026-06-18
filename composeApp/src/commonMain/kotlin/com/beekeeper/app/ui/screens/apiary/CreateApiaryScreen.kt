package com.beekeeper.app.ui.screens.apiary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.beekeeper.app.location.rememberLocationPermissionRequest
import com.beekeeper.app.ui.theme.AlertRed
import com.beekeeper.app.ui.theme.BackgroundDark
import com.beekeeper.app.ui.theme.BeekeeperGold
import com.beekeeper.app.ui.theme.BeekeeperGreenDark
import com.beekeeper.app.ui.theme.CardBackground
import com.beekeeper.app.ui.theme.HealthyGreen
import com.beekeeper.app.ui.theme.TextPrimary
import com.beekeeper.app.ui.viewmodel.CreateApiaryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateApiaryScreen(
    viewModel: CreateApiaryViewModel,
    onBackClick: () -> Unit,
    onCreated: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Requests location permission; captures coords once granted.
    val requestPermission = rememberLocationPermissionRequest { granted ->
        if (granted) viewModel.captureLocation()
    }

    LaunchedEffect(state.created) {
        if (state.created) onCreated()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Apiary", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BeekeeperGreenDark,
                    titleContentColor = TextPrimary,
                ),
            )
        },
        containerColor = BackgroundDark,
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.location,
                onValueChange = viewModel::onLocationChange,
                label = { Text("Location") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            // "Set it at the spot" — capture device GPS.
            Button(
                onClick = { requestPermission() },
                enabled = !state.capturingLocation,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CardBackground,
                    contentColor = TextPrimary,
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = BeekeeperGold)
                Spacer(Modifier.width(8.dp))
                Text(if (state.capturingLocation) "Getting location…" else "Use current location")
            }

            val lat = state.latitude
            val lon = state.longitude
            if (lat != null && lon != null) {
                Text(
                    "Captured: $lat, $lon",
                    color = HealthyGreen,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            state.error?.let {
                Text(it, color = AlertRed, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = viewModel::create,
                enabled = !state.creating,
                colors = ButtonDefaults.buttonColors(
                    containerColor = BeekeeperGold,
                    contentColor = Color.Black,
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (state.creating) "Creating…" else "Create Apiary")
            }
        }
    }
}
