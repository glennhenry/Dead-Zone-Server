package game.socket

import encore.network.transport.Connection
import game.socket.protocol.PIOSerializer

/**
 * Send a PIO message which:
 * - uses the message [type]
 * - variables amount of argument
 *
 * all built into list and serialized using [PIOSerializer]
 *
 * @param type The type of the message (e.g., "gr", "ic")
 * @param args Any number of message content
 */
suspend fun Connection.sendPIOMessage(type: String, vararg args: Any, logFull: Boolean = false) {
    val msg = buildList {
        add(type)
        addAll(args)
    }
    val bytes = PIOSerializer.serialize(msg)

    this.write(bytes)
}
