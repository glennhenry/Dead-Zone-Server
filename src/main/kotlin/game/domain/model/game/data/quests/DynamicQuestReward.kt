package game.domain.model.game.data.quests

import game.domain.survivor.model.MoraleConstants
import kotlinx.serialization.Serializable

@Serializable
data class DynamicQuestReward(
    val type: DynamicQuestRewardEnum,
    val value: String, // actually string and integer
    val moraleType: MoraleConstants?,  // Only if type == "morale"
)
