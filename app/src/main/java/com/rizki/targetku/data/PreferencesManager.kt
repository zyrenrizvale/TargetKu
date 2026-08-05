package com.rizki.targetku.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rizki.targetku.data.models.*

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("targetku_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    // ============ Auth ============
    var isLoggedIn: Boolean
        get() = prefs.getBoolean(KEY_LOGGED_IN, false)
        set(value) = prefs.edit().putBoolean(KEY_LOGGED_IN, value).apply()

    var isOnboarded: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDED, false)
        set(value) = prefs.edit().putBoolean(KEY_ONBOARDED, value).apply()

    // ============ User Profile ============
    var userName: String
        get() = prefs.getString(KEY_USER_NAME, "Muhammad Rizki") ?: "Muhammad Rizki"
        set(value) = prefs.edit().putString(KEY_USER_NAME, value).apply()

    var schoolName: String
        get() = prefs.getString(KEY_SCHOOL_NAME, "") ?: ""
        set(value) = prefs.edit().putString(KEY_SCHOOL_NAME, value).apply()

    var studyPlan: String
        get() = prefs.getString(KEY_STUDY_PLAN, "Dalam Negeri") ?: "Dalam Negeri"
        set(value) = prefs.edit().putString(KEY_STUDY_PLAN, value).apply()

    var targetCampus: String
        get() = prefs.getString(KEY_TARGET_CAMPUS, "") ?: ""
        set(value) = prefs.edit().putString(KEY_TARGET_CAMPUS, value).apply()

    var targetMajor: String
        get() = prefs.getString(KEY_TARGET_MAJOR, "") ?: ""
        set(value) = prefs.edit().putString(KEY_TARGET_MAJOR, value).apply()

    var profilePhotoUri: String
        get() = prefs.getString(KEY_PROFILE_PHOTO, "") ?: ""
        set(value) = prefs.edit().putString(KEY_PROFILE_PHOTO, value).apply()

    fun getUserPrefs(): UserPrefs = UserPrefs(
        isLoggedIn = isLoggedIn,
        isOnboarded = isOnboarded,
        name = userName,
        schoolName = schoolName,
        studyPlan = studyPlan,
        targetCampus = targetCampus,
        targetMajor = targetMajor,
        profilePhotoUri = profilePhotoUri
    )

    fun saveUserPrefs(prefs: UserPrefs) {
        userName = prefs.name
        schoolName = prefs.schoolName
        studyPlan = prefs.studyPlan
        targetCampus = prefs.targetCampus
        targetMajor = prefs.targetMajor
    }

    // ============ Daily Note ============
    var dailyNote: String
        get() = prefs.getString(KEY_DAILY_NOTE, "") ?: ""
        set(value) = prefs.edit().putString(KEY_DAILY_NOTE, value).apply()

    // ============ Grade Entries ============
    fun saveGradeEntries(entries: List<GradeEntry>) {
        val json = gson.toJson(entries)
        prefs.edit().putString(KEY_GRADES, json).apply()
    }

    fun getGradeEntries(): List<GradeEntry> {
        val json = prefs.getString(KEY_GRADES, null) ?: return defaultGrades()
        return try {
            val type = object : TypeToken<List<GradeEntry>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            defaultGrades()
        }
    }

    private fun defaultGrades(): List<GradeEntry> = listOf(
        GradeEntry("1", "Kelas 10 Semester 1", "Matematika", 85.0, 0.0),
        GradeEntry("2", "Kelas 10 Semester 1", "Bahasa Indonesia", 88.0, 0.0),
        GradeEntry("3", "Kelas 10 Semester 1", "Bahasa Inggris", 90.0, 0.0),
        GradeEntry("4", "Kelas 10 Semester 2", "Matematika", 85.0, 0.0),
        GradeEntry("5", "Kelas 11 Semester 1", "Fisika", 82.0, 0.0),
        GradeEntry("6", "Kelas 12 Semester 1", "Kimia", 80.0, 0.0),
    )

    // ============ Tasks ============
    fun saveTasks(tasks: List<Task>) {
        val json = gson.toJson(tasks)
        prefs.edit().putString(KEY_TASKS, json).apply()
    }

    fun getTasks(): List<Task> {
        val json = prefs.getString(KEY_TASKS, null) ?: return defaultTasks()
        return try {
            val type = object : TypeToken<List<Task>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            defaultTasks()
        }
    }

    private fun defaultTasks(): List<Task> = listOf(
        Task("t1", "Belajar Matematika - Kalkulus", "Chapter 1-3", false, TaskPriority.HIGH),
        Task("t2", "Latihan Soal UTBK", "Paket A", false, TaskPriority.HIGH),
        Task("t3", "Baca Buku Bahasa Inggris", "Unit 5", false, TaskPriority.MEDIUM),
        Task("t4", "Riset Beasiswa LPDP", "", false, TaskPriority.MEDIUM),
        Task("t5", "Buat Essay Motivasi", "Draft pertama", false, TaskPriority.LOW),
    )

    // ============ Schedule ============
    fun saveScheduleItems(items: List<ScheduleItem>) {
        val json = gson.toJson(items)
        prefs.edit().putString(KEY_SCHEDULE, json).apply()
    }

    fun getScheduleItems(): List<ScheduleItem> {
        val json = prefs.getString(KEY_SCHEDULE, null) ?: return defaultSchedule()
        return try {
            val type = object : TypeToken<List<ScheduleItem>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            defaultSchedule()
        }
    }

    private fun defaultSchedule(): List<ScheduleItem> = listOf(
        ScheduleItem("s1", "Belajar Matematika", "Matematika", "07:00", "08:30", 1, 0xFFB3D9F7),
        ScheduleItem("s2", "Latihan Bahasa Inggris", "Bahasa Inggris", "09:00", "10:00", 1, 0xFFF7B3D9),
        ScheduleItem("s3", "Review Materi Fisika", "Fisika", "13:00", "14:30", 2, 0xFFD9B3F7),
        ScheduleItem("s4", "Latihan Soal UTBK", "UTBK Prep", "15:00", "17:00", 2, 0xFFB3F7D9),
        ScheduleItem("s5", "Belajar Kimia", "Kimia", "07:00", "08:30", 3, 0xFFF7D9B3),
        ScheduleItem("s6", "Membaca Buku", "Literasi", "20:00", "21:00", 1, 0xFFB3D9F7),
    )

    // ============ Chat Messages ============
    fun saveChatMessages(messages: List<ChatMessage>) {
        // Keep last 50 messages to avoid large storage
        val recent = if (messages.size > 50) messages.takeLast(50) else messages
        val json = gson.toJson(recent)
        prefs.edit().putString(KEY_CHAT, json).apply()
    }

    fun getChatMessages(): List<ChatMessage> {
        val json = prefs.getString(KEY_CHAT, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<ChatMessage>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ============ AI API Key ============
    var geminiApiKey: String
        get() = prefs.getString(KEY_GEMINI_KEY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_GEMINI_KEY, value).apply()

    // ============ Logout ============
    fun logout() {
        prefs.edit()
            .putBoolean(KEY_LOGGED_IN, false)
            .apply()
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_LOGGED_IN = "is_logged_in"
        private const val KEY_ONBOARDED = "is_onboarded"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_SCHOOL_NAME = "school_name"
        private const val KEY_STUDY_PLAN = "study_plan"
        private const val KEY_TARGET_CAMPUS = "target_campus"
        private const val KEY_TARGET_MAJOR = "target_major"
        private const val KEY_PROFILE_PHOTO = "profile_photo"
        private const val KEY_DAILY_NOTE = "daily_note"
        private const val KEY_GRADES = "grade_entries"
        private const val KEY_TASKS = "tasks"
        private const val KEY_SCHEDULE = "schedule_items"
        private const val KEY_CHAT = "chat_messages"
        private const val KEY_GEMINI_KEY = "gemini_api_key"
    }
}
