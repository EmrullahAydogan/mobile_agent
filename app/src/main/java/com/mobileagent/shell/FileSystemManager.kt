package com.mobileagent.shell

import com.mobileagent.MobileAgentApplication
import java.io.File

class FileSystemManager {
    private val context = MobileAgentApplication.getAppContext()

    val homeDirectory: File
        get() = context.getExternalFilesDir(null)?.resolve("home") ?: context.filesDir

    val binDirectory: File
        get() = homeDirectory.resolve("bin").also { it.mkdirs() }

    val tmpDirectory: File
        get() = homeDirectory.resolve("tmp").also { it.mkdirs() }

    fun listFiles(directory: File = homeDirectory): List<FileInfo> {
        if (!directory.exists() || !directory.isDirectory) {
            return emptyList()
        }

        return directory.listFiles()?.map { file ->
            FileInfo(
                name = file.name,
                path = file.absolutePath,
                isDirectory = file.isDirectory,
                size = if (file.isFile) file.length() else 0,
                lastModified = file.lastModified(),
                canRead = file.canRead(),
                canWrite = file.canWrite(),
                canExecute = file.canExecute()
            )
        } ?: emptyList()
    }

    fun createDirectory(path: String): Boolean {
        val dir = File(path)
        return dir.mkdirs()
    }

    fun createFile(path: String): Boolean {
        return try {
            val file = File(path)
            file.parentFile?.mkdirs()
            file.createNewFile()
        } catch (e: Exception) {
            false
        }
    }

    fun deleteFile(path: String): Boolean {
        val file = File(path)
        return file.deleteRecursively()
    }

    fun readFile(path: String): String? {
        return try {
            val file = File(path)
            if (file.exists() && file.isFile) {
                file.readText()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun writeFile(path: String, content: String): Boolean {
        return try {
            val file = File(path)
            file.parentFile?.mkdirs()
            file.writeText(content)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun copyFile(source: String, destination: String): Boolean {
        return try {
            val sourceFile = File(source)
            val destFile = File(destination)
            destFile.parentFile?.mkdirs()
            sourceFile.copyTo(destFile, overwrite = true)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun moveFile(source: String, destination: String): Boolean {
        return try {
            val sourceFile = File(source)
            val destFile = File(destination)
            destFile.parentFile?.mkdirs()
            sourceFile.renameTo(destFile)
        } catch (e: Exception) {
            false
        }
    }

    fun fileExists(path: String): Boolean {
        return File(path).exists()
    }

    fun isDirectory(path: String): Boolean {
        val file = File(path)
        return file.exists() && file.isDirectory
    }

    fun getFileSize(path: String): Long {
        val file = File(path)
        return if (file.exists() && file.isFile) file.length() else 0
    }
}

data class FileInfo(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long,
    val lastModified: Long,
    val canRead: Boolean,
    val canWrite: Boolean,
    val canExecute: Boolean
)
