package game.domain.model.game.data.bounty

import kotlinx.serialization.Serializable

@Serializable
data class InfectedBounty(
    val id: String,
    val completed: Boolean,
    val abandoned: Boolean,
    val viewed: Boolean,
    val rewardItemId: String,
    val issueTime: Long,
    val tasks: List<InfectedBountyTask> = listOf()
)
