package com.example.gyrection

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.gyrection.sensor.Orientation
import com.example.gyrection.sensor.OrientationProcessor
import com.example.gyrection.sensor.Quaternion
import com.example.gyrection.sensor.SensorManager
import com.example.gyrection.ui.GyrectionApp

class MainActivity : ComponentActivity() {

    private lateinit var sensorManager: SensorManager
    private lateinit var orientationProcessor: OrientationProcessor

    private var quaternion by mutableStateOf(Quaternion())
    private var orientation by mutableStateOf(Orientation())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        orientationProcessor = OrientationProcessor()

        sensorManager = SensorManager(this) { newQuaternion ->

            quaternion = newQuaternion

            orientation = orientationProcessor.process(
                newQuaternion
            )
        }

        setContent {
            GyrectionApp(
                quaternion = quaternion,
                orientation = orientation
            )
        }
    }

    override fun onResume() {
        super.onResume()
        sensorManager.start()
    }

    override fun onPause() {
        super.onPause()
        sensorManager.stop()
    }
}