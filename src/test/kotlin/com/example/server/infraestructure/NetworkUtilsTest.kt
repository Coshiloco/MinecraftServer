package com.example.server.infrastructure.network

import io.ktor.server.testing.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.test.Test
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class NetworkUtilsTest {

    @Test
    fun testReadVarInt() = testApplication {
        val buffer = BytePacketBuilder().apply {
            writeByte(0x7F)
        }.build().readBytes()
        val byteReadChannel = ByteReadChannel(buffer)

        val result = byteReadChannel.readVarInt()
        assertEquals(127, result)
    }

    @Test
    fun testReadString() = testApplication {
        val testString = "Hello, World!"
        val buffer = BytePacketBuilder().apply {
            writeVarInt(testString.length)
            writeFully(testString.toByteArray())
        }.build().readBytes()
        val byteReadChannel = ByteReadChannel(buffer)

        val result = byteReadChannel.readString()
        assertEquals(testString, result)
    }
}

// Helper function to write VarInts to a BytePacketBuilder
fun BytePacketBuilder.writeVarInt(value: Int) {
    var currentValue = value
    do {
        var temp = (currentValue and 0x7F)
        // Note: >>> means that the sign bit is shifted with the rest of the number rather than being left alone
        currentValue = currentValue ushr 7
        if (currentValue != 0) {
            temp = temp or 0x80
        }
        writeByte(temp.toByte())
    } while (currentValue != 0)
}
