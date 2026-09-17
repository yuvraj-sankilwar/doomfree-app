package com.doomfree.launcher.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "doomfree_settings")

enum class AccentColor(val key: String, val argb: Long) {
    ORANGE("orange", 0xFFFF7A3D),
    PURPLE("purple", 0xFF9B6BFF),
    GREEN("green", 0xFF4CD97B),
    TEAL("teal", 0xFF2FD3C6),
    PINK("pink", 0xFFFF6FA5),
    RED("red", 0xFFFF5A5F),
    BLUE("blue", 0xFF4D7CFF),
    SKY_BLUE("sky_blue", 0xFF4FC3F7),
    ;

    companion object {
        fun fromKey(key: String?): AccentColor = entries.find { it.key == key } ?: ORANGE
    }
}

class SettingsDataStore(private val context: Context) {
    private val accentColorKey = stringPreferencesKey("accent_color_key")
    private val onboardingCompleteKey = booleanPreferencesKey("onboarding_complete")

    val accentColor: Flow<AccentColor> =
        context.dataStore.data.map { AccentColor.fromKey(it[accentColorKey]) }

    val onboardingComplete: Flow<Boolean> =
        context.dataStore.data.map { it[onboardingCompleteKey] ?: false }

    suspend fun setAccentColor(color: AccentColor) {
        context.dataStore.edit { it[accentColorKey] = color.key }
    }

    suspend fun setOnboardingComplete(complete: Boolean) {
        context.dataStore.edit { it[onboardingCompleteKey] = complete }
    }

    fun quickAction(slot: QuickActionSlot): Flow<String?> {
        val key = stringPreferencesKey(slot.prefKey)
        return context.dataStore.data.map { it[key] }
    }

    suspend fun setQuickAction(slot: QuickActionSlot, packageName: String?) {
        val key = stringPreferencesKey(slot.prefKey)
        context.dataStore.edit {
            if (packageName == null) it.remove(key) else it[key] = packageName
        }
    }
}
