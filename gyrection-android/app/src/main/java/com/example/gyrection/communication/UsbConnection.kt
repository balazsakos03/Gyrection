package com.example.gyrection.communication

import com.example.gyrection.protocol.GyrectionPacket

class UsbConnection : Connection {
    override fun connect(host: String, port: Int): Boolean = false
    override fun disconnect() {}
    override fun send(packet: GyrectionPacket): Boolean = false
    override fun isConnected(): Boolean = false
}