package com.example.gyrection

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.example.gyrection.communication.Connection
import com.example.gyrection.communication.UdpConnection
import com.example.gyrection.controller.ControllerMapper
import com.example.gyrection.controller.ControllerState
import com.example.gyrection.protocol.GyrectionPacket
import com.example.gyrection.sensor.Orientation
import com.example.gyrection.sensor.OrientationProcessor
import com.example.gyrection.sensor.Quaternion
import com.example.gyrection.sensor.SensorManager
import com.example.gyrection.ui.GyrectionApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private val udpPort = 9999

    private lateinit var sensorManager: SensorManager
    private lateinit var orientationProcessor: OrientationProcessor
    private lateinit var controllerMapper: ControllerMapper
    private lateinit var connection: Connection

    private var quaternion by mutableStateOf(Quaternion())
    private var orientation by mutableStateOf(Orientation())
    private var controllerState by mutableStateOf(ControllerState())
    private var isHandbrakeActive by mutableStateOf(false)
    private var isConnected by mutableStateOf(false)

    // Az utoljára megadott PC IP-cím, hogy ne kelljen újra beírni
    private var lastIp by mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Kényszerített Landscape mód, hogy a telefon mindig kormany-szeruen alljon
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE

        orientationProcessor = OrientationProcessor()
        controllerMapper = ControllerMapper()
        connection = UdpConnection() // Wi-Fi, UDP datagramok a PC-re

        sensorManager = SensorManager(this) { newQuaternion ->
            quaternion = newQuaternion
            orientation = orientationProcessor.process(newQuaternion)
            controllerState = controllerMapper.map(orientation, isHandbrakeActive)

            if (isConnected) {
                val packet = GyrectionPacket(
                    steering = controllerState.steering,
                    throttle = controllerState.throttle,
                    brake = controllerState.brake,
                    handbrake = controllerState.handbrake,
                    quaternion = newQuaternion,
                    pitch = orientation.rotY,
                    yaw = orientation.rotZ
                )
                sendPacketAsync(packet)
            }
        }

        setContent {
            GyrectionApp(
                defaultIp = lastIp,
                quaternion = quaternion,
                orientation = orientation,
                controllerState = controllerState,
                isConnected = isConnected,
                onConnectClick = { ip -> connectToPc(ip) },
                onCalibrateClick = { orientationProcessor.calibrate(quaternion) },
                onHandbrakeChange = { pressed -> isHandbrakeActive = pressed }
            )
        }
    }

    private fun connectToPc(ip: String) {
        if (ip.isBlank() || isConnected) return
        lastIp = ip.trim()
        lifecycleScope.launch(Dispatchers.IO) {
            val success = connection.connect(ip.trim(), udpPort)
            withContext(Dispatchers.Main) {
                if (success) {
                    isConnected = true
                }
            }
        }
    }

    private fun sendPacketAsync(packet: GyrectionPacket) {
        lifecycleScope.launch(Dispatchers.IO) {
            connection.send(packet)
        }
    }

    override fun onResume() {
        super.onResume()
        sensorManager.start()
    }

    override fun onPause() {
        super.onPause()
        sensorManager.stop()
        connection.disconnect()
        isConnected = false
    }
}