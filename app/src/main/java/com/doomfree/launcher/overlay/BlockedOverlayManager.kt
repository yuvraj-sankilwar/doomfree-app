package com.doomfree.launcher.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.doomfree.launcher.data.AccentColor
import com.doomfree.launcher.ui.components.BlockedCard
import com.doomfree.launcher.ui.theme.DoomfreeTheme

/**
 * Draws the Blocked popup as a system overlay when a Distraction app is opened from
 * outside this launcher (caught by AppBlockAccessibilityService).
 */
class BlockedOverlayManager(private val context: Context) : LifecycleOwner, SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    private val windowManager = context.getSystemService(WindowManager::class.java)
    private var overlayView: ComposeView? = null

    fun show(appLabel: String, sessionEndsAt: Long, accent: AccentColor) {
        if (overlayView != null) return
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED

        val composeView = ComposeView(context).apply {
            setViewTreeLifecycleOwner(this@BlockedOverlayManager)
            setViewTreeSavedStateRegistryOwner(this@BlockedOverlayManager)
            setContent {
                DoomfreeTheme(accent = accent) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        BlockedCard(
                            appLabel = appLabel,
                            sessionEndsAt = sessionEndsAt,
                            onDismiss = { hide() },
                        )
                    }
                }
            }
        }

        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT,
        ).apply { gravity = Gravity.CENTER }

        windowManager?.addView(composeView, params)
        overlayView = composeView
    }

    fun hide() {
        val view = overlayView ?: return
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        windowManager?.removeView(view)
        overlayView = null
    }
}
