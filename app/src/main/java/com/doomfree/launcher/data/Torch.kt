package com.doomfree.launcher.data

import android.content.Context
import android.hardware.camera2.CameraManager

/** Direct hardware flashlight toggle — no app needed for this one. */
object Torch {
    private var isOn = false

    fun toggle(context: Context) {
        val manager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager ?: return
        val cameraId = manager.cameraIdList.firstOrNull { id ->
            manager.getCameraCharacteristics(id)
                .get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        } ?: return
        isOn = !isOn
        runCatching { manager.setTorchMode(cameraId, isOn) }
    }
}
