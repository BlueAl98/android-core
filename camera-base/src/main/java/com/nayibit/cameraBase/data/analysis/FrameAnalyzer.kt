package com.nayibit.cameraBase.data.analysis

import androidx.camera.core.ImageProxy
import java.util.concurrent.atomic.AtomicLong

/**
 * Receives live camera frames for real-time processing (OpenCV, TFLite, ML Kit, a custom model, ...).
 * Runs on a background executor, never the main thread. Caller MUST call [ImageProxy.close] when
 * done with the frame — including on every branch of your own error handling — otherwise CameraX
 * stalls the pipeline and stops delivering new frames.
 *
 * camera-base intentionally has no opinion on what analyzes the frame: it only hands you the raw
 * [ImageProxy] (see [com.nayibit.cameraBase.data.analysis.toBitmap] to convert it). Wire your actual
 * CV/ML library in the consuming app, or in a separate module that depends on this one — never add
 * that dependency to camera-base itself, or every consumer of this library pays for it.
 */
fun interface FrameAnalyzer {
    fun analyze(image: ImageProxy)
}

/**
 * Wraps [this] so it runs at most once every [intervalMs]. Frames arriving before the interval
 * elapses are closed immediately (not queued, not dropped-but-blocking) so the CameraX pipeline
 * never stalls waiting on a slow model. Use this to turn a per-frame analyzer into a "real-time
 * with some delay" one, e.g. run a heavy detector at 2-3 fps instead of the full preview frame rate.
 */
fun FrameAnalyzer.throttled(intervalMs: Long): FrameAnalyzer {
    val lastRun = AtomicLong(0L)
    return FrameAnalyzer { image ->
        val now = System.currentTimeMillis()
        if (now - lastRun.get() >= intervalMs) {
            lastRun.set(now)
            analyze(image)
        } else {
            image.close()
        }
    }
}
