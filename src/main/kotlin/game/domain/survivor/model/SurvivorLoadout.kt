package game.domain.survivor.model

import kotlinx.serialization.Serializable

@Serializable
data class SurvivorLoadout(
    val type: String,
    val survivor: Survivor,
    val weapon: SurvivorLoadoutData,
    val gearPassive: SurvivorLoadoutData,
    val gearActive: SurvivorLoadoutData,
    val supressChanges: Boolean = false
)
