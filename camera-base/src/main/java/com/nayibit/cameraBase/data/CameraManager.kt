package com.nayibit.cameraBase.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.UseCase
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.nayibit.cameraBase.data.analysis.FrameAnalyzer
import com.nayibit.cameraBase.data.error.CameraError
import com.nayibit.cameraBase.data.result.CaptureResult
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraManager(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner
) {
    private var cameraProvider: ProcessCameraProvider? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalysis: ImageAnalysis? = null
    private var analysisExecutor: ExecutorService? = null
    private var camera: Camera? = null

    fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED

    fun setFlashMode(mode: Int) {
        imageCapture?.flashMode = mode
        // Torch (continuous LED) mirrors the flash button state so the preview reflects it
        camera?.cameraControl?.enableTorch(mode == ImageCapture.FLASH_MODE_ON)
    }

    /**
     * @param frameAnalyzer Optional. When non-null, binds a CameraX `ImageAnalysis` use case
     *   alongside preview/capture and delivers frames to it on a dedicated background thread —
     *   for real-time or throttled (see [com.nayibit.cameraBase.data.analysis.throttled]) object
     *   detection / CV / ML processing. Left null (the default), no analyzer is bound and this
     *   behaves exactly as before: zero extra cost for preview/capture-only consumers.
     */
    fun startPreview(
        previewView: PreviewView,
        lensFacing: Int = CameraSelector.LENS_FACING_BACK,
        flashMode: Int = ImageCapture.FLASH_MODE_OFF,
        frameAnalyzer: FrameAnalyzer? = null,
        onReady: () -> Unit = {},
        onError: (CameraError) -> Unit
    ) {
        if (!hasPermission()) {
            onError(CameraError.PermissionDenied)
            return
        }

        val providerFuture = ProcessCameraProvider.getInstance(context)
        providerFuture.addListener({
            try {
                val provider = providerFuture.get()
                cameraProvider = provider
                bindCamera(provider, previewView, lensFacing, flashMode, frameAnalyzer, onReady, onError)
            } catch (e: Exception) {
                onError(CameraError.InitFailed(e))
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun bindCamera(
        provider: ProcessCameraProvider,
        previewView: PreviewView,
        lensFacing: Int,
        flashMode: Int,
        frameAnalyzer: FrameAnalyzer?,
        onReady: () -> Unit,
        onError: (CameraError) -> Unit
    ) {
        try {
            val preview = Preview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setFlashMode(flashMode)
                .build()

            val selector = CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build()

            val useCases = mutableListOf<UseCase>(preview, imageCapture!!)

            if (frameAnalyzer != null) {
                val executor = Executors.newSingleThreadExecutor().also { analysisExecutor = it }
                imageAnalysis = ImageAnalysis.Builder()
                    // Drop stale frames instead of queuing them: bounds latency when the analyzer
                    // (a model, OpenCV, ...) is slower than the camera's frame rate.
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { it.setAnalyzer(executor) { image -> frameAnalyzer.analyze(image) } }
                useCases += imageAnalysis!!
            }

            provider.unbindAll()
            camera = provider.bindToLifecycle(lifecycleOwner, selector, *useCases.toTypedArray())
            // Restore torch if flash was ON before a camera flip
            if (flashMode == ImageCapture.FLASH_MODE_ON) {
                camera?.cameraControl?.enableTorch(true)
            }
            onReady()
        } catch (e: Exception) {
            onError(CameraError.BindingFailed(e))
        }
    }

    /**
     * Captures a frame in memory. Result delivers on the main thread.
     * Caller MUST call [androidx.camera.core.ImageProxy.close] after processing the image,
     * otherwise the camera pipeline will stall.
     */
    fun takePicture(onResult: (CaptureResult) -> Unit) {
        val capture = imageCapture ?: run {
            onResult(CaptureResult.Failure(IllegalStateException("Call startPreview() first")))
            return
        }

        capture.takePicture(
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    onResult(CaptureResult.ImageCaptured(image))
                }
                override fun onError(e: ImageCaptureException) {
                    onResult(CaptureResult.Failure(e))
                }
            }
        )
    }

    /**
     * Captures and saves the image to [file]. Result delivers on the main thread.
     */
    fun saveToFile(file: File?, onResult: (CaptureResult) -> Unit) {
        val capture = imageCapture ?: run {
            onResult(CaptureResult.Failure(IllegalStateException("Call startPreview() first")))
            return
        }
        val resolveFile = file ?: File(context.filesDir, "IMG_${System.currentTimeMillis()}.jpg")


        val options = ImageCapture.OutputFileOptions.Builder(resolveFile).build()

        capture.takePicture(
            options,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    onResult(CaptureResult.FileSaved(output.savedUri ?: Uri.fromFile(file)))
                }
                override fun onError(e: ImageCaptureException) {
                    onResult(CaptureResult.Failure(e))
                }
            }
        )
    }

    /**
     * Captures and saves to the app's cache directory as a JPEG.
     * File is named by timestamp so repeated calls never overwrite each other.
     * Result delivers on the main thread.
     */
    fun saveToCache(onResult: (CaptureResult) -> Unit) {
        val cacheDir = context.cacheDir.resolve("camera").also { it.mkdirs() }
        val file = File(cacheDir, "capture_${System.currentTimeMillis()}.jpg")
        saveToFile(file, onResult)
    }

    fun stopPreview() {
        camera?.cameraControl?.enableTorch(false)
        cameraProvider?.unbindAll()
        imageAnalysis?.clearAnalyzer()
        analysisExecutor?.shutdown()
        analysisExecutor = null
        imageAnalysis = null
        cameraProvider = null
        imageCapture = null
        camera = null
    }
}