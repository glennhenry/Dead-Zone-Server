package game.socket.handler

import game.context.ServerContext
import encore.network.handler.FanchantHandler
import encore.network.handler.HandlerContext
import game.socket.protocol.NetworkMessage
import game.socket.protocol.PIOSerializer
import game.socket.protocol.PioFanchant
import kotlin.reflect.KClass

class ZombieAttackHandler(private val serverContext: ServerContext) : FanchantHandler<PioFanchant> {
    override val fanchantType: String = NetworkMessage.REQUEST_ZOMBIE_ATTACK
    override val expectedFanchantClass: KClass<PioFanchant> = PioFanchant::class

    override suspend fun handle(ctx: HandlerContext<PioFanchant>): Unit = with(ctx) {
        val message = listOf(NetworkMessage.ZOMBIE_ATTACK)
        connection.write(PIOSerializer.serialize(message))
    }
}
