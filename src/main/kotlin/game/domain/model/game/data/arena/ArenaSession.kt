package game.domain.model.game.data.arena

import game.domain.model.game.data.assignment.AssignmentStageState
import kotlinx.serialization.Serializable

@Serializable
data class ArenaSession(
    val points: Int,
    val state: Map<String, AssignmentStageState> = mapOf()
)
