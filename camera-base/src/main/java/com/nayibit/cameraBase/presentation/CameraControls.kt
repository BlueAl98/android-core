package com.nayibit.cameraBase.presentation

import android.content.res.Configuration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.io.File

/**
 * Default camera overlay. All controls except capture are optional — pass null to hide.
 *
 * - [onFlip]       null → flip button hidden
 * - [onClose]      null → close button hidden
 * - [flashMode]    null → flash button hidden; non-null → cycles OFF → ON → AUTO on tap
 * - [onFlashToggle] called with the next [FlashMode]; update your state here
 *
 * Layout adapts automatically: top/bottom bars in portrait, right column in landscape.
 */
@Composable
fun BoxScope.CameraControls(
    scope: CameraScope,
    state: CameraBaseState,
    onClose: (() -> Unit)? = null,
    captureButtonSize: Dp = 72.dp,
    overlayColor: Color = Color.Black.copy(alpha = 0.35f)
) {
    val context = LocalContext.current
    val isLandscape =
        LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    val onCapture = {
        scope.saveToFile {}
    }
    val onFlip: () -> Unit = { scope.flipCamera() }
    val onFlashToggle: (FlashMode) -> Unit = { newMode ->
        state.flashMode = newMode
        scope.setFlashMode(newMode)
    }

    Box(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        if (isLandscape) {
            LandscapeLayout(
                onCapture = onCapture,
                onFlip = onFlip,
                onClose = onClose,
                flashMode = state.flashMode,
                onFlashToggle = onFlashToggle,
                captureButtonSize = captureButtonSize,
                overlayColor = overlayColor
            )
        } else {
            PortraitLayout(
                onCapture = onCapture,
                onFlip = onFlip,
                onClose = onClose,
                flashMode = state.flashMode,
                onFlashToggle = onFlashToggle,
                captureButtonSize = captureButtonSize,
                overlayColor = overlayColor
            )
        }
    }
}

// ── Portrait ─────────────────────────────────────────────────────────────────

@Composable
private fun BoxScope.PortraitLayout(
    onCapture: () -> Unit,
    onFlip: (() -> Unit)?,
    onClose: (() -> Unit)?,
    flashMode: FlashMode?,
    onFlashToggle: (FlashMode) -> Unit,
    captureButtonSize: Dp,
    overlayColor: Color
) {
    val hasTopBar = onClose != null || onFlip != null || flashMode != null

    // Top bar: Close (start) | Flash + Flip (end)
    if (hasTopBar) {
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .background(overlayColor)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onClose != null) {
                CameraIconButton(Icons.Default.Close, "Close", onClick = onClose)
            } else {
                Spacer(Modifier.size(48.dp))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (flashMode != null) {
                    FlashButton(flashMode, onFlashToggle)
                }
                if (onFlip != null) {
                    CameraIconButton(Icons.Filled.Cameraswitch, "Flip camera", onClick = onFlip)
                }
            }
        }
    }

    // Bottom bar: Capture centered
    Row(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .background(overlayColor)
            .padding(vertical = 28.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CaptureButton(size = captureButtonSize, onClick = onCapture)
    }
}

// ── Landscape ────────────────────────────────────────────────────────────────

@Composable
private fun BoxScope.LandscapeLayout(
    onCapture: () -> Unit,
    onFlip: (() -> Unit)?,
    onClose: (() -> Unit)?,
    flashMode: FlashMode?,
    onFlashToggle: (FlashMode) -> Unit,
    captureButtonSize: Dp,
    overlayColor: Color
) {
    // Right column: Close (top) | Flash + Flip (center) | Capture (bottom)
    Column(
        modifier = Modifier
            .align(Alignment.CenterEnd)
            .fillMaxHeight()
            .background(overlayColor)
            .padding(horizontal = 12.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top slot — Close
        if (onClose != null) {
            CameraIconButton(Icons.Default.Close, "Close", onClick = onClose)
        } else {
            Spacer(Modifier.size(48.dp))
        }

        // Center slot — Flash + Flip
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (flashMode != null) {
                FlashButton(flashMode, onFlashToggle)
            }
            if (onFlip != null) {
                CameraIconButton(Icons.Filled.Cameraswitch, "Flip camera", onClick = onFlip)
            }
        }

        // Bottom slot — Capture
        CaptureButton(size = captureButtonSize, onClick = onCapture)
    }
}

// ── Shared components ────────────────────────────────────────────────────────

@Composable
private fun FlashButton(mode: FlashMode, onToggle: (FlashMode) -> Unit) {
    CameraIconButton(
        icon = mode.icon,
        contentDescription = mode.label,
        tint = if (mode == FlashMode.OFF) Color.White.copy(alpha = 0.55f) else Color.Yellow,
        onClick = { onToggle(mode.next()) }
    )
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

// ── FlashMode icon/label extensions (UI concern, kept here) ──────────────────

private val FlashMode.icon: ImageVector
    get() = when (this) {
        FlashMode.OFF -> Icons.Filled.FlashOff
        FlashMode.ON -> Icons.Filled.FlashOn
        FlashMode.AUTO -> Icons.Filled.FlashAuto
    }

private val FlashMode.label: String
    get() = when (this) {
        FlashMode.OFF -> "Flash off"
        FlashMode.ON -> "Flash on"
        FlashMode.AUTO -> "Flash auto"
    }
