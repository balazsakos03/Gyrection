package com.example.gyrection.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gyrection.controller.ControllerState
import com.example.gyrection.sensor.Orientation
import com.example.gyrection.ui.components.SensitivityPreset
import com.example.gyrection.ui.components.SensitivitySelector

@Composable
fun OrientationCard(
    orientation: Orientation,
    controllerState: ControllerState,
    steeringPreset: SensitivityPreset,
    tiltPreset: SensitivityPreset,
    onSteeringPresetChange: (SensitivityPreset) -> Unit,
    onTiltPresetChange: (SensitivityPreset) -> Unit,
    onCalibrateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Raw sensor values
                Column(modifier = Modifier.weight(1f)) {
                    Text("Sensor data", color = Color.Gray, style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Steering (Z): %.1f°".format(orientation.rotZ), fontWeight = FontWeight.Medium)
                    Text("Throttle/Brake (Y): %.1f°".format(orientation.rotY), fontWeight = FontWeight.Medium)
                }

                // Output controller values with progress bars
                Column(modifier = Modifier.weight(1.5f)) {
                    Text("Gamepad output", color = Color.Gray, style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    TelemetryBar(label = "Steering", value = controllerState.steering, isBiDirectional = true)
                    TelemetryBar(label = "Throttle", value = controllerState.throttle, color = Color(0xFF4CAF50))
                    TelemetryBar(label = "Brake", value = controllerState.brake, color = Color(0xFFF44336))
                }
            }

            // Calibration button in the bottom-left corner (reachable with the left thumb)
            Button(
                onClick = onCalibrateClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Calibrate Center", fontWeight = FontWeight.Bold)
            }

            // Sensitivity selectors
            SensitivitySelector(
                label = "Steering sensitivity (Z rotation)",
                currentPreset = steeringPreset,
                onPresetChange = onSteeringPresetChange
            )

            SensitivitySelector(
                label = "Throttle/Brake sensitivity (Y tilt)",
                currentPreset = tiltPreset,
                onPresetChange = onTiltPresetChange
            )
        }
    }
}

@Composable
fun TelemetryBar(
    label: String,
    value: Float,
    isBiDirectional: Boolean = false,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Text(text = label, modifier = Modifier.width(70.dp), style = MaterialTheme.typography.bodySmall)
        Box(
            modifier = Modifier
                .weight(1f)
                .height(12.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.DarkGray)
        ) {
            if (isBiDirectional) {
                // Make sure the weight is never exactly 0 or 1
                val safeWidth = Math.abs(value).coerceIn(0.001f, 0.999f)
                val remainder = (1f - safeWidth).coerceAtLeast(0.001f)

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.width(2.dp).fillMaxHeight().background(Color.White))
                }
                Row(modifier = Modifier.fillMaxSize()) {
                    if (value < 0) {
                        Spacer(modifier = Modifier.weight(remainder))
                        Box(modifier = Modifier.weight(safeWidth).fillMaxHeight().background(Color.White))
                        Spacer(modifier = Modifier.weight(1f))
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                        Box(modifier = Modifier.weight(safeWidth).fillMaxHeight().background(Color.White))
                        Spacer(modifier = Modifier.weight(remainder))
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(value.coerceAtLeast(0f)) // fillMaxWidth handles zero
                        .fillMaxHeight()
                        .background(color)
                )
            }
        }
    }
}