package game.domain.model.game.data.alliance

import kotlinx.serialization.Serializable

@Serializable
data class AllianceMessageList(
    val list: List<AllianceMessage> = listOf()
)
