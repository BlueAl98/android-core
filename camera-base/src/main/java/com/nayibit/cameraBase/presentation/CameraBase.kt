package com.nayibit.cameraBase.presentation

import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.nayibit.cameraBase.data.CameraManager
import com.nayibit.cameraBase.data.error.CameraError
import com.nayibit.cameraBase.data.result.CaptureResult
import java.io.File

/**
 * Drop-in camera composable. Completely passive about permissions — the caller owns
 * the launcher and updates [state].permissionGranted with the result.
 *
 * @param state              Owns permission, lens-facing and error state.
 * @param modifier           Applied to the root [Box].
 * @param onPermissionRequest Called when the user taps the button in [permissionContent].
 *                           Wire your [ActivityResultLauncher] here.
 * @param permissionContent  Slot shown while [state].permissionGranted is false.
 *                           Receives [onPermissionRequest] so the default button works out of the box.
 * @param overlayContent     Drawn over the live preview. Receives [CameraScope] to capture,
 *                           save, or flip without touching [CameraManager] directly.
 * @param errorContent       Slot shown on camera error. Call [onDismiss] to return to the preview.
 */
@Composable
fun CameraBase(
    modifier: Modifier = Modifier,
    state: CameraBaseState = rememberCameraBaseState(),
    onPermissionRequest: () -> Unit = {},
    permissionContent: @Composable BoxScope.(onRequest: () -> Unit) -> Unit = { onRequest ->
        DefaultCameraPermissionContent(onRequest)
    },
    overlayContent: @Composable BoxScope.(scope: CameraScope) -> Unit = { scope ->
        CameraControls(scope = scope, state = state)
    },
    errorContent: @Composable BoxScope.(error: CameraError, onDismiss: () -> Unit) -> Unit = { error, onDismiss ->
        DefaultCameraErrorContent(error, onDismiss)
    }
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraManager = remember(context, lifecycleOwner) {
        CameraManager(context, lifecycleOwner)
    }

    DisposableEffect(cameraManager) {
        onDispose { cameraManager.stopPreview() }
    }
    

    val scope = remember(cameraManager) {
        object : CameraScope {
            override fun capture(onResult: (CaptureResult) -> Unit) =
                cameraManager.takePicture(onResult)

            override fun saveToCache(onResult: (CaptureResult) -> Unit) =
                cameraManager.saveToCache(onResult)

            override fun saveToFile(file: File?, onResult: (CaptureResult) -> Unit) =
                cameraManager.saveToFile(file, onResult)

            override fun flipCamera() = state.flipCamera()

            override fun setFlashMode(mode: FlashMode) =
                cameraManager.setFlashMode(mode.toCameraX())
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            state.error != null -> {
                errorContent(state.error!!) { state.dismissError() }
            }

            state.permissionGranted -> {
                key(state.lensFacing) {
                    AndroidView(
                        factory = { ctx ->
                            PreviewView(ctx).also { previewView ->
                                cameraManager.startPreview(
                                    previewView = previewView,
                                    lensFacing = state.lensFacing,
                                    flashMode = state.flashMode.toCameraX(),
                                    onError = { state.error = it }
                                )
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                overlayContent(scope)
            }
            else -> permissionContent(onPermissionRequest)
        }
    }
}

@Composable
fun BoxScope.DefaultCameraPermissionContent(onRequest: () -> Unit) {
    Column(
        modifier = Modifier
            .align(Alignment.Center)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Camera permission is required", style = MaterialTheme.typography.bodyLarge)
        Button(onClick = onRequest) { Text("Grant Permission") }
    }
}

@Composable
fun BoxScope.DefaultCameraErrorContent(error: CameraError, onDismiss: () -> Unit) {
    val message = when (error) {
        CameraError.PermissionDenied -> "Camera permission denied"
        CameraError.NoCameraAvailable -> "No camera found on this device"
        is CameraError.InitFailed -> "Camera init failed: ${error.cause.message}"
        is CameraError.BindingFailed -> "Camera binding failed: ${error.cause.message}"
    }
    Snackbar(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(16.dp),
        dismissAction = { TextButton(onClick = onDismiss) { Text("Dismiss") } }
    ) { Text(message) }
}
