package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun MizanTheme(
    themeMode: String = "SYSTEM",
    colorPalette: String = "EMERALD",
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        "DARK" -> true
        "LIGHT" -> false
        else -> isSystemInDarkTheme()
    }

    val colorScheme = when (colorPalette) {
        "OCEAN" -> if (isDark) {
            darkColorScheme(
                primary = OceanPrimaryDark,
                onPrimary = Color.Black,
                primaryContainer = Color(0xFF1E3A8A),
                onPrimaryContainer = Color(0xFFDBEAFE),
                secondary = Color(0xFF38BDF8),
                background = OceanBgDark,
                surface = OceanSurfaceDark,
                surfaceVariant = Color(0xFF1E293B),
                onBackground = Color(0xFFF1F5F9),
                onSurface = Color(0xFFF8FAFC)
            )
        } else {
            lightColorScheme(
                primary = OceanPrimaryLight,
                onPrimary = Color.White,
                primaryContainer = Color(0xFFDBEAFE),
                onPrimaryContainer = Color(0xFF1E3A8A),
                secondary = Color(0xFF0284C7),
                background = OceanBgLight,
                surface = OceanSurfaceLight,
                surfaceVariant = Color(0xFFF1F5F9),
                onBackground = Color(0xFF0F172A),
                onSurface = Color(0xFF0F172A)
            )
        }

        "GOLD" -> if (isDark) {
            darkColorScheme(
                primary = GoldPrimaryDark,
                onPrimary = Color.Black,
                primaryContainer = Color(0xFF78350F),
                onPrimaryContainer = Color(0xFFFEF3C7),
                secondary = Color(0xFFFCD34D),
                background = GoldBgDark,
                surface = GoldSurfaceDark,
                surfaceVariant = Color(0xFF322818),
                onBackground = Color(0xFFFEF3C7),
                onSurface = Color(0xFFFFFBEB)
            )
        } else {
            lightColorScheme(
                primary = GoldPrimaryLight,
                onPrimary = Color.White,
                primaryContainer = Color(0xFFFEF3C7),
                onPrimaryContainer = Color(0xFF78350F),
                secondary = Color(0xFFD97706),
                background = GoldBgLight,
                surface = GoldSurfaceLight,
                surfaceVariant = Color(0xFFFDF6E2),
                onBackground = Color(0xFF291E0A),
                onSurface = Color(0xFF291E0A)
            )
        }

        "ROSE" -> if (isDark) {
            darkColorScheme(
                primary = RosePrimaryDark,
                onPrimary = Color.Black,
                primaryContainer = Color(0xFF881337),
                onPrimaryContainer = Color(0xFFFFE4E6),
                secondary = Color(0xFFFDA4AF),
                background = RoseBgDark,
                surface = RoseSurfaceDark,
                surfaceVariant = Color(0xFF331620),
                onBackground = Color(0xFFFFE4E6),
                onSurface = Color(0xFFFFF1F2)
            )
        } else {
            lightColorScheme(
                primary = RosePrimaryLight,
                onPrimary = Color.White,
                primaryContainer = Color(0xFFFFE4E6),
                onPrimaryContainer = Color(0xFF881337),
                secondary = Color(0xFFE11D48),
                background = RoseBgLight,
                surface = RoseSurfaceLight,
                surfaceVariant = Color(0xFFFFF0F2),
                onBackground = Color(0xFF300B17),
                onSurface = Color(0xFF300B17)
            )
        }

        "PURPLE" -> if (isDark) {
            darkColorScheme(
                primary = PurplePrimaryDark,
                onPrimary = Color.Black,
                primaryContainer = Color(0xFF4C1D95),
                onPrimaryContainer = Color(0xFFEDE9FE),
                secondary = Color(0xFFC4B5FD),
                background = PurpleBgDark,
                surface = PurpleSurfaceDark,
                surfaceVariant = Color(0xFF2C1F45),
                onBackground = Color(0xFFEDE9FE),
                onSurface = Color(0xFFFAF5FF)
            )
        } else {
            lightColorScheme(
                primary = PurplePrimaryLight,
                onPrimary = Color.White,
                primaryContainer = Color(0xFFEDE9FE),
                onPrimaryContainer = Color(0xFF4C1D95),
                secondary = Color(0xFF7C3AED),
                background = PurpleBgLight,
                surface = PurpleSurfaceLight,
                surfaceVariant = Color(0xFFF5F0FF),
                onBackground = Color(0xFF1E1035),
                onSurface = Color(0xFF1E1035)
            )
        }

        else -> if (isDark) { // EMERALD (Default)
            darkColorScheme(
                primary = EmeraldPrimaryDark,
                onPrimary = Color(0xFF042F20),
                primaryContainer = Color(0xFF064E3B),
                onPrimaryContainer = Color(0xFFA7F3D0),
                secondary = Color(0xFF2DD4BF),
                background = EmeraldBgDark,
                surface = EmeraldSurfaceDark,
                surfaceVariant = Color(0xFF1E2F29),
                onBackground = Color(0xFFECFDF5),
                onSurface = Color(0xFFF0FDF4)
            )
        } else {
            lightColorScheme(
                primary = EmeraldPrimaryLight,
                onPrimary = Color.White,
                primaryContainer = Color(0xFFD1FAE5),
                onPrimaryContainer = Color(0xFF065F46),
                secondary = Color(0xFF0D9488),
                background = EmeraldBgLight,
                surface = EmeraldSurfaceLight,
                surfaceVariant = Color(0xFFEDF7F2),
                onBackground = Color(0xFF062319),
                onSurface = Color(0xFF062319)
            )
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
