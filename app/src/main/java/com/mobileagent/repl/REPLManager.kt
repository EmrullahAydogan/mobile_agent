package com.mobileagent.repl

import com.mobileagent.runtime.RuntimeManager
import com.mobileagent.shell.ShellExecutor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class REPLOutput(
    val content: String,
    val isError: Boolean = false,
    val isInput: Boolean = false
)

enum class REPLType {
    PYTHON, JAVASCRIPT, BASH
}

class REPLManager(
    private val runtimeManager: RuntimeManager,
    private val shellExecutor: ShellExecutor
) {
    private val _output = MutableStateFlow<List<REPLOutput>>(emptyList())
    val output: StateFlow<List<REPLOutput>> = _output.asStateFlow()

    private var pythonContext = StringBuilder()
    private var jsContext = StringBuilder()
    private var currentType: REPLType = REPLType.BASH

    fun setREPLType(type: REPLType) {
        currentType = type
        addOutput(REPLOutput("Switched to ${type.name} REPL", isInput = false))
    }

    suspend fun execute(code: String): REPLOutput {
        addOutput(REPLOutput(formatPrompt() + code, isInput = true))

        val result = when (currentType) {
            REPLType.PYTHON -> executePython(code)
            REPLType.JAVASCRIPT -> executeJavaScript(code)
            REPLType.BASH -> executeBash(code)
        }

        addOutput(result)
        return result
    }

    private suspend fun executePython(code: String): REPLOutput {
        // Add to context for multi-line support
        pythonContext.appendLine(code)

        val fullCode = pythonContext.toString()
        val result = runtimeManager.pythonRuntime.executeCode(fullCode)

        return when (result) {
            is com.mobileagent.agent.Result.Success -> {
                REPLOutput(result.data.ifEmpty { "Executed successfully" }, isError = false)
            }
            is com.mobileagent.agent.Result.Error -> {
                // If syntax error, might be incomplete statement
                val errorMsg = result.exception.message ?: "Unknown error"
                if (errorMsg.contains("SyntaxError") || errorMsg.contains("IndentationError")) {
                    // Wait for more input
                    REPLOutput("... (continue input)", isError = false)
                } else {
                    pythonContext.clear() // Reset context on error
                    REPLOutput(errorMsg, isError = true)
                }
            }
        }
    }

    private suspend fun executeJavaScript(code: String): REPLOutput {
        jsContext.appendLine(code)

        val fullCode = jsContext.toString()
        val result = runtimeManager.nodeRuntime.executeCode(fullCode)

        return when (result) {
            is com.mobileagent.agent.Result.Success -> {
                REPLOutput(result.data.ifEmpty { "undefined" }, isError = false)
            }
            is com.mobileagent.agent.Result.Error -> {
                jsContext.clear()
                REPLOutput(result.exception.message ?: "Error", isError = true)
            }
        }
    }

    private suspend fun executeBash(code: String): REPLOutput {
        val result = shellExecutor.execute(code)

        return if (result.exitCode == 0) {
            REPLOutput(result.output.ifEmpty { "Command executed" }, isError = false)
        } else {
            REPLOutput(result.error.ifEmpty { "Command failed" }, isError = true)
        }
    }

    fun clear() {
        _output.value = emptyList()
        pythonContext.clear()
        jsContext.clear()
    }

    fun resetContext() {
        pythonContext.clear()
        jsContext.clear()
        addOutput(REPLOutput("Context reset", isInput = false))
    }

    private fun addOutput(output: REPLOutput) {
        _output.value = _output.value + output
    }

    private fun formatPrompt(): String {
        return when (currentType) {
            REPLType.PYTHON -> ">>> "
            REPLType.JAVASCRIPT -> "> "
            REPLType.BASH -> "$ "
        }
    }

    fun getHistory(): List<String> {
        return _output.value
            .filter { it.isInput }
            .map { it.content.removePrefix(formatPrompt()) }
    }
}
