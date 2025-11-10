package com.nayibit.android_core

import TutorialBase
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.nayibit.android_core.ui.theme.AndroidcoreTheme
import com.nayibit.compose_tutorial.model.TutorialStep
import com.nayibit.compose_tutorial.util.LabelPosition

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidcoreTheme {
                Scaffold(modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.systemBars)) { innerPadding ->

                    var currentStep by remember { mutableIntStateOf(0) }
                    var showTutotial by remember { mutableStateOf(true) }
                    var rectFab by remember { mutableStateOf<Rect>(Rect.Zero) }
                    var rectSwipeCard by remember { mutableStateOf<Rect>(Rect.Zero) }


                    val steps = listOf(
                        TutorialStep(
                            rect = rectFab ,
                            description = "Somethin here first eement",
                            labelPosition = LabelPosition.Right
                        ),
                        TutorialStep(
                            rect = rectSwipeCard,
                            description = "second elemment",
                            labelPosition = LabelPosition.Bottom
                        )
                    )

                    TutorialBase(
                        listComponents = steps,
                        currentIndex = currentStep,
                        isTutorialEnabled = showTutotial,
                        onTutorialFinish = { showTutotial = false },
                        onNextStep = { currentStep ++ }
                    ){
                        Column(modifier = Modifier.padding(innerPadding)) {
                            Text(text = "Hello" , modifier = Modifier.onGloballyPositioned{
                                rectFab = it.boundsInWindow()
                            })

                            Text(text = "second text" , modifier = Modifier.onGloballyPositioned{
                                rectSwipeCard = it.boundsInWindow()
                            })
                        }
                    }

                }
            }
        }
    }
}

