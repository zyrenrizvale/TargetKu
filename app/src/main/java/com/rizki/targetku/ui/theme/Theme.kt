package com.rizki.targetku.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TargetKuColorScheme = lightColorScheme(
    primary = BabyBlueDark,
    onPrimary = White,
    primaryContainer = BabyBlueLight,
    onPrimaryContainer = TextPrimary,

    secondary = SoftPinkDark,
    onSecondary = White,
    secondaryContainer = SoftPinkLight,
    onSecondaryContainer = TextPrimary,

    tertiary = LavenderDark,
    onTertiary = White,
    tertiaryContainer = LavenderLight,
    onTertiaryContainer = TextPrimary,

    background = BackgroundBlue,
    onBackground = TextPrimary,

    surface = White,
    onSurface = TextPrimary,
    surfaceVariant = BabyBlueSurface,
    onSurfaceVariant = TextSecondary,

    outline = BabyBlue,
    outlineVariant = Color(0xFFE2E8F0),

    error = ErrorRose,
    onError = White,
    errorContainer = Color(0xFFFFE4E6),
    onErrorContainer = Color(0xFF9F1239),

    inverseSurface = TextPrimary,
    inverseOnSurface = White,
    inversePrimary = BabyBlueLight,

    scrim = Color(0x80000000),
    surfaceTint = BabyBlue,
)

@Composable
fun TargetKuTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = TargetKuColorScheme,
        typography = TargetKuTypography,
        shapes = TargetKuShapes,
        content = content
    )
}
