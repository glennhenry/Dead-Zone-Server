package game.socket.handler.save

import encore.context.ServerContext
import encore.network.transport.Connection

interface SaveSubHandler {
    val supportedTypes: Set<String>

    suspend fun handle(
        connection: Connection,
        type: String,
        saveId: String,
        data: Map<String, Any?>,
        serverContext: ServerContext,
        send: suspend (ByteArray) -> Unit
    )
}
