package com.rizki.targetku.ui.screens

@file:OptIn(ExperimentalMaterial3Api::class)

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rizki.targetku.data.models.GradeEntry
import com.rizki.targetku.data.models.Task
import com.rizki.targetku.data.models.TaskPriority
import com.rizki.targetku.ui.components.GlassCard
import com.rizki.targetku.ui.components.GradientCard
import com.rizki.targetku.viewmodel.AcademicViewModel
import com.rizki.targetku.ui.theme.*
import java.text.DecimalFormat

@Composable
fun AcademicScreen(
    viewModel: AcademicViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize().background(
        brush = Brush.verticalGradient(colors = listOf(SoftPinkSurface, OffWhite))
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
                                colors = listOf(SoftPinkDark, LavenderDark)
                            )
                        )
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    Column {
                        Text("Tracker Akademik", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = White)
                        Text("Pantau nilai dan tugasmu", fontSize = 13.sp, color = White.copy(alpha = 0.8f))
                    }
                }
            }

            // Export buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.exportToPdf(context) },
                        modifier = Modifier.weight(1f).height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRose),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !state.isExporting
                    ) {
                        Icon(Icons.Default.PictureAsPdf, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Export PDF", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { viewModel.exportToExcel(context) },
                        modifier = Modifier.weight(1f).height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen.copy(alpha = 0.9f)),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !state.isExporting
                    ) {
                        Icon(Icons.Default.TableChart, null, Modifier.size(16.dp), tint = TextPrimary)
                        Spacer(Modifier.width(4.dp))
                        Text("Export Excel", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                }
            }

            // Export status
            if (state.exportSuccess.isNotEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SuccessGreen.copy(alpha = 0.2f))
                            .padding(12.dp)
                    ) {
                        Text(state.exportSuccess, fontSize = 13.sp, color = Color(0xFF16A34A), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    }
                }
            }

            // Grade Tracker Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Nilai Per Semester", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                    IconButton(onClick = viewModel::showAddGrade) {
                        Icon(Icons.Default.Add, null, tint = SoftPinkDark)
                    }
                }
            }

            // Grade entries grouped by semester
            val groupedGrades = state.gradeEntries.groupBy { it.semester }
            groupedGrades.forEach { (semester, entries) ->
                item {
                    Text(
                        semester,
                        fontSize = 13.sp, fontWeight = FontWeight.Bold,
                        color = SoftPinkDark,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
                items(entries) { entry ->
                    GradeEntryRow(
                        entry = entry,
                        onDelete = { viewModel.deleteGradeEntry(entry.id) }
                    )
                }
            }

            if (state.gradeEntries.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Default.School,
                        title = "Belum ada data nilai",
                        subtitle = "Tap '+' untuk menambahkan nilai"
                    )
                }
            }

            // Add Grade Dialog
            if (state.isAddingGrade) {
                item {
                    AddGradeSheet(
                        viewModel = viewModel,
                        onDismiss = viewModel::hideAddGrade
                    )
                }
            }

            // Task Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Daftar Tugas", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                    Row {
                        val done = state.tasks.count { it.isDone }
                        Text("$done/${state.tasks.size}", fontSize = 13.sp, color = TextSecondary)
                        Spacer(Modifier.width(8.dp))
                        IconButton(onClick = viewModel::showAddTask) {
                            Icon(Icons.Default.Add, null, tint = BabyBlueDark)
                        }
                    }
                }
            }

            items(state.tasks) { task ->
                TaskItem(
                    task = task,
                    onToggle = { viewModel.toggleTask(task.id) },
                    onDelete = { viewModel.deleteTask(task.id) }
                )
            }

            if (state.tasks.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Default.CheckCircle,
                        title = "Belum ada tugas",
                        subtitle = "Tap '+' untuk menambahkan tugas"
                    )
                }
            }

            if (state.isAddingTask) {
                item {
                    AddTaskSheet(
                        viewModel = viewModel,
                        onDismiss = viewModel::hideAddTask
                    )
                }
            }
        }
    }
}

@Composable
private fun GradeEntryRow(entry: GradeEntry, onDelete: () -> Unit) {
    val deltaColor = when {
        entry.actualGrade == 0.0 -> TextMuted
        entry.isOnTarget -> Color(0xFF16A34A)
        else -> Color(0xFFDC2626)
    }
    val df = DecimalFormat("#.#")

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        backgroundColor = White.copy(alpha = 0.85f),
        shadowElevation = 3.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Subject indicator
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SoftPink.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    entry.subject.take(2).uppercase(),
                    fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = SoftPinkDark
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(entry.subject, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                Text(entry.semester, fontSize = 11.sp, color = TextSecondary)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Target", fontSize = 10.sp, color = TextMuted)
                Text(df.format(entry.targetGrade), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BabyBlueDark)
            }
            Spacer(Modifier.width(10.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Aktual", fontSize = 10.sp, color = TextMuted)
                Text(
                    if (entry.actualGrade > 0) df.format(entry.actualGrade) else "-",
                    fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary
                )
            }
            Spacer(Modifier.width(8.dp))
            if (entry.actualGrade > 0) {
                val deltaText = if (entry.delta > 0) "+${df.format(entry.delta)}" else df.format(entry.delta)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(deltaColor.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(deltaText, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = deltaColor)
                }
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, null, tint = ErrorRose, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun TaskItem(task: Task, onToggle: () -> Unit, onDelete: () -> Unit) {
    val priorityColor = Color(task.priority.color)

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onToggle),
        backgroundColor = if (task.isDone) SuccessGreen.copy(alpha = 0.1f) else White.copy(alpha = 0.85f),
        shadowElevation = 3.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isDone,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = SuccessGreen,
                    checkmarkColor = White,
                    uncheckedColor = BabyBlue
                )
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    task.title,
                    fontWeight = FontWeight.Bold, fontSize = 14.sp,
                    color = if (task.isDone) TextMuted else TextPrimary,
                    textDecoration = if (task.isDone) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                )
                if (task.description.isNotEmpty()) {
                    Text(task.description, fontSize = 12.sp, color = TextMuted)
                }
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(priorityColor.copy(alpha = 0.25f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(task.priority.label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = priorityColor.copy(red = priorityColor.red * 0.6f, green = priorityColor.green * 0.6f, blue = priorityColor.blue * 0.6f))
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, null, tint = ErrorRose, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun AddGradeSheet(viewModel: AcademicViewModel, onDismiss: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    GlassCard(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        backgroundColor = White,
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Tambah Nilai", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = SoftPinkDark)
            Spacer(Modifier.height(12.dp))

            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = state.newGradeSemester,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Semester") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    viewModel.semesters.forEach { semester ->
                        DropdownMenuItem(
                            text = { Text(semester) },
                            onClick = { viewModel.onNewGradeSemesterChange(semester); expanded = false }
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.newGradeSubject,
                onValueChange = viewModel::onNewGradeSubjectChange,
                label = { Text("Mata Pelajaran") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.newGradeTarget,
                    onValueChange = viewModel::onNewGradeTargetChange,
                    label = { Text("Target") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = state.newGradeActual,
                    onValueChange = viewModel::onNewGradeActualChange,
                    label = { Text("Aktual") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Batal") }
                Button(
                    onClick = viewModel::addGradeEntry,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = SoftPinkDark)
                ) { Text("Simpan") }
            }
        }
    }
}

@Composable
private fun AddTaskSheet(viewModel: AcademicViewModel, onDismiss: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    GlassCard(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        backgroundColor = White,
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Tambah Tugas", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = BabyBlueDark)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = state.newTaskTitle,
                onValueChange = viewModel::onNewTaskTitleChange,
                label = { Text("Judul Tugas") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.newTaskDescription,
                onValueChange = viewModel::onNewTaskDescChange,
                label = { Text("Deskripsi (opsional)") },
                modifier = Modifier.fillMaxWidth().height(80.dp),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text("Prioritas:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TaskPriority.values().forEach { priority ->
                    FilterChip(
                        selected = state.newTaskPriority == priority,
                        onClick = { viewModel.onNewTaskPriorityChange(priority) },
                        label = { Text(priority.label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(priority.color).copy(alpha = 0.3f)
                        )
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Batal") }
                Button(
                    onClick = viewModel::addTask,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = BabyBlueDark)
                ) { Text("Simpan") }
            }
        }
    }
}

@Composable
private fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, null, tint = TextMuted, modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(8.dp))
        Text(title, fontWeight = FontWeight.Bold, color = TextSecondary)
        Text(subtitle, fontSize = 12.sp, color = TextMuted, textAlign = TextAlign.Center)
    }
}
