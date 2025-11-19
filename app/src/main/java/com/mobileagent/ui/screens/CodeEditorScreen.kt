package com.mobileagent.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobileagent.shell.FileSystemManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeEditorScreen(
    filePath: String,
    fileSystemManager: FileSystemManager = FileSystemManager(),
    onClose: () -> Unit
) {
    var content by remember { mutableStateOf("") }
    var isModified by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(filePath) {
        content = fileSystemManager.readFile(filePath) ?: ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = filePath.substringAfterLast("/"),
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (isModified) {
                            Text(
                                text = "Modified",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isModified) {
                            showSaveDialog = true
                        } else {
                            onClose()
                        }
                    }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            if (fileSystemManager.writeFile(filePath, content)) {
                                isModified = false
                                snackbarHostState.showSnackbar("File saved successfully")
                            } else {
                                snackbarHostState.showSnackbar("Failed to save file")
                            }
                        }
                    }) {
                        Icon(Icons.Filled.Save, contentDescription = "Save")
                    }
                    IconButton(onClick = {
                        // TODO: Implement search
                    }) {
                        Icon(Icons.Filled.Search, contentDescription = "Search")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            CodeEditorContent(
                content = content,
                onContentChange = {
                    content = it
                    isModified = true
                }
            )
        }
    }

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Unsaved Changes") },
            text = { Text("Do you want to save your changes?") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        fileSystemManager.writeFile(filePath, content)
                        showSaveDialog = false
                        onClose()
                    }
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showSaveDialog = false
                    onClose()
                }) {
                    Text("Discard")
                }
            }
        )
    }
}

@Composable
fun CodeEditorContent(
    content: String,
    onContentChange: (String) -> Unit
) {
    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()
    val lines = content.split("\n")

    Row(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(verticalScrollState)
            .horizontalScroll(horizontalScrollState)
    ) {
        // Line numbers
        Column(
            modifier = Modifier
                .padding(8.dp)
                .width(40.dp)
        ) {
            lines.forEachIndexed { index, _ ->
                Text(
                    text = "${index + 1}",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        color = Color.Gray
                    ),
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }

        // Code content
        BasicTextField(
            value = content,
            onValueChange = onContentChange,
            modifier = Modifier
                .weight(1f)
                .padding(8.dp),
            textStyle = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                color = Color.White,
                lineHeight = 20.sp
            ),
            decorationBox = { innerTextField ->
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (content.isEmpty()) {
                        Text(
                            text = "Start typing...",
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}
