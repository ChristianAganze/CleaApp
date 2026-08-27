package com.drcmind.cleaapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.drcmind.cleaapp.data.local.datastore.AuthDataStore
import com.drcmind.cleaapp.ui.navigation.AppNavigation
import com.drcmind.cleaapp.ui.theme.CleaAppTheme
import com.drcmind.cleaapp.ui.theme.ThemeMode
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val authDataStore: AuthDataStore by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by authDataStore.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
            CleaAppTheme(themeMode = themeMode) {
                AppNavigation()
            }
        }
    }
}
