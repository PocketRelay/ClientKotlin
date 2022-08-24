package com.jacobtread.relay.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.jacobtread.blaze.logging.BlazeLoggingHandler
import com.jacobtread.blaze.logging.PacketLogger
import com.jacobtread.blaze.packet.Packet.Companion.addPacketHandlers
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.ssl.SslContext
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.util.InsecureTrustManagerFactory
import java.security.KeyStore
import javax.net.ssl.KeyManagerFactory
import kotlin.system.exitProcess

object RedirectorServer {

    var state by mutableStateOf(AppState.INITIAL)

    var channel: Channel? = null

    var targetAddress: String = ""
    var targetPort: Int = 0
    val context = createServerSslContext()

    fun stop() {
        println("Stopping Redirector")
        state = AppState.STOPPING
        val channel = channel
        if (channel != null) {
            channel.close().addListener {
                this.channel = null
                state = AppState.INITIAL
            }
        } else {
            state = AppState.INITIAL
        }
    }

    fun start(targetAddress: String, targetPort: Int) {
        this.targetAddress = targetAddress
        this.targetPort = targetPort

        val targetAddressEncoded = getIPv4Encoded(targetAddress)
        val port = 42127
        ServerBootstrap()
            .group(NioEventLoopGroup())
            .channel(NioServerSocketChannel::class.java)
            .childHandler(object : ChannelInitializer<Channel>() {
                override fun initChannel(ch: Channel) {
                    val remoteAddress = ch.remoteAddress()
                    println("Connection at $remoteAddress to Redirector Server")
                    PacketLogger.init(object :BlazeLoggingHandler {
                        override fun debug(text: String) {
                            println(text)
                        }

                        override fun error(text: String) {
                          System.err.println(text)
                        }

                        override fun error(text: String, cause: Throwable) {
                            System.err.println(text)
                            cause.printStackTrace()
                        }

                        override fun getCommandNames(): Map<Int, String> {
                         return emptyMap()
                        }

                        override fun getComponentNames(): Map<Int, String> {
                            return emptyMap()
                        }

                        override fun getNotifyNames(): Map<Int, String> {
                            return emptyMap()
                        }

                        override fun warn(text: String) {
                            System.err.println(text)
                        }

                        override fun warn(text: String, cause: Throwable) {
                            System.err.println(text)
                            cause.printStackTrace()
                        }

                    })
                    PacketLogger.setEnabled(ch, true)
                    ch.addPacketHandlers(context)
                        .addLast(RedirectorHandler(ch, targetAddress, targetAddressEncoded, targetPort))
                }
            })
            .bind(port)
            .addListener(object : ChannelFutureListener {
                override fun operationComplete(future: ChannelFuture) {
                    channel = future.channel()
                    state = AppState.STARTED
                    println("Started Redirector")
                }
            })
    }


    private fun getIPv4Encoded(value: String): ULong {
        val ipv4Regex = Regex("^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)(\\.(?!\$)|\$)){4}\$")
        if (value.matches(ipv4Regex)) { // Check if the address is an IPv4 Address
            val ipParts = value.split('.', limit = 4) // Split the address into 4 parts
            require(ipParts.size == 4) { "Invalid IPv4 Address" } // Ensure that the address is 4 parts
            // Encoding the address as an unsigned long value
            return (ipParts[0].toULong() shl 24)
                .or(ipParts[1].toULong() shl 16)
                .or(ipParts[2].toULong() shl 8)
                .or(ipParts[3].toULong())

        }
        return 0u
    }

    private fun createServerSslContext(): SslContext {
        try {
            val keyStorePassword = charArrayOf('1', '2', '3', '4', '5', '6')
            val keyStoreStream = RedirectorServer::class.java.getResourceAsStream("/redirector.pfx")
            checkNotNull(keyStoreStream) { "Missing required keystore for SSLv3" }
            val keyStore = KeyStore.getInstance("PKCS12")
            keyStoreStream.use {
                keyStore.load(keyStoreStream, keyStorePassword)
            }
            val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
            kmf.init(keyStore, keyStorePassword)

            // Create new SSLv3 compatible context
            val context = SslContextBuilder.forServer(kmf)
                .ciphers(listOf("TLS_RSA_WITH_RC4_128_SHA", "TLS_RSA_WITH_RC4_128_MD5"))
                .protocols("SSLv3", "TLSv1.2", "TLSv1.3")
                .startTls(true)
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build()
            checkNotNull(context) { "Unable to create SSL Context" }
            return context
        } catch (e: Exception) {
            System.err.println("Failed to create SSLContext for redirector")
            e.printStackTrace()
            exitProcess(1)
        }
    }
}