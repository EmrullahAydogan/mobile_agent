package com.mobileagent.agent

import com.mobileagent.agent.models.ClaudeRequest
import com.mobileagent.agent.models.ClaudeResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface ClaudeApiService {
    @POST("v1/messages")
    @Headers(
        "anthropic-version: 2023-06-01",
        "Content-Type: application/json"
    )
    suspend fun sendMessage(
        @Header("x-api-key") apiKey: String,
        @Body request: ClaudeRequest
    ): Response<ClaudeResponse>
}
