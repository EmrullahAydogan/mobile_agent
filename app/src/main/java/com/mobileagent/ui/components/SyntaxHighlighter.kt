package com.mobileagent.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

object SyntaxHighlighter {
    private val commandColor = Color(0xFF58A6FF)
    private val argumentColor = Color(0xFF79C0FF)
    private val stringColor = Color(0xFFA5D6FF)
    private val optionColor = Color(0xFFFFD580)
    private val numberColor = Color(0xFF79C0FF)
    private val operatorColor = Color(0xFFFF7B72)
    private val commentColor = Color(0xFF8B949E)
    private val errorColor = Color(0xFFFF7B72)
    private val successColor = Color(0xFF7EE787)

    private val commands = setOf(
        "ls", "cd", "pwd", "cat", "echo", "mkdir", "rm", "cp", "mv", "touch",
        "chmod", "chown", "grep", "find", "sed", "awk", "sort", "uniq", "wc",
        "head", "tail", "diff", "tar", "gzip", "gunzip", "zip", "unzip",
        "wget", "curl", "ssh", "scp", "rsync", "git", "npm", "pip", "python",
        "node", "java", "javac", "gcc", "make", "cmake", "docker", "kubectl"
    )

    fun highlightCommand(text: String): AnnotatedString {
        if (text.startsWith("Error:") || text.contains("error", ignoreCase = true)) {
            return buildAnnotatedString {
                withStyle(SpanStyle(color = errorColor)) {
                    append(text)
                }
            }
        }

        if (text.startsWith("$ ")) {
            return highlightShellCommand(text.substring(2))
        }

        // Detect if it's a success message
        if (text.contains("success", ignoreCase = true) ||
            text.contains("completed", ignoreCase = true) ||
            text.contains("done", ignoreCase = true)) {
            return buildAnnotatedString {
                withStyle(SpanStyle(color = successColor)) {
                    append(text)
                }
            }
        }

        return AnnotatedString(text)
    }

    private fun highlightShellCommand(command: String): AnnotatedString = buildAnnotatedString {
        withStyle(SpanStyle(color = Color(0xFF58A6FF))) {
            append("$ ")
        }

        val tokens = tokenize(command)
        var isFirstToken = true

        for (token in tokens) {
            when {
                isFirstToken && commands.contains(token) -> {
                    withStyle(SpanStyle(color = commandColor)) {
                        append(token)
                    }
                    isFirstToken = false
                }
                token.startsWith("-") -> {
                    withStyle(SpanStyle(color = optionColor)) {
                        append(token)
                    }
                }
                token.startsWith("\"") || token.startsWith("'") -> {
                    withStyle(SpanStyle(color = stringColor)) {
                        append(token)
                    }
                }
                token.matches(Regex("\\d+")) -> {
                    withStyle(SpanStyle(color = numberColor)) {
                        append(token)
                    }
                }
                token in setOf("|", ">", ">>", "<", "&&", "||", ";") -> {
                    withStyle(SpanStyle(color = operatorColor)) {
                        append(token)
                    }
                }
                else -> {
                    withStyle(SpanStyle(color = argumentColor)) {
                        append(token)
                    }
                }
            }
            append(" ")
        }
    }

    private fun tokenize(command: String): List<String> {
        val tokens = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false
        var quoteChar = ' '

        for (char in command) {
            when {
                char == '"' || char == '\'' -> {
                    if (inQuotes && char == quoteChar) {
                        current.append(char)
                        tokens.add(current.toString())
                        current = StringBuilder()
                        inQuotes = false
                    } else if (!inQuotes) {
                        if (current.isNotEmpty()) {
                            tokens.add(current.toString())
                            current = StringBuilder()
                        }
                        current.append(char)
                        inQuotes = true
                        quoteChar = char
                    } else {
                        current.append(char)
                    }
                }
                char.isWhitespace() && !inQuotes -> {
                    if (current.isNotEmpty()) {
                        tokens.add(current.toString())
                        current = StringBuilder()
                    }
                }
                else -> current.append(char)
            }
        }

        if (current.isNotEmpty()) {
            tokens.add(current.toString())
        }

        return tokens
    }

    fun highlightOutput(text: String): AnnotatedString {
        return buildAnnotatedString {
            val lines = text.split("\n")

            for ((index, line) in lines.withIndex()) {
                when {
                    line.contains("error", ignoreCase = true) -> {
                        withStyle(SpanStyle(color = errorColor)) {
                            append(line)
                        }
                    }
                    line.contains("warning", ignoreCase = true) -> {
                        withStyle(SpanStyle(color = optionColor)) {
                            append(line)
                        }
                    }
                    line.contains("success", ignoreCase = true) -> {
                        withStyle(SpanStyle(color = successColor)) {
                            append(line)
                        }
                    }
                    line.startsWith("#") -> {
                        withStyle(SpanStyle(color = commentColor)) {
                            append(line)
                        }
                    }
                    else -> {
                        append(line)
                    }
                }

                if (index < lines.size - 1) {
                    append("\n")
                }
            }
        }
    }
}
