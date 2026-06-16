package com.nayibit.android_core.camera.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class CameraAction(
    val icon: ImageVector,
    val contentDescription: String,
    val alignment: Alignment,
    val onClick: () -> Unit
)

/**
 * Camera overlay with a top bar (close/flip) and bottom bar (capture).
 * Pass [extraActions] to place additional buttons at any [Alignment] in the Box.
 */
@Composable
fun BoxScope.CameraControls(
    onCapture: () -> Unit,
    onFlip: (() -> Unit)? = null,
    onClose: (() -> Unit)? = null,
    extraActions: List<CameraAction> = emptyList(),
    captureButtonSize: Dp = 72.dp,
    overlayColor: Color = Color.Black.copy(alpha = 0.3f)
) {
    // Top bar
    Row(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .fillMaxWidth()
            .background(overlayColor)
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onClose != null) {
            CameraIconButton(Icons.Default.Close, "Close", onClick = onClose)
        } else {
            Box(Modifier.size(48.dp))
        }

        if (onFlip != null) {
            CameraIconButton(Icons.Default.Refresh, "Flip camera", onClick = onFlip)
        } else {
            Box(Modifier.size(48.dp))
        }
    }

    // Bottom bar
    Row(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .background(overlayColor)
            .navigationBarsPadding()
            .padding(vertical = 24.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CaptureButton(size = captureButtonSize, onClick = onCapture)
    }

    // Extra buttons at custom positions
    extraActions.forEach { action ->
        Box(
            modifier = Modifier
                .align(action.alignment)
                .padding(16.dp)
        ) {
            CameraIconButton(action.icon, action.contentDescription, onClick = action.onClick)
        }
    }
}

@Composable
fun CaptureButton(
    size: Dp = 72.dp,
    ringColor: Color = Color.White,
    innerColor: Color = Color.White,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .border(4.dp, ringColor, CircleShape)
            .padding(6.dp)
            .clip(CircleShape)
            .background(innerColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    )
}

@Composable
fun CameraIconButton(
    icon: ImageVector,
    contentDescription: String,
    tint: Color = Color.White,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.4f))
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
    }
}
