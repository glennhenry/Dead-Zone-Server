package game.socket.handler

import encore.network.handler.FanchantHandler
import encore.network.handler.HandlerContext
import game.context.ServerContext
import game.socket.protocol.NetworkMessage
import game.socket.protocol.PIOSerializer
import game.socket.protocol.PioFanchant

class ZombieAttackHandler(private val serverContext: ServerContext) : FanchantHandler<PioFanchant> {
    override val fanchantType: String = NetworkMessage.REQUEST_ZOMBIE_ATTACK

    override suspend fun handle(ctx: HandlerContext<PioFanchant>): Unit = with(ctx) {
        val message = listOf(NetworkMessage.ZOMBIE_ATTACK)
        connection.write(PIOSerializer.serialize(message))
    }
}
