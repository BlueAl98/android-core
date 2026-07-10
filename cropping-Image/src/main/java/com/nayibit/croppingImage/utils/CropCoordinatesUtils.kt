package com.nayibit.croppingImage.utils

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import com.nayibit.croppingImage.model.CropCorner
import com.nayibit.croppingImage.model.CropResult
import com.nayibit.croppingImage.model.Point

internal fun buildCropResult(
    topLeft: Offset,
    topRight: Offset,
    bottomLeft: Offset,
    bottomRight: Offset,
    imageBitmap: Bitmap,
    imageSize: IntSize,
    imageOffset: Offset
): CropResult {
    val scaleX = imageBitmap.width.toFloat() / imageSize.width
    val scaleY = imageBitmap.height.toFloat() / imageSize.height

    fun toOriginal(offset: Offset) = floatArrayOf(
        (offset.x - imageOffset.x) * scaleX,
        (offset.y - imageOffset.y) * scaleY
    )

    val originalTopLeft = toOriginal(topLeft)
    val originalTopRight = toOriginal(topRight)
    val originalBottomLeft = toOriginal(bottomLeft)
    val originalBottomRight = toOriginal(bottomRight)

    val coordinates = floatArrayOf(
        originalTopLeft[0], originalTopLeft[1],
        originalTopRight[0], originalTopRight[1],
        originalBottomLeft[0], originalBottomLeft[1],
        originalBottomRight[0], originalBottomRight[1]
    )

    val points = mapOf(
        CropCorner.TOP_LEFT to Point(originalTopLeft[0].toDouble(), originalTopLeft[1].toDouble()),
        CropCorner.TOP_RIGHT to Point(originalTopRight[0].toDouble(), originalTopRight[1].toDouble()),
        CropCorner.BOTTOM_LEFT to Point(originalBottomLeft[0].toDouble(), originalBottomLeft[1].toDouble()),
        CropCorner.BOTTOM_RIGHT to Point(originalBottomRight[0].toDouble(), originalBottomRight[1].toDouble())
    )

    return CropResult(points, coordinates)
}
