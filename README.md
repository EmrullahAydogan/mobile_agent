# Mobile Agent

Android Native CLI Agent with AI capabilities - A Termux-like terminal emulator combined with Claude Code intelligence.

## Overview

Mobile Agent is a powerful Android application that combines a full-featured terminal emulator with Claude AI agent capabilities, allowing you to execute shell commands, manage files, and interact with an AI assistant directly on your Android device.

## ✨ Features

### 🖥️ Advanced Terminal Emulator
- **Full terminal interface** with command execution
- **Syntax highlighting** for commands, arguments, and output
- **Command history** with up/down navigation (500+ commands)
- **Auto-completion** for commands and file paths (Tab support)
- **Multiple terminal sessions** (tabs support)
- Built-in shell commands (ls, cd, cat, mkdir, rm, cp, mv, etc.)
- File system navigation and management
- Support for native Android shell commands

### 🤖 AI Agent (Claude Integration with Tool Calling)
- **Conversational AI** interface powered by Claude Sonnet 4.5
- **Automatic tool calling** - Claude can actually execute commands!
- **8 Built-in tools**:
  - execute_command: Run shell commands
  - read_file / write_file: File operations
  - list_files / create_directory / delete_file: File management
  - run_python / run_javascript: Code execution
- Context-aware assistance for programming tasks
- Multi-turn conversations with tool result feedback
- Real-time tool execution display

### 📦 Package Management
- Install and manage packages
- Support for Python, Node.js, Git, Vim, curl, and more
- Simple package installation and removal
- Package search functionality
- Dependency management

### 🔐 SSH Client
- Connect to remote servers via SSH
- Execute commands on remote machines
- File transfer (SFTP)
- Multiple connection management
- Key-based authentication support

### 🐍 Runtime Support & REPL
- **Python 3**: Execute Python scripts and code
- **Node.js**: Run JavaScript/Node.js applications
- **Interactive REPL** for Python, JavaScript, and Bash
- Context preservation for multi-line code
- **npm**: Install and manage Node.js packages
- **pip**: Install Python packages
- Script execution with arguments

### 📁 File System Management
- **Visual File Browser** with touch gestures
- Context menus (long press)
- Create files and folders
- Full file system access (with permissions)
- Read, write, copy, move operations
- Directory creation and navigation
- File metadata and permissions
- Search and filter

### ✏️ Code Editor
- Integrated code editor with line numbers
- Syntax support for multiple languages
- Auto-save and change detection
- Search and replace (coming soon)
- Multiple file editing
- Monospace font with adjustable size

### 🔄 Git Integration
- Full Git client functionality
- View status, branches, commit history
- Stage, commit, push, pull operations
- Branch management (create, switch, merge)
- Diff viewer
- Clone repositories
- Remote operations

### 🌐 HTTP Client
- **Postman-like HTTP client**
- Support for GET, POST, PUT, DELETE, PATCH
- Custom headers and body
- Response viewing with metadata
- Request history (last 50 requests)
- Response time tracking
- Multiple content types

### 🐳 Docker Management
- **Full Docker support**
- List containers and images
- Start, stop, restart containers
- View logs and stats
- Execute commands in containers
- Build and pull images
- Docker Compose support
- Container inspection

### 🗄️ Database Client (SQLite)
- **SQLite database browser**
- Execute SQL queries
- View table structure and data
- Insert, update, delete rows
- Create and drop tables
- Export to SQL file
- Query result visualization
- Transaction support

### 📝 Script Templates
- **10+ ready-to-use script templates**:
  - HTTP Server (Python)
  - Web Scraper (Python)
  - File Organizer (Python)
  - JSON Formatter (Python)
  - Express Server (Node.js)
  - API Client (Node.js)
  - File Watcher (Node.js)
  - System Info (Bash)
  - Backup Script (Bash)
  - Git Auto Commit (Bash)
- Categories: Web, Utilities, Data, System, Git
- Searchable template library

### ⏰ Task Automation
- **Cron-like task scheduler**
- Schedule: Once, Daily, Weekly, Interval
- Background execution
- Task management (enable/disable)
- Notification support

### 🎨 Theme Customization
- **7 built-in themes**:
  - Dark (Default GitHub style)
  - Dracula
  - Monokai
  - Nord
  - Gruvbox Dark
  - Light (Default)
  - Solarized Light
- Adjustable font size (10-24px)
- Adjustable line height
- Custom color schemes
- Per-theme terminal colors

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