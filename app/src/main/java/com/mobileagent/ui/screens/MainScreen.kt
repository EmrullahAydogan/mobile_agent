package com.mobileagent.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobileagent.viewmodel.TerminalViewModel

enum class AppMode {
    TERMINAL, AGENT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: TerminalViewModel = viewModel()) {
    var currentMode by remember { mutableStateOf(AppMode.TERMINAL) }
    var showSettings by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (currentMode == AppMode.TERMINAL) "Terminal Mode" else "AI Agent Mode"
                    )
                },
                actions = {
                    IconButton(onClick = {
                        currentMode = if (currentMode == AppMode.TERMINAL) AppMode.AGENT else AppMode.TERMINAL
                    }) {
                        Icon(
                            imageVector = if (currentMode == AppMode.TERMINAL) Icons.Filled.Psychology else Icons.Filled.Terminal,
                            contentDescription = "Switch Mode"
                        )
                    }
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (currentMode) {
                AppMode.TERMINAL -> TerminalScreen(viewModel = viewModel)
                AppMode.AGENT -> AgentScreen(viewModel = viewModel)
            }
        }

        if (showSettings) {
            SettingsDialog(onDismiss = { showSettings = false })
        }
    }
}

@Composable
fun SettingsDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Settings") },
        text = {
            Column {
                Text("Claude API Configuration")
                Spacer(modifier = Modifier.height(8.dp))
                // TODO: Add API key configuration UI
                Text("API key configuration will be added here", style = MaterialTheme.typography.bodyMedium)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
