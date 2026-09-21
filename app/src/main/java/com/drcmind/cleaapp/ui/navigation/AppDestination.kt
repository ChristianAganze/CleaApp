package com.drcmind.cleaapp.ui.navigation

import kotlinx.serialization.Serializable

sealed interface AppDestination {
    @Serializable
    data object Splash : AppDestination
    @Serializable
    data object Onboarding : AppDestination
    @Serializable
    data object Login : AppDestination
    @Serializable
    data object SignIn : AppDestination
    @Serializable
    data object InitialSetup : AppDestination
    @Serializable
    data object Home : AppDestination
    @Serializable
    data object Profile : AppDestination
}
