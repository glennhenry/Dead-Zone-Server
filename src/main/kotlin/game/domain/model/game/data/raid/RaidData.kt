package game.domain.model.game.data.raid

import game.domain.model.game.data.assignment.AssignmentData
import kotlinx.serialization.Serializable

@Serializable
data class RaidData(
    val assignmentData: AssignmentData,
    val ptsPerSurvivor: Int,
    val maxSurvivorMissionPoints: Int,
    val points: Int
)
