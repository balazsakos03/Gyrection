package com.example.gyrection.sensor

import kotlin.math.atan2
import kotlin.math.asin
import kotlin.math.sqrt

data class Orientation(
    val pitch: Float = 0f,
    val yaw: Float = 0f
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
        val reference = calibrationQuaternion
            ?: return Orientation()

        val current = quaternion.normalized()

        // Relative rotation:
        // Q_relative = inverse(reference) * current
        val relative = reference.inverse() * current

        return quaternionToOrientation(relative)
    }

    private fun quaternionToOrientation(
        q: Quaternion
    ): Orientation {

        val pitchSin = 2f * (q.w * q.x + q.y * q.z)

        val pitch = if (kotlin.math.abs(pitchSin) >= 1f) {
            Math.copySign(
                Math.PI.toFloat() / 2f,
                pitchSin
            )
        } else {
            asin(pitchSin)
        }

        val yaw = atan2(
            2f * (q.w * q.y - q.z * q.x),
            1f - 2f * (q.x * q.x + q.y * q.y)
        )

        return Orientation(
            pitch = Math.toDegrees(pitch.toDouble()).toFloat(),
            yaw = Math.toDegrees(yaw.toDouble()).toFloat()
        )
    }
}