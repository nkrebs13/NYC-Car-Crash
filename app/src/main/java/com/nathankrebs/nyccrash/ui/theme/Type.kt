package com.nathankrebs.nyccrash.ui.theme

import androidx.compose.material.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.nathankrebs.nyccrash.R

private val darkThemeColor = Color.White
private val lightThemeColor = Color.Black

val DarkTypography = Typography(
    h1 = TextStyle(
        color = darkThemeColor,
        fontWeight = FontWeight.W300,
        fontSize = 96.sp
    ),
    h2 = TextStyle(
        color = darkThemeColor,
        fontWeight = FontWeight.W400,
        fontSize = 60.sp
    ),
    h3 = TextStyle(
        color = darkThemeColor,
        fontWeight = FontWeight.W600,
        fontSize = 48.sp
    ),
    h4 = TextStyle(
        color = darkThemeColor,
        fontWeight = FontWeight.W600,
        fontSize = 34.sp
    ),
    h5 = TextStyle(
        color = darkThemeColor,
        fontWeight = FontWeight.W600,
        fontSize = 24.sp
    ),
    h6 = TextStyle(
        color = darkThemeColor,
        fontWeight = FontWeight.W400,
        fontSize = 20.sp
    ),
    subtitle1 = TextStyle(
        color = darkThemeColor,
        fontWeight = FontWeight.W500,
        fontSize = 16.sp
    ),
    subtitle2 = TextStyle(
        color = darkThemeColor,
        fontWeight = FontWeight.W600,
        fontSize = 14.sp
    ),
    body1 = TextStyle(
        color = darkThemeColor,
        fontWeight = FontWeight.W600,
        fontSize = 16.sp
    ),
    body2 = TextStyle(
        color = darkThemeColor,
        fontWeight = FontWeight.W400,
        fontSize = 14.sp
    ),
    button = TextStyle(
        color = darkThemeColor,
        fontWeight = FontWeight.W600,
        fontSize = 14.sp
    ),
    caption = TextStyle(
        color = darkThemeColor,
        fontWeight = FontWeight.W500,
        fontSize = 12.sp
    ),
    overline = TextStyle(
        color = darkThemeColor,
        fontWeight = FontWeight.W400,
        fontSize = 12.sp
    )
)

val LightTypography = DarkTypography.copy(
    h1 = DarkTypography.h1.copy(color = lightThemeColor),
    h2 = DarkTypography.h2.copy(color = lightThemeColor),
    h3 = DarkTypography.h3.copy(color = lightThemeColor),
    h4 = DarkTypography.h4.copy(color = lightThemeColor),
    h5 = DarkTypography.h5.copy(color = lightThemeColor),
    h6 = DarkTypography.h6.copy(color = lightThemeColor),
    subtitle1 = DarkTypography.subtitle1.copy(color = lightThemeColor),
    subtitle2 = DarkTypography.subtitle2.copy(color = lightThemeColor),
    body1 = DarkTypography.body1.copy(color = lightThemeColor),
    body2 = DarkTypography.body2.copy(color = lightThemeColor),
    button = DarkTypography.button.copy(color = lightThemeColor),
    caption = DarkTypography.caption.copy(color = lightThemeColor),
    overline = DarkTypography.overline.copy(color = lightThemeColor),
)