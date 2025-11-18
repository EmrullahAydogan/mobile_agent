package com.mobileagent.shell

import com.mobileagent.MobileAgentApplication
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class CommandResult(
    val output: String,
    val error: String,
    val exitCode: Int
)

class ShellExecutor {
    private var currentDirectory: File = getHomeDirectory()

    companion object {
        fun getHomeDirectory(): File {
            val context = MobileAgentApplication.getAppContext()
            val homeDir = context.getExternalFilesDir(null)?.resolve("home")
            homeDir?.mkdirs()
            return homeDir ?: context.filesDir
        }
    }

    suspend fun execute(command: String): CommandResult = withContext(Dispatchers.IO) {
        try {
            when {
                command.startsWith("ls") -> executeLs(command)
                command.startsWith("pwd") -> executePwd()
                command.startsWith("cat ") -> executeCat(command)
                command.startsWith("echo ") -> executeEcho(command)
                command.startsWith("mkdir ") -> executeMkdir(command)
                command.startsWith("touch ") -> executeTouch(command)
                command.startsWith("rm ") -> executeRm(command)
                command.startsWith("cp ") -> executeCp(command)
                command.startsWith("mv ") -> executeMv(command)
                else -> executeNative(command)
            }
        } catch (e: Exception) {
            CommandResult("", "Error: ${e.message}", 1)
        }
    }

    fun changeDirectory(path: String): CommandResult {
        val targetDir = if (path.startsWith("/")) {
            File(path)
        } else if (path == "..") {
            currentDirectory.parentFile ?: currentDirectory
        } else if (path == "~") {
            getHomeDirectory()
        } else {
            File(currentDirectory, path)
        }

        return if (targetDir.exists() && targetDir.isDirectory) {
            currentDirectory = targetDir
            CommandResult("", "", 0)
        } else {
            CommandResult("", "Directory not found: $path", 1)
        }
    }

    private fun executeLs(command: String): CommandResult {
        val args = command.split(" ")
        val targetDir = if (args.size > 1) {
            File(currentDirectory, args[1])
        } else {
            currentDirectory
        }

        if (!targetDir.exists() || !targetDir.isDirectory) {
            return CommandResult("", "Directory not found", 1)
        }

        val files = targetDir.listFiles() ?: emptyArray()
        val output = files.joinToString("\n") { file ->
            val prefix = if (file.isDirectory) "d" else "-"
            val size = if (file.isFile) file.length() else 0
            "$prefix ${file.name} ($size bytes)"
        }

        return CommandResult(output, "", 0)
    }

    private fun executePwd(): CommandResult {
        return CommandResult(currentDirectory.absolutePath, "", 0)
    }

    private fun executeCat(command: String): CommandResult {
        val fileName = command.substringAfter("cat ").trim()
        val file = File(currentDirectory, fileName)

        if (!file.exists()) {
            return CommandResult("", "File not found: $fileName", 1)
        }

        if (!file.isFile) {
            return CommandResult("", "$fileName is not a file", 1)
        }

        val content = file.readText()
        return CommandResult(content, "", 0)
    }

    private fun executeEcho(command: String): CommandResult {
        val text = command.substringAfter("echo ").trim()
        return CommandResult(text, "", 0)
    }

    private fun executeMkdir(command: String): CommandResult {
        val dirName = command.substringAfter("mkdir ").trim()
        val dir = File(currentDirectory, dirName)

        return if (dir.mkdirs()) {
            CommandResult("Directory created: $dirName", "", 0)
        } else {
            CommandResult("", "Failed to create directory: $dirName", 1)
        }
    }

    private fun executeTouch(command: String): CommandResult {
        val fileName = command.substringAfter("touch ").trim()
        val file = File(currentDirectory, fileName)

        return try {
            file.createNewFile()
            CommandResult("File created: $fileName", "", 0)
        } catch (e: Exception) {
            CommandResult("", "Failed to create file: ${e.message}", 1)
        }
    }

    private fun executeRm(command: String): CommandResult {
        val fileName = command.substringAfter("rm ").trim()
        val file = File(currentDirectory, fileName)

        if (!file.exists()) {
            return CommandResult("", "File not found: $fileName", 1)
        }

        return if (file.deleteRecursively()) {
            CommandResult("Removed: $fileName", "", 0)
        } else {
            CommandResult("", "Failed to remove: $fileName", 1)
        }
    }

    private fun executeCp(command: String): CommandResult {
        val parts = command.substringAfter("cp ").trim().split(" ")
        if (parts.size < 2) {
            return CommandResult("", "Usage: cp <source> <destination>", 1)
        }

        val source = File(currentDirectory, parts[0])
        val dest = File(currentDirectory, parts[1])

        if (!source.exists()) {
            return CommandResult("", "Source not found: ${parts[0]}", 1)
        }

        return try {
            source.copyRecursively(dest, overwrite = true)
            CommandResult("Copied: ${parts[0]} -> ${parts[1]}", "", 0)
        } catch (e: Exception) {
            CommandResult("", "Copy failed: ${e.message}", 1)
        }
    }

    private fun executeMv(command: String): CommandResult {
        val parts = command.substringAfter("mv ").trim().split(" ")
        if (parts.size < 2) {
            return CommandResult("", "Usage: mv <source> <destination>", 1)
        }

        val source = File(currentDirectory, parts[0])
        val dest = File(currentDirectory, parts[1])

        if (!source.exists()) {
            return CommandResult("", "Source not found: ${parts[0]}", 1)
        }

        return if (source.renameTo(dest)) {
            CommandResult("Moved: ${parts[0]} -> ${parts[1]}", "", 0)
        } else {
            CommandResult("", "Move failed", 1)
        }
    }

    private fun executeNative(command: String): CommandResult {
        return try {
            val process = ProcessBuilder()
                .command("sh", "-c", command)
                .directory(currentDirectory)
                .redirectErrorStream(false)
                .start()

            val output = BufferedReader(InputStreamReader(process.inputStream)).use {
                it.readText()
            }
            val error = BufferedReader(InputStreamReader(process.errorStream)).use {
                it.readText()
            }

            val exitCode = process.waitFor()
            CommandResult(output, error, exitCode)
        } catch (e: Exception) {
            CommandResult("", "Command execution failed: ${e.message}", 1)
        }
    }
}
