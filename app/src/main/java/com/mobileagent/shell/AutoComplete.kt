package com.mobileagent.shell

import java.io.File

class AutoComplete(private val fileSystemManager: FileSystemManager) {

    private val commands = listOf(
        "ls", "cd", "pwd", "cat", "echo", "mkdir", "rm", "cp", "mv", "touch",
        "chmod", "chown", "grep", "find", "sed", "awk", "sort", "uniq", "wc",
        "head", "tail", "diff", "tar", "gzip", "gunzip", "zip", "unzip",
        "wget", "curl", "ssh", "scp", "rsync", "git", "npm", "pip", "python",
        "node", "java", "clear", "help", "exit", "history"
    )

    fun getSuggestions(input: String, currentDirectory: File): List<String> {
        if (input.isBlank()) return emptyList()

        val parts = input.split(" ")

        return when {
            // Command completion
            parts.size == 1 -> {
                commands.filter { it.startsWith(input) }.take(5)
            }
            // File/directory completion
            else -> {
                val lastPart = parts.last()
                getFileCompletions(lastPart, currentDirectory)
            }
        }
    }

    private fun getFileCompletions(partial: String, currentDirectory: File): List<String> {
        val dir = if (partial.contains("/")) {
            val path = partial.substringBeforeLast("/")
            File(currentDirectory, path)
        } else {
            currentDirectory
        }

        if (!dir.exists() || !dir.isDirectory) {
            return emptyList()
        }

        val prefix = partial.substringAfterLast("/")
        val files = dir.listFiles() ?: return emptyList()

        return files
            .filter { it.name.startsWith(prefix) }
            .map {
                if (it.isDirectory) "${it.name}/" else it.name
            }
            .take(10)
    }

    fun completeCommand(input: String, currentDirectory: File): String {
        val suggestions = getSuggestions(input, currentDirectory)

        if (suggestions.isEmpty()) return input

        if (suggestions.size == 1) {
            val parts = input.split(" ")
            return if (parts.size == 1) {
                suggestions[0]
            } else {
                parts.dropLast(1).joinToString(" ") + " " + suggestions[0]
            }
        }

        // Find common prefix
        val commonPrefix = findCommonPrefix(suggestions)
        val parts = input.split(" ")

        return if (parts.size == 1) {
            commonPrefix
        } else {
            parts.dropLast(1).joinToString(" ") + " " + commonPrefix
        }
    }

    private fun findCommonPrefix(strings: List<String>): String {
        if (strings.isEmpty()) return ""
        if (strings.size == 1) return strings[0]

        var prefix = strings[0]
        for (i in 1 until strings.size) {
            while (!strings[i].startsWith(prefix)) {
                prefix = prefix.dropLast(1)
                if (prefix.isEmpty()) return ""
            }
        }

        return prefix
    }
}
