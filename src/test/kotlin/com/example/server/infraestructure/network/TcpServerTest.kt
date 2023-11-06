import com.example.server.infrastructure.network.TcpServer
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.*
import kotlin.experimental.or
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.text.toByteArray

class TcpServerTest {

    @Test
    fun testServerStartAndHandleClient() = runBlocking {
        val port = 25565
        // Asumiendo que TcpServer tiene un método 'start' que no bloquea indefinidamente
        val server = TcpServer(port)
        val serverJob = launch { server.start() }

        // Dar tiempo al servidor para que inicie
        delay(1000)

        // Simular un cliente que se conecta al servidor
        val selectorManager = SelectorManager(Dispatchers.IO)
        val socket = aSocket(selectorManager).tcp().connect("localhost", port)
        val output = socket.openWriteChannel(autoFlush = true)

        // Enviar datos de handshake simulados
        val handshakePacket = createHandshakePacket()
        output.writeFully(handshakePacket)

        // Puedes añadir un delay aquí para esperar la respuesta del servidor
        delay(1000)

        // Aserción de ejemplo para verificar que se ha recibido el handshake
        // Asume que tienes una forma de verificar los datos recibidos en el servidor
        assertTrue { server.wasHandshakeReceived() }

        // Cerrar el cliente
        socket.close()
        selectorManager.close()

        // Detener el servidor
        server.stop()  // Asegúrate de implementar este método en tu servidor
        serverJob.cancelAndJoin()  // Detener la corutina del servidor
    }
}

// Función de ayuda para crear un paquete de handshake simulado
fun createHandshakePacket(): ByteArray {
    val packetId = 0x00 // ID del paquete de handshake
    val protocolVersion = 754 // Versión del protocolo de Minecraft 1.16.5
    val serverAddress = "localhost"
    val serverPort = 25565 // El puerto del servidor
    val nextState = 1 // Estado siguiente (1 para status, 2 para login)

    val bytePacketBuilder = BytePacketBuilder()
    with(bytePacketBuilder) {
        // Estructura del paquete de handshake:
        // [longitud de la siguiente parte] [ID del paquete] [versión del protocolo] [dirección del servidor] [puerto del servidor] [siguiente estado]
        writeByte(0) // Placeholder para el tamaño del paquete
        writeVarInt(packetId)
        writeVarInt(protocolVersion)
        writeString(serverAddress)
        writeShort(serverPort.toShort())
        writeVarInt(nextState)
    }

    // Calcula el tamaño del paquete y reemplaza el placeholder
    val packetBytes = bytePacketBuilder.build().readBytes()
    val sizeIndex = 0 // El índice donde el tamaño del paquete es escrito
    val packetSize = packetBytes.size - 1 // El tamaño del paquete sin contar el byte del tamaño
    val packetSizeBytes = buildPacket {
        writeVarInt(packetSize)
    }.readBytes()

    // Reemplaza el placeholder del tamaño del paquete con el tamaño correcto
    System.arraycopy(packetSizeBytes, 0, packetBytes, sizeIndex, packetSizeBytes.size)

    return packetBytes
}

// Funciones de extensión para escribir datos en BytePacketBuilder
fun BytePacketBuilder.writeVarInt(value: Int) {
    var tempValue = value
    do {
        var byteVal = (tempValue and 127).toByte()
        tempValue = tempValue ushr 7
        if (tempValue != 0) {
            byteVal = byteVal or (1 shl 7).toByte()
        }
        writeByte(byteVal)
    } while (tempValue != 0)
}

fun BytePacketBuilder.writeString(value: String) {
    val bytes = value.toByteArray(Charsets.UTF_8)
    writeVarInt(bytes.size)
    writeFully(bytes)
}
