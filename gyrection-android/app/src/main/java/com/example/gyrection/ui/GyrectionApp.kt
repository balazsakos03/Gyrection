package com.example.gyrection.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.gyrection.controller.ControllerState
import com.example.gyrection.sensor.Orientation
import com.example.gyrection.sensor.Quaternion
import com.example.gyrection.ui.components.ConnectionCard
import com.example.gyrection.ui.components.HandbrakeButton
import com.example.gyrection.ui.components.OrientationCard
import com.example.gyrection.ui.components.SensitivityPreset

private val DarkColors = darkColorScheme(
    background = Color(0xFF111315),
    surface = Color(0xFF1E2124),
    primary = Color(0xFF4CAF50),
    secondary = Color(0xFF03A9F4),
    error = Color(0xFFF44336)
)

@Composable
fun GyrectionApp(
    quaternion: Quaternion,
    orientation: Orientation,
    controllerState: ControllerState,
    isConnected: Boolean,
    steeringPreset: SensitivityPreset,
    tiltPreset: SensitivityPreset,
    onConnectClick: () -> Unit,
    onCalibrateClick: () -> Unit,
    onSteeringPresetChange: (SensitivityPreset) -> Unit,
    onTiltPresetChange: (SensitivityPreset) -> Unit,
    onHandbrakeChange: (Boolean) -> Unit
) {
    MaterialTheme(colorScheme = DarkColors) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            // Landscape layout: split the screen into two columns (Left: info/buttons, Right: handbrake)
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left column (weight 1f fills the remaining space)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    ConnectionCard(
                        isConnected = isConnected,
                        onConnectClick = onConnectClick
                    )

                    OrientationCard(
                        orientation = orientation,
                        controllerState = controllerState,
                        steeringPreset = steeringPreset,
                        tiltPreset = tiltPreset,
                        onSteeringPresetChange = onSteeringPresetChange,
                        onTiltPresetChange = onTiltPresetChange,
                        onCalibrateClick = onCalibrateClick,
                        modifier = Modifier.weight(1f).padding(top = 12.dp)
                    )
                }

                // Right column (fixed ratio: one huge handbrake button)
                HandbrakeButton(
                    onPressChange = onHandbrakeChange,
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.35f) // 35% of the screen width
                )
            }
        }
    }
}