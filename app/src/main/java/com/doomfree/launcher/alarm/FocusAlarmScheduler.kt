package com.doomfree.launcher.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

object FocusAlarmScheduler {
    private const val EXTRA_SESSION_ID = "session_id"

    fun schedule(context: Context, sessionId: Long, endsAt: Long) {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        val pendingIntent = pendingIntent(context, sessionId)
        if (alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, endsAt, pendingIntent)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, endsAt, pendingIntent)
        }
    }

    fun cancel(context: Context, sessionId: Long) {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        alarmManager.cancel(pendingIntent(context, sessionId))
    }

    private fun pendingIntent(context: Context, sessionId: Long): PendingIntent {
        val intent = Intent(context, FocusAlarmReceiver::class.java).apply {
            putExtra(EXTRA_SESSION_ID, sessionId)
        }
        return PendingIntent.getBroadcast(
            context,
            sessionId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    fun extractSessionId(intent: Intent): Long = intent.getLongExtra(EXTRA_SESSION_ID, -1L)
}
