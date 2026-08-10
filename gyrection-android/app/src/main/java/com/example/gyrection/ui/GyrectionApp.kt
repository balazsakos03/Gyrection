package com.example.gyrection.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gyrection.sensor.Orientation
import com.example.gyrection.sensor.Quaternion

@Composable
fun GyrectionApp(
    quaternion: Quaternion,
    orientation: Orientation
) {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF111315)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // Header
                Column {
                    Text(
                        text = "Gyrection",
                        color = Color(0xFFF1F3F5),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Phone motion controller",
                        color = Color(0xFF858B94),
                        fontSize = 14.sp
                    )
                }

                // PC Connection
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1B1E22)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "PC CONNECTION",
                            color = Color(0xFF858B94),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Not connected",
                            color = Color(0xFFF1F3F5),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = "Connect this phone to the Gyrection PC application.",
                            color = Color(0xFF858B94),
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = {
                                // USB connection will be implemented later
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF64B5F6)
                            )
                        ) {
                            Text(
                                text = "Connect",
                                color = Color.Black
                            )
                        }
                    }
                }

                // Orientation
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1B1E22)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "ORIENTATION",
                            color = Color(0xFF858B94),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )

                        QuaternionRow("W", quaternion.w)
                        QuaternionRow("X", quaternion.x)
                        QuaternionRow("Y", quaternion.y)
                        QuaternionRow("Z", quaternion.z)

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "RELATIVE ROTATION",
                            color = Color(0xFF858B94),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )

                        OrientationRow(
                            label = "Pitch",
                            value = orientation.pitch
                        )

                        OrientationRow(
                            label = "Yaw",
                            value = orientation.yaw
                        )
                    }
                }

                // Controller
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1B1E22)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "CONTROLLER",
                            color = Color(0xFF858B94),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Steering: ${formatAngle(orientation.yaw)}",
                            color = Color(0xFFE8EAED),
                            fontSize = 16.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Throttle: ${formatAngle(orientation.pitch)}",
                            color = Color(0xFFE8EAED),
                            fontSize = 16.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                // Handbrake will be implemented later
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2A2E34)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "HANDBRAKE",
                                color = Color(0xFFF1F3F5),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuaternionRow(
    label: String,
    value: Float
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.width(40.dp),
            color = Color(0xFF626872),
            fontSize = 14.sp
        )

        Text(
            text = String.format("%.4f", value),
            color = Color(0xFFE8EAED),
            fontSize = 14.sp
        )
    }
}

@Composable
fun OrientationRow(
    label: String,
    value: Float
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.width(80.dp),
            color = Color(0xFF626872),
            fontSize = 14.sp
        )

        Text(
            text = formatAngle(value),
            color = Color(0xFFE8EAED),
            fontSize = 14.sp
        )
    }
}

fun formatAngle(value: Float): String {
    return String.format("%.2f°", value)
}