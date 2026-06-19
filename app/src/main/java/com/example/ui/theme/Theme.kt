package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = SchoolGreenSecondary,
    secondary = SchoolTealTertiary,
    tertiary = SchoolGreenPrimary,
    background = SchoolDarkBg,
    surface = SchoolDarkSurface,
    onPrimary = SchoolDarkBg,
    onSecondary = SchoolMilkWhite,
    onBackground = SchoolMilkWhite,
    onSurface = SchoolMilkWhite
)

private val LightColorScheme = lightColorScheme(
    primary = SchoolGreenPrimary,
    secondary = SchoolTealTertiary,
    tertiary = SchoolGreenSecondary,
    background = SchoolCreamBg,
    surface = SchoolMilkWhite,
    onPrimary = SchoolMilkWhite,
    onSecondary = SchoolMilkWhite,
    onBackground = Color.Black,
    onSurface = Color.Black
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // false to enforce custom play branding
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
