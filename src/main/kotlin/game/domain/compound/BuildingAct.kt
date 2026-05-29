package game.domain.compound

import encore.acts.ActConcept
import encore.acts.StageAct
import encore.acts.choreo.BasicChoreography
import encore.acts.choreo.Choreography
import encore.acts.choreo.PerformMode
import encore.network.transport.Connection
import game.socket.sendPIOMessage
import kotlin.time.Duration

class BuildingAct : StageAct<BuildingActConcept> {
    override val enableLogging: Boolean = true

    override fun choreography(concept: BuildingActConcept): Choreography<BuildingActConcept> {
        return BasicChoreography(
            initialDelay = concept.buildDuration,
            performMode = PerformMode.Once
        )
    }

    override suspend fun perform(concept: BuildingActConcept, performNumber: Int) {
        concept.connection.sendPIOMessage(concept.messageType, concept.buildingId)
    }
}

data class BuildingActConcept(
    val connection: Connection,
    val messageType: String,
    val buildingId: String,
    val buildDuration: Duration
): ActConcept
