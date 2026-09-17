package com.doomfree.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.doomfree.launcher.data.SettingsDataStore
import com.doomfree.launcher.onboarding.OnboardingScreen
import com.doomfree.launcher.ui.DoomfreeApp
import com.doomfree.launcher.ui.MainViewModel
import com.doomfree.launcher.ui.theme.DoomfreeTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppRoot(viewModel)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadInstalledApps()
    }
}

@Composable
private fun AppRoot(viewModel: MainViewModel) {
    val context = LocalContext.current
    val settings = remember { SettingsDataStore(context) }
    val onboardingComplete by settings.onboardingComplete.collectAsState(initial = false)

    if (onboardingComplete) {
        DoomfreeApp(viewModel)
    } else {
        val accent by viewModel.accentColor.collectAsState()
        DoomfreeTheme(accent = accent) {
            OnboardingScreen(onComplete = { /* recomposition picks up the flag */ })
        }
    }
}
