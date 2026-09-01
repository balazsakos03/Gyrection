package com.example.gyrection.controller

import com.example.gyrection.sensor.Orientation
import kotlin.math.abs

data class ControllerState(
    val steering: Float = 0f,
    val throttle: Float = 0f,
    val brake: Float = 0f,
    val handbrake: Boolean = false
)

/**
 * Maps phone orientation to controller output.
 *
 * @param steeringDeadZone degrees of rotation around Z to ignore (center dead zone)
 * @param tiltDeadZone     degrees of tilt around Y to ignore (center dead zone)
 * @param steeringMaxTilt  degrees of Z rotation at which steering reaches 100%
 * @param tiltMaxTilt      degrees of Y tilt at which throttle/brake reaches 100%
 */
class ControllerMapper(
    private val steeringDeadZone: Float = 1.0f,
    private val tiltDeadZone: Float = 5.0f,
    private val steeringMaxTilt: Float = 35f,
    private val tiltMaxTilt: Float = 45f
) {
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
            throttle = (tiltAngle / tiltMaxTilt).coerceIn(0f, 1f)
            brake = 0f
        } else {
            brake = (-tiltAngle / tiltMaxTilt).coerceIn(0f, 1f)
            throttle = 0f
        }

        // STEERING logic (inverted):
        // -steeringAngle makes the result negative to the left, positive to the right
        val steering = if (abs(steeringAngle) > steeringDeadZone){
            (-steeringAngle / steeringMaxTilt).coerceIn(-1f, 1f)
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