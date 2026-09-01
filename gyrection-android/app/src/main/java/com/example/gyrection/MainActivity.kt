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
import com.example.gyrection.ui.components.SensitivityPreset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private val udpPort = 9999

    private lateinit var sensorManager: SensorManager
    private lateinit var orientationProcessor: OrientationProcessor
    private lateinit var connection: Connection

    private var quaternion by mutableStateOf(Quaternion())
    private var orientation by mutableStateOf(Orientation())
    private var controllerState by mutableStateOf(ControllerState())
    private var isHandbrakeActive by mutableStateOf(false)
    private var isConnected by mutableStateOf(false)

    // Sensitivity presets
    private var steeringPreset by mutableStateOf(SensitivityPreset.MEDIUM)
    private var tiltPreset by mutableStateOf(SensitivityPreset.MEDIUM)

    // The controller mapper must be recreated when presets change
    private var controllerMapper by mutableStateOf(
        ControllerMapper(
            steeringMaxTilt = SensitivityPreset.MEDIUM.maxDegrees,
            tiltMaxTilt = SensitivityPreset.MEDIUM.maxDegrees
        )
    )

    // A discovery request is already in progress; avoid overlapping calls
    private var discoverInProgress = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Forced landscape orientation so the phone always sits like a steering wheel
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE

        orientationProcessor = OrientationProcessor()
        connection = UdpConnection() // Wi-Fi, sends UDP datagrams to the PC

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
                quaternion = quaternion,
                orientation = orientation,
                controllerState = controllerState,
                isConnected = isConnected,
                steeringPreset = steeringPreset,
                tiltPreset = tiltPreset,
                onConnectClick = { autoConnect() },
                onCalibrateClick = { orientationProcessor.calibrate(quaternion) },
                onSteeringPresetChange = { preset ->
                    steeringPreset = preset
                    controllerMapper = ControllerMapper(
                        steeringMaxTilt = preset.maxDegrees,
                        tiltMaxTilt = tiltPreset.maxDegrees
                    )
                },
                onTiltPresetChange = { preset ->
                    tiltPreset = preset
                    controllerMapper = ControllerMapper(
                        steeringMaxTilt = steeringPreset.maxDegrees,
                        tiltMaxTilt = preset.maxDegrees
                    )
                },
                onHandbrakeChange = { pressed -> isHandbrakeActive = pressed }
            )
        }
    }

    /**
     * Auto-connect: finds the PC on the network via broadcast
     * (the user does not need to type an IP address), then connects.
     */
    private fun autoConnect() {
        if (isConnected || discoverInProgress) return
        discoverInProgress = true
        lifecycleScope.launch(Dispatchers.IO) {
            val udp = connection as? UdpConnection
            val ip = udp?.discover(udpPort, timeoutMs = 2000)
            val success = if (ip != null) connection.connect(ip, udpPort) else false

            withContext(Dispatchers.Main) {
                discoverInProgress = false
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
        // Automatically finds the PC on the network in the background
        autoConnect()
    }

    override fun onPause() {
        super.onPause()
        sensorManager.stop()
        connection.disconnect()
        isConnected = false
    }
}