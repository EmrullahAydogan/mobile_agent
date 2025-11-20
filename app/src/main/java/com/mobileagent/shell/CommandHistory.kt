package com.mobileagent.shell

import android.content.Context
import com.mobileagent.MobileAgentApplication

class CommandHistory {
    private val prefs = MobileAgentApplication.getAppContext()
        .getSharedPreferences("command_history", Context.MODE_PRIVATE)

    private val history = mutableListOf<String>()
    private var currentIndex = -1

    companion object {
        private const val MAX_HISTORY_SIZE = 500
        private const val KEY_HISTORY = "history"
    }

    init {
        loadHistory()
    }

    fun addCommand(command: String) {
        if (command.isBlank()) return

        // Remove duplicate if exists
        history.remove(command)

        // Add to end
        history.add(command)

        // Trim if too large
        if (history.size > MAX_HISTORY_SIZE) {
            history.removeAt(0)
        }

        // Reset index
        currentIndex = history.size

        saveHistory()
    }

    fun getPreviousCommand(): String? {
        if (history.isEmpty()) return null

        if (currentIndex > 0) {
            currentIndex--
        }

        return if (currentIndex >= 0 && currentIndex < history.size) {
            history[currentIndex]
        } else null
    }

    fun getNextCommand(): String? {
        if (history.isEmpty()) return null

        if (currentIndex < history.size - 1) {
            currentIndex++
            return history[currentIndex]
        } else if (currentIndex == history.size - 1) {
            currentIndex = history.size
            return ""
        }

        return null
    }

    fun resetIndex() {
        currentIndex = history.size
    }

    fun searchHistory(query: String): List<String> {
        return history.filter { it.contains(query, ignoreCase = true) }
            .reversed()
            .take(10)
    }

    fun getAllHistory(): List<String> {
        return history.toList().reversed()
    }

    fun clearHistory() {
        history.clear()
        currentIndex = -1
        saveHistory()
    }

    private fun loadHistory() {
        val historyString = prefs.getString(KEY_HISTORY, "") ?: ""
        if (historyString.isNotEmpty()) {
            history.addAll(historyString.split("\n").filter { it.isNotBlank() })
        }
        currentIndex = history.size
    }

    private fun saveHistory() {
        val historyString = history.joinToString("\n")
        prefs.edit().putString(KEY_HISTORY, historyString).apply()
    }
}
