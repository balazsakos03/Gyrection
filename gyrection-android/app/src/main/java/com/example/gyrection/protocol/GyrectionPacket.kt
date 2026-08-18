package com.example.gyrection.protocol

import java.nio.ByteBuffer
import java.nio.ByteOrder

data class GyrectionPacket(
    val steering: Float,
    val throttle: Float,
    val brake: Float,
    val handbrake: Boolean
) {
    fun toBytes(): ByteArray {
        val buffer = ByteBuffer.allocate(17).order(ByteOrder.LITTLE_ENDIAN)
        buffer.put(0x01)
        buffer.putFloat(steering)
        buffer.putFloat(throttle)
        buffer.putFloat(brake)
        buffer.putFloat(if (handbrake) 1f else 0f)
        return buffer.array()
    }
}