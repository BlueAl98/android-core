package com.nayibit.cameraBase.presentation

import com.nayibit.cameraBase.data.result.CaptureResult
import java.io.File

interface CameraScope {
    fun capture(onResult: (CaptureResult) -> Unit)
    fun saveToCache(onResult: (CaptureResult) -> Unit)
    fun saveToFile(file: File, onResult: (CaptureResult) -> Unit)
    fun flipCamera()
    fun setFlashMode(mode: FlashMode)
}
