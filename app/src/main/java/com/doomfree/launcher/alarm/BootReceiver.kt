package com.doomfree.launcher.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.doomfree.launcher.data.AppRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Re-arms the end-of-session alarm for any FocusSession that was still active across a reboot. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val repo = AppRepository.get(context)
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            val active = repo.getActiveSession()
            if (active != null) {
                if (active.endsAt <= System.currentTimeMillis()) {
                    repo.endSession(active.id)
                } else {
                    FocusAlarmScheduler.schedule(context, active.id, active.endsAt)
                }
            }
            pending.finish()
        }
    }
}
