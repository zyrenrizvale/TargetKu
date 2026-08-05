package com.rizki.targetku.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rizki.targetku.data.PreferencesManager
import com.rizki.targetku.data.api.RetrofitClient
import com.rizki.targetku.data.models.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

data class AiTutorState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isTyping: Boolean = false,
    val apiKeyInput: String = "",
    val showApiKeyDialog: Boolean = false
)

class AiTutorViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsManager = PreferencesManager(application)
    private val _state = MutableStateFlow(AiTutorState())
    val state: StateFlow<AiTutorState> = _state.asStateFlow()

    private val API_KEY = "" // Leave empty to use mock, or set via settings

    init {
        val history = prefsManager.getChatMessages().filter { !it.isLoading }
        if (history.isEmpty()) {
            // Welcome message from Kiku
            val welcome = ChatMessage(
                id = UUID.randomUUID().toString(),
                content = "Halo! Aku Kiku, AI Study Buddy kamu! Tanyakan apa saja tentang pelajaran, persiapan UTBK, beasiswa, atau tips belajar. Aku siap membantu! Belajar bareng yuk!",
                isFromUser = false
            )
            _state.value = _state.value.copy(messages = listOf(welcome))
            prefsManager.saveChatMessages(listOf(welcome))
        } else {
            _state.value = _state.value.copy(messages = history)
        }
    }

    fun onInputChange(text: String) {
        _state.value = _state.value.copy(inputText = text)
    }

    fun sendMessage() {
        val text = _state.value.inputText.trim()
        if (text.isBlank()) return

        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            content = text,
            isFromUser = true
        )

        val loadingMsg = ChatMessage(
            id = "loading",
            content = "",
            isFromUser = false,
            isLoading = true
        )

        val updatedMessages = _state.value.messages + userMsg + loadingMsg
        _state.value = _state.value.copy(
            messages = updatedMessages,
            inputText = "",
            isTyping = true
        )

        viewModelScope.launch {
            val apiKey = prefsManager.geminiApiKey.ifEmpty { API_KEY }
            val response = if (apiKey.isNotEmpty()) {
                callGeminiApi(text, apiKey)
            } else {
                kotlinx.coroutines.delay(1200) // Simulate network delay
                getMockResponse(text)
            }

            val aiMsg = ChatMessage(
                id = UUID.randomUUID().toString(),
                content = response,
                isFromUser = false
            )

            val finalMessages = _state.value.messages
                .filter { it.id != "loading" } + aiMsg

            _state.value = _state.value.copy(
                messages = finalMessages,
                isTyping = false
            )
            prefsManager.saveChatMessages(finalMessages)
        }
    }

    private suspend fun callGeminiApi(userMessage: String, apiKey: String): String {
        return try {
            val systemPrompt = """Kamu adalah Kiku, AI Study Buddy yang ramah, cerdas, dan bersemangat untuk membantu siswa Indonesia mempersiapkan diri masuk universitas impian. 
                Jawab dalam Bahasa Indonesia yang ramah dan mudah dipahami. 
                Bantu dengan pertanyaan tentang pelajaran sekolah (matematika, fisika, kimia, biologi, bahasa, sejarah), 
                persiapan UTBK/SNBT, beasiswa (LPDP, dll), tips belajar, dan strategi masuk universitas.
                Berikan jawaban yang informatif, terstruktur, dan motivatif.
                User bertanya: $userMessage"""

            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(GeminiPart(text = systemPrompt)),
                        role = "user"
                    )
                )
            )

            val response = RetrofitClient.geminiApi.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: getMockResponse(userMessage)
        } catch (e: Exception) {
            getMockResponse(userMessage)
        }
    }

    private fun getMockResponse(userMessage: String): String {
        val lower = userMessage.lowercase()
        return when {
            // Math
            lower.contains("matematika") || lower.contains("math") || lower.contains("kalkulus") ||
                    lower.contains("aljabar") || lower.contains("geometri") || lower.contains("statistik") -> {
                getMathResponse(lower)
            }
            // Physics
            lower.contains("fisika") || lower.contains("physics") || lower.contains("newton") ||
                    lower.contains("gravitasi") || lower.contains("listrik") || lower.contains("magnet") -> {
                getPhysicsResponse(lower)
            }
            // Chemistry
            lower.contains("kimia") || lower.contains("chemistry") || lower.contains("atom") ||
                    lower.contains("molekul") || lower.contains("reaksi") -> {
                getChemistryResponse(lower)
            }
            // Biology
            lower.contains("biologi") || lower.contains("biology") || lower.contains("sel") ||
                    lower.contains("dna") || lower.contains("evolusi") || lower.contains("ekologi") -> {
                getBiologyResponse(lower)
            }
            // Psychology
            lower.contains("psikologi") || lower.contains("psychology") || lower.contains("perilaku") ||
                    lower.contains("kognitif") || lower.contains("emosi") -> {
                getPsychologyResponse(lower)
            }
            // English
            lower.contains("bahasa inggris") || lower.contains("english") || lower.contains("grammar") ||
                    lower.contains("vocabulary") || lower.contains("toefl") || lower.contains("ielts") -> {
                getEnglishResponse(lower)
            }
            // UTBK/SNBT
            lower.contains("utbk") || lower.contains("snbt") || lower.contains("sbmptn") ||
                    lower.contains("tes") -> {
                getUtbkResponse()
            }
            // Scholarship
            lower.contains("beasiswa") || lower.contains("scholarship") || lower.contains("lpdp") -> {
                getScholarshipResponse()
            }
            // Study tips
            lower.contains("belajar") || lower.contains("tips") || lower.contains("cara") ||
                    lower.contains("strategi") -> {
                getStudyTipsResponse()
            }
            // University
            lower.contains("universitas") || lower.contains("kampus") || lower.contains("kuliah") -> {
                getUniversityResponse()
            }
            // Greetings
            lower.contains("halo") || lower.contains("hai") || lower.contains("hello") ||
                    lower.contains("hi") -> {
                "Halo! Senang bertemu denganmu! Aku Kiku, siap membantu perjalanan belajarmu. Ada yang ingin kamu pelajari hari ini? Tanyakan apa saja - matematika, fisika, kimia, tips UTBK, atau informasi beasiswa!"
            }
            // Default
            else -> {
                "Pertanyaan yang menarik! Berdasarkan pemahamanku sebagai AI Study Buddy, ini yang bisa aku jelaskan:\n\n" +
                        "Topik yang kamu tanyakan tentang \"${userMessage.take(50)}\" adalah hal yang penting dalam persiapan akademikmu.\n\n" +
                        "Untuk penjelasan yang lebih detail, kamu bisa:\n" +
                        "1. Coba tanyakan dengan lebih spesifik (misalnya: 'Jelaskan rumus integral', 'Apa itu fotosintesis?')\n" +
                        "2. Sebutkan mata pelajaran yang ingin dipelajari\n" +
                        "3. Tanyakan tentang UTBK, beasiswa, atau tips belajar\n\n" +
                        "Aku siap membantu! Semangat belajar!"
            }
        }
    }

    private fun getMathResponse(lower: String): String {
        return when {
            lower.contains("kalkulus") || lower.contains("turunan") || lower.contains("integral") -> {
                """Kalkulus adalah cabang matematika yang mempelajari perubahan dan akumulasi!

**Turunan (Diferensial):**
- f(x) = x^n → f'(x) = n * x^(n-1)
- Contoh: f(x) = x^3 → f'(x) = 3x^2

**Integral:**
- ∫x^n dx = x^(n+1)/(n+1) + C
- Contoh: ∫x^2 dx = x^3/3 + C

**Tips belajar kalkulus:**
1. Kuasai aljabar dasar dulu
2. Latihan soal setiap hari minimal 10 soal
3. Gunakan grafik untuk memahami konsep
4. Kalkulus itu soal latihan, bukan hafalan!

Semangat! Kalkulus yang konsisten = nilai bagus!"""
            }
            lower.contains("statistik") || lower.contains("probabilitas") -> {
                """Statistik dan Probabilitas sangat penting untuk UTBK!

**Ukuran Pemusatan:**
- Mean (rata-rata): x̄ = Σx/n
- Median: nilai tengah data terurut
- Modus: nilai yang paling sering muncul

**Probabilitas:**
- P(A) = jumlah kejadian A / total kejadian
- P(A ∪ B) = P(A) + P(B) - P(A ∩ B)

**Contoh soal UTBK:**
Dari 10 bola (5 merah, 3 biru, 2 kuning), peluang mengambil bola merah = 5/10 = 1/2

Latihan banyak soal ya!"""
            }
            else -> {
                """Matematika adalah fondasi semua ilmu sains!

**Topik utama Matematika SMA:**
- **Aljabar:** fungsi, persamaan, pertidaksamaan
- **Geometri:** bangun datar, ruang, koordinat
- **Trigonometri:** sin, cos, tan, identitas trig
- **Kalkulus:** turunan, integral
- **Statistik:** distribusi, probabilitas

**Tips sukses Matematika:**
1. Pahami konsep, jangan sekadar hafal rumus
2. Latihan soal bertahap dari mudah ke sulit
3. Buat catatan rumus yang rapi
4. Kerjakan soal UTBK tahun lalu
5. Jangan skip materi - matematika itu berurutan!

Mau belajar topik spesifik apa? Aku siap jelaskan!"""
            }
        }
    }

    private fun getPhysicsResponse(lower: String): String {
        return """Fisika itu seru! Ini membantu kita memahami cara kerja alam semesta!

**Hukum Newton:**
- Newton I: Benda diam tetap diam, benda bergerak tetap bergerak (Inersia)
- Newton II: F = m × a (Gaya = massa × percepatan)
- Newton III: Aksi = Reaksi

**Rumus Kinematika:**
- v = v₀ + at
- s = v₀t + ½at²
- v² = v₀² + 2as

**Energi:**
- Ep (Potensial) = mgh
- Ek (Kinetik) = ½mv²
- Hukum Kekekalan Energi: Ep + Ek = konstan

**Tips belajar Fisika:**
1. Gambar diagram setiap soal
2. Identifikasi besaran yang diketahui dan dicari
3. Pilih rumus yang tepat
4. Cek satuan!

Fisika UTBK biasanya 15-20 soal. Latihan soal setiap hari ya!"""
    }

    private fun getChemistryResponse(lower: String): String {
        return """Kimia adalah ilmu yang mempelajari materi dan perubahannya!

**Konsep Dasar:**
- Atom: unit terkecil unsur
- Molekul: gabungan 2+ atom
- Ion: atom bermuatan

**Tabel Periodik - Hal Penting:**
- Periode: baris horizontal (jumlah kulit elektron)
- Golongan: kolom vertikal (elektron valensi)
- Logam | Non-logam | Metaloid

**Reaksi Kimia:**
- Sintesis: A + B → AB
- Dekomposisi: AB → A + B
- Redoks: transfer elektron

**Hukum Dasar:**
- Hukum Lavoisier: massa sebelum = massa sesudah
- Molaritas: M = n/V (mol/liter)

**Tips UTBK Kimia:**
1. Hafal konfigurasi elektron unsur-unsur penting
2. Pahami konsep mol dan stoikiometri
3. Latihan reaksi setara kimia

Kimia membutuhkan banyak latihan, semangat!"""
    }

    private fun getBiologyResponse(lower: String): String {
        return """Biologi adalah ilmu kehidupan yang sangat menarik!

**Organisasi Kehidupan:**
Sel → Jaringan → Organ → Sistem Organ → Organisme

**Sel:**
- Sel prokariot: tanpa nukleus (bakteri)
- Sel eukariot: dengan nukleus (hewan, tumbuhan)
- Organel penting: mitokondria, ribosom, kloroplas

**Genetika:**
- DNA → RNA → Protein (dogma sentral)
- Hukum Mendel: dominan vs resesif
- Genotipe vs Fenotipe

**Ekosistem:**
- Produsen → Konsumen → Dekomposer
- Rantai makanan & jaring makanan
- Siklus biogeokimia

**Tips Biologi UTBK:**
1. Buat mindmap setiap bab
2. Hafal istilah ilmiah (bahasa Latin/Yunani)
3. Latihan soal analisis, bukan hafalan semata

Biologi itu luas, tapi kamu pasti bisa menguasainya!"""
    }

    private fun getPsychologyResponse(lower: String): String {
        return """Psikologi mempelajari pikiran, emosi, dan perilaku manusia - sangat relevan!

**Cabang Utama Psikologi:**
- Psikologi Klinis: gangguan mental
- Psikologi Pendidikan: proses belajar
- Psikologi Sosial: perilaku dalam kelompok
- Neuropsikologi: otak dan perilaku

**Teori Perkembangan:**
- Piaget: tahap kognitif (sensorimotor → formal operasional)
- Vygotsky: ZPD & scaffolding
- Erikson: 8 tahap perkembangan psikososial

**Teori Belajar:**
- Behaviorisme: stimulus-respons (Pavlov, Skinner)
- Kognitivisme: proses mental internal
- Konstruktivisme: aktif membangun pengetahuan

**Tips belajar efektif (dari psikologi):**
1. Spaced repetition: belajar sedikit tapi konsisten
2. Active recall: tes diri sendiri, jangan pasif membaca
3. Pomodoro: 25 menit fokus, 5 menit istirahat
4. Growth mindset: percaya kemampuan bisa berkembang!

Psikologi sangat berguna untuk self-improvement!"""
    }

    private fun getEnglishResponse(lower: String): String {
        return """English is very important for your future! Let's improve together!

**Grammar Essentials:**
- Tenses: Present, Past, Future (16 tenses total)
- Modal verbs: can, could, should, must, will, would
- Passive voice: Object + to be + V3

**UTBK TPS - Literasi Bahasa Inggris:**
1. Reading comprehension - baca, pahami konteks
2. Vocabulary in context - cari makna dari konteks
3. Error recognition - temukan kesalahan grammar

**Tips Meningkatkan English:**
1. Tonton film/series berbahasa Inggris dengan subtitle Inggris
2. Baca artikel English (BBC, CNN) 15 menit/hari
3. Tulis diary dalam bahasa Inggris
4. Gunakan Anki untuk vocabulary flashcards

**IELTS/TOEFL untuk beasiswa:**
- IELTS target 6.5+ untuk beasiswa luar negeri
- TOEFL target 79+ (iBT)
- Latihan listening, reading, writing, speaking

You got this! Konsistensi adalah kuncinya!"""
    }

    private fun getUtbkResponse(): String {
        return """UTBK/SNBT adalah gerbang menuju universitas impianmu!

**Struktur Tes SNBT:**
1. **TPS (Tes Potensi Skolastik):**
   - Penalaran Umum
   - Pengetahuan Kuantitatif
   - Pengetahuan dan Pemahaman Umum
   - Pemahaman Bacaan dan Menulis
2. **Literasi:**
   - Bahasa Indonesia
   - Bahasa Inggris
3. **Penalaran Matematika**

**Strategi Sukses UTBK:**
1. Mulai persiapan minimal 6 bulan sebelum
2. Try-out rutin - minimal 1x per minggu
3. Analisis kesalahan setelah setiap TO
4. Fokus pada materi yang sering keluar
5. Latihan manajemen waktu

**Tips Hari H:**
- Tidur cukup malam sebelumnya
- Sarapan yang baik
- Baca soal dengan teliti
- Skip soal sulit, kerjakan yang mudah dulu
- Jaga mental dan kepercayaan diri!

Kamu pasti bisa! Semangat persiapan UTBK!"""
    }

    private fun getScholarshipResponse(): String {
        return """Beasiswa adalah investasi terbaik untuk masa depanmu!

**Beasiswa Populer:**
1. **LPDP (Lembaga Pengelola Dana Pendidikan)**
   - S2/S3 dalam dan luar negeri
   - Syarat: prestasi akademik, essay, wawancara
   
2. **KIP Kuliah** (Kartu Indonesia Pintar)
   - S1 di PT dalam negeri
   - Berbasis ekonomi & prestasi
   
3. **Beasiswa Unggulan Kemendikbud**
   - D4/S1/S2/S3
   - Butuh portofolio prestasi
   
4. **Beasiswa Luar Negeri:**
   - Fulbright (AS)
   - Chevening (UK)
   - MEXT (Jepang)
   - Korean Government Scholarship (Korea)

**Tips Dapat Beasiswa:**
1. Mulai persiapkan akademik dari kelas 10
2. Aktif organisasi dan raih prestasi
3. Pelajari cara menulis personal statement/essay
4. Tingkatkan kemampuan bahasa Inggris (IELTS/TOEFL)
5. Jalin relasi dengan mentor dan alumni beasiswa

Mulai persiapkan dari sekarang ya, jangan tunggu kelas 12!"""
    }

    private fun getStudyTipsResponse(): String {
        return """Tips belajar efektif dari Kiku untuk kamu!

**Teknik Belajar Terbukti:**

1. **Pomodoro Technique:**
   - 25 menit fokus belajar
   - 5 menit istirahat
   - Setelah 4 sesi, istirahat 15-30 menit

2. **Active Recall:**
   - Setelah baca materi, tutup buku dan coba ingat
   - Jauh lebih efektif daripada membaca ulang!

3. **Spaced Repetition:**
   - Ulang materi secara bertahap: 1 hari, 3 hari, 1 minggu, 1 bulan
   - Gunakan flashcard atau Anki

4. **Mind Mapping:**
   - Visualisasikan koneksi antar konsep
   - Sangat membantu untuk pelajaran hafalan

5. **Feynman Technique:**
   - Jelaskan materi seolah mengajar orang lain
   - Jika tidak bisa jelaskan, berarti belum paham betul

**Lingkungan Belajar Ideal:**
- Tempat tenang, meja rapi
- Cahaya cukup
- Handphone di luar jangkauan!
- Musik instrumental (opsional)

**Jaga Kesehatan:**
- Tidur 7-8 jam sehari
- Olahraga ringan 30 menit/hari
- Minum air cukup (8 gelas/hari)

Ingat: kualitas lebih penting dari kuantitas! Belajar 3 jam fokus > 8 jam sambil main HP!"""
    }

    private fun getUniversityResponse(): String {
        return """Informasi kampus untuk membantumu menentukan pilihan!

**Universitas Top Indonesia:**
1. **Universitas Indonesia (UI)** - Depok
   - Unggul: Hukum, Kedokteran, Ekonomi, Komputer
2. **Institut Teknologi Bandung (ITB)** - Bandung
   - Unggul: Teknik, Sains, Seni Rupa
3. **Universitas Gadjah Mada (UGM)** - Yogyakarta
   - Unggul: Pertanian, Kedokteran, Hukum, Teknik
4. **Universitas Brawijaya (UB)** - Malang
   - Unggul: Pertanian, Perikanan, Ekonomi
5. **ITS (Institut Teknologi Sepuluh Nopember)** - Surabaya
   - Unggul: Teknik, Kelautan, Informatika

**Universitas Luar Negeri Populer:**
- Seoul National University (Korea) - Top 10 Asia
- Universitas Tokyo (Jepang)
- National University of Singapore (NUS)
- Peking University (China)
- Australian National University (Australia)

**Tips Memilih Kampus:**
1. Sesuaikan dengan minat dan bakat
2. Pertimbangkan akreditasi dan reputasi jurusan
3. Riset peluang kerja alumni
4. Pertimbangkan biaya dan lokasi
5. Cek jalur masuk (SNBP, SNBT, Mandiri)

Kampus terbaik adalah yang paling sesuai dengan tujuanmu!"""
    }

    fun clearChat() {
        val welcome = ChatMessage(
            id = UUID.randomUUID().toString(),
            content = "Chat dibersihkan! Halo lagi! Aku Kiku, siap membantu belajarmu. Ada yang ingin ditanyakan?",
            isFromUser = false
        )
        _state.value = _state.value.copy(messages = listOf(welcome))
        prefsManager.saveChatMessages(listOf(welcome))
    }

    fun showApiKeyDialog() { _state.value = _state.value.copy(showApiKeyDialog = true) }
    fun hideApiKeyDialog() { _state.value = _state.value.copy(showApiKeyDialog = false) }
    fun onApiKeyInputChange(key: String) { _state.value = _state.value.copy(apiKeyInput = key) }
    fun saveApiKey() {
        prefsManager.geminiApiKey = _state.value.apiKeyInput
        _state.value = _state.value.copy(showApiKeyDialog = false)
    }
}
