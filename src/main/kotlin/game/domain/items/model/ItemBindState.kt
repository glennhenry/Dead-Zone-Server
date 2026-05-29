package game.domain.items.model

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class ItemBindState(val value: UInt)

object ItemBindState_Constants {
    val NotBindable = game.domain.items.model.ItemBindState(0u)
    val OnEquip = game.domain.items.model.ItemBindState(1u)
    val Bound = game.domain.items.model.ItemBindState(2u)
}
