package com.nayibit.android_core.screen

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nayibit.android_core.camera.CameraState
import com.nayibit.android_core.camera.CameraViewModel
import com.nayibit.android_core.camera.ui.CameraControls
import com.nayibit.cameraBase.data.CameraManager
import com.nayibit.cameraBase.data.error.CameraError
import com.nayibit.cameraBase.data.result.CaptureResult
import com.nayibit.cameraBase.presentation.CameraBase
import com.nayibit.cameraBase.presentation.rememberCameraBaseState
import com.nayibit.composables.presentation.components.buttons.ButtonBase
import com.nayibit.composables.presentation.components.dialogs.BaseDialog

@Composable
fun TestScreen(){

    val vm : CameraViewModel = viewModel()
    val st = vm.state.collectAsStateWithLifecycle()

    val state = rememberCameraBaseState(
        initialLensFacing = st.value.cameraLends
    )

        CameraBase(
         state = state,
         overlayContent = { scope ->
            CameraControls(
                onCapture = { scope.saveToCache{
                    println(it)
                }  },
                onFlip = {
                    scope.flipCamera()
                  //  vm.change(state.lensFacing)
               },
                onClose = {}
            )
        }
    )


}