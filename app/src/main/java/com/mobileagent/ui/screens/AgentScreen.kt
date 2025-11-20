package com.mobileagent.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.mobileagent.viewmodel.TerminalViewModel
import kotlinx.coroutines.launch

data class AgentMessage(
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@Composable
fun AgentScreen(viewModel: TerminalViewModel) {
    val messages = remember { mutableStateListOf<AgentMessage>() }
    var currentInput by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117))
    ) {
        // Messages Area
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            items(messages) { message ->
                MessageBubble(message)
            }
        }

        // Input Area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF161B22))
                .padding(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            TextField(
                value = currentInput,
                onValueChange = { currentInput = it },
                modifier = Modifier.weight(1f),
                enabled = !isProcessing,
                placeholder = { Text("Ask AI agent...") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF21262D),
                    unfocusedContainerColor = Color(0xFF21262D),
                    disabledContainerColor = Color(0xFF21262D),
                    focusedTextColor = Color(0xFFC9D1D9),
                    unfocusedTextColor = Color(0xFFC9D1D9),
                    cursorColor = Color(0xFF58A6FF)
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (currentInput.isNotBlank() && !isProcessing) {
                            messages.add(AgentMessage(currentInput, isUser = true))
                            val userMessage = currentInput
                            currentInput = ""
                            isProcessing = true

                            coroutineScope.launch {
                                // TODO: Integrate with Claude API
                                messages.add(
                                    AgentMessage(
                                        "AI Agent integration coming soon. You said: \"$userMessage\"",
                                        isUser = false
                                    )
                                )
                                isProcessing = false
                            }
                        }
                    }
                ),
                maxLines = 4,
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (currentInput.isNotBlank() && !isProcessing) {
                        messages.add(AgentMessage(currentInput, isUser = true))
                        val userMessage = currentInput
                        currentInput = ""
                        isProcessing = true

                        coroutineScope.launch {
                            // TODO: Integrate with Claude API
                            messages.add(
                                AgentMessage(
                                    "AI Agent integration coming soon. You said: \"$userMessage\"",
                                    isUser = false
                                )
                            )
                            isProcessing = false
                        }
                    }
                },
                enabled = !isProcessing && currentInput.isNotBlank()
            ) {
                Icon(
                    imageVector = Icons.Filled.Send,
                    contentDescription = "Send",
                    tint = if (!isProcessing && currentInput.isNotBlank()) Color(0xFF58A6FF) else Color(0xFF484F58)
                )
            }
        }
    }
}

@Composable
fun MessageBubble(message: AgentMessage) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 280.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isUser) Color(0xFF1E3A5F) else Color(0xFF21262D)
            )
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(12.dp),
                color = Color(0xFFC9D1D9),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
