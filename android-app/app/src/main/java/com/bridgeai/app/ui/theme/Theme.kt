package com.bridgeai.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = BridgeBlue,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    background = AppBackground,
    onBackground = CardText,
    surface = androidx.compose.ui.graphics.Color.White,
    onSurface = CardText,
    error = DangerRed,
)

private val DarkColors = darkColorScheme(
    primary = BridgeBlue,
)

@Composable
fun BridgeAiTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content,
    )
}
