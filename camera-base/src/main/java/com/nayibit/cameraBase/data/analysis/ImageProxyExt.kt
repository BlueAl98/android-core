package com.nayibit.cameraBase.data.analysis

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream

/**
 * Converts a YUV_420_888 camera frame to an upright RGB [Bitmap] — the input shape most
 * OpenCV/TFLite/ML Kit code expects instead of raw YUV planes.
 *
 * Does NOT close [this]; your [FrameAnalyzer] still owns that. Uses a JPEG round-trip, which is
 * simple and dependency-free but not the cheapest possible conversion — if you're running a model
 * that accepts YUV/`Image` directly (e.g. ML Kit's `InputImage.fromMediaImage`), prefer feeding it
 * `image.image` + `image.imageInfo.rotationDegrees` directly and skip this conversion.
 *
 * @param applyRotation Rotates the bitmap upright using [ImageProxy.getImageInfo]'s rotation
 *                       degrees. Leave true unless you're handling rotation yourself downstream.
 */
fun ImageProxy.toBitmap(applyRotation: Boolean = true): Bitmap {
    require(format == ImageFormat.YUV_420_888) { "Unsupported image format: $format" }

    val yBuffer = planes[0].buffer
    val uBuffer = planes[1].buffer
    val vBuffer = planes[2].buffer

    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    val nv21 = ByteArray(ySize + uSize + vSize)
    yBuffer.get(nv21, 0, ySize)
    vBuffer.get(nv21, ySize, vSize)
    uBuffer.get(nv21, ySize + vSize, uSize)

    val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, width, height), 100, out)
    val bytes = out.toByteArray()
    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

    val rotation = imageInfo.rotationDegrees
    return if (applyRotation && rotation != 0) {
        val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
        Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    } else {
        bitmap
    }
}
