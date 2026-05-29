package game.socket.handler.save.misc

import encore.context.ServerContext
import game.socket.handler.save.SaveSubHandler
import game.socket.protocol.SaveDataMethod
import encore.fancam.Fancam
import encore.network.transport.Connection

class MiscSaveHandler : SaveSubHandler {
    override val supportedTypes: Set<String> = SaveDataMethod.MISC_SAVES

    override suspend fun handle(
        connection: Connection,
        type: String,
        saveId: String,
        data: Map<String, Any?>,
        serverContext: ServerContext,
        send: suspend (ByteArray) -> Unit
    ) {
        when (type) {
            SaveDataMethod.TUTORIAL_PVP_PRACTICE -> {
                Fancam.warn(tag = "save") { "Received 'TUTORIAL_PVP_PRACTICE' message [not implemented]" }
            }

            SaveDataMethod.TUTORIAL_COMPLETE -> {
                Fancam.warn(tag = "save") { "Received 'TUTORIAL_COMPLETE' message [not implemented]" }
            }

            SaveDataMethod.GET_OFFERS -> {
                Fancam.warn(tag = "save") { "Received 'GET_OFFERS' message [not implemented]" }
            }

            SaveDataMethod.NEWS_READ -> {
                Fancam.warn(tag = "save") { "Received 'NEWS_READ' message [not implemented]" }
            }

            SaveDataMethod.CLEAR_NOTIFICATIONS -> {
                Fancam.warn(tag = "save") { "Received 'CLEAR_NOTIFICATIONS' message [not implemented]" }
            }

            SaveDataMethod.FLUSH_PLAYER -> {
                Fancam.warn(tag = "save") { "Received 'FLUSH_PLAYER' message [not implemented]" }
            }

            SaveDataMethod.SAVE_ALT_IDS -> {
                Fancam.warn(tag = "save") { "Received 'SAVE_ALT_IDS' message [not implemented]" }
            }

            SaveDataMethod.TRADE_DO_TRADE -> {
                Fancam.warn(tag = "save") { "Received 'TRADE_DO_TRADE' message [not implemented]" }
            }

            SaveDataMethod.GET_INVENTORY_SIZE -> {
                Fancam.warn(tag = "save") { "Received 'GET_INVENTORY_SIZE' message [not implemented]" }
            }
        }
    }
}
