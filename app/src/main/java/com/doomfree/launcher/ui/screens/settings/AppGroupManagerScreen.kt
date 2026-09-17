package com.doomfree.launcher.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.doomfree.launcher.data.InstalledApp
import com.doomfree.launcher.ui.components.AppIcon
import com.doomfree.launcher.ui.theme.CardShape

private enum class GroupFilter { ALL, SELECTED }

/** Generic "which apps are in this bucket" screen, reused for Frequent/Essentials/Distractions. */
@Composable
fun AppGroupManagerScreen(
    title: String,
    subtitle: String,
    selectedLabel: String = "Selected",
    installedApps: List<InstalledApp>,
    isSelected: (InstalledApp) -> Boolean,
    onToggle: (InstalledApp) -> Unit,
    onReset: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(GroupFilter.ALL) }
    val sorted = remember(installedApps) { installedApps.sortedBy { it.label.lowercase() } }
    val selectedCount = remember(installedApps) { installedApps.count(isSelected) }

    val filtered = sorted
        .let { list -> if (filter == GroupFilter.SELECTED) list.filter(isSelected) else list }
        .let { list -> if (query.isBlank()) list else list.filter { it.label.contains(query, ignoreCase = true) } }

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding().padding(top = 16.dp)) {
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterPill(
                        label = "All",
                        selected = filter == GroupFilter.ALL,
                        onClick = { filter = GroupFilter.ALL },
                    )
                    FilterPill(
                        label = "$selectedLabel ($selectedCount)",
                        selected = filter == GroupFilter.SELECTED,
                        onClick = { filter = GroupFilter.SELECTED },
                    )
                }
                TextButton(onClick = onReset, enabled = selectedCount > 0) {
                    Icon(
                        Icons.Filled.RestartAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = "Reset",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp),
                    )
                }
            }

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search apps") },
                singleLine = true,
                shape = CardShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
            )
        }

        LazyColumn(modifier = Modifier.fillMaxSize().padding(top = 8.dp)) {
            items(filtered, key = { it.packageName }) { app ->
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggle(app) }
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AppIcon(icon = app.icon, modifier = Modifier.size(36.dp))
                        Text(
                            text = app.label,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.weight(1f).padding(start = 16.dp),
                        )
                        if (isSelected(app)) {
                            Icon(Icons.Filled.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.surface)
                }
            }
        }
    }
}

@Composable
private fun FilterPill(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
        )
    }
}
