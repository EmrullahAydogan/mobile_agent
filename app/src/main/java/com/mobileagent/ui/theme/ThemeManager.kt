package com.mobileagent.ui.theme

import android.content.Context
import androidx.compose.ui.graphics.Color
import com.mobileagent.MobileAgentApplication

data class ThemeColors(
    val name: String,
    val background: Color,
    val surface: Color,
    val primary: Color,
    val secondary: Color,
    val text: Color,
    val textSecondary: Color,
    val error: Color,
    val success: Color,
    val warning: Color
)

object Themes {
    val DARK_DEFAULT = ThemeColors(
        name = "Dark (Default)",
        background = Color(0xFF0D1117),
        surface = Color(0xFF161B22),
        primary = Color(0xFF58A6FF),
        secondary = Color(0xFF43A047),
        text = Color(0xFFC9D1D9),
        textSecondary = Color(0xFF8B949E),
        error = Color(0xFFFF7B72),
        success = Color(0xFF7EE787),
        warning = Color(0xFFFFD580)
    )

    val DARK_DRACULA = ThemeColors(
        name = "Dracula",
        background = Color(0xFF282A36),
        surface = Color(0xFF44475A),
        primary = Color(0xFFBD93F9),
        secondary = Color(0xFF50FA7B),
        text = Color(0xFFF8F8F2),
        textSecondary = Color(0xFF6272A4),
        error = Color(0xFFFF5555),
        success = Color(0xFF50FA7B),
        warning = Color(0xFFF1FA8C)
    )

    val DARK_MONOKAI = ThemeColors(
        name = "Monokai",
        background = Color(0xFF272822),
        surface = Color(0xFF3E3D32),
        primary = Color(0xFFF92672),
        secondary = Color(0xFFA6E22E),
        text = Color(0xFFF8F8F2),
        textSecondary = Color(0xFF75715E),
        error = Color(0xFFF92672),
        success = Color(0xFFA6E22E),
        warning = Color(0xFFE6DB74)
    )

    val DARK_NORD = ThemeColors(
        name = "Nord",
        background = Color(0xFF2E3440),
        surface = Color(0xFF3B4252),
        primary = Color(0xFF88C0D0),
        secondary = Color(0xFFA3BE8C),
        text = Color(0xFFECEFF4),
        textSecondary = Color(0xFF4C566A),
        error = Color(0xFFBF616A),
        success = Color(0xFFA3BE8C),
        warning = Color(0xFFEBCB8B)
    )

    val DARK_GRUVBOX = ThemeColors(
        name = "Gruvbox Dark",
        background = Color(0xFF282828),
        surface = Color(0xFF3C3836),
        primary = Color(0xFFFE8019),
        secondary = Color(0xFFB8BB26),
        text = Color(0xFFFBF1C7),
        textSecondary = Color(0xFF928374),
        error = Color(0xFFFB4934),
        success = Color(0xFFB8BB26),
        warning = Color(0xFFFABD2F)
    )

    val LIGHT_DEFAULT = ThemeColors(
        name = "Light (Default)",
        background = Color(0xFFFFFFFF),
        surface = Color(0xFFF5F5F5),
        primary = Color(0xFF1E88E5),
        secondary = Color(0xFF43A047),
        text = Color(0xFF000000),
        textSecondary = Color(0xFF666666),
        error = Color(0xFFD32F2F),
        success = Color(0xFF388E3C),
        warning = Color(0xFFF57C00)
    )

    val LIGHT_SOLARIZED = ThemeColors(
        name = "Solarized Light",
        background = Color(0xFFFDF6E3),
        surface = Color(0xFFEEE8D5),
        primary = Color(0xFF268BD2),
        secondary = Color(0xFF859900),
        text = Color(0xFF657B83),
        textSecondary = Color(0xFF93A1A1),
        error = Color(0xFFDC322F),
        success = Color(0xFF859900),
        warning = Color(0xFFCB4B16)
    )

    val ALL_THEMES = listOf(
        DARK_DEFAULT,
        DARK_DRACULA,
        DARK_MONOKAI,
        DARK_NORD,
        DARK_GRUVBOX,
        LIGHT_DEFAULT,
        LIGHT_SOLARIZED
    )

    fun getThemeByName(name: String): ThemeColors? {
        return ALL_THEMES.find { it.name == name }
    }
}

class ThemeManager {
    private val prefs = MobileAgentApplication.getAppContext()
        .getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_THEME = "current_theme"
        private const val KEY_FONT_SIZE = "font_size"
        private const val KEY_LINE_HEIGHT = "line_height"

        @Volatile
        private var instance: ThemeManager? = null

        fun getInstance(): ThemeManager {
            return instance ?: synchronized(this) {
                instance ?: ThemeManager().also { instance = it }
            }
        }
    }

    var currentTheme: ThemeColors
        get() {
            val themeName = prefs.getString(KEY_THEME, Themes.DARK_DEFAULT.name)
            return Themes.getThemeByName(themeName ?: Themes.DARK_DEFAULT.name) ?: Themes.DARK_DEFAULT
        }
        set(value) {
            prefs.edit().putString(KEY_THEME, value.name).apply()
        }

    var fontSize: Int
        get() = prefs.getInt(KEY_FONT_SIZE, 14)
        set(value) {
            prefs.edit().putInt(KEY_FONT_SIZE, value.coerceIn(10, 24)).apply()
        }

    var lineHeight: Int
        get() = prefs.getInt(KEY_LINE_HEIGHT, 20)
        set(value) {
            prefs.edit().putInt(KEY_LINE_HEIGHT, value.coerceIn(14, 32)).apply()
        }

    fun isDarkTheme(): Boolean {
        return currentTheme.name.contains("Dark", ignoreCase = true) ||
               currentTheme.name.contains("Dracula", ignoreCase = true) ||
               currentTheme.name.contains("Monokai", ignoreCase = true) ||
               currentTheme.name.contains("Nord", ignoreCase = true) ||
               currentTheme.name.contains("Gruvbox", ignoreCase = true)
    }
}
