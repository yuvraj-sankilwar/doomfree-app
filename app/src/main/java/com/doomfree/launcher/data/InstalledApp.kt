package com.doomfree.launcher.data

import android.graphics.drawable.Drawable

data class InstalledApp(
    val packageName: String,
    val label: String,
    val icon: Drawable,
    val group: AppGroup,
    val isFrequent: Boolean,
)
