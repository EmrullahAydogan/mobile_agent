package com.mobileagent.agent.models

import com.google.gson.annotations.SerializedName
import com.mobileagent.agent.tools.Tool

data class ClaudeRequest(
    val model: String = "claude-sonnet-4-5-20250929",
    val messages: List<Message>,
    @SerializedName("max_tokens")
    val maxTokens: Int = 4096,
    val temperature: Double = 1.0,
    val system: String? = null,
    val tools: List<Tool>? = null,
    val stream: Boolean = false
)

data class Message(
    val role: String, // "user" or "assistant"
    val content: Any // Can be String or List<ContentBlock>
)

data class ClaudeResponse(
    val id: String,
    val type: String,
    val role: String,
    val content: List<ContentBlock>,
    val model: String,
    @SerializedName("stop_reason")
    val stopReason: String?,
    val usage: Usage
)

data class ContentBlock(
    val type: String, // "text" or "tool_use"
    val text: String? = null,
    val id: String? = null,
    val name: String? = null,
    val input: Map<String, Any>? = null
)

data class ToolUseBlock(
    val type: String = "tool_use",
    val id: String,
    val name: String,
    val input: Map<String, Any>
)

data class ToolResultBlock(
    val type: String = "tool_result",
    @SerializedName("tool_use_id")
    val toolUseId: String,
    val content: String,
    @SerializedName("is_error")
    val isError: Boolean = false
)

data class Usage(
    @SerializedName("input_tokens")
    val inputTokens: Int,
    @SerializedName("output_tokens")
    val outputTokens: Int
)

data class ClaudeError(
    val type: String,
    val error: ErrorDetail
)

data class ErrorDetail(
    val type: String,
    val message: String
)
