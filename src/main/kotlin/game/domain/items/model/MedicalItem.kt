package game.domain.items.model

import kotlinx.serialization.Serializable

@Serializable
data class MedicalItem(
    val item: game.domain.items.model.Item
)
