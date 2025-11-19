package com.mobileagent.git

import com.mobileagent.shell.ShellExecutor
import java.io.File

data class GitStatus(
    val branch: String,
    val staged: List<String>,
    val modified: List<String>,
    val untracked: List<String>,
    val ahead: Int = 0,
    val behind: Int = 0
)

data class GitCommit(
    val hash: String,
    val author: String,
    val date: String,
    val message: String
)

class GitManager(private val shellExecutor: ShellExecutor) {

    suspend fun isGitRepository(directory: File): Boolean {
        val result = shellExecutor.execute("git -C ${directory.absolutePath} rev-parse --git-dir")
        return result.exitCode == 0
    }

    suspend fun initRepository(directory: File): Result<String> {
        val result = shellExecutor.execute("git -C ${directory.absolutePath} init")
        return if (result.exitCode == 0) {
            Result.success("Repository initialized")
        } else {
            Result.failure(Exception(result.error))
        }
    }

    suspend fun getStatus(directory: File): Result<GitStatus> {
        val statusResult = shellExecutor.execute("git -C ${directory.absolutePath} status --porcelain --branch")

        if (statusResult.exitCode != 0) {
            return Result.failure(Exception(statusResult.error))
        }

        val lines = statusResult.output.split("\n").filter { it.isNotBlank() }
        var branch = "unknown"
        val staged = mutableListOf<String>()
        val modified = mutableListOf<String>()
        val untracked = mutableListOf<String>()

        for (line in lines) {
            when {
                line.startsWith("## ") -> {
                    branch = line.substring(3).split("...").first()
                }
                line.startsWith("?? ") -> {
                    untracked.add(line.substring(3))
                }
                line.startsWith(" M ") -> {
                    modified.add(line.substring(3))
                }
                line.startsWith("M  ") || line.startsWith("A  ") -> {
                    staged.add(line.substring(3))
                }
            }
        }

        return Result.success(GitStatus(branch, staged, modified, untracked))
    }

    suspend fun getCommitHistory(directory: File, limit: Int = 20): Result<List<GitCommit>> {
        val result = shellExecutor.execute(
            "git -C ${directory.absolutePath} log --pretty=format:\"%H|%an|%ad|%s\" --date=short -n $limit"
        )

        if (result.exitCode != 0) {
            return Result.failure(Exception(result.error))
        }

        val commits = result.output.split("\n")
            .filter { it.isNotBlank() }
            .map { line ->
                val parts = line.split("|")
                GitCommit(
                    hash = parts[0],
                    author = parts[1],
                    date = parts[2],
                    message = parts.getOrNull(3) ?: ""
                )
            }

        return Result.success(commits)
    }

    suspend fun getBranches(directory: File): Result<List<String>> {
        val result = shellExecutor.execute("git -C ${directory.absolutePath} branch --list")

        if (result.exitCode != 0) {
            return Result.failure(Exception(result.error))
        }

        val branches = result.output.split("\n")
            .filter { it.isNotBlank() }
            .map { it.trim().removePrefix("* ") }

        return Result.success(branches)
    }

    suspend fun createBranch(directory: File, branchName: String): Result<String> {
        val result = shellExecutor.execute("git -C ${directory.absolutePath} branch $branchName")
        return if (result.exitCode == 0) {
            Result.success("Branch created: $branchName")
        } else {
            Result.failure(Exception(result.error))
        }
    }

    suspend fun checkoutBranch(directory: File, branchName: String): Result<String> {
        val result = shellExecutor.execute("git -C ${directory.absolutePath} checkout $branchName")
        return if (result.exitCode == 0) {
            Result.success("Switched to branch: $branchName")
        } else {
            Result.failure(Exception(result.error))
        }
    }

    suspend fun stageFile(directory: File, filePath: String): Result<String> {
        val result = shellExecutor.execute("git -C ${directory.absolutePath} add $filePath")
        return if (result.exitCode == 0) {
            Result.success("File staged: $filePath")
        } else {
            Result.failure(Exception(result.error))
        }
    }

    suspend fun stageAll(directory: File): Result<String> {
        val result = shellExecutor.execute("git -C ${directory.absolutePath} add .")
        return if (result.exitCode == 0) {
            Result.success("All files staged")
        } else {
            Result.failure(Exception(result.error))
        }
    }

    suspend fun commit(directory: File, message: String): Result<String> {
        val result = shellExecutor.execute("git -C ${directory.absolutePath} commit -m \"$message\"")
        return if (result.exitCode == 0) {
            Result.success("Committed: $message")
        } else {
            Result.failure(Exception(result.error))
        }
    }

    suspend fun push(directory: File, remote: String = "origin", branch: String = "main"): Result<String> {
        val result = shellExecutor.execute("git -C ${directory.absolutePath} push $remote $branch")
        return if (result.exitCode == 0) {
            Result.success("Pushed to $remote/$branch")
        } else {
            Result.failure(Exception(result.error))
        }
    }

    suspend fun pull(directory: File, remote: String = "origin", branch: String = "main"): Result<String> {
        val result = shellExecutor.execute("git -C ${directory.absolutePath} pull $remote $branch")
        return if (result.exitCode == 0) {
            Result.success("Pulled from $remote/$branch")
        } else {
            Result.failure(Exception(result.error))
        }
    }

    suspend fun getDiff(directory: File, filePath: String?): Result<String> {
        val command = if (filePath != null) {
            "git -C ${directory.absolutePath} diff $filePath"
        } else {
            "git -C ${directory.absolutePath} diff"
        }

        val result = shellExecutor.execute(command)
        return if (result.exitCode == 0) {
            Result.success(result.output)
        } else {
            Result.failure(Exception(result.error))
        }
    }

    suspend fun clone(url: String, directory: File): Result<String> {
        val result = shellExecutor.execute("git clone $url ${directory.absolutePath}")
        return if (result.exitCode == 0) {
            Result.success("Repository cloned")
        } else {
            Result.failure(Exception(result.error))
        }
    }
}
