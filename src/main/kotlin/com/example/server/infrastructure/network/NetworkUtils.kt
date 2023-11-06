package com.example.server.infrastructure.network

import io.ktor.utils.io.*

suspend fun ByteReadChannel.readVarInt(): Int {
    var numRead = 0
    var result = 0
    var read: Byte
    val byteArray = ByteArray(1) // Array para leer un solo byte
    do {
        readAvailable(byteArray) // Lee un byte y lo almacena en el array
        read = byteArray[0] // Accede al byte leído
        val value = (read.toInt() and 0b01111111)
        result = result or (value shl (7 * numRead))
        numRead++
        if (numRead > 5) {
            throw RuntimeException("VarInt is too big")
        }
    } while ((read.toInt() and 0b10000000) != 0)

    return result
}

suspend fun ByteReadChannel.readString(): String {
    val length = readVarInt()
    val bytes = ByteArray(length)
    readFully(bytes) // Esto leerá 'length' bytes en el array 'bytes'
    return String(bytes, Charsets.UTF_8)
}
