package com.drcmind.cleaapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.runtime.NavEntry
import com.drcmind.cleaapp.ui.auth.login.LoginScreen
import com.drcmind.cleaapp.ui.auth.login.SignInScreen
import com.drcmind.cleaapp.ui.auth.onboarding.OnboardingScreen
import com.drcmind.cleaapp.ui.auth.setup.InitialSetupScreen
import com.drcmind.cleaapp.ui.auth.splash.SplashScreen
import com.drcmind.cleaapp.ui.home.MainScreen
import com.drcmind.cleaapp.ui.profile.ProfileScreen

@Composable
fun AppNavigation() {
    val backStack = remember { mutableStateListOf<AppDestination>(AppDestination.Splash) }
    
    NavDisplay(
        backStack = backStack,
    ) { destination ->
        when (destination) {
            is AppDestination.Splash -> NavEntry(destination) {
                SplashScreen(
                    onNavigate = { dest ->
                        backStack.clear()
                        backStack.add(dest)
                    }
                )
            }
            is AppDestination.Onboarding -> NavEntry(destination) {
                OnboardingScreen(
                    onFinish = {
                        backStack.clear()
                        backStack.add(AppDestination.Login)
                    }
                )
            }
            is AppDestination.Login -> NavEntry(destination) {
                LoginScreen(
                    onLoginSuccess = {
                        backStack.clear()
                        backStack.add(AppDestination.Home)
                    },
                    onSignInNavigation = {
                        backStack.add(AppDestination.SignIn)
                    }
                )
            }
            is AppDestination.SignIn -> NavEntry(destination) {
                SignInScreen(
                    onBackToLogin = {
                        backStack.removeLast()
                    }
                )
            }
            is AppDestination.InitialSetup -> NavEntry(destination) {
                InitialSetupScreen(
                    onSetupCompleted = {
                        backStack.clear()
                        backStack.add(AppDestination.Home)
                    }
                )
            }
            is AppDestination.Home -> NavEntry(destination) {
                MainScreen(
                    onNavigateToProfile = {
                        backStack.add(AppDestination.Profile)
                    },
                    onLogoutSuccess = {
                        backStack.clear()
                        backStack.add(AppDestination.Login)
                    }
                )
            }
            is AppDestination.Profile -> NavEntry(destination) {
                ProfileScreen(
                    onLogoutSuccess = {
                        backStack.clear()
                        backStack.add(AppDestination.Login)
                    },
                    onBack = {
                        backStack.removeLast()
                    }
                )
            }
        }
    }
}
