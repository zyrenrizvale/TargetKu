package com.rizki.targetku.ui.screens

import android.app.Application
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rizki.targetku.data.PreferencesManager
import com.rizki.targetku.ui.components.GlassCard
import com.rizki.targetku.ui.components.GradientCard
import com.rizki.targetku.ui.theme.*

@Composable
fun ProfileScreen(
    onEditProfile: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val prefsManager = remember { PreferencesManager(context) }
    val prefs = remember { mutableStateOf(prefsManager.getUserPrefs()) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Refresh when screen is shown
    LaunchedEffect(Unit) {
        prefs.value = prefsManager.getUserPrefs()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(BabyBlueSurface, OffWhite)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Profile Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(BabyBlueDark, Color(0xFF2563EB), LavenderDark)
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(SoftPinkLight, SoftPink)
                                )
                            )
                            .border(3.dp, White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        val initials = prefs.value.name
                            .split(" ")
                            .take(2)
                            .joinToString("") { it.take(1).uppercase() }
                        Text(
                            initials,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SoftPinkDark
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        prefs.value.name.ifEmpty { "Muhammad Rizki" },
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = White
                    )

                    Text(
                        prefs.value.schoolName.ifEmpty { "Sekolah belum diisi" },
                        fontSize = 14.sp,
                        color = White.copy(alpha = 0.85f)
                    )

                    Spacer(Modifier.height(12.dp))

                    // Target badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(White.copy(alpha = 0.2f))
                            .border(1.dp, White.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.GpsFixed, null, tint = SoftPink, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                prefs.value.targetCampus.ifEmpty { "Belum dipilih" },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = White
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Info Section
                Text("Informasi Profil", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = White.copy(alpha = 0.9f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        ProfileInfoRow(
                            icon = Icons.Default.Person,
                            iconColor = BabyBlueDark,
                            label = "Nama",
                            value = prefs.value.name.ifEmpty { "Belum diisi" }
                        )
                        ProfileDivider()
                        ProfileInfoRow(
                            icon = Icons.Default.School,
                            iconColor = SoftPinkDark,
                            label = "Sekolah",
                            value = prefs.value.schoolName.ifEmpty { "Belum diisi" }
                        )
                        ProfileDivider()
                        ProfileInfoRow(
                            icon = Icons.Default.TravelExplore,
                            iconColor = LavenderDark,
                            label = "Rencana Kuliah",
                            value = prefs.value.studyPlan.ifEmpty { "Belum dipilih" }
                        )
                        ProfileDivider()
                        ProfileInfoRow(
                            icon = Icons.Default.AccountBalance,
                            iconColor = BabyBlueDark,
                            label = "Kampus Target",
                            value = prefs.value.targetCampus.ifEmpty { "Belum dipilih" }
                        )
                        ProfileDivider()
                        ProfileInfoRow(
                            icon = Icons.Default.LocalLibrary,
                            iconColor = SoftPinkDark,
                            label = "Jurusan Target",
                            value = prefs.value.targetMajor.ifEmpty { "Belum dipilih" }
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                // Action Buttons
                Text("Pengaturan", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)

                ActionButton(
                    icon = Icons.Default.Edit,
                    iconColor = BabyBlueDark,
                    label = "Edit Personalisasi",
                    subtitle = "Ubah nama, sekolah, kampus dan jurusan target",
                    backgroundColor = BabyBlueLight,
                    onClick = onEditProfile
                )

                ActionButton(
                    icon = Icons.Default.Info,
                    iconColor = LavenderDark,
                    label = "Tentang TargetKu",
                    subtitle = "Versi 1.0 - Personal Study Planner",
                    backgroundColor = LavenderLight,
                    onClick = {}
                )

                ActionButton(
                    icon = Icons.Default.Logout,
                    iconColor = ErrorRose,
                    label = "Keluar",
                    subtitle = "Logout dari akun demo",
                    backgroundColor = Color(0xFFFFF0F2),
                    onClick = { showLogoutDialog = true }
                )

                Spacer(Modifier.height(8.dp))

                // Footer
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MenuBook, null, tint = BabyBlueDark, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Default.GpsFixed, null, tint = SoftPinkDark, modifier = Modifier.size(12.dp))
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("TargetKu - Raih Kampus Impianmu!", fontSize = 12.sp, color = TextMuted, textAlign = TextAlign.Center)
                    Text("v1.0.0 | Dibuat dengan semangat", fontSize = 11.sp, color = TextMuted)
                }

                Spacer(Modifier.height(24.dp))
            }
        }

        // Logout Confirmation
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = {
                    Text("Konfirmasi Keluar", fontWeight = FontWeight.ExtraBold)
                },
                text = {
                    Text("Apakah kamu yakin ingin keluar? Data kamu tetap tersimpan.", color = TextSecondary)
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showLogoutDialog = false
                            prefsManager.logout()
                            onLogout()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRose)
                    ) {
                        Text("Ya, Keluar")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showLogoutDialog = false }) {
                        Text("Batal")
                    }
                },
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@Composable
private fun ProfileInfoRow(
    icon: ImageVector,
    iconColor: Color,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Medium)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
    }
}

@Composable
private fun ProfileDivider() {
    Divider(
        modifier = Modifier.padding(vertical = 2.dp),
        color = Color(0xFFF1F5F9)
    )
}

@Composable
private fun ActionButton(
    icon: ImageVector,
    iconColor: Color,
    label: String,
    subtitle: String,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .border(1.dp, iconColor.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                Text(subtitle, fontSize = 12.sp, color = TextSecondary)
            }
            Icon(Icons.Default.ChevronRight, null, tint = TextMuted)
        }
    }
}
