package com.mobileagent.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileagent.shell.ShellExecutor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TerminalViewModel : ViewModel() {
    private val shellExecutor = ShellExecutor()

    private val _terminalOutput = MutableStateFlow<List<String>>(emptyList())
    val terminalOutput: StateFlow<List<String>> = _terminalOutput.asStateFlow()

    private val _currentCommand = MutableStateFlow("")
    val currentCommand: StateFlow<String> = _currentCommand.asStateFlow()

    private val _isExecuting = MutableStateFlow(false)
    val isExecuting: StateFlow<Boolean> = _isExecuting.asStateFlow()

    init {
        addOutput("Mobile Agent Terminal v1.0")
        addOutput("Type 'help' for available commands")
        addOutput("")
    }

    fun updateCommand(command: String) {
        _currentCommand.value = command
    }

    suspend fun executeCommand() {
        val command = _currentCommand.value.trim()
        if (command.isEmpty()) return

        _isExecuting.value = true
        addOutput("$ $command")

        try {
            when {
                command == "clear" -> {
                    _terminalOutput.value = emptyList()
                }
                command == "help" -> {
                    showHelp()
                }
                command.startsWith("cd ") -> {
                    val result = shellExecutor.changeDirectory(command.substringAfter("cd ").trim())
                    addOutput(result.output)
                }
                else -> {
                    val result = shellExecutor.execute(command)
                    if (result.output.isNotEmpty()) {
                        addOutput(result.output)
                    }
                    if (result.error.isNotEmpty()) {
                        addOutput("Error: ${result.error}")
                    }
                }
            }
        } catch (e: Exception) {
            addOutput("Error executing command: ${e.message}")
        }

        _currentCommand.value = ""
        _isExecuting.value = false
    }

    private fun addOutput(text: String) {
        val lines = text.split("\n")
        _terminalOutput.value = _terminalOutput.value + lines
    }

    private fun showHelp() {
        addOutput("Available commands:")
        addOutput("  help     - Show this help message")
        addOutput("  clear    - Clear terminal screen")
        addOutput("  ls       - List files in directory")
        addOutput("  cd       - Change directory")
        addOutput("  pwd      - Print working directory")
        addOutput("  cat      - Display file contents")
        addOutput("  mkdir    - Create directory")
        addOutput("  rm       - Remove file")
        addOutput("  touch    - Create empty file")
        addOutput("  echo     - Print text")
        addOutput("")
        addOutput("Switch to AI Agent mode for Claude-powered assistance")
    }
}
