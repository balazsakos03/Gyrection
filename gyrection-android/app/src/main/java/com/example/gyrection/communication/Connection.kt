package com.example.gyrection.communication

import com.example.gyrection.protocol.GyrectionPacket

interface Connection {
    /**
     * Builds the connection to the given target (host:port).
     * For UDP, this initializes the sending socket.
     */
    fun connect(host: String, port: Int): Boolean
    fun disconnect()
    fun send(packet: GyrectionPacket): Boolean
    fun isConnected(): Boolean
}