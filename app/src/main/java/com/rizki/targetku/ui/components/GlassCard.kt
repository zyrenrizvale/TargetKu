package com.rizki.targetku.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rizki.targetku.ui.theme.*

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    backgroundColor: Color = CardGlass,
    borderColor: Color = CardBorder,
    shadowElevation: Dp = 8.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = shadowElevation,
                shape = RoundedCornerShape(cornerRadius),
                ambientColor = BabyBlue.copy(alpha = 0.15f),
                spotColor = SoftPink.copy(alpha = 0.1f)
            )
            .clip(RoundedCornerShape(cornerRadius))
            .background(color = backgroundColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(cornerRadius)
            ),
        content = content
    )
}

@Composable
fun GradientCard(
    modifier: Modifier = Modifier,
    gradientColors: List<Color> = listOf(BabyBlueLight, SoftPinkLight),
    cornerRadius: Dp = 16.dp,
    shadowElevation: Dp = 6.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = shadowElevation,
                shape = RoundedCornerShape(cornerRadius),
                ambientColor = gradientColors.firstOrNull()?.copy(alpha = 0.2f) ?: BabyBlue.copy(alpha = 0.2f),
                spotColor = gradientColors.lastOrNull()?.copy(alpha = 0.15f) ?: SoftPink.copy(alpha = 0.15f)
            )
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                brush = Brush.linearGradient(colors = gradientColors)
            )
            .border(
                width = 1.dp,
                color = White.copy(alpha = 0.6f),
                shape = RoundedCornerShape(cornerRadius)
            ),
        content = content
    )
}

@Composable
fun PastelCard(
    modifier: Modifier = Modifier,
    color: Color = BabyBlue,
    cornerRadius: Dp = 16.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(cornerRadius),
                ambientColor = color.copy(alpha = 0.3f),
                spotColor = color.copy(alpha = 0.2f)
            )
            .clip(RoundedCornerShape(cornerRadius))
            .background(color = color.copy(alpha = 0.3f))
            .border(
                width = 1.dp,
                color = color.copy(alpha = 0.5f),
                shape = RoundedCornerShape(cornerRadius)
            ),
        content = content
    )
}
