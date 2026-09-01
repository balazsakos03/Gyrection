package com.example.gyrection.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Sensitivity level preset for steering or tilt.
 */
enum class SensitivityPreset(
    val displayName: String,
    val maxDegrees: Float  // at how many degrees the output reaches 100%
) {
    LOW("Low", 60f),        // gentle / precise – needs a large motion
    MEDIUM("Medium", 35f),  // balanced default
    HIGH("High", 20f);      // sporty / aggressive – small motion = full output

    companion object {
        fun fromDegrees(d: Float): SensitivityPreset = entries.minByOrNull {
            kotlin.math.abs(it.maxDegrees - d)
        } ?: MEDIUM
    }
}

@Composable
fun SensitivitySelector(
    label: String,
    currentPreset: SensitivityPreset,
    onPresetChange: (SensitivityPreset) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            color = Color.Gray,
            style = MaterialTheme.typography.labelSmall
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            SensitivityPreset.entries.forEach { preset ->
                FilterChip(
                    selected = preset == currentPreset,
                    onClick = { onPresetChange(preset) },
                    label = {
                        Text(
                            text = preset.displayName,
                            fontWeight = if (preset == currentPreset) FontWeight.Bold else FontWeight.Normal,
                            fontSize = MaterialTheme.typography.labelSmall.fontSize
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}