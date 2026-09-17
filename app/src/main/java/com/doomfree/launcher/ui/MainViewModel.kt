package com.doomfree.launcher.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.doomfree.launcher.alarm.FocusAlarmScheduler
import com.doomfree.launcher.data.AccentColor
import com.doomfree.launcher.data.AppEntry
import com.doomfree.launcher.data.AppGroup
import com.doomfree.launcher.data.AppRepository
import com.doomfree.launcher.data.FocusSession
import com.doomfree.launcher.data.InstalledApp
import com.doomfree.launcher.data.QuickActionSlot
import com.doomfree.launcher.data.SettingsDataStore
import com.doomfree.launcher.data.TodoItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AppRepository.get(application)
    private val settings = SettingsDataStore(application)

    val accentColor: StateFlow<AccentColor> =
        stateFlowOf(settings.accentColor, AccentColor.ORANGE)

    val activeSession: StateFlow<FocusSession?> =
        stateFlowOf(repository.activeSession, null)

    val essentials: StateFlow<List<AppEntry>> =
        stateFlowOf(repository.essentials, emptyList())

    val distractions: StateFlow<List<AppEntry>> =
        stateFlowOf(repository.distractions, emptyList())

    val frequentApps: StateFlow<List<AppEntry>> =
        stateFlowOf(repository.frequent, emptyList())

    val todos: StateFlow<List<TodoItem>> =
        stateFlowOf(repository.todos, emptyList())

    private val _installedApps = MutableStateFlow<List<InstalledApp>>(emptyList())
    val installedApps: StateFlow<List<InstalledApp>> = _installedApps.asStateFlow()

    private val quickActionPackages: Map<QuickActionSlot, StateFlow<String?>> =
        QuickActionSlot.entries.associateWith { slot -> stateFlowOf(settings.quickAction(slot), null) }

    fun quickActionPackage(slot: QuickActionSlot): StateFlow<String?> = quickActionPackages.getValue(slot)

    fun setQuickAction(slot: QuickActionSlot, packageName: String?) {
        viewModelScope.launch { settings.setQuickAction(slot, packageName) }
    }

    init {
        loadInstalledApps()
    }

    fun loadInstalledApps() {
        viewModelScope.launch {
            _installedApps.value = repository.loadInstalledAppsWithIcons()
        }
    }

    fun launchApp(packageName: String) = repository.launchApp(packageName)

    fun setGroup(packageName: String, group: AppGroup) {
        viewModelScope.launch {
            repository.setGroup(packageName, group)
            loadInstalledApps()
        }
    }

    fun toggleFrequent(packageName: String, makeFrequent: Boolean, sortOrder: Int? = null) {
        viewModelScope.launch {
            repository.setFrequent(packageName, makeFrequent, sortOrder)
            loadInstalledApps()
        }
    }

    fun resetGroup(group: AppGroup) {
        viewModelScope.launch {
            repository.resetGroup(group)
            loadInstalledApps()
        }
    }

    fun resetFrequent() {
        viewModelScope.launch {
            repository.resetFrequent()
            loadInstalledApps()
        }
    }

    fun addTodo(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch { repository.addTodo(text.trim()) }
    }

    fun toggleTodo(item: TodoItem) {
        viewModelScope.launch { repository.updateTodo(item.copy(done = !item.done)) }
    }

    fun deleteTodo(item: TodoItem) {
        viewModelScope.launch { repository.deleteTodo(item) }
    }

    fun setAccentColor(color: AccentColor) {
        viewModelScope.launch { settings.setAccentColor(color) }
    }

    suspend fun groupOf(packageName: String): AppGroup = repository.getGroup(packageName)

    fun startFocusSession(durationMs: Long) {
        viewModelScope.launch {
            val session = repository.startFocusSession(durationMs)
            FocusAlarmScheduler.schedule(getApplication(), session.id, session.endsAt)
        }
    }

    private fun <T> stateFlowOf(flow: kotlinx.coroutines.flow.Flow<T>, initial: T): StateFlow<T> {
        val state = MutableStateFlow(initial)
        viewModelScope.launch { flow.collect { state.value = it } }
        return state.asStateFlow()
    }
}
