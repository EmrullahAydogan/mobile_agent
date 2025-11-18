# Mobile Agent

Android Native CLI Agent with AI capabilities - A Termux-like terminal emulator combined with Claude Code intelligence.

## Overview

Mobile Agent is a powerful Android application that combines a full-featured terminal emulator with Claude AI agent capabilities, allowing you to execute shell commands, manage files, and interact with an AI assistant directly on your Android device.

## Features

### Terminal Emulator
- Full terminal interface with command execution
- Built-in shell commands (ls, cd, cat, mkdir, rm, cp, mv, etc.)
- File system navigation and management
- Support for native Android shell commands

### AI Agent (Claude Integration)
- Conversational AI interface powered by Claude
- Context-aware assistance for programming tasks
- Ability to execute commands through AI
- Tool execution (file operations, command execution)

### Package Management
- Install and manage packages
- Support for Python, Node.js, Git, Vim, curl, and more
- Simple package installation and removal
- Package search functionality

### SSH Client
- Connect to remote servers via SSH
- Execute commands on remote machines
- File transfer (SFTP)
- Multiple connection management

### Runtime Support
- **Python 3**: Execute Python scripts and code
- **Node.js**: Run JavaScript/Node.js applications
- **npm**: Install and manage Node.js packages
- **pip**: Install Python packages

### File System Management
- Full file system access (with permissions)
- Read, write, copy, move operations
- Directory creation and navigation
- File metadata and permissions

## Architecture

```
com.mobileagent/
├── agent/                  # Claude AI integration
│   ├── models/            # API models and data classes
│   ├── ClaudeAgent.kt     # Main AI agent logic
│   ├── ClaudeApiService.kt
│   └── ClaudeRepository.kt
├── shell/                  # Terminal and shell execution
│   ├── ShellExecutor.kt   # Command execution engine
│   └── FileSystemManager.kt
├── packages/              # Package management
│   └── PackageManager.kt
├── network/               # SSH and network operations
│   └── SSHClient.kt
├── runtime/               # Runtime environments
│   ├── PythonRuntime.kt
│   ├── NodeRuntime.kt
│   └── RuntimeManager.kt
├── ui/                    # User interface
│   ├── screens/
│   │   ├── MainScreen.kt
│   │   ├── TerminalScreen.kt
│   │   └── AgentScreen.kt
│   └── theme/
└── viewmodel/             # ViewModels
    └── TerminalViewModel.kt
```

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel)
- **Networking**: Retrofit2 + OkHttp
- **SSH**: JSch
- **Minimum SDK**: 26 (Android 8.0)
- **Target SDK**: 34 (Android 14)

## Setup

### Prerequisites
- Android Studio (latest version)
- Android SDK 34
- Gradle 8.2+
- JDK 17

### Building the Project

1. Clone the repository:
```bash
git clone <repository-url>
cd mobile_agent
```

2. Open the project in Android Studio

3. Sync Gradle files

4. Configure Claude API key (see Configuration section)

5. Build and run:
```bash
./gradlew assembleDebug
```

## Configuration

### Claude API Key

The app requires a Claude API key to use AI features. You can configure it in two ways:

1. **Through the app**: Settings > API Configuration
2. **Via SharedPreferences**: The key is stored securely in app preferences

```kotlin
val prefsManager = PreferencesManager.getInstance()
prefsManager.apiKey = "your-api-key-here"
```

## Usage

### Terminal Mode

Launch the app and you'll see the terminal interface. Available commands:

```bash
# File operations
ls              # List files
cd <dir>        # Change directory
pwd             # Print working directory
cat <file>      # Display file contents
mkdir <dir>     # Create directory
touch <file>    # Create empty file
rm <file>       # Remove file
cp <src> <dst>  # Copy file
mv <src> <dst>  # Move/rename file

# Utilities
echo <text>     # Print text
clear           # Clear terminal
help            # Show help

# Native commands
<any-command>   # Execute native shell command
```

### AI Agent Mode

Switch to AI Agent mode using the brain icon in the toolbar. You can:

- Ask questions about programming
- Request file operations
- Get help with debugging
- Execute commands through natural language

Example conversations:
```
User: "List all Python files in the current directory"
Agent: [Executes ls *.py and shows results]

User: "Create a simple HTTP server script"
Agent: [Creates a Python script with HTTP server code]
```

### Package Management

```kotlin
val packageManager = PackageManager()

// List available packages
val packages = packageManager.listAvailablePackages()

// Install a package
packageManager.installPackage("python")

// Search for packages
val results = packageManager.searchPackages("node")
```

### SSH Connections

```kotlin
val sshClient = SSHClient()
val connection = SSHConnection(
    host = "example.com",
    username = "user",
    password = "password"
)

// Connect
sshClient.connect(connection)

// Execute command
val result = sshClient.executeCommand("ls -la")

// Transfer file
sshClient.transferFile(
    localPath = "/path/to/local",
    remotePath = "/path/to/remote",
    mode = SSHClient.TransferMode.UPLOAD
)
```

### Runtime Execution

```kotlin
val runtimeManager = RuntimeManager()

// Execute Python code
runtimeManager.executeCode(
    code = "print('Hello from Python')",
    runtime = "python"
)

// Execute Node.js code
runtimeManager.executeCode(
    code = "console.log('Hello from Node')",
    runtime = "node"
)

// Run a script
runtimeManager.executeScript("/path/to/script.py")
```

## Permissions

The app requires the following permissions:

- **INTERNET**: For API calls and network operations
- **READ_EXTERNAL_STORAGE**: To read files
- **WRITE_EXTERNAL_STORAGE**: To write files
- **MANAGE_EXTERNAL_STORAGE**: For full file system access (Android 11+)
- **FOREGROUND_SERVICE**: For background operations

## Security Considerations

- API keys are stored in encrypted SharedPreferences
- File operations are scoped to app directories by default
- SSH connections support key-based authentication
- All network calls use HTTPS

## Roadmap

- [ ] Tool calling support for Claude API
- [ ] More package repositories (custom sources)
- [ ] Syntax highlighting in terminal
- [ ] Tab completion
- [ ] Command history with arrow keys
- [ ] Multi-session support
- [ ] Plugin system
- [ ] Cloud sync for settings
- [ ] Gesture controls
- [ ] Theming options

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

[Your License Here]

## Acknowledgments

- Inspired by [Termux](https://termux.com/)
- Powered by [Claude AI](https://www.anthropic.com/)
- Built with [Jetpack Compose](https://developer.android.com/jetpack/compose)

## Contact

[Your Contact Information]

---

Made with ❤️ for Android developers and terminal enthusiasts