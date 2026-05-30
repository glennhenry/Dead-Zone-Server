package game.domain.time

import encore.acts.ActConcept
import encore.acts.StageAct
import encore.acts.choreo.BasicChoreography
import encore.acts.choreo.Choreography
import encore.acts.choreo.PerformMode
import encore.network.transport.Connection
import encore.time.TimeCenter
import game.socket.protocol.NetworkMessage
import game.socket.sendPIOMessage
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Sends a time update ('tu') message to client.
 *
 * The game registers callback for such message, though not sure how frequent should we send the message.
 */
class TimeUpdateAct : StageAct<TimeUpdateActConcept> {
    override val enableLogging: Boolean = false

    override fun choreography(concept: TimeUpdateActConcept): Choreography<TimeUpdateActConcept> {
        return BasicChoreography(
            initialDelay = 0.milliseconds,
            performMode = PerformMode.Forever(1.seconds)
        )
    }

    override suspend fun perform(concept: TimeUpdateActConcept, performNumber: Int) {
        concept.connection.sendPIOMessage(NetworkMessage.TIME_UPDATE, TimeCenter.game.nowInDouble(), false)
    }
}

data class TimeUpdateActConcept(
    val connection: Connection
) : ActConcept
