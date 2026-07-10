package com.nayibit.croppingImage

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.nayibit.croppingImage.model.CropCorner
import com.nayibit.croppingImage.model.CropResult
import com.nayibit.croppingImage.model.ImageCropperColors
import com.nayibit.croppingImage.model.Point
import com.nayibit.croppingImage.utils.OffsetSaver
import com.nayibit.croppingImage.utils.buildCropResult
import com.nayibit.croppingImage.utils.isInsideQuadrant
import kotlin.math.absoluteValue

private const val MIN_RECT_WIDTH = 600f
private const val MIN_RECT_HEIGHT = 200f
private const val CORNER_TOUCH_RADIUS = 80f
private const val CROP_TOO_SMALL_MESSAGE = "El área seleccionada es demasiado pequeña"

@Composable
fun ImageCropper(
    imageBitmap: Bitmap,
    modifier: Modifier = Modifier,
    initialPoints: Map<CropCorner, Point>? = null,
    colors: ImageCropperColors = ImageCropperColors.defaults(),
    onCropConfirmed: (CropResult) -> Unit,
    onCropRejected: (String) -> Unit = {}
) {
    val context = LocalContext.current

    var imageSize by remember { mutableStateOf(IntSize.Zero) }
    var imageOffset by remember { mutableStateOf(Offset.Zero) }

    // Saveable so the selection survives configuration changes instead of resetting.
    var topLeft by rememberSaveable(stateSaver = OffsetSaver) { mutableStateOf(Offset.Zero) }
    var topRight by rememberSaveable(stateSaver = OffsetSaver) { mutableStateOf(Offset.Zero) }
    var bottomLeft by rememberSaveable(stateSaver = OffsetSaver) { mutableStateOf(Offset.Zero) }
    var bottomRight by rememberSaveable(stateSaver = OffsetSaver) { mutableStateOf(Offset.Zero) }

    var initialTopLeft by rememberSaveable(stateSaver = OffsetSaver) { mutableStateOf(Offset.Zero) }
    var initialTopRight by rememberSaveable(stateSaver = OffsetSaver) { mutableStateOf(Offset.Zero) }
    var initialBottomLeft by rememberSaveable(stateSaver = OffsetSaver) { mutableStateOf(Offset.Zero) }
    var initialBottomRight by rememberSaveable(stateSaver = OffsetSaver) { mutableStateOf(Offset.Zero) }

    var isCornerPressed by remember { mutableStateOf(false) }
    var activeCorner by remember { mutableStateOf<CropCorner?>(null) }
    var activeCornerOffset by remember { mutableStateOf(Offset.Zero) }

    val screenWidth = remember { context.resources.displayMetrics.widthPixels }
    val screenHeight = remember { context.resources.displayMetrics.heightPixels }
    val canvasWidth = screenWidth / 1.5
    val canvasHeight = screenHeight / 1.5

    val aspectRatio = imageBitmap.width.toFloat() / imageBitmap.height.toFloat()
    val newWidth: Int
    val newHeight: Int
    if (imageBitmap.width > canvasWidth || imageBitmap.height > canvasHeight) {
        if (aspectRatio > 1) {
            newWidth = canvasWidth.toInt()
            newHeight = (canvasWidth / aspectRatio).toInt()
        } else {
            newHeight = canvasHeight.toInt()
            newWidth = (canvasHeight * aspectRatio).toInt()
        }
    } else {
        newWidth = imageBitmap.width
        newHeight = imageBitmap.height
    }
    val scaledBitmap = Bitmap.createScaledBitmap(imageBitmap, newWidth, newHeight, true)
    val imageBit = scaledBitmap.asImageBitmap()

    val initialMargin = 50f

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        when {
                            (offset - topLeft).getDistance() < CORNER_TOUCH_RADIUS -> {
                                isCornerPressed = true
                                activeCorner = CropCorner.TOP_LEFT
                                activeCornerOffset = topLeft
                            }
                            (offset - topRight).getDistance() < CORNER_TOUCH_RADIUS -> {
                                isCornerPressed = true
                                activeCorner = CropCorner.TOP_RIGHT
                                activeCornerOffset = topRight
                            }
                            (offset - bottomLeft).getDistance() < CORNER_TOUCH_RADIUS -> {
                                isCornerPressed = true
                                activeCorner = CropCorner.BOTTOM_LEFT
                                activeCornerOffset = bottomLeft
                            }
                            (offset - bottomRight).getDistance() < CORNER_TOUCH_RADIUS -> {
                                isCornerPressed = true
                                activeCorner = CropCorner.BOTTOM_RIGHT
                                activeCornerOffset = bottomRight
                            }
                        }
                    },
                    onDragEnd = {
                        isCornerPressed = false
                        activeCorner = null
                    },
                    onDragCancel = {
                        isCornerPressed = false
                        activeCorner = null
                    },
                    onDrag = { _, dragAmount ->
                        val (x, y) = dragAmount
                        when (activeCorner) {
                            CropCorner.TOP_LEFT -> {
                                val newTopLeft = topLeft + Offset(x, y)
                                if (isInsideQuadrant(newTopLeft, imageOffset, imageSize, CropCorner.TOP_LEFT)) {
                                    topLeft = newTopLeft
                                    activeCornerOffset = newTopLeft
                                }
                            }
                            CropCorner.TOP_RIGHT -> {
                                val newTopRight = topRight + Offset(x, y)
                                if (isInsideQuadrant(newTopRight, imageOffset, imageSize, CropCorner.TOP_RIGHT)) {
                                    topRight = newTopRight
                                    activeCornerOffset = newTopRight
                                }
                            }
                            CropCorner.BOTTOM_LEFT -> {
                                val newBottomLeft = bottomLeft + Offset(x, y)
                                if (isInsideQuadrant(newBottomLeft, imageOffset, imageSize, CropCorner.BOTTOM_LEFT)) {
                                    bottomLeft = newBottomLeft
                                    activeCornerOffset = newBottomLeft
                                }
                            }
                            CropCorner.BOTTOM_RIGHT -> {
                                val newBottomRight = bottomRight + Offset(x, y)
                                if (isInsideQuadrant(newBottomRight, imageOffset, imageSize, CropCorner.BOTTOM_RIGHT)) {
                                    bottomRight = newBottomRight
                                    activeCornerOffset = newBottomRight
                                }
                            }
                            else -> Unit
                        }
                    }
                )
            }
    ) {
        Image(
            bitmap = imageBit,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { layoutCoordinates ->
                    val layoutSize = layoutCoordinates.size
                    val imageAspectRatio = imageBitmap.width.toFloat() / imageBitmap.height
                    val boxAspectRatio = layoutSize.width.toFloat() / layoutSize.height
                    val newImageSize = if (imageAspectRatio > boxAspectRatio) {
                        IntSize(width = layoutSize.width, height = (layoutSize.width / imageAspectRatio).toInt())
                    } else {
                        IntSize(width = (layoutSize.height * imageAspectRatio).toInt(), height = layoutSize.height)
                    }
                    imageSize = newImageSize
                    imageOffset = Offset(
                        (layoutSize.width - newImageSize.width) / 2f,
                        (layoutSize.height - newImageSize.height) / 2f
                    )

                    val scaleX = newImageSize.width.toFloat() / imageBitmap.width
                    val scaleY = newImageSize.height.toFloat() / imageBitmap.height

                    if (topLeft == Offset.Zero) {
                        fun scaledInitial(corner: CropCorner): Offset? =
                            initialPoints?.get(corner)?.let {
                                Offset(
                                    imageOffset.x + (it.x.toFloat() * scaleX),
                                    imageOffset.y + (it.y.toFloat() * scaleY)
                                )
                            }

                        topLeft = scaledInitial(CropCorner.TOP_LEFT)
                            ?: Offset(imageOffset.x + initialMargin, imageOffset.y + initialMargin + 50)
                        topRight = scaledInitial(CropCorner.TOP_RIGHT)
                            ?: Offset(imageOffset.x + newImageSize.width - initialMargin, imageOffset.y + initialMargin + 50)
                        bottomLeft = scaledInitial(CropCorner.BOTTOM_LEFT)
                            ?: Offset(imageOffset.x + initialMargin, imageOffset.y + newImageSize.height - (initialMargin + 50))
                        bottomRight = scaledInitial(CropCorner.BOTTOM_RIGHT)
                            ?: Offset(imageOffset.x + newImageSize.width - initialMargin, imageOffset.y + newImageSize.height - (initialMargin + 50))

                        initialTopLeft = topLeft
                        initialTopRight = topRight
                        initialBottomLeft = bottomLeft
                        initialBottomRight = bottomRight
                    }
                }
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val selectionPath = Path().apply {
                moveTo(topLeft.x, topLeft.y)
                lineTo(topRight.x, topRight.y)
                lineTo(bottomRight.x, bottomRight.y)
                lineTo(bottomLeft.x, bottomLeft.y)
                close()
            }
            clipPath(selectionPath, clipOp = ClipOp.Difference) {
                drawRect(colors.overlay, blendMode = BlendMode.SrcOver)
            }
            drawLine(colors.frameLine, topLeft, topRight, strokeWidth = 5f)
            drawLine(colors.frameLine, topRight, bottomRight, strokeWidth = 5f)
            drawLine(colors.frameLine, bottomRight, bottomLeft, strokeWidth = 5f)
            drawLine(colors.frameLine, bottomLeft, topLeft, strokeWidth = 5f)
            drawCircle(colors.cornerHandle, 9f, topLeft)
            drawCircle(colors.cornerHandle, 9f, topRight)
            drawCircle(colors.cornerHandle, 9f, bottomLeft)
            drawCircle(colors.cornerHandle, 9f, bottomRight)
        }

        if (isCornerPressed) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.Center)
                    .clip(CircleShape)
                    .background(colors.zoomPreviewBackground)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val zoomFactor = 2f
                    val overlayCenter = Offset(size.width / 2f, size.height / 2f)

                    val scaleX = imageSize.width.toFloat() / imageBit.width
                    val scaleY = imageSize.height.toFloat() / imageBit.height

                    val rawX = (activeCornerOffset.x - imageOffset.x) / scaleX
                    val rawY = (activeCornerOffset.y - imageOffset.y) / scaleY

                    val imageDrawWidth = imageBit.width * zoomFactor
                    val imageDrawHeight = imageBit.height * zoomFactor

                    val imageDrawOffset = Offset(
                        x = overlayCenter.x - rawX * zoomFactor,
                        y = overlayCenter.y - rawY * zoomFactor
                    )

                    drawImage(
                        image = imageBit,
                        dstSize = IntSize(imageDrawWidth.toInt(), imageDrawHeight.toInt()),
                        dstOffset = IntOffset(imageDrawOffset.x.toInt(), imageDrawOffset.y.toInt())
                    )

                    drawCircle(
                        color = colors.cornerHandle,
                        radius = 8f,
                        center = overlayCenter
                    )
                }
            }
        }

        Row(
            modifier = modifier
                .fillMaxSize()
                .padding(WindowInsets.navigationBars.asPaddingValues())
                .padding(end = 40.dp)
        ) {
            Column(
                modifier.fillMaxSize(),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(onClick = {
                        topLeft = initialTopLeft
                        topRight = initialTopRight
                        bottomLeft = initialBottomLeft
                        bottomRight = initialBottomRight
                    }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = colors.resetIcon,
                            modifier = modifier.size(65.dp)
                        )
                    }
                    Text(text = "Restablecer", color = colors.labelText)
                    Spacer(modifier = modifier.weight(0.8f))
                    IconButton(onClick = {
                        val selectedWidth = (topRight.x - topLeft.x).absoluteValue
                        val selectedHeight = (bottomLeft.y - topLeft.y).absoluteValue
                        val selectedWidthOpposite = (bottomRight.x - bottomLeft.x).absoluteValue
                        val selectedHeightOpposite = (bottomRight.y - topRight.y).absoluteValue
                        if (selectedWidth >= MIN_RECT_WIDTH && selectedHeight >= MIN_RECT_HEIGHT &&
                            selectedWidthOpposite >= MIN_RECT_WIDTH && selectedHeightOpposite >= MIN_RECT_HEIGHT
                        ) {
                            onCropConfirmed(
                                buildCropResult(
                                    topLeft, topRight, bottomLeft, bottomRight,
                                    imageBitmap, imageSize, imageOffset
                                )
                            )
                        } else {
                            onCropRejected(CROP_TOO_SMALL_MESSAGE)
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Camera,
                            contentDescription = null,
                            tint = colors.cropIcon,
                            modifier = modifier.size(65.dp)
                        )
                    }
                    Text(text = "Recortar", color = colors.labelText)
                    Spacer(modifier = modifier.weight(1f))
                }
            }
        }
    }
}
