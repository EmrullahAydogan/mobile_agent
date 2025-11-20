package com.mobileagent.automation

data class ScriptTemplate(
    val name: String,
    val description: String,
    val language: String, // python, javascript, bash
    val code: String,
    val category: String
)

object ScriptTemplates {
    val templates = listOf(
        // Python Templates
        ScriptTemplate(
            name = "HTTP Server",
            description = "Simple HTTP server on port 8000",
            language = "python",
            category = "Web",
            code = """
                #!/usr/bin/env python3
                import http.server
                import socketserver

                PORT = 8000

                Handler = http.server.SimpleHTTPRequestHandler
                with socketserver.TCPServer(("", PORT), Handler) as httpd:
                    print(f"Server running on port {PORT}")
                    httpd.serve_forever()
            """.trimIndent()
        ),

        ScriptTemplate(
            name = "Web Scraper",
            description = "Basic web scraper template",
            language = "python",
            category = "Web",
            code = """
                #!/usr/bin/env python3
                import requests
                from bs4 import BeautifulSoup

                url = "https://example.com"
                response = requests.get(url)
                soup = BeautifulSoup(response.content, 'html.parser')

                # Extract data
                titles = soup.find_all('h1')
                for title in titles:
                    print(title.text)
            """.trimIndent()
        ),

        ScriptTemplate(
            name = "File Organizer",
            description = "Organize files by extension",
            language = "python",
            category = "Utilities",
            code = """
                #!/usr/bin/env python3
                import os
                import shutil
                from pathlib import Path

                def organize_files(directory):
                    for file in Path(directory).iterdir():
                        if file.is_file():
                            ext = file.suffix[1:]  # Remove dot
                            if ext:
                                dest_dir = Path(directory) / ext
                                dest_dir.mkdir(exist_ok=True)
                                shutil.move(str(file), str(dest_dir / file.name))

                organize_files('.')
                print("Files organized!")
            """.trimIndent()
        ),

        ScriptTemplate(
            name = "JSON Formatter",
            description = "Pretty print JSON files",
            language = "python",
            category = "Data",
            code = """
                #!/usr/bin/env python3
                import json
                import sys

                def format_json(file_path):
                    with open(file_path, 'r') as f:
                        data = json.load(f)

                    print(json.dumps(data, indent=2, sort_keys=True))

                if len(sys.argv) > 1:
                    format_json(sys.argv[1])
                else:
                    print("Usage: script.py <json_file>")
            """.trimIndent()
        ),

        // JavaScript Templates
        ScriptTemplate(
            name = "Express Server",
            description = "Basic Express.js server",
            language = "javascript",
            category = "Web",
            code = """
                const express = require('express');
                const app = express();
                const PORT = 3000;

                app.use(express.json());

                app.get('/', (req, res) => {
                    res.json({ message: 'Hello World!' });
                });

                app.listen(PORT, () => {
                    console.log(`Server running on port ${'$'}{PORT}`);
                });
            """.trimIndent()
        ),

        ScriptTemplate(
            name = "API Client",
            description = "Fetch data from API",
            language = "javascript",
            category = "Web",
            code = """
                const fetch = require('node-fetch');

                async function fetchData(url) {
                    try {
                        const response = await fetch(url);
                        const data = await response.json();
                        console.log(data);
                        return data;
                    } catch (error) {
                        console.error('Error:', error);
                    }
                }

                fetchData('https://api.github.com/users/github');
            """.trimIndent()
        ),

        ScriptTemplate(
            name = "File Watcher",
            description = "Watch files for changes",
            language = "javascript",
            category = "Utilities",
            code = """
                const fs = require('fs');
                const path = require('path');

                const watchDir = process.argv[2] || '.';

                console.log(`Watching directory: ${'$'}{watchDir}`);

                fs.watch(watchDir, { recursive: true }, (eventType, filename) => {
                    if (filename) {
                        console.log(`${'$'}{eventType}: ${'$'}{filename}`);
                    }
                });
            """.trimIndent()
        ),

        // Bash Templates
        ScriptTemplate(
            name = "System Info",
            description = "Display system information",
            language = "bash",
            category = "System",
            code = """
                #!/bin/bash

                echo "System Information"
                echo "=================="
                echo "Hostname: $(hostname)"
                echo "OS: $(uname -s)"
                echo "Kernel: $(uname -r)"
                echo "Uptime: $(uptime -p)"
                echo "Memory: $(free -h | grep Mem | awk '{print $3 "/" $2}')"
                echo "Disk: $(df -h / | tail -1 | awk '{print $3 "/" $2 " (" $5 ")"}')"
            """.trimIndent()
        ),

        ScriptTemplate(
            name = "Backup Script",
            description = "Backup files to archive",
            language = "bash",
            category = "System",
            code = """
                #!/bin/bash

                SOURCE_DIR="$1"
                BACKUP_DIR="$2"
                DATE=$(date +%Y%m%d_%H%M%S)
                BACKUP_FILE="backup_${'$'}DATE.tar.gz"

                if [ -z "$SOURCE_DIR" ] || [ -z "$BACKUP_DIR" ]; then
                    echo "Usage: $0 <source_dir> <backup_dir>"
                    exit 1
                fi

                mkdir -p "$BACKUP_DIR"
                tar -czf "$BACKUP_DIR/$BACKUP_FILE" "$SOURCE_DIR"
                echo "Backup created: $BACKUP_DIR/$BACKUP_FILE"
            """.trimIndent()
        ),

        ScriptTemplate(
            name = "Git Auto Commit",
            description = "Auto commit and push changes",
            language = "bash",
            category = "Git",
            code = """
                #!/bin/bash

                MESSAGE="${'$'}{1:-Auto commit $(date +%Y-%m-%d %H:%M:%S)}"

                git add .
                git commit -m "$MESSAGE"
                git push

                echo "Changes committed and pushed"
            """.trimIndent()
        )
    )

    fun getTemplatesByLanguage(language: String): List<ScriptTemplate> {
        return templates.filter { it.language.equals(language, ignoreCase = true) }
    }

    fun getTemplatesByCategory(category: String): List<ScriptTemplate> {
        return templates.filter { it.category.equals(category, ignoreCase = true) }
    }

    fun getAllCategories(): List<String> {
        return templates.map { it.category }.distinct().sorted()
    }

    fun getAllLanguages(): List<String> {
        return templates.map { it.language }.distinct().sorted()
    }

    fun searchTemplates(query: String): List<ScriptTemplate> {
        return templates.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.description.contains(query, ignoreCase = true) ||
            it.category.contains(query, ignoreCase = true)
        }
    }
}
