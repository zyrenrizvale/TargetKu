package com.rizki.targetku.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import android.app.Application
import com.rizki.targetku.data.models.ScheduleItem
import com.rizki.targetku.viewmodel.HomeViewModel
import com.rizki.targetku.viewmodel.TargetKuViewModelFactory
import com.rizki.targetku.ui.theme.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    onNavigateTo: (String) -> Unit = {},
    viewModel: HomeViewModel = viewModel(
        factory = TargetKuViewModelFactory(LocalContext.current.applicationContext as Application)
    )
) {
    val state by viewModel.collectAsStateWithLifecycle()
    
    LaunchedEffect(Unit) { viewModel.loadData() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ─── Header ───────────────────────────────────────
            HomeHeader(
                name = state.userName,
                targetCampus = state.targetCampus
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                // ─── UTBK Countdown ───────────────────────────
                UtbkCountdownCard(
                    countdownDays = state.utbkCountdownDays,
                    utbkDate = state.utbkDate,
                    onSetDate = { viewModel.showUtbkDialog() }
                )

                // ─── Stats Row ────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatMiniCard(
                        icon = Icons.Default.LocalFire,
                        label = "Streak",
                        value = "${state.studyStreak} hari",
                        color = Color(0xFFFF6B35),
                        modifier = Modifier.weight(1f)
                    )
                    StatMiniCard(
                        icon = Icons.Default.Star,
                        label = "Rata-rata",
                        value = if (state.averageGrade > 0) DecimalFormat("#.#").format(state.averageGrade) else "-",
                        color = Color(0xFFFFB800),
                        modifier = Modifier.weight(1f)
                    )
                    StatMiniCard(
                        icon = Icons.Default.TaskAlt,
                        label = "Selesai",
                        value = "${state.tasksDone} tugas",
                        color = Color(0xFF00C896),
                        modifier = Modifier.weight(1f)
                    )
                }

                // ─── Today's Schedule ─────────────────────────
                TodayScheduleSection(
                    dayName = state.todayDayName,
                    scheduleItems = state.todaySchedule,
                    onViewAll = { onNavigateTo("schedule") }
                )

                // ─── Quick Actions ────────────────────────────
                QuickActionsSection(onNavigateTo = onNavigateTo)

                // ─── Daily Quote ──────────────────────────────
                QuoteCard(quote = state.dailyQuote, author = state.dailyQuoteAuthor)

                // ─── Quick Note ───────────────────────────────
                QuickNoteWidget(
                    note = state.dailyNote,
                    onNoteChange = viewModel::onNoteChange,
                    onSave = viewModel::saveNote,
                    isSaving = state.isSavingNote,
                    saveSuccess = state.noteSavedSuccess
                )

                Spacer(Modifier.height(8.dp))
            }
        }
    }

    // ─── UTBK Date Dialog ─────────────────────────────────────
    if (state.showUtbkDialog) {
        AlertDialog(
            onDismissRequest = viewModel::hideUtbkDialog,
            title = {
                Text("Set Tanggal UTBK", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text("Format: DD/MM/YYYY", fontSize = 12.sp, color = TextSecondary)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.utbkDateInput,
                        onValueChange = viewModel::onUtbkDateInputChange,
                        placeholder = { Text("Contoh: 15/05/2027") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = viewModel::saveUtbkDate,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BabyBlueDark)
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::hideUtbkDialog) {
                    Text("Batal")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

// ─── Composable: collect state ─────────────────────────────────
@Composable
private fun HomeViewModel.collectAsStateWithLifecycle() =
    state.collectAsStateWithLifecycle().value

// ─── Header ────────────────────────────────────────────────────
@Composable
private fun HomeHeader(name: String, targetCampus: String) {
    val dayOfWeek = SimpleDateFormat("EEEE", Locale("id")).format(Date())
    val date = SimpleDateFormat("d MMMM yyyy", Locale("id")).format(Date())
    val greeting = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 0..10 -> "Selamat Pagi"
        in 11..14 -> "Selamat Siang"
        in 15..17 -> "Selamat Sore"
        else -> "Selamat Malam"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF667EEA), Color(0xFF764BA2))
                )
            )
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        // Decorative circle
        Box(
            modifier = Modifier
                .size(100.dp)
                .offset(x = 260.dp, y = (-20).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.08f))
        )
        Box(
            modifier = Modifier
                .size(60.dp)
                .offset(x = 300.dp, y = 40.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.06f))
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "$greeting,",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Normal
                )
                Text(
                    name.ifEmpty { "Pelajar" } + "! 👋",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        "$dayOfWeek, $date",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }
            }

            // Avatar
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
                    .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape)
            ) {
                Text(
                    text = name.firstOrNull()?.uppercase() ?: "T",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }

        // Target Campus chip
        if (targetCampus.isNotEmpty() && targetCampus != "Kampus Impianmu") {
            Spacer(Modifier.height(56.dp))
            Row(
                modifier = Modifier
                    .padding(top = 68.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.15f))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.School, null, tint = Color.White, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    targetCampus,
                    fontSize = 12.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ─── UTBK Countdown ────────────────────────────────────────────
@Composable
private fun UtbkCountdownCard(
    countdownDays: Int,
    utbkDate: String,
    onSetDate: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFFF6B6B), Color(0xFFFF8E53))
                )
            )
            .clickable { onSetDate() }
            .padding(20.dp)
    ) {
        // Decorative
        Box(
            modifier = Modifier
                .size(80.dp)
                .offset(x = 280.dp, y = (-20).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f))
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Timer,
                        null,
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Countdown UTBK/Ujian",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(Modifier.height(6.dp))

                if (countdownDays >= 0) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            "$countdownDays",
                            fontSize = 42.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "hari lagi",
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    Text(
                        utbkDate,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                } else {
                    Text(
                        "Belum diatur",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        "Tap untuk set tanggal UTBK",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            // Edit icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Ubah tanggal",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ─── Stat Mini Card ────────────────────────────────────────────
@Composable
private fun StatMiniCard(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
        Text(label, fontSize = 10.sp, color = TextMuted, textAlign = TextAlign.Center)
    }
}

// ─── Today Schedule ────────────────────────────────────────────
@Composable
private fun TodayScheduleSection(
    dayName: String,
    scheduleItems: List<ScheduleItem>,
    onViewAll: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CalendarToday,
                    null,
                    tint = Color(0xFF667EEA),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Jadwal $dayName",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            TextButton(onClick = onViewAll) {
                Text("Lihat semua", fontSize = 12.sp, color = Color(0xFF667EEA))
            }
        }

        Spacer(Modifier.height(8.dp))

        if (scheduleItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF8F9FA))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.EventAvailable,
                        null,
                        tint = TextMuted,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Tidak ada jadwal hari ini",
                        fontSize = 13.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            scheduleItems.forEach { item ->
                ScheduleItemRow(item = item)
                if (item != scheduleItems.last()) {
                    Divider(
                        color = Color(0xFFF0F0F0),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ScheduleItemRow(item: ScheduleItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Color dot
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(Color(item.color))
        )
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                item.subject,
                fontSize = 11.sp,
                color = TextSecondary,
                maxLines = 1
            )
        }
        Text(
            "${item.startTime}–${item.endTime}",
            fontSize = 11.sp,
            color = TextMuted,
            fontWeight = FontWeight.Medium
        )
    }
}

// ─── Quick Actions ─────────────────────────────────────────────
@Composable
private fun QuickActionsSection(onNavigateTo: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(16.dp)
    ) {
        Text(
            "⚡ Aksi Cepat",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickActionButton(
                icon = Icons.Default.MenuBook,
                label = "Nilai",
                color = Color(0xFF667EEA),
                modifier = Modifier.weight(1f),
                onClick = { onNavigateTo("academic") }
            )
            QuickActionButton(
                icon = Icons.Default.CalendarMonth,
                label = "Jadwal",
                color = Color(0xFFFF6B6B),
                modifier = Modifier.weight(1f),
                onClick = { onNavigateTo("schedule") }
            )
            QuickActionButton(
                icon = Icons.Default.AutoAwesome,
                label = "AI Tutor",
                color = Color(0xFF00C896),
                modifier = Modifier.weight(1f),
                onClick = { onNavigateTo("ai_tutor") }
            )
            QuickActionButton(
                icon = Icons.Default.Person,
                label = "Profil",
                color = Color(0xFFFFB800),
                modifier = Modifier.weight(1f),
                onClick = { onNavigateTo("profile") }
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(color.copy(alpha = 0.08f))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text(
            label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = color,
            textAlign = TextAlign.Center
        )
    }
}

// ─── Quote Card ────────────────────────────────────────────────
@Composable
private fun QuoteCard(quote: String, author: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF667EEA).copy(alpha = 0.08f), Color(0xFF764BA2).copy(alpha = 0.08f))
                )
            )
            .border(1.dp, Color(0xFF667EEA).copy(alpha = 0.15f), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row {
            Text(
                "❝",
                fontSize = 32.sp,
                color = Color(0xFF667EEA).copy(alpha = 0.4f),
                modifier = Modifier.offset(y = (-4).dp)
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    quote,
                    fontSize = 13.sp,
                    color = TextPrimary,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "— $author",
                    fontSize = 11.sp,
                    color = Color(0xFF667EEA),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ─── Quick Note ────────────────────────────────────────────────
@Composable
private fun QuickNoteWidget(
    note: String,
    onNoteChange: (String) -> Unit,
    onSave: () -> Unit,
    isSaving: Boolean,
    saveSuccess: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Edit,
                null,
                tint = Color(0xFF667EEA),
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "Catatan Harian",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = TextPrimary
            )
        }

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = note,
            onValueChange = onNoteChange,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 90.dp),
            placeholder = {
                Text(
                    "Tulis catatan harianmu di sini...",
                    color = TextMuted,
                    fontSize = 13.sp
                )
            },
            maxLines = 6,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF667EEA),
                unfocusedBorderColor = Color(0xFFE2E8F0),
                focusedContainerColor = Color(0xFFF8F9FF),
                unfocusedContainerColor = Color(0xFFFAFAFC)
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedVisibility(visible = saveSuccess) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CheckCircle,
                        null,
                        tint = Color(0xFF00C896),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Tersimpan!", fontSize = 12.sp, color = Color(0xFF00C896))
                }
            }
            if (!saveSuccess) {
                Text("${note.length} karakter", fontSize = 11.sp, color = TextMuted)
            }
            Button(
                onClick = onSave,
                enabled = note.isNotBlank() && !isSaving,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF667EEA)),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                } else {
                    Icon(Icons.Default.Save, null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Simpan", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
