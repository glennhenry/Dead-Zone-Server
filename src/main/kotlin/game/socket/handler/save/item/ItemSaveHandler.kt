package game.socket.handler.save.item

import encore.context.ServerContext
import game.socket.handler.save.SaveSubHandler
import game.socket.protocol.SaveDataMethod
import encore.fancam.Fancam
import encore.network.transport.Connection

class ItemSaveHandler : SaveSubHandler {
    override val supportedTypes: Set<String> = SaveDataMethod.ITEM_SAVES

    override suspend fun handle(
        connection: Connection,
        type: String,
        saveId: String,
        data: Map<String, Any?>,
        serverContext: ServerContext,
        send: suspend (ByteArray) -> Unit
    ) {
        when (type) {
            SaveDataMethod.ITEM -> {
                Fancam.warn(tag = "save") { "Received 'ITEM' message [not implemented]" }
            }

            SaveDataMethod.ITEM_BUY -> {
                Fancam.warn(tag = "save") { "Received 'ITEM_BUY' message [not implemented]" }
            }

            SaveDataMethod.ITEM_LIST -> {
                Fancam.warn(tag = "save") { "Received 'ITEM_LIST' message [not implemented]" }
            }

            SaveDataMethod.ITEM_RECYCLE -> {
                Fancam.warn(tag = "save") { "Received 'ITEM_RECYCLE' message [not implemented]" }
            }

            SaveDataMethod.ITEM_DISPOSE -> {
                Fancam.warn(tag = "save") { "Received 'ITEM_DISPOSE' message [not implemented]" }
            }

            SaveDataMethod.ITEM_CLEAR_NEW -> {
                Fancam.warn(tag = "save") { "Received 'ITEM_CLEAR_NEW' message [not implemented]" }
            }

            SaveDataMethod.ITEM_BATCH_RECYCLE -> {
                Fancam.warn(tag = "save") { "Received 'ITEM_BATCH_RECYCLE' message [not implemented]" }
            }

            SaveDataMethod.ITEM_BATCH_RECYCLE_SPEED_UP -> {
                Fancam.warn(tag = "save") { "Received 'ITEM_BATCH_RECYCLE_SPEED_UP' message [not implemented]" }
            }

            SaveDataMethod.ITEM_BATCH_DISPOSE -> {
                Fancam.warn(tag = "save") { "Received 'ITEM_BATCH_DISPOSE' message [not implemented]" }
            }
        }
    }
}
