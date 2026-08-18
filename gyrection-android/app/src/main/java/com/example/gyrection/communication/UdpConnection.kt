package com.example.gyrection.communication

import com.example.gyrection.protocol.GyrectionPacket
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

/**
 * UDP-alapú kapcsolat (Wi-Fi, low-latency szenzorstream).
 *
 * A `connect(host, port)` létrehozza a küldő socketet és beállítja a
 * célt (a PC IP-címét). Előtte a `discover()` képes automatikusan
 * megtalálni a PC-t a helyi hálózaton egy broadcast üzenettel — így a
 * felhasználónak nem kell kézzel beírnia az IP-címet.
 */
class UdpConnection : Connection {

    private var socket: DatagramSocket? = null
    private var address: InetAddress? = null
    private var port: Int = 0

    /**
     * Felfedezi a PC-t a hálózaton: broadcast "GYRECTION_DISCOVERY" üzenetet
     * küld, és vár a válaszra, amiben a PC elküldi az IP-címét.
     *
     * @return a megtalált PC IP-címe, vagy null, ha nem volt válasz a megadott időn belül
     */
    fun discover(port: Int, timeoutMs: Int = 2000): String? {
        val s = DatagramSocket()
        return try {
            s.broadcast = true
            s.soTimeout = timeoutMs

            val payload = "GYRECTION_DISCOVERY".toByteArray(Charsets.UTF_8)
            val broadcast = InetAddress.getByName("255.255.255.255")
            s.send(DatagramPacket(payload, payload.size, broadcast, port))

            val buf = ByteArray(256)
            val deadline = System.currentTimeMillis() + timeoutMs
            while (System.currentTimeMillis() < deadline) {
                val packet = DatagramPacket(buf, buf.size)
                try {
                    s.receive(packet)
                } catch (e: java.net.SocketTimeoutException) {
                    break
                }
                val text = String(buf, 0, packet.length, Charsets.UTF_8)
                if (text.startsWith("GYRECTION_IP ")) {
                    return text.substringAfter("GYRECTION_IP ").trim()
                }
            }
            null
        } catch (e: Exception) {
            null
        } finally {
            s.close()
        }
    }

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