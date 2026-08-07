package com.rizki.targetku.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rizki.targetku.data.PreferencesManager
import com.rizki.targetku.data.api.RetrofitClient
import com.rizki.targetku.data.models.Major
import com.rizki.targetku.data.models.University
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class OnboardingState(
    val currentStep: Int = 0,
    val name: String = "Muhammad Rizki",
    val schoolName: String = "MAN 1 Indragiri Hilir",
    val studyPlan: String = "Dalam Negeri",
    val selectedUniversity: University? = null,
    val universitySearchQuery: String = "",
    val universityResults: List<University> = emptyList(),
    val isUniversityLoading: Boolean = false,
    val universityError: String = "",
    val selectedMajor: Major? = null,
    val majorSearchQuery: String = "",
    val availableMajors: List<Major> = emptyList(),
    val isMajorLoading: Boolean = false,
    val isFinishing: Boolean = false,
    val isCompleted: Boolean = false  // triggers navigation from Composable safely
)

class OnboardingViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsManager = PreferencesManager(application)

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    init {
        setupUniversitySearch()
        // Pre-load with existing prefs if re-editing
        val prefs = prefsManager.getUserPrefs()
        if (prefs.name.isNotEmpty()) {
            _state.value = _state.value.copy(
                name = prefs.name,
                schoolName = prefs.schoolName,
                studyPlan = prefs.studyPlan
            )
        }
    }

    @OptIn(FlowPreview::class)
    private fun setupUniversitySearch() {
        viewModelScope.launch {
            _searchQuery
                .debounce(500L)
                .filter { it.length >= 2 }
                .distinctUntilChanged()
                .collect { query ->
                    searchUniversities(query)
                }
        }
    }

    fun onNameChange(value: String) {
        _state.value = _state.value.copy(name = value)
    }

    fun onSchoolNameChange(value: String) {
        _state.value = _state.value.copy(schoolName = value)
    }

    fun onStudyPlanChange(value: String) {
        _state.value = _state.value.copy(studyPlan = value)
    }

    fun onUniversitySearchChange(query: String) {
        _state.value = _state.value.copy(
            universitySearchQuery = query,
            universityError = ""
        )
        _searchQuery.value = query
        if (query.length < 2) {
            _state.value = _state.value.copy(universityResults = emptyList())
        }
    }

    private fun searchUniversities(query: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isUniversityLoading = true, universityError = "")
            try {
                val results = RetrofitClient.universityApi.searchUniversities(query)
                _state.value = _state.value.copy(
                    universityResults = results.take(20),
                    isUniversityLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isUniversityLoading = false,
                    universityError = "Gagal mencari universitas. Coba lagi.",
                    universityResults = getFallbackUniversities(query)
                )
            }
        }
    }

    private fun getFallbackUniversities(query: String): List<University> {
        val allUniversities = listOf(
            University("Seoul National University", "South Korea", "KR"),
            University("Universitas Indonesia", "Indonesia", "ID"),
            University("Institut Teknologi Bandung", "Indonesia", "ID"),
            University("Universitas Gadjah Mada", "Indonesia", "ID"),
            University("Universitas Brawijaya", "Indonesia", "ID"),
            University("National University of Singapore", "Singapore", "SG"),
            University("University of Tokyo", "Japan", "JP"),
            University("Peking University", "China", "CN"),
            University("University of Melbourne", "Australia", "AU"),
            University("Harvard University", "United States", "US"),
            University("MIT", "United States", "US"),
            University("Oxford University", "United Kingdom", "GB"),
        )
        return allUniversities.filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.country.contains(query, ignoreCase = true)
        }
    }

    fun onUniversitySelected(university: University) {
        _state.value = _state.value.copy(
            selectedUniversity = university,
            universitySearchQuery = university.name,
            universityResults = emptyList(),
            selectedMajor = null,
            majorSearchQuery = ""
        )
        loadMajorsForUniversity(university)
    }

    private fun loadMajorsForUniversity(university: University) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isMajorLoading = true)

            // Simulate a delay as if fetching from API
            kotlinx.coroutines.delay(800)

            val majors = getMajorsForUniversity(university.name)
            _state.value = _state.value.copy(
                availableMajors = majors,
                isMajorLoading = false
            )
        }
    }

    private fun getMajorsForUniversity(universityName: String): List<Major> {
        // Smart mock: return majors based on university keyword matching
        val techUniversities = listOf("teknologi", "itb", "its", "mit", "technology", "engineering",
            "polytechnic", "teknik")
        val medicalUniversities = listOf("kedokteran", "medical", "health", "kesehatan")
        val generalUniversities = listOf("indonesia", "gadjah", "brawijaya", "national", "seoul",
            "tokyo", "harvard", "oxford", "cambridge", "melbourne", "snu", "ui", "ugm")

        val lowerName = universityName.lowercase()

        return when {
            techUniversities.any { lowerName.contains(it) } -> techMajors
            medicalUniversities.any { lowerName.contains(it) } -> medicalMajors
            lowerName.contains("hukum") || lowerName.contains("law") -> lawMajors
            lowerName.contains("ekonomi") || lowerName.contains("economic") || lowerName.contains("bisnis") -> economicsMajors
            else -> generalMajors
        }
    }

    fun onMajorSearchChange(query: String) {
        _state.value = _state.value.copy(majorSearchQuery = query)
    }

    fun onMajorSelected(major: Major) {
        _state.value = _state.value.copy(selectedMajor = major, majorSearchQuery = major.name)
    }

    fun nextStep() {
        val currentStep = _state.value.currentStep
        if (currentStep < TOTAL_STEPS - 1) {
            _state.value = _state.value.copy(currentStep = currentStep + 1)
        }
    }

    fun previousStep() {
        val currentStep = _state.value.currentStep
        if (currentStep > 0) {
            _state.value = _state.value.copy(currentStep = currentStep - 1)
        }
    }

    fun canProceed(): Boolean {
        return when (_state.value.currentStep) {
            0 -> _state.value.name.isNotBlank()
            1 -> _state.value.schoolName.isNotBlank()
            2 -> _state.value.studyPlan.isNotBlank()
            3 -> _state.value.selectedUniversity != null
            4 -> _state.value.selectedMajor != null
            5 -> true
            else -> true
        }
    }

    fun finishOnboarding() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isFinishing = true)
            val s = _state.value
            prefsManager.userName = s.name
            prefsManager.schoolName = s.schoolName
            prefsManager.studyPlan = s.studyPlan
            prefsManager.targetCampus = s.selectedUniversity?.name ?: ""
            prefsManager.targetMajor = s.selectedMajor?.name ?: ""
            prefsManager.isOnboarded = true
            _state.value = _state.value.copy(isFinishing = false, isCompleted = true)
        }
    }

    companion object {
        const val TOTAL_STEPS = 6

        val generalMajors = listOf(
            Major("cs", "Ilmu Komputer / Computer Science"),
            Major("if", "Teknik Informatika"),
            Major("psych", "Psikologi"),
            Major("econ", "Ekonomi"),
            Major("law", "Hukum / Ilmu Hukum"),
            Major("med", "Kedokteran / Medicine"),
            Major("eng", "Teknik Mesin"),
            Major("arch", "Arsitektur"),
            Major("comm", "Ilmu Komunikasi"),
            Major("intl", "Hubungan Internasional"),
            Major("bio", "Biologi"),
            Major("chem", "Kimia"),
            Major("phys", "Fisika"),
            Major("math", "Matematika"),
            Major("hist", "Sejarah"),
            Major("edu", "Pendidikan / PGSD"),
            Major("agri", "Pertanian"),
            Major("marine", "Ilmu Kelautan"),
        )

        val techMajors = listOf(
            Major("cs", "Ilmu Komputer / Computer Science"),
            Major("if", "Teknik Informatika"),
            Major("ee", "Teknik Elektro"),
            Major("me", "Teknik Mesin"),
            Major("ce", "Teknik Sipil"),
            Major("chem_eng", "Teknik Kimia"),
            Major("aero", "Teknik Penerbangan / Aerospace"),
            Major("ds", "Data Science / Sains Data"),
            Major("ai", "Kecerdasan Buatan / Artificial Intelligence"),
            Major("cyber", "Keamanan Siber / Cybersecurity"),
            Major("robotics", "Teknik Robotika"),
            Major("math", "Matematika"),
            Major("phys", "Fisika"),
            Major("bio_eng", "Teknik Biomedis"),
            Major("env_eng", "Teknik Lingkungan"),
        )

        val medicalMajors = listOf(
            Major("med", "Kedokteran Umum / Medicine"),
            Major("dent", "Kedokteran Gigi"),
            Major("pharm", "Farmasi / Pharmacy"),
            Major("nurse", "Keperawatan / Nursing"),
            Major("pub_health", "Kesehatan Masyarakat"),
            Major("nutrition", "Gizi / Nutrition"),
            Major("physio", "Fisioterapi"),
            Major("bio", "Biologi / Biology"),
            Major("bio_med", "Biomedis / Biomedical"),
            Major("vet", "Kedokteran Hewan"),
        )

        val lawMajors = listOf(
            Major("law", "Ilmu Hukum / Law"),
            Major("intl_law", "Hukum Internasional"),
            Major("business_law", "Hukum Bisnis"),
            Major("criminal_law", "Hukum Pidana"),
            Major("civil_law", "Hukum Perdata"),
            Major("admin_law", "Hukum Administrasi Negara"),
        )

        val economicsMajors = listOf(
            Major("econ", "Ilmu Ekonomi / Economics"),
            Major("mgmt", "Manajemen / Management"),
            Major("acct", "Akuntansi / Accounting"),
            Major("fin", "Keuangan / Finance"),
            Major("bus", "Bisnis Internasional"),
            Major("mkt", "Pemasaran / Marketing"),
            Major("stat", "Statistika"),
            Major("dev_econ", "Ekonomi Pembangunan"),
        )
    }
}
