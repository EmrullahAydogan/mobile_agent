package com.mobileagent.runtime

import com.mobileagent.shell.ShellExecutor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class PythonRuntime {
    private val shellExecutor = ShellExecutor()

    suspend fun execute(scriptPath: String, args: List<String> = emptyList()): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val script = File(scriptPath)
                if (!script.exists()) {
                    return@withContext Result.failure(Exception("Script not found: $scriptPath"))
                }

                val argsString = args.joinToString(" ")
                val command = "python3 ${script.absolutePath} $argsString"

                val result = shellExecutor.execute(command)

                if (result.exitCode == 0) {
                    Result.success(result.output)
                } else {
                    Result.failure(Exception("Python execution failed:\n${result.error}"))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Failed to execute Python script: ${e.message}"))
            }
        }

    suspend fun executeCode(code: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val tmpDir = ShellExecutor.getHomeDirectory().resolve("tmp")
            tmpDir.mkdirs()

            val tempScript = File.createTempFile("python_", ".py", tmpDir)
            tempScript.writeText(code)

            val command = "python3 ${tempScript.absolutePath}"
            val result = shellExecutor.execute(command)

            tempScript.delete()

            if (result.exitCode == 0) {
                Result.success(result.output)
            } else {
                Result.failure(Exception("Python execution failed:\n${result.error}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Failed to execute Python code: ${e.message}"))
        }
    }

    suspend fun installPip(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val result = shellExecutor.execute("python3 -m ensurepip --upgrade")
            if (result.exitCode == 0) {
                Result.success("pip installed successfully")
            } else {
                Result.failure(Exception("Failed to install pip: ${result.error}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("pip installation failed: ${e.message}"))
        }
    }

    suspend fun installPackage(packageName: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val result = shellExecutor.execute("pip3 install $packageName")
            if (result.exitCode == 0) {
                Result.success("Package installed: $packageName")
            } else {
                Result.failure(Exception("Failed to install package: ${result.error}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Package installation failed: ${e.message}"))
        }
    }

    suspend fun checkVersion(): String? {
        return try {
            val result = shellExecutor.execute("python3 --version")
            if (result.exitCode == 0) result.output.trim() else null
        } catch (e: Exception) {
            null
        }
    }
}
