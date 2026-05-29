package game.domain.compound.model

import encore.utils.identifier.Ids
import game.domain.model.game.data.TimerData
import kotlinx.serialization.Serializable

@Serializable
data class JunkBuilding(
    // from Building
    val id: String = Ids.uuid(),
    val name: String? = null,
    val type: String,
    val level: Int = 0,
    val rotation: Int = 0,
    val tx: Int = 0,
    val ty: Int = 0,
    val destroyed: Boolean = false,
    val resourceValue: Double = 0.0,
    val upgrade: TimerData? = null,
    val repair: TimerData? = null,

    // JunkBuilding-specific fields
    val items: List<game.domain.items.model.Item> = emptyList(),
    val pos: String,
    val rot: String
) : BuildingLike()
