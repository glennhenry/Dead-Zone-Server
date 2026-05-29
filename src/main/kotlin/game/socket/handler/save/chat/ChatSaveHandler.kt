package game.socket.handler.save.chat

import encore.context.ServerContext
import game.socket.handler.save.SaveSubHandler
import game.socket.protocol.SaveDataMethod
import encore.fancam.Fancam
import encore.network.transport.Connection

class ChatSaveHandler : SaveSubHandler {
    override val supportedTypes: Set<String> = SaveDataMethod.CHAT_SAVES

    override suspend fun handle(
        connection: Connection,
        type: String,
        saveId: String,
        data: Map<String, Any?>,
        serverContext: ServerContext,
        send: suspend (ByteArray) -> Unit
    ) {
        when (type) {
            SaveDataMethod.CHAT_SILENCED -> {
                Fancam.warn(tag = "save") { "Received 'CHAT_SILENCED' message [not implemented]" }
            }

            SaveDataMethod.CHAT_KICKED -> {
                Fancam.warn(tag = "save") { "Received 'CHAT_KICKED' message [not implemented]" }
            }

            SaveDataMethod.CHAT_GET_CONTACTS_AND_BLOCKS -> {
                Fancam.warn(tag = "save") { "Received 'CHAT_GET_CONTACTS_AND_BLOCKS' message [not implemented]" }
            }

            SaveDataMethod.CHAT_MIGRATE_CONTACTS_AND_BLOCKS -> {
                Fancam.warn(tag = "save") { "Received 'CHAT_MIGRATE_CONTACTS_AND_BLOCKS' message [not implemented]" }
            }

            SaveDataMethod.CHAT_ADD_CONTACT -> {
                Fancam.warn(tag = "save") { "Received 'CHAT_ADD_CONTACT' message [not implemented]" }
            }

            SaveDataMethod.CHAT_REMOVE_CONTACT -> {
                Fancam.warn(tag = "save") { "Received 'CHAT_REMOVE_CONTACT' message [not implemented]" }
            }

            SaveDataMethod.CHAT_REMOVE_ALL_CONTACTS -> {
                Fancam.warn(tag = "save") { "Received 'CHAT_REMOVE_ALL_CONTACTS' message [not implemented]" }
            }

            SaveDataMethod.CHAT_ADD_BLOCK -> {
                Fancam.warn(tag = "save") { "Received 'CHAT_ADD_BLOCK' message [not implemented]" }
            }

            SaveDataMethod.CHAT_REMOVE_BLOCK -> {
                Fancam.warn(tag = "save") { "Received 'CHAT_REMOVE_BLOCK' message [not implemented]" }
            }

            SaveDataMethod.CHAT_REMOVE_ALL_BLOCKS -> {
                Fancam.warn(tag = "save") { "Received 'CHAT_REMOVE_ALL_BLOCKS' message [not implemented]" }
            }
        }
    }
}
