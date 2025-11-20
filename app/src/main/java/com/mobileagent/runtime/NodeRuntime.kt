package com.mobileagent.runtime

import com.mobileagent.shell.ShellExecutor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class NodeRuntime {
    private val shellExecutor = ShellExecutor()

    suspend fun execute(scriptPath: String, args: List<String> = emptyList()): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val script = File(scriptPath)
                if (!script.exists()) {
                    return@withContext Result.failure(Exception("Script not found: $scriptPath"))
                }

                val argsString = args.joinToString(" ")
                val command = "node ${script.absolutePath} $argsString"

                val result = shellExecutor.execute(command)

                if (result.exitCode == 0) {
                    Result.success(result.output)
                } else {
                    Result.failure(Exception("Node.js execution failed:\n${result.error}"))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Failed to execute Node.js script: ${e.message}"))
            }
        }

    suspend fun executeCode(code: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val tmpDir = ShellExecutor.getHomeDirectory().resolve("tmp")
            tmpDir.mkdirs()

            val tempScript = File.createTempFile("node_", ".js", tmpDir)
            tempScript.writeText(code)

            val command = "node ${tempScript.absolutePath}"
            val result = shellExecutor.execute(command)

            tempScript.delete()

            if (result.exitCode == 0) {
                Result.success(result.output)
            } else {
                Result.failure(Exception("Node.js execution failed:\n${result.error}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Failed to execute Node.js code: ${e.message}"))
        }
    }

    suspend fun installNpm(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val result = shellExecutor.execute("npm --version")
            if (result.exitCode == 0) {
                Result.success("npm is already installed: ${result.output.trim()}")
            } else {
                Result.failure(Exception("npm not found"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("npm check failed: ${e.message}"))
        }
    }

    suspend fun installPackage(packageName: String, global: Boolean = false): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val globalFlag = if (global) "-g" else ""
                val result = shellExecutor.execute("npm install $globalFlag $packageName")

                if (result.exitCode == 0) {
                    Result.success("Package installed: $packageName")
                } else {
                    Result.failure(Exception("Failed to install package: ${result.error}"))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Package installation failed: ${e.message}"))
            }
        }

    suspend fun runNpmScript(scriptName: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val result = shellExecutor.execute("npm run $scriptName")

            if (result.exitCode == 0) {
                Result.success(result.output)
            } else {
                Result.failure(Exception("Script execution failed: ${result.error}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Failed to run npm script: ${e.message}"))
        }
    }

    suspend fun checkVersion(): String? {
        return try {
            val result = shellExecutor.execute("node --version")
            if (result.exitCode == 0) result.output.trim() else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun checkNpmVersion(): String? {
        return try {
            val result = shellExecutor.execute("npm --version")
            if (result.exitCode == 0) result.output.trim() else null
        } catch (e: Exception) {
            null
        }
    }
}
