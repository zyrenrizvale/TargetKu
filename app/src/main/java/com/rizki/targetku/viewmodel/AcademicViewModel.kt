package com.rizki.targetku.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rizki.targetku.data.PreferencesManager
import com.rizki.targetku.data.models.GradeEntry
import com.rizki.targetku.data.models.Task
import com.rizki.targetku.data.models.TaskPriority
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

data class AcademicState(
    val gradeEntries: List<GradeEntry> = emptyList(),
    val tasks: List<Task> = emptyList(),
    val selectedSemester: String = "Semua Semester",
    val isAddingGrade: Boolean = false,
    val isAddingTask: Boolean = false,
    val isExporting: Boolean = false,
    val exportSuccess: String = "",
    val newGradeSemester: String = "Kelas 10 Semester 1",
    val newGradeSubject: String = "",
    val newGradeTarget: String = "",
    val newGradeActual: String = "",
    val newTaskTitle: String = "",
    val newTaskDescription: String = "",
    val newTaskPriority: TaskPriority = TaskPriority.MEDIUM
)

class AcademicViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsManager = PreferencesManager(application)
    private val _state = MutableStateFlow(AcademicState())
    val state: StateFlow<AcademicState> = _state.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        _state.value = _state.value.copy(
            gradeEntries = prefsManager.getGradeEntries(),
            tasks = prefsManager.getTasks()
        )
    }

    // ============ Grade Operations ============
    fun showAddGrade() { _state.value = _state.value.copy(isAddingGrade = true) }
    fun hideAddGrade() {
        _state.value = _state.value.copy(
            isAddingGrade = false,
            newGradeSubject = "", newGradeTarget = "", newGradeActual = ""
        )
    }

    fun onNewGradeSemesterChange(v: String) = run { _state.value = _state.value.copy(newGradeSemester = v) }
    fun onNewGradeSubjectChange(v: String) = run { _state.value = _state.value.copy(newGradeSubject = v) }
    fun onNewGradeTargetChange(v: String) = run { _state.value = _state.value.copy(newGradeTarget = v) }
    fun onNewGradeActualChange(v: String) = run { _state.value = _state.value.copy(newGradeActual = v) }

    fun addGradeEntry() {
        val s = _state.value
        if (s.newGradeSubject.isBlank()) return
        val entry = GradeEntry(
            id = UUID.randomUUID().toString(),
            semester = s.newGradeSemester,
            subject = s.newGradeSubject,
            targetGrade = s.newGradeTarget.toDoubleOrNull() ?: 0.0,
            actualGrade = s.newGradeActual.toDoubleOrNull() ?: 0.0
        )
        val updated = _state.value.gradeEntries + entry
        _state.value = _state.value.copy(gradeEntries = updated)
        prefsManager.saveGradeEntries(updated)
        hideAddGrade()
    }

    fun deleteGradeEntry(id: String) {
        val updated = _state.value.gradeEntries.filter { it.id != id }
        _state.value = _state.value.copy(gradeEntries = updated)
        prefsManager.saveGradeEntries(updated)
    }

    fun updateGradeActual(id: String, actual: String) {
        val updated = _state.value.gradeEntries.map {
            if (it.id == id) it.copy(actualGrade = actual.toDoubleOrNull() ?: it.actualGrade)
            else it
        }
        _state.value = _state.value.copy(gradeEntries = updated)
        prefsManager.saveGradeEntries(updated)
    }

    val semesters = listOf(
        "Kelas 10 Semester 1", "Kelas 10 Semester 2",
        "Kelas 11 Semester 1", "Kelas 11 Semester 2",
        "Kelas 12 Semester 1", "Kelas 12 Semester 2"
    )

    // ============ Task Operations ============
    fun showAddTask() { _state.value = _state.value.copy(isAddingTask = true) }
    fun hideAddTask() {
        _state.value = _state.value.copy(
            isAddingTask = false, newTaskTitle = "", newTaskDescription = ""
        )
    }

    fun onNewTaskTitleChange(v: String) = run { _state.value = _state.value.copy(newTaskTitle = v) }
    fun onNewTaskDescChange(v: String) = run { _state.value = _state.value.copy(newTaskDescription = v) }
    fun onNewTaskPriorityChange(v: TaskPriority) = run { _state.value = _state.value.copy(newTaskPriority = v) }

    fun addTask() {
        val s = _state.value
        if (s.newTaskTitle.isBlank()) return
        val task = Task(
            id = UUID.randomUUID().toString(),
            title = s.newTaskTitle,
            description = s.newTaskDescription,
            priority = s.newTaskPriority
        )
        val updated = _state.value.tasks + task
        _state.value = _state.value.copy(tasks = updated)
        prefsManager.saveTasks(updated)
        hideAddTask()
    }

    fun toggleTask(id: String) {
        val updated = _state.value.tasks.map {
            if (it.id == id) it.copy(isDone = !it.isDone) else it
        }
        _state.value = _state.value.copy(tasks = updated)
        prefsManager.saveTasks(updated)
    }

    fun deleteTask(id: String) {
        val updated = _state.value.tasks.filter { it.id != id }
        _state.value = _state.value.copy(tasks = updated)
        prefsManager.saveTasks(updated)
    }

    // ============ Export ============
    fun exportToPdf(context: Context) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isExporting = true)
            try {
                val pdfDocument = PdfDocument()
                val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas

                val titlePaint = Paint().apply {
                    textSize = 24f
                    isFakeBoldText = true
                }
                val headerPaint = Paint().apply {
                    textSize = 16f
                    isFakeBoldText = true
                }
                val bodyPaint = Paint().apply { textSize = 12f }
                val smallPaint = Paint().apply { textSize = 10f }

                var y = 60f
                canvas.drawText("TargetKu - Laporan Akademik", 50f, y, titlePaint)
                y += 20f
                canvas.drawText("Diekspor oleh: ${prefsManager.userName}", 50f, y, smallPaint)
                y += 30f

                // Grades table
                canvas.drawText("NILAI PER SEMESTER", 50f, y, headerPaint)
                y += 20f
                canvas.drawText("Semester | Mata Pelajaran | Target | Aktual | Status", 50f, y, smallPaint)
                y += 15f

                for (entry in _state.value.gradeEntries) {
                    if (y > 780f) break
                    val line = "${entry.semester} | ${entry.subject} | ${entry.targetGrade} | ${entry.actualGrade} | ${if (entry.isOnTarget) "OK" else "Perlu Ditingkatkan"}"
                    canvas.drawText(line, 50f, y, bodyPaint)
                    y += 15f
                }

                y += 20f
                canvas.drawText("DAFTAR TUGAS", 50f, y, headerPaint)
                y += 20f
                for (task in _state.value.tasks) {
                    if (y > 780f) break
                    val status = if (task.isDone) "[V]" else "[ ]"
                    canvas.drawText("$status ${task.title} [${task.priority.label}]", 50f, y, bodyPaint)
                    y += 15f
                }

                pdfDocument.finishPage(page)

                val file = File(context.cacheDir, "TargetKu_Laporan_${System.currentTimeMillis()}.pdf")
                pdfDocument.writeTo(FileOutputStream(file))
                pdfDocument.close()

                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Share PDF"))

                _state.value = _state.value.copy(isExporting = false, exportSuccess = "PDF berhasil dibuat!")
                kotlinx.coroutines.delay(3000)
                _state.value = _state.value.copy(exportSuccess = "")

            } catch (e: Exception) {
                _state.value = _state.value.copy(isExporting = false, exportSuccess = "Gagal membuat PDF: ${e.message}")
            }
        }
    }

    fun exportToExcel(context: Context) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isExporting = true)
            try {
                val sb = StringBuilder()
                sb.appendLine("TargetKu - Laporan Akademik")
                sb.appendLine("Dibuat oleh: ${prefsManager.userName}")
                sb.appendLine()
                sb.appendLine("=== NILAI PER SEMESTER ===")
                sb.appendLine("Semester,Mata Pelajaran,Target Nilai,Nilai Aktual,Delta,Status")

                for (entry in _state.value.gradeEntries) {
                    sb.appendLine("${entry.semester},${entry.subject},${entry.targetGrade},${entry.actualGrade},${entry.delta},${if (entry.isOnTarget) "Tercapai" else "Belum Tercapai"}")
                }

                sb.appendLine()
                sb.appendLine("=== DAFTAR TUGAS ===")
                sb.appendLine("Judul,Deskripsi,Prioritas,Status")

                for (task in _state.value.tasks) {
                    sb.appendLine("${task.title},${task.description},${task.priority.label},${if (task.isDone) "Selesai" else "Belum Selesai"}")
                }

                val file = File(context.cacheDir, "TargetKu_Laporan_${System.currentTimeMillis()}.csv")
                file.writeText(sb.toString())

                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Share ke Excel"))

                _state.value = _state.value.copy(isExporting = false, exportSuccess = "File CSV berhasil dibuat! Buka dengan Microsoft Excel.")
                kotlinx.coroutines.delay(3000)
                _state.value = _state.value.copy(exportSuccess = "")

            } catch (e: Exception) {
                _state.value = _state.value.copy(isExporting = false, exportSuccess = "Gagal export: ${e.message}")
            }
        }
    }
}
