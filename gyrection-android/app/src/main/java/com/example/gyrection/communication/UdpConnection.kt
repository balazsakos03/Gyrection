package com.example.gyrection.communication

import com.example.gyrection.protocol.GyrectionPacket
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.Collections

/**
 * UDP-based connection (Wi-Fi, low-latency sensor streaming).
 *
 * `connect(host, port)` creates the sending socket and sets the target
 * (the PC's IP address). Before that, `discover()` can automatically find
 * the PC on the local network using a broadcast message — so the user
 * never has to type an IP address.
 */
class UdpConnection : Connection {

    private var socket: DatagramSocket? = null
    private var address: InetAddress? = null
    private var port: Int = 0

    /**
     * Finds the PC on the network: sends a broadcast "GYRECTION_DISCOVERY"
     * message and waits for the reply, in which the PC sends its IP address.
     *
     * @return the found PC IP address, or null if there was no reply within the timeout
     */
    fun discover(port: Int, timeoutMs: Int = 2000): String? {
        val s = DatagramSocket()
        return try {
            s.broadcast = true
            s.soTimeout = timeoutMs

            val payload = "GYRECTION_DISCOVERY".toByteArray(Charsets.UTF_8)

            // 1) Limited broadcast – a legtöbb esetben ez elég
            val broadcast = InetAddress.getByName("255.255.255.255")
            s.send(DatagramPacket(payload, payload.size, broadcast, port))

            // 2) Minden aktív interfész subnet-broadcast címére is (ír a USB-tethering,
            //    VPN, illetve több hálózati interfész esetén a limited broadcast
            //    csak az alapértelmezett útvonalra megy ki, így az nem mindig célba ér)
            try {
                NetworkInterface.getNetworkInterfaces()?.let { enums ->
                    Collections.list(enums).forEach { nif ->
                        if (nif.isUp && !nif.isLoopback) {
                            nif.interfaceAddresses?.firstOrNull { it.broadcast != null }?.let { ia ->
                                ia.broadcast?.let { bc ->
                                    s.send(DatagramPacket(payload, payload.size, bc, port))
                                }
                            }
                        }
                    }
                }
            } catch (ignore: Exception) {
                // An interface-listing failure should not abort the discovery
            }

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
        // Wrong IP, no network, etc.
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