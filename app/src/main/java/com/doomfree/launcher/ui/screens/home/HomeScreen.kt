package com.doomfree.launcher.ui.screens.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils
import com.doomfree.launcher.data.QuickActionSlot
import com.doomfree.launcher.ui.MainViewModel
import com.doomfree.launcher.ui.theme.CardShape
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(viewModel: MainViewModel, onConfigureQuickAction: (QuickActionSlot) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val clockAppPackage by viewModel.quickActionPackage(QuickActionSlot.CLOCK).collectAsState()
    val calendarAppPackage by viewModel.quickActionPackage(QuickActionSlot.CALENDAR).collectAsState()
    val cameraAppPackage by viewModel.quickActionPackage(QuickActionSlot.CAMERA).collectAsState()
    val scannerAppPackage by viewModel.quickActionPackage(QuickActionSlot.SCANNER).collectAsState()
    val phoneAppPackage by viewModel.quickActionPackage(QuickActionSlot.PHONE).collectAsState()

    var now by remember { mutableStateOf(Date()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = Date()
            kotlinx.coroutines.delay(1000)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(homeRadialGradient(scheme)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(top = 20.dp),
        ) {
            SegmentedClockCard(
                now = now,
                scheme = scheme,
                onClockClick = {
                    val pkg = clockAppPackage
                    if (pkg != null) viewModel.launchApp(pkg) else onConfigureQuickAction(QuickActionSlot.CLOCK)
                },
                onClockLongClick = { onConfigureQuickAction(QuickActionSlot.CLOCK) },
                onDateClick = {
                    val pkg = calendarAppPackage
                    if (pkg != null) viewModel.launchApp(pkg) else onConfigureQuickAction(QuickActionSlot.CALENDAR)
                },
                onDateLongClick = { onConfigureQuickAction(QuickActionSlot.CALENDAR) },
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                QuickActionTile(
                    icon = Icons.Filled.CameraAlt,
                    label = "Camera",
                    scheme = scheme,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val pkg = cameraAppPackage
                        if (pkg != null) viewModel.launchApp(pkg) else onConfigureQuickAction(QuickActionSlot.CAMERA)
                    },
                    onLongClick = { onConfigureQuickAction(QuickActionSlot.CAMERA) },
                )
                QuickActionTile(
                    icon = Icons.Filled.QrCodeScanner,
                    label = "Scanner",
                    scheme = scheme,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val pkg = scannerAppPackage
                        if (pkg != null) viewModel.launchApp(pkg) else onConfigureQuickAction(QuickActionSlot.SCANNER)
                    },
                    onLongClick = { onConfigureQuickAction(QuickActionSlot.SCANNER) },
                )
                QuickActionTile(
                    icon = Icons.Filled.Phone,
                    label = "Phone",
                    scheme = scheme,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val pkg = phoneAppPackage
                        if (pkg != null) viewModel.launchApp(pkg) else onConfigureQuickAction(QuickActionSlot.PHONE)
                    },
                    onLongClick = { onConfigureQuickAction(QuickActionSlot.PHONE) },
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun QuickActionTile(
    icon: ImageVector,
    label: String,
    scheme: ColorScheme,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .aspectRatio(1f)
            .background(scheme.surface, CardShape)
            .border(1.dp, scheme.outlineVariant.copy(alpha = 0.35f), CardShape)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(icon, contentDescription = label, tint = scheme.primary)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurface,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

private fun homeRadialGradient(scheme: ColorScheme): Brush {
    val glow = Color(ColorUtils.blendARGB(scheme.background.toArgbInt(), scheme.surfaceVariant.toArgbInt(), 0.7f))
    return Brush.verticalGradient(colors = listOf(glow, scheme.background, Color.Black))
}

private fun Color.toArgbInt(): Int = android.graphics.Color.argb(
    (alpha * 255).toInt(),
    (red * 255).toInt(),
    (green * 255).toInt(),
    (blue * 255).toInt(),
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SegmentedClockCard(
    now: Date,
    scheme: ColorScheme,
    onClockClick: () -> Unit,
    onClockLongClick: () -> Unit,
    onDateClick: () -> Unit,
    onDateLongClick: () -> Unit,
) {
    val timeText = SimpleDateFormat("hh:mm", Locale.getDefault()).format(now)
    val amPm = SimpleDateFormat("a", Locale.getDefault()).format(now).uppercase(Locale.getDefault())
    val dateLabel = remember(now.time / 60000) {
        SimpleDateFormat("EEE · d MMM", Locale.getDefault()).format(now).uppercase(Locale.getDefault())
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(listOf(scheme.surfaceVariant, scheme.surface)),
                CardShape,
            )
            .border(1.dp, scheme.outlineVariant.copy(alpha = 0.4f), CardShape)
            .padding(vertical = 28.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.combinedClickable(onClick = onClockClick, onLongClick = onClockLongClick),
        ) {
            Text(
                text = timeText,
                fontWeight = FontWeight.Bold,
                fontSize = 60.sp,
                color = Color.White,
            )
            Text(
                text = amPm,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.padding(start = 6.dp, bottom = 10.dp),
            )
        }
        Text(
            text = dateLabel,
            fontSize = 13.sp,
            letterSpacing = 3.sp,
            color = Color.White.copy(alpha = 0.7f),
            modifier = Modifier
                .padding(top = 14.dp)
                .combinedClickable(onClick = onDateClick, onLongClick = onDateLongClick),
        )
    }
}
