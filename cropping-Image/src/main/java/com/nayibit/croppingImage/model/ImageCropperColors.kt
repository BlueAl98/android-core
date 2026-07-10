package com.nayibit.croppingImage.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class ImageCropperColors(
    val background: Color = Color.Black,
    val overlay: Color = Color.Black.copy(alpha = 0.5f),
    val frameLine: Color = Color.Blue,
    val cornerHandle: Color = Color.Blue,
    val zoomPreviewBackground: Color = Color.Black.copy(alpha = 0.5f),
    val resetIcon: Color = Color.White,
    val cropIcon: Color = Color.White,
    val labelText: Color = Color.White
) {
    companion object {
        fun defaults() = ImageCropperColors()
    }
}
