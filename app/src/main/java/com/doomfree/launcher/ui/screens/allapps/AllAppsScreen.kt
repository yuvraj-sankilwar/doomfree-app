package com.doomfree.launcher.ui.screens.allapps

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.doomfree.launcher.data.AppGroup
import com.doomfree.launcher.data.InstalledApp
import com.doomfree.launcher.ui.MainViewModel
import com.doomfree.launcher.ui.components.AppIcon
import com.doomfree.launcher.ui.theme.CardShape
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val INDEX_LETTERS = ('A'..'Z').toList()

@Composable
fun AllAppsScreen(viewModel: MainViewModel) {
    val installedApps by viewModel.installedApps.collectAsState()
    val activeSession by viewModel.activeSession.collectAsState()
    var query by remember { mutableStateOf("") }
    var blockedApp by remember { mutableStateOf<InstalledApp?>(null) }

    val nonFrequent = installedApps.filter { !it.isFrequent }
    val filtered = remember(nonFrequent, query) {
        val base = if (query.isBlank()) nonFrequent else nonFrequent.filter { it.label.contains(query, ignoreCase = true) }
        base.sortedBy { it.label.lowercase() }
    }

    val firstIndexForLetter = remember(filtered) {
        val map = mutableMapOf<Char, Int>()
        filtered.forEachIndexed { index, app ->
            val letter = app.label.firstOrNull()?.uppercaseChar() ?: return@forEachIndexed
            map.getOrPut(letter) { index }
        }
        map
    }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var activeLetter by remember { mutableStateOf<Char?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding().padding(top = 16.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search apps") },
                singleLine = true,
                shape = CardShape,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            )

            Row(modifier = Modifier.fillMaxSize().padding(top = 8.dp)) {
                LazyColumn(state = listState, modifier = Modifier.weight(1f).fillMaxHeight()) {
                    items(filtered, key = { it.packageName }) { app ->
                        AppRow(
                            app = app,
                            onClick = {
                                if (activeSession != null && app.group == AppGroup.DISTRACTION) {
                                    blockedApp = app
                                } else {
                                    viewModel.launchApp(app.packageName)
                                }
                            },
                        )
                    }
                }

                AlphabetIndex(
                    availableLetters = firstIndexForLetter.keys,
                    activeLetter = activeLetter,
                    onLetterActive = { letter ->
                        activeLetter = letter
                        firstIndexForLetter[letter]?.let { index ->
                            scope.launch { listState.scrollToItem(index) }
                        }
                    },
                    onReleased = { activeLetter = null },
                )
            }
        }

        val bubbleLetter = activeLetter
        if (bubbleLetter != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 56.dp)
                    .size(64.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = bubbleLetter.toString(),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }

    val session = activeSession
    if (blockedApp != null && session != null) {
        val app = blockedApp!!
        AlertDialog(
            onDismissRequest = { blockedApp = null },
            confirmButton = {},
            title = { Text("${app.label} is blocked") },
            text = {
                val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
                Column {
                    Text("Marked as a Distraction — locked until your session ends at ${timeFormat.format(Date(session.endsAt))}")
                    Button(
                        onClick = { blockedApp = null },
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    ) { Text("Got it") }
                }
            },
        )
    }
}

/** Right-edge fast-scroll strip: tap or drag across it to jump straight to a letter. */
@Composable
private fun AlphabetIndex(
    availableLetters: Set<Char>,
    activeLetter: Char?,
    onLetterActive: (Char) -> Unit,
    onReleased: () -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxHeight()
            .width(28.dp)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    onLetterActive(letterFor(down.position.y, size.height, INDEX_LETTERS))
                    drag(down.id) { change ->
                        onLetterActive(letterFor(change.position.y, size.height, INDEX_LETTERS))
                        change.consume()
                    }
                    onReleased()
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            INDEX_LETTERS.forEach { letter ->
                val isActive = letter == activeLetter
                Text(
                    text = letter.toString(),
                    fontSize = if (isActive) 12.sp else 9.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                    color = when {
                        isActive -> MaterialTheme.colorScheme.primary
                        letter in availableLetters -> MaterialTheme.colorScheme.onSurfaceVariant
                        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                    },
                )
            }
        }
    }
}

private fun letterFor(y: Float, totalHeight: Int, letters: List<Char>): Char {
    val fraction = (y / totalHeight.toFloat()).coerceIn(0f, 0.9999f)
    val index = (fraction * letters.size).toInt().coerceIn(0, letters.size - 1)
    return letters[index]
}

@Composable
private fun AppRow(app: InstalledApp, onClick: () -> Unit) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppIcon(icon = app.icon, modifier = Modifier.size(40.dp))
            Text(
                text = app.label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 16.dp),
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.surface)
    }
}
