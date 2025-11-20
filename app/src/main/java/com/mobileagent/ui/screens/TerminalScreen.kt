package com.mobileagent.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobileagent.viewmodel.TerminalViewModel
import kotlinx.coroutines.launch

@Composable
fun TerminalScreen(viewModel: TerminalViewModel = viewModel()) {
    val terminalOutput by viewModel.terminalOutput.collectAsState()
    val currentCommand by viewModel.currentCommand.collectAsState()
    val isExecuting by viewModel.isExecuting.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(terminalOutput.size) {
        if (terminalOutput.isNotEmpty()) {
            listState.animateScrollToItem(terminalOutput.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117))
    ) {
        // Terminal Output Area
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            items(terminalOutput) { line ->
                Text(
                    text = line,
                    color = Color(0xFFC9D1D9),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }

        // Command Input Area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF161B22))
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$ ",
                color = Color(0xFF58A6FF),
                style = MaterialTheme.typography.bodyLarge
            )

            TextField(
                value = currentCommand,
                onValueChange = { viewModel.updateCommand(it) },
                modifier = Modifier.weight(1f),
                enabled = !isExecuting,
                placeholder = { Text("Enter command...") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedTextColor = Color(0xFFC9D1D9),
                    unfocusedTextColor = Color(0xFFC9D1D9),
                    disabledTextColor = Color(0xFF8B949E),
                    cursorColor = Color(0xFF58A6FF)
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        coroutineScope.launch {
                            viewModel.executeCommand()
                        }
                    }
                ),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge
            )

            IconButton(
                onClick = {
                    coroutineScope.launch {
                        viewModel.executeCommand()
                    }
                },
                enabled = !isExecuting && currentCommand.isNotBlank()
            ) {
                Icon(
                    imageVector = Icons.Filled.Send,
                    contentDescription = "Execute",
                    tint = if (!isExecuting && currentCommand.isNotBlank()) Color(0xFF58A6FF) else Color(0xFF484F58)
                )
            }
        }
    }
}
