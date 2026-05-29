package game.domain.model.game.data.task

import game.domain.compound.model.JunkBuilding
import game.domain.model.game.data.Task
import kotlinx.serialization.Serializable

@Serializable
data class JunkRemovalTask(
    val task: Task,
    val target: JunkBuilding,
    val targetId: String,  // obtained from buildingId
    val buildingId: String,
    val xp: Int = 0
)
