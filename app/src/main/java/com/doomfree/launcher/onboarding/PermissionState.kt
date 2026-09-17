package com.doomfree.launcher.onboarding

import android.app.role.RoleManager
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.text.TextUtils

object PermissionState {

    fun isDefaultLauncher(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(RoleManager::class.java)
            roleManager?.isRoleHeld(RoleManager.ROLE_HOME) ?: false
        } else {
            val intent = android.content.Intent(android.content.Intent.ACTION_MAIN)
                .addCategory(android.content.Intent.CATEGORY_HOME)
            val resolveInfo = context.packageManager.resolveActivity(
                intent,
                android.content.pm.PackageManager.MATCH_DEFAULT_ONLY,
            )
            resolveInfo?.activityInfo?.packageName == context.packageName
        }
    }

    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val expectedComponent = "${context.packageName}/${context.packageName}.accessibility.AppBlockAccessibilityService"
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
        ) ?: return false
        val splitter = TextUtils.SimpleStringSplitter(':')
        splitter.setString(enabledServices)
        return splitter.any { it.equals(expectedComponent, ignoreCase = true) }
    }

    fun canDrawOverlays(context: Context): Boolean = Settings.canDrawOverlays(context)
}
