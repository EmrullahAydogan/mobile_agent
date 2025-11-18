package com.mobileagent.agent.models

import com.google.gson.annotations.SerializedName

data class ClaudeRequest(
    val model: String = "claude-sonnet-4-5-20250929",
    val messages: List<Message>,
    @SerializedName("max_tokens")
    val maxTokens: Int = 4096,
    val temperature: Double = 1.0,
    val system: String? = null,
    val stream: Boolean = false
)

data class Message(
    val role: String, // "user" or "assistant"
    val content: String
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
    val type: String, // "text"
    val text: String
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
