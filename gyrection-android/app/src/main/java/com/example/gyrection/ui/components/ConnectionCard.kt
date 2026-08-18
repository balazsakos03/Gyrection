package com.example.gyrection.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun ConnectionCard(
    defaultIp: String,
    isConnected: Boolean,
    onConnectClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var ip by remember { mutableStateOf(defaultIp) }

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
                        text = "PC Kapcsolat",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Gray
                    )
                    Text(
                        text = if (isConnected) "Csatlakozva" else "Nincs kapcsolat",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isConnected) Color(0xFF4CAF50) else Color(0xFFF44336)
                    )
                }
            }

            OutlinedTextField(
                value = ip,
                onValueChange = { ip = it },
                enabled = !isConnected,
                singleLine = true,
                label = { Text("PC IP címe (Wi-Fi)") },
                placeholder = { Text("pl. 192.168.1.7") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    android.util.Log.d("GyrectionDebug", "Csatlakozás kattintva -> $ip")
                    onConnectClick(ip)
                },
                enabled = !isConnected && ip.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(if (isConnected) "Aktív" else "Csatlakozás")
            }
        }
    }
}