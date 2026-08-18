package com.example.gyrection.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ConnectionCard(
    isConnected: Boolean,
    onConnectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PC Connection",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Gray
                    )
                    Text(
                        text = if (isConnected) "Connected" else "Not connected",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isConnected) Color(0xFF4CAF50) else Color(0xFFF44336)
                    )
                }

                Button(
                    onClick = {
                        android.util.Log.d("GyrectionDebug", "Connect clicked")
                        onConnectClick()
                    },
                    enabled = !isConnected,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(if (isConnected) "Active" else "Connect")
                }
            }

            Text(
                text = if (isConnected)
                    "Streaming sensor data to PC..."
                else
                    "The button automatically finds your PC on the network.",
                color = Color.Gray,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}