package com.drcmind.cleaapp.ui.home

import androidx.compose.animation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.drcmind.cleaapp.ui.agenda.AgendaScreen
import com.drcmind.cleaapp.ui.library.LibraryScreen
import com.drcmind.cleaapp.ui.menstrual.MenstrualDashboardScreen
import com.drcmind.cleaapp.ui.profile.ProfileScreen

enum class MainTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
) {
    HOME("Accueil", Icons.Filled.Home, Icons.Outlined.Home, "tab_home"),
    CYCLE("Cycle", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder, "tab_cycle"),
    AGENDA("Agenda", Icons.Filled.EventNote, Icons.Outlined.EventNote, "tab_agenda"),
    LIBRARY("Bibliothèque", Icons.Filled.AutoStories, Icons.Outlined.AutoStories, "tab_library"),
    PROFILE("Profil", Icons.Filled.Person, Icons.Outlined.Person, "tab_profile")
}

@Composable
fun MainScreen(
    onNavigateToProfile: () -> Unit,
    onLogoutSuccess: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(MainTab.HOME) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.testTag("main_bottom_nav_bar")
            ) {
                MainTab.entries.forEach { tab ->
                    val isSelected = selectedTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag(tab.tag)
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selectedTab) {
                MainTab.HOME -> HomeScreen(
                    onNavigateToCycle = { selectedTab = MainTab.CYCLE },
                    onNavigateToAgenda = { selectedTab = MainTab.AGENDA },
                    onNavigateToLibrary = { selectedTab = MainTab.LIBRARY },
                    onNavigateToProfile = { selectedTab = MainTab.PROFILE }
                )
                MainTab.CYCLE -> MenstrualDashboardScreen(
                    onNavigateToProfile = { selectedTab = MainTab.PROFILE }
                )
                MainTab.AGENDA -> AgendaScreen(
                    onNavigateToProfile = { selectedTab = MainTab.PROFILE }
                )
                MainTab.LIBRARY -> LibraryScreen(
                    onNavigateToProfile = { selectedTab = MainTab.PROFILE }
                )
                MainTab.PROFILE -> ProfileScreen(
                    onLogoutSuccess = onLogoutSuccess,
                    onBack = { selectedTab = MainTab.HOME }
                )
            }
        }
    }
}
