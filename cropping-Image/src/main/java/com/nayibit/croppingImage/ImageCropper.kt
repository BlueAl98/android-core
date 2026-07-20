package com.nayibit.croppingImage

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalConfiguration
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
import com.nayibit.croppingImage.utils.findActivity
import com.nayibit.croppingImage.utils.isInsideQuadrant
import kotlin.math.absoluteValue

private const val DEFAULT_MIN_CROP_WIDTH_PERCENT = 0.1f
private const val DEFAULT_MIN_CROP_HEIGHT_PERCENT = 0.1f
private const val CORNER_TOUCH_RADIUS = 80f
private const val CROP_TOO_SMALL_MESSAGE = "El área seleccionada es demasiado pequeña"
private const val ACTION_PANEL_WEIGHT = 0.2f
private const val IMAGE_AREA_WEIGHT = 1f - ACTION_PANEL_WEIGHT

@Composable
fun ImageCropper(
    imageBitmap: Bitmap,
    modifier: Modifier = Modifier,
    initialPoints: Map<CropCorner, Point>? = null,
    colors: ImageCropperColors = ImageCropperColors.defaults(),
    lockToLandscape: Boolean = true,
    minCropWidthPercent: Float = DEFAULT_MIN_CROP_WIDTH_PERCENT,
    minCropHeightPercent: Float = DEFAULT_MIN_CROP_HEIGHT_PERCENT,
    onCropConfirmed: (CropResult) -> Unit,
    onCropRejected: (String) -> Unit = {}
) {
    require(minCropWidthPercent in 0f..1f) { "minCropWidthPercent must be between 0 and 1" }
    require(minCropHeightPercent in 0f..1f) { "minCropHeightPercent must be between 0 and 1" }

    val context = LocalContext.current

    if (lockToLandscape) {
        DisposableEffect(Unit) {
            val activity = context.findActivity()
            val previousOrientation = activity?.requestedOrientation
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            onDispose {
                activity?.requestedOrientation = previousOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }
    }

    var imageSize by remember { mutableStateOf(IntSize.Zero) }
    var imageOffset by remember { mutableStateOf(Offset.Zero) }
    var lastImageSize by remember { mutableStateOf(IntSize.Zero) }
    var lastImageOffset by remember { mutableStateOf(Offset.Zero) }

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

    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    val screenWidth = remember { context.resources.displayMetrics.widthPixels }
    val screenHeight = remember { context.resources.displayMetrics.heightPixels }
    // In landscape the action panel reserves ACTION_PANEL_WEIGHT of the width, so the
    // image only has IMAGE_AREA_WEIGHT of the screen to fit into.
    val canvasWidth = if (isLandscape) (screenWidth * IMAGE_AREA_WEIGHT) / 1.5 else screenWidth / 1.5
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

    val cropGestureModifier = Modifier.pointerInput(Unit) {
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

    val imageAreaContent: @Composable BoxScope.() -> Unit = {
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
                    } else if (lastImageSize != newImageSize || lastImageOffset != imageOffset) {
                        // The box was re-measured (e.g. the forced landscape rotation settled
                        // after the first layout pass) — rescale the existing selection into
                        // the new bounds instead of leaving it at stale coordinates that can
                        // fall outside the new, possibly narrower, image area.
                        if (lastImageSize.width > 0 && lastImageSize.height > 0) {
                            fun rescale(offset: Offset): Offset {
                                val relX = (offset.x - lastImageOffset.x) / lastImageSize.width.toFloat()
                                val relY = (offset.y - lastImageOffset.y) / lastImageSize.height.toFloat()
                                return Offset(
                                    imageOffset.x + relX * newImageSize.width,
                                    imageOffset.y + relY * newImageSize.height
                                )
                            }
                            topLeft = rescale(topLeft)
                            topRight = rescale(topRight)
                            bottomLeft = rescale(bottomLeft)
                            bottomRight = rescale(bottomRight)
                            initialTopLeft = rescale(initialTopLeft)
                            initialTopRight = rescale(initialTopRight)
                            initialBottomLeft = rescale(initialBottomLeft)
                            initialBottomRight = rescale(initialBottomRight)
                        }
                    }

                    lastImageSize = newImageSize
                    lastImageOffset = imageOffset
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

            // Rule-of-thirds guide lines, dashed, inside the selection.
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f)
            fun lerp(a: Offset, b: Offset, t: Float) = Offset(
                a.x + (b.x - a.x) * t,
                a.y + (b.y - a.y) * t
            )
            for (t in floatArrayOf(1f / 3f, 2f / 3f)) {
                drawLine(
                    color = colors.gridLine,
                    start = lerp(topLeft, bottomLeft, t),
                    end = lerp(topRight, bottomRight, t),
                    strokeWidth = 2f,
                    pathEffect = dashEffect
                )
                drawLine(
                    color = colors.gridLine,
                    start = lerp(topLeft, topRight, t),
                    end = lerp(bottomLeft, bottomRight, t),
                    strokeWidth = 2f,
                    pathEffect = dashEffect
                )
            }

            drawLine(colors.frameLine, topLeft, topRight, strokeWidth = 5f)
            drawLine(colors.frameLine, topRight, bottomRight, strokeWidth = 5f)
            drawLine(colors.frameLine, bottomRight, bottomLeft, strokeWidth = 5f)
            drawLine(colors.frameLine, bottomLeft, topLeft, strokeWidth = 5f)

            val handleFill = 11f
            val handleBorder = 13f
            val handles = listOf(topLeft, topRight, bottomLeft, bottomRight)
            handles.forEach { handle ->
                drawCircle(colors.cornerHandle, handleFill, handle)
                drawCircle(colors.cornerHandleBorder, handleBorder, handle, style = Stroke(width = 3f))
            }
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
                        color = colors.cornerHandleBorder,
                        radius = 8f,
                        center = overlayCenter
                    )
                }
            }
        }
    }

    val actionPanelContent: @Composable (Modifier) -> Unit = { panelModifier ->
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = colors.panelBackground,
            shadowElevation = 6.dp,
            modifier = panelModifier
        ) {
            Column {
                CropActionTile(
                    icon = Icons.Default.Refresh,
                    label = "Restablecer",
                    iconTint = colors.resetIcon,
                    labelColor = colors.resetLabelText,
                    containerColor = colors.resetContainer,
                    onClick = {
                        topLeft = initialTopLeft
                        topRight = initialTopRight
                        bottomLeft = initialBottomLeft
                        bottomRight = initialBottomRight
                    }
                )
                HorizontalDivider(color = colors.dividerColor)
                CropActionTile(
                    icon = Icons.Default.Crop,
                    label = "Recortar imagen",
                    iconTint = colors.cropIcon,
                    labelColor = colors.cropLabelText,
                    containerColor = colors.cropContainer,
                    onClick = {
                        val minRectWidth = imageSize.width * minCropWidthPercent
                        val minRectHeight = imageSize.height * minCropHeightPercent
                        val selectedWidth = (topRight.x - topLeft.x).absoluteValue
                        val selectedHeight = (bottomLeft.y - topLeft.y).absoluteValue
                        val selectedWidthOpposite = (bottomRight.x - bottomLeft.x).absoluteValue
                        val selectedHeightOpposite = (bottomRight.y - topRight.y).absoluteValue
                        if (selectedWidth >= minRectWidth && selectedHeight >= minRectHeight &&
                            selectedWidthOpposite >= minRectWidth && selectedHeightOpposite >= minRectHeight
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
                    }
                )
            }
        }
    }

    if (isLandscape) {
        // Landscape: the image and the action panel are laid out side by side so the
        // panel reliably reserves ACTION_PANEL_WEIGHT of the width instead of floating
        // on top of the image.
        Row(
            modifier = modifier
                .fillMaxSize()
                .background(colors.background)
        ) {
            Box(
                modifier = Modifier
                    .weight(IMAGE_AREA_WEIGHT)
                    .fillMaxHeight()
                    .then(cropGestureModifier)
            ) {
                imageAreaContent()
            }
            Column(
                modifier = Modifier
                    .weight(ACTION_PANEL_WEIGHT)
                    .fillMaxHeight()
                    .padding(WindowInsets.navigationBars.asPaddingValues())
                    .padding(horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                actionPanelContent(Modifier.fillMaxWidth())
            }
        }
    } else {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(colors.background)
                .then(cropGestureModifier)
        ) {
            imageAreaContent()

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(WindowInsets.navigationBars.asPaddingValues())
                    .padding(end = 32.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Center
                ) {
                    actionPanelContent(Modifier.width(150.dp))
                }
            }
        }
    }
}

@Composable
private fun CropActionTile(
    icon: ImageVector,
    label: String,
    iconTint: Color,
    labelColor: Color,
    containerColor: Color,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(containerColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 18.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconTint,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, color = labelColor, textAlign = TextAlign.Center)
    }
}
