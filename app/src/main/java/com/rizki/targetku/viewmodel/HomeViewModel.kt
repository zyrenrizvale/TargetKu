package com.rizki.targetku.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rizki.targetku.data.PreferencesManager
import com.rizki.targetku.data.models.ScheduleItem
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class HomeState(
    val userName: String = "",
    val targetCampus: String = "",
    val targetMajor: String = "",
    val dailyNote: String = "",
    val averageGrade: Double = 0.0,
    val tasksDone: Int = 0,
    val tasksPending: Int = 0,
    val dailyQuote: String = "",
    val dailyQuoteAuthor: String = "",
    val isSavingNote: Boolean = false,
    val noteSavedSuccess: Boolean = false,
    // New features
    val todaySchedule: List<ScheduleItem> = emptyList(),
    val todayDayName: String = "",
    val utbkCountdownDays: Int = -1,
    val utbkDate: String = "",
    val studyStreak: Int = 0,
    val onTargetCount: Int = 0,
    val totalGradeCount: Int = 0,
    val showUtbkDialog: Boolean = false,
    val utbkDateInput: String = ""
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsManager = PreferencesManager(application)

    private val quotes = listOf(
        Pair("Pendidikan adalah senjata paling ampuh yang dapat kamu gunakan untuk mengubah dunia.", "Nelson Mandela"),
        Pair("Belajarlah seolah kamu akan hidup selamanya, hiduplah seolah kamu akan mati besok.", "Mahatma Gandhi"),
        Pair("Kesuksesan adalah hasil dari persiapan, kerja keras, dan belajar dari kegagalan.", "Colin Powell"),
        Pair("Jangan biarkan hari ini berlalu tanpa melakukan sesuatu yang lebih baik dari kemarin.", "Pepatah"),
        Pair("Setiap detik yang kamu habiskan untuk belajar hari ini adalah investasi untuk masa depanmu.", "TargetKu"),
        Pair("Mimpi besar membutuhkan usaha besar. Kamu sudah di jalur yang benar!", "TargetKu"),
        Pair("Ilmu itu seperti cahaya - semakin kamu berbagi, semakin terang jalanmu.", "TargetKu"),
        Pair("Mulai dari di mana kamu berada. Gunakan apa yang kamu punya. Lakukan apa yang kamu bisa.", "Arthur Ashe"),
        Pair("Kunci keberhasilan bukan pada seberapa pintar kamu, tapi seberapa keras kamu berusaha.", "Pepatah"),
        Pair("Setiap pagi membawa kesempatan baru untuk menjadi lebih baik dari kemarin.", "TargetKu"),
    )

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            val prefs = prefsManager.getUserPrefs()
            val grades = prefsManager.getGradeEntries()
            val tasks = prefsManager.getTasks()
            val scheduleItems = prefsManager.getScheduleItems()

            val avg = grades.filter { it.actualGrade > 0 }
                .let { filtered ->
                    if (filtered.isEmpty()) 0.0
                    else filtered.sumOf { it.actualGrade } / filtered.size
                }

            val quote = quotes.random()

            // Today's schedule
            val cal = Calendar.getInstance()
            val dayOfWeek = when (cal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> 1
                Calendar.TUESDAY -> 2
                Calendar.WEDNESDAY -> 3
                Calendar.THURSDAY -> 4
                Calendar.FRIDAY -> 5
                Calendar.SATURDAY -> 6
                Calendar.SUNDAY -> 7
                else -> 1
            }
            val dayNames = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu")
            val todayName = dayNames.getOrElse(dayOfWeek - 1) { "Hari Ini" }

            val todaySchedule = scheduleItems
                .filter { it.dayOfWeek == dayOfWeek }
                .sortedBy { it.startTime }
                .take(3)

            // UTBK countdown
            val utbkDate = prefsManager.utbkDate
            val countdownDays = if (utbkDate.isNotEmpty()) {
                try {
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val targetDate = sdf.parse(utbkDate)
                    val today = Date()
                    val diff = (targetDate!!.time - today.time)
                    (diff / (1000L * 60 * 60 * 24)).toInt().coerceAtLeast(0)
                } catch (e: Exception) { -1 }
            } else -1

            // Study streak
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val lastStudy = prefsManager.lastStudyDate
            val currentStreak = prefsManager.studyStreak
            val newStreak = when {
                lastStudy == todayStr -> currentStreak
                lastStudy == getYesterday() -> currentStreak + 1
                lastStudy.isEmpty() -> 1
                else -> 1
            }
            if (lastStudy != todayStr) {
                prefsManager.studyStreak = newStreak
                prefsManager.lastStudyDate = todayStr
            }

            _state.value = HomeState(
                userName = prefs.name,
                targetCampus = prefs.targetCampus.ifEmpty { "Kampus Impianmu" },
                targetMajor = prefs.targetMajor,
                dailyNote = prefsManager.dailyNote,
                averageGrade = avg,
                tasksDone = tasks.count { it.isDone },
                tasksPending = tasks.count { !it.isDone },
                dailyQuote = quote.first,
                dailyQuoteAuthor = quote.second,
                todaySchedule = todaySchedule,
                todayDayName = todayName,
                utbkCountdownDays = countdownDays,
                utbkDate = utbkDate,
                studyStreak = newStreak,
                onTargetCount = grades.count { it.isOnTarget },
                totalGradeCount = grades.size
            )
        }
    }

    private fun getYesterday(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
    }

    fun onNoteChange(value: String) {
        _state.value = _state.value.copy(dailyNote = value, noteSavedSuccess = false)
    }

    fun saveNote() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSavingNote = true)
            prefsManager.dailyNote = _state.value.dailyNote
            kotlinx.coroutines.delay(500)
            _state.value = _state.value.copy(isSavingNote = false, noteSavedSuccess = true)
            kotlinx.coroutines.delay(2000)
            _state.value = _state.value.copy(noteSavedSuccess = false)
        }
    }

    // UTBK Dialog
    fun showUtbkDialog() {
        _state.value = _state.value.copy(
            showUtbkDialog = true,
            utbkDateInput = _state.value.utbkDate
        )
    }

    fun hideUtbkDialog() {
        _state.value = _state.value.copy(showUtbkDialog = false)
    }

    fun onUtbkDateInputChange(value: String) {
        _state.value = _state.value.copy(utbkDateInput = value)
    }

    fun saveUtbkDate() {
        prefsManager.utbkDate = _state.value.utbkDateInput
        _state.value = _state.value.copy(showUtbkDialog = false)
        loadData()
    }
}
