package com.example.gyrection.communication

import com.example.gyrection.protocol.GyrectionPacket
import java.io.OutputStream
import java.net.Socket

class WifiConnection(
    private val host: String = "127.0.0.1",
    private val port: Int = 9999
) : Connection {

    private var socket: Socket? = null
    private var outputStream: OutputStream? = null

    override fun connect(): Boolean = try {
        socket = Socket(host, port)
        outputStream = socket?.getOutputStream()
        true
    } catch (e: Exception) {
        false
    }

    override fun disconnect() {
        outputStream?.close()
        socket?.close()
        outputStream = null
        socket = null
    }

    override fun send(packet: GyrectionPacket): Boolean = try {
        outputStream?.write(packet.toBytes())
        outputStream?.flush()
        true
    } catch (e: Exception) {
        false
    }

    override fun isConnected(): Boolean =
        socket?.isConnected == true && socket?.isClosed == false
}