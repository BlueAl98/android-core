package com.nayibit.android_core.screen

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nayibit.android_core.camera.CameraViewModel
import com.nayibit.cameraBase.presentation.CameraBase
import com.nayibit.cameraBase.presentation.CameraControls
import com.nayibit.cameraBase.presentation.rememberCameraBaseState
import com.nayibit.composables.presentation.components.dropMenus.DropdownMenuBase
import com.nayibit.composables.presentation.components.textFields.TextFieldBase
import com.nayibit.croppingImage.ImageCropper
import com.nayibit.croppingImage.model.ImageCropperColors
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater

@Composable
fun TestScreen(){

    val vm : CameraViewModel = viewModel()
    val st = vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

  //  CameraBase()

    Column(Modifier.padding(30.dp)) {

    /*    val testBitmap = remember {
            context.assets.open("error.png").use { BitmapFactory.decodeStream(it) }
        }

        ImageCropper(
            imageBitmap = testBitmap,
            onCropConfirmed = {},
            lockToLandscape = true
            )*/

        DropdownMenuBase(
            searchable = true,
            items =  listOf(1, 2),
            selectedItem = 1,
            onItemSelected = {  },
            labelSelector = {it.toString()},
            numbersOnly = true
            )

    }

}

private fun sampleDocumentBitmap(): Bitmap {
    val bitmap = Bitmap.createBitmap(900, 600, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(Color.rgb(18, 18, 22))
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(214, 196, 168) }
    canvas.drawRect(120f, 60f, 780f, 540f, paint)
    return bitmap
}

@Preview(name = "Cropper - light", showBackground = true, widthDp = 800, heightDp = 400)
@Composable
private fun ImageCropperLightPreview() {
    val bitmap = remember { sampleDocumentBitmap() }
    ImageCropper(
        imageBitmap = bitmap,
        colors = ImageCropperColors.defaults(),
        lockToLandscape = false,
        onCropConfirmed = {}
    )
}

@Preview(name = "Cropper - dark", showBackground = true, widthDp = 800, heightDp = 400, backgroundColor = 0xFF000000)
@Composable
private fun ImageCropperDarkPreview() {
    val bitmap = remember { sampleDocumentBitmap() }
    ImageCropper(
        imageBitmap = bitmap,
        colors = ImageCropperColors.defaultsDark(),
        lockToLandscape = false,
        onCropConfirmed = {}
    )
}