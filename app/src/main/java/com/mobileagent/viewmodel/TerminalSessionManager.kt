package com.mobileagent.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

data class TerminalSession(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val output: MutableList<String> = mutableListOf(),
    val workingDirectory: String = ""
)

class TerminalSessionManager {
    private val _sessions = MutableStateFlow<List<TerminalSession>>(emptyList())
    val sessions: StateFlow<List<TerminalSession>> = _sessions.asStateFlow()

    private val _currentSessionId = MutableStateFlow<String?>(null)
    val currentSessionId: StateFlow<String?> = _currentSessionId.asStateFlow()

    init {
        createSession("Terminal 1")
    }

    fun createSession(name: String? = null): String {
        val sessionName = name ?: "Terminal ${_sessions.value.size + 1}"
        val newSession = TerminalSession(
            name = sessionName,
            output = mutableListOf("Mobile Agent Terminal", "Type 'help' for commands", "")
        )

        _sessions.value = _sessions.value + newSession
        _currentSessionId.value = newSession.id

        return newSession.id
    }

    fun deleteSession(sessionId: String) {
        val sessions = _sessions.value.toMutableList()
        val index = sessions.indexOfFirst { it.id == sessionId }

        if (index != -1) {
            sessions.removeAt(index)
            _sessions.value = sessions

            // Switch to another session if current was deleted
            if (_currentSessionId.value == sessionId) {
                _currentSessionId.value = sessions.firstOrNull()?.id
            }
        }
    }

    fun switchSession(sessionId: String) {
        if (_sessions.value.any { it.id == sessionId }) {
            _currentSessionId.value = sessionId
        }
    }

    fun getCurrentSession(): TerminalSession? {
        return _sessions.value.find { it.id == _currentSessionId.value }
    }

    fun updateSessionOutput(sessionId: String, output: List<String>) {
        val sessions = _sessions.value.toMutableList()
        val index = sessions.indexOfFirst { it.id == sessionId }

        if (index != -1) {
            sessions[index] = sessions[index].copy(output = output.toMutableList())
            _sessions.value = sessions
        }
    }

    fun renameSession(sessionId: String, newName: String) {
        val sessions = _sessions.value.toMutableList()
        val index = sessions.indexOfFirst { it.id == sessionId }

        if (index != -1) {
            sessions[index] = sessions[index].copy(name = newName)
            _sessions.value = sessions
        }
    }

    fun addOutputToSession(sessionId: String, text: String) {
        val session = _sessions.value.find { it.id == sessionId } ?: return
        val lines = text.split("\n")
        session.output.addAll(lines)
        updateSessionOutput(sessionId, session.output)
    }

    fun clearSessionOutput(sessionId: String) {
        updateSessionOutput(sessionId, emptyList())
    }
}
