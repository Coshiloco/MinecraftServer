package com.example

import com.example.plugins.*
import com.example.server.infrastructure.network.TcpServer
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    val minecraftServer = TcpServer(25800)
    minecraftServer.start()
}

fun Application.module() {
    configureSerialization()
    configureDatabases()
    configureSockets()
    configureMonitoring()
    configureRouting()
}
