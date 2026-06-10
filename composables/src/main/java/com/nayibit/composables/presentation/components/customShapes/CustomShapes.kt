package com.nayibit.composables.presentation.components.customShapes

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

// Perfect circle regardless of component size — always uses min(width, height) as diameter.
class UniformCircleShape : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val radius = minOf(size.width, size.height) / 2f
        val cx = size.width / 2f
        val cy = size.height / 2f
        return Outline.Rounded(
            RoundRect(
                left = cx - radius,
                top = cy - radius,
                right = cx + radius,
                bottom = cy + radius,
                cornerRadius = CornerRadius(radius)
            )
        )
    }
}

// 5-point star by default. Adjust points and innerRatio for different looks.
class StarShape(
    private val points: Int = 5,
    private val innerRatio: Float = 0.45f
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val outer = minOf(cx, cy)
        val inner = outer * innerRatio
        val step = Math.PI / points
        val path = Path()
        for (i in 0 until points * 2) {
            val r = if (i % 2 == 0) outer else inner
            val angle = i * step - Math.PI / 2
            val x = (cx + r * cos(angle)).toFloat()
            val y = (cy + r * sin(angle)).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        return Outline.Generic(path)
    }
}

// Regular polygon: triangle(3), pentagon(5), hexagon(6), octagon(8), etc.
class PolygonShape(private val sides: Int) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = minOf(cx, cy)
        val path = Path()
        for (i in 0 until sides) {
            val angle = -Math.PI / 2 + 2 * Math.PI * i / sides
            val x = (cx + r * cos(angle)).toFloat()
            val y = (cy + r * sin(angle)).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        return Outline.Generic(path)
    }
}

// Diamond / rhombus — 4 corner points.
class DiamondShape : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path().apply {
            moveTo(size.width / 2f, 0f)
            lineTo(size.width, size.height / 2f)
            lineTo(size.width / 2f, size.height)
            lineTo(0f, size.height / 2f)
            close()
        }
        return Outline.Generic(path)
    }
}

// Classic ticket with semicircle notches cut from left and right sides.
class TicketShape(private val notchRadius: Dp = 12.dp) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val r = with(density) { notchRadius.toPx() }
        val midY = size.height / 2f
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width, midY - r)
            arcTo(Rect(size.width - r, midY - r, size.width + r, midY + r), 270f, -180f, false)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            lineTo(0f, midY + r)
            arcTo(Rect(-r, midY - r, r, midY + r), 90f, -180f, false)
            close()
        }
        return Outline.Generic(path)
    }
}

// Shield — rounded top corners, straight sides, single point at the bottom.
class ShieldShape : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val w = size.width
        val h = size.height
        val cr = w * 0.2f
        val path = Path().apply {
            moveTo(w / 2f, h)
            lineTo(0f, h * 0.45f)
            cubicTo(0f, h * 0.2f, 0f, cr, cr, 0f)
            lineTo(w - cr, 0f)
            cubicTo(w, cr, w, h * 0.2f, w, h * 0.45f)
            close()
        }
        return Outline.Generic(path)
    }
}
