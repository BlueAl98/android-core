package com.nayibit.android_core.camera

import androidx.camera.core.CameraSelector
import androidx.lifecycle.ViewModel
import com.nayibit.cameraBase.presentation.CameraBaseState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


data class CameraBlue(
    val cameraLends: Int = CameraSelector.LENS_FACING_FRONT
)

class CameraViewModel : ViewModel() {
    private val _state = MutableStateFlow(CameraBlue())
    val state = _state.asStateFlow()


    fun change(value: Int){
        _state.update {
            it.copy(cameraLends = value)
        }
    }

    // CameraBaseState uses mutableStateOf internally — Compose tracks it via snapshots,
    // not StateFlow. Expose it directly so the composable reads it as Compose state.

}
