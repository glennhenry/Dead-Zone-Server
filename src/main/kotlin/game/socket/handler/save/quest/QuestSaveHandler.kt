package game.socket.handler.save.quest

import encore.context.ServerContext
import game.socket.handler.save.SaveSubHandler
import game.socket.protocol.SaveDataMethod

import encore.fancam.Fancam
import encore.network.transport.Connection

class QuestSaveHandler : SaveSubHandler {
    override val supportedTypes: Set<String> = SaveDataMethod.QUEST_SAVES

    override suspend fun handle(
        connection: Connection,
        type: String,
        saveId: String,
        data: Map<String, Any?>,
        serverContext: ServerContext,
        send: suspend (ByteArray) -> Unit
    ) {
        when (type) {
            SaveDataMethod.QUEST_COLLECT -> {
                Fancam.warn(tag = "save") { "Received 'QUEST_COLLECT' message [not implemented]" }
            }

            SaveDataMethod.QUEST_TRACK -> {
                Fancam.warn(tag = "save") { "Received 'QUEST_TRACK' message [not implemented]" }
            }

            SaveDataMethod.QUEST_UNTRACK -> {
                Fancam.warn(tag = "save") { "Received 'QUEST_UNTRACK' message [not implemented]" }
            }

            SaveDataMethod.QUEST_DAILY_DECLINE -> {
                Fancam.warn(tag = "save") { "Received 'QUEST_DAILY_DECLINE' message [not implemented]" }
            }

            SaveDataMethod.QUEST_DAILY_ACCEPT -> {
                Fancam.warn(tag = "save") { "Received 'QUEST_DAILY_ACCEPT' message [not implemented]" }
            }

            SaveDataMethod.REPEAT_ACHIEVEMENT -> {
                Fancam.warn(tag = "save") { "Received 'REPEAT_ACHIEVEMENT' message [not implemented]" }
            }

            SaveDataMethod.GLOBAL_QUEST_COLLECT -> {
                Fancam.warn(tag = "save") { "Received 'GLOBAL_QUEST_COLLECT' message [not implemented]" }
            }
        }
    }
}
