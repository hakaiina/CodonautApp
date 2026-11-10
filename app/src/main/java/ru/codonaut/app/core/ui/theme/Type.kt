package ru.codonaut.app.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import ru.codonaut.app.R

val Rubik = FontFamily(
    Font(R.font.rubik_regular, FontWeight.Normal),
    Font(R.font.rubik_medium, FontWeight.Medium),
    Font(R.font.rubik_semibold, FontWeight.SemiBold),
    Font(R.font.rubik_bold, FontWeight.Bold)
)

val JetBrainsMono = FontFamily(
    Font(R.font.jetbrains_mono_regular, FontWeight.Normal)
)

private fun rubikStyle(
    size: Int,
    lineHeight: Int,
    weight: FontWeight
) = TextStyle(
    fontFamily = Rubik,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp
)

val CodonautTypography = Typography(
    displayLarge = rubikStyle(32, 40, FontWeight.Bold),
    displayMedium = rubikStyle(32, 40, FontWeight.Bold),
    displaySmall = rubikStyle(30, 38, FontWeight.Bold),

    headlineLarge = rubikStyle(30, 38, FontWeight.Bold),
    headlineMedium = rubikStyle(26, 34, FontWeight.SemiBold),
    headlineSmall = rubikStyle(24, 32, FontWeight.SemiBold),

    titleLarge = rubikStyle(20, 28, FontWeight.Medium),
    titleMedium = rubikStyle(18, 26, FontWeight.Medium),
    titleSmall = rubikStyle(16, 24, FontWeight.Medium),

    bodyLarge = rubikStyle(18, 28, FontWeight.Normal),
    bodyMedium = rubikStyle(16, 24, FontWeight.Normal),
    bodySmall = rubikStyle(14, 20, FontWeight.Normal),

    labelLarge = rubikStyle(14, 20, FontWeight.SemiBold),
    labelMedium = rubikStyle(12, 16, FontWeight.Medium),
    labelSmall = rubikStyle(12, 16, FontWeight.Normal)
)

val CodonautCodeStyle = TextStyle(
    fontFamily = JetBrainsMono,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 22.sp
)