package game.domain.items.model

import kotlinx.serialization.Serializable

@Serializable
data class MiscEffectItem(
    val effectItem: game.domain.items.model.EffectItem
)
