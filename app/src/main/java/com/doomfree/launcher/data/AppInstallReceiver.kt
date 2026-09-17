package com.doomfree.launcher.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppInstallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        intent.data?.schemeSpecificPart ?: return
        val repo = AppRepository.get(context)
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            when (intent.action) {
                Intent.ACTION_PACKAGE_ADDED -> repo.refreshInstalledApps()
                Intent.ACTION_PACKAGE_REMOVED -> repo.refreshInstalledApps()
            }
            pending.finish()
        }
    }
}
