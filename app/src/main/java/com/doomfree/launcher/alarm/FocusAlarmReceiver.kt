package com.doomfree.launcher.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.doomfree.launcher.data.AppRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FocusAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val sessionId = FocusAlarmScheduler.extractSessionId(intent)
        if (sessionId < 0) return
        val repo = AppRepository.get(context)
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            repo.endSession(sessionId)
            pending.finish()
        }
    }
}
