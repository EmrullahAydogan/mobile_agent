package com.mobileagent.network

import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.ChannelShell
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Properties

data class SSHConnection(
    val host: String,
    val port: Int = 22,
    val username: String,
    val password: String? = null,
    val privateKey: String? = null
)

class SSHClient {
    private var session: Session? = null
    private var jsch: JSch = JSch()

    suspend fun connect(connection: SSHConnection): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (connection.privateKey != null) {
                jsch.addIdentity(connection.privateKey)
            }

            session = jsch.getSession(connection.username, connection.host, connection.port).apply {
                if (connection.password != null) {
                    setPassword(connection.password)
                }

                val config = Properties()
                config["StrictHostKeyChecking"] = "no"
                setConfig(config)

                timeout = 30000
                connect()
            }

            Result.success("Connected to ${connection.host}")
        } catch (e: Exception) {
            Result.failure(Exception("Connection failed: ${e.message}"))
        }
    }

    suspend fun executeCommand(command: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val currentSession = session ?: return@withContext Result.failure(
                Exception("Not connected")
            )

            val channel = currentSession.openChannel("exec") as ChannelExec
            channel.setCommand(command)

            val inputStream = channel.inputStream
            val errorStream = channel.errStream

            channel.connect()

            val output = StringBuilder()
            val error = StringBuilder()

            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    output.appendLine(line)
                }
            }

            BufferedReader(InputStreamReader(errorStream)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    error.appendLine(line)
                }
            }

            channel.disconnect()

            if (error.isNotEmpty()) {
                Result.success("$output\nErrors:\n$error")
            } else {
                Result.success(output.toString())
            }
        } catch (e: Exception) {
            Result.failure(Exception("Command execution failed: ${e.message}"))
        }
    }

    suspend fun openShell(onOutput: (String) -> Unit): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val currentSession = session ?: return@withContext Result.failure(
                Exception("Not connected")
            )

            val channel = currentSession.openChannel("shell") as ChannelShell
            val inputStream = channel.inputStream

            channel.connect()

            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    line?.let { onOutput(it) }
                }
            }

            channel.disconnect()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Shell session failed: ${e.message}"))
        }
    }

    fun disconnect() {
        try {
            session?.disconnect()
            session = null
        } catch (e: Exception) {
            // Ignore disconnect errors
        }
    }

    fun isConnected(): Boolean {
        return session?.isConnected ?: false
    }

    suspend fun transferFile(
        localPath: String,
        remotePath: String,
        mode: TransferMode = TransferMode.UPLOAD
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val currentSession = session ?: return@withContext Result.failure(
                Exception("Not connected")
            )

            val channel = currentSession.openChannel("sftp") as com.jcraft.jsch.ChannelSftp
            channel.connect()

            when (mode) {
                TransferMode.UPLOAD -> {
                    channel.put(localPath, remotePath)
                    Result.success("File uploaded: $localPath -> $remotePath")
                }
                TransferMode.DOWNLOAD -> {
                    channel.get(remotePath, localPath)
                    Result.success("File downloaded: $remotePath -> $localPath")
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception("File transfer failed: ${e.message}"))
        }
    }

    enum class TransferMode {
        UPLOAD, DOWNLOAD
    }
}

class SSHManager {
    private val connections = mutableMapOf<String, SSHClient>()

    fun getClient(connectionId: String): SSHClient? {
        return connections[connectionId]
    }

    fun createConnection(connectionId: String): SSHClient {
        val client = SSHClient()
        connections[connectionId] = client
        return client
    }

    fun removeConnection(connectionId: String) {
        connections[connectionId]?.disconnect()
        connections.remove(connectionId)
    }

    fun getAllConnections(): Map<String, SSHClient> {
        return connections.toMap()
    }

    fun disconnectAll() {
        connections.values.forEach { it.disconnect() }
        connections.clear()
    }
}
