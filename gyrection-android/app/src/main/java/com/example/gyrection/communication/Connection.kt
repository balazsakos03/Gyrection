package com.example.gyrection.communication

import com.example.gyrection.protocol.GyrectionPacket

interface Connection {
    /**
     * Felépíti a kapcsolatot a megadott célra (host:port).
     * UDP esetén ez a küldő socket inicializálását jelenti.
     */
    fun connect(host: String, port: Int): Boolean
    fun disconnect()
    fun send(packet: GyrectionPacket): Boolean
    fun isConnected(): Boolean
}