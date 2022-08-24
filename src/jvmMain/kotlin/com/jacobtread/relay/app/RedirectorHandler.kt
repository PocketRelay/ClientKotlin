package com.jacobtread.relay.app

import com.jacobtread.blaze.group
import com.jacobtread.blaze.handler.PacketNettyHandler
import com.jacobtread.blaze.packet.Packet
import com.jacobtread.blaze.respond
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext

class RedirectorHandler(
    override val channel: Channel,
    private val targetAddress: String,
    private val targetAddressEncoded: ULong,
    private val targetPort: Int,
) : PacketNettyHandler() {
    private val isHostname: Boolean get() = targetAddressEncoded == 0uL

    override fun handlePacket(ctx: ChannelHandlerContext, packet: Packet) {
        println(packet)
        if (packet.component == 0x5 && packet.command == 0x1) {
            push(packet.respond {
                optional("ADDR", group("VALU") {
                    if (isHostname) {
                        text("HOST", targetAddress)
                    } else {
                        number("IP", targetAddressEncoded)
                    }
                    number("PORT", targetPort)
                })
                // Determines if SSLv3 should be used when connecting to the main server
                bool("SECU", false)
                bool("XDNS", false)
            })
        } else {
            push(packet.respond())
        }
    }

    override fun handleConnectionLost(ctx: ChannelHandlerContext) {
        val ipAddress = channel.remoteAddress()
        println("Connection to client at $ipAddress lost")
    }
}
