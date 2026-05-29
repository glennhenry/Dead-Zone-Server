package game.domain.model.game.data.bounty

import kotlinx.serialization.Serializable

@Serializable
data class InfectedBountyTask(
    val suburb: String,
    val conditions: List<InfectedBountyTaskCondition> = listOf()
)
