package com.nayibit.cameraBase.presentation

import android.Manifest
import android.content.pm.PackageManager
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.nayibit.cameraBase.data.error.CameraError

enum class FlashMode {
    OFF, ON, AUTO;

    fun next() = when (this) {
        OFF -> ON
        ON -> AUTO
        AUTO -> OFF
    }

    fun toCameraX(): Int = when (this) {
        OFF -> ImageCapture.FLASH_MODE_OFF
        ON -> ImageCapture.FLASH_MODE_ON
        AUTO -> ImageCapture.FLASH_MODE_AUTO
    }
}

@Stable
class CameraBaseState(
    initialLensFacing: Int = CameraSelector.LENS_FACING_BACK,
    initialPermissionGranted: Boolean = false
) {
    var lensFacing by mutableStateOf(initialLensFacing)
    var permissionGranted by mutableStateOf(initialPermissionGranted)
    var flashMode by mutableStateOf(FlashMode.OFF)
    var error by mutableStateOf<CameraError?>(null)

    fun flipCamera() {
        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK)
            CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK
    }

    fun dismissError() {
        error = null
    }
}

/**
 * Creates and remembers a [CameraBaseState] for use directly in a composable.
 * Reads the current permission status so the camera shows immediately if already granted.
 *
 * For ViewModel usage, create [CameraBaseState] in the VM instead and pass it to [CameraBase].
 */
@Composable
fun rememberCameraBaseState(
    initialLensFacing: Int = CameraSelector.LENS_FACING_BACK
): CameraBaseState {
    val context = LocalContext.current
    val state = remember {
        val alreadyGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        CameraBaseState(
            initialLensFacing = initialLensFacing,
            initialPermissionGranted = alreadyGranted
        )
    }

    // Re-check when the user returns from Settings after granting permission manually
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        state.permissionGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    return state
}
