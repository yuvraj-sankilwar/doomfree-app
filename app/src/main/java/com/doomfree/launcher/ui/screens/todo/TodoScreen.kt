package com.doomfree.launcher.ui.screens.todo

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.doomfree.launcher.data.Torch
import com.doomfree.launcher.ui.MainViewModel
import com.doomfree.launcher.ui.theme.CardShape

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TodoScreen(viewModel: MainViewModel, onOpenFocus: () -> Unit) {
    val todos by viewModel.todos.collectAsState()
    val activeSession by viewModel.activeSession.collectAsState()
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    val leftCount = todos.count { !it.done }
    val focusActive = activeSession != null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(scheme.background)
            .statusBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(top = 20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            QuickTile(
                modifier = Modifier.weight(1f),
                onClick = onOpenFocus,
                background = if (focusActive) scheme.primary else scheme.surface,
            ) {
                Icon(Icons.Filled.Bolt, contentDescription = null, tint = if (focusActive) scheme.onPrimary else scheme.primary)
                Text(
                    text = "Focus",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (focusActive) scheme.onPrimary else scheme.onSurface,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            QuickTile(
                modifier = Modifier.weight(1f),
                onClick = { context.startActivity(Intent(Settings.ACTION_SETTINGS)) },
                background = scheme.surface,
            ) {
                Icon(Icons.Filled.Settings, contentDescription = null, tint = scheme.primary)
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            QuickTile(
                modifier = Modifier.weight(1f),
                onClick = { Torch.toggle(context) },
                background = scheme.surface,
            ) {
                Icon(Icons.Filled.FlashlightOn, contentDescription = null, tint = scheme.primary)
                Text(
                    text = "Torch",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = "TO DO", fontSize = 11.sp, letterSpacing = 2.sp, color = scheme.onSurfaceVariant)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "$leftCount left", fontSize = 11.sp, letterSpacing = 1.sp, color = scheme.onSurfaceVariant.copy(alpha = 0.7f))
                Box(
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .size(36.dp)
                        .background(scheme.primary, CircleShape)
                        .border(1.dp, scheme.outlineVariant.copy(alpha = 0.5f), CircleShape)
                        .clickable { showAddDialog = true },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add task", tint = scheme.onPrimary)
                }
            }
        }

        LazyColumn(modifier = Modifier.weight(1f).padding(top = 4.dp, bottom = 12.dp)) {
            items(todos, key = { it.id }) { todo ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = { viewModel.toggleTodo(todo) },
                            onLongClick = { viewModel.deleteTodo(todo) },
                        )
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (todo.done) {
                        Box(
                            modifier = Modifier.size(20.dp).background(scheme.primary, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Filled.Check, contentDescription = null, tint = scheme.onPrimary, modifier = Modifier.size(11.dp))
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .border(1.5.dp, scheme.onSurfaceVariant.copy(alpha = 0.4f), CircleShape),
                        )
                    }
                    Text(
                        text = todo.text,
                        fontSize = 15.sp,
                        color = if (todo.done) scheme.onSurfaceVariant else scheme.onSurface,
                        textDecoration = if (todo.done) TextDecoration.LineThrough else null,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddTodoDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { text ->
                viewModel.addTodo(text)
                showAddDialog = false
            },
        )
    }
}

@Composable
private fun AddTodoDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    val scheme = MaterialTheme.colorScheme

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            DialogPillButton(
                label = "Add",
                icon = Icons.Filled.Check,
                background = scheme.primary,
                contentColor = scheme.onPrimary,
                onClick = { if (text.isNotBlank()) onConfirm(text) },
            )
        },
        dismissButton = {
            DialogPillButton(
                label = "Close",
                icon = Icons.Filled.Close,
                background = scheme.surface,
                contentColor = scheme.onSurface,
                onClick = onDismiss,
                modifier = Modifier.padding(end = 8.dp),
            )
        },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("What do you need to do?") },
                minLines = 3,
                maxLines = 6,
                shape = CardShape,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { if (text.isNotBlank()) onConfirm(text) }),
                modifier = Modifier.fillMaxWidth(),
            )
        },
    )
}

@Composable
private fun DialogPillButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    background: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .background(background, CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = label, tint = contentColor, modifier = Modifier.size(18.dp))
        Text(
            text = label,
            color = contentColor,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 6.dp),
        )
    }
}

@Composable
private fun QuickTile(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    background: Color,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .aspectRatio(1f)
            .background(background, CardShape)
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        content = content,
    )
}
