package com.doomfree.launcher.data

/** A configurable "which app opens here" slot — the gesture shortcut and the Home screen quick tiles. */
enum class QuickActionSlot(val prefKey: String, val label: String) {
    GESTURE("quick_action_gesture", "Swipe-up shortcut"),
    CAMERA("quick_action_camera", "Camera"),
    SCANNER("quick_action_scanner", "Scanner"),
    PHONE("quick_action_phone", "Phone"),
    CLOCK("quick_action_clock", "Clock"),
    CALENDAR("quick_action_calendar", "Date & calendar"),
}
