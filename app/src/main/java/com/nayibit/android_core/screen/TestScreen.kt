package com.nayibit.android_core.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import com.nayibit.composables.presentation.components.customShapes.UniformCircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nayibit.composables.presentation.components.buttons.ButtonBase
import com.nayibit.composables.presentation.components.customShapes.DiamondShape
import com.nayibit.composables.presentation.components.customShapes.PolygonShape
import com.nayibit.composables.presentation.components.customShapes.ShieldShape
import com.nayibit.composables.presentation.components.customShapes.StarShape
import com.nayibit.composables.presentation.components.customShapes.TicketShape

@Composable
fun TestScreen() {
    Column(Modifier.fillMaxSize().padding(20.dp)) {

        Text("Normal")
        Spacer(Modifier.height(8.dp))
        ButtonBase(text = "Normal", onClick = {})

        Spacer(Modifier.height(16.dp))

        Text("Star (6 points)")
        Spacer(Modifier.height(8.dp))
        ButtonBase(
            text = "Najib",
            onClick = {},
            shape = StarShape(points = 6),
            backgroundColor = Color(0xFFFFCC00),
            modifier = Modifier.size(120.dp)
        )

        Spacer(Modifier.height(16.dp))

        Text("Hexagon (PolygonShape 6)")
        Spacer(Modifier.height(8.dp))
        ButtonBase(
            text = "Hex",
            onClick = {},
            shape = PolygonShape(sides = 6),
            backgroundColor = Color(0xFF6200EE),
            modifier = Modifier.size(120.dp)
        )

        Spacer(Modifier.height(16.dp))

        Text("Diamond")
        Spacer(Modifier.height(8.dp))
        ButtonBase(
            text = "♦",
            onClick = {},
            shape = DiamondShape(),
            backgroundColor = Color(0xFFE91E63),
            modifier = Modifier.size(120.dp)
        )

        Spacer(Modifier.height(16.dp))

        Text("Ticket")
        Spacer(Modifier.height(8.dp))
        ButtonBase(text = "VIP Pass", onClick = {}, shape = TicketShape())

        Spacer(Modifier.height(16.dp))

        Text("Uniform Circle")
        Spacer(Modifier.height(8.dp))
        ButtonBase(
            text = "Circle",
            onClick = {},
            shape = UniformCircleShape(),
            backgroundColor = Color(0xFF1565C0),
            modifier = Modifier.size(120.dp)
        )

    }
}

@Preview(showBackground = true)
@Composable
fun TestScreenPreview() {
    TestScreen()
}
