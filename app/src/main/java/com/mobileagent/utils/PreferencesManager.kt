package com.mobileagent.utils

import android.content.Context
import android.content.SharedPreferences
import com.mobileagent.MobileAgentApplication

class PreferencesManager {
    private val prefs: SharedPreferences = MobileAgentApplication.getAppContext()
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var apiKey: String?
        get() = prefs.getString(KEY_API_KEY, null)
        set(value) = prefs.edit().putString(KEY_API_KEY, value).apply()

    var defaultModel: String
        get() = prefs.getString(KEY_MODEL, DEFAULT_MODEL) ?: DEFAULT_MODEL
        set(value) = prefs.edit().putString(KEY_MODEL, value).apply()

    var maxTokens: Int
        get() = prefs.getInt(KEY_MAX_TOKENS, DEFAULT_MAX_TOKENS)
        set(value) = prefs.edit().putInt(KEY_MAX_TOKENS, value).apply()

    var temperature: Float
        get() = prefs.getFloat(KEY_TEMPERATURE, DEFAULT_TEMPERATURE)
        set(value) = prefs.edit().putFloat(KEY_TEMPERATURE, value).apply()

    fun clearApiKey() {
        prefs.edit().remove(KEY_API_KEY).apply()
    }

    companion object {
        private const val PREFS_NAME = "mobile_agent_prefs"
        private const val KEY_API_KEY = "api_key"
        private const val KEY_MODEL = "model"
        private const val KEY_MAX_TOKENS = "max_tokens"
        private const val KEY_TEMPERATURE = "temperature"

        private const val DEFAULT_MODEL = "claude-sonnet-4-5-20250929"
        private const val DEFAULT_MAX_TOKENS = 4096
        private const val DEFAULT_TEMPERATURE = 1.0f

        @Volatile
        private var instance: PreferencesManager? = null

        fun getInstance(): PreferencesManager {
            return instance ?: synchronized(this) {
                instance ?: PreferencesManager().also { instance = it }
            }
        }
    }
}
