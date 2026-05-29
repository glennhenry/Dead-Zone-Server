package game.socket.handler.save.bounty

import encore.context.ServerContext
import game.socket.handler.save.SaveSubHandler
import game.socket.protocol.SaveDataMethod
import encore.fancam.Fancam
import encore.network.transport.Connection

class BountySaveHandler : SaveSubHandler {
    override val supportedTypes: Set<String> = SaveDataMethod.BOUNTY_SAVES

    override suspend fun handle(
        connection: Connection,
        type: String,
        saveId: String,
        data: Map<String, Any?>,
        serverContext: ServerContext,
        send: suspend (ByteArray) -> Unit
    ) {
        when (type) {
            SaveDataMethod.BOUNTY_VIEW -> {
                Fancam.warn(tag = "save") { "Received 'BOUNTY_VIEW' message [not implemented]" }
            }

            SaveDataMethod.BOUNTY_SPEED_UP -> {
                Fancam.warn(tag = "save") { "Received 'BOUNTY_SPEED_UP' message [not implemented]" }
            }

            SaveDataMethod.BOUNTY_NEW -> {
                Fancam.warn(tag = "save") { "Received 'BOUNTY_NEW' message [not implemented]" }
            }

            SaveDataMethod.BOUNTY_ABANDON -> {
                Fancam.warn(tag = "save") { "Received 'BOUNTY_ABANDON' message [not implemented]" }
            }

            SaveDataMethod.BOUNTY_ADD -> {
                Fancam.warn(tag = "save") { "Received 'BOUNTY_ADD' message [not implemented]" }
            }
        }
    }
}
