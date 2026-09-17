package com.doomfree.launcher.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.view.accessibility.AccessibilityEvent
import com.doomfree.launcher.data.AccentColor
import com.doomfree.launcher.data.AppGroup
import com.doomfree.launcher.data.AppRepository
import com.doomfree.launcher.data.SettingsDataStore
import com.doomfree.launcher.overlay.BlockedOverlayManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppBlockAccessibilityService : AccessibilityService() {

    private lateinit var repository: AppRepository
    private lateinit var settings: SettingsDataStore
    private lateinit var overlayManager: BlockedOverlayManager
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    override fun onServiceConnected() {
        super.onServiceConnected()
        repository = AppRepository.get(applicationContext)
        settings = SettingsDataStore(applicationContext)
        overlayManager = BlockedOverlayManager(applicationContext)

        serviceInfo = serviceInfo?.apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val packageName = event.packageName?.toString() ?: return
        if (packageName == applicationContext.packageName) return

        serviceScope.launch {
            val session = repository.getActiveSession() ?: return@launch
            val group = repository.getGroup(packageName)
            if (group != AppGroup.DISTRACTION) return@launch

            performGlobalAction(GLOBAL_ACTION_HOME)

            val label = runCatching {
                packageManager.getApplicationInfo(packageName, 0)
                    .loadLabel(packageManager).toString()
            }.getOrDefault(packageName)
            val accent = settings.accentColor.first()

            withContext(Dispatchers.Main) {
                overlayManager.show(appLabel = label, sessionEndsAt = session.endsAt, accent = accent)
            }
        }
    }

    override fun onInterrupt() {
        overlayManager.hide()
    }

    override fun onDestroy() {
        super.onDestroy()
        overlayManager.hide()
        serviceJob.cancel()
    }
}
