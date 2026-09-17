package com.doomfree.launcher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.doomfree.launcher.ui.theme.CardShape
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BlockedCard(
    appLabel: String,
    sessionEndsAt: Long,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val timeFormat = remember(sessionEndsAt) {
        SimpleDateFormat("h:mm a", Locale.getDefault())
    }
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface, CardShape)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "$appLabel is blocked",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "Marked as a Distraction — locked until your session ends at ${timeFormat.format(Date(sessionEndsAt))}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        ) {
            Text("Got it")
        }
    }
}
