package game.domain.items.model

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class ItemCounterType(val value: UInt)

object ItemCounterType_Constants {
    val None = game.domain.items.model.ItemCounterType(0u)
    val ZombieKills = game.domain.items.model.ItemCounterType(1u)
    val HumanKills = game.domain.items.model.ItemCounterType(2u)
    val SurvivorKills = game.domain.items.model.ItemCounterType(3u)
}
