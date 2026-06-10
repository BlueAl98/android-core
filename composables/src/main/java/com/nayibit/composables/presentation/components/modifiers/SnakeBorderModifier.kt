package com.nayibit.composables.presentation.components.modifiers

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Draws an animated snake line that travels around the contour of [shape].
 *
 * @param shape        The shape to trace — works with any Shape including custom ones.
 * @param brush        Color or gradient of the snake. Defaults to solid white.
 * @param strokeWidth  Thickness of the line.
 * @param segmentFraction  Fraction of the total perimeter that is visible at once (0f–1f).
 * @param durationMillis   Time in ms for one full loop around the shape.
 */
fun Modifier.snakeBorder(
    shape: Shape = RoundedCornerShape(8.dp),
    brush: Brush = SolidColor(Color.Red),
    strokeWidth: Dp = 2.dp,
    segmentFraction: Float = 0.25f,
    durationMillis: Int = 1500
): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "snakeBorder")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis, easing = LinearEasing)),
        label = "snakePhase"
    )

    drawWithContent {
        drawContent()

        val outline = shape.createOutline(size, layoutDirection, this)
        val shapePath = Path().apply {
            when (outline) {
                is Outline.Generic -> addPath(outline.path)
                is Outline.Rounded -> addRoundRect(outline.roundRect)
                is Outline.Rectangle -> {
                    val r = outline.rect
                    moveTo(r.left, r.top)
                    lineTo(r.right, r.top)
                    lineTo(r.right, r.bottom)
                    lineTo(r.left, r.bottom)
                    close()
                }
            }
        }

        val pathMeasure = PathMeasure()
        pathMeasure.setPath(shapePath, false)
        val totalLength = pathMeasure.length
        val segmentLength = totalLength * segmentFraction.coerceIn(0f, 1f)
        val startDistance = phase * totalLength
        val endDistance = startDistance + segmentLength

        val segmentPath = Path()
        if (endDistance <= totalLength) {
            pathMeasure.getSegment(startDistance, endDistance, segmentPath, true)
        } else {
            // wrap around the end back to the start
            pathMeasure.getSegment(startDistance, totalLength, segmentPath, true)
            val wrapPath = Path()
            pathMeasure.getSegment(0f, endDistance - totalLength, wrapPath, true)
            segmentPath.addPath(wrapPath)
        }

        drawPath(
            path = segmentPath,
            brush = brush,
            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        )
    }
}
