package com.rutapay.devapp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.rutapay.devapp.R

// Definimos la familia de fuente Lexend
val Lexend = FontFamily(
    Font(R.font.lexend_thin, FontWeight.Thin),
    Font(R.font.lexend_light, FontWeight.Light),
    Font(R.font.lexend_regular, FontWeight.Normal),
    Font(R.font.lexend_medium, FontWeight.Medium),
    Font(R.font.lexend_semibold, FontWeight.SemiBold),
    Font(R.font.lexend_bold, FontWeight.Bold),
    Font(R.font.lexend_black, FontWeight.Black)
)

private val defaultTypography = Typography()

val Typography = Typography(
    displayLarge = defaultTypography.displayLarge.copy(fontFamily = Lexend),
    displayMedium = defaultTypography.displayMedium.copy(fontFamily = Lexend),
    displaySmall = defaultTypography.displaySmall.copy(fontFamily = Lexend),

    headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = Lexend),
    headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = Lexend),
    headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = Lexend),

    titleLarge = defaultTypography.titleLarge.copy(fontFamily = Lexend),
    titleMedium = defaultTypography.titleMedium.copy(fontFamily = Lexend),
    titleSmall = defaultTypography.titleSmall.copy(fontFamily = Lexend),

    bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = Lexend),
    bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = Lexend),
    bodySmall = defaultTypography.bodySmall.copy(fontFamily = Lexend),

    labelLarge = defaultTypography.labelLarge.copy(fontFamily = Lexend),
    labelMedium = defaultTypography.labelMedium.copy(fontFamily = Lexend),
    labelSmall = defaultTypography.labelSmall.copy(fontFamily = Lexend)
)
