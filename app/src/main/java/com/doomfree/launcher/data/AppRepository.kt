package com.doomfree.launcher.data

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.graphics.drawable.Drawable
import android.os.Process
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class AppRepository(private val context: Context) {
    private val db = AppDatabase.get(context)
    private val appEntryDao = db.appEntryDao()
    private val todoDao = db.todoDao()
    private val focusSessionDao = db.focusSessionDao()
    private val launcherApps = context.getSystemService(LauncherApps::class.java)

    val essentials: Flow<List<AppEntry>> = appEntryDao.observeEssentials()
    val distractions: Flow<List<AppEntry>> = appEntryDao.observeDistractions()
    val frequent: Flow<List<AppEntry>> = appEntryDao.observeFrequent()
    val activeSession: Flow<FocusSession?> = focusSessionDao.observeActive()
    val todos: Flow<List<TodoItem>> = todoDao.observeAll()

    suspend fun refreshInstalledApps() = withContext(Dispatchers.IO) {
        val user = Process.myUserHandle()
        val activities = launcherApps?.getActivityList(null, user) ?: emptyList()
        // Some apps (Amazon Shopping, Samsung "Dual Apps" clones, etc.) expose more than one
        // launcher activity for the same package — dedupe or downstream lists get duplicate keys.
        val discovered = activities
            .distinctBy { it.applicationInfo.packageName }
            .map { info ->
                AppEntry(
                    packageName = info.applicationInfo.packageName,
                    label = info.label.toString(),
                )
            }
        appEntryDao.insertAll(discovered)

        val installedPackages = discovered.map { it.packageName }.toSet()
        val stored = appEntryDao.observeAll().first()
        stored.filter { it.packageName !in installedPackages }.forEach {
            appEntryDao.delete(it.packageName)
        }
    }

    suspend fun loadInstalledAppsWithIcons(): List<InstalledApp> = withContext(Dispatchers.IO) {
        val user = Process.myUserHandle()
        val activities = launcherApps?.getActivityList(null, user) ?: emptyList()
        val entries = appEntryDao.observeAll().first().associateBy { it.packageName }
        activities.distinctBy { it.applicationInfo.packageName }.mapNotNull { info ->
            val pkg = info.applicationInfo.packageName
            val entry = entries[pkg]
            val icon: Drawable = runCatching { info.getBadgedIcon(0) }
                .getOrElse {
                    Log.w(TAG, "Failed to load icon for $pkg", it)
                    runCatching { context.packageManager.getApplicationIcon(pkg) }.getOrNull()
                } ?: return@mapNotNull null
            InstalledApp(
                packageName = pkg,
                label = info.label.toString(),
                icon = icon,
                group = entry?.group ?: AppGroup.GENERAL,
                isFrequent = entry?.isFrequent ?: false,
            )
        }.sortedBy { it.label.lowercase() }
    }

    fun launchApp(packageName: String) {
        val user = Process.myUserHandle()
        runCatching {
            val target = launcherApps?.getActivityList(packageName, user)?.firstOrNull()
            if (target != null) {
                launcherApps.startMainActivity(target.componentName, user, null, null)
                return
            }
        }.onFailure { Log.w(TAG, "LauncherApps launch failed for $packageName, falling back", it) }

        runCatching {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
                ?: return
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
        }.onFailure { Log.e(TAG, "Failed to launch $packageName", it) }
    }

    suspend fun setGroup(packageName: String, group: AppGroup) =
        appEntryDao.setGroup(packageName, group)

    suspend fun resetGroup(group: AppGroup) = appEntryDao.resetGroup(group)

    suspend fun setFrequent(packageName: String, isFrequent: Boolean, sortOrder: Int?) =
        appEntryDao.setFrequent(packageName, isFrequent, sortOrder)

    suspend fun resetFrequent() = appEntryDao.resetFrequent()

    suspend fun getGroup(packageName: String): AppGroup =
        appEntryDao.getByPackage(packageName)?.group ?: AppGroup.GENERAL

    suspend fun addTodo(text: String) = todoDao.insert(TodoItem(text = text))
    suspend fun updateTodo(item: TodoItem) = todoDao.update(item)
    suspend fun deleteTodo(item: TodoItem) = todoDao.delete(item)

    suspend fun startFocusSession(durationMs: Long): FocusSession {
        val now = System.currentTimeMillis()
        val session = FocusSession(
            startedAt = now,
            durationMs = durationMs,
            endsAt = now + durationMs,
            isActive = true,
        )
        val id = focusSessionDao.insert(session)
        return session.copy(id = id)
    }

    suspend fun getActiveSession(): FocusSession? = focusSessionDao.getActive()
    suspend fun endSession(id: Long) = focusSessionDao.deactivate(id)
    suspend fun endAllSessions() = focusSessionDao.deactivateAll()

    companion object {
        private const val TAG = "AppRepository"
        @Volatile private var instance: AppRepository? = null
        fun get(context: Context): AppRepository =
            instance ?: synchronized(this) {
                instance ?: AppRepository(context.applicationContext).also { instance = it }
            }
    }
}
