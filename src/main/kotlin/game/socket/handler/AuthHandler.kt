package game.socket.handler

import encore.fancam.Fancam
import encore.network.handler.FanchantHandler
import encore.network.handler.HandlerContext
import game.socket.protocol.PioFanchant

/**
 * Auth message is send after game ready message.
 * 'auth' contains MD5 hash produced from hashing all binaries sent in the join message.
 */
class AuthHandler : FanchantHandler<PioFanchant> {
    override val fanchantType: String = "auth"

    override suspend fun handle(ctx: HandlerContext<PioFanchant>) {
        Fancam.info { "Received auth message, ignoring." }
    }
}
