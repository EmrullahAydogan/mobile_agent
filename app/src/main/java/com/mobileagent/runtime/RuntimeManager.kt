package com.mobileagent.runtime

class RuntimeManager {
    val pythonRuntime = PythonRuntime()
    val nodeRuntime = NodeRuntime()

    suspend fun checkAvailableRuntimes(): Map<String, String?> {
        return mapOf(
            "python" to pythonRuntime.checkVersion(),
            "node" to nodeRuntime.checkVersion(),
            "npm" to nodeRuntime.checkNpmVersion()
        )
    }

    suspend fun executeScript(scriptPath: String, runtime: String? = null): Result<String> {
        val detectedRuntime = runtime ?: detectRuntime(scriptPath)

        return when (detectedRuntime) {
            "python" -> pythonRuntime.execute(scriptPath)
            "node" -> nodeRuntime.execute(scriptPath)
            else -> Result.failure(Exception("Unknown or unsupported runtime: $detectedRuntime"))
        }
    }

    private fun detectRuntime(scriptPath: String): String {
        return when {
            scriptPath.endsWith(".py") -> "python"
            scriptPath.endsWith(".js") || scriptPath.endsWith(".mjs") -> "node"
            else -> "unknown"
        }
    }

    suspend fun executeCode(code: String, runtime: String): Result<String> {
        return when (runtime.lowercase()) {
            "python" -> pythonRuntime.executeCode(code)
            "node", "javascript", "js" -> nodeRuntime.executeCode(code)
            else -> Result.failure(Exception("Unsupported runtime: $runtime"))
        }
    }
}
