package com.accessibility.launcher.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

val CustomColorScheme = lightColorScheme(
    primary = HighContrastBlue,
    background = Black,
    onBackground = HighContrastYellow
)

@Composable
fun AccessibilityLauncherTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CustomColorScheme,
        typography = AppTypography,
        content = content
    )
}
