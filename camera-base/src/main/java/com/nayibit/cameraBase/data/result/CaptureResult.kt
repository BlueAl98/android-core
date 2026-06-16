package com.nayibit.cameraBase.data.result

import android.net.Uri
import androidx.camera.core.ImageProxy

sealed class CaptureResult {
    // In-memory — caller must close ImageProxy after use
    data class ImageCaptured(val imageProxy: ImageProxy) : CaptureResult()

    // Saved to disk
    data class FileSaved(val uri: Uri) : CaptureResult()

    data class Failure(val cause: Throwable) : CaptureResult()
}
