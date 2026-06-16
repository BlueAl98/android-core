package com.nayibit.android_core

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.nayibit.android_core.camera.ui.CameraControls
import com.nayibit.android_core.screen.TestScreen
import com.nayibit.android_core.ui.theme.AndroidcoreTheme
import com.nayibit.cameraBase.data.result.CaptureResult

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidcoreTheme {
                TestScreen()
            }
        }
    }
}
