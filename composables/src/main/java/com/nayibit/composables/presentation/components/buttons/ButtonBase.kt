package com.nayibit.composables.presentation.components.buttons

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nayibit.composables.presentation.components.modifiers.snakeBorder

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
    snakeDurationMillis: Int = 1500
) {
    val resolvedModifier = if (snakeActive) {
        modifier.snakeBorder(
            shape = shape,
            brush = snakeBrush,
            strokeWidth = snakeStrokeWidth,
            segmentFraction = snakeSegmentFraction,
            durationMillis = snakeDurationMillis
        )
    } else {
        modifier
    }

    Button(
        onClick = onClick,
        modifier = resolvedModifier.fillMaxWidth(),
        enabled = enabled && !loading,
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
