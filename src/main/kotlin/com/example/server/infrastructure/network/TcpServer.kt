package com.example.server.infrastructure.network

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*

class TcpServer(private val port: Int) {
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private lateinit var serverSocket: ServerSocket
    private var handshakeReceived = false

    fun start() {
        scope.launch {
            val selectorManager = SelectorManager(Dispatchers.IO)
            serverSocket = aSocket(selectorManager).tcp().bind(port = port)
            println("Server is running on port $port")

            while (isActive) {  // Use `isActive` to control the loop.
                val socket = serverSocket.accept()
                println("Client connected: ${socket.remoteAddress}")

                launch {
                    handleClient(socket)
                }
            }
        }
    }

    private suspend fun handleClient(socket: Socket) {
        val input = socket.openReadChannel()
        val output = socket.openWriteChannel(autoFlush = true)

        try {
            val length = input.readVarInt()
            val packetId = input.readVarInt()

            if (packetId == 0x00) {
                val protocolVersion = input.readVarInt()
                val host = input.readString()
                val port = input.readShort()
                val nextState = input.readVarInt()

                println("Handshake received: protocolVersion=$protocolVersion, host=$host, port=$port, nextState=$nextState")
                handshakeReceived = true

                // Aquí podrías responder al cliente o manejar el siguiente estado...
            }
            output.close()
        } catch (e: Exception) {
            println(e.message)
        } finally {
            withContext(Dispatchers.IO) {
                socket.close()
            }
        }
    }

    fun stop() {
        job.cancel()  // Cancel the scope to stop the server.
    }

    fun wasHandshakeReceived(): Boolean {
        return handshakeReceived
    }
}
