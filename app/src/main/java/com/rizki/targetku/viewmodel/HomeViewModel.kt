package com.rizki.targetku.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rizki.targetku.data.PreferencesManager
import com.rizki.targetku.data.models.Task
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HomeState(
    val userName: String = "",
    val targetCampus: String = "",
    val dailyNote: String = "",
    val averageGrade: Double = 0.0,
    val tasksDone: Int = 0,
    val tasksPending: Int = 0,
    val dailyQuote: String = "",
    val dailyQuoteAuthor: String = "",
    val isSavingNote: Boolean = false,
    val noteSavedSuccess: Boolean = false
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsManager = PreferencesManager(application)

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

            val avg = if (grades.isNotEmpty()) {
                grades.filter { it.actualGrade > 0 }
                    .let { filtered ->
                        if (filtered.isEmpty()) 0.0
                        else filtered.sumOf { it.actualGrade } / filtered.size
                    }
            } else 0.0

            val quote = quotes.random()

            _state.value = HomeState(
                userName = prefs.name,
                targetCampus = prefs.targetCampus.ifEmpty { "Kampus Impianmu" },
                dailyNote = prefsManager.dailyNote,
                averageGrade = avg,
                tasksDone = tasks.count { it.isDone },
                tasksPending = tasks.count { !it.isDone },
                dailyQuote = quote.first,
                dailyQuoteAuthor = quote.second
            )
        }
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
}
