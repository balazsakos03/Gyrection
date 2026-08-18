package com.example.gyrection.communication

import com.example.gyrection.protocol.GyrectionPacket

interface Connection {
    fun connect(): Boolean
    fun disconnect()
    fun send(packet: GyrectionPacket): Boolean
    fun isConnected(): Boolean
}