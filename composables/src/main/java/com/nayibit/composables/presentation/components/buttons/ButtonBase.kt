package com.nayibit.composables.presentation.components.buttons

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nayibit.composables.presentation.components.modifiers.snakeBorder
import kotlinx.coroutines.launch
import kotlin.math.sqrt

enum class ButtonPressAnimation {
    None,
    Scale,   // subtle uniform shrink on press, smooth return
    Bounce,  // shrinks more, springs back with overshoot
    Squish   // flattens vertically and widens on press
}

@Composable
fun ButtonBase(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    loadingColor: Color? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.secondary,
    contentColor: Color = Color.White,
    disabledBackgroundColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.30f),
    disabledContentColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
    shape: Shape = RoundedCornerShape(8.dp),
    textStyle: TextStyle = MaterialTheme.typography.labelLarge,
    customContent: (@Composable () -> Unit)? = null,
    snakeActive: Boolean = false,
    snakeBrush: Brush = SolidColor(Color.Gray),
    snakeStrokeWidth: Dp = 3.dp,
    snakeSegmentFraction: Float = 0.25f,
    snakeDurationMillis: Int = 1500,
    pressAnimation: ButtonPressAnimation = ButtonPressAnimation.None,
    holdFillActive: Boolean = false,
    holdFillColor: Color = Color.White.copy(alpha = 0.25f),
    holdFillDurationMillis: Int = 1000,
    onHoldComplete: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scope = rememberCoroutineScope()

    // Press scale animation
    val bouncy = spring<Float>(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
    val smooth = spring<Float>(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)

    val scaleX by animateFloatAsState(
        targetValue = when {
            !isPressed -> 1f
            pressAnimation == ButtonPressAnimation.Scale -> 0.80f
            pressAnimation == ButtonPressAnimation.Bounce -> 0.90f
            pressAnimation == ButtonPressAnimation.Squish -> 1.06f
            else -> 1f
        },
        animationSpec = if (pressAnimation == ButtonPressAnimation.Bounce) bouncy else smooth,
        label = "pressScaleX"
    )
    val scaleY by animateFloatAsState(
        targetValue = when {
            !isPressed -> 1f
            pressAnimation == ButtonPressAnimation.Scale -> 0.80f
            pressAnimation == ButtonPressAnimation.Bounce -> 0.90f
            pressAnimation == ButtonPressAnimation.Squish -> 0.88f
            else -> 1f
        },
        animationSpec = if (pressAnimation == ButtonPressAnimation.Bounce) bouncy else smooth,
        label = "pressScaleY"
    )

    // Hold fill animation
    val fillProgress = remember { Animatable(0f) }

    if (holdFillActive) {
        LaunchedEffect(interactionSource) {
            interactionSource.interactions.collect { interaction ->
                when (interaction) {
                    is PressInteraction.Press -> scope.launch {
                        fillProgress.snapTo(0f)
                        fillProgress.animateTo(1f, tween(holdFillDurationMillis, easing = LinearEasing))
                        onHoldComplete?.invoke()
                    }
                    is PressInteraction.Release, is PressInteraction.Cancel -> {
                        fillProgress.stop()
                        scope.launch { fillProgress.animateTo(0f, tween(150)) }
                    }
                }
            }
        }
    }

    val resolvedModifier = modifier
        .then(if (snakeActive) Modifier.snakeBorder(
            shape = shape,
            brush = snakeBrush,
            strokeWidth = snakeStrokeWidth,
            segmentFraction = snakeSegmentFraction,
            durationMillis = snakeDurationMillis
        ) else Modifier)
        .then(if (holdFillActive) Modifier.drawWithContent {
            drawContent()
            val progress = fillProgress.value
            if (progress > 0f) {
                val outline = shape.createOutline(size, layoutDirection, this)
                val clipPath = Path().apply {
                    when (outline) {
                        is Outline.Generic -> addPath(outline.path)
                        is Outline.Rounded -> addRoundRect(outline.roundRect)
                        is Outline.Rectangle -> addRect(outline.rect)
                    }
                }
                clipPath(clipPath) {
                    val maxRadius = sqrt(
                        (size.width / 2f) * (size.width / 2f) +
                        (size.height / 2f) * (size.height / 2f)
                    )
                    drawCircle(
                        color = holdFillColor,
                        radius = maxRadius * progress,
                        center = Offset(size.width / 2f, size.height / 2f)
                    )
                }
            }
        } else Modifier)
        .then(if (pressAnimation != ButtonPressAnimation.None)
            Modifier.scale(scaleX = scaleX, scaleY = scaleY) else Modifier)

    Button(
        onClick = onClick,
        modifier = resolvedModifier.fillMaxWidth(),
        enabled = enabled && !loading,
        interactionSource = interactionSource,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor,
            disabledContainerColor = disabledBackgroundColor,
            disabledContentColor = disabledContentColor
        ),
        shape = shape
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = loadingColor ?: contentColor,
                strokeWidth = 2.dp
            )
        } else if (customContent != null) {
            customContent()
        } else {
            Text(text = text, style = textStyle)
        }
    }
}
