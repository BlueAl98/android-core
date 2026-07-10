package com.nayibit.croppingImage.utils

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import com.nayibit.croppingImage.model.CropCorner

internal fun isInsideQuadrant(
    point: Offset,
    imageOffset: Offset,
    imageSize: IntSize,
    quadrant: CropCorner
): Boolean {
    val halfWidth = imageSize.width / 2
    val halfHeight = imageSize.height / 2

    return when (quadrant) {
        CropCorner.TOP_LEFT -> {
            point.x in imageOffset.x..(imageOffset.x + halfWidth) &&
                    point.y in imageOffset.y..(imageOffset.y + halfHeight)
        }
        CropCorner.TOP_RIGHT -> {
            point.x in (imageOffset.x + halfWidth)..(imageOffset.x + imageSize.width) &&
                    point.y in imageOffset.y..(imageOffset.y + halfHeight)
        }
        CropCorner.BOTTOM_LEFT -> {
            point.x in imageOffset.x..(imageOffset.x + halfWidth) &&
                    point.y in (imageOffset.y + halfHeight)..(imageOffset.y + imageSize.height)
        }
        CropCorner.BOTTOM_RIGHT -> {
            point.x in (imageOffset.x + halfWidth)..(imageOffset.x + imageSize.width) &&
                    point.y in (imageOffset.y + halfHeight)..(imageOffset.y + imageSize.height)
        }
    }
}
