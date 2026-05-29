package game.socket.handler

import encore.network.fanchant.FanchantType
import encore.network.handler.FanchantHandler
import encore.network.handler.HandlerContext
import game.socket.protocol.NetworkMessage
import game.socket.protocol.PIOSerializer
import game.socket.protocol.PioFanchant
import game.socket.protocol.PioFanchantType

/**
 * Handle `qp` message by:
 *
 * 1. Sending quest progress JSON
 *
 */
class QuestProgressHandler : FanchantHandler<PioFanchant> {
    override val fanchantType: FanchantType<PioFanchant> = PioFanchantType(NetworkMessage.QUEST_PROGRESS)

    override suspend fun handle(ctx: HandlerContext<PioFanchant>): Unit = with(ctx) {
        val message = listOf(NetworkMessage.QUEST_PROGRESS, questProgressJson.trimIndent())
        connection.write(PIOSerializer.serialize(message))
    }
}

const val questProgressJson = """
{
  "complete": null,
  "progress": null
}
"""
