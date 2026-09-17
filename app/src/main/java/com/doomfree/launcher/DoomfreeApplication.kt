package com.doomfree.launcher

import android.app.Application
import com.doomfree.launcher.data.AppRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DoomfreeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.IO).launch {
            AppRepository.get(this@DoomfreeApplication).refreshInstalledApps()
        }
    }
}
