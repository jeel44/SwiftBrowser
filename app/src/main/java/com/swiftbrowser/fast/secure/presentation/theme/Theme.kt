package com.swiftbrowser.fast.secure.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun SwiftBrowserTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = SwiftBlue,
            secondary = SwiftCyan,
            background = SwiftNavy,
            surface = SwiftNavySurface,
            surfaceVariant = SwiftNavyCard,
            onBackground = SwiftTextPrimary,
            onSurface = SwiftTextPrimary,
            onPrimary = Color.White,
            error = SwiftRed,
        ),
        typography = SwiftBrowserTypography,
        shapes = SwiftBrowserShapes,
        content = content,
    )
}
