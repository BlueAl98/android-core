package com.nayibit.croppingImage.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

private val DefaultAccent = Color(0xFFE6127E)

@Immutable
data class ImageCropperColors(
    val background: Color = Color.Black,
    val overlay: Color = Color.Black.copy(alpha = 0.55f),
    val frameLine: Color = DefaultAccent,
    val gridLine: Color = Color.White.copy(alpha = 0.7f),
    val cornerHandle: Color = Color.White,
    val cornerHandleBorder: Color = DefaultAccent,
    val zoomPreviewBackground: Color = Color.Black.copy(alpha = 0.5f),
    val panelBackground: Color = Color(0xFFF8F8F9),
    val dividerColor: Color = Color(0xFFE3E3E6),
    val resetContainer: Color = Color.Transparent,
    val resetIcon: Color = DefaultAccent,
    val resetLabelText: Color = Color(0xFF1C1C1F),
    val cropContainer: Color = DefaultAccent,
    val cropIcon: Color = Color.White,
    val cropLabelText: Color = Color.White,
    val labelText: Color = Color(0xFF1C1C1F)
) {
    companion object {
        fun defaults() = ImageCropperColors()

        fun defaultsDark() = ImageCropperColors(
            overlay = Color.Black.copy(alpha = 0.65f),
            panelBackground = Color(0xFF19191C),
            dividerColor = Color(0xFF2E2E33),
            resetLabelText = Color.White,
            labelText = Color.White
        )
    }
}
