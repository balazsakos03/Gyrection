package com.example.gyrection.sensor

import kotlin.math.sqrt

data class Quaternion(
    val w: Float = 1f,
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f
) {

    fun normalized(): Quaternion {
        val magnitude = sqrt(
            w * w +
                    x * x +
                    y * y +
                    z * z
        )

        if (magnitude == 0f) {
            return Quaternion()
        }

        return Quaternion(
            w = w / magnitude,
            x = x / magnitude,
            y = y / magnitude,
            z = z / magnitude
        )
    }

    fun inverse(): Quaternion {
        val normalized = normalized()

        return Quaternion(
            w = normalized.w,
            x = -normalized.x,
            y = -normalized.y,
            z = -normalized.z
        )
    }

    operator fun times(other: Quaternion): Quaternion {
        return Quaternion(
            w = w * other.w -
                    x * other.x -
                    y * other.y -
                    z * other.z,

            x = w * other.x +
                    x * other.w +
                    y * other.z -
                    z * other.y,

            y = w * other.y -
                    x * other.z +
                    y * other.w +
                    z * other.x,

            z = w * other.z +
                    x * other.y -
                    y * other.x +
                    z * other.w
        )
    }
}