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

@Composable
fun OrientationCard(
    orientation: Orientation,
    controllerState: ControllerState,
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
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Nyers szenzor adatok
                Column(modifier = Modifier.weight(1f)) {
                    Text("Szenzor adatok", color = Color.Gray, style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Kormány (Z): %.1f°".format(orientation.rotZ), fontWeight = FontWeight.Medium)
                    Text("Gáz/Fék (Y): %.1f°".format(orientation.rotY), fontWeight = FontWeight.Medium)
                }

                // Kimeneti kontroller adatok progress barokkal
                Column(modifier = Modifier.weight(1.5f)) {
                    Text("Játékvezérlő kimenet", color = Color.Gray, style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    TelemetryBar(label = "Steering", value = controllerState.steering, isBiDirectional = true)
                    TelemetryBar(label = "Throttle", value = controllerState.throttle, color = Color(0xFF4CAF50))
                    TelemetryBar(label = "Brake", value = controllerState.brake, color = Color(0xFFF44336))
                }
            }

            // Kalibrációs gomb a bal alsó sarokban (bal hüvelykujjal elérhető)
            Button(
                onClick = onCalibrateClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Középállás Kalibrálása", fontWeight = FontWeight.Bold)
            }
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
                // Biztosítjuk, hogy a súly sosem lesz pontosan 0 vagy 1
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
                        .fillMaxWidth(value.coerceAtLeast(0f)) // fillMaxWidth bírja a nullát
                        .fillMaxHeight()
                        .background(color)
                )
            }
        }
    }
}