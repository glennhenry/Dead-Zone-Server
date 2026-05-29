package game.socket.handler

import encore.acts.ActScope
import encore.context.ServerContext
import encore.network.fanchant.FanchantType
import encore.network.handler.FanchantHandler
import encore.network.handler.HandlerContext
import game.domain.time.TimeUpdateAct
import game.domain.time.TimeUpdateActConcept
import game.socket.protocol.NetworkMessage
import game.socket.protocol.PioFanchant
import game.socket.protocol.PioFanchantType

/**
 * Handle `ic` message by:
 *
 * 1. Do the necessary setup in server.
 *
 * Very important signal sent by client. It doesn't expects a response, so likely a one-way signal.
 * In here the game is guaranteed to be loaded (in timeline screen). So we could do some setup here.
 */
class InitCompleteHandler(private val serverContext: ServerContext) : FanchantHandler<PioFanchant> {
    // IC message is null, so only check for "ic" present
    override val fanchantType: FanchantType<PioFanchant> = PioFanchantType(NetworkMessage.INIT_COMPLETE)

    override suspend fun handle(ctx: HandlerContext<PioFanchant>): Unit = with(ctx) {
        val playerId = requireNotNull(connectionIdentity.playerId) { "PlayerId null during INIT_COMPLETE" }
        // When game init is completed, mark player as active
        serverContext.subunits.presence.markOnline(playerId)

        // send serverTime to client (required)
        serverContext.stageActDirector.run(
            act = TimeUpdateAct(),
            concept = TimeUpdateActConcept(connection),
            scope = ActScope(playerId, connection.connectionScope)
        )
    }
}
