package com.nayibit.android_core.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nayibit.composables.presentation.components.buttons.ButtonBase
import com.nayibit.composables.presentation.components.customShapes.DiamondShape
import com.nayibit.composables.presentation.components.customShapes.PolygonShape
import com.nayibit.composables.presentation.components.customShapes.StarShape
import com.nayibit.composables.presentation.components.customShapes.UniformCircleShape

@Composable
fun TestScreen() {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text("Snake — solid color (default shape)")
        Spacer(Modifier.height(8.dp))
        ButtonBase(
            text = "Press me",
            onClick = {},
            backgroundColor = Color(0xFF1A237E),
            snakeActive = true
         //   snakeBrush = SolidColor(Color.Cyan),
           // snakeStrokeWidth = 3.dp
        )

        Spacer(Modifier.height(24.dp))

        Text("Snake — gradient on star")
        Spacer(Modifier.height(8.dp))
        ButtonBase(
            text = "Star",
            onClick = {},
            shape = StarShape(points = 5),
            backgroundColor = Color(0xFF311B92),
            modifier = Modifier.size(140.dp),
            snakeActive = true,
            snakeBrush = Brush.sweepGradient(listOf(Color.Magenta, Color.Yellow, Color.Cyan)),
            snakeStrokeWidth = 4.dp,
            snakeSegmentFraction = 0.3f,
            snakeDurationMillis = 2000
        )

        Spacer(Modifier.height(24.dp))

        Text("Snake — circle")
        Spacer(Modifier.height(8.dp))
        ButtonBase(
            text = "Go",
            onClick = {},
            shape = UniformCircleShape(),
            backgroundColor = Color(0xFF004D40),
            modifier = Modifier.size(120.dp),
            snakeActive = true,
            snakeBrush = Brush.linearGradient(listOf(Color.Green, Color.Yellow)),
            snakeStrokeWidth = 5.dp,
            snakeSegmentFraction = 0.2f,
            snakeDurationMillis = 1000
        )

        Spacer(Modifier.height(24.dp))

        Text("Snake off (reference)")
        Spacer(Modifier.height(8.dp))
        ButtonBase(text = "Normal", onClick = {})
        Spacer(Modifier.height(8.dp))
        ButtonBase(
            text = "♦",
            onClick = {},
            shape = DiamondShape(),
            backgroundColor = Color(0xFFE91E63),
            modifier = Modifier.size(120.dp)
        )
        Spacer(Modifier.height(8.dp))
        ButtonBase(
            text = "Hex",
            onClick = {},
            shape = PolygonShape(sides = 6),
            backgroundColor = Color(0xFF6200EE),
            modifier = Modifier.size(120.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TestScreenPreview() {
    TestScreen()
}
