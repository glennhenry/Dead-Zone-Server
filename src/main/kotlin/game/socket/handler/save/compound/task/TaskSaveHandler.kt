package game.socket.handler.save.compound.task

import encore.context.ServerContext
import game.socket.handler.save.SaveSubHandler
import game.socket.protocol.SaveDataMethod
import encore.fancam.Fancam
import encore.network.transport.Connection

class TaskSaveHandler : SaveSubHandler {
    override val supportedTypes: Set<String> = SaveDataMethod.COMPOUND_TASK_SAVES

    override suspend fun handle(
        connection: Connection,
        type: String,
        saveId: String,
        data: Map<String, Any?>,
        serverContext: ServerContext,
        send: suspend (ByteArray) -> Unit
    ) {
        when (type) {
            SaveDataMethod.TASK_STARTED -> {
                Fancam.warn(tag = "save") { "Received 'TASK_STARTED' message [not implemented]" }
            }

            SaveDataMethod.TASK_CANCELLED -> {
                Fancam.warn(tag = "save") { "Received 'TASK_CANCELLED' message [not implemented]" }
            }

            SaveDataMethod.TASK_SURVIVOR_ASSIGNED -> {
                Fancam.warn(tag = "save") { "Received 'TASK_SURVIVOR_ASSIGNED' message [not implemented]" }
            }

            SaveDataMethod.TASK_SURVIVOR_REMOVED -> {
                Fancam.warn(tag = "save") { "Received 'TASK_SURVIVOR_REMOVED' message [not implemented]" }
            }

            SaveDataMethod.TASK_SPEED_UP -> {
                Fancam.warn(tag = "save") { "Received 'TASK_SPEED_UP' message [not implemented]" }
            }
        }
    }
}
