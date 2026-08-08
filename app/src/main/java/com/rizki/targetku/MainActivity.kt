package com.rizki.targetku

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rizki.targetku.data.PreferencesManager
import com.rizki.targetku.ui.components.BottomNavItem
import com.rizki.targetku.ui.components.TargetKuBottomNav
import com.rizki.targetku.ui.components.bottomNavItems
import com.rizki.targetku.ui.screens.*
import com.rizki.targetku.ui.theme.TargetKuTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Capture crash reason and restart app showing the error
        val crashMessage = intent?.getStringExtra("crash_message")

        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            val msg = buildString {
                appendLine("CRASH: ${throwable::class.java.simpleName}")
                appendLine(throwable.message)
                appendLine()
                throwable.stackTrace.take(10).forEach { appendLine(it.toString()) }
            }
            val restart = Intent(this, MainActivity::class.java).apply {
                putExtra("crash_message", msg)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            startActivity(restart)
            android.os.Process.killProcess(android.os.Process.myPid())
        }

        enableEdgeToEdge()
        setContent {
            TargetKuTheme {
                if (crashMessage != null) {
                    CrashScreen(crashMessage)
                } else {
                    TargetKuApp()
                }
            }
        }
    }
}

// Navigation routes
object Routes {
    const val LOGIN = "login"
    const val ONBOARDING = "onboarding"
    const val MAIN = "main"
    const val HOME = "home"
    const val ACADEMIC = "academic"
    const val SCHEDULE = "schedule"
    const val AI_TUTOR = "ai_tutor"
    const val PROFILE = "profile"
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun TargetKuApp() {
    val context = LocalContext.current
    val prefsManager = remember { PreferencesManager(context) }

    // Determine start destination
    val startDestination = when {
        !prefsManager.isLoggedIn -> Routes.LOGIN
        !prefsManager.isOnboarded -> Routes.ONBOARDING
        else -> Routes.MAIN
    }

    val rootNavController = rememberNavController()

    NavHost(
        navController = rootNavController,
        startDestination = startDestination,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(350, easing = EaseInOutCubic)
            ) + fadeIn(tween(350))
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(350, easing = EaseInOutCubic)
            ) + fadeOut(tween(350))
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(350, easing = EaseInOutCubic)
            ) + fadeIn(tween(350))
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(350, easing = EaseInOutCubic)
            ) + fadeOut(tween(350))
        }
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = { isFirstTime ->
                    if (isFirstTime) {
                        rootNavController.navigate(Routes.ONBOARDING) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    } else {
                        rootNavController.navigate(Routes.MAIN) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onOnboardingComplete = {
                    rootNavController.navigate(Routes.MAIN) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.MAIN) {
            MainScreen(
                onLogout = {
                    rootNavController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.MAIN) { inclusive = true }
                    }
                },
                onEditProfile = {
                    rootNavController.navigate(Routes.ONBOARDING)
                }
            )
        }
    }
}

@Composable
fun MainScreen(
    onLogout: () -> Unit,
    onEditProfile: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Routes.HOME

    Scaffold(
        bottomBar = {
            TargetKuBottomNav(
                currentRoute = currentRoute,
                onNavItemClick = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
        ) {
            NavHost(
                navController = navController,
                startDestination = Routes.HOME,
                enterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90))
                },
                exitTransition = {
                    fadeOut(animationSpec = tween(90))
                },
                popEnterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90))
                },
                popExitTransition = {
                    fadeOut(animationSpec = tween(90))
                }
            ) {
                composable(Routes.HOME) { HomeScreen() }
                composable(Routes.ACADEMIC) { AcademicScreen() }
                composable(Routes.SCHEDULE) { ScheduleScreen() }
                composable(Routes.AI_TUTOR) { AiTutorScreen() }
                composable(Routes.PROFILE) {
                    ProfileScreen(
                        onEditProfile = onEditProfile,
                        onLogout = onLogout
                    )
                }
            }
        }
    }
}

@Composable
fun CrashScreen(message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A2E))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(60.dp))
        Text(
            "💥 App Crash - Error Info",
            color = Color(0xFFFF6B6B),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Screenshot layar ini dan kirimkan ke developer!",
            color = Color.White,
            fontSize = 14.sp
        )
        Spacer(Modifier.height(16.dp))
        Text(
            message,
            color = Color(0xFF90EE90),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}
