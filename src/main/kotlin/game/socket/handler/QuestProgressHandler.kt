package game.socket.handler

import encore.network.handler.FanchantHandler
import encore.network.handler.HandlerContext
import game.socket.protocol.NetworkMessage
import game.socket.protocol.PIOSerializer
import game.socket.protocol.PioFanchant
import kotlin.reflect.KClass

/**
 * Handle `qp` message by:
 *
 * 1. Sending quest progress JSON
 *
 */
class QuestProgressHandler : FanchantHandler<PioFanchant> {
    override val fanchantType: String = NetworkMessage.QUEST_PROGRESS
    override val expectedFanchantClass: KClass<PioFanchant> = PioFanchant::class

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
