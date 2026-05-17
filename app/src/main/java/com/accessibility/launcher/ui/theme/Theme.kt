package com.accessibility.launcher.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

val CustomColorScheme = lightColorScheme(
    primary = HighContrastBlue,
    background = Black,
    onBackground = HighContrastYellow
)

val CustomTypography = typography(
    bodyLarge = TextStyle(fontSize = 32.sp)
)

@Composable
fun AccessibilityLauncherTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CustomColorScheme,
        typography = CustomTypography,
        content = content
    )
}
