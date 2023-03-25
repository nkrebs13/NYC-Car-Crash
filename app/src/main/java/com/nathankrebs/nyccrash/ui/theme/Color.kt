package com.nathankrebs.nyccrash.ui.theme

import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver

val DarkColorScheme = darkColors(
    primary = Color(0xFF5C6BC0),
    primaryVariant = Color(0xFF3949AB),
    secondary = Color(0xFFE91E63),
    secondaryVariant = Color(0xFF8E0038),
)

val LightColorScheme = lightColors(
    primary = Color(0xFF283593),
    primaryVariant = Color(0xFF001064),
    secondary = Color(0xFFC51162),
    secondaryVariant = Color(0xFF8E0038),
)

val statusBarColor: Color
    @Composable
    get() = getStatusBarColor(MaterialTheme.colors)

fun getStatusBarColor(colorScheme: Colors): Color =
    colorScheme.primary
        .copy(alpha = 0.3f)
        .compositeOver(colorScheme.background)
