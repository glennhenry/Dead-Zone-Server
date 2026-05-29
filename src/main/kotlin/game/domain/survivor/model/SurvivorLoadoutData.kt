package game.domain.survivor.model

import game.domain.items.model.Item
import kotlinx.serialization.Serializable

@Serializable
data class SurvivorLoadoutData(
    val type: String,
    val item: Item,
    val quantity: Int,
    val loadout: SurvivorLoadout
)
