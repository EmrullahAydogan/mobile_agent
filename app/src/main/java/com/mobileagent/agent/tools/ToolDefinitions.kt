package com.mobileagent.agent.tools

import com.google.gson.annotations.SerializedName

data class Tool(
    val name: String,
    val description: String,
    @SerializedName("input_schema")
    val inputSchema: InputSchema
)

data class InputSchema(
    val type: String = "object",
    val properties: Map<String, Property>,
    val required: List<String> = emptyList()
)

data class Property(
    val type: String,
    val description: String,
    val enum: List<String>? = null
)

object ToolDefinitions {
    val EXECUTE_COMMAND = Tool(
        name = "execute_command",
        description = "Execute a shell command in the terminal. Use this to run any command like ls, cat, mkdir, etc.",
        inputSchema = InputSchema(
            properties = mapOf(
                "command" to Property(
                    type = "string",
                    description = "The shell command to execute"
                )
            ),
            required = listOf("command")
        )
    )

    val READ_FILE = Tool(
        name = "read_file",
        description = "Read the contents of a file",
        inputSchema = InputSchema(
            properties = mapOf(
                "path" to Property(
                    type = "string",
                    description = "The path to the file to read"
                )
            ),
            required = listOf("path")
        )
    )

    val WRITE_FILE = Tool(
        name = "write_file",
        description = "Write content to a file. Creates the file if it doesn't exist.",
        inputSchema = InputSchema(
            properties = mapOf(
                "path" to Property(
                    type = "string",
                    description = "The path to the file"
                ),
                "content" to Property(
                    type = "string",
                    description = "The content to write to the file"
                )
            ),
            required = listOf("path", "content")
        )
    )

    val LIST_FILES = Tool(
        name = "list_files",
        description = "List files in a directory",
        inputSchema = InputSchema(
            properties = mapOf(
                "path" to Property(
                    type = "string",
                    description = "The directory path to list. Defaults to current directory if not specified."
                )
            ),
            required = emptyList()
        )
    )

    val CREATE_DIRECTORY = Tool(
        name = "create_directory",
        description = "Create a new directory",
        inputSchema = InputSchema(
            properties = mapOf(
                "path" to Property(
                    type = "string",
                    description = "The path of the directory to create"
                )
            ),
            required = listOf("path")
        )
    )

    val DELETE_FILE = Tool(
        name = "delete_file",
        description = "Delete a file or directory",
        inputSchema = InputSchema(
            properties = mapOf(
                "path" to Property(
                    type = "string",
                    description = "The path to the file or directory to delete"
                )
            ),
            required = listOf("path")
        )
    )

    val RUN_PYTHON = Tool(
        name = "run_python",
        description = "Execute Python code",
        inputSchema = InputSchema(
            properties = mapOf(
                "code" to Property(
                    type = "string",
                    description = "The Python code to execute"
                )
            ),
            required = listOf("code")
        )
    )

    val RUN_JAVASCRIPT = Tool(
        name = "run_javascript",
        description = "Execute JavaScript/Node.js code",
        inputSchema = InputSchema(
            properties = mapOf(
                "code" to Property(
                    type = "string",
                    description = "The JavaScript code to execute"
                )
            ),
            required = listOf("code")
        )
    )

    fun getAllTools(): List<Tool> = listOf(
        EXECUTE_COMMAND,
        READ_FILE,
        WRITE_FILE,
        LIST_FILES,
        CREATE_DIRECTORY,
        DELETE_FILE,
        RUN_PYTHON,
        RUN_JAVASCRIPT
    )
}
