package com.rizki.targetku

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
        enableEdgeToEdge()
        setContent {
            TargetKuTheme {
                TargetKuApp()
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
