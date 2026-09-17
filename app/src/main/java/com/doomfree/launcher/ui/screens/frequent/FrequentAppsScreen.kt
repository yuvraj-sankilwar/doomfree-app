package com.doomfree.launcher.ui.screens.frequent

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.doomfree.launcher.data.InstalledApp
import com.doomfree.launcher.ui.MainViewModel
import com.doomfree.launcher.ui.components.AppIcon
import com.doomfree.launcher.ui.theme.CardShape

@Composable
fun FrequentAppsScreen(
    viewModel: MainViewModel,
    onOpenSettings: () -> Unit,
) {
    val frequentEntries by viewModel.frequentApps.collectAsState()
    val installedApps by viewModel.installedApps.collectAsState()
    val frequentApps = remember(frequentEntries, installedApps) {
        val byPackage = installedApps.associateBy { it.packageName }
        frequentEntries.mapNotNull { byPackage[it.packageName] }
    }

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding().padding(16.dp)) {
        Box(modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onOpenSettings, modifier = Modifier.align(Alignment.TopEnd)) {
                Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize().padding(top = 8.dp),
        ) {
            items(frequentApps, key = { it.packageName }) { app ->
                FrequentAppTile(
                    app = app,
                    onClick = { viewModel.launchApp(app.packageName) },
                )
            }
        }
    }
}

@Composable
private fun FrequentAppTile(app: InstalledApp, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .aspectRatio(1f)
            .background(MaterialTheme.colorScheme.surface, CardShape)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f), CardShape)
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        AppIcon(icon = app.icon, modifier = Modifier.size(40.dp))
        Text(
            text = app.label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}
