package com.mobileagent.agent

import com.mobileagent.agent.models.ClaudeRequest
import com.mobileagent.agent.models.ClaudeResponse
import com.mobileagent.agent.models.Message
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ClaudeRepository(private val apiKey: String) {
    private val service: ClaudeApiService

    init {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.anthropic.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        service = retrofit.create(ClaudeApiService::class.java)
    }

    suspend fun sendMessage(
        messages: List<Message>,
        systemPrompt: String? = null
    ): Result<ClaudeResponse> {
        return try {
            val request = ClaudeRequest(
                messages = messages,
                system = systemPrompt
            )

            val response = service.sendMessage(apiKey, request)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("API Error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun chat(
        userMessage: String,
        conversationHistory: List<Message> = emptyList(),
        systemPrompt: String? = null
    ): Result<String> {
        val messages = conversationHistory + Message("user", userMessage)

        return when (val result = sendMessage(messages, systemPrompt)) {
            is Result.Success -> {
                val content = result.data.content.firstOrNull()?.text
                if (content != null) {
                    Result.success(content)
                } else {
                    Result.failure(Exception("No content in response"))
                }
            }
            is Result.Error -> Result.failure(result.exception)
        }
    }
}

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()

    inline fun <R> map(transform: (T) -> R): Result<R> {
        return when (this) {
            is Success -> Success(transform(data))
            is Error -> Error(exception)
        }
    }

    inline fun onSuccess(action: (T) -> Unit): Result<T> {
        if (this is Success) action(data)
        return this
    }

    inline fun onError(action: (Exception) -> Unit): Result<T> {
        if (this is Error) action(exception)
        return this
    }
}
