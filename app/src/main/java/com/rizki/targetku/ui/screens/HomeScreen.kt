package com.rizki.targetku.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rizki.targetku.ui.components.GlassCard
import com.rizki.targetku.ui.components.GradientCard
import com.rizki.targetku.viewmodel.HomeViewModel
import com.rizki.targetku.ui.theme.*
import java.text.DecimalFormat

import androidx.lifecycle.ViewModelProvider
import android.app.Application
import com.rizki.targetku.viewmodel.TargetKuViewModelFactory

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(
        factory = TargetKuViewModelFactory(LocalContext.current.applicationContext as Application)
    )
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) { viewModel.loadData() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(GradientStart, OffWhite)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 16.dp)
        ) {
            // Welcome Banner
            WelcomeBanner(
                name = state.userName,
                targetCampus = state.targetCampus
            )

            // Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Daily Quote Widget
                DailyQuoteCard(
                    quote = state.dailyQuote,
                    author = state.dailyQuoteAuthor
                )

                // Summary Cards
                Text(
                    "Ringkasan Kamu",
                    fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SummaryCard(
                        title = "Rata-rata Nilai",
                        value = if (state.averageGrade > 0) DecimalFormat("#.#").format(state.averageGrade) else "-",
                        icon = Icons.Default.Star,
                        iconColor = WarningAmber,
                        cardColors = listOf(Color(0xFFFFFBE6), Color(0xFFFFF0B3)),
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCard(
                        title = "Tugas Selesai",
                        value = "${state.tasksDone}",
                        icon = Icons.Default.TaskAlt,
                        iconColor = SuccessGreen,
                        cardColors = listOf(Color(0xFFE6FFF4), Color(0xFFB3FFE0)),
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCard(
                        title = "Tugas Pending",
                        value = "${state.tasksPending}",
                        icon = Icons.Default.Pending,
                        iconColor = SoftPinkDark,
                        cardColors = listOf(SoftPinkSurface, SoftPinkLight),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Quick Note Widget
                Text(
                    "Catatan Harian",
                    fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary
                )
                QuickNoteWidget(
                    note = state.dailyNote,
                    onNoteChange = viewModel::onNoteChange,
                    onSave = viewModel::saveNote,
                    isSaving = state.isSavingNote,
                    saveSuccess = state.noteSavedSuccess
                )

                // Motivational footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Keep going! Kampus impianmu menunggumu!",
                        fontSize = 12.sp, color = TextMuted, textAlign = TextAlign.Center
                    )
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun WelcomeBanner(name: String, targetCampus: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "banner_anim")
    val shimmer by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(BabyBlueDark, Color(0xFF7AB8E8), SoftPinkDark),
                    start = androidx.compose.ui.geometry.Offset(shimmer * 1000f, 0f),
                    end = androidx.compose.ui.geometry.Offset((shimmer * 1000f) + 500f, 500f)
                )
            )
    ) {
        // Decorative shapes
        Box(
            modifier = Modifier
                .size(120.dp)
                .offset(x = (-30).dp, y = (-30).dp)
                .clip(CircleShape)
                .background(White.copy(alpha = 0.1f))
                .align(Alignment.TopEnd)
        )
        Box(
            modifier = Modifier
                .size(80.dp)
                .offset(x = 20.dp, y = 20.dp)
                .clip(CircleShape)
                .background(White.copy(alpha = 0.08f))
                .align(Alignment.BottomStart)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Hai, $name!",
                fontSize = 14.sp, color = White.copy(alpha = 0.9f), fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Semangat mengejar",
                fontSize = 16.sp, color = White, fontWeight = FontWeight.Bold
            )
            Text(
                targetCampus,
                fontSize = 20.sp, color = White, fontWeight = FontWeight.ExtraBold,
                maxLines = 2
            )
            Spacer(Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.Default.Rocket, null, tint = White.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                Text("Impianmu ada di sini!", fontSize = 13.sp, color = White.copy(alpha = 0.85f))
            }
        }
    }
}

@Composable
private fun DailyQuoteCard(quote: String, author: String) {
    GradientCard(
        gradientColors = listOf(LavenderLight, SoftPinkLight),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.FormatQuote,
                contentDescription = null,
                tint = LavenderDark,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    quote,
                    fontSize = 13.sp,
                    color = TextOnPastel,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "- $author",
                    fontSize = 11.sp,
                    color = LavenderDark,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    cardColors: List<Color>,
    modifier: Modifier = Modifier
) {
    GradientCard(
        gradientColors = cardColors,
        modifier = modifier,
        cornerRadius = 14.dp,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(6.dp))
            Text(
                value,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
            Text(
                title,
                fontSize = 10.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
private fun QuickNoteWidget(
    note: String,
    onNoteChange: (String) -> Unit,
    onSave: () -> Unit,
    isSaving: Boolean,
    saveSuccess: Boolean
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = White.copy(alpha = 0.9f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Edit, null, tint = BabyBlueDark, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Catatan & Refleksi Hari Ini", fontWeight = FontWeight.Bold, color = TextPrimary)
            }

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = note,
                onValueChange = onNoteChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp),
                placeholder = {
                    Text(
                        "Tulis catatan atau refleksi harianmu di sini...\n\nApa yang sudah kamu pelajari hari ini? Apa yang ingin kamu capai besok?",
                        color = TextMuted, fontSize = 13.sp, lineHeight = 20.sp
                    )
                },
                maxLines = 8,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BabyBlueDark,
                    unfocusedBorderColor = BabyBlue.copy(alpha = 0.5f),
                    focusedContainerColor = BabyBlueSurface,
                    unfocusedContainerColor = OffWhite
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedVisibility(
                    visible = saveSuccess,
                    enter = fadeIn() + slideInHorizontally(),
                    exit = fadeOut()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Catatan tersimpan!", fontSize = 12.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.Medium)
                    }
                }

                if (!saveSuccess) {
                    Text("${note.length} karakter", fontSize = 11.sp, color = TextMuted)
                }

                Button(
                    onClick = onSave,
                    enabled = note.isNotBlank() && !isSaving,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BabyBlueDark),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(color = White, modifier = Modifier.size(16.dp))
                    } else {
                        Icon(Icons.Default.Save, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Simpan", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
