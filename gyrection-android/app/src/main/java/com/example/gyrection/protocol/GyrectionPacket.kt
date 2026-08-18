package com.example.gyrection.protocol

import com.example.gyrection.sensor.Quaternion
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Phone → PC UDP packet (little-endian).
 *
 * Byte layout (41 bytes):
 * ```
 * [0]     magic                 0x01
 * [1..4]  steering              f32  (-1..1)
 * [5..8]  throttle              f32  (0..1)
 * [9..12] brake                 f32  (0..1)
 * [13..16] handbrake            f32  (0.0 or 1.0)
 * [17..20] qw (quaternion W)
 * [21..24] qx (quaternion X)
 * [25..28] qy (quaternion Y)
 * [29..32] qz (quaternion Z)
 * [33..36] pitch (rotY, throttle/brake axis)
 * [37..40] yaw   (rotZ, steering axis)
 * ```
 */
data class GyrectionPacket(
    val steering: Float,
    val throttle: Float,
    val brake: Float,
    val handbrake: Boolean,
    val quaternion: Quaternion = Quaternion(),
    val pitch: Float = 0f,
    val yaw: Float = 0f
) {
    fun toBytes(): ByteArray {
        val q = quaternion.normalized()
        val buffer = ByteBuffer.allocate(41).order(ByteOrder.LITTLE_ENDIAN)
        buffer.put(0x01)
        buffer.putFloat(steering)
        buffer.putFloat(throttle)
        buffer.putFloat(brake)
        buffer.putFloat(if (handbrake) 1f else 0f)
        buffer.putFloat(q.w)
        buffer.putFloat(q.x)
        buffer.putFloat(q.y)
        buffer.putFloat(q.z)
        buffer.putFloat(pitch)
        buffer.putFloat(yaw)
        return buffer.array()
    }
}