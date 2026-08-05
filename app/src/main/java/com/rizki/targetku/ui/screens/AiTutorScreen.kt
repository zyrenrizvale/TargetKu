package com.rizki.targetku.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rizki.targetku.data.models.ChatMessage
import com.rizki.targetku.ui.components.GlassCard
import com.rizki.targetku.viewmodel.AiTutorViewModel
import com.rizki.targetku.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AiTutorScreen(
    viewModel: AiTutorViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFE8F8F0), OffWhite)
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            AiTutorHeader(onClear = viewModel::clearChat, onApiKey = viewModel::showApiKeyDialog)

            // Chat Messages
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.messages, key = { it.id }) { message ->
                    AnimatedVisibility(
                        visible = true,
                        enter = slideInVertically(initialOffsetY = { 30 }) + fadeIn()
                    ) {
                        if (message.isLoading) {
                            TypingIndicator()
                        } else {
                            ChatBubble(message = message)
                        }
                    }
                }
            }

            // Input Bar
            ChatInputBar(
                text = state.inputText,
                onTextChange = viewModel::onInputChange,
                onSend = {
                    focusManager.clearFocus()
                    viewModel.sendMessage()
                },
                isTyping = state.isTyping
            )
        }

        // API Key Dialog
        if (state.showApiKeyDialog) {
            ApiKeyDialog(
                apiKeyInput = state.apiKeyInput,
                onKeyChange = viewModel::onApiKeyInputChange,
                onSave = viewModel::saveApiKey,
                onDismiss = viewModel::hideApiKeyDialog
            )
        }
    }
}

@Composable
private fun AiTutorHeader(onClear: () -> Unit, onApiKey: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF16A34A), Color(0xFF065F46))
                )
            )
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Kiku Avatar
            val pulseAnim = rememberInfiniteTransition(label = "kiku_pulse")
            val pulse by pulseAnim.animateFloat(
                1f, 1.1f,
                infiniteRepeatable(tween(1500, easing = EaseInOutSine), RepeatMode.Reverse),
                label = "pulse"
            )
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF4ADE80), Color(0xFF16A34A))
                        )
                    )
                    .border(2.dp, White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.SmartToy, null, tint = White, modifier = Modifier.size(28.dp))
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text("Kiku AI Study Buddy", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = White)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF4ADE80)))
                    Spacer(Modifier.width(4.dp))
                    Text("Online - Siap membantu!", fontSize = 12.sp, color = White.copy(alpha = 0.8f))
                }
            }

            IconButton(onClick = onApiKey) {
                Icon(Icons.Default.Key, null, tint = White.copy(alpha = 0.8f))
            }
            IconButton(onClick = onClear) {
                Icon(Icons.Default.DeleteSweep, null, tint = White.copy(alpha = 0.8f))
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    val isUser = message.isFromUser

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF4ADE80), Color(0xFF16A34A))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.SmartToy, null, tint = White, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(6.dp))
        }

        Column(
            modifier = Modifier.widthIn(max = 280.dp),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = if (isUser) 16.dp else 4.dp,
                            topEnd = if (isUser) 4.dp else 16.dp,
                            bottomStart = 16.dp,
                            bottomEnd = 16.dp
                        )
                    )
                    .background(
                        brush = if (isUser) Brush.linearGradient(
                            colors = listOf(BabyBlueDark, Color(0xFF2563EB))
                        ) else Brush.linearGradient(
                            colors = listOf(White, OffWhite)
                        )
                    )
                    .border(
                        1.dp,
                        if (isUser) Color.Transparent else Color(0xFFE2E8F0),
                        RoundedCornerShape(
                            topStart = if (isUser) 16.dp else 4.dp,
                            topEnd = if (isUser) 4.dp else 16.dp,
                            bottomStart = 16.dp,
                            bottomEnd = 16.dp
                        )
                    )
                    .padding(12.dp)
            ) {
                Text(
                    text = message.content,
                    fontSize = 14.sp,
                    color = if (isUser) White else TextPrimary,
                    lineHeight = 21.sp
                )
            }

            Spacer(Modifier.height(2.dp))
            Text(
                text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp)),
                fontSize = 10.sp,
                color = TextMuted,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        if (isUser) {
            Spacer(Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(BabyBlueDark),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, null, tint = White, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun TypingIndicator() {
    Row(
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF4ADE80), Color(0xFF16A34A))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.SmartToy, null, tint = White, modifier = Modifier.size(18.dp))
        }

        Spacer(Modifier.width(8.dp))

        val infiniteTransition = rememberInfiniteTransition(label = "typing")
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                .background(White)
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                repeat(3) { index ->
                    val dotAnim by infiniteTransition.animateFloat(
                        0f, 1f,
                        infiniteRepeatable(
                            tween(600, delayMillis = index * 200, easing = EaseInOutSine),
                            RepeatMode.Reverse
                        ),
                        label = "dot_$index"
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .offset(y = (-4 * dotAnim).dp)
                            .clip(CircleShape)
                            .background(Color(0xFF16A34A).copy(alpha = 0.5f + 0.5f * dotAnim))
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    isTyping: Boolean
) {
    Column {
        Divider(color = Color(0xFFE2E8F0), thickness = 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(White)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text("Tanyakan sesuatu kepada Kiku...", color = TextMuted, fontSize = 14.sp)
                },
                maxLines = 4,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF16A34A),
                    unfocusedBorderColor = Color(0xFFD1FAE5),
                    focusedContainerColor = Color(0xFFF0FFF4),
                    unfocusedContainerColor = OffWhite
                ),
                shape = RoundedCornerShape(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (text.isNotBlank() && !isTyping)
                            Brush.linearGradient(colors = listOf(Color(0xFF16A34A), Color(0xFF4ADE80)))
                        else Brush.linearGradient(colors = listOf(Color(0xFFD1FAE5), Color(0xFFD1FAE5)))
                    )
                    .clickable(enabled = text.isNotBlank() && !isTyping, onClick = onSend),
                contentAlignment = Alignment.Center
            ) {
                if (isTyping) {
                    CircularProgressIndicator(
                        color = Color(0xFF16A34A),
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Send, "Kirim", tint = if (text.isNotBlank()) White else TextMuted, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun ApiKeyDialog(
    apiKeyInput: String,
    onKeyChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Gemini API Key", fontWeight = FontWeight.ExtraBold) },
        text = {
            Column {
                Text("Masukkan API key Gemini untuk AI yang lebih canggih. Biarkan kosong untuk menggunakan mode demo.", fontSize = 13.sp, color = TextSecondary)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = apiKeyInput,
                    onValueChange = onKeyChange,
                    label = { Text("API Key") },
                    placeholder = { Text("AIza...", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                Text("Dapatkan API key gratis di: aistudio.google.com", fontSize = 11.sp, color = BabyBlueDark)
            }
        },
        confirmButton = {
            Button(onClick = onSave) { Text("Simpan") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Batal") }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

// End of AiTutorScreen.kt
