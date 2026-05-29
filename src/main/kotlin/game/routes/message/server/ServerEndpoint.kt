package game.routes.message.server

import encore.venue.Venue
import kotlinx.serialization.Serializable

@Serializable
data class ServerEndpoint(
    val address: String = "",
    val port: Int = 0,
) {
    companion object {
        fun socketServer(): ServerEndpoint {
            return ServerEndpoint(
                address = Venue.encore.server.host,
                port = Venue.encore.server.socketPort
            )
        }
    }
}
