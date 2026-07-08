package game.domain.mission

import encore.acts.ActConcept
import encore.acts.StageAct
import encore.acts.choreo.BasicChoreography
import encore.acts.choreo.Choreography
import encore.acts.choreo.PerformMode
import encore.network.transport.Connection
import game.socket.protocol.NetworkMessage
import game.socket.sendPIOMessage
import kotlin.time.Duration

/**
 * This is used for:
 * - MISSION_RETURN_COMPLETE (used in MISSION_END)
 */
class MissionReturnAct : StageAct<MissionReturnActConcept> {
    override val enableLogging: Boolean = false

    override fun choreography(concept: MissionReturnActConcept): Choreography<MissionReturnActConcept> {
        return BasicChoreography(
            initialDelay = concept.returnTime,
            performMode = PerformMode.Once
        )
    }

    override suspend fun perform(concept: MissionReturnActConcept, performNumber: Int) {
        concept.connection.sendPIOMessage(NetworkMessage.MISSION_RETURN_COMPLETE, concept.missionId, logOutput = false)
    }
}

data class MissionReturnActConcept(
    val connection: Connection,
    val missionId: String,
    val returnTime: Duration
) : ActConcept
