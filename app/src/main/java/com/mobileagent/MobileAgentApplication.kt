package com.mobileagent

import android.app.Application
import android.content.Context

class MobileAgentApplication : Application() {
    companion object {
        private lateinit var instance: MobileAgentApplication

        fun getAppContext(): Context = instance.applicationContext
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Initialize app directories
        initializeDirectories()
    }

    private fun initializeDirectories() {
        // Create necessary directories for the agent
        val homeDir = getExternalFilesDir(null)?.resolve("home")
        homeDir?.mkdirs()

        val binDir = homeDir?.resolve("bin")
        binDir?.mkdirs()

        val tmpDir = homeDir?.resolve("tmp")
        tmpDir?.mkdirs()
    }
}
