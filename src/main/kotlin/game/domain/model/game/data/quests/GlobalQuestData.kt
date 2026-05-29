package game.domain.model.game.data.quests

import kotlinx.serialization.Serializable

@Serializable
data class GlobalQuestData(
    val raw: ByteArray,
    val map: Map<String, GQDataObj> = mapOf()
)
