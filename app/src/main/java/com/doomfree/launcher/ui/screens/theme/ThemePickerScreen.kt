package com.doomfree.launcher.ui.screens.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.doomfree.launcher.data.AccentColor
import com.doomfree.launcher.data.QuickActionSlot
import com.doomfree.launcher.ui.MainViewModel
import com.doomfree.launcher.ui.theme.CardShape

@Composable
fun ThemePickerScreen(viewModel: MainViewModel, onConfigureQuickAction: (QuickActionSlot) -> Unit) {
    val current by viewModel.accentColor.collectAsState()
    val installedApps by viewModel.installedApps.collectAsState()

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding().padding(20.dp)) {
        Text(
            text = "Theme",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(top = 24.dp),
        ) {
            items(AccentColor.entries.toList()) { color ->
                Swatch(
                    color = color,
                    selected = color == current,
                    onClick = { viewModel.setAccentColor(color) },
                )
            }
        }

        Text(
            text = "Quick actions",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 32.dp, bottom = 4.dp),
        )
        Text(
            text = "Which app opens for each shortcut",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, CardShape),
        ) {
            QuickActionSlot.entries.forEachIndexed { index, slot ->
                val packageName by viewModel.quickActionPackage(slot).collectAsState()
                val appLabel = installedApps.find { it.packageName == packageName }?.label ?: "Not set"
                QuickActionRow(
                    label = slot.label,
                    value = appLabel,
                    onClick = { onConfigureQuickAction(slot) },
                )
                if (index != QuickActionSlot.entries.lastIndex) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun QuickActionRow(label: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun Swatch(color: AccentColor, selected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick)
            .padding(4.dp)
            .background(Color(color.argb), CircleShape)
            .then(
                if (selected) {
                    Modifier.border(3.dp, MaterialTheme.colorScheme.onBackground, CircleShape)
                } else {
                    Modifier
                },
            ),
    ) {}
}
