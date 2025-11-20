package com.mobileagent.agent.tools

import com.mobileagent.runtime.RuntimeManager
import com.mobileagent.shell.FileSystemManager
import com.mobileagent.shell.ShellExecutor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class ToolExecutor(
    private val shellExecutor: ShellExecutor,
    private val fileSystemManager: FileSystemManager,
    private val runtimeManager: RuntimeManager
) {
    suspend fun executeTool(toolName: String, parameters: Map<String, Any>): ToolResult =
        withContext(Dispatchers.IO) {
            try {
                when (toolName) {
                    "execute_command" -> executeCommand(parameters)
                    "read_file" -> readFile(parameters)
                    "write_file" -> writeFile(parameters)
                    "list_files" -> listFiles(parameters)
                    "create_directory" -> createDirectory(parameters)
                    "delete_file" -> deleteFile(parameters)
                    "run_python" -> runPython(parameters)
                    "run_javascript" -> runJavaScript(parameters)
                    else -> ToolResult.Error("Unknown tool: $toolName")
                }
            } catch (e: Exception) {
                ToolResult.Error("Tool execution failed: ${e.message}")
            }
        }

    private suspend fun executeCommand(params: Map<String, Any>): ToolResult {
        val command = params["command"]?.toString() ?: return ToolResult.Error("Missing command parameter")

        val result = shellExecutor.execute(command)

        return if (result.exitCode == 0) {
            ToolResult.Success(
                output = result.output.ifEmpty { "Command executed successfully" },
                metadata = mapOf("exit_code" to result.exitCode)
            )
        } else {
            ToolResult.Error(
                message = result.error.ifEmpty { "Command failed with exit code ${result.exitCode}" },
                details = mapOf("exit_code" to result.exitCode, "output" to result.output)
            )
        }
    }

    private fun readFile(params: Map<String, Any>): ToolResult {
        val path = params["path"]?.toString() ?: return ToolResult.Error("Missing path parameter")

        val content = fileSystemManager.readFile(path)
        return if (content != null) {
            ToolResult.Success(
                output = content,
                metadata = mapOf("path" to path, "size" to content.length)
            )
        } else {
            ToolResult.Error("Failed to read file: $path")
        }
    }

    private fun writeFile(params: Map<String, Any>): ToolResult {
        val path = params["path"]?.toString() ?: return ToolResult.Error("Missing path parameter")
        val content = params["content"]?.toString() ?: return ToolResult.Error("Missing content parameter")

        return if (fileSystemManager.writeFile(path, content)) {
            ToolResult.Success(
                output = "File written successfully to $path",
                metadata = mapOf("path" to path, "size" to content.length)
            )
        } else {
            ToolResult.Error("Failed to write file: $path")
        }
    }

    private fun listFiles(params: Map<String, Any>): ToolResult {
        val path = params["path"]?.toString() ?: fileSystemManager.homeDirectory.absolutePath
        val dir = java.io.File(path)

        if (!fileSystemManager.isDirectory(path)) {
            return ToolResult.Error("Not a directory: $path")
        }

        val files = fileSystemManager.listFiles(dir)
        val output = files.joinToString("\n") { file ->
            val type = if (file.isDirectory) "DIR" else "FILE"
            val size = if (file.isDirectory) "" else "${file.size} bytes"
            "$type  ${file.name}  $size"
        }

        return ToolResult.Success(
            output = output.ifEmpty { "Directory is empty" },
            metadata = mapOf("path" to path, "count" to files.size)
        )
    }

    private fun createDirectory(params: Map<String, Any>): ToolResult {
        val path = params["path"]?.toString() ?: return ToolResult.Error("Missing path parameter")

        return if (fileSystemManager.createDirectory(path)) {
            ToolResult.Success(
                output = "Directory created: $path",
                metadata = mapOf("path" to path)
            )
        } else {
            ToolResult.Error("Failed to create directory: $path")
        }
    }

    private fun deleteFile(params: Map<String, Any>): ToolResult {
        val path = params["path"]?.toString() ?: return ToolResult.Error("Missing path parameter")

        return if (fileSystemManager.deleteFile(path)) {
            ToolResult.Success(
                output = "Deleted: $path",
                metadata = mapOf("path" to path)
            )
        } else {
            ToolResult.Error("Failed to delete: $path")
        }
    }

    private suspend fun runPython(params: Map<String, Any>): ToolResult {
        val code = params["code"]?.toString() ?: return ToolResult.Error("Missing code parameter")

        return when (val result = runtimeManager.pythonRuntime.executeCode(code)) {
            is com.mobileagent.agent.Result.Success -> {
                ToolResult.Success(
                    output = result.data,
                    metadata = mapOf("language" to "python")
                )
            }
            is com.mobileagent.agent.Result.Error -> {
                ToolResult.Error(
                    message = result.exception.message ?: "Python execution failed",
                    details = mapOf("language" to "python")
                )
            }
        }
    }

    private suspend fun runJavaScript(params: Map<String, Any>): ToolResult {
        val code = params["code"]?.toString() ?: return ToolResult.Error("Missing code parameter")

        return when (val result = runtimeManager.nodeRuntime.executeCode(code)) {
            is com.mobileagent.agent.Result.Success -> {
                ToolResult.Success(
                    output = result.data,
                    metadata = mapOf("language" to "javascript")
                )
            }
            is com.mobileagent.agent.Result.Error -> {
                ToolResult.Error(
                    message = result.exception.message ?: "JavaScript execution failed",
                    details = mapOf("language" to "javascript")
                )
            }
        }
    }
}

sealed class ToolResult {
    data class Success(
        val output: String,
        val metadata: Map<String, Any> = emptyMap()
    ) : ToolResult()

    data class Error(
        val message: String,
        val details: Map<String, Any> = emptyMap()
    ) : ToolResult()
}
