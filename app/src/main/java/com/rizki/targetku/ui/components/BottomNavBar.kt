package com.rizki.targetku.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rizki.targetku.ui.theme.*

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : BottomNavItem("home", "Beranda", Icons.Filled.Home, Icons.Outlined.Home)
    object Academic : BottomNavItem("academic", "Akademik", Icons.Filled.MenuBook, Icons.Outlined.MenuBook)
    object Schedule : BottomNavItem("schedule", "Jadwal", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth)
    object AiTutor : BottomNavItem("ai_tutor", "AI Tutor", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome)
    object Profile : BottomNavItem("profile", "Profil", Icons.Filled.Person, Icons.Outlined.Person)
}

val bottomNavItems = listOf(
    BottomNavItem.Home,
    BottomNavItem.Academic,
    BottomNavItem.Schedule,
    BottomNavItem.AiTutor,
    BottomNavItem.Profile
)

@Composable
fun TargetKuBottomNav(
    currentRoute: String,
    onNavItemClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 24.dp,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                ambientColor = BabyBlue.copy(alpha = 0.3f),
                spotColor = SoftPink.copy(alpha = 0.2f)
            )
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(White, BabyBlueSurface)
                )
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomNavItems.forEach { item ->
                val isSelected = currentRoute == item.route
                BottomNavItemView(
                    item = item,
                    isSelected = isSelected,
                    onClick = { onNavItemClick(item.route) }
                )
            }
        }
    }
}

@Composable
private fun BottomNavItemView(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val animatedSize by animateDpAsState(
        targetValue = if (isSelected) 52.dp else 44.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "icon_size"
    )
    val bgAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(300),
        label = "bg_alpha"
    )
    val contentColor = if (isSelected) BabyBlueDark else TextMuted

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(animatedSize)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    color = when (item) {
                        is BottomNavItem.Home -> BabyBlue.copy(alpha = bgAlpha)
                        is BottomNavItem.Academic -> SoftPink.copy(alpha = bgAlpha)
                        is BottomNavItem.Schedule -> Lavender.copy(alpha = bgAlpha)
                        is BottomNavItem.AiTutor -> Mint.copy(alpha = bgAlpha)
                        is BottomNavItem.Profile -> Peach.copy(alpha = bgAlpha)
                        else -> BabyBlue.copy(alpha = bgAlpha)
                    }
                )
        ) {
            Icon(
                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                contentDescription = item.label,
                tint = if (isSelected) when (item) {
                    is BottomNavItem.Home -> BabyBlueDark
                    is BottomNavItem.Academic -> SoftPinkDark
                    is BottomNavItem.Schedule -> LavenderDark
                    is BottomNavItem.AiTutor -> Color(0xFF2ECC71)
                    is BottomNavItem.Profile -> PeachDark
                    else -> BabyBlueDark
                } else TextMuted,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(Modifier.height(2.dp))

        AnimatedVisibility(visible = isSelected) {
            Text(
                text = item.label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = when (item) {
                    is BottomNavItem.Home -> BabyBlueDark
                    is BottomNavItem.Academic -> SoftPinkDark
                    is BottomNavItem.Schedule -> LavenderDark
                    is BottomNavItem.AiTutor -> Color(0xFF2ECC71)
                    is BottomNavItem.Profile -> PeachDark
                    else -> BabyBlueDark
                }
            )
        }
    }
}
