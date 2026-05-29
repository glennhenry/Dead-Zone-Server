package game.domain.model.game.data.quests

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class DynamicQuestPenaltyEnum(val value: String)

object DynamicQuestPenaltyEnum_Constants {
    val morale = DynamicQuestPenaltyEnum("morale")
}
