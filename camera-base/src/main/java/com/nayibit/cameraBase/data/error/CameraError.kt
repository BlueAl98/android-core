package com.nayibit.cameraBase.data.error

sealed class CameraError {
    object PermissionDenied : CameraError()
    data class InitFailed(val cause: Throwable) : CameraError()
    data class BindingFailed(val cause: Throwable) : CameraError()
    object NoCameraAvailable : CameraError()
}
