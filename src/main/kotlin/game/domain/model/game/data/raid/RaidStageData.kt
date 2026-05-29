package game.domain.model.game.data.raid

import game.domain.model.game.data.assignment.AssignmentStageData
import kotlinx.serialization.Serializable

@Serializable
data class RaidStageData(
    val assignmentStageData: AssignmentStageData,
    val objectiveIndex: Int,
    val objectiveState: RaidStageObjectiveState = RaidStageObjectiveState_Constants.INCOMPLETE,
    val objectiveXML: String?,  // actually an XML type
    val imageURI: String
)
