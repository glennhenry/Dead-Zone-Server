package game.socket.handler.save.compound.misc

import encore.context.ServerContext
import game.socket.handler.save.SaveSubHandler
import game.socket.protocol.SaveDataMethod
import encore.fancam.Fancam
import encore.network.transport.Connection

class CmpMiscSaveHandler : SaveSubHandler {
    override val supportedTypes: Set<String> = SaveDataMethod.COMPOUND_MISC_SAVES

    override suspend fun handle(
        connection: Connection,
        type: String,
        saveId: String,
        data: Map<String, Any?>,
        serverContext: ServerContext,
        send: suspend (ByteArray) -> Unit
    ) {
        when (type) {
            SaveDataMethod.CRAFT_ITEM -> {
                Fancam.warn(tag = "save") { "Received 'CRAFT_ITEM' message [not implemented]" }
            }

            SaveDataMethod.CRAFT_UPGRADE -> {
                Fancam.warn(tag = "save") { "Received 'CRAFT_UPGRADE' message [not implemented]" }
            }

            SaveDataMethod.CRAFT_SCHEMATIC -> {
                Fancam.warn(tag = "save") { "Received 'CRAFT_SCHEMATIC' message [not implemented]" }
            }

            SaveDataMethod.EFFECT_SET -> {
                Fancam.warn(tag = "save") { "Received 'EFFECT_SET' message [not implemented]" }
            }

            SaveDataMethod.RESEARCH_START -> {
                Fancam.warn(tag = "save") { "Received 'RESEARCH_START' message [not implemented]" }
            }

            SaveDataMethod.AH_EVENT -> {
                Fancam.warn(tag = "save") { "Received 'AH_EVENT' message [not implemented]" }
            }

            SaveDataMethod.CULL_NEIGHBORS -> {
                Fancam.warn(tag = "save") { "Received 'CULL_NEIGHBORS' message [not implemented]" }
            }

            SaveDataMethod.RALLY_ASSIGNMENT -> {
                Fancam.warn(tag = "save") { "Received 'RALLY_ASSIGNMENT' message [not implemented]" }
            }
        }
    }
}
