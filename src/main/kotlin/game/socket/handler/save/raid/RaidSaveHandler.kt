package game.socket.handler.save.raid

import encore.context.ServerContext
import encore.fancam.Fancam
import encore.network.transport.Connection
import game.socket.handler.save.SaveSubHandler
import game.socket.protocol.SaveDataMethod

class RaidSaveHandler : SaveSubHandler {
    override val supportedTypes: Set<String> = SaveDataMethod.RAID_SAVES

    override suspend fun handle(
        connection: Connection,
        type: String,
        saveId: String,
        data: Map<String, Any?>,
        serverContext: ServerContext,
        send: suspend (ByteArray) -> Unit
    ) {
        when (type) {
            SaveDataMethod.RAID_START -> {
                Fancam.warn(tag = "save") { "Received 'RAID_START' message [not implemented]" }
            }

            SaveDataMethod.RAID_CONTINUE -> {
                Fancam.warn(tag = "save") { "Received 'RAID_CONTINUE' message [not implemented]" }
            }

            SaveDataMethod.RAID_ABORT -> {
                Fancam.warn(tag = "save") { "Received 'RAID_ABORT' message [not implemented]" }
            }

            SaveDataMethod.RAID_DEATH -> {
                Fancam.warn(tag = "save") { "Received 'RAID_DEATH' message [not implemented]" }
            }
        }
    }
}
