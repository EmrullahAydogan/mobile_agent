package com.mobileagent.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mobileagent.shell.FileInfo
import com.mobileagent.shell.FileSystemManager
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileBrowserScreen(
    fileSystemManager: FileSystemManager = FileSystemManager(),
    onFileSelected: (String) -> Unit = {}
) {
    var currentPath by remember { mutableStateOf(fileSystemManager.homeDirectory.absolutePath) }
    var files by remember { mutableStateOf<List<FileInfo>>(emptyList()) }
    var showContextMenu by remember { mutableStateOf<FileInfo?>(null) }
    var showNewFileDialog by remember { mutableStateOf(false) }
    var showNewFolderDialog by remember { mutableStateOf(false) }

    LaunchedEffect(currentPath) {
        files = fileSystemManager.listFiles(File(currentPath))
            .sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = currentPath,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        val parent = File(currentPath).parent
                        if (parent != null) {
                            currentPath = parent
                        }
                    }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showNewFolderDialog = true }) {
                        Icon(Icons.Filled.CreateNewFolder, contentDescription = "New Folder")
                    }
                    IconButton(onClick = { showNewFileDialog = true }) {
                        Icon(Icons.Filled.NoteAdd, contentDescription = "New File")
                    }
                    IconButton(onClick = {
                        files = fileSystemManager.listFiles(File(currentPath))
                            .sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
                    }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            items(files) { file ->
                FileListItem(
                    file = file,
                    onClick = {
                        if (file.isDirectory) {
                            currentPath = file.path
                        } else {
                            onFileSelected(file.path)
                        }
                    },
                    onLongClick = {
                        showContextMenu = file
                    }
                )
            }
        }
    }

    // Context menu
    showContextMenu?.let { file ->
        FileContextMenu(
            file = file,
            onDismiss = { showContextMenu = null },
            onDelete = {
                fileSystemManager.deleteFile(file.path)
                files = fileSystemManager.listFiles(File(currentPath))
                    .sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
                showContextMenu = null
            },
            onRename = {
                // TODO: Implement rename dialog
                showContextMenu = null
            }
        )
    }

    // New file dialog
    if (showNewFileDialog) {
        CreateFileDialog(
            onDismiss = { showNewFileDialog = false },
            onCreate = { fileName ->
                fileSystemManager.createFile(File(currentPath, fileName).absolutePath)
                files = fileSystemManager.listFiles(File(currentPath))
                    .sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
                showNewFileDialog = false
            }
        )
    }

    // New folder dialog
    if (showNewFolderDialog) {
        CreateFolderDialog(
            onDismiss = { showNewFolderDialog = false },
            onCreate = { folderName ->
                fileSystemManager.createDirectory(File(currentPath, folderName).absolutePath)
                files = fileSystemManager.listFiles(File(currentPath))
                    .sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
                showNewFolderDialog = false
            }
        )
    }
}

@Composable
fun FileListItem(
    file: FileInfo,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

    ListItem(
        headlineContent = {
            Text(
                text = file.name,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        supportingContent = {
            Text(
                text = if (file.isDirectory) {
                    "Folder • ${dateFormat.format(Date(file.lastModified))}"
                } else {
                    "${formatFileSize(file.size)} • ${dateFormat.format(Date(file.lastModified))}"
                },
                style = MaterialTheme.typography.bodySmall
            )
        },
        leadingContent = {
            Icon(
                imageVector = if (file.isDirectory) Icons.Filled.Folder else Icons.Filled.InsertDriveFile,
                contentDescription = null,
                tint = if (file.isDirectory) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        },
        trailingContent = {
            if (!file.canWrite) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = "Read-only",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Composable
fun FileContextMenu(
    file: FileInfo,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onRename: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(file.name) },
        text = {
            Column {
                TextButton(
                    onClick = onRename,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Rename")
                }
                TextButton(
                    onClick = onDelete,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Delete")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CreateFileDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var fileName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New File") },
        text = {
            TextField(
                value = fileName,
                onValueChange = { fileName = it },
                label = { Text("File name") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onCreate(fileName) },
                enabled = fileName.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CreateFolderDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var folderName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Folder") },
        text = {
            TextField(
                value = folderName,
                onValueChange = { folderName = it },
                label = { Text("Folder name") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onCreate(folderName) },
                enabled = folderName.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun formatFileSize(bytes: Long): String {
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0

    return when {
        gb >= 1 -> String.format("%.2f GB", gb)
        mb >= 1 -> String.format("%.2f MB", mb)
        kb >= 1 -> String.format("%.2f KB", kb)
        else -> "$bytes B"
    }
}
