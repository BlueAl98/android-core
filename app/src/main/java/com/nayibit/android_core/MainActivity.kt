package com.nayibit.android_core

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.nayibit.android_core.screen.TestScreen
import com.nayibit.android_core.ui.theme.AndroidcoreTheme

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
