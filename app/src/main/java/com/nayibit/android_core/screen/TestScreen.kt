package com.nayibit.android_core.screen

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nayibit.android_core.camera.CameraViewModel
import com.nayibit.cameraBase.presentation.CameraBase
import com.nayibit.cameraBase.presentation.CameraControls
import com.nayibit.cameraBase.presentation.rememberCameraBaseState
import com.nayibit.composables.presentation.components.textFields.TextFieldBase
import com.nayibit.croppingImage.ImageCropper

@Composable
fun TestScreen(){

    val vm : CameraViewModel = viewModel()
    val st = vm.state.collectAsStateWithLifecycle()

  //  CameraBase()

    Column(Modifier.padding(30.dp)) {
        val path = "/data/data/com.nayibit.android_core/files/IMG_1781702192987.jpg"
        val image =  BitmapFactory.decodeFile(path)
        ImageCropper(
            imageBitmap = image,
           // initialPoints = savedPoints, // or null
            onCropConfirmed = { result -> /* result.points, result.coordinates */ },
            onCropRejected = { message -> /* show snackbar/toast */ }
        )
    }

}