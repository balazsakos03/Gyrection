package com.example.gyrection.controller

import com.example.gyrection.sensor.Orientation
import kotlin.math.abs

data class ControllerState(
    val steering: Float = 0f,
    val throttle: Float = 0f,
    val brake: Float = 0f,
    val handbrake: Boolean = false
)

class ControllerMapper(
    private val steeringDeadZone: Float = 1.0f,
    private val tiltDeadZone: Float = 5.0f,
    private val maxPhoneTilt: Float = 45.0f
){
    fun map(orientation: Orientation, handbrake: Boolean): ControllerState {
        val steeringAngle = orientation.rotZ
        val tiltAngle = orientation.rotY

        val throttle: Float
        val brake: Float

        // THROTTLE / BRAKE logic (inverted):
        // Tilting forward (positive tiltAngle) -> throttle
        // Tilting backward (negative tiltAngle) -> brake
        if(abs(tiltAngle) < tiltDeadZone){
            throttle = 0f
            brake = 0f
        } else if(tiltAngle > 0){
            throttle = (tiltAngle / maxPhoneTilt).coerceIn(0f, 1f) // throttle increases
            brake = 0f
        } else {
            brake = (-tiltAngle / maxPhoneTilt).coerceIn(0f, 1f) // brake increases
            throttle = 0f
        }

        // STEERING logic (inverted):
        // -steeringAngle makes the result negative to the left, positive to the right
        val steering = if (abs(steeringAngle) > steeringDeadZone){
            (-steeringAngle / maxPhoneTilt).coerceIn(-1f, 1f)
        } else {
            0f
        }

        return ControllerState(
            steering = steering,
            throttle = throttle,
            brake = brake,
            handbrake = handbrake
        )
    }
}