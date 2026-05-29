package game.socket.handler.save.arena

import encore.context.ServerContext
import game.socket.handler.save.SaveSubHandler
import game.socket.protocol.SaveDataMethod
import encore.fancam.Fancam
import encore.network.transport.Connection

class ArenaSaveHandler : SaveSubHandler {
    override val supportedTypes: Set<String> = SaveDataMethod.ARENA_SAVES

    override suspend fun handle(
        connection: Connection,
        type: String,
        saveId: String,
        data: Map<String, Any?>,
        serverContext: ServerContext,
        send: suspend (ByteArray) -> Unit
    ) {
        when (type) {
            SaveDataMethod.ARENA_START -> {
                Fancam.warn(tag = "save") { "Received 'ARENA_START' message [not implemented]" }
            }

            SaveDataMethod.ARENA_CONTINUE -> {
                Fancam.warn(tag = "save") { "Received 'ARENA_CONTINUE' message [not implemented]" }
            }

            SaveDataMethod.ARENA_FINISH -> {
                Fancam.warn(tag = "save") { "Received 'ARENA_FINISH' message [not implemented]" }
            }

            SaveDataMethod.ARENA_ABORT -> {
                Fancam.warn(tag = "save") { "Received 'ARENA_ABORT' message [not implemented]" }
            }

            SaveDataMethod.ARENA_DEATH -> {
                Fancam.warn(tag = "save") { "Received 'ARENA_DEATH' message [not implemented]" }
            }

            SaveDataMethod.ARENA_UPDATE -> {
                Fancam.warn(tag = "save") { "Received 'ARENA_UPDATE' message [not implemented]" }
            }

            SaveDataMethod.ARENA_LEADER -> {
                Fancam.warn(tag = "save") { "Received 'ARENA_LEADER' message [not implemented]" }
            }

            SaveDataMethod.ARENA_LEADERBOARD -> {
                Fancam.warn(tag = "save") { "Received 'ARENA_LEADERBOARD' message [not implemented]" }
            }
        }
    }
}
