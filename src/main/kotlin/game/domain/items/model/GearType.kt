package game.domain.items.model

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class GearType(val value: UInt)

object GearType_Constants {
    val UNKNOWN = game.domain.items.model.GearType(0u)
    val PASSIVE = game.domain.items.model.GearType(1u)
    val ACTIVE = game.domain.items.model.GearType(2u)
    val CONSUMABLE = game.domain.items.model.GearType(4u)
    val EXPLOSIVE = game.domain.items.model.GearType(8u)
    val IMPROVISED = game.domain.items.model.GearType(16u)
}
