@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.rizki.targetku.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rizki.targetku.data.models.Major
import com.rizki.targetku.data.models.University
import com.rizki.targetku.viewmodel.OnboardingViewModel
import com.rizki.targetku.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit,
    viewModel: OnboardingViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(pageCount = { OnboardingViewModel.TOTAL_STEPS })
    val scope = rememberCoroutineScope()

    LaunchedEffect(state.currentStep) {
        if (pagerState.currentPage != state.currentStep) {
            pagerState.animateScrollToPage(state.currentStep)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(GradientStart, GradientEnd)
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top progress bar
            OnboardingProgressBar(
                currentStep = state.currentStep,
                totalSteps = OnboardingViewModel.TOTAL_STEPS
            )

            // Pager content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                userScrollEnabled = false
            ) { page ->
                when (page) {
                    0 -> StepName(
                        name = state.name,
                        onNameChange = viewModel::onNameChange
                    )
                    1 -> StepSchool(
                        schoolName = state.schoolName,
                        onSchoolChange = viewModel::onSchoolNameChange
                    )
                    2 -> StepStudyPlan(
                        selectedPlan = state.studyPlan,
                        onPlanChange = viewModel::onStudyPlanChange
                    )
                    3 -> StepUniversity(
                        searchQuery = state.universitySearchQuery,
                        results = state.universityResults,
                        selectedUniversity = state.selectedUniversity,
                        isLoading = state.isUniversityLoading,
                        error = state.universityError,
                        onQueryChange = viewModel::onUniversitySearchChange,
                        onSelect = viewModel::onUniversitySelected
                    )
                    4 -> StepMajor(
                        searchQuery = state.majorSearchQuery,
                        majors = state.availableMajors,
                        selectedMajor = state.selectedMajor,
                        isLoading = state.isMajorLoading,
                        selectedUniversity = state.selectedUniversity?.name ?: "",
                        onQueryChange = viewModel::onMajorSearchChange,
                        onSelect = viewModel::onMajorSelected
                    )
                    5 -> StepTutorial()
                }
            }

            // Navigation buttons
            OnboardingNavButtons(
                currentStep = state.currentStep,
                totalSteps = OnboardingViewModel.TOTAL_STEPS,
                canProceed = viewModel.canProceed(),
                isFinishing = state.isFinishing,
                onPrevious = {
                    viewModel.previousStep()
                },
                onNext = {
                    if (state.currentStep < OnboardingViewModel.TOTAL_STEPS - 1) {
                        viewModel.nextStep()
                    } else {
                        viewModel.finishOnboarding { onOnboardingComplete() }
                    }
                }
            )
        }
    }
}

@Composable
private fun OnboardingProgressBar(currentStep: Int, totalSteps: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Langkah ${currentStep + 1} dari $totalSteps",
                fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium
            )
            Text(
                "${((currentStep + 1) * 100 / totalSteps)}%",
                fontSize = 12.sp, color = BabyBlueDark, fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { (currentStep + 1f) / totalSteps },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = BabyBlueDark,
            trackColor = BabyBlue.copy(alpha = 0.3f),
        )
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(totalSteps) { index ->
                Box(
                    modifier = Modifier
                        .size(if (index == currentStep) 10.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                index < currentStep -> BabyBlueDark
                                index == currentStep -> SoftPinkDark
                                else -> BabyBlue.copy(alpha = 0.3f)
                            }
                        )
                )
            }
        }
    }
}

@Composable
private fun OnboardingNavButtons(
    currentStep: Int,
    totalSteps: Int,
    canProceed: Boolean,
    isFinishing: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(White.copy(alpha = 0.9f))
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (currentStep > 0) {
            OutlinedButton(
                onClick = onPrevious,
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = BabyBlueDark)
            ) {
                Icon(Icons.Default.ArrowBack, null, Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Kembali", fontWeight = FontWeight.Bold)
            }
        }

        Button(
            onClick = onNext,
            modifier = Modifier.weight(if (currentStep > 0) 1f else Float.MAX_VALUE).height(52.dp),
            enabled = canProceed && !isFinishing,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (currentStep == totalSteps - 1) SoftPinkDark else BabyBlueDark
            )
        ) {
            if (isFinishing) {
                CircularProgressIndicator(color = White, modifier = Modifier.size(20.dp))
            } else {
                Text(
                    if (currentStep == totalSteps - 1) "Mulai Belajar!" else "Lanjut",
                    fontWeight = FontWeight.ExtraBold, fontSize = 16.sp
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    if (currentStep == totalSteps - 1) Icons.Default.Rocket else Icons.Default.ArrowForward,
                    null, Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun StepContainer(
    emoji: ImageVector,
    emojiTint: Color,
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(emojiTint.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(emoji, null, tint = emojiTint, modifier = Modifier.size(44.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text(title, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary, textAlign = TextAlign.Center)
        Text(subtitle, fontSize = 14.sp, color = TextSecondary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        content()
    }
}

@Composable
private fun StepName(name: String, onNameChange: (String) -> Unit) {
    StepContainer(
        emoji = Icons.Default.EmojiPeople,
        emojiTint = BabyBlueDark,
        title = "Halo! Siapa namamu?",
        subtitle = "Kami akan menyapa kamu dengan nama ini"
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Nama Lengkapmu") },
            leadingIcon = { Icon(Icons.Default.Person, null, tint = BabyBlueDark) },
            placeholder = { Text("Muhammad Rizki", color = TextMuted) },
            singleLine = true,
            colors = onboardingFieldColors(),
            shape = RoundedCornerShape(14.dp)
        )
    }
}

@Composable
private fun StepSchool(schoolName: String, onSchoolChange: (String) -> Unit) {
    StepContainer(
        emoji = Icons.Default.School,
        emojiTint = SoftPinkDark,
        title = "Apa nama sekolahmu?",
        subtitle = "Masukkan nama sekolah/madrasah kamu saat ini"
    ) {
        OutlinedTextField(
            value = schoolName,
            onValueChange = onSchoolChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Nama Sekolah") },
            leadingIcon = { Icon(Icons.Default.School, null, tint = SoftPinkDark) },
            placeholder = { Text("MAN 1 Indragiri Hilir", color = TextMuted) },
            singleLine = true,
            colors = onboardingFieldColors(SoftPinkDark, SoftPink),
            shape = RoundedCornerShape(14.dp)
        )
    }
}

@Composable
private fun StepStudyPlan(selectedPlan: String, onPlanChange: (String) -> Unit) {
    StepContainer(
        emoji = Icons.Default.TravelExplore,
        emojiTint = LavenderDark,
        title = "Apa rencana kuliahmu?",
        subtitle = "Pilih jenis universitas yang kamu impikan"
    ) {
        val options = listOf(
            Triple("Dalam Negeri", "Universitas di Indonesia", Icons.Default.Home),
            Triple("Luar Negeri", "Universitas di luar Indonesia", Icons.Default.Flight)
        )
        options.forEach { (label, subtitle, icon) ->
            val isSelected = selectedPlan == label
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isSelected) LavenderDark.copy(alpha = 0.15f) else OffWhite
                    )
                    .border(
                        2.dp,
                        if (isSelected) LavenderDark else Color(0xFFE2E8F0),
                        RoundedCornerShape(16.dp)
                    )
                    .clickable { onPlanChange(label) }
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) LavenderDark else Color(0xFFE2E8F0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, null, tint = if (isSelected) White else TextMuted, modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(label, fontWeight = FontWeight.Bold, color = if (isSelected) LavenderDark else TextPrimary)
                        Text(subtitle, fontSize = 12.sp, color = TextSecondary)
                    }
                    Spacer(Modifier.weight(1f))
                    if (isSelected) {
                        Icon(Icons.Default.CheckCircle, null, tint = LavenderDark)
                    }
                }
            }
        }
    }
}

@Composable
private fun StepUniversity(
    searchQuery: String,
    results: List<University>,
    selectedUniversity: University?,
    isLoading: Boolean,
    error: String,
    onQueryChange: (String) -> Unit,
    onSelect: (University) -> Unit
) {
    StepContainer(
        emoji = Icons.Default.AccountBalance,
        emojiTint = BabyBlueDark,
        title = "Pilih Kampus Impianmu!",
        subtitle = "Cari universitas di seluruh dunia"
    ) {
        // Selected university display
        if (selectedUniversity != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(BabyBlue.copy(alpha = 0.2f))
                    .border(1.dp, BabyBlueDark, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.School, null, tint = BabyBlueDark)
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(selectedUniversity.name, fontWeight = FontWeight.Bold, color = BabyBlueDark)
                        Text(selectedUniversity.country, fontSize = 12.sp, color = TextSecondary)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Cari Universitas") },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = BabyBlueDark) },
            trailingIcon = {
                if (isLoading) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = BabyBlueDark)
            },
            placeholder = { Text("Ketik min. 2 huruf...", color = TextMuted) },
            singleLine = true,
            colors = onboardingFieldColors(),
            shape = RoundedCornerShape(14.dp)
        )

        if (error.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(error, fontSize = 12.sp, color = ErrorRose, textAlign = TextAlign.Center)
        }

        if (results.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 280.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(White)
                    .border(1.dp, BabyBlue, RoundedCornerShape(12.dp))
            ) {
                LazyColumn {
                    items(results) { university ->
                        ListItem(
                            headlineContent = {
                                Text(university.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            },
                            supportingContent = {
                                Text(university.country, fontSize = 12.sp, color = TextSecondary)
                            },
                            leadingContent = {
                                Box(
                                    modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp))
                                        .background(BabyBlue.copy(alpha = 0.3f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        university.countryCode.take(2),
                                        fontWeight = FontWeight.Bold, fontSize = 11.sp, color = BabyBlueDark
                                    )
                                }
                            },
                            modifier = Modifier.clickable { onSelect(university) }
                        )
                        if (results.indexOf(university) < results.size - 1) {
                            Divider(color = BabyBlue.copy(alpha = 0.3f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepMajor(
    searchQuery: String,
    majors: List<Major>,
    selectedMajor: Major?,
    isLoading: Boolean,
    selectedUniversity: String,
    onQueryChange: (String) -> Unit,
    onSelect: (Major) -> Unit
) {
    StepContainer(
        emoji = Icons.Default.LocalLibrary,
        emojiTint = SoftPinkDark,
        title = "Pilih Jurusan Targetmu!",
        subtitle = if (selectedUniversity.isNotEmpty()) "Jurusan di $selectedUniversity" else "Pilih jurusan impianmu"
    ) {
        if (isLoading) {
            Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = SoftPinkDark)
                    Spacer(Modifier.height(8.dp))
                    Text("Memuat daftar jurusan...", fontSize = 13.sp, color = TextSecondary)
                }
            }
        } else {
            if (selectedMajor != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SoftPink.copy(alpha = 0.2f))
                        .border(1.dp, SoftPinkDark, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, null, tint = SoftPinkDark)
                        Spacer(Modifier.width(8.dp))
                        Text(selectedMajor.name, fontWeight = FontWeight.Bold, color = SoftPinkDark)
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Cari Jurusan") },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = SoftPinkDark) },
                placeholder = { Text("Ketik nama jurusan...", color = TextMuted) },
                singleLine = true,
                colors = onboardingFieldColors(SoftPinkDark, SoftPink),
                shape = RoundedCornerShape(14.dp)
            )

            val filteredMajors = if (searchQuery.isEmpty()) majors
            else majors.filter { it.name.contains(searchQuery, ignoreCase = true) }

            if (filteredMajors.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(White)
                        .border(1.dp, SoftPink, RoundedCornerShape(12.dp))
                ) {
                    LazyColumn {
                        items(filteredMajors) { major ->
                            ListItem(
                                headlineContent = {
                                    Text(major.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                },
                                leadingContent = {
                                    Icon(Icons.Default.Book, null, tint = SoftPinkDark, modifier = Modifier.size(20.dp))
                                },
                                modifier = Modifier.clickable { onSelect(major) },
                                colors = ListItemDefaults.colors(
                                    containerColor = if (selectedMajor?.id == major.id) SoftPink.copy(alpha = 0.15f) else Color.Transparent
                                )
                            )
                            if (filteredMajors.indexOf(major) < filteredMajors.size - 1) {
                                Divider(color = SoftPink.copy(alpha = 0.3f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepTutorial() {
    val features = listOf(
        TutorialFeature(
            Icons.Default.Home,
            BabyBlueDark,
            "Beranda",
            "Dashboard dengan motivasi harian, ringkasan nilai, dan catatan cepat"
        ),
        TutorialFeature(
            Icons.Default.MenuBook,
            SoftPinkDark,
            "Tracker Akademik",
            "Catat nilai per semester, kelola tugas, dan export laporan PDF/Excel"
        ),
        TutorialFeature(
            Icons.Default.CalendarMonth,
            LavenderDark,
            "Jadwal & Alarm",
            "Atur jadwal belajar dengan sistem Strict Alarm untuk menjaga konsistensi"
        ),
        TutorialFeature(
            Icons.Default.AutoAwesome,
            Color(0xFF16A34A),
            "AI Tutor Kiku",
            "Chat dengan AI untuk bantu belajar, tanya UTBK, beasiswa, dan lebih banyak lagi"
        ),
        TutorialFeature(
            Icons.Default.Person,
            PeachDark,
            "Profil",
            "Lihat dan edit data pribadimu, target kampus, dan jurusan impian"
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(BabyBlue, SoftPink)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Celebration, null, tint = White, modifier = Modifier.size(44.dp))
        }
        Spacer(Modifier.height(12.dp))
        Text("Yuk, Kenali TargetKu!", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary, textAlign = TextAlign.Center)
        Text("Fitur-fitur yang akan membantumu meraih kampus impian", fontSize = 13.sp, color = TextSecondary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(20.dp))

        features.forEach { feature ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(feature.color.copy(alpha = 0.08f))
                    .border(1.dp, feature.color.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(feature.color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(feature.icon, null, tint = feature.color, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(feature.title, fontWeight = FontWeight.Bold, color = feature.color, fontSize = 15.sp)
                    Text(feature.description, fontSize = 12.sp, color = TextSecondary, lineHeight = 16.sp)
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(
            "Semua data tersimpan di perangkatmu secara aman",
            fontSize = 11.sp, color = TextMuted, textAlign = TextAlign.Center
        )
    }
}

data class TutorialFeature(
    val icon: ImageVector,
    val color: Color,
    val title: String,
    val description: String
)

@Composable
private fun onboardingFieldColors(
    focusedColor: Color = BabyBlueDark,
    unfocusedColor: Color = BabyBlue
) = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = focusedColor,
    unfocusedBorderColor = unfocusedColor,
    focusedLabelColor = focusedColor,
    focusedContainerColor = White,
    unfocusedContainerColor = OffWhite
)
