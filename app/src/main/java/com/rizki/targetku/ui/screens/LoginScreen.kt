package com.rizki.targetku.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.app.Application
import androidx.compose.ui.platform.LocalContext
import com.rizki.targetku.viewmodel.AuthState
import com.rizki.targetku.viewmodel.AuthViewModel
import com.rizki.targetku.viewmodel.TargetKuViewModelFactory
import com.rizki.targetku.ui.theme.*

@Composable
fun LoginScreen(
    onLoginSuccess: (isFirstTime: Boolean) -> Unit,
    viewModel: AuthViewModel = viewModel(
        factory = TargetKuViewModelFactory(LocalContext.current.applicationContext as Application)
    )
) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val username by viewModel.usernameInput.collectAsStateWithLifecycle()
    val password by viewModel.passwordInput.collectAsStateWithLifecycle()
    val passwordVisible by viewModel.passwordVisible.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    // Floating animation for logo
    val infiniteTransition = rememberInfiniteTransition(label = "logo_float")
    val logoOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -12f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_float"
    )
    val logoRotate by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_rotate"
    )

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onLoginSuccess((authState as AuthState.Success).isFirstTime)
            viewModel.resetState()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(GradientStart, GradientEnd),
                    startY = 0f, endY = Float.POSITIVE_INFINITY
                )
            )
            .imePadding()
    ) {
        // Decorative circles
        DecorativeCircle(
            modifier = Modifier.align(Alignment.TopStart).offset(x = (-40).dp, y = (-40).dp),
            color = BabyBlue.copy(alpha = 0.4f), size = 180.dp
        )
        DecorativeCircle(
            modifier = Modifier.align(Alignment.TopEnd).offset(x = 40.dp, y = (-20).dp),
            color = SoftPink.copy(alpha = 0.3f), size = 140.dp
        )
        DecorativeCircle(
            modifier = Modifier.align(Alignment.BottomEnd).offset(x = 30.dp, y = 40.dp),
            color = Lavender.copy(alpha = 0.35f), size = 160.dp
        )
        DecorativeCircle(
            modifier = Modifier.align(Alignment.BottomStart).offset(x = (-30).dp, y = 30.dp),
            color = Mint.copy(alpha = 0.3f), size = 120.dp
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.height(60.dp))

            // Logo
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(120.dp)
                    .offset(y = logoOffset.dp)
                    .rotate(logoRotate)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(BabyBlueLight, BabyBlue)
                        )
                    )
                    .border(3.dp, White, RoundedCornerShape(28.dp))
            ) {
                LogoContent()
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = "TargetKu",
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = BabyBlueDark
            )
            Text(
                text = "Raih Kampus Impianmu!",
                fontSize = 15.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(40.dp))

            // Login Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(White.copy(alpha = 0.85f))
                    .border(1.5.dp, BabyBlue.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                    .padding(24.dp)
            ) {
                Column {
                    Text(
                        text = "Selamat Datang Kembali!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Masuk untuk lanjutkan perjalanan belajarmu",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )

                    Spacer(Modifier.height(20.dp))

                    // Username Field
                    OutlinedTextField(
                        value = username,
                        onValueChange = viewModel::onUsernameChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Username") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = BabyBlueDark
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BabyBlueDark,
                            unfocusedBorderColor = BabyBlue,
                            focusedLabelColor = BabyBlueDark,
                            focusedContainerColor = BabyBlueSurface,
                            unfocusedContainerColor = OffWhite
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(Modifier.height(12.dp))

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = viewModel::onPasswordChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Password") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = SoftPinkDark
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = viewModel::togglePasswordVisibility) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (passwordVisible) "Sembunyikan" else "Tampilkan",
                                    tint = TextSecondary
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                viewModel.login()
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SoftPinkDark,
                            unfocusedBorderColor = SoftPink,
                            focusedLabelColor = SoftPinkDark,
                            focusedContainerColor = SoftPinkSurface,
                            unfocusedContainerColor = OffWhite
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Error message
                    AnimatedVisibility(
                        visible = authState is AuthState.Error,
                        enter = slideInVertically(initialOffsetY = { -20 }) + fadeIn(),
                        exit = slideOutVertically() + fadeOut()
                    ) {
                        Spacer(Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(ErrorRose.copy(alpha = 0.2f))
                                .border(1.dp, ErrorRose, RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = null,
                                    tint = Color(0xFFBE123C),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = (authState as? AuthState.Error)?.message ?: "",
                                    fontSize = 13.sp,
                                    color = Color(0xFFBE123C),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // Login Button
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.login()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        enabled = authState !is AuthState.Loading,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BabyBlueDark
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        if (authState is AuthState.Loading) {
                            CircularProgressIndicator(
                                color = White,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Login,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Masuk Sekarang",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Demo hint
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Lavender.copy(alpha = 0.2f))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "Demo: rizkibismillahsnu / 130310",
                            fontSize = 12.sp,
                            color = LavenderDark,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            Text(
                text = "TargetKu - Planner Studi & Beasiswa",
                fontSize = 12.sp,
                color = TextMuted,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun LogoContent() {
    // Book with target arrow icon built from composables
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.MenuBook,
                contentDescription = "TargetKu Logo",
                tint = White,
                modifier = Modifier.size(40.dp)
            )
            Icon(
                imageVector = Icons.Default.GpsFixed,
                contentDescription = null,
                tint = SoftPink,
                modifier = Modifier.size(24.dp).offset(x = 16.dp, y = (-8).dp)
            )
        }
    }
}

@Composable
private fun DecorativeCircle(
    modifier: Modifier = Modifier,
    color: Color,
    size: androidx.compose.ui.unit.Dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
    )
}
