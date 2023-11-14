package com.example.server.infrastructure.network

enum class ConnectionState {
    HANDSHAKING, STATUS, LOGIN, PLAY
}

interface Packet {
    val id: Int
}