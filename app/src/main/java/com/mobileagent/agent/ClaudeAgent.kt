package com.mobileagent.agent

import com.mobileagent.agent.models.Message
import com.mobileagent.shell.FileSystemManager
import com.mobileagent.shell.ShellExecutor

class ClaudeAgent(private val apiKey: String) {
    private val repository = ClaudeRepository(apiKey)
    private val shellExecutor = ShellExecutor()
    private val fileSystemManager = FileSystemManager()
    private val conversationHistory = mutableListOf<Message>()

    private val systemPrompt = """
        You are Claude, a helpful AI assistant running on a mobile Android device as a terminal agent.
        You have access to execute shell commands and manipulate files through a terminal interface.

        Key capabilities:
        - Execute shell commands (ls, cat, mkdir, rm, cp, mv, etc.)
        - Read and write files
        - Navigate the file system
        - Help users with programming and system administration tasks

        When users ask you to perform tasks:
        1. Break down complex tasks into steps
        2. Execute necessary commands
        3. Explain what you're doing
        4. Handle errors gracefully

        Current working directory: ${ShellExecutor.getHomeDirectory().absolutePath}

        Be concise and helpful. Format code blocks properly.
    """.trimIndent()

    suspend fun chat(userMessage: String): String {
        conversationHistory.add(Message("user", userMessage))

        return when (val result = repository.chat(userMessage, conversationHistory, systemPrompt)) {
            is Result.Success -> {
                val response = result.data
                conversationHistory.add(Message("assistant", response))
                response
            }
            is Result.Error -> {
                "Error: ${result.exception.message}"
            }
        }
    }

    suspend fun executeAgentTask(task: String): String {
        // Enhanced task execution with command detection
        val response = chat(task)

        // Check if response contains command suggestions
        // Extract and execute if user confirms
        return response
    }

    fun clearHistory() {
        conversationHistory.clear()
    }

    fun getHistory(): List<Message> {
        return conversationHistory.toList()
    }

    // Tool execution methods that Claude can call
    suspend fun executeTool(toolName: String, params: Map<String, String>): String {
        return when (toolName) {
            "execute_command" -> {
                val command = params["command"] ?: return "No command specified"
                val result = shellExecutor.execute(command)
                buildString {
                    if (result.output.isNotEmpty()) appendLine(result.output)
                    if (result.error.isNotEmpty()) appendLine("Error: ${result.error}")
                }
            }
            "read_file" -> {
                val path = params["path"] ?: return "No path specified"
                fileSystemManager.readFile(path) ?: "File not found or cannot be read"
            }
            "write_file" -> {
                val path = params["path"] ?: return "No path specified"
                val content = params["content"] ?: return "No content specified"
                if (fileSystemManager.writeFile(path, content)) {
                    "File written successfully"
                } else {
                    "Failed to write file"
                }
            }
            "list_files" -> {
                val path = params["path"]
                val dir = if (path != null) java.io.File(path) else fileSystemManager.homeDirectory
                val files = fileSystemManager.listFiles(dir)
                files.joinToString("\n") { "${it.name} (${if (it.isDirectory) "dir" else "${it.size} bytes"})" }
            }
            else -> "Unknown tool: $toolName"
        }
    }
}
