package ru.codonaut.app.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val CodonautColorScheme = lightColorScheme(
    primary = CodonautColors.DeepTeal,
    onPrimary = CodonautColors.SoftCream,

    primaryContainer = CodonautColors.DarkCocoa,
    onPrimaryContainer = CodonautColors.SoftCream,

    secondary = CodonautColors.DarkCocoa,
    onSecondary = CodonautColors.SoftCream,

    secondaryContainer = CodonautColors.WarmStone,
    onSecondaryContainer = CodonautColors.EspressoBlack,

    tertiary = CodonautColors.Amber,
    onTertiary = CodonautColors.EspressoBlack,

    background = CodonautColors.WarmMist,
    onBackground = CodonautColors.EspressoBlack,

    surface = CodonautColors.SoftCream,
    onSurface = CodonautColors.EspressoBlack,

    surfaceVariant = CodonautColors.WarmStone,
    onSurfaceVariant = CodonautColors.MochaGray,

    outline = CodonautColors.Outline,
    outlineVariant = CodonautColors.WarmStone,

    error = CodonautColors.Error,
    onError = CodonautColors.SoftCream
)

@Composable
fun CodonautTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CodonautColorScheme,
        typography = CodonautTypography,
        shapes = CodonautShapes,
        content = content
    )
}