package com.mobileagent.packages

import com.mobileagent.MobileAgentApplication
import com.mobileagent.shell.ShellExecutor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

data class Package(
    val name: String,
    val version: String,
    val description: String,
    val downloadUrl: String,
    val dependencies: List<String> = emptyList(),
    val installed: Boolean = false
)

class PackageManager {
    private val context = MobileAgentApplication.getAppContext()
    private val packagesDir = context.getExternalFilesDir(null)?.resolve("packages")
    private val binDir = ShellExecutor.getHomeDirectory().resolve("bin")

    init {
        packagesDir?.mkdirs()
        binDir.mkdirs()
    }

    private val availablePackages = mapOf(
        "python" to Package(
            name = "python",
            version = "3.11",
            description = "Python 3.11 runtime for Android",
            downloadUrl = "https://github.com/termux/termux-packages/releases/download/python/python-3.11.tar.gz"
        ),
        "nodejs" to Package(
            name = "nodejs",
            version = "18",
            description = "Node.js runtime for Android",
            downloadUrl = "https://unofficial-builds.nodejs.org/download/release/v18.0.0/node-v18.0.0-linux-arm64.tar.gz"
        ),
        "git" to Package(
            name = "git",
            version = "2.40",
            description = "Git version control system",
            downloadUrl = "https://github.com/termux/termux-packages/releases/download/git/git-2.40.tar.gz"
        ),
        "vim" to Package(
            name = "vim",
            version = "9.0",
            description = "Vim text editor",
            downloadUrl = "https://github.com/termux/termux-packages/releases/download/vim/vim-9.0.tar.gz"
        ),
        "curl" to Package(
            name = "curl",
            version = "8.0",
            description = "Command line tool for transferring data",
            downloadUrl = "https://github.com/termux/termux-packages/releases/download/curl/curl-8.0.tar.gz"
        )
    )

    suspend fun listAvailablePackages(): List<Package> = withContext(Dispatchers.IO) {
        availablePackages.values.map { pkg ->
            pkg.copy(installed = isPackageInstalled(pkg.name))
        }
    }

    suspend fun listInstalledPackages(): List<Package> = withContext(Dispatchers.IO) {
        availablePackages.values.filter { isPackageInstalled(it.name) }
    }

    private fun isPackageInstalled(packageName: String): Boolean {
        val packageDir = packagesDir?.resolve(packageName)
        return packageDir?.exists() == true && packageDir.isDirectory
    }

    suspend fun installPackage(packageName: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val pkg = availablePackages[packageName]
                ?: return@withContext Result.failure(Exception("Package not found: $packageName"))

            if (isPackageInstalled(packageName)) {
                return@withContext Result.success("Package already installed: $packageName")
            }

            // Download package
            val packageDir = packagesDir?.resolve(packageName) ?: return@withContext Result.failure(
                Exception("Cannot access packages directory")
            )
            packageDir.mkdirs()

            // Simulate package installation
            // In a real implementation, you would download and extract the package
            val executable = binDir.resolve(packageName)
            executable.createNewFile()
            executable.setExecutable(true)
            executable.writeText("#!/system/bin/sh\necho 'Mock $packageName executable'\n")

            Result.success("Successfully installed: $packageName ${pkg.version}")
        } catch (e: Exception) {
            Result.failure(Exception("Installation failed: ${e.message}"))
        }
    }

    suspend fun uninstallPackage(packageName: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (!isPackageInstalled(packageName)) {
                return@withContext Result.failure(Exception("Package not installed: $packageName"))
            }

            val packageDir = packagesDir?.resolve(packageName)
            packageDir?.deleteRecursively()

            val executable = binDir.resolve(packageName)
            executable.delete()

            Result.success("Successfully uninstalled: $packageName")
        } catch (e: Exception) {
            Result.failure(Exception("Uninstall failed: ${e.message}"))
        }
    }

    suspend fun updatePackage(packageName: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            uninstallPackage(packageName)
            installPackage(packageName)
        } catch (e: Exception) {
            Result.failure(Exception("Update failed: ${e.message}"))
        }
    }

    suspend fun searchPackages(query: String): List<Package> = withContext(Dispatchers.IO) {
        availablePackages.values.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.description.contains(query, ignoreCase = true)
        }.map { pkg ->
            pkg.copy(installed = isPackageInstalled(pkg.name))
        }
    }

    fun getPackageInfo(packageName: String): Package? {
        val pkg = availablePackages[packageName] ?: return null
        return pkg.copy(installed = isPackageInstalled(packageName))
    }
}
