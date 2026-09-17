package com.doomfree.launcher.onboarding

import android.app.role.RoleManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.doomfree.launcher.data.SettingsDataStore

private data class OnboardingStep(
    val title: String,
    val why: String,
    val isGranted: (android.content.Context) -> Boolean,
    val launch: (android.content.Context) -> Intent,
    val skippable: Boolean,
)

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val settings = remember { SettingsDataStore(context) }

    val steps = remember {
        listOf(
            OnboardingStep(
                title = "Set as your default launcher",
                why = "Doomfree replaces your home screen so it can show the 3-panel layout.",
                isGranted = { PermissionState.isDefaultLauncher(it) },
                launch = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        it.getSystemService(RoleManager::class.java)
                            .createRequestRoleIntent(RoleManager.ROLE_HOME)
                    } else {
                        Intent(Settings.ACTION_HOME_SETTINGS)
                    }
                },
                skippable = false,
            ),
            OnboardingStep(
                title = "Turn on the Accessibility Service",
                why = "This is what lets Doomfree block a Distraction app even when it's opened from outside the launcher.",
                isGranted = { PermissionState.isAccessibilityServiceEnabled(it) },
                launch = { Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS) },
                skippable = true,
            ),
            OnboardingStep(
                title = "Allow \"Draw over other apps\"",
                why = "Needed so the blocked-app popup can appear on top of the app that just opened.",
                isGranted = { PermissionState.canDrawOverlays(it) },
                launch = {
                    Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${it.packageName}"),
                    )
                },
                skippable = true,
            ),
        )
    }

    var stepIndex by remember { mutableIntStateOf(0) }
    var refreshTick by remember { mutableStateOf(0) }

    DisposableRefreshOnResume(lifecycleOwner) { refreshTick++ }

    val current = steps.getOrNull(stepIndex)
    if (current == null) {
        LaunchedEffect(Unit) {
            settings.setOnboardingComplete(true)
            onComplete()
        }
        return
    }

    val granted = remember(refreshTick, stepIndex) { current.isGranted(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Step ${stepIndex + 1} of ${steps.size}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = current.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
        )
        Text(
            text = current.why,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Column(modifier = Modifier.padding(top = 32.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (granted) {
                Button(
                    onClick = { stepIndex++ },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Continue") }
            } else {
                Button(
                    onClick = { context.startActivity(current.launch(context)) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Grant permission") }

                if (current.skippable) {
                    TextButton(
                        onClick = { stepIndex++ },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Skip for now") }
                }
            }
        }
    }
}

@Composable
private fun DisposableRefreshOnResume(
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    onResume: () -> Unit,
) {
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) onResume()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}
