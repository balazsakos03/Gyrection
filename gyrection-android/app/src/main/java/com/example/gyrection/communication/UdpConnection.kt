package com.example.gyrection.communication

import com.example.gyrection.protocol.GyrectionPacket
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

/**
 * UDP-alapú kapcsolat (Wi-Fi, low-latency szenzorstream).
 *
 * A `connect(host, port)` létrehozza a küldő socketet és beállítja a
 * célt (a PC IP-címét). A telefon ezután folyamatosan UDP datagramokat
 * küld a PC-re. UDP lévén nincs kézfogás, így a `connect()` szinte
 * mindig sikerül — a valódi "működést" a fogadó (PC) oldalon a beérkező
 * adatok jelzik majd.
 */
class UdpConnection : Connection {

    private var socket: DatagramSocket? = null
    private var address: InetAddress? = null
    private var port: Int = 0

    override fun connect(host: String, port: Int): Boolean = try {
        address = InetAddress.getByName(host.trim())
        this.port = port
        socket = DatagramSocket()
        true
    } catch (e: Exception) {
        // Rossz IP, nincs hálózat stb.
        socket = null
        address = null
        false
    }

    override fun disconnect() {
        socket?.close()
        socket = null
        address = null
    }

    override fun send(packet: GyrectionPacket): Boolean = try {
        val s = socket ?: return false
        val a = address ?: return false
        val bytes = packet.toBytes()
        val datagram = DatagramPacket(bytes, bytes.size, a, port)
        s.send(datagram)
        true
    } catch (e: Exception) {
        false
    }

    override fun isConnected(): Boolean =
        socket != null && socket?.isClosed == false && address != null
}