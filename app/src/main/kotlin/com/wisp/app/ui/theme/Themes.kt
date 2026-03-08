package com.wisp.app.ui.theme

import androidx.compose.ui.graphics.Color

object Themes {
    val themes = listOf(
        ThemePreset(
            name = "wisp",
            displayName = "Wisp",
            dark = ThemeColors(
                primary = Color(0xFFFF9800),
                secondary = Color(0xFFFFB74D),
                background = Color(0xFF131215),
                surface = Color(0xFF1F1E21),
                surfaceVariant = Color(0xFF2B2A2E),
                onBackground = Color(0xFFE0E0E0),
                onSurface = Color(0xFFE0E0E0),
                onSurfaceVariant = Color(0xFF9998A0),
                outline = Color(0xFF343338)
            ),
            light = ThemeColors(
                primary = Color(0xFFFF9800),
                secondary = Color(0xFFFFB74D),
                background = Color(0xFFECECEC),
                surface = Color(0xFFF5F5F5),
                surfaceVariant = Color(0xFFE0E0E0),
                onBackground = Color(0xFF1C1B1F),
                onSurface = Color(0xFF1C1B1F),
                onSurfaceVariant = Color(0xFF6B6B6B),
                outline = Color(0xFFCCCCCC)
            )
        ),
        ThemePreset(
            name = "nord",
            displayName = "Nord",
            dark = ThemeColors(
                primary = Color(0xFF88C0D0),
                secondary = Color(0xFF81A1C1),
                background = Color(0xFF2E3440),
                surface = Color(0xFF3B4252),
                surfaceVariant = Color(0xFF434C5E),
                onBackground = Color(0xFFECEFF4),
                onSurface = Color(0xFFECEFF4),
                onSurfaceVariant = Color(0xFFD8DEE9),
                outline = Color(0xFF4C566A)
            ),
            light = ThemeColors(
                primary = Color(0xFF5E81AC),
                secondary = Color(0xFF81A1C1),
                background = Color(0xFFECEFF4),
                surface = Color(0xFFE5E9F0),
                surfaceVariant = Color(0xFFD8DEE9),
                onBackground = Color(0xFF2E3440),
                onSurface = Color(0xFF2E3440),
                onSurfaceVariant = Color(0xFF4C566A),
                outline = Color(0xFFD8DEE9)
            )
        ),
        ThemePreset(
            name = "dracula",
            displayName = "Dracula",
            dark = ThemeColors(
                primary = Color(0xFFBD93F9),
                secondary = Color(0xFFFF79C6),
                background = Color(0xFF282A36),
                surface = Color(0xFF383A59),
                surfaceVariant = Color(0xFF44475A),
                onBackground = Color(0xFFF8F8F2),
                onSurface = Color(0xFFF8F8F2),
                onSurfaceVariant = Color(0xFF6272A4),
                outline = Color(0xFF6272A4)
            ),
            light = ThemeColors(
                primary = Color(0xFFBD93F9),
                secondary = Color(0xFFFF79C6),
                background = Color(0xFFF8F8F2),
                surface = Color(0xFFFFFFFF),
                surfaceVariant = Color(0xFFE8E8E8),
                onBackground = Color(0xFF282A36),
                onSurface = Color(0xFF282A36),
                onSurfaceVariant = Color(0xFF6272A4),
                outline = Color(0xFFD8D8D8)
            )
        ),
        ThemePreset(
            name = "gruvbox",
            displayName = "Gruvbox",
            dark = ThemeColors(
                primary = Color(0xFFFB4934),
                secondary = Color(0xFFFE8019),
                background = Color(0xFF282828),
                surface = Color(0xFF3C3836),
                surfaceVariant = Color(0xFF504945),
                onBackground = Color(0xFFEBDBB2),
                onSurface = Color(0xFFEBDBB2),
                onSurfaceVariant = Color(0xFFA89984),
                outline = Color(0xFF665C54)
            ),
            light = ThemeColors(
                primary = Color(0xFFCC241D),
                secondary = Color(0xFFFE8019),
                background = Color(0xFFFBF1C7),
                surface = Color(0xFFEBDBB2),
                surfaceVariant = Color(0xFFD5C4A1),
                onBackground = Color(0xFF3C3836),
                onSurface = Color(0xFF3C3836),
                onSurfaceVariant = Color(0xFF665C54),
                outline = Color(0xFFD5C4A1)
            )
        ),
        ThemePreset(
            name = "solarized",
            displayName = "Solarized",
            dark = ThemeColors(
                primary = Color(0xFF268BD2),
                secondary = Color(0xFF2AA198),
                background = Color(0xFF002B36),
                surface = Color(0xFF073642),
                surfaceVariant = Color(0xFF586E75),
                onBackground = Color(0xFF839496),
                onSurface = Color(0xFF93A1A1),
                onSurfaceVariant = Color(0xFF657B83),
                outline = Color(0xFF586E75)
            ),
            light = ThemeColors(
                primary = Color(0xFF268BD2),
                secondary = Color(0xFF2AA198),
                background = Color(0xFFFDF6E3),
                surface = Color(0xFFEEE8D5),
                surfaceVariant = Color(0xFF93A1A1),
                onBackground = Color(0xFF657B83),
                onSurface = Color(0xFF586E75),
                onSurfaceVariant = Color(0xFF839496),
                outline = Color(0xFF93A1A1)
            )
        ),
        ThemePreset(
            name = "catppuccin",
            displayName = "Catppuccin",
            dark = ThemeColors(
                primary = Color(0xFF89B4FA),
                secondary = Color(0xFFCBA6F7),
                background = Color(0xFF1E1E2E),
                surface = Color(0xFF313244),
                surfaceVariant = Color(0xFF45475A),
                onBackground = Color(0xFFCDD6F4),
                onSurface = Color(0xFFCDD6F4),
                onSurfaceVariant = Color(0xFFBAC2DE),
                outline = Color(0xFF585B70)
            ),
            light = ThemeColors(
                primary = Color(0xFF1E66F5),
                secondary = Color(0xFF8839EF),
                background = Color(0xFFEFF1F5),
                surface = Color(0xFFE6E9EF),
                surfaceVariant = Color(0xFFCCD0DA),
                onBackground = Color(0xFF4C4F69),
                onSurface = Color(0xFF4C4F69),
                onSurfaceVariant = Color(0xFF6C6F85),
                outline = Color(0xFFCCD0DA)
            )
        ),
        ThemePreset(
            name = "monokai",
            displayName = "Monokai",
            dark = ThemeColors(
                primary = Color(0xFFF92672),
                secondary = Color(0xFFA6E22E),
                background = Color(0xFF272822),
                surface = Color(0xFF3E3D32),
                surfaceVariant = Color(0xFF49483E),
                onBackground = Color(0xFFF8F8F2),
                onSurface = Color(0xFFF8F8F2),
                onSurfaceVariant = Color(0xFF75715E),
                outline = Color(0xFF49483E)
            ),
            light = ThemeColors(
                primary = Color(0xFFFC3D68),
                secondary = Color(0xFFA6E22E),
                background = Color(0xFFFFFAF8),
                surface = Color(0xFFF5F5F5),
                surfaceVariant = Color(0xFFE8E8E8),
                onBackground = Color(0xFF272822),
                onSurface = Color(0xFF272822),
                onSurfaceVariant = Color(0xFF75715E),
                outline = Color(0xFFE0E0E0)
            )
        ),
        ThemePreset(
            name = "onedark",
            displayName = "One Dark",
            dark = ThemeColors(
                primary = Color(0xFF61AFEF),
                secondary = Color(0xFFC678DD),
                background = Color(0xFF282C34),
                surface = Color(0xFF21252B),
                surfaceVariant = Color(0xFF2C313C),
                onBackground = Color(0xFFABB2BF),
                onSurface = Color(0xFFABB2BF),
                onSurfaceVariant = Color(0xFF5C6370),
                outline = Color(0xFF4B5263)
            ),
            light = ThemeColors(
                primary = Color(0xFF61AFEF),
                secondary = Color(0xFFC678DD),
                background = Color(0xFFFAFAFA),
                surface = Color(0xFFF0F0F0),
                surfaceVariant = Color(0xFFE0E0E0),
                onBackground = Color(0xFF282C34),
                onSurface = Color(0xFF282C34),
                onSurfaceVariant = Color(0xFF5C6370),
                outline = Color(0xFFD0D0D0)
            )
        ),
        ThemePreset(
            name = "tokyonight",
            displayName = "Tokyo Night",
            dark = ThemeColors(
                primary = Color(0xFF7AA2F7),
                secondary = Color(0xFFBB9AF7),
                background = Color(0xFF1A1B26),
                surface = Color(0xFF24283B),
                surfaceVariant = Color(0xFF414868),
                onBackground = Color(0xFFC0CAF5),
                onSurface = Color(0xFFC0CAF5),
                onSurfaceVariant = Color(0xFF565F89),
                outline = Color(0xFF414868)
            ),
            light = ThemeColors(
                primary = Color(0xFF7AA2F7),
                secondary = Color(0xFFBB9AF7),
                background = Color(0xFFEEF1F8),
                surface = Color(0xFFE8EAEF),
                surfaceVariant = Color(0xFFD0D4E0),
                onBackground = Color(0xFF1A1B26),
                onSurface = Color(0xFF1A1B26),
                onSurfaceVariant = Color(0xFF565F89),
                outline = Color(0xFFD0D4E0)
            )
        )
    )

    fun getTheme(name: String): ThemePreset = themes.find { it.name == name } ?: themes.first()
    fun getThemeNames(): List<String> = themes.map { it.name }
}

data class ThemePreset(
    val name: String,
    val displayName: String,
    val dark: ThemeColors,
    val light: ThemeColors
)

data class ThemeColors(
    val primary: Color,
    val secondary: Color,
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val onBackground: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val outline: Color
)
