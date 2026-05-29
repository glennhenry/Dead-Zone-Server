package game.socket.handler

import encore.context.ServerContext
import encore.network.fanchant.FanchantType
import encore.network.handler.FanchantHandler
import encore.network.handler.HandlerContext
import game.socket.protocol.NetworkMessage
import game.socket.protocol.PIOSerializer
import game.socket.protocol.PioFanchant
import game.socket.protocol.PioFanchantType

class ZombieAttackHandler(private val serverContext: ServerContext) : FanchantHandler<PioFanchant> {
    override val fanchantType: FanchantType<PioFanchant> = PioFanchantType(NetworkMessage.REQUEST_ZOMBIE_ATTACK)

    override suspend fun handle(ctx: HandlerContext<PioFanchant>): Unit = with(ctx) {
        val message = listOf(NetworkMessage.ZOMBIE_ATTACK)
        connection.write(PIOSerializer.serialize(message))
    }
}
