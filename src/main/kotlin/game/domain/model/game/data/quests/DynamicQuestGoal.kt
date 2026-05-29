package game.domain.model.game.data.quests

import kotlinx.serialization.Serializable

@Serializable
data class DynamicQuestGoal(
    val type: DynamicQuestGoalEnum,
    val stat: String?,
    val survivor: String?,
    val goal: Int
)
