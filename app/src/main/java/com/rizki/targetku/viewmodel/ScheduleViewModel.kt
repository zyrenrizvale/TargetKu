package com.rizki.targetku.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rizki.targetku.data.PreferencesManager
import com.rizki.targetku.data.models.ScheduleItem
import com.rizki.targetku.data.models.StudySession
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

data class ScheduleState(
    val scheduleItems: List<ScheduleItem> = emptyList(),
    val selectedDayIndex: Int = 0,
    val isSessionActive: Boolean = false,
    val sessionStartTime: Long = 0L,
    val sessionElapsedSeconds: Int = 0,
    val showSkipDialog: Boolean = false,
    val showStrictAlarmDialog: Boolean = false,
    val skipReason: String = "",
    val skipPhotoPath: String = "",
    val aiWarningText: String = "",
    val isAddingSchedule: Boolean = false,
    val newScheduleTitle: String = "",
    val newScheduleSubject: String = "",
    val newScheduleStart: String = "07:00",
    val newScheduleEnd: String = "08:00",
    val newScheduleDay: Int = 1
)

class ScheduleViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsManager = PreferencesManager(application)
    private val _state = MutableStateFlow(ScheduleState())
    val state: StateFlow<ScheduleState> = _state.asStateFlow()

    private var timerJob: kotlinx.coroutines.Job? = null

    init {
        loadSchedule()
        // Set current day
        val cal = java.util.Calendar.getInstance()
        val dayOfWeek = when (cal.get(java.util.Calendar.DAY_OF_WEEK)) {
            java.util.Calendar.MONDAY -> 0
            java.util.Calendar.TUESDAY -> 1
            java.util.Calendar.WEDNESDAY -> 2
            java.util.Calendar.THURSDAY -> 3
            java.util.Calendar.FRIDAY -> 4
            java.util.Calendar.SATURDAY -> 5
            java.util.Calendar.SUNDAY -> 6
            else -> 0
        }
        _state.value = _state.value.copy(selectedDayIndex = dayOfWeek)
    }

    private fun loadSchedule() {
        _state.value = _state.value.copy(
            scheduleItems = prefsManager.getScheduleItems()
        )
    }

    fun selectDay(index: Int) {
        _state.value = _state.value.copy(selectedDayIndex = index)
    }

    fun getScheduleForDay(dayIndex: Int): List<ScheduleItem> {
        val dayOfWeek = dayIndex + 1 // 1 = Monday
        return _state.value.scheduleItems
            .filter { it.dayOfWeek == dayOfWeek }
            .sortedBy { it.startTime }
    }

    // ============ Study Session ============
    fun startSession() {
        _state.value = _state.value.copy(
            isSessionActive = true,
            sessionStartTime = System.currentTimeMillis(),
            sessionElapsedSeconds = 0
        )
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(1000)
                val elapsed = ((System.currentTimeMillis() - _state.value.sessionStartTime) / 1000).toInt()
                _state.value = _state.value.copy(sessionElapsedSeconds = elapsed)
            }
        }
    }

    fun requestSkipSession() {
        _state.value = _state.value.copy(showStrictAlarmDialog = true)
    }

    fun onSkipReasonChange(reason: String) {
        _state.value = _state.value.copy(skipReason = reason)
    }

    fun onSkipPhotoCaptured(photoPath: String) {
        _state.value = _state.value.copy(skipPhotoPath = photoPath)
    }

    fun confirmSkip() {
        val warnings = listOf(
            "AI mendeteksi kamu mencoba bolos! Bukti foto disimpan. Jangan ulangi ya!",
            "Sistem TargetKu mencatat: kamu melewatkan sesi belajar hari ini. Besok harus lebih semangat!",
            "Peringatan AI: Konsistensi adalah kunci sukses. Melewatkan 1 sesi = mundur 3 langkah!",
            "AI mendeteksi adanya kemalasan level tinggi! Foto buktimu telah disimpan di sistem.",
            "TargetKu AI: Kamu boleh istirahat, tapi ingat - kampus impianmu tidak pernah istirahat menunggu mahasiswa baru!",
        )

        timerJob?.cancel()
        _state.value = _state.value.copy(
            isSessionActive = false,
            showStrictAlarmDialog = false,
            aiWarningText = warnings.random(),
            sessionElapsedSeconds = 0
        )

        viewModelScope.launch {
            kotlinx.coroutines.delay(8000)
            _state.value = _state.value.copy(aiWarningText = "")
        }
    }

    fun endSessionNormally() {
        timerJob?.cancel()
        _state.value = _state.value.copy(
            isSessionActive = false,
            sessionElapsedSeconds = 0,
            aiWarningText = ""
        )
    }

    fun dismissStrictAlarmDialog() {
        _state.value = _state.value.copy(showStrictAlarmDialog = false)
    }

    // ============ Add Schedule ============
    fun showAddSchedule() { _state.value = _state.value.copy(isAddingSchedule = true) }
    fun hideAddSchedule() {
        _state.value = _state.value.copy(
            isAddingSchedule = false,
            newScheduleTitle = "", newScheduleSubject = "",
            newScheduleStart = "07:00", newScheduleEnd = "08:00"
        )
    }

    fun onNewScheduleTitleChange(v: String) = run { _state.value = _state.value.copy(newScheduleTitle = v) }
    fun onNewScheduleSubjectChange(v: String) = run { _state.value = _state.value.copy(newScheduleSubject = v) }
    fun onNewScheduleStartChange(v: String) = run { _state.value = _state.value.copy(newScheduleStart = v) }
    fun onNewScheduleEndChange(v: String) = run { _state.value = _state.value.copy(newScheduleEnd = v) }
    fun onNewScheduleDayChange(v: Int) = run { _state.value = _state.value.copy(newScheduleDay = v) }

    fun addScheduleItem() {
        val s = _state.value
        if (s.newScheduleTitle.isBlank()) return
        val colors = listOf(0xFFB3D9F7, 0xFFF7B3D9, 0xFFD9B3F7, 0xFFB3F7D9, 0xFFF7D9B3)
        val item = ScheduleItem(
            id = UUID.randomUUID().toString(),
            title = s.newScheduleTitle,
            subject = s.newScheduleSubject.ifEmpty { s.newScheduleTitle },
            startTime = s.newScheduleStart,
            endTime = s.newScheduleEnd,
            dayOfWeek = s.newScheduleDay,
            color = colors.random()
        )
        val updated = _state.value.scheduleItems + item
        _state.value = _state.value.copy(scheduleItems = updated)
        prefsManager.saveScheduleItems(updated)
        hideAddSchedule()
    }

    fun deleteScheduleItem(id: String) {
        val updated = _state.value.scheduleItems.filter { it.id != id }
        _state.value = _state.value.copy(scheduleItems = updated)
        prefsManager.saveScheduleItems(updated)
    }

    fun formatElapsed(seconds: Int): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return if (h > 0) "%d:%02d:%02d".format(h, m, s)
        else "%02d:%02d".format(m, s)
    }

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }

    val dayNames = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")
    val dayFullNames = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu")
}
