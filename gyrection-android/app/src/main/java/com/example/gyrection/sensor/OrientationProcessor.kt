package com.example.gyrection.sensor

import kotlin.math.atan2
import kotlin.math.asin
import kotlin.math.abs

data class Orientation(
    val rotX: Float = 0f,
    val rotY: Float = 0f,
    val rotZ: Float = 0f
)

class OrientationProcessor {

    private var calibrationQuaternion: Quaternion? = null

    fun calibrate(quaternion: Quaternion) {
        calibrationQuaternion = quaternion.normalized()
    }

    fun isCalibrated(): Boolean {
        return calibrationQuaternion != null
    }

    fun resetCalibration() {
        calibrationQuaternion = null
    }

    fun process(quaternion: Quaternion): Orientation {
        val reference = calibrationQuaternion ?: return Orientation()
        val current = quaternion.normalized()

        // Relative rotation compared to the calibrated reference point
        val relative = reference.inverse() * current

        return quaternionToOrientation(relative)
    }

    private fun normalizeAngle(angleDeg: Float): Float {
        var angle = angleDeg
        while (angle > 180f) angle -= 360f
        while (angle < -180f) angle += 360f
        return angle
    }

    private fun quaternionToOrientation(q: Quaternion): Orientation {
        // Rotation around the X axis (roll)
        val sinr_cosp = 2f * (q.w * q.x + q.y * q.z)
        val cosr_cosp = 1f - 2f * (q.x * q.x + q.y * q.y)
        val rotX = atan2(sinr_cosp, cosr_cosp)

        // Rotation around the Y axis (pitch -> throttle/brake)
        val sinp = 2f * (q.w * q.y - q.z * q.x)
        val rotY = if (abs(sinp) >= 1f) {
            Math.copySign(Math.PI.toFloat() / 2f, sinp)
        } else {
            asin(sinp)
        }

        // Rotation around the Z axis (yaw -> steering)
        val siny_cosp = 2f * (q.w * q.z + q.x * q.y)
        val cosy_cosp = 1f - 2f * (q.y * q.y + q.z * q.z)
        val rotZ = atan2(siny_cosp, cosy_cosp)

        return Orientation(
            rotX = normalizeAngle(Math.toDegrees(rotX.toDouble()).toFloat()),
            rotY = normalizeAngle(Math.toDegrees(rotY.toDouble()).toFloat()),
            rotZ = normalizeAngle(Math.toDegrees(rotZ.toDouble()).toFloat())
        )
    }
}