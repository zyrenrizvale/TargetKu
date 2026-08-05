package com.rizki.targetku.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.accompanist.permissions.*
import com.rizki.targetku.ui.theme.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun StrictAlarmDialog(
    skipReason: String,
    skipPhotoPath: String,
    onReasonChange: (String) -> Unit,
    onPhotoCaptured: (String) -> Unit,
    onConfirmSkip: () -> Unit,
    onDismiss: () -> Unit
) {
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)
    var showCamera by remember { mutableStateOf(false) }
    var photoTaken by remember { mutableStateOf(false) }
    var warningVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(300)
        warningVisible = true
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(White, SoftPinkSurface)
                        )
                    )
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // AI Warning Header
                AnimatedVisibility(
                    visible = warningVisible,
                    enter = slideInVertically(initialOffsetY = { -50 }) + fadeIn()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(SoftPink, ErrorRose)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = White,
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = "STOP! Verifikasi Diperlukan",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF991B1B)
                        )

                        Spacer(Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(ErrorRose.copy(alpha = 0.2f))
                                .border(1.dp, ErrorRose, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "AI mendeteksi kamu mencoba bolos! Bukti foto disimpan. Kamu harus memberikan alasan yang valid dan foto dirimu untuk melanjutkan.",
                                fontSize = 13.sp,
                                color = Color(0xFF991B1B),
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))
                Divider(color = SoftPink.copy(alpha = 0.4f))
                Spacer(Modifier.height(16.dp))

                // Reason Input
                Text(
                    text = "Apa alasanmu melewatkan sesi belajar?",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = skipReason,
                    onValueChange = onReasonChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    placeholder = {
                        Text("Ketik alasanmu di sini (wajib diisi)...", color = TextMuted, fontSize = 13.sp)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SoftPinkDark,
                        unfocusedBorderColor = SoftPink,
                        focusedContainerColor = White,
                        unfocusedContainerColor = SoftPinkSurface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 4
                )

                Spacer(Modifier.height(16.dp))

                // Camera Section
                Text(
                    text = "Foto Bukti (wajib)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                if (!photoTaken) {
                    if (showCamera) {
                        CameraCapture(
                            onPhotoCaptured = { path ->
                                onPhotoCaptured(path)
                                photoTaken = true
                                showCamera = false
                            },
                            onClose = { showCamera = false }
                        )
                    } else {
                        Button(
                            onClick = {
                                if (cameraPermissionState.status.isGranted) {
                                    showCamera = true
                                } else {
                                    cameraPermissionState.launchPermissionRequest()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BabyBlueDark
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Ambil Foto Bukti", fontWeight = FontWeight.Bold)
                        }

                        if (!cameraPermissionState.status.isGranted) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Izin kamera diperlukan untuk mengambil foto",
                                fontSize = 11.sp, color = TextMuted,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    // Photo taken confirmation
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Mint.copy(alpha = 0.3f))
                            .border(1.dp, Mint, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF16A34A)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Foto berhasil diambil dan disimpan!",
                                color = Color(0xFF16A34A),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedButtonDefaults.colors(
                            contentColor = BabyBlueDark
                        )
                    ) {
                        Text("Lanjut Belajar!", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (skipReason.isNotBlank() && (photoTaken || skipPhotoPath.isNotEmpty())) {
                                onConfirmSkip()
                            }
                        },
                        enabled = skipReason.isNotBlank() && (photoTaken || skipPhotoPath.isNotEmpty()),
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ErrorRose,
                            disabledContainerColor = ErrorRose.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Konfirmasi Lewati", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Kamu yakin mau lewati? Ingat, kampus impianmu tidak menunggu!",
                    fontSize = 11.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
