package com.mobileagent.agent

import com.mobileagent.agent.models.Message
import com.mobileagent.agent.models.ToolResultBlock
import com.mobileagent.agent.models.ContentBlock
import com.mobileagent.agent.tools.ToolDefinitions
import com.mobileagent.agent.tools.ToolExecutor
import com.mobileagent.agent.tools.ToolResult
import com.mobileagent.runtime.RuntimeManager
import com.mobileagent.shell.FileSystemManager
import com.mobileagent.shell.ShellExecutor

class ClaudeAgent(private val apiKey: String) {
    private val repository = ClaudeRepository(apiKey)
    private val shellExecutor = ShellExecutor()
    private val fileSystemManager = FileSystemManager()
    private val runtimeManager = RuntimeManager()
    private val toolExecutor = ToolExecutor(shellExecutor, fileSystemManager, runtimeManager)
    private val conversationHistory = mutableListOf<Message>()

    private val systemPrompt = """
        You are Claude, a helpful AI assistant running on a mobile Android device as a terminal agent.
        You have access to execute shell commands and manipulate files through a terminal interface.

        You have the following tools available:
        - execute_command: Run shell commands (ls, cat, mkdir, rm, cp, mv, etc.)
        - read_file: Read file contents
        - write_file: Create or update files
        - list_files: List directory contents
        - create_directory: Create new directories
        - delete_file: Delete files or directories
        - run_python: Execute Python code
        - run_javascript: Execute JavaScript/Node.js code

        When users ask you to perform tasks:
        1. USE THE TOOLS to actually perform the requested actions
        2. Break down complex tasks into steps
        3. Explain what you're doing before and after using tools
        4. Handle errors gracefully and report them clearly

        Current working directory: ${ShellExecutor.getHomeDirectory().absolutePath}

        Be proactive! If a user asks you to do something, use the appropriate tools to do it.
        Don't just suggest commands - actually execute them using the tools.
    """.trimIndent()

    suspend fun chat(userMessage: String, onToolUse: ((String, String) -> Unit)? = null): String {
        conversationHistory.add(Message("user", userMessage))

        var continueLoop = true
        var finalResponse = ""

        while (continueLoop) {
            when (val result = repository.sendMessageWithTools(
                conversationHistory,
                systemPrompt,
                ToolDefinitions.getAllTools()
            )) {
                is Result.Success -> {
                    val response = result.data
                    val assistantContent = mutableListOf<Any>()

                    // Process response content
                    for (block in response.content) {
                        when (block.type) {
                            "text" -> {
                                block.text?.let {
                                    assistantContent.add(mapOf("type" to "text", "text" to it))
                                    finalResponse += it
                                }
                            }
                            "tool_use" -> {
                                // Execute the tool
                                val toolName = block.name ?: continue
                                val toolInput = block.input ?: emptyMap()
                                val toolUseId = block.id ?: continue

                                onToolUse?.invoke(toolName, "Executing...")

                                val toolResult = toolExecutor.executeTool(toolName, toolInput)

                                // Add tool use to conversation
                                assistantContent.add(mapOf(
                                    "type" to "tool_use",
                                    "id" to toolUseId,
                                    "name" to toolName,
                                    "input" to toolInput
                                ))

                                // Add tool result to conversation
                                conversationHistory.add(Message("assistant", assistantContent.toList()))

                                val resultContent = when (toolResult) {
                                    is ToolResult.Success -> {
                                        onToolUse?.invoke(toolName, toolResult.output)
                                        listOf(ToolResultBlock(
                                            toolUseId = toolUseId,
                                            content = toolResult.output,
                                            isError = false
                                        ))
                                    }
                                    is ToolResult.Error -> {
                                        onToolUse?.invoke(toolName, "Error: ${toolResult.message}")
                                        listOf(ToolResultBlock(
                                            toolUseId = toolUseId,
                                            content = toolResult.message,
                                            isError = true
                                        ))
                                    }
                                }

                                conversationHistory.add(Message("user", resultContent))
                                assistantContent.clear()
                            }
                        }
                    }

                    // Check stop reason
                    when (response.stopReason) {
                        "end_turn" -> {
                            if (assistantContent.isNotEmpty()) {
                                conversationHistory.add(Message("assistant", assistantContent.toList()))
                            }
                            continueLoop = false
                        }
                        "tool_use" -> {
                            // Continue loop to send tool results back
                            continueLoop = true
                        }
                        else -> continueLoop = false
                    }
                }
                is Result.Error -> {
                    finalResponse = "Error: ${result.exception.message}"
                    continueLoop = false
                }
            }
        }

        return finalResponse
    }

    suspend fun executeAgentTask(task: String, onToolUse: ((String, String) -> Unit)? = null): String {
        return chat(task, onToolUse)
    }

    fun clearHistory() {
        conversationHistory.clear()
    }

    fun getHistory(): List<Message> {
        return conversationHistory.toList()
    }
}
