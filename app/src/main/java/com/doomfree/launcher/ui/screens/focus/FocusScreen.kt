package com.doomfree.launcher.ui.screens.focus

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.doomfree.launcher.ui.MainViewModel
import com.doomfree.launcher.ui.theme.CardShape
import com.doomfree.launcher.ui.theme.accentBackdropBrush
import kotlin.math.roundToInt

private data class DurationPreset(val label: String, val millis: Long)

private val PRESETS = listOf(
    DurationPreset("15m", 15 * 60_000L),
    DurationPreset("45m", 45 * 60_000L),
    DurationPreset("3h", 3 * 60 * 60_000L),
)

@Composable
fun FocusScreen(viewModel: MainViewModel, onSessionStarted: () -> Unit, onOpenSettings: () -> Unit) {
    val essentials by viewModel.essentials.collectAsState()
    val distractions by viewModel.distractions.collectAsState()
    val accent by viewModel.accentColor.collectAsState()

    var selectedDuration by remember { mutableStateOf(PRESETS[0].millis) }
    var isCustom by remember { mutableStateOf(false) }
    var showCustomDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(accentBackdropBrush(accent))
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Focus",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth(),
        )

        DurationPicker(
            selectedMillis = selectedDuration,
            isCustom = isCustom,
            onSelect = { selectedDuration = it; isCustom = false },
            onCustomRequested = { showCustomDialog = true },
            modifier = Modifier.padding(top = 32.dp),
        )

        SummaryRow(
            essentialsCount = essentials.size,
            distractionsCount = distractions.size,
            onOpenSettings = onOpenSettings,
            modifier = Modifier.padding(top = 32.dp),
        )

        SlideToStart(
            modifier = Modifier.padding(top = 40.dp),
            onStart = {
                viewModel.startFocusSession(selectedDuration)
                onSessionStarted()
            },
        )
    }

    if (showCustomDialog) {
        CustomDurationDialog(
            onDismiss = { showCustomDialog = false },
            onConfirm = { millis ->
                selectedDuration = millis
                isCustom = true
                showCustomDialog = false
            },
        )
    }
}

@Composable
private fun DurationPicker(
    selectedMillis: Long,
    isCustom: Boolean,
    onSelect: (Long) -> Unit,
    onCustomRequested: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        val days = selectedMillis / 86_400_000
        val hours = (selectedMillis % 86_400_000) / 3_600_000
        val minutes = (selectedMillis % 3_600_000) / 60_000
        val seconds = (selectedMillis % 60_000) / 1000
        Text(
            text = if (days > 0) {
                String.format("%dd %02d:%02d:%02d", days, hours, minutes, seconds)
            } else {
                String.format("%02d:%02d:%02d", hours, minutes, seconds)
            },
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 20.dp),
        ) {
            PRESETS.forEach { preset ->
                val selected = !isCustom && preset.millis == selectedMillis
                DurationPill(
                    label = preset.label,
                    selected = selected,
                    onClick = { onSelect(preset.millis) },
                )
            }
            DurationPill(
                label = "Custom",
                selected = isCustom,
                onClick = onCustomRequested,
            )
        }
    }
}

@Composable
private fun DurationPill(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Text(
            text = label,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
        )
    }
}

@Composable
private fun CustomDurationDialog(onDismiss: () -> Unit, onConfirm: (Long) -> Unit) {
    var days by remember { mutableStateOf("") }
    var hours by remember { mutableStateOf("") }
    var minutes by remember { mutableStateOf("") }
    var seconds by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            IconButton(
                onClick = {
                    val totalMillis = (days.toLongOrNull() ?: 0) * 86_400_000L +
                        (hours.toLongOrNull() ?: 0) * 3_600_000L +
                        (minutes.toLongOrNull() ?: 0) * 60_000L +
                        (seconds.toLongOrNull() ?: 0) * 1000L
                    if (totalMillis > 0) onConfirm(totalMillis)
                },
            ) {
                Icon(Icons.Filled.CheckCircle, contentDescription = "Set", tint = MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            IconButton(onClick = onDismiss) {
                Icon(Icons.Filled.Close, contentDescription = "Cancel", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        title = { Text("Custom duration") },
        text = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DurationField(value = days, onValueChange = { days = it }, label = "Days", modifier = Modifier.weight(1f))
                DurationField(value = hours, onValueChange = { hours = it }, label = "Hrs", modifier = Modifier.weight(1f))
                DurationField(value = minutes, onValueChange = { minutes = it }, label = "Min", modifier = Modifier.weight(1f))
                DurationField(value = seconds, onValueChange = { seconds = it }, label = "Sec", modifier = Modifier.weight(1f))
            }
        },
    )
}

@Composable
private fun DurationField(value: String, onValueChange: (String) -> Unit, label: String, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = { input -> onValueChange(input.filter { it.isDigit() }) },
        label = { Text(label) },
        singleLine = true,
        shape = CardShape,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier,
    )
}

@Composable
private fun SummaryRow(
    essentialsCount: Int,
    distractionsCount: Int,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, CardShape)
            .clickable(onClick = onOpenSettings)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$essentialsCount Essentials · $distractionsCount Distractions",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Manage which apps in Settings",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(
            text = "Everything else stays General — untouched for now.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 12.dp),
        )
    }
}

@Composable
private fun SlideToStart(modifier: Modifier = Modifier, onStart: () -> Unit) {
    var trackWidthPx by remember { mutableFloatStateOf(0f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    val knobSize = 56.dp
    val knobSizePx = with(LocalDensity.current) { knobSize.toPx() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(32.dp))
            .padding(4.dp)
            .onSizeChanged { trackWidthPx = it.width.toFloat() },
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = "Slide to start",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .size(knobSize)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        val max = (trackWidthPx - knobSizePx).coerceAtLeast(0f)
                        offsetX = (offsetX + delta).coerceIn(0f, max)
                    },
                    onDragStopped = {
                        val max = (trackWidthPx - knobSizePx).coerceAtLeast(0f)
                        if (max > 0f && offsetX >= max * 0.7f) {
                            onStart()
                        }
                        offsetX = 0f
                    },
                ),
        ) {
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = "Slide to start",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.fillMaxSize().padding(12.dp),
            )
        }
    }
}
