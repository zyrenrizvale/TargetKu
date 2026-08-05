package com.rizki.targetku.data.models

import com.google.gson.annotations.SerializedName

// === User Preferences ===
data class UserPrefs(
    val isLoggedIn: Boolean = false,
    val isOnboarded: Boolean = false,
    val name: String = "",
    val schoolName: String = "",
    val studyPlan: String = "Dalam Negeri",
    val targetCampus: String = "",
    val targetMajor: String = "",
    val profilePhotoUri: String = ""
)

// === University from Hipolabs API ===
data class University(
    @SerializedName("name") val name: String = "",
    @SerializedName("country") val country: String = "",
    @SerializedName("alpha_two_code") val countryCode: String = "",
    @SerializedName("web_pages") val webPages: List<String> = emptyList(),
    @SerializedName("domains") val domains: List<String> = emptyList(),
    @SerializedName("state-province") val stateProvince: String? = null
) {
    val displayName: String get() = "$name ($country)"
}

// === Major ===
data class Major(
    val id: String,
    val name: String,
    val description: String = ""
)

// === Grade Entry ===
data class GradeEntry(
    val id: String,
    val semester: String,   // e.g., "Kelas 10 Semester 1"
    val subject: String,
    val targetGrade: Double,
    val actualGrade: Double,
    val notes: String = ""
) {
    val delta: Double get() = actualGrade - targetGrade
    val isOnTarget: Boolean get() = actualGrade >= targetGrade
}

// === Task ===
data class Task(
    val id: String,
    val title: String,
    val description: String = "",
    val isDone: Boolean = false,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val dueDate: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

enum class TaskPriority(val label: String, val color: Long) {
    LOW("Rendah", 0xFF86EFAC),
    MEDIUM("Sedang", 0xFFFCD34D),
    HIGH("Tinggi", 0xFFFDA4AF)
}

// === Schedule Item ===
data class ScheduleItem(
    val id: String,
    val title: String,
    val subject: String,
    val startTime: String,
    val endTime: String,
    val dayOfWeek: Int, // 1 = Monday, 7 = Sunday
    val color: Long = 0xFFB3D9F7,
    val isCompleted: Boolean = false
)

// === Chat Message ===
data class ChatMessage(
    val id: String,
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val isLoading: Boolean = false
)

// === Study Session ===
data class StudySession(
    val id: String,
    val startTime: Long,
    val endTime: Long? = null,
    val durationMinutes: Int = 0,
    val isActive: Boolean = true,
    val skipReason: String = "",
    val skipPhotoPath: String = ""
)

// === Daily Note ===
data class DailyNote(
    val id: String,
    val content: String,
    val date: String,
    val createdAt: Long = System.currentTimeMillis()
)

// === Gemini API Request/Response ===
data class GeminiRequest(
    val contents: List<GeminiContent>
)

data class GeminiContent(
    val parts: List<GeminiPart>,
    val role: String = "user"
)

data class GeminiPart(
    val text: String
)

data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null,
    val error: GeminiError? = null
)

data class GeminiCandidate(
    val content: GeminiContent? = null,
    val finishReason: String = ""
)

data class GeminiError(
    val code: Int,
    val message: String,
    val status: String
)
