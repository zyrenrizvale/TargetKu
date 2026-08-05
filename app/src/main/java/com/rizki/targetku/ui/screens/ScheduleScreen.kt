@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.rizki.targetku.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rizki.targetku.data.models.ScheduleItem
import com.rizki.targetku.ui.components.StrictAlarmDialog
import com.rizki.targetku.viewmodel.ScheduleViewModel
import com.rizki.targetku.ui.theme.*

@Composable
fun ScheduleScreen(
    viewModel: ScheduleViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize().background(
        brush = Brush.verticalGradient(colors = listOf(LavenderLight, OffWhite))
    )) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Header
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(LavenderDark, BabyBlueDark)
                            )
                        )
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    Column {
                        Text("Jadwal Belajar", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = White)
                        Text("Atur waktu belajarmu dengan disiplin", fontSize = 13.sp, color = White.copy(alpha = 0.8f))
                    }
                }
            }

            // Study Session Widget
            item {
                StudySessionWidget(
                    isActive = state.isSessionActive,
                    elapsed = state.sessionElapsedSeconds,
                    formatElapsed = viewModel::formatElapsed,
                    onStart = viewModel::startSession,
                    onSkip = viewModel::requestSkipSession,
                    onEnd = viewModel::endSessionNormally
                )
            }

            // AI Warning Banner (when session skipped)
            if (state.aiWarningText.isNotEmpty()) {
                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = slideInVertically() + fadeIn()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(ErrorRose.copy(alpha = 0.3f), SoftPink.copy(alpha = 0.3f))
                                    )
                                )
                                .border(1.dp, ErrorRose, RoundedCornerShape(12.dp))
                                .padding(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(Icons.Default.SmartToy, null, tint = ErrorRose, modifier = Modifier.size(24.dp))
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text("AI TargetKu", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = Color(0xFF991B1B))
                                    Text(state.aiWarningText, fontSize = 13.sp, color = Color(0xFF7F1D1D), lineHeight = 19.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Day selector
            item {
                DaySelector(
                    days = viewModel.dayNames,
                    selectedIndex = state.selectedDayIndex,
                    onSelect = viewModel::selectDay
                )
            }

            // Schedule timeline
            val todaySchedule = viewModel.getScheduleForDay(state.selectedDayIndex)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${viewModel.dayFullNames[state.selectedDayIndex]} - ${todaySchedule.size} kegiatan",
                        fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary
                    )
                    IconButton(onClick = viewModel::showAddSchedule) {
                        Icon(Icons.Default.Add, null, tint = LavenderDark)
                    }
                }
            }

            if (todaySchedule.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Event, null, tint = TextMuted, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("Tidak ada jadwal", fontWeight = FontWeight.Bold, color = TextSecondary)
                        Text("Tap '+' untuk menambahkan kegiatan", fontSize = 12.sp, color = TextMuted)
                    }
                }
            } else {
                items(todaySchedule.size) { index ->
                    TimelineItem(
                        item = todaySchedule[index],
                        isLast = index == todaySchedule.size - 1,
                        onDelete = { viewModel.deleteScheduleItem(todaySchedule[index].id) }
                    )
                }
            }

            // Add Schedule Form
            if (state.isAddingSchedule) {
                item {
                    AddScheduleForm(viewModel = viewModel, onDismiss = viewModel::hideAddSchedule)
                }
            }
        }

        // Strict Alarm Dialog
        if (state.showStrictAlarmDialog) {
            StrictAlarmDialog(
                skipReason = state.skipReason,
                skipPhotoPath = state.skipPhotoPath,
                onReasonChange = viewModel::onSkipReasonChange,
                onPhotoCaptured = viewModel::onSkipPhotoCaptured,
                onConfirmSkip = viewModel::confirmSkip,
                onDismiss = viewModel::dismissStrictAlarmDialog
            )
        }
    }
}

@Composable
private fun StudySessionWidget(
    isActive: Boolean,
    elapsed: Int,
    formatElapsed: (Int) -> String,
    onStart: () -> Unit,
    onSkip: () -> Unit,
    onEnd: () -> Unit
) {
    val pulseAnim = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulseAnim.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = if (isActive)
                        listOf(Color(0xFF1E3A5F), Color(0xFF2D5016))
                    else
                        listOf(LavenderLight, BabyBlueLight)
                )
            )
            .border(
                1.dp,
                if (isActive) Color(0xFF60A5FA) else Lavender,
                RoundedCornerShape(20.dp)
            )
            .padding(20.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            if (isActive) {
                Icon(Icons.Default.Timer, null, tint = Color(0xFF60A5FA), modifier = Modifier.size(32.dp))
                Spacer(Modifier.height(8.dp))
                Text(
                    "SESI BELAJAR AKTIF",
                    fontSize = 12.sp, fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF93C5FD), letterSpacing = 2.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    formatElapsed(elapsed),
                    fontSize = 42.sp, fontWeight = FontWeight.ExtraBold,
                    color = White
                )
                Spacer(Modifier.height(8.dp))
                Text("Fokus dan tetap semangat!", fontSize = 13.sp, color = Color(0xFF86EFAC))
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = onSkip,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRose),
                        border = BorderStroke(1.dp, ErrorRose)
                    ) {
                        Icon(Icons.Default.SkipNext, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Lewati", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onEnd,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                    ) {
                        Icon(Icons.Default.Done, null, Modifier.size(16.dp), tint = TextPrimary)
                        Spacer(Modifier.width(4.dp))
                        Text("Selesai", fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                }
            } else {
                Icon(Icons.Default.PlayCircle, null, tint = LavenderDark, modifier = Modifier.size(40.dp))
                Spacer(Modifier.height(8.dp))
                Text("Mulai Sesi Belajar", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                Text("Aktifkan timer dan tetap fokus!", fontSize = 13.sp, color = TextSecondary)
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onStart,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LavenderDark),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Mulai Sekarang!", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun DaySelector(days: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        days.forEachIndexed { index, day ->
            val isSelected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isSelected) LavenderDark
                        else Lavender.copy(alpha = 0.2f)
                    )
                    .clickable { onSelect(index) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    day,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                    color = if (isSelected) White else LavenderDark
                )
            }
        }
    }
}

@Composable
private fun TimelineItem(item: ScheduleItem, isLast: Boolean, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        // Time column
        Column(
            modifier = Modifier.width(56.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(item.startTime, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = LavenderDark)
            Text(item.endTime, fontSize = 10.sp, color = TextMuted)
        }

        Spacer(Modifier.width(12.dp))

        // Timeline line
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(Color(item.color))
                    .border(2.dp, LavenderDark, CircleShape)
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(60.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(LavenderDark, Lavender.copy(alpha = 0.3f))
                            )
                        )
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        // Content card
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(item.color).copy(alpha = 0.15f))
                .border(1.dp, Color(item.color).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                    Text(item.subject, fontSize = 12.sp, color = TextSecondary)
                    Text("${item.startTime} - ${item.endTime}", fontSize = 11.sp, color = TextMuted)
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Delete, null, tint = ErrorRose, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

@Composable
private fun AddScheduleForm(viewModel: ScheduleViewModel, onDismiss: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(White)
            .border(1.dp, Lavender, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Text("Tambah Jadwal", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = LavenderDark)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = state.newScheduleTitle,
                onValueChange = viewModel::onNewScheduleTitleChange,
                label = { Text("Nama Kegiatan") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp), singleLine = true
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.newScheduleSubject,
                onValueChange = viewModel::onNewScheduleSubjectChange,
                label = { Text("Mata Pelajaran") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp), singleLine = true
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.newScheduleStart,
                    onValueChange = viewModel::onNewScheduleStartChange,
                    label = { Text("Mulai") },
                    placeholder = { Text("07:00") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp), singleLine = true
                )
                OutlinedTextField(
                    value = state.newScheduleEnd,
                    onValueChange = viewModel::onNewScheduleEndChange,
                    label = { Text("Selesai") },
                    placeholder = { Text("08:00") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp), singleLine = true
                )
            }
            Spacer(Modifier.height(8.dp))
            Text("Hari:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                viewModel.dayNames.forEachIndexed { index, day ->
                    val dayNum = index + 1
                    FilterChip(
                        selected = state.newScheduleDay == dayNum,
                        onClick = { viewModel.onNewScheduleDayChange(dayNum) },
                        label = { Text(day, fontSize = 12.sp) }
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Batal") }
                Button(
                    onClick = viewModel::addScheduleItem,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = LavenderDark)
                ) { Text("Simpan") }
            }
        }
    }
}
