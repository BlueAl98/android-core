package com.nayibit.android_core.camera

import com.nayibit.cameraBase.data.error.CameraError

sealed class CameraState {
    object RequestingPermission : CameraState()
    object Preview : CameraState()
    data class Error(val error: CameraError) : CameraState()
}
