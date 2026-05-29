package game.domain.model.game.data

import kotlinx.serialization.Serializable

@Serializable
data class CraftingInfo(
    val user_id: String,
    val user_name: String,
    val date: Long
)
