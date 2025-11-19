package com.mobileagent.automation

import com.mobileagent.shell.ShellExecutor

data class DockerContainer(
    val id: String,
    val name: String,
    val image: String,
    val status: String,
    val ports: String,
    val created: String
)

data class DockerImage(
    val id: String,
    val repository: String,
    val tag: String,
    val size: String,
    val created: String
)

class DockerManager(private val shellExecutor: ShellExecutor) {

    suspend fun isDockerInstalled(): Boolean {
        val result = shellExecutor.execute("docker --version")
        return result.exitCode == 0
    }

    suspend fun listContainers(all: Boolean = false): Result<List<DockerContainer>> {
        val command = if (all) "docker ps -a --format \"{{.ID}}|{{.Names}}|{{.Image}}|{{.Status}}|{{.Ports}}|{{.CreatedAt}}\""
        else "docker ps --format \"{{.ID}}|{{.Names}}|{{.Image}}|{{.Status}}|{{.Ports}}|{{.CreatedAt}}\""

        val result = shellExecutor.execute(command)

        if (result.exitCode != 0) {
            return Result.failure(Exception(result.error))
        }

        val containers = result.output.split("\n")
            .filter { it.isNotBlank() }
            .map { line ->
                val parts = line.split("|")
                DockerContainer(
                    id = parts.getOrNull(0) ?: "",
                    name = parts.getOrNull(1) ?: "",
                    image = parts.getOrNull(2) ?: "",
                    status = parts.getOrNull(3) ?: "",
                    ports = parts.getOrNull(4) ?: "",
                    created = parts.getOrNull(5) ?: ""
                )
            }

        return Result.success(containers)
    }

    suspend fun listImages(): Result<List<DockerImage>> {
        val command = "docker images --format \"{{.ID}}|{{.Repository}}|{{.Tag}}|{{.Size}}|{{.CreatedAt}}\""
        val result = shellExecutor.execute(command)

        if (result.exitCode != 0) {
            return Result.failure(Exception(result.error))
        }

        val images = result.output.split("\n")
            .filter { it.isNotBlank() }
            .map { line ->
                val parts = line.split("|")
                DockerImage(
                    id = parts.getOrNull(0) ?: "",
                    repository = parts.getOrNull(1) ?: "",
                    tag = parts.getOrNull(2) ?: "",
                    size = parts.getOrNull(3) ?: "",
                    created = parts.getOrNull(4) ?: ""
                )
            }

        return Result.success(images)
    }

    suspend fun startContainer(containerId: String): Result<String> {
        val result = shellExecutor.execute("docker start $containerId")
        return if (result.exitCode == 0) {
            Result.success("Container started: $containerId")
        } else {
            Result.failure(Exception(result.error))
        }
    }

    suspend fun stopContainer(containerId: String): Result<String> {
        val result = shellExecutor.execute("docker stop $containerId")
        return if (result.exitCode == 0) {
            Result.success("Container stopped: $containerId")
        } else {
            Result.failure(Exception(result.error))
        }
    }

    suspend fun restartContainer(containerId: String): Result<String> {
        val result = shellExecutor.execute("docker restart $containerId")
        return if (result.exitCode == 0) {
            Result.success("Container restarted: $containerId")
        } else {
            Result.failure(Exception(result.error))
        }
    }

    suspend fun removeContainer(containerId: String, force: Boolean = false): Result<String> {
        val command = if (force) "docker rm -f $containerId" else "docker rm $containerId"
        val result = shellExecutor.execute(command)

        return if (result.exitCode == 0) {
            Result.success("Container removed: $containerId")
        } else {
            Result.failure(Exception(result.error))
        }
    }

    suspend fun getContainerLogs(containerId: String, tail: Int = 100): Result<String> {
        val result = shellExecutor.execute("docker logs --tail $tail $containerId")
        return if (result.exitCode == 0) {
            Result.success(result.output)
        } else {
            Result.failure(Exception(result.error))
        }
    }

    suspend fun execInContainer(containerId: String, command: String): Result<String> {
        val result = shellExecutor.execute("docker exec $containerId $command")
        return if (result.exitCode == 0) {
            Result.success(result.output)
        } else {
            Result.failure(Exception(result.error))
        }
    }

    suspend fun inspectContainer(containerId: String): Result<String> {
        val result = shellExecutor.execute("docker inspect $containerId")
        return if (result.exitCode == 0) {
            Result.success(result.output)
        } else {
            Result.failure(Exception(result.error))
        }
    }

    suspend fun runContainer(
        image: String,
        name: String? = null,
        ports: Map<String, String>? = null,
        environment: Map<String, String>? = null,
        volumes: Map<String, String>? = null,
        detached: Boolean = true
    ): Result<String> {
        val commandParts = mutableListOf("docker run")

        if (detached) commandParts.add("-d")

        name?.let { commandParts.add("--name $it") }

        ports?.forEach { (host, container) ->
            commandParts.add("-p $host:$container")
        }

        environment?.forEach { (key, value) ->
            commandParts.add("-e $key=\"$value\"")
        }

        volumes?.forEach { (host, container) ->
            commandParts.add("-v $host:$container")
        }

        commandParts.add(image)

        val command = commandParts.joinToString(" ")
        val result = shellExecutor.execute(command)

        return if (result.exitCode == 0) {
            Result.success("Container created: ${result.output.trim()}")
        } else {
            Result.failure(Exception(result.error))
        }
    }

    suspend fun pullImage(image: String): Result<String> {
        val result = shellExecutor.execute("docker pull $image")
        return if (result.exitCode == 0) {
            Result.success("Image pulled: $image")
        } else {
            Result.failure(Exception(result.error))
        }
    }

    suspend fun removeImage(imageId: String, force: Boolean = false): Result<String> {
        val command = if (force) "docker rmi -f $imageId" else "docker rmi $imageId"
        val result = shellExecutor.execute(command)

        return if (result.exitCode == 0) {
            Result.success("Image removed: $imageId")
        } else {
            Result.failure(Exception(result.error))
        }
    }

    suspend fun buildImage(
        dockerfile: String,
        tag: String,
        context: String = "."
    ): Result<String> {
        val result = shellExecutor.execute("docker build -f $dockerfile -t $tag $context")
        return if (result.exitCode == 0) {
            Result.success("Image built: $tag")
        } else {
            Result.failure(Exception(result.error))
        }
    }

    suspend fun getContainerStats(containerId: String): Result<String> {
        val result = shellExecutor.execute("docker stats --no-stream $containerId")
        return if (result.exitCode == 0) {
            Result.success(result.output)
        } else {
            Result.failure(Exception(result.error))
        }
    }

    suspend fun pruneContainers(): Result<String> {
        val result = shellExecutor.execute("docker container prune -f")
        return if (result.exitCode == 0) {
            Result.success("Containers pruned")
        } else {
            Result.failure(Exception(result.error))
        }
    }

    suspend fun pruneImages(): Result<String> {
        val result = shellExecutor.execute("docker image prune -f")
        return if (result.exitCode == 0) {
            Result.success("Images pruned")
        } else {
            Result.failure(Exception(result.error))
        }
    }

    suspend fun composeUp(composeFile: String, detached: Boolean = true): Result<String> {
        val command = if (detached) {
            "docker-compose -f $composeFile up -d"
        } else {
            "docker-compose -f $composeFile up"
        }

        val result = shellExecutor.execute(command)
        return if (result.exitCode == 0) {
            Result.success("Docker Compose started")
        } else {
            Result.failure(Exception(result.error))
        }
    }

    suspend fun composeDown(composeFile: String): Result<String> {
        val result = shellExecutor.execute("docker-compose -f $composeFile down")
        return if (result.exitCode == 0) {
            Result.success("Docker Compose stopped")
        } else {
            Result.failure(Exception(result.error))
        }
    }
}
