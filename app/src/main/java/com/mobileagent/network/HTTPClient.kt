package com.mobileagent.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.util.concurrent.TimeUnit

data class HTTPRequest(
    val method: String = "GET", // GET, POST, PUT, DELETE, PATCH
    val url: String,
    val headers: Map<String, String> = emptyMap(),
    val body: String? = null,
    val contentType: String = "application/json"
)

data class HTTPResponse(
    val statusCode: Int,
    val statusMessage: String,
    val headers: Map<String, List<String>>,
    val body: String,
    val responseTime: Long
)

class HTTPClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun execute(request: HTTPRequest): Result<HTTPResponse> = withContext(Dispatchers.IO) {
        try {
            val startTime = System.currentTimeMillis()

            val requestBuilder = Request.Builder().url(request.url)

            // Add headers
            request.headers.forEach { (key, value) ->
                requestBuilder.addHeader(key, value)
            }

            // Build request with method and body
            val okHttpRequest = when (request.method.uppercase()) {
                "GET" -> requestBuilder.get().build()
                "POST" -> {
                    val body = (request.body ?: "").toRequestBody(request.contentType.toMediaType())
                    requestBuilder.post(body).build()
                }
                "PUT" -> {
                    val body = (request.body ?: "").toRequestBody(request.contentType.toMediaType())
                    requestBuilder.put(body).build()
                }
                "DELETE" -> {
                    if (request.body != null) {
                        val body = request.body.toRequestBody(request.contentType.toMediaType())
                        requestBuilder.delete(body).build()
                    } else {
                        requestBuilder.delete().build()
                    }
                }
                "PATCH" -> {
                    val body = (request.body ?: "").toRequestBody(request.contentType.toMediaType())
                    requestBuilder.patch(body).build()
                }
                else -> return@withContext Result.failure(Exception("Unsupported HTTP method: ${request.method}"))
            }

            val response: Response = client.newCall(okHttpRequest).execute()
            val responseTime = System.currentTimeMillis() - startTime

            val httpResponse = HTTPResponse(
                statusCode = response.code,
                statusMessage = response.message,
                headers = response.headers.toMultimap(),
                body = response.body?.string() ?: "",
                responseTime = responseTime
            )

            response.close()
            Result.success(httpResponse)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun get(url: String, headers: Map<String, String> = emptyMap()): Result<HTTPResponse> {
        return execute(HTTPRequest(method = "GET", url = url, headers = headers))
    }

    suspend fun post(
        url: String,
        body: String,
        headers: Map<String, String> = emptyMap(),
        contentType: String = "application/json"
    ): Result<HTTPResponse> {
        return execute(HTTPRequest(method = "POST", url = url, headers = headers, body = body, contentType = contentType))
    }

    suspend fun put(
        url: String,
        body: String,
        headers: Map<String, String> = emptyMap(),
        contentType: String = "application/json"
    ): Result<HTTPResponse> {
        return execute(HTTPRequest(method = "PUT", url = url, headers = headers, body = body, contentType = contentType))
    }

    suspend fun delete(
        url: String,
        headers: Map<String, String> = emptyMap()
    ): Result<HTTPResponse> {
        return execute(HTTPRequest(method = "DELETE", url = url, headers = headers))
    }

    suspend fun patch(
        url: String,
        body: String,
        headers: Map<String, String> = emptyMap(),
        contentType: String = "application/json"
    ): Result<HTTPResponse> {
        return execute(HTTPRequest(method = "PATCH", url = url, headers = headers, body = body, contentType = contentType))
    }
}

// Request history manager
class HTTPRequestHistory {
    private val history = mutableListOf<HTTPRequest>()
    private val maxSize = 50

    fun addRequest(request: HTTPRequest) {
        history.add(0, request)
        if (history.size > maxSize) {
            history.removeLast()
        }
    }

    fun getHistory(): List<HTTPRequest> = history.toList()

    fun clearHistory() {
        history.clear()
    }

    fun getByUrl(url: String): List<HTTPRequest> {
        return history.filter { it.url.contains(url, ignoreCase = true) }
    }
}
