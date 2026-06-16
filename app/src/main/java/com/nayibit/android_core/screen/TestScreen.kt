package com.nayibit.android_core.screen

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nayibit.android_core.camera.CameraViewModel
import com.nayibit.cameraBase.presentation.CameraBase
import com.nayibit.cameraBase.presentation.CameraControls
import com.nayibit.cameraBase.presentation.rememberCameraBaseState

@Composable
fun TestScreen(){

    val vm : CameraViewModel = viewModel()
    val st = vm.state.collectAsStateWithLifecycle()

    CameraBase()


}