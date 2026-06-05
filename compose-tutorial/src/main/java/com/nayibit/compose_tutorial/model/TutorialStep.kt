package com.nayibit.compose_tutorial.model

import androidx.compose.ui.geometry.Rect
import com.nayibit.compose_tutorial.util.LabelPosition

data class TutorialStep(
    val rect: Rect = Rect.Zero,
    val description: String = "",
    val labelPosition: LabelPosition
)
