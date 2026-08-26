package com.drcmind.cleaapp.ui.home

import androidx.compose.runtime.Composable
import com.drcmind.cleaapp.ui.menstrual.MenstrualDashboardScreen

@Composable
fun MainScreen(
    onNavigateToProfile: () -> Unit
) {
    MenstrualDashboardScreen(onNavigateToProfile = onNavigateToProfile)
}

