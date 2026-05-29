package game.domain.model.game.data.quests

import game.domain.survivor.model.MoraleConstants
import kotlinx.serialization.Serializable

@Serializable
data class DynamicQuestPenalty(
    val type: DynamicQuestPenaltyEnum,
    val value: String, // actually string and int
    val moraleType: MoraleConstants?,  // Only if type == "morale"
)
