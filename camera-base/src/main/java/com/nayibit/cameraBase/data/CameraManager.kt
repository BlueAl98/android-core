package com.nayibit.cameraBase.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.nayibit.cameraBase.data.error.CameraError
import com.nayibit.cameraBase.data.result.CaptureResult
import java.io.File

class CameraManager(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner
) {
    private var cameraProvider: ProcessCameraProvider? = null
    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null

    fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED

    fun setFlashMode(mode: Int) {
        imageCapture?.flashMode = mode
        // Torch (continuous LED) mirrors the flash button state so the preview reflects it
        camera?.cameraControl?.enableTorch(mode == ImageCapture.FLASH_MODE_ON)
    }

    fun startPreview(
        previewView: PreviewView,
        lensFacing: Int = CameraSelector.LENS_FACING_BACK,
        flashMode: Int = ImageCapture.FLASH_MODE_OFF,
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
                bindCamera(provider, previewView, lensFacing, flashMode, onReady, onError)
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

            provider.unbindAll()
            camera = provider.bindToLifecycle(lifecycleOwner, selector, preview, imageCapture!!)
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
        cameraProvider = null
        imageCapture = null
        camera = null
    }
}